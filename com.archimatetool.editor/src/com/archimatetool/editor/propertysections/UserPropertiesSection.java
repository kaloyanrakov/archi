/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.editor.propertysections;

import java.net.MalformedURLException;
import java.text.Collator;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.archimatetool.editor.model.commands.EObjectFeatureCommand;
import com.archimatetool.editor.model.commands.EObjectNonNotifyingCompoundCommand;
import com.archimatetool.editor.preferences.IPreferenceConstants;
import com.archimatetool.editor.ui.IArchiImages;
import com.archimatetool.editor.ui.ThemeUtils;
import com.archimatetool.editor.ui.components.GlobalActionDisablementHandler;
import com.archimatetool.editor.ui.components.StringComboBoxCellEditor;
import com.archimatetool.editor.ui.dialog.UserPropertiesKeySelectionDialog;
import com.archimatetool.editor.utils.HTMLUtils;
import com.archimatetool.editor.utils.StringUtils;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IArchimateModelObject;
import com.archimatetool.model.IArchimatePackage;
import com.archimatetool.model.IProperties;
import com.archimatetool.model.IProperty;
import com.archimatetool.model.util.LightweightEContentAdapter;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.editor.propertysections.IterationPropertyDecorator;
import com.archimatetool.editor.propertysections.LevelingPropertyDecorator;
import com.archimatetool.model.IDiagramModel;


/**
 * User Properties Section
 * 
 * @author Phillip Beauvoir
 */
public class UserPropertiesSection extends AbstractECorePropertySection {

    private static final String HELP_ID = "com.archimatetool.help.userProperties"; //$NON-NLS-1$

    /**
     * Filter to show or reject this section depending on input value
     */
    public static class Filter extends ObjectFilter {
        @Override
        public boolean isRequiredType(Object object) {
            return object instanceof IProperties properties
                    && shouldExposeFeature(properties, IArchimatePackage.Literals.PROPERTIES__PROPERTIES.getName());
        }

        @Override
        public Class<?> getAdaptableType() {
            return IProperties.class;
        }
    }

    // Selected properties elements
    private List<IProperties> fPropertiesElements = new ArrayList<>();

    private TableViewer fTableViewer;
    
    private IAction fActionNewProperty, fActionNewMultipleProperties, fActionRemoveProperties, fActionShowKeyEditor;

    private boolean ignoreMessages;
    
    // Indicates multiple property values (unlikely to be a user-given value)
    private static final String multipleValuesIndicator = UUID.randomUUID().toString();
    
    // Maximum amount of items to display when getting all unique keys and values for combo boxes
    private static final int MAX_ITEMS_COMBO = 20000;
    
    // Display all items
    private static final int MAX_ITEMS_ALL = -1;
    
    @Override
    protected void createControls(Composite parent) {
        createTableControl(parent);
        createActionsAndToolbar(parent);
        
        // We are interested in listening to notifications from child IProperty objects
        ((LightweightEContentAdapter)getECoreAdapter()).addClass(IProperty.class);
    }

    @Override
    protected void notifyChanged(Notification msg) {
        super.notifyChanged(msg);
        
        if(msg.getEventType() == EObjectNonNotifyingCompoundCommand.START) {
            ignoreMessages = true;
            return;
        }
        else if(msg.getEventType() == EObjectNonNotifyingCompoundCommand.END) {
            ignoreMessages = false;
            fTableViewer.refresh();
        }

        if(!ignoreMessages) {
            Object feature = msg.getFeature();
            
            if(feature == IArchimatePackage.Literals.LOCKABLE__LOCKED) {
                updateLocked();
            }

            if(feature == IArchimatePackage.Literals.PROPERTIES__PROPERTIES) {
                fTableViewer.refresh();
            }

            if(feature == IArchimatePackage.Literals.PROPERTY__KEY || feature == IArchimatePackage.Literals.PROPERTY__VALUE) {
                if(isMultiSelection()) {
                    fTableViewer.refresh();
                }
                else {
                    fTableViewer.update(msg.getNotifier(), null);
                }
            }
        }
    }
    
    // Returns true for any property key that should show a view-name dropdown
    private static boolean isIterationProperty(String key) {
        if(key == null) return false;
        String lower = key.toLowerCase();
        return lower.equals("previous iteration") || lower.equals("next iteration")
            || lower.equals("previous version") || lower.equals("next version");
    }

    private String[] getAllViewNamesForModel() {
        IArchimateModel model = getArchimateModel();
        if(model == null) return new String[0];
        
        return model.getDiagramModels().stream()
            .map(IDiagramModel::getName)
            .filter(name -> name != null && !name.isBlank())
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toArray(String[]::new);
    }
    
    @Override
    protected void addAdapter() {
        if(getEObjects() != null && getECoreAdapter() != null) {
            for(IArchimateModelObject eObject : getEObjects()) {
                if(!eObject.eAdapters().contains(getECoreAdapter())) {
                    eObject.eAdapters().add(getECoreAdapter());
                }
            }
        }
    }
    
    private static final String[] MODEL_LEVEL_VALUES = {
            "Level 1",
            "Level 2",
            "Level 3"
        };
    
    private static boolean isReadOnlyProperty(String key) {
        return "Model Level".equals(key)
            || "Previous Iteration".equals(key)
            || "Next Iteration".equals(key)
            || "Previous Version".equals(key)
            || "Next Version".equals(key);
    }
    
