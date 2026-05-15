/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.zest;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;

import com.archimatetool.editor.model.IEditorModelManager;
import com.archimatetool.editor.ui.ArchiLabelProvider;
import com.archimatetool.editor.ui.IArchiImages;
import com.archimatetool.editor.ui.services.ViewManager;
import com.archimatetool.editor.utils.StringUtils;
import com.archimatetool.editor.views.AbstractModelView;
import com.archimatetool.editor.views.tree.ITreeModelView;
import com.archimatetool.editor.views.tree.actions.IViewerAction;
import com.archimatetool.editor.views.tree.actions.PropertiesAction;
import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IArchimateModelObject;
import com.archimatetool.model.IArchimatePackage;
import com.archimatetool.model.util.ArchimateModelUtils;
import com.archimatetool.model.viewpoints.IViewpoint;
import com.archimatetool.model.viewpoints.ViewpointManager;
import com.archimatetool.model.IDiagramModel;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalTreeLayoutAlgorithm;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import com.archimatetool.model.IDiagramModel;
import java.util.Set;
import java.util.LinkedList;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.widgets.GraphNode;
import com.archimatetool.model.IDiagramModel;
/**
 * Zest View
 * 
 * @author Phillip Beauvoir
 * @author Jean-Baptiste Sarrodie
 */
