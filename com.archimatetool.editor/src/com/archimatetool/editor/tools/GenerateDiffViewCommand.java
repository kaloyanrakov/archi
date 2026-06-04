package com.archimatetool.editor.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IArchimateDiagramModel;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IDiagramModelArchimateObject;
import com.archimatetool.model.IDiagramModelArchimateConnection;
import com.archimatetool.model.IDiagramModelConnection;
import com.archimatetool.model.IDiagramModelContainer;
import com.archimatetool.model.IDiagramModelObject;
import com.archimatetool.model.IFolder;
import java.util.Set;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimateRelationship;
/**
 * Command that generates a diff view between two ArchiMate diagram views.
 *
 * Visual encoding:
 *   - Removed elements (in A, not in B): alpha = 80  (faded/ghosted)
 *   - Added elements   (in B, not in A): alpha = 255 (normal, optionally tinted green via fillColor)
 *   - Unchanged        (in both)       : alpha = 255 (normal)
 */
public class GenerateDiffViewCommand extends Command {

    /** Alpha value for "removed" elements — roughly 30% opacity */
    private static final int REMOVED_ALPHA = 80;
    private static final String REMOVED_LINE_COLOR = "#AAAAAA"; //$NON-NLS-1$

    /** Fill colour for "added" elements expressed as RGB hex string — pale green */
    private static final String ADDED_FILL_COLOR = "#AAFFAA"; //$NON-NLS-1$

    private final IArchimateDiagramModel viewA;
    private IArchimateDiagramModel viewB;
    private IArchimateDiagramModel diffView;
    private IFolder targetFolder;

    public GenerateDiffViewCommand(IArchimateDiagramModel viewA) {
        super("Generate Diff View");
        this.viewA = viewA;
    }

    /**
     * Open a dialog so the user can choose viewB.
     * Returns true if the user confirmed a selection.
     */
    public boolean openDialog(Shell shell) {
        // Use only reachable (connected) views instead of all views in the model
        List<IDiagramModel> candidates = new ArrayList<>(
            com.archimatetool.editor.propertysections.IterationPropertyDecorator.getAllReachableViews(viewA)
        );

        if (candidates.isEmpty()) {
            org.eclipse.jface.dialogs.MessageDialog.openInformation(shell,
                "Compare Views", "No connected views found to compare against.");
            return false;
        }

        SelectViewDialog dialog = new SelectViewDialog(shell, candidates);
        if (dialog.open() != Dialog.OK || dialog.getSelectedView() == null) {
            return false;
        }

        viewB = (IArchimateDiagramModel) dialog.getSelectedView();
        targetFolder = (IFolder) viewA.eContainer();
        return true;
    }

    @Override
    public void execute() {
        Map<String, IDiagramModelArchimateObject> baseElems   = collectElementsById(viewA);
        Map<String, IDiagramModelArchimateObject> targetElems = collectElementsById(viewB);

        diffView = IArchimateFactory.eINSTANCE.createArchimateDiagramModel();
        diffView.setName("Diff: " + viewA.getName() + " \u2192 " + viewB.getName()); //$NON-NLS-1$
        targetFolder.getElements().add(diffView);

        // Step 1: Copy elements from viewA preserving exact positions
        // Map from original viewA object -> copy in diffView
        Map<IDiagramModelArchimateObject, IDiagramModelArchimateObject> srcToCopy = new java.util.LinkedHashMap<>();

        for(IDiagramModelObject child : viewA.getChildren()) {
            if(child instanceof IDiagramModelArchimateObject dmo) {
                IDiagramModelArchimateObject copy = copyDmo(dmo, dmo.getBounds().getX(), dmo.getBounds().getY());
                com.archimatetool.model.IArchimateElement el = dmo.getArchimateElement();
                if(el != null && !targetElems.containsKey(el.getId())) {
                    copy.setFillColor("#FFCCCC"); //$NON-NLS-1$
                }
                diffView.getChildren().add(copy);
                srcToCopy.put(dmo, copy);
            }
        }

        // Step 2: Recreate connections from viewA, deduplicated by relationship ID
        Set<String> addedRelIds = new java.util.HashSet<>();
        for(IDiagramModelArchimateObject srcDmo : srcToCopy.keySet()) {
            for(IDiagramModelConnection conn : srcDmo.getSourceConnections()) {
                if(conn instanceof IDiagramModelArchimateConnection dmac) {
                    IArchimateRelationship rel = dmac.getArchimateRelationship();
                    if(rel == null || !addedRelIds.add(rel.getId())) continue;
                    IDiagramModelArchimateObject copySrc = srcToCopy.get(conn.getSource());
                    IDiagramModelArchimateObject copyTgt = srcToCopy.get(conn.getTarget());
                    if(copySrc != null && copyTgt != null) {
                        IDiagramModelArchimateConnection newConn =
                            IArchimateFactory.eINSTANCE.createDiagramModelArchimateConnection();
                        newConn.setArchimateRelationship(rel);
                        newConn.connect(copySrc, copyTgt);
                    }
                }
            }
        }

        // Step 3: Add elements only in target (added) below — light green
        // Also build a map from viewB element ID -> copy in diffView for connection wiring
        int maxY = diffView.getChildren().stream()
            .mapToInt(c -> c.getBounds().getY() + Math.max(c.getBounds().getHeight(), 0))
            .max().orElse(0) + 40;

        final int CELL_W = 160, CELL_H = 80, PADDING = 20, COLS = 5;
        int col = 0, row = 0;

        // Map from viewB object -> copy in diffView (for added elements)
        Map<IDiagramModelArchimateObject, IDiagramModelArchimateObject> srcToCopyB = new java.util.LinkedHashMap<>();

        for(Map.Entry<String, IDiagramModelArchimateObject> e : targetElems.entrySet()) {
            if(!baseElems.containsKey(e.getKey())) {
                IDiagramModelArchimateObject copy = copyDmo(e.getValue(),
                    PADDING + col * CELL_W, maxY + row * CELL_H);
                copy.setFillColor("#CCFFCC"); //$NON-NLS-1$
                diffView.getChildren().add(copy);
                srcToCopyB.put(e.getValue(), copy);
                if(++col >= COLS) { col = 0; row++; }
            }
        }

        // Step 4: Wire connections from viewB for added elements
        // Build a combined element-ID -> diffView copy map for lookup
        Map<String, IDiagramModelArchimateObject> diffById = new java.util.LinkedHashMap<>();
        for(Map.Entry<IDiagramModelArchimateObject, IDiagramModelArchimateObject> e : srcToCopy.entrySet()) {
            com.archimatetool.model.IArchimateElement el = e.getKey().getArchimateElement();
            if(el != null) diffById.put(el.getId(), e.getValue());
        }
        for(Map.Entry<IDiagramModelArchimateObject, IDiagramModelArchimateObject> e : srcToCopyB.entrySet()) {
            com.archimatetool.model.IArchimateElement el = e.getKey().getArchimateElement();
            if(el != null) diffById.put(el.getId(), e.getValue());
        }

        for(IDiagramModelObject child : viewB.getChildren()) {
            if(!(child instanceof IDiagramModelArchimateObject srcDmo)) continue;
            for(IDiagramModelConnection conn : srcDmo.getSourceConnections()) {
                if(!(conn instanceof IDiagramModelArchimateConnection dmac)) continue;
                IArchimateRelationship rel = dmac.getArchimateRelationship();
                if(rel == null || !addedRelIds.add(rel.getId())) continue; // skip already added
                String srcId = rel.getSource() != null ? rel.getSource().getId() : null;
                String tgtId = rel.getTarget() != null ? rel.getTarget().getId() : null;
                if(srcId == null || tgtId == null) continue;
                IDiagramModelArchimateObject copySrc = diffById.get(srcId);
                IDiagramModelArchimateObject copyTgt = diffById.get(tgtId);
                if(copySrc != null && copyTgt != null) {
                    IDiagramModelArchimateConnection newConn =
                        IArchimateFactory.eINSTANCE.createDiagramModelArchimateConnection();
                    newConn.setArchimateRelationship(rel);
                    newConn.connect(copySrc, copyTgt);
                }
            }
        }

        com.archimatetool.editor.ui.services.EditorManager.openDiagramEditor(diffView, false);
    }

