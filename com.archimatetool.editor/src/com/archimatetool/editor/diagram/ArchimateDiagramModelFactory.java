/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.editor.diagram;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorPart;

import com.archimatetool.editor.ArchiPlugin;
import com.archimatetool.editor.preferences.IPreferenceConstants;
import com.archimatetool.editor.ui.ArchiLabelProvider;
import com.archimatetool.editor.ui.ColorFactory;
import com.archimatetool.editor.ui.factory.IGraphicalObjectUIProvider;
import com.archimatetool.editor.ui.factory.ObjectUIFactory;
import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimateRelationship;
import com.archimatetool.model.IDiagramModelArchimateConnection;
import com.archimatetool.model.IDiagramModelArchimateObject;
import com.archimatetool.model.IDiagramModelConnection;
import com.archimatetool.model.IDiagramModelGroup;
import com.archimatetool.model.IDiagramModelNote;
import com.archimatetool.model.IDiagramModelObject;
import com.archimatetool.model.ILegendOptions;
import com.archimatetool.model.IProfile;
import com.archimatetool.model.IProperty;
import com.archimatetool.model.ITextAlignment;
import com.archimatetool.model.ITextPosition;



/**
 * Diagram Model Factory for creating objects from the Palette in the Archimate Diagram Editor
 * 
 * @author Phillip Beauvoir
 */
public class ArchimateDiagramModelFactory implements ICreationFactory {
    
    /**
     * Factory method for creating a new IDiagramModelArchimateObject for an IArchimateElement
     * @param element
     * @return a new IDiagramModelArchimateObject
     */
    public static IDiagramModelArchimateObject createDiagramModelArchimateObject(IArchimateElement element) {
        IDiagramModelArchimateObject dmo = IArchimateFactory.eINSTANCE.createDiagramModelArchimateObject();
        dmo.setArchimateElement(element);
        // Figure Type
        dmo.setType(ArchiPlugin.getInstance().getPreferenceStore().getInt(IPreferenceConstants.DEFAULT_FIGURE_PREFIX + element.eClass().getName()));
        
        // Add new bounds with a default user size
        IGraphicalObjectUIProvider provider = (IGraphicalObjectUIProvider)ObjectUIFactory.INSTANCE.getProvider(dmo);
        Dimension size = provider.getDefaultSize();
        dmo.setBounds(0, 0, size.width, size.height);
        
        dmo.setTextPosition(provider.getDefaultTextPosition());
        dmo.setTextAlignment(provider.getDefaultTextAlignment());

        // Set user default colors as set in prefs
        ColorFactory.setDefaultColors(dmo);
        
        // Gradient
        dmo.setGradient(ArchiPlugin.getInstance().getPreferenceStore().getInt(IPreferenceConstants.DEFAULT_GRADIENT));
        
        return dmo;
    }

    /**
     * Factory method for creating a new IDiagramModelArchimateConnection for an IRelationship
     * @param element
     * @return a new IDiagramModelArchimateConnection
     */
    public static IDiagramModelArchimateConnection createDiagramModelArchimateConnection(IArchimateRelationship relation) {
        IDiagramModelArchimateConnection connection = IArchimateFactory.eINSTANCE.createDiagramModelArchimateConnection();
        connection.setArchimateRelationship(relation);
        
        IGraphicalObjectUIProvider provider = (IGraphicalObjectUIProvider)ObjectUIFactory.INSTANCE.getProvider(connection);
        connection.setTextAlignment(provider.getDefaultTextAlignment());
        
        // Set user default colors as set in prefs
        ColorFactory.setDefaultColors(connection);
        
        addOntologyConnectionProperties(relation);
        
        return connection;
    }
    