    @Override
    protected void removeAdapter() {
        if(getEObjects() != null && getECoreAdapter() != null) {
            for(IArchimateModelObject eObject : getEObjects()) {
                eObject.eAdapters().remove(getECoreAdapter());
            }
        }
    }

    @Override
    protected void update() {
        // Get selected properties elements
        fPropertiesElements = new ArrayList<>();
        
        for(IArchimateModelObject obj : getEObjects()) {
            if(obj instanceof IProperties p) {
                fPropertiesElements.add(p);
            }
        }
        
        fTableViewer.setInput(fPropertiesElements);
        
        // Locked
        updateLocked();
        injectMissingProperties();
    }
    
    private void injectMissingProperties() {
        if(fPropertiesElements.isEmpty()) return;

        for(IProperties target : fPropertiesElements) {
            if(!(target instanceof IArchimateElement) && !(target instanceof IDiagramModel)) {
                continue;
            }

            boolean isView = target instanceof IDiagramModel;
            boolean changed = false;
            ((org.eclipse.emf.ecore.EObject)target).eSetDeliver(false);
            try {
            	if(isView) {
            	    boolean removed = target.getProperties().removeIf(p -> "Model Level".equals(p.getKey()));
            	    if(removed) changed = true;
            	}
            	else {
            	    boolean removed = target.getProperties().removeIf(p -> 
            	        IterationPropertyDecorator.PROPERTY_PREVIOUS_ITERATION.equals(p.getKey()) ||
            	        IterationPropertyDecorator.PROPERTY_NEXT_ITERATION.equals(p.getKey()) ||
            	        IterationPropertyDecorator.PROPERTY_PREVIOUS_VERSION.equals(p.getKey()) ||
            	        IterationPropertyDecorator.PROPERTY_NEXT_VERSION.equals(p.getKey()));
            	    if(removed) changed = true;
            	}

                // ADD missing properties
                for(IPropertyDecorator decorator : PropertyDecoratorRegistry.getAllDecorators()) {
                    boolean shouldAdd = false;
                    
                    if(decorator instanceof IterationPropertyDecorator) {
                        shouldAdd = isView;
                    }
                    else if(decorator instanceof LevelingPropertyDecorator) {
                        shouldAdd = !isView;
                    }
                    
                    if(!shouldAdd) continue;
                    
                    String key = decorator.getPropertyKey();
                    boolean alreadyExists = target.getProperties().stream()
                        .anyMatch(p -> key.equals(p.getKey()));
                    if(!alreadyExists) {
                        IProperty newProperty = IArchimateFactory.eINSTANCE.createProperty();
                        newProperty.setKey(key);
                        newProperty.setValue(""); //$NON-NLS-1$
                        target.getProperties().add(newProperty);
                        changed = true;
                    }
                }
            }
            finally {
                ((org.eclipse.emf.ecore.EObject)target).eSetDeliver(true);
            }

            if(changed) {
                fTableViewer.refresh();
            }
        }
    }
    
    private void updateLocked() {
        boolean locked = isLocked(getFirstSelectedObject());
        fTableViewer.getTable().setEnabled(!locked);
        fActionNewProperty.setEnabled(!locked);
        fActionRemoveProperties.setEnabled(!locked && !fTableViewer.getSelection().isEmpty());
        fActionNewMultipleProperties.setEnabled(!locked);
    }

    @Override
    protected IObjectFilter getFilter() {
        return new Filter();
    }
    
    @Override
    public boolean shouldUseExtraSpace() {
        return true;
    }

    /**
     * Create table
     */
    private void createTableControl(Composite parent) {
        // Table Composite
        Composite tableComp = createTableComposite(parent, SWT.NULL);
        TableColumnLayout tableLayout = (TableColumnLayout)tableComp.getLayout();
        
        // Table Viewer
        fTableViewer = new TableViewer(tableComp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);

        // Set CSS ID
        ThemeUtils.registerCssId(fTableViewer.getTable(), "UserPropertiesTable"); //$NON-NLS-1$
        
        // Set font in case CSS theming is disabled
        ThemeUtils.setFontIfCssThemingDisabled(fTableViewer.getTable(), IPreferenceConstants.PROPERTIES_TABLE_FONT);

        // Edit cell on double-click and add Tab key traversal
        TableViewerEditor.create(fTableViewer, new ColumnViewerEditorActivationStrategy(fTableViewer) {
            @Override
            protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
                return super.isEditorActivationEvent(event) ||
                      (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION);
            }
            
        }, ColumnViewerEditor.TABBING_HORIZONTAL | 
                ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | 
                ColumnViewerEditor.TABBING_VERTICAL |
                ColumnViewerEditor.KEEP_EDITOR_ON_DOUBLE_CLICK |
                ColumnViewerEditor.KEYBOARD_ACTIVATION);
        
        fTableViewer.getTable().setHeaderVisible(true);
        fTableViewer.getTable().setLinesVisible(true);

        addDragSupport();
        addDropSupport();

        // Help ID on table
        PlatformUI.getWorkbench().getHelpSystem().setHelp(fTableViewer.getTable(), HELP_ID);

        // Columns
        TableViewerColumn columnBlank = new TableViewerColumn(fTableViewer, SWT.NONE, 0);
        tableLayout.setColumnData(columnBlank.getColumn(), new ColumnWeightData(3, false));
        columnBlank.getColumn().setWidth(38);