    private Map<String, IDiagramModelArchimateObject> collectElementsById(IDiagramModel view) {
        Map<String, IDiagramModelArchimateObject> map = new java.util.LinkedHashMap<>();
        for(IDiagramModelObject child : view.getChildren()) {
            if(child instanceof IDiagramModelArchimateObject dmo) {
                com.archimatetool.model.IArchimateElement el = dmo.getArchimateElement();
                if(el != null && el.getId() != null) {
                    map.put(el.getId(), dmo);
                }
            }
        }
        return map;
    }

    private IDiagramModelArchimateObject copyDmo(IDiagramModelArchimateObject src, int x, int y) {
        IDiagramModelArchimateObject copy = IArchimateFactory.eINSTANCE.createDiagramModelArchimateObject();
        copy.setArchimateElement(src.getArchimateElement());
        int w = src.getBounds().getWidth()  > 0 ? src.getBounds().getWidth()  : 120;
        int h = src.getBounds().getHeight() > 0 ? src.getBounds().getHeight() : 55;
        copy.setBounds(x, y, w, h);
        copy.setType(src.getType());
        copy.setTextAlignment(src.getTextAlignment());
        copy.setTextPosition(src.getTextPosition());
        copy.setFillColor(src.getFillColor());
        return copy;
    }

    @Override
    public void undo() {
        if (diffView != null) {
            targetFolder.getElements().remove(diffView);
        }
    }

    @Override
    public void redo() {
        if (diffView != null) {
            targetFolder.getElements().add(diffView);
        }
    }


    private static class SelectViewDialog extends Dialog {
        private final List<IDiagramModel> views;
        private IDiagramModel selectedView;
        private TableViewer tableViewer;

        SelectViewDialog(Shell shell, List<IDiagramModel> views) {
            super(shell);
            this.views = views;
        }

        @Override
        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            shell.setText("Compare with...");
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);
            composite.setLayout(new GridLayout(1, false));

            new Label(composite, SWT.NONE).setText("Select the view to compare against:");

            tableViewer = new TableViewer(composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
            tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            tableViewer.setContentProvider(ArrayContentProvider.getInstance());
            tableViewer.setLabelProvider(new LabelProvider() {
                @Override
                public String getText(Object element) {
                    return element instanceof IDiagramModel ? ((IDiagramModel) element).getName() : "";
                }
            });
            tableViewer.setInput(views);

            // Double-click to confirm
            tableViewer.addDoubleClickListener(e -> {
                okPressed();
            });

            return composite;
        }

        @Override
        protected void okPressed() {
            if (!tableViewer.getStructuredSelection().isEmpty()) {
                selectedView = (IDiagramModel) tableViewer.getStructuredSelection().getFirstElement();
            }
            super.okPressed();
        }

        IDiagramModel getSelectedView() {
            return selectedView;
        }
    }
}