    public static void addOntologyConnectionProperties(IArchimateRelationship relation) {
        // Source and target may not be set yet at factory creation time
        if(relation.getSource() == null || relation.getTarget() == null) {
            return;
        }

        boolean sourceIsDesignDecision = relation.getSource().eClass().getName().equals("DesignDecision"); //$NON-NLS-1$
        boolean targetIsDesignDecision = relation.getTarget().eClass().getName().equals("DesignDecision"); //$NON-NLS-1$

        // Design Decision -> Design Decision: add Impact property
        if(sourceIsDesignDecision && targetIsDesignDecision) {
            relation.getProperties().add(IArchimateFactory.eINSTANCE.createProperty("Impact", "")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // Design Decision -> resource (anything else): add State property
        else if(sourceIsDesignDecision) {
            relation.getProperties().add(IArchimateFactory.eINSTANCE.createProperty("State", "")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    private EClass template;
    private Object property;
    
    /**
     * Constructor for creating a new Ecore type model
     * @param template the EClass template
     */
    public ArchimateDiagramModelFactory(EClass template) {
        this(template, null);
    }
    
    /**
     * Concstructor for creating a new Ecore type model
     * @param template the EClass template
     * @param property Additional information
     */
    public ArchimateDiagramModelFactory(EClass template, Object property) {
        this.template = template;
        this.property = property;
    }
    
    @Override
    public boolean isUsedFor(IEditorPart editor) {
        return editor instanceof IArchimateDiagramEditor;
    }
    
    @Override
    public Object getNewObject() {
        if(template == null) {
            return null;
        }
        
        // Try the standard core factory first, fall back to EMF registry for external packages
        EObject object;
        try {
            object = IArchimateFactory.eINSTANCE.create(template);
        }
        catch(IllegalArgumentException ex) {
            // Element is from an external EMF package (e.g. a plugin-contributed element)
            // Use the package's own factory instead
            object = template.getEPackage().getEFactoryInstance().create(template);
        }
        
        // Are we creating a profile?
        IProfile profile = property instanceof IProfile p && p.getArchimateModel() != null ? p : null;
        
        // Add Profile to Concept if set
        if(object instanceof IArchimateConcept concept && profile != null) {
            concept.getProfiles().add(profile);
        }
        
        // Connection created from Relationship Template
        if(object instanceof IArchimateRelationship relationship) {
            return createDiagramModelArchimateConnection(relationship);
        }
        
        // Archimate Diagram Object created from Archimate Element Template
        else if(object instanceof IArchimateElement element) {
            element.setName(profile != null ? profile.getName() : ArchiLabelProvider.INSTANCE.getDefaultName(template));
            
            // If this is a Design Decision, add the default Decision Type property
            if(element.eClass().getName().equals("DesignDecision")) { //$NON-NLS-1$
                IProperty property = IArchimateFactory.eINSTANCE.createProperty("Decision Type", ""); //$NON-NLS-1$ //$NON-NLS-2$
                element.getProperties().add(property);
            }
            
            return createDiagramModelArchimateObject(element);
        }
        
        // Group
        else if(object instanceof IDiagramModelGroup group) {
            // Explicitly set the name
            group.setName(ArchiLabelProvider.INSTANCE.getDefaultName(template));
            
            // Colours
            ColorFactory.setDefaultColors(group);
            
            // Gradient
            group.setGradient(ArchiPlugin.getInstance().getPreferenceStore().getInt(IPreferenceConstants.DEFAULT_GRADIENT));
        }
        
        // Note
        else if(object instanceof IDiagramModelNote note) {
            // Legend. Set this first before setting default colors
            if(IDiagramModelNote.LEGEND_MODEL_NAME.equals(property)) {
                // Name
                note.setName(Messages.ArchimateDiagramModelFactory_0);

                // User defaults
                IPreferenceStore store = ArchiPlugin.getInstance().getPreferenceStore();
                
                note.setLegendOptions(ILegendOptions.create()
                                                    .rowsPerColumn(store.getInt(IPreferenceConstants.LEGEND_ROWS_PER_COLUMN_DEFAULT))
                                                    .colorScheme(store.getInt(IPreferenceConstants.LEGEND_COLORS_DEFAULT))
                                                    .sortMethod(store.getInt(IPreferenceConstants.LEGEND_SORT_DEFAULT)));
            }

            // Colours
            ColorFactory.setDefaultColors(note);
            
            // Gradient
            note.setGradient(ArchiPlugin.getInstance().getPreferenceStore().getInt(IPreferenceConstants.DEFAULT_GRADIENT));
        }
        
        // Connection
        else if(object instanceof IDiagramModelConnection dmc) {
            ColorFactory.setDefaultColors(dmc);
        }
        
        IGraphicalObjectUIProvider provider = (IGraphicalObjectUIProvider)ObjectUIFactory.INSTANCE.getProvider(object);

        if(object instanceof ITextAlignment ta) {
            ta.setTextAlignment(provider.getDefaultTextAlignment());
        }
                
        if(object instanceof ITextPosition tp) {
            tp.setTextPosition(provider.getDefaultTextPosition());
        }
        
        // Add new bounds with a default user size
        if(object instanceof IDiagramModelObject dmo) {
            Dimension size = provider.getDefaultSize();
            dmo.setBounds(0, 0, size.width, size.height);
        }

        return object;
    }

    @Override
    public Object getObjectType() {
        return template;
    }
}
