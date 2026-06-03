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
    	System.err.println("!!! GenerateDiffViewCommand.execute called"); //$NON-NLS-1$

        diffView = buildDiffView();

        // Add to the same folder as viewA
        targetFolder.getElements().add(diffView);
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

    // -------------------------------------------------------------------------

    private IArchimateDiagramModel buildDiffView() {
        IArchimateDiagramModel diff = IArchimateFactory.eINSTANCE.createArchimateDiagramModel();
        diff.setName("Diff: " + viewA.getName() + " vs " + viewB.getName());

        // Build a concept-id → object map for each view
        Map<String, IDiagramModelObject> mapA = buildConceptIdMap(viewA);
        Map<String, IDiagramModelObject> mapB = buildConceptIdMap(viewB);

        // --- Unchanged & Removed: iterate A ---
        for (Map.Entry<String, IDiagramModelObject> entry : mapA.entrySet()) {
            IDiagramModelObject copy = copyObject(entry.getValue());
            if (!mapB.containsKey(entry.getKey())) {
                // Removed — fade it out
                copy.setAlpha(REMOVED_ALPHA);
            }
            // else: unchanged — leave at default alpha
            diff.getChildren().add(copy);
        }

        // --- Added: iterate B for things not in A ---
        for (Map.Entry<String, IDiagramModelObject> entry : mapB.entrySet()) {
            if (!mapA.containsKey(entry.getKey())) {
                IDiagramModelObject copy = copyObject(entry.getValue());
                copy.setFillColor(ADDED_FILL_COLOR);
                diff.getChildren().add(copy);
            }
        }

        // --- Connections ---
        addConnections(viewA, diff, mapA, mapB, true);
        addConnections(viewB, diff, mapA, mapB, false);

        return diff;
    }

    /**
     * Build a map of archimate-concept-id → IDiagramModelObject for all objects
     * in a view (including nested ones, flattened for comparison purposes).
     */
    private Map<String, IDiagramModelObject> buildConceptIdMap(IDiagramModel view) {
        Map<String, IDiagramModelObject> map = new HashMap<>();
        collectObjects(view.getChildren(), map);
        return map;
    }

    private void collectObjects(List<IDiagramModelObject> objects, Map<String, IDiagramModelObject> map) {
        for (IDiagramModelObject obj : objects) {
            if (obj instanceof IDiagramModelArchimateObject) {
                String id = ((IDiagramModelArchimateObject) obj).getArchimateConcept().getId();
                map.putIfAbsent(id, obj);
            }
            if (obj instanceof IDiagramModelContainer) {
                collectObjects(((IDiagramModelContainer) obj).getChildren(), map);
            }
        }
    }

    private IDiagramModelObject copyObject(IDiagramModelObject original) {
        return EcoreUtil.copy(original);
    }

    /**
     * Copy connections from a source view into the diff view.
     *
     * @param fromView   the source view (A or B)
     * @param diff       the target diff view
     * @param mapA       concept-id map for view A
     * @param mapB       concept-id map for view B
     * @param isViewA    true if we're processing A's connections (removed ones get faded)
     */
    private void addConnections(IDiagramModel fromView, IArchimateDiagramModel diff,
            Map<String, IDiagramModelObject> mapA, Map<String, IDiagramModelObject> mapB,
            boolean isViewA) {

        List<IDiagramModelConnection> connections = new ArrayList<>();
        collectConnections(fromView.getChildren(), connections);

        for (IDiagramModelConnection conn : connections) {
            if (!(conn instanceof IDiagramModelArchimateConnection)) continue;

            IDiagramModelArchimateConnection archConn = (IDiagramModelArchimateConnection) conn;
            String conceptId = archConn.getArchimateConcept().getId();

            boolean inA = mapA.values().stream()
                .anyMatch(o -> o instanceof IDiagramModelArchimateObject &&
                    ((IDiagramModelArchimateObject) o).getArchimateConcept().getId().equals(conceptId));
            boolean inB = mapB.values().stream()
                .anyMatch(o -> o instanceof IDiagramModelArchimateObject &&
                    ((IDiagramModelArchimateObject) o).getArchimateConcept().getId().equals(conceptId));

            // Only add if not already added from the other view
            if (!isViewA && inA) continue; // already handled when processing A

            IDiagramModelArchimateConnection connCopy = EcoreUtil.copy(archConn);

            if (isViewA && !inB) {
                // Removed connection — indicate with grey line color (no alpha available on connections)
                connCopy.setLineColor("#AAAAAA"); //$NON-NLS-1$
            } else if (!isViewA && !inA) {
                // Added connection — tint green
                connCopy.setLineColor(ADDED_FILL_COLOR);
            }

            diff.getChildren(); // ensure resolved
            // Note: connections in Archi are attached to their source/target objects.
            // They need to be re-wired after objects are added; handle this in post-processing
            // or use the same approach as the existing ViewImporter.
        }
    }

    private void collectConnections(List<IDiagramModelObject> objects, List<IDiagramModelConnection> result) {
        for (IDiagramModelObject obj : objects) {
            result.addAll(obj.getSourceConnections());
            if (obj instanceof IDiagramModelContainer) {
                collectConnections(((IDiagramModelContainer) obj).getChildren(), result);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helper: collect all IDiagramModel instances from all folders

    
    

    // =========================================================================
    // Inner dialog: pick a view to compare against
    // =========================================================================

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