        TableViewerColumn columnKey = new TableViewerColumn(fTableViewer, SWT.NONE, 1);
        columnKey.getColumn().setText(Messages.UserPropertiesSection_0);
        tableLayout.setColumnData(columnKey.getColumn(), new ColumnWeightData(30, true));
        columnKey.setEditingSupport(new KeyEditingSupport(fTableViewer));

        // Click on Key Table Header
        columnKey.getColumn().addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                sortKeys();
            }
        });

        TableViewerColumn columnValue = new TableViewerColumn(fTableViewer, SWT.NONE, 2);
        columnValue.getColumn().setText(Messages.UserPropertiesSection_1);
        tableLayout.setColumnData(columnValue.getColumn(), new ColumnWeightData(67, true));
        columnValue.setEditingSupport(new ValueEditingSupport(fTableViewer));

        // Content Provider
        fTableViewer.setContentProvider(new TableContentProvider());

        // Label Provider
        fTableViewer.setLabelProvider(new LabelCellProvider());
        
        // Enable tooltips
        ColumnViewerToolTipSupport.enableFor(fTableViewer);

        /*
         * Selection Listener
         */
        fTableViewer.addSelectionChangedListener(e -> {
            fActionRemoveProperties.setEnabled(!e.getSelection().isEmpty());
        });

        /*
         * Table Double-click
         */
        fTableViewer.getTable().addMouseListener(MouseListener.mouseDoubleClickAdapter(e -> {
            // Get Table item
            Point pt = new Point(e.x, e.y);
            TableItem item = fTableViewer.getTable().getItem(pt);
            
            // Double-click into empty table creates new Property
            if(item == null) {
                fActionNewProperty.run();                    
            }
            // Double-clicked in column 0 with item
            else if(item.getData() instanceof IProperty) {
                Rectangle rect = item.getBounds(0);
                if(rect.contains(pt)) {
                    handleDoubleClick((IProperty)item.getData());
                }
            }
        }));
        
        /*
         * Edit table row on key press
         */
        fTableViewer.getTable().addKeyListener(KeyListener.keyPressedAdapter(e -> {
            if(e.keyCode == SWT.CR) {
                Object selected = fTableViewer.getStructuredSelection().getFirstElement();
                if(selected != null) {
                    fTableViewer.editElement(selected, 1);
                }
            }
        }));
    }
    
    /**
     * Create actions, local toolbar and context menu
     */
    private void createActionsAndToolbar(Composite parent) {
        // New Property Action
        fActionNewProperty = new NewPropertyAction();

        // New Multiple Properties Action
        fActionNewMultipleProperties = new NewMultiplePropertiesAction();

        // Remove Properties Action
        fActionRemoveProperties = new RemovePropertiesAction();

        // Manage Keys Action
        fActionShowKeyEditor = new ShowKeyEditorAction();

        // Toolbar
        ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.VERTICAL);
        getWidgetFactory().adapt(toolBar);
        GridDataFactory.fillDefaults().align(SWT.END, SWT.TOP).applyTo(toolBar);

        ToolBarManager toolBarmanager = new ToolBarManager(toolBar);
        toolBarmanager.add(fActionNewProperty);
        toolBarmanager.add(fActionNewMultipleProperties);
        toolBarmanager.add(fActionRemoveProperties);
        toolBarmanager.add(fActionShowKeyEditor);
        toolBarmanager.update(true);

        // Hook into the context menu
        MenuManager menuMgr = new MenuManager("#PropertiesPopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(manager -> {
            manager.add(fActionNewProperty);
            manager.add(fActionNewMultipleProperties);
            manager.add(new Separator());
            manager.add(fActionRemoveProperties);
            manager.add(new Separator());
            manager.add(fActionShowKeyEditor);
        });

        Menu menu = menuMgr.createContextMenu(fTableViewer.getControl());
        fTableViewer.getControl().setMenu(menu);
    }

    /**
     * Sort Keys
     */
    private void sortKeys() {
        if(!isMultiSelection() && isAlive(getFirstSelectedElement())) {
            executeCommand(new SortPropertiesCommand(getFirstSelectedElement().getProperties()));
        }
    }

    /**
     * If it's a URL open in Browser
     */
    private void handleDoubleClick(IProperty selected) {
        Matcher matcher = HTMLUtils.HTML_LINK_PATTERN.matcher(selected.getValue());
        if(matcher.find()) {
            String href = matcher.group();
            try {
                HTMLUtils.openLinkInBrowser(href);
            }
            catch(PartInitException | MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    // -----------------------------------------------------------------------------------------------------------------
    //
    // Properties handling
    //
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * @return true if more than one element is selected
     */
    private boolean isMultiSelection() {
        return fPropertiesElements.size() > 1;
    }
    
    /**
     * @return The first selected element with properties, or null
     */
    private IProperties getFirstSelectedElement() {
        return fPropertiesElements.size() == 0 ? null : fPropertiesElements.get(0);
    }
    
    /**
     * @return a list of common properties for selected elements that share the same key
     */
    private List<IProperty> getCommonProperties() {
        List<IProperty> properties = new ArrayList<>();
        Map<String, Entry<Set<String>, Set<IProperties>>> map = new LinkedHashMap<>(); // Map of key -> values -> elements
        
        // Iterate thru all selected elements and their properties
        for(IProperties propertiesElement : fPropertiesElements) {
            for(IProperty property : propertiesElement.getProperties()) {
                // Get the first matching property in case the element has more than one with the same key
                property = getFirstMatchingProperty(propertiesElement.getProperties(), property);
                
                // Do we have this property key in the map?
                Entry<Set<String>, Set<IProperties>> entry = map.get(property.getKey());
                
                // No, create and add a new entry
                if(entry == null) {
                    entry = new SimpleEntry<>(new HashSet<>(), new HashSet<>());
                    map.put(property.getKey(), entry);
                }
                
                entry.getKey().add(property.getValue()); // Add the property value
                entry.getValue().add(propertiesElement); // Add the properties element
            }
        }
        
        for(Entry<String, Entry<Set<String>, Set<IProperties>>> entry : map.entrySet()) {
            // If the size of elements equals the size of selected elements, then they all have the property key in common
            if(entry.getValue().getValue().size() == fPropertiesElements.size()) {
                // If there is only one value, use that, else use the multipleValuesIndicator
                IProperty property = IArchimateFactory.eINSTANCE.createProperty(entry.getKey(),
                                                                                entry.getValue().getKey().size() == 1 ? entry.getValue().getKey().iterator().next() : multipleValuesIndicator);
                properties.add(property);
            }
        }
        
        return properties;
    }
    
    /**
     * @return the first matching property in properties that matches the given property
     */
    private IProperty getFirstMatchingProperty(List<IProperty> properties, IProperty property) {
        for(IProperty p : properties) {
            if(Objects.equals(p.getKey(), property.getKey())) { // match on key
                return p;
            }
        }
        return null;
    }
    
    /**
     * Return the Archimate model bound to the properties element
     */
    private IArchimateModel getArchimateModel() {
        if(getFirstSelectedElement() instanceof IArchimateModelObject) {
            return ((IArchimateModelObject)getFirstSelectedElement()).getArchimateModel();
        }
        return null;
    }
    
    /**
     * @return All unique Property Keys for an entire model (sorted)
     */
    private String[] getAllUniquePropertyKeysForModel(int maxSize) {
        IArchimateModel model = getArchimateModel();
        Set<String> set = new LinkedHashSet<>(); // LinkedHashSet is faster when sorting
        
        for(Iterator<EObject> iter = model.eAllContents(); iter.hasNext();) {
            EObject element = iter.next();
            if(element instanceof IProperty p) {
                if(maxSize != MAX_ITEMS_ALL && set.size() > maxSize) { // Don't get more than this
                    break;
                }
                String key = p.getKey();
                if(StringUtils.isSetAfterTrim(key)) {
                    set.add(key);
                }
            }
        }

        String[] items = set.toArray(new String[set.size()]);
        Arrays.sort(items, (s1, s2) -> s1.compareToIgnoreCase(s2)); // Don't use Collator.getInstance() as it's too slow

        return items;
    }
    
    /**
     * @return All unique Property Values for an entire model (sorted)
     */
    private String[] getAllUniquePropertyValuesForKeyForModel(String key, int maxSize) {
        IArchimateModel model = getArchimateModel();
        Set<String> set = new LinkedHashSet<>(); // LinkedHashSet is faster when sorting

        for(Iterator<EObject> iter = model.eAllContents(); iter.hasNext();) {
            EObject element = iter.next();
            if(element instanceof IProperty p) {
                if(maxSize != MAX_ITEMS_ALL && set.size() > maxSize) { // Don't get more than this
                    break;
                }
                if(p.getKey().equals(key)) {
                    String value = p.getValue();
                    if(StringUtils.isSetAfterTrim(value)) {
                        set.add(value);
                    }
                }
            }
        }

        String[] items = set.toArray(new String[set.size()]);
        Arrays.sort(items, (s1, s2) -> s1.compareToIgnoreCase(s2)); // Don't use Collator.getInstance() as it's too slow

        return items;
    }


    // -----------------------------------------------------------------------------------------------------------------
    //
    // Table functions
    //
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Content Provider
     */
    private class TableContentProvider implements IStructuredContentProvider {
        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if(isMultiSelection()) {
                return getCommonProperties().toArray();
            }

            if(!isAlive(getFirstSelectedElement())) {
                return new Object[0];
            }

            List<IProperty> all = new ArrayList<>(getFirstSelectedElement().getProperties());
            boolean isView = getFirstSelectedElement() instanceof IDiagramModel;

            List<String> managedKeys = new ArrayList<>();
            for(IPropertyDecorator decorator : PropertyDecoratorRegistry.getAllDecorators()) {
                // Only include decorator if appropriate for this element type
                if(decorator instanceof IterationPropertyDecorator && isView) {
                    managedKeys.add(decorator.getPropertyKey());
                }
                else if(decorator instanceof LevelingPropertyDecorator && !isView) {
                    managedKeys.add(decorator.getPropertyKey());
                }
            }

            List<IProperty> managed = new ArrayList<>();
            List<IProperty> user    = new ArrayList<>();

            for(String key : managedKeys) {
                all.stream()
                    .filter(p -> key.equals(p.getKey()))
                    .findFirst()
                    .ifPresent(managed::add);
            }

            for(IProperty p : all) {
                if(!managedKeys.contains(p.getKey())) {
                    // Also hide Model Level on views even if it was previously saved
                    if(isView && "Model Level".equals(p.getKey())) continue;
                    user.add(p);
                }
            }

            managed.addAll(user);
            return managed.toArray();
        }
    }

    /**
     * Label Provider
     */
    private static class LabelCellProvider extends CellLabelProvider {
        @Override
        public void update(ViewerCell cell) {
            cell.setText(getColumnText(cell.getElement(), cell.getColumnIndex()));
            cell.setForeground(getForeground(cell.getElement(), cell.getColumnIndex()));
            cell.setImage(getColumnImage(cell.getElement(), cell.getColumnIndex()));
        }
        
        public Image getColumnImage(Object element, int columnIndex) {
            if(columnIndex == 0) {
                return isLink(element) ? IArchiImages.ImageFactory.getImage(IArchiImages.ICON_BROWSER) : null;
            }
            
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            switch(columnIndex) {
                case 1:
                    String key = ((IProperty)element).getKey();
                    return StringUtils.isSetAfterTrim(key) ? key : Messages.UserPropertiesSection_9;

                case 2:
                    String value = ((IProperty)element).getValue();
                    return multipleValuesIndicator.equals(value) ? Messages.UserPropertiesSection_22 : value;

                default:
                    return null;
            }
        }

        public Color getForeground(Object element, int columnIndex) {
            if(columnIndex == 2) {
                return isLink(element) ? ColorConstants.blue : null;
            }
            return null;
        }

        @Override
        public String getToolTipText(Object element) {
            return isLink(element) ? Messages.UserPropertiesSection_21 : null;
        }
        
        private boolean isLink(Object element) {
            Matcher matcher = HTMLUtils.HTML_LINK_PATTERN.matcher(((IProperty)element).getValue());
            return matcher.find();
        }
    }

    /**
     * Key Editor
     */
    private class KeyEditingSupport extends EditingSupport {
        StringComboBoxCellEditor cellEditor;

        public KeyEditingSupport(ColumnViewer viewer) {
            super(viewer);
            cellEditor = new StringComboBoxCellEditor((Composite)viewer.getControl(), new String[0], true);
            
            // Nullify some global Action Handlers so that this cell editor can handle them
            hookCellEditorGlobalActionHandler(cellEditor);
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            String[] items = isAlive(getFirstSelectedElement()) ? getAllUniquePropertyKeysForModel(MAX_ITEMS_COMBO) : new String[0];
            cellEditor.setItems(items);
            return cellEditor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return !isReadOnlyProperty(((IProperty)element).getKey());
        }

        @Override
        protected Object getValue(Object element) {
            return ((IProperty)element).getKey();
        }

        @Override
        protected void setValue(Object element, Object value) {
            CompoundCommand compoundCmd = new CompoundCommand();

            for(IProperties propertiesElement : fPropertiesElements) {
                IProperty property = (IProperty)element;
                
                if(isAlive(propertiesElement)) {
                    if(isMultiSelection()) {
                        property = getFirstMatchingProperty(propertiesElement.getProperties(), property);
                    }
                    if(property != null) {
                        Command cmd = new EObjectFeatureCommand(Messages.UserPropertiesSection_10, property, IArchimatePackage.Literals.PROPERTY__KEY, value);
                        if(cmd.canExecute()) {
                            compoundCmd.add(cmd);
                        }
                    }
                }
            }
            
            if(isMultiSelection()) {
                try {
                    ignoreMessages = true;
                    executeCommand(compoundCmd.unwrap());
                    ((IProperty)element).setKey((String)value);
                    fTableViewer.update(element, null);
                }
                finally {
                    ignoreMessages = false;
                }
            }
            else {
                executeCommand(compoundCmd.unwrap());
            }
        }
    }

    /**
     * Value Editor
     */
    private class ValueEditingSupport extends EditingSupport {
        StringComboBoxCellEditor cellEditor;

        public ValueEditingSupport(ColumnViewer viewer) {
            super(viewer);
            cellEditor = new StringComboBoxCellEditor((Composite)viewer.getControl(), new String[0], true);
            
            // Nullify some global Action Handlers so that this cell editor can handle them
            hookCellEditorGlobalActionHandler(cellEditor);
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            IProperty property = (IProperty)element;
            String[] items;
            
            if(isIterationProperty(property.getKey())) {
                items = getAllViewNamesForModel();
            }
            else if("Model Level".equalsIgnoreCase(property.getKey())) {
                items = MODEL_LEVEL_VALUES;
            }
            else {
                items = isAlive(getFirstSelectedElement())
                    ? getAllUniquePropertyValuesForKeyForModel(property.getKey(), MAX_ITEMS_COMBO)
                    : new String[0];
            }
            
            cellEditor.setItems(items);
            cellEditor.setEditable(!isReadOnlyProperty(property.getKey()));
            return cellEditor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            String value = ((IProperty)element).getValue();
            return multipleValuesIndicator.equals(value) ? "" : value; //$NON-NLS-1$
        }

        @Override
        protected void setValue(Object element, Object value) {
            CompoundCommand compoundCmd = new CompoundCommand();

            for(IProperties propertiesElement : fPropertiesElements) {
                IProperty property = (IProperty)element;
                
                if(isAlive(propertiesElement)) {
                    if(isMultiSelection()) {
                        property = getFirstMatchingProperty(propertiesElement.getProperties(), property);
                    }
                    if(property != null) {
                        Command cmd = new EObjectFeatureCommand(Messages.UserPropertiesSection_11, property, IArchimatePackage.Literals.PROPERTY__VALUE, value);
                        if(cmd.canExecute()) {
                            compoundCmd.add(cmd);
                        }
                    }
                }
            }
            
            // Decorator side effects
            if(element instanceof IProperty p) {
                IPropertyDecorator decorator = PropertyDecoratorRegistry.getDecorator(p.getKey());
                if(decorator != null) {
                    for(IProperties propertiesElement : fPropertiesElements) {
                        // Call the IProperties version which handles both IArchimateElement and IDiagramModel
                        decorator.contributeCommands(propertiesElement, (String)value, compoundCmd);
                    }
                }
            }
            
            if(isMultiSelection()) {
                try {
                    ignoreMessages = true;
                    executeCommand(compoundCmd.unwrap());
                    ((IProperty)element).setValue((String)value);
                    fTableViewer.update(element, null);
                }
                finally {
                    ignoreMessages = false;
                }
            }
            else {
                executeCommand(compoundCmd.unwrap());
            }
        }
    }
    
    /**
     * Set some editing global Action Handlers to null when the cell editor is activated
     * And restore them when the cell editor is deactivated.
     */
    private void hookCellEditorGlobalActionHandler(CellEditor cellEditor) {
        Listener listener = new Listener() {
            GlobalActionDisablementHandler propertiesViewGlobalActionHandler, globalActionHandler;
            
            @Override
            public void handleEvent(Event event) {
                switch(event.type) {
                    case SWT.Activate:
                        IActionBars actionBars = getTabbedPropertySheetPage().getSite().getActionBars();
                        propertiesViewGlobalActionHandler = new GlobalActionDisablementHandler(actionBars);
                        propertiesViewGlobalActionHandler.clearGlobalActions();
                        globalActionHandler = new GlobalActionDisablementHandler();
                        globalActionHandler.update();
                        break;

                    case SWT.Deactivate:
                        if(propertiesViewGlobalActionHandler != null) {
                            propertiesViewGlobalActionHandler.restoreGlobalActions();
                            globalActionHandler.update();
                        }
                        break;

                    default:
                        break;
                }
            }
        };
        
        cellEditor.getControl().addListener(SWT.Activate, listener);
        cellEditor.getControl().addListener(SWT.Deactivate, listener);
    }

    // -----------------------------------------------------------------------------------------------------------------
    //
    // Table Drag & Drop
    //
    // -----------------------------------------------------------------------------------------------------------------

    private boolean fDragSourceValid;

    private void addDragSupport() {
        int operations = DND.DROP_MOVE;
        Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer.getTransfer() };
        fTableViewer.addDragSupport(operations, transferTypes, new DragSourceListener() {
            @Override
            public void dragFinished(DragSourceEvent event) {
                LocalSelectionTransfer.getTransfer().setSelection(null);
                fDragSourceValid = false;
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = LocalSelectionTransfer.getTransfer().getSelection();
            }

            @Override
            public void dragStart(DragSourceEvent event) {
                if(isMultiSelection()) {
                    event.doit = false;
                }
                else if(isAlive(getFirstSelectedElement())) {
                    IStructuredSelection selection = (IStructuredSelection)fTableViewer.getSelection();
                    LocalSelectionTransfer.getTransfer().setSelection(selection);
                    event.doit = true;
                    fDragSourceValid = true;
                }
            }
        });
    }

    private void addDropSupport() {
        int operations = DND.DROP_MOVE;
        Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer.getTransfer() };
        fTableViewer.addDropSupport(operations, transferTypes, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetEvent event) {
            }

            @Override
            public void dragLeave(DropTargetEvent event) {
            }

            @Override
            public void dragOperationChanged(DropTargetEvent event) {
                event.detail = getEventDetail(event);
            }

            @Override
            public void dragOver(DropTargetEvent event) {
                event.detail = getEventDetail(event);

                if(event.detail == DND.DROP_NONE) {
                    event.feedback = DND.FEEDBACK_NONE;
                }
                else {
                    event.feedback = getDragFeedbackType(event);
                }

                event.feedback |= DND.FEEDBACK_SCROLL;
            }

            @Override
            public void drop(DropTargetEvent event) {
                doDropOperation(event);
            }

            @Override
            public void dropAccept(DropTargetEvent event) {
                event.detail = getEventDetail(event);
            }

            private int getEventDetail(DropTargetEvent event) {
                return fDragSourceValid ? DND.DROP_MOVE : DND.DROP_NONE;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void doDropOperation(DropTargetEvent event) {
        if(!LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)){
            return;
        }

        ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
        if(selection == null || selection.isEmpty()) {
            return;
        }

        int index = getDropTargetPosition(event);

        List<?> list = ((IStructuredSelection)selection).toList();
        for(Object o : list) {
            IProperty property = (IProperty)o;
            int movedIndex = getFirstSelectedElement().getProperties().indexOf(property);
            if(movedIndex == index || (movedIndex + 1) == index) {
                return;
            }
        }

        movePropertiesToIndex((List<IProperty>)list, index);
    }

    private void movePropertiesToIndex(List<IProperty> propertiesToMove, int index) {
        EList<IProperty> properties = getFirstSelectedElement().getProperties();

        if(index < 0) {
            index = 0;
        }
        if(index > properties.size()) {
            index = properties.size();
        }

        CompoundCommand compoundCmd = new CompoundCommand(Messages.UserPropertiesSection_8);

        for(IProperty property : propertiesToMove) {
            int oldIndex = properties.indexOf(property);

            if(index > oldIndex) {
                index--;
            }

            if(index == oldIndex) {
                break;
            }

            compoundCmd.add(new MovePropertyCommand(properties, property, index));

            index++;
        }

        executeCommand(compoundCmd.unwrap());
    }

    private int getDropTargetPosition(DropTargetEvent event) {
        int index = -1;

        Point pt = fTableViewer.getControl().toControl(event.x, event.y);

        if(pt.y < 5) {
            index = 0;
        }
        else if(event.item != null) {
            IProperty property = (IProperty)event.item.getData();
            index = getFirstSelectedElement().getProperties().indexOf(property);
        }
        else {
            index = getFirstSelectedElement().getProperties().size();
        }

        int feedback = getDragFeedbackType(event);
        if(feedback == DND.FEEDBACK_INSERT_AFTER) {
            index += 1;
        }

        return index;
    }

    private int getDragFeedbackType(DropTargetEvent event) {
        if(event.item == null) {
            return DND.FEEDBACK_NONE;
        }

        Rectangle rect = ((TableItem)event.item).getBounds();
        Point pt = fTableViewer.getControl().toControl(event.x, event.y);
        if(pt.y < rect.y + 3) {
            return DND.FEEDBACK_INSERT_BEFORE;
        }
        if(pt.y > rect.y + rect.height - 3) {
            return DND.FEEDBACK_INSERT_AFTER;
        }

        return DND.FEEDBACK_NONE;
    }

    // -----------------------------------------------------------------------------------------------------------------
    //
    // Actions
    //
    // -----------------------------------------------------------------------------------------------------------------
    
    private class NewPropertyAction extends Action {
        private NewPropertyAction() {
            super(Messages.UserPropertiesSection_2);
            setToolTipText(Messages.UserPropertiesSection_2);
            setImageDescriptor(IArchiImages.ImageFactory.getImageDescriptor(IArchiImages.ICON_PLUS));
        }

        @Override
        public void run() {
            fTableViewer.applyEditorValue();
            IProperty newProperty = null;
            
            if(isMultiSelection()) {
                CompoundCommand cmd = new CompoundCommand();
                
                for(IProperties propertiesElement : fPropertiesElements) {
                    if(isAlive(propertiesElement)) {
                        IProperty property = IArchimateFactory.eINSTANCE.createProperty();
                        cmd.add(new NewPropertyCommand(propertiesElement.getProperties(), property, -1));
                    }
                }
                
                executeCommand(cmd.unwrap());
                
                if(fTableViewer.getTable().getItemCount() > 0) {
                    newProperty = (IProperty)fTableViewer.getElementAt(fTableViewer.getTable().getItemCount() - 1);
                }
            }
            else if(isAlive(getFirstSelectedElement())) {
                newProperty = IArchimateFactory.eINSTANCE.createProperty();
                int index = -1;
                IProperty selected = (IProperty)((IStructuredSelection)fTableViewer.getSelection()).getFirstElement();
                if(selected != null) {
                    index = getFirstSelectedElement().getProperties().indexOf(selected) + 1;
                }
                
                executeCommand(new NewPropertyCommand(getFirstSelectedElement().getProperties(), newProperty, index));
            }
            
            if(newProperty != null) {
                fTableViewer.editElement(newProperty, 1);
            }
        }
    }
    
    private class NewMultiplePropertiesAction extends Action {
        private NewMultiplePropertiesAction() {
            super(Messages.UserPropertiesSection_3);
            setToolTipText(Messages.UserPropertiesSection_3);
            setImageDescriptor(IArchiImages.ImageFactory.getImageDescriptor(IArchiImages.ICON_MUTIPLE));
        }

        @Override
        public void run() {
            if(isAlive(getFirstSelectedElement())) {
                MultipleAddDialog dialog = new MultipleAddDialog(getTabbedPropertySheetPage().getSite().getShell(), List.of(getAllUniquePropertyKeysForModel(MAX_ITEMS_ALL)));
                if(dialog.open() != Window.CANCEL) {
                    List<String> newKeys = dialog.getSelectedKeys();
                    if(newKeys == null || newKeys.isEmpty()) {
                        return;
                    }
                    
                    CompoundCommand cmd = isMultiSelection() ? new CompoundCommand(Messages.UserPropertiesSection_20) : 
                                                               new EObjectNonNotifyingCompoundCommand(getFirstSelectedElement(), Messages.UserPropertiesSection_20);
                    
                    boolean addUnique = dialog.getReturnCode() == IDialogConstants.CLIENT_ID;
                    
                    for(IProperties propertiesElement : fPropertiesElements) {
                        if(isAlive(propertiesElement)) {
                            for(String key : newKeys) {
                                if(!(addUnique && hasPropertyKey(propertiesElement, key))) {
                                    IProperty property = IArchimateFactory.eINSTANCE.createProperty(key, ""); //$NON-NLS-1$
                                    cmd.add(new NewPropertyCommand(propertiesElement.getProperties(), property, -1));
                                }
                            }
                        }
                    }
                    
                    executeCommand(cmd.unwrap());
                }
            }
        }
        
        private boolean hasPropertyKey(IProperties propertiesElement, String key) {
            for(IProperty property : propertiesElement.getProperties()) {
                if(key.equals(property.getKey())) {
                    return true;
                }
            }
            return false;
        }
    }

    private class RemovePropertiesAction extends Action {
        private RemovePropertiesAction() {
            super(Messages.UserPropertiesSection_4);
            setToolTipText(Messages.UserPropertiesSection_4);
            setImageDescriptor(IArchiImages.ImageFactory.getImageDescriptor(IArchiImages.ICON_SMALL_X));
            setEnabled(false);
        }

        @Override
        public void run() {
            CompoundCommand cmd = isMultiSelection() ? new CompoundCommand() : new EObjectNonNotifyingCompoundCommand(getFirstSelectedElement());
            
            for(Object o : ((IStructuredSelection)fTableViewer.getSelection()).toList()) {
                IProperty selectedProperty = (IProperty)o;
                if(isReadOnlyProperty(selectedProperty.getKey())) {
                    continue; // skip protected properties
                }
                for(IProperties propertiesElement : fPropertiesElements) {
                    if(isAlive(propertiesElement)) {
                        IProperty property = selectedProperty;
                        if(isMultiSelection()) {
                            property = getFirstMatchingProperty(propertiesElement.getProperties(), property);
                        }
                        if(property != null) {
                            cmd.add(new RemovePropertyCommand(propertiesElement.getProperties(), property));
                        }
                    }
                }
            }
            
            executeCommand(cmd);
        }
    }

    private class ShowKeyEditorAction extends Action {
        private ShowKeyEditorAction() {
            super(Messages.UserPropertiesSection_7);
            setToolTipText(Messages.UserPropertiesSection_7);
            setImageDescriptor(IArchiImages.ImageFactory.getImageDescriptor(IArchiImages.ICON_COG));
        }

        @Override
        public void run() {
            if(isAlive(getFirstSelectedElement())) {
                UserPropertiesManagerDialog dialog = new UserPropertiesManagerDialog(getTabbedPropertySheetPage().getSite().getShell(),
                        getArchimateModel());
                dialog.open();
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    //
    // Commands
    //
    // -----------------------------------------------------------------------------------------------------------------

    private static class NewPropertyCommand extends Command {
        private EList<IProperty> properties;
        private IProperty property;
        private int index;

        NewPropertyCommand(EList<IProperty> properties, IProperty property, int index) {
            this.properties = properties;
            this.property = property;
            this.index = index;
            setLabel(Messages.UserPropertiesSection_12);
        }

        @Override
        public void execute() {
            if(index < 0 || index > properties.size()) {
                properties.add(property);
            }
            else {
                properties.add(index, property);
            }
        }

        @Override
        public void undo() {
            properties.remove(property);
        }

        @Override
        public void dispose() {
            properties = null;
            property = null;
        }
    }

    private static class RemovePropertyCommand extends Command {
        private EList<IProperty> properties;
        private IProperty property;
        private int index;

        RemovePropertyCommand(EList<IProperty> properties, IProperty property) {
            this.properties = properties;
            this.property = property;
            setLabel(Messages.UserPropertiesSection_5);
        }

        @Override
        public void execute() {
            index = properties.indexOf(property); 
            if(index != -1) {
                properties.remove(property);
            }
        }

        @Override
        public void undo() {
            if(index != -1) {
                properties.add(index, property);
            }
        }

        @Override
        public void dispose() {
            properties = null;
            property = null;
        }
    }

    private static class MovePropertyCommand extends Command {
        private EList<IProperty> properties;
        private IProperty property;
        private int oldIndex;
        private int newIndex;

        MovePropertyCommand(EList<IProperty> properties, IProperty property, int newIndex) {
            this.properties = properties;
            this.property = property;
            this.newIndex = newIndex;
            setLabel(Messages.UserPropertiesSection_13);
        }

        @Override
        public void execute() {
            oldIndex = properties.indexOf(property);
            properties.move(newIndex, property);
        }

        @Override
        public void undo() {
            properties.move(oldIndex, property);
        }

        @Override
        public void dispose() {
            properties = null;
            property = null;
        }
    }

    private static class SortPropertiesCommand extends Command {
        private EList<IProperty> properties;
        private List<IProperty> original;
        private Collator collator = Collator.getInstance();

        public SortPropertiesCommand(EList<IProperty> properties) {
            setLabel(Messages.UserPropertiesSection_14);
            this.properties = properties;
            original = new ArrayList<IProperty>(properties);
        }

        @Override
        public boolean canExecute() {
            if(properties.size() < 2) {
                return false;
            }

            List<IProperty> temp = new ArrayList<IProperty>(properties);
            Collections.sort(temp, (p1, p2) -> collator.compare(p1.getKey(), p2.getKey()));

            for(int i = 0; i < temp.size(); i++) {
                if(temp.get(i) != properties.get(i)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public void execute() {
            ECollections.sort(properties, (p1, p2) -> collator.compare(p1.getKey(), p2.getKey()));
        }

        @Override
        public void undo() {
            properties.clear();
            properties.addAll(original);
        }

        @Override
        public void dispose() {
            properties = null;
            original = null;
            collator = null;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    //
    // Multiple Add Dialog
    //
    // -----------------------------------------------------------------------------------------------------------------

    private static class MultipleAddDialog extends UserPropertiesKeySelectionDialog {
        public MultipleAddDialog(Shell parentShell, List<String> keys) {
            super(parentShell, keys, null);
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite)super.createDialogArea(parent);
            setTitle(Messages.UserPropertiesSection_16);
            setMessage(Messages.UserPropertiesSection_17);
            return composite;
        }

        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            createButton(parent, IDialogConstants.OK_ID, Messages.UserPropertiesSection_6, true);
            createButton(parent, IDialogConstants.CLIENT_ID, Messages.UserPropertiesSection_23, false);
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
            
            if(keys.size() == 0) {
                getButton(IDialogConstants.CLIENT_ID).setEnabled(false);
            }
        }
    }

}