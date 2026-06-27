package com.archimatetool.editor.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.archimatetool.model.IDiagramModelGroup;

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
     // Step 1: Copy ALL elements from viewA recursively, using absolute positions
        Map<IDiagramModelArchimateObject, IDiagramModelArchimateObject> srcToCopy = new java.util.LinkedHashMap<>();
        copyViewRecursive(viewA.getChildren(), diffView, targetElems, srcToCopy);

       

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

     // Step 3: Two-pass — groups first, then root items below everything
        int maxY = getMaxYRecursive(diffView.getChildren(), 0) + 40;
        final int CELL_W = 160, CELL_H = 80, PADDING = 20, COLS = 5;

        Map<String, IDiagramModelContainer> diffGroupsByName = new java.util.LinkedHashMap<>();
        collectGroupsByName(diffView.getChildren(), diffGroupsByName);

        Map<IDiagramModelArchimateObject, IDiagramModelArchimateObject> srcToCopyB = new java.util.LinkedHashMap<>();
        Map<IDiagramModelContainer, int[]> groupCounters = new java.util.LinkedHashMap<>();

        // First pass: add groups and their contents, collect new root items separately
        List<IDiagramModelArchimateObject> newRootDmos = new java.util.ArrayList<>();
        addNewElementsFromViewB(viewB.getChildren(), diffView, diffGroupsByName, baseElems,
            srcToCopyB, newRootDmos, groupCounters, CELL_W, CELL_H, PADDING, COLS);

        // Second pass: now that all groups are placed, calculate true final maxY
        int finalMaxY = getMaxYRecursive(diffView.getChildren(), 0) + 40;
        int col = 0, row = 0;
        for(IDiagramModelArchimateObject dmo : newRootDmos) {
            IDiagramModelArchimateObject copy = copyDmo(dmo,
                PADDING + col * CELL_W, finalMaxY + row * CELL_H);
            copy.setFillColor(darkenColor(dmo));   // in addNewElementsFromViewB
            diffView.getChildren().add(copy);
            srcToCopyB.put(dmo, copy);
            if(++col >= COLS) { col = 0; row++; }
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

        for(IDiagramModelArchimateObject srcDmo : collectAllDmos(viewB)) {
            for(IDiagramModelConnection conn : srcDmo.getSourceConnections()) {
                if(!(conn instanceof IDiagramModelArchimateConnection dmac)) continue;
                IArchimateRelationship rel = dmac.getArchimateRelationship();
                if(rel == null || !addedRelIds.add(rel.getId())) continue;
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
        nudgeOverlappingRootItems(diffView);
        com.archimatetool.editor.ui.services.EditorManager.openDiagramEditor(diffView, false);
    }
    private void expandGroupToFit(IDiagramModelObject group, int requiredRight, int requiredBottom) {
        int currentW = Math.max(group.getBounds().getWidth(), 0);
        int currentH = Math.max(group.getBounds().getHeight(), 0);
        int newW = Math.max(currentW, requiredRight);
        int newH = Math.max(currentH, requiredBottom);
        if(newW != currentW || newH != currentH) {
            group.setBounds(group.getBounds().getX(), group.getBounds().getY(), newW, newH);
        }
    }
    
    private int getMaxYRecursive(java.util.List<IDiagramModelObject> children, int offsetY) {
        int maxY = 0;
        for(IDiagramModelObject child : children) {
            int absY = offsetY + child.getBounds().getY();
            int bottom = absY + Math.max(child.getBounds().getHeight(), 0);
            maxY = Math.max(maxY, bottom);
            if(child instanceof IDiagramModelContainer container) {
                maxY = Math.max(maxY, getMaxYRecursive(container.getChildren(), absY));
            }
        }
        return maxY;
    }
    private String lightenColor(IDiagramModelArchimateObject dmo) {
        String hexColor = dmo.getFillColor();
        if(hexColor == null) {
            org.eclipse.swt.graphics.Color color =
                com.archimatetool.editor.ui.ColorFactory.getDefaultFillColor(dmo.getArchimateElement());
            if(color != null) hexColor = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()); //$NON-NLS-1$
        }
        if(hexColor == null) return null;
        try {
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);
            r = r + (int)((255 - r) * 0.65);
            g = g + (int)((255 - g) * 0.65);
            b = b + (int)((255 - b) * 0.65);
            return String.format("#%02X%02X%02X", r, g, b); //$NON-NLS-1$
        } catch(Exception e) { return null; }
    }

    private String darkenColor(IDiagramModelArchimateObject dmo) {
        String hexColor = dmo.getFillColor();
        if(hexColor == null) {
            org.eclipse.swt.graphics.Color color =
                com.archimatetool.editor.ui.ColorFactory.getDefaultFillColor(dmo.getArchimateElement());
            if(color != null) hexColor = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()); //$NON-NLS-1$
        }
        if(hexColor == null) return null;
        try {
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);
            r = (int)(r * 0.8);
            g = (int)(g * 0.8);
            b = (int)(b * 0.8);
            return String.format("#%02X%02X%02X", r, g, b); //$NON-NLS-1$
        } catch(Exception e) { return null; }
    }


    private void collectGroupsByName(java.util.List<IDiagramModelObject> children, Map<String, IDiagramModelContainer> result) {
        for(IDiagramModelObject child : children) {
            if(child instanceof IDiagramModelGroup group && group.getName() != null) {
                result.put(group.getName(), group);
            }
            if(child instanceof IDiagramModelContainer container) {
                collectGroupsByName(container.getChildren(), result);
            }
        }
    }
    
    private void nudgeOverlappingRootItems(IArchimateDiagramModel diffView) {
        List<IDiagramModelObject> rootChildren = new ArrayList<>(diffView.getChildren());
        for(IDiagramModelObject item : rootChildren) {
            if(!(item instanceof IDiagramModelArchimateObject)) continue;
            boolean overlapping = true;
            while(overlapping) {
                overlapping = false;
                for(IDiagramModelObject other : rootChildren) {
                    if(other != item && other instanceof IDiagramModelGroup && boundsContain(other, item)) {
                        // Move item just to the right of the overlapping group
                        int groupRight = other.getBounds().getX() + other.getBounds().getWidth();
                        item.setBounds(groupRight + 5, item.getBounds().getY(),
                            item.getBounds().getWidth(), item.getBounds().getHeight());
                        overlapping = true; // re-check in case it now overlaps another group
                        break;
                    }
                }
            }
        }
    }
    private boolean boundsContain(IDiagramModelObject outer, IDiagramModelObject inner) {
        int ox1 = outer.getBounds().getX();
        int oy1 = outer.getBounds().getY();
        int ox2 = ox1 + outer.getBounds().getWidth();
        int oy2 = oy1 + outer.getBounds().getHeight();
        int ix1 = inner.getBounds().getX();
        int iy1 = inner.getBounds().getY();
        int ix2 = ix1 + inner.getBounds().getWidth();
        int iy2 = iy1 + inner.getBounds().getHeight();
        // Check if inner overlaps with outer at all
        return ix1 < ox2 && ix2 > ox1 && iy1 < oy2 && iy2 > oy1;
    }

    private void addNewElementsFromViewB(
            java.util.List<IDiagramModelObject> srcChildren,
            IDiagramModelContainer destContainer,
            Map<String, IDiagramModelContainer> diffGroupsByName,
            Map<String, IDiagramModelArchimateObject> baseElems,
            Map<IDiagramModelArchimateObject, IDiagramModelArchimateObject> srcToCopyB,
            List<IDiagramModelArchimateObject> newRootDmos,
            Map<IDiagramModelContainer, int[]> groupCounters,
            int CELL_W, int CELL_H, int PADDING, int COLS) {

        for(IDiagramModelObject child : srcChildren) {
            if(child instanceof IDiagramModelArchimateObject dmo) {
                IArchimateElement el = dmo.getArchimateElement();
                IDiagramModelContainer childDest = destContainer; // where children of this dmo should go

                if(el == null || baseElems.containsKey(el.getId())) {
                    // Element already in viewA — but still recurse into its children
                    // using the corresponding copy in the diff view as the container
                    // (find it via srcToCopy which is populated from viewA — but we
                    // don't have access to it here; just use destContainer as fallback)
                } else {
                    // New element — add it
                    IDiagramModelArchimateObject copy;
                    if(destContainer instanceof IDiagramModel) {
                        newRootDmos.add(dmo);
                        // Can't recurse into children properly without the copy yet;
                        // add dmo to newRootDmos and handle children in the second pass
                        copy = null;
                    } else {
                        if(!groupCounters.containsKey(destContainer)) {
                            int baseY = PADDING;
                            for(IDiagramModelObject existing : destContainer.getChildren()) {
                                int bottom = existing.getBounds().getY() + Math.max(existing.getBounds().getHeight(), 0);
                                baseY = Math.max(baseY, bottom + PADDING);
                            }
                            groupCounters.put(destContainer, new int[]{0, 0, baseY});
                        }
                        int[] gc = groupCounters.get(destContainer);
                        int x = PADDING + gc[0] * CELL_W;
                        int y = gc[2] + gc[1] * CELL_H;
                        copy = copyDmo(dmo, x, y);
                        copy.setFillColor(darkenColor(dmo));
                        destContainer.getChildren().add(copy);
                        srcToCopyB.put(dmo, copy);
                        if(++gc[0] >= COLS) { gc[0] = 0; gc[1]++; }
                        expandContainerToFit((IDiagramModelObject)destContainer);
                        childDest = copy; // recurse into this new copy
                    }
                    if(copy != null && !dmo.getChildren().isEmpty()) {
                        addNewElementsFromViewB(dmo.getChildren(), childDest,
                            diffGroupsByName, baseElems, srcToCopyB, newRootDmos,
                            groupCounters, CELL_W, CELL_H, PADDING, COLS);
                    }
                }

                // Recurse into children of existing (viewA) elements too
                if((el == null || baseElems.containsKey(el.getId())) && !dmo.getChildren().isEmpty()) {
                    addNewElementsFromViewB(dmo.getChildren(), destContainer,
                        diffGroupsByName, baseElems, srcToCopyB, newRootDmos,
                        groupCounters, CELL_W, CELL_H, PADDING, COLS);
                }

            } else if(child instanceof IDiagramModelGroup group) {
                String groupName = group.getName();
                IDiagramModelContainer targetGroup;
                if(groupName != null && diffGroupsByName.containsKey(groupName)) {
                    targetGroup = diffGroupsByName.get(groupName);
                } else {
                    targetGroup = copyContainerShell(child);
                    destContainer.getChildren().add((IDiagramModelObject) targetGroup);
                    if(groupName != null) diffGroupsByName.put(groupName, targetGroup);
                }
                addNewElementsFromViewB(group.getChildren(), targetGroup, diffGroupsByName,
                    baseElems, srcToCopyB, newRootDmos, groupCounters, CELL_W, CELL_H, PADDING, COLS);
            }
        }
    }
    
    
    private void copyViewRecursive(
            java.util.List<IDiagramModelObject> srcChildren,
            IDiagramModelContainer destContainer,
            Map<String, IDiagramModelArchimateObject> targetElems,
            Map<IDiagramModelArchimateObject, IDiagramModelArchimateObject> srcToCopy) {

        for(IDiagramModelObject child : srcChildren) {
            if(child instanceof IDiagramModelArchimateObject dmo) {
            	
                IDiagramModelArchimateObject copy = copyDmo(dmo, dmo.getBounds().getX(), dmo.getBounds().getY());
                IArchimateElement el = dmo.getArchimateElement();
                if(el != null && !targetElems.containsKey(el.getId())) {
                	copy.setFillColor(lightenColor(dmo));
                }
                destContainer.getChildren().add(copy);
                srcToCopy.put(dmo, copy);
                // Recurse into nested children of this element
                if(!dmo.getChildren().isEmpty()) {
                    copyViewRecursive(dmo.getChildren(), copy, targetElems, srcToCopy);
                }
            } else if(child instanceof IDiagramModelContainer container) {
                // It's a group — copy shell then recurse into its children
                IDiagramModelContainer groupCopy = copyContainerShell(child);
                destContainer.getChildren().add((IDiagramModelObject) groupCopy);
                copyViewRecursive(container.getChildren(), groupCopy, targetElems, srcToCopy);
            }
        }
    }
    private void expandContainerToFit(IDiagramModelObject container) {

        if(!(container instanceof IDiagramModelContainer modelContainer)) {
            return;
        }

        int maxRight = 0;
        int maxBottom = 0;
        final int PADDING = 20;

        for(IDiagramModelObject child : modelContainer.getChildren()) {
            maxRight = Math.max(maxRight,
                    child.getBounds().getX() + child.getBounds().getWidth());

            maxBottom = Math.max(maxBottom,
                    child.getBounds().getY() + child.getBounds().getHeight());
        }

        if(maxRight + PADDING > container.getBounds().getWidth()) {
            container.getBounds().setWidth(maxRight + PADDING);
        }

        if(maxBottom + PADDING > container.getBounds().getHeight()) {
            container.getBounds().setHeight(maxBottom + PADDING);
        }

        // Grow the parent container as well
        if(container.eContainer() instanceof IDiagramModelObject parent) {
            expandContainerToFit(parent);
        }
    }

    private IDiagramModelContainer copyContainerShell(IDiagramModelObject src) {
        IDiagramModelObject copy;
        if(src instanceof com.archimatetool.model.IDiagramModelGroup) {
            copy = IArchimateFactory.eINSTANCE.createDiagramModelGroup();
        } else {
            copy = (IDiagramModelObject) IArchimateFactory.eINSTANCE.create(src.eClass());
        }
        int w = src.getBounds().getWidth()  > 0 ? src.getBounds().getWidth()  : 200;
        int h = src.getBounds().getHeight() > 0 ? src.getBounds().getHeight() : 100;
        copy.setBounds(src.getBounds().getX(), src.getBounds().getY(), w, h);
        copy.setFillColor(src.getFillColor());
        copy.setLineColor(src.getLineColor());
        if(src instanceof com.archimatetool.model.INameable nameable &&
           copy instanceof com.archimatetool.model.INameable copyNameable) {
            copyNameable.setName(nameable.getName());
        }
        return (IDiagramModelContainer) copy;
    }
    private Map<String, IDiagramModelArchimateObject> collectElementsById(IDiagramModel view) {
        Map<String, IDiagramModelArchimateObject> map = new java.util.LinkedHashMap<>();
        for(IDiagramModelArchimateObject dmo : collectAllDmos(view)) {
            com.archimatetool.model.IArchimateElement el = dmo.getArchimateElement();
            if(el != null && el.getId() != null) {
                map.put(el.getId(), dmo);
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
    private java.util.List<IDiagramModelArchimateObject> collectAllDmos(IDiagramModel view) {
        java.util.List<IDiagramModelArchimateObject> list = new java.util.ArrayList<>();
        collectAllDmosRecursive(view.getChildren(), list);
        return list;
    }

    private void collectAllDmosRecursive(java.util.List<IDiagramModelObject> children,
                                          java.util.List<IDiagramModelArchimateObject> result) {
        for(IDiagramModelObject child : children) {
            if(child instanceof IDiagramModelArchimateObject dmo) {
                result.add(dmo);
            }
            if(child instanceof IDiagramModelContainer container) {
                collectAllDmosRecursive(container.getChildren(), result);
            }
        }
    }

    private int[] getAbsolutePosition(IDiagramModelObject dmo) {
        int x = dmo.getBounds().getX();
        int y = dmo.getBounds().getY();
        org.eclipse.emf.ecore.EObject container = dmo.eContainer();
        while(container instanceof IDiagramModelObject parent) {
            x += parent.getBounds().getX();
            y += parent.getBounds().getY();
            container = parent.eContainer();
        }
        return new int[]{x, y};
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