public class ZestView extends AbstractModelView
implements IZestView, ISelectionListener {
    
    private ZestGraphViewer fGraphViewer;
    
    private CLabel fLabel;
    
    private IAction fActionLayout;
    private IViewerAction fActionProperties;
    private IAction fActionPinContent;
    private IAction fActionCopyImageToClipboard;
    private IAction fActionExportImageToFile;
    private IAction fActionSelectInModelTree;
    
    private IAction[] fDepthActions;
    
    private List<IAction> fLevelFilterActions;
    
    private IAction[] fDirectionActions;
    private List<IAction> fViewpointActions;
    
    private List<IAction> fRelationshipActions;
    private IAction fNoneRelationshipAction;

    private List<IAction> fAllElementActions;
    private IAction fNoneElementAction;
    private List<IAction> fStrategyElementActions;
    private List<IAction> fBusinessElementActions;
    private List<IAction> fApplicationElementActions;
    private List<IAction> fTechnologyElementActions;
    private List<IAction> fPhysicalElementActions;
    private List<IAction> fImplementationMigrationElementActions;
    private List<IAction> fMotivationElementActions;
    private List<IAction> fOtherElementActions;
  
    private DrillDownManager fDrillDownManager;
    
    @Override
    protected void doCreatePartControl(Composite parent) {
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        parent.setLayout(layout);
        
        fLabel = new CLabel(parent, SWT.NONE);
        fLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        fGraphViewer = new ZestGraphViewer(parent, SWT.NONE);
        fGraphViewer.getGraphControl().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        
        // spring is the default - we do need to set this here!
        fGraphViewer.setLayoutAlgorithm(new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
        //fGraphViewer.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
        //fGraphViewer.setLayoutAlgorithm(new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
        //fGraphViewer.setLayoutAlgorithm(new HorizontalTreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
        
        // Graph selection listener
        fGraphViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                // Update actions
                updateActions();
                // Need to do this in order for Tabbed Properties View to update on Selection
                getSite().getSelectionProvider().setSelection(event.getSelection());
            }
        });
        
        // Double-click
        fGraphViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                fDrillDownManager.goInto();
            }
        });

        fDrillDownManager = new DrillDownManager(this);
        
        makeActions();
        hookContextMenu();
        registerGlobalActions();
        makeLocalToolBar();

        // This will update previous Undo/Redo text if View was closed before
        updateActions();

        // Register selections
        getSite().setSelectionProvider(getViewer());
        
        // Listen to global selections to update the viewer
        getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
        
        // Help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(fGraphViewer.getControl(), HELP_ID);
        
        // Initialise with whatever is selected in the workbench
        ISelection selection = getSite().getWorkbenchWindow().getSelectionService().getSelection();
        selectionChanged(null, selection);
    }

    
    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if(part == this) {
            return;
        }
        
        if(fActionPinContent.isChecked()) {
            return;
        }
        
        if(selection instanceof IStructuredSelection && !selection.isEmpty()) {
            Object object = ((IStructuredSelection)selection).getFirstElement();
            setElement(object);
        }
    }
    
    private LinkedList<IDiagramModel> buildIterationChain(IDiagramModel current) {
        LinkedList<IDiagramModel> chain = new LinkedList<>();
        chain.add(current);
        Set<IDiagramModel> visited = new HashSet<>();
        visited.add(current);

        // Walk backwards (Previous Iteration) → prepend to chain
        IDiagramModel cursor = current;
        while(true) {
            IDiagramModel prev = getLinkedDiagram(cursor, "Previous Iteration"); //$NON-NLS-1$
            if(prev == null || visited.contains(prev)) break;
            chain.addFirst(prev);
            visited.add(prev);
            cursor = prev;
        }

        // Walk forwards (Next Iteration) → append to chain
        cursor = current;
        while(true) {
            IDiagramModel next = getLinkedDiagram(cursor, "Next Iteration"); //$NON-NLS-1$
            if(next == null || visited.contains(next)) break;
            chain.addLast(next);
            visited.add(next);
            cursor = next;
        }

        return chain;
    }
    
    private LinkedList<IDiagramModel> buildVersionChain(IDiagramModel current) {
        LinkedList<IDiagramModel> chain = new LinkedList<>();
        chain.add(current);
        Set<IDiagramModel> visited = new HashSet<>();
        visited.add(current);

        // Walk backwards (Previous Version) → prepend (goes UP)
        IDiagramModel cursor = current;
        while(true) {
            IDiagramModel prev = getLinkedDiagram(cursor, "Previous Version"); //$NON-NLS-1$
            if(prev == null || visited.contains(prev)) break;
            chain.addFirst(prev);
            visited.add(prev);
            cursor = prev;
        }

        // Walk forwards (Next Version) → append (goes DOWN)
        cursor = current;
        while(true) {
            IDiagramModel next = getLinkedDiagram(cursor, "Next Version"); //$NON-NLS-1$
            if(next == null || visited.contains(next)) break;
            chain.addLast(next);
            visited.add(next);
            cursor = next;
        }

        return chain;
    }

    /**
     * Resolve a Previous/Next Iteration property value to its IDiagramModel.
     */
    private IDiagramModel getLinkedDiagram(IDiagramModel diagram, String propertyKey) {
        return diagram.getProperties().stream()
            .filter(p -> propertyKey.equals(p.getKey()) && p.getValue() != null && !p.getValue().isEmpty())
            .findFirst()
            .map(p -> diagram.getArchimateModel().getDiagramModels().stream()
                    .filter(d -> p.getValue().equals(d.getName()))
                    .findFirst()
                    .orElse(null))
            .orElse(null);
    }
    
    @Override
    protected void selectAll() {
        fGraphViewer.getGraphControl().selectAll();
    }
    
    private void setElement(Object object) {
        if(object instanceof IDiagramModel diagramModel) {
        	LinkedList<IDiagramModel> iterChain    = buildIterationChain(diagramModel);
        	LinkedList<IDiagramModel> versionChain = buildVersionChain(diagramModel);
        	fGraphViewer.setInput(diagramModel);
        	Display.getCurrent().asyncExec(() -> positionNodes(iterChain, versionChain));
            updateActions();
            updateLabel();
            return;
        }

        IArchimateConcept concept = null;
        if(object instanceof IArchimateConcept) {
            concept = (IArchimateConcept)object;
        }
        else if(object instanceof IAdaptable) {
            concept = ((IAdaptable)object).getAdapter(IArchimateConcept.class);
        }

        fGraphViewer.setLayoutAlgorithm(new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), false);
        fDrillDownManager.setNewInput(concept);
        updateActions();
        updateLabel();
    }
    
    private void positionNodes(LinkedList<IDiagramModel> iterChain, LinkedList<IDiagramModel> versionChain) {
        if(fGraphViewer.getGraphControl().isDisposed()) return;

        final int NODE_WIDTH = 120;
        final int NODE_HEIGHT = 40;
        final int H_SPACING   = 200;
        final int V_SPACING   = 100;

        org.eclipse.swt.graphics.Rectangle bounds = fGraphViewer.getGraphControl().getBounds();

        // --- Horizontal: iteration chain ---
        int iterCount  = iterChain.size();
        int totalWidth = iterCount * NODE_WIDTH + (iterCount - 1) * H_SPACING;
        int startX     = Math.max(20, (bounds.width  - totalWidth) / 2);
        int centerY    = Math.max(20, (bounds.height - 40) / 2);

        java.util.Map<IDiagramModel, int[]> positions = new java.util.HashMap<>();
        for(int i = 0; i < iterChain.size(); i++) {
            positions.put(iterChain.get(i), new int[]{ startX + i * (NODE_WIDTH + H_SPACING), centerY });
        }

        // --- Vertical: version chain ---
        // Find the anchor = the diagram that appears in both chains
        IDiagramModel anchor = null;
        for(IDiagramModel dm : iterChain) {
            if(versionChain.contains(dm)) { anchor = dm; break; }
        }

        if(anchor != null && versionChain.size() > 1) {
            int[] anchorPos       = positions.get(anchor);
            int   anchorX         = anchorPos != null ? anchorPos[0] : (bounds.width / 2);
            int   anchorIdxV      = versionChain.indexOf(anchor);
            int   startY          = Math.max(20, centerY - anchorIdxV * (NODE_HEIGHT + V_SPACING));

            for(int i = 0; i < versionChain.size(); i++) {
                IDiagramModel dm = versionChain.get(i);
                if(!positions.containsKey(dm)) { // don't overwrite the anchor
                    positions.put(dm, new int[]{ anchorX, startY + i * (NODE_HEIGHT + V_SPACING) });
                }
            }
        }

        // --- Apply positions to graph nodes ---
        for(Object obj : fGraphViewer.getGraphControl().getNodes()) {
            GraphNode node = (GraphNode) obj;
            if(node.getData() instanceof IDiagramModel dm && positions.containsKey(dm)) {
                int[] pos = positions.get(dm);
                node.setLocation(pos[0], pos[1]);
            }
        }
    }
    
    void refresh() {
        updateActions();
        updateLabel();
        getViewer().refresh();
    }
    
    /**
     * Update local label
     */
    void updateLabel() {
    	Object input = fGraphViewer.getInput();
        Object labelSource = (input instanceof IDiagramModel) ? input : fDrillDownManager.getCurrentConcept();

        String text = ArchiLabelProvider.INSTANCE.getLabel(labelSource);
        text = StringUtils.escapeAmpersandsInText(text);
        
        // Viewpoint
        String viewPointName = getContentProvider().getViewpointFilter().getName();
        
        // Filtered elements
        String elements;
        if(getContentProvider().getElementFilters().isEmpty()) {
            elements = Messages.ZestView_7;
        }
        else {
            elements = "["; //$NON-NLS-1$
            for(EClass eClass : getContentProvider().getElementFilters()) {
                elements += getFilterName(eClass) + ", "; //$NON-NLS-1$
            }
            elements = elements.replaceAll(", $", ""); // Remove trailing comma //$NON-NLS-1$ //$NON-NLS-2$
            elements += "]"; //$NON-NLS-1$
        }
       	
        // Filtered relations
        String relations;
        if(getContentProvider().getRelationshipFilters().isEmpty()) {
            relations = Messages.ZestView_7;
        }
        else {
            relations = "["; //$NON-NLS-1$
            for(EClass eClass : getContentProvider().getRelationshipFilters()) {
                relations += getFilterName(eClass) + ", "; //$NON-NLS-1$
            }
            relations = relations.replaceAll(", $", ""); // Remove trailing comma //$NON-NLS-1$ //$NON-NLS-2$
            relations += "]"; //$NON-NLS-1$
        }
        
        fLabel.setText(text + " (" + Messages.ZestView_5 + ": " + viewPointName + ", " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    Messages.ZestView_9 + ": " + elements + ", " + Messages.ZestView_6 + ": " + relations + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        fLabel.setImage(ArchiLabelProvider.INSTANCE.getImage(labelSource));

    }

    /**
     * Update the Local Actions depending on the selection, and undo/redo actions
     */
    void updateActions() {
        IStructuredSelection selection = (IStructuredSelection)getViewer().getSelection();
        fActionProperties.update();
        fActionSelectInModelTree.setEnabled(!selection.isEmpty());
        
        boolean hasData = fGraphViewer.getInput() != null;
        fActionExportImageToFile.setEnabled(hasData);
        fActionCopyImageToClipboard.setEnabled(hasData);
        fActionLayout.setEnabled(hasData);
        
        updateUndoActions();
    }
    
    /**
     * Populate the ToolBar
     */
    private void makeLocalToolBar() {
        IActionBars bars = getViewSite().getActionBars();
        
        IToolBarManager manager = bars.getToolBarManager();
        
        fDrillDownManager.addNavigationActions(manager);
        manager.add(new Separator());
        manager.add(fActionPinContent);
        manager.add(new Separator());
        manager.add(fActionLayout);
        
        final IMenuManager menuManager = bars.getMenuManager();
        
        // Depth
        menuManager.add(createDepthMenu());

        // Viewpoints
        menuManager.add(createViewpointMenu());

        // Elements
        menuManager.add(createElementsMenu());

        // Relationships
        menuManager.add(createRelationshipsMenu());

        // Direction
        menuManager.add(createDirectionMenu());
        menuManager.add(createLevelMenu());
		menuManager.add(new Separator());
		
		menuManager.add(fActionSelectInModelTree);
		menuManager.add(fActionCopyImageToClipboard);
		menuManager.add(fActionExportImageToFile);
    }

    private String getFilterName(EClass eClass) {
        return eClass == null ? Messages.ZestView_7 : ArchiLabelProvider.INSTANCE.getDefaultName(eClass);
    }

    @Override
    public void setFocus() {
        if(fGraphViewer != null) {
            fGraphViewer.getControl().setFocus();
        }
    }

    @Override
    public ZestGraphViewer getViewer() {
        return fGraphViewer;
    }

    // ==============================================================================================
    // Menu Actions
    // ==============================================================================================
    
    /**
     * Make local actions
     */
    private void makeActions() {
        // Depth
        createDepthActions();
        
        // Viewpoints
        createViewpointActions();
        
        // Elements
        createElementsActions();
        
        // Relationships
        createRelationshipsActions();
        
        // Direction
        createDirectionActions();
        
        createLevelFilterActions();

        fActionProperties = new PropertiesAction(getViewer());

        fActionLayout = new Action(Messages.ZestView_0) {

            @Override
            public void run() {
                fGraphViewer.doApplyLayout();
            }

            @Override
            public String getToolTipText() {
                return getText();
            }

            @Override
            public ImageDescriptor getImageDescriptor() {
                return ResourceLocator.imageDescriptorFromBundle(ArchiZestPlugin.PLUGIN_ID, "img/layout.gif").orElse(null); //$NON-NLS-1$
            }
        };

        fActionPinContent = new Action(Messages.ZestView_4, IAction.AS_CHECK_BOX) {

            {
                setToolTipText(Messages.ZestView_1);
                setImageDescriptor(IArchiImages.ImageFactory.getImageDescriptor(IArchiImages.ICON_PIN));
            }
        };

        fActionCopyImageToClipboard = new CopyZestViewAsImageToClipboardAction(this);
        fActionExportImageToFile = new ExportAsImageAction(fGraphViewer);

        fActionSelectInModelTree = new Action(Messages.ZestView_8) {
            
            {
                setToolTipText(getText());
                // Register for key binding
                setActionDefinitionId("com.archimatetool.editor.selectInModelTree"); //$NON-NLS-1$
                IHandlerService service = getSite().getService(IHandlerService.class);
                service.activateHandler(getActionDefinitionId(), new ActionHandler(this));
            }

            @Override
            public void run() {
                IStructuredSelection selection = (IStructuredSelection)getViewer().getSelection();
                ITreeModelView view = (ITreeModelView)ViewManager.showViewPart(ITreeModelView.ID, true);
                if(view != null && !selection.isEmpty()) {
                    view.getViewer().setSelection(new StructuredSelection(selection.toArray()), true);
                }
            }
        };
    }

    private void createDepthActions() {
        fDepthActions = new Action[6];
        for(int i = 0; i < fDepthActions.length; i++) {
            fDepthActions[i] = createDepthAction(i, i + 1);
        }

        // Set depth from prefs
        int depth = ArchiZestPlugin.getInstance().getPreferenceStore().getInt(IPreferenceConstants.VISUALISER_DEPTH);
        getContentProvider().setDepth(depth);
        fDepthActions[depth].setChecked(true);
    }
    
    
    private void createLevelFilterActions() {
        fLevelFilterActions = new ArrayList<IAction>();

        // "All" option
        IAction allAction = new Action("All", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                getContentProvider().setLevelFilter(null);
                fGraphViewer.setInput(fGraphViewer.getInput());
                fGraphViewer.setSelection((IStructuredSelection)fGraphViewer.getSelection());
                fGraphViewer.doApplyLayout();
            }
        };
        allAction.setChecked(true);
        fLevelFilterActions.add(allAction);

        // Level 1, 2, 3
        for(String level : new String[]{"Level 1", "Level 2", "Level 3"}) {
            IAction act = new Action(level, IAction.AS_RADIO_BUTTON) {
                @Override
                public void run() {
                    getContentProvider().setLevelFilter(getText());
                    fGraphViewer.setInput(fGraphViewer.getInput());
                    fGraphViewer.setSelection((IStructuredSelection)fGraphViewer.getSelection());
                    fGraphViewer.doApplyLayout();
                }
            };
            fLevelFilterActions.add(act);
        }
    }

    private IAction createDepthAction(final int actionId, final int depth) {
        IAction act = new Action(Messages.ZestView_3 + " " + depth, IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                IStructuredSelection selection = (IStructuredSelection)fGraphViewer.getSelection();
                int depth = Integer.valueOf(getId());
                getContentProvider().setDepth(depth);
                ArchiZestPlugin.getInstance().getPreferenceStore().setValue(IPreferenceConstants.VISUALISER_DEPTH, depth);

                Object input = fGraphViewer.getInput();
                if(input instanceof IDiagramModel diagramModel) {
                    // Rebuild the chain respecting the new depth, reposition without doApplyLayout
                	LinkedList<IDiagramModel> iterChain    = buildIterationChain(diagramModel);
                	LinkedList<IDiagramModel> versionChain = buildVersionChain(diagramModel);
                	fGraphViewer.setInput(diagramModel);
                	Display.getCurrent().asyncExec(() -> positionNodes(iterChain, versionChain));
                }
                else {
                    fGraphViewer.setInput(input);
                    fGraphViewer.setSelection(selection);
                    fGraphViewer.doApplyLayout();
                }
            }
        };
        act.setId(Integer.toString(actionId));
        return act;
    }

    private void createViewpointActions() {
        // Get viewpoint from prefs
        String viewpointID = ArchiZestPlugin.getInstance().getPreferenceStore().getString(IPreferenceConstants.VISUALISER_VIEWPOINT);
        getContentProvider().setViewpointFilter(ViewpointManager.INSTANCE.getViewpoint(viewpointID));

        // Viewpoint actions
        fViewpointActions = new ArrayList<IAction>();

        for(IViewpoint vp : ViewpointManager.INSTANCE.getAllViewpoints()) {
            IAction action = createViewpointMenuAction(vp);
            fViewpointActions.add(action);

            // Set checked
            if(vp.getID().equals(viewpointID)) {
                action.setChecked(true);
            }
        }
    }

    private IAction createViewpointMenuAction(final IViewpoint vp) {
        IAction act = new Action(vp.getName(), IAction.AS_RADIO_BUTTON) {

            @Override
            public void run() {
                // Set viewpoint filter
                getContentProvider().setViewpointFilter(vp);
                // Store in prefs
                ArchiZestPlugin.getInstance().getPreferenceStore().setValue(IPreferenceConstants.VISUALISER_VIEWPOINT, vp.getID());

                // update viewer
                fGraphViewer.setInput(fGraphViewer.getInput());
                IStructuredSelection selection = (IStructuredSelection)fGraphViewer.getSelection();
                fGraphViewer.setSelection(selection);
                fGraphViewer.doApplyLayout();
                updateLabel();
            }
        };

        act.setId(vp.getID());

        return act;
    }

    private void createElementsActions() {
        fAllElementActions = new ArrayList<IAction>();

        // The "All" option
        fNoneElementAction = createElementAction(null);
        fAllElementActions.add(fNoneElementAction);

        // Strategy
        fStrategyElementActions = createElementActionsGroup(ArchimateModelUtils.getStrategyClasses());

        // Business
        fBusinessElementActions = createElementActionsGroup(ArchimateModelUtils.getBusinessClasses());

        // Application
        fApplicationElementActions = createElementActionsGroup(ArchimateModelUtils.getApplicationClasses());

        // Technology
        fTechnologyElementActions = createElementActionsGroup(ArchimateModelUtils.getTechnologyClasses());

        // Physical
        fPhysicalElementActions = createElementActionsGroup(ArchimateModelUtils.getPhysicalClasses());

        // Motivation
        fMotivationElementActions = createElementActionsGroup(ArchimateModelUtils.getMotivationClasses());

        // Implementation & Migration
        fImplementationMigrationElementActions = createElementActionsGroup(ArchimateModelUtils.getImplementationMigrationClasses());

        // Other
        fOtherElementActions = createElementActionsGroup(ArchimateModelUtils.getOtherClasses());

        // Get selected elements from prefs
        String elementPrefs = ArchiZestPlugin.getInstance().getPreferenceStore().getString(IPreferenceConstants.VISUALISER_ELEMENTS);
        
        // All
        if("".equals(elementPrefs)) { //$NON-NLS-1$
            fNoneElementAction.setChecked(true);
        }
        // Elements
        else {
            for(String s : elementPrefs.split(" ")) { //$NON-NLS-1$
                EClass elementClass = (EClass)IArchimatePackage.eINSTANCE.getEClassifier(s);
                if(elementClass != null) {
                    getContentProvider().addElementFilter(elementClass);
                    for(IAction a : fAllElementActions) {
                        if(a.getId().equals(elementClass.getName())) {
                            a.setChecked(true);
                        }
                    }
                }
            }
        }
    }

    private List<IAction> createElementActionsGroup(EClass[] eClasses) {
        List<IAction> actions = new ArrayList<IAction>();

        ArrayList<EClass> list = new ArrayList<EClass>(Arrays.asList(eClasses));
        list.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));

        for(EClass elem : list) {
            IAction elementAction = createElementAction(elem);
            actions.add(elementAction);
            fAllElementActions.add(elementAction);
        }

        return actions;
    }

    private IAction createElementAction(final EClass elementClass) {
        String id = elementClass == null ? "none" : elementClass.getName(); //$NON-NLS-1$

        IAction act = new Action(getFilterName(elementClass), IAction.AS_CHECK_BOX) {

            @Override
            public void run() {
                // Set element filter
                if(isChecked()) {
                    getContentProvider().addElementFilter(elementClass);
                }
                else {
                    getContentProvider().removeElementFilter(elementClass);
                }
                
                // update viewer
                fGraphViewer.setInput(fGraphViewer.getInput());
                IStructuredSelection selection = (IStructuredSelection)fGraphViewer.getSelection();
                fGraphViewer.setSelection(selection);
                fGraphViewer.doApplyLayout();
                updateLabel();
                
                // If this is "All" uncheck all other actions and ensure "All" is always checked
                if(elementClass == null) {
                    for(IAction a : fAllElementActions) {
                        a.setChecked(a == this);
                    }
                }
                // Else set "All" checked if no filters
                else {
                    fNoneElementAction.setChecked(getContentProvider().getElementFilters().isEmpty());
                }

                // Save to Preferences
                String elements = ""; //$NON-NLS-1$
                for(EClass eClass : getContentProvider().getElementFilters()) {
                    elements += eClass.getName() + " "; //$NON-NLS-1$
                }
                ArchiZestPlugin.getInstance().getPreferenceStore().setValue(IPreferenceConstants.VISUALISER_ELEMENTS, elements);
            }
        };

        act.setId(id);

        return act;
    }

    private void createRelationshipsActions() {
        fRelationshipActions = new ArrayList<IAction>();

        // The "All" option
        fNoneRelationshipAction = createRelationshipMenuAction(null);
        fRelationshipActions.add(fNoneRelationshipAction);

        // Then get all relationships and sort them
        ArrayList<EClass> list = new ArrayList<EClass>(Arrays.asList(ArchimateModelUtils.getRelationsClasses()));
        list.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        
        for(EClass rel : list) {
            fRelationshipActions.add(createRelationshipMenuAction(rel));
        }
        
        // Get selected relationships from prefs
        String relationsPrefs = ArchiZestPlugin.getInstance().getPreferenceStore().getString(IPreferenceConstants.VISUALISER_RELATIONSHIPS);
        
        // All
        if("".equals(relationsPrefs)) { //$NON-NLS-1$
            fNoneRelationshipAction.setChecked(true);
        }
        // Relations
        else {
            for(String s : relationsPrefs.split(" ")) { //$NON-NLS-1$
                EClass relationClass = (EClass)IArchimatePackage.eINSTANCE.getEClassifier(s);
                if(relationClass != null) {
                    getContentProvider().addRelationshipFilter(relationClass);
                    for(IAction a : fRelationshipActions) {
                        if(a.getId().equals(relationClass.getName())) {
                            a.setChecked(true);
                        }
                    }
                }
            }
        }
    }

    private IAction createRelationshipMenuAction(final EClass relationClass) {
        String id = relationClass == null ? "none" : relationClass.getName(); //$NON-NLS-1$

        IAction act = new Action(getFilterName(relationClass), IAction.AS_CHECK_BOX) {

            @Override
            public void run() {
                // Set element filter
                if(isChecked()) {
                    getContentProvider().addRelationshipFilter(relationClass);
                }
                else {
                    getContentProvider().removeElementFilter(relationClass);
                }

                // update viewer
                fGraphViewer.setInput(fGraphViewer.getInput());
                IStructuredSelection selection = (IStructuredSelection)fGraphViewer.getSelection();
                fGraphViewer.setSelection(selection);
                fGraphViewer.doApplyLayout();
                updateLabel();
                
                // If this is "All" uncheck all other actions and ensure "All" is always checked
                if(relationClass == null) {
                    for(IAction a : fRelationshipActions) {
                        a.setChecked(a == this);
                    }
                }
                // Else set "All" checked if no filters
                else {
                    fNoneRelationshipAction.setChecked(getContentProvider().getRelationshipFilters().isEmpty());
                }
                
                // Save to Preferences
                String relations = ""; //$NON-NLS-1$
                for(EClass eClass : getContentProvider().getRelationshipFilters()) {
                    relations += eClass.getName() + " "; //$NON-NLS-1$
                }
                ArchiZestPlugin.getInstance().getPreferenceStore().setValue(IPreferenceConstants.VISUALISER_RELATIONSHIPS, relations);
            }
        };

        act.setId(id);

        return act;
    }

    private void createDirectionActions() {
        // Direction
        fDirectionActions = new Action[3];
        fDirectionActions[0] = createDirectionMenuAction(0, Messages.ZestView_33, ZestViewerContentProvider.DIR_BOTH);
        fDirectionActions[1] = createDirectionMenuAction(1, Messages.ZestView_34, ZestViewerContentProvider.DIR_IN);
        fDirectionActions[2] = createDirectionMenuAction(2, Messages.ZestView_35, ZestViewerContentProvider.DIR_OUT);

        // Set direction from prefs
        int direction = ArchiZestPlugin.getInstance().getPreferenceStore().getInt(IPreferenceConstants.VISUALISER_DIRECTION);
        getContentProvider().setDirection(direction);
        fDirectionActions[direction].setChecked(true);
    }

    private IAction createDirectionMenuAction(final int actionId, String label, final int orientation) {
        IAction act = new Action(label, IAction.AS_RADIO_BUTTON) {

            @Override
            public void run() {
                IStructuredSelection selection = (IStructuredSelection)fGraphViewer.getSelection();
                // Set orientation
                getContentProvider().setDirection(orientation);
                // Store in prefs
                ArchiZestPlugin.getInstance().getPreferenceStore().setValue(IPreferenceConstants.VISUALISER_DIRECTION, actionId);
                // update viewer
                fGraphViewer.setInput(fGraphViewer.getInput());
                fGraphViewer.setSelection(selection);
                fGraphViewer.doApplyLayout();
            }
        };

        act.setId(Integer.toString(actionId));

        return act;
    }

    /**
     * Register Global Action Handlers
     */
    private void registerGlobalActions() {
        IActionBars actionBars = getViewSite().getActionBars();

        // Register our interest in the global menu actions
        actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), fActionProperties);
    }

    /**
     * Hook into a right-click menu
     */
    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#ZestViewPopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);

        menuMgr.addMenuListener(new IMenuListener() {

            @Override
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }
        });

        Menu menu = menuMgr.createContextMenu(getViewer().getControl());
        getViewer().getControl().setMenu(menu);

        getSite().registerContextMenu(menuMgr, getViewer());
    }
    
    private IMenuManager createLevelMenu() {
        IMenuManager levelMenuManager = new MenuManager("Level Filter");
        for(IAction action : fLevelFilterActions) {
            levelMenuManager.add(action);
        }
        return levelMenuManager;
    }
    

    /**
     * Fill context menu when user right-clicks
     * 
     * @param manager
     */
    private void fillContextMenu(IMenuManager manager) {
        Object selected = ((IStructuredSelection)getViewer().getSelection()).getFirstElement();
        boolean isEmpty = selected == null;

        fDrillDownManager.addNavigationActions(manager);
        manager.add(new Separator());
        manager.add(fActionLayout);

        manager.add(new Separator());

        // Depth
        manager.add(createDepthMenu());
        
        // Viewpoint filter
        manager.add(createViewpointMenu());

        // Element filter
        manager.add(createElementsMenu());

        // Relationship filter
        manager.add(createRelationshipsMenu());
        
        // Direction
        manager.add(createDirectionMenu());
        manager.add(createLevelMenu());
        manager.add(new Separator());

        manager.add(fActionCopyImageToClipboard);
        manager.add(fActionExportImageToFile);

        if(!isEmpty) {
            manager.add(fActionSelectInModelTree);
            manager.add(new Separator());
            manager.add(fActionProperties);
        }

        // Other plug-ins can contribute their actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }
    
    private IMenuManager createViewpointMenu() {
        IMenuManager vpMenuManager = new MenuManager(Messages.ZestView_5);

        for(IAction action : fViewpointActions) {
            vpMenuManager.add(action);
        }
        
        return vpMenuManager;
    }
    
    private IMenuManager createDirectionMenu() {
        IMenuManager directionMenuManager = new MenuManager(Messages.ZestView_32);

        for(IAction action : fDirectionActions) {
            directionMenuManager.add(action);
        }

        return directionMenuManager;
    }
    
    private IMenuManager createDepthMenu() {
        IMenuManager depthMenuManager = new MenuManager(Messages.ZestView_3);

        for(IAction action : fDepthActions) {
            depthMenuManager.add(action);
        }

        return depthMenuManager;
    }

    private IMenuManager createElementsMenu() {
        IMenuManager elementMenuManager = new MenuManager(Messages.ZestView_9);

        // "All"
        elementMenuManager.add(fNoneElementAction);

        IMenuManager strategyElementMenuManager = new MenuManager(Messages.ZestView_10);
        elementMenuManager.add(strategyElementMenuManager);
        for(IAction action : fStrategyElementActions) {
            strategyElementMenuManager.add(action);
        }

        IMenuManager businessElementMenuManager = new MenuManager(Messages.ZestView_11);
        elementMenuManager.add(businessElementMenuManager);
        for(IAction action : fBusinessElementActions) {
            businessElementMenuManager.add(action);
        }

        IMenuManager applicationElementMenuManager = new MenuManager(Messages.ZestView_12);
        elementMenuManager.add(applicationElementMenuManager);
        for(IAction action : fApplicationElementActions) {
            applicationElementMenuManager.add(action);
        }

        IMenuManager technologyElementMenuManager = new MenuManager(Messages.ZestView_13);
        elementMenuManager.add(technologyElementMenuManager);
        for(IAction action : fTechnologyElementActions) {
            technologyElementMenuManager.add(action);
        }

        IMenuManager physicalElementMenuManager = new MenuManager(Messages.ZestView_14);
        elementMenuManager.add(physicalElementMenuManager);
        for(IAction action : fPhysicalElementActions) {
            physicalElementMenuManager.add(action);
        }

        IMenuManager motivationElementMenuManager = new MenuManager(Messages.ZestView_15);
        elementMenuManager.add(motivationElementMenuManager);
        for(IAction action : fMotivationElementActions) {
            motivationElementMenuManager.add(action);
        }

        IMenuManager implementationMigrationElementMenuManager = new MenuManager(Messages.ZestView_16);
        elementMenuManager.add(implementationMigrationElementMenuManager);
        for(IAction action : fImplementationMigrationElementActions) {
            implementationMigrationElementMenuManager.add(action);
        }

        IMenuManager otherElementMenuManager = new MenuManager(Messages.ZestView_17);
        elementMenuManager.add(otherElementMenuManager);
        for(IAction action : fOtherElementActions) {
            otherElementMenuManager.add(action);
        }

        return elementMenuManager;
    }
    
    private IMenuManager createRelationshipsMenu() {
        IMenuManager relationshipMenuManager = new MenuManager(Messages.ZestView_6);

        for(IAction action : fRelationshipActions) {
            relationshipMenuManager.add(action);
        }

        return relationshipMenuManager;
    }
    
    @Override
    protected IArchimateModel getActiveArchimateModel() {
        Object input = fGraphViewer.getInput();
        if(input instanceof IDiagramModel diagramModel) {
            return diagramModel.getArchimateModel();
        }
        IArchimateConcept concept = fDrillDownManager.getCurrentConcept();
        return concept != null ? concept.getArchimateModel() : null;
    }
    
    /**
     * @return Casted Content Provider
     */
    protected ZestViewerContentProvider getContentProvider() {
        return (ZestViewerContentProvider)fGraphViewer.getContentProvider();
    }
    
    @Override
    public void dispose() {
        super.dispose();
        
        // Explicit dispose seems to be needed if the GraphViewer is displaying scrollbars
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=373191
        // In fact, Graph.dispose() seems never to be called
        // fGraphViewer.getControl().dispose();
        
        // Unregister selection listener
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
    }
    
    // =================================================================================
    //                       Listen to Editor Model Changes
    // =================================================================================
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        Object newValue = evt.getNewValue();
        
        // Model Closed
        if(propertyName == IEditorModelManager.PROPERTY_MODEL_REMOVED) {
            Object input = getViewer().getInput();
            if(input instanceof IArchimateModelObject && ((IArchimateModelObject)input).getArchimateModel() == newValue) {
                fDrillDownManager.reset();
            }
        }
        // Command Stack - update Actions
        else if(propertyName == IEditorModelManager.COMMAND_STACK_CHANGED) {
            updateActions();
        }
        else {
            super.propertyChange(evt);
        }
    }
    
    // =================================================================================
    //                       React to ECore Model Changes
    // =================================================================================
    
    @Override
    protected void eCoreChanged(Notification msg) {
        doRefresh(msg);
    }
    
    @Override
    protected void doRefreshFromNotifications(List<Notification> notifications) {
        for(Notification msg : notifications) {
            if(doRefresh(msg)) {
                break; // Only need to refresh once
            }
        }
    }
    
    private boolean doRefresh(Notification msg) {
        if(msg.getFeature() == IArchimatePackage.Literals.NAMEABLE__NAME) {
            getViewer().update(msg.getNotifier(), null);
            if(msg.getNotifier() == fDrillDownManager.getCurrentConcept()) {
                updateLabel();
            }
        }
        else if(isRefreshEvent(msg)) {
            // If viewing an iteration chain, rebuild and reposition after refresh
            Object input = fGraphViewer.getInput();
            if(input instanceof IDiagramModel diagramModel) {
            	LinkedList<IDiagramModel> iterChain    = buildIterationChain(diagramModel);
            	LinkedList<IDiagramModel> versionChain = buildVersionChain(diagramModel);
            	fGraphViewer.setInput(diagramModel);
            	Display.getCurrent().asyncExec(() -> positionNodes(iterChain, versionChain));
            }
            else {
                refresh();
            }
            return true;
        }
        return false;
    }

    private boolean isRefreshEvent(Notification msg) {
        // Existing: concept added/removed
        if(msg.getNewValue() instanceof IArchimateConcept || msg.getOldValue() instanceof IArchimateConcept) {
            return true;
        }
        // New: a property value changed on a diagram model (e.g. Previous/Next Iteration set)
        if(msg.getFeature() == IArchimatePackage.Literals.PROPERTY__VALUE
                && msg.getNotifier() instanceof com.archimatetool.model.IProperty) {
            return fGraphViewer.getInput() instanceof IDiagramModel;
        }
        // New: a property was added/removed on a diagram model
        if(msg.getFeature() == IArchimatePackage.Literals.PROPERTIES__PROPERTIES
                && msg.getNotifier() instanceof IDiagramModel) {
            return true;
        }
        return false;
    }
    
    // =================================================================================
    //                       Contextual Help support
    // =================================================================================
    
    @Override
    public int getContextChangeMask() {
        return NONE;
    }

    @Override
    public IContext getContext(Object target) {
        return HelpSystem.getContext(HELP_ID);
    }

    @Override
    public String getSearchExpression(Object target) {
        return Messages.ZestView_2;
    }
}