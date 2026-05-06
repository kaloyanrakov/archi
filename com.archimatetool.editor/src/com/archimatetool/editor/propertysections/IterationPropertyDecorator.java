package com.archimatetool.editor.propertysections;

import org.eclipse.gef.commands.CompoundCommand;

import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IDiagramModel;

public class IterationPropertyDecorator implements IPropertyDecorator {

    public static final String PROPERTY_PREVIOUS_ITERATION = "Previous Iteration"; //$NON-NLS-1$
    public static final String PROPERTY_NEXT_ITERATION = "Next Iteration"; //$NON-NLS-1$

    private final String propertyKey;

    public IterationPropertyDecorator(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    @Override
    public String getPropertyKey() {
        return propertyKey;
    }

    @Override
    public String[] getRestrictedValues() {
        // Default empty array - should use context-aware version
        return new String[]{""};
    }

    /**
     * Get available diagrams dynamically based on the current diagram
     */
    @Override
    public String[] getRestrictedValues(com.archimatetool.model.IProperties element) {
        if(!(element instanceof IDiagramModel currentDiagram)) {
            return new String[]{""};
        }
        
        if(currentDiagram.getArchimateModel() == null) {
            return new String[]{""};
        }

        // Get all diagram names except current one, prefixed with empty option
        java.util.List<String> diagramNames = new java.util.ArrayList<>();
        diagramNames.add(""); // Empty option for "none"
        
        for(IDiagramModel diagram : currentDiagram.getArchimateModel().getDiagramModels()) {
            if(!diagram.equals(currentDiagram) && diagram.getName() != null) {
                diagramNames.add(diagram.getName());
            }
        }

        return diagramNames.toArray(new String[0]);
    }

    @Override
    public void contributeCommands(IArchimateElement element, String newValue, CompoundCommand cmd) {
        // Only handle IDiagramModel (views), not regular elements
        if(element instanceof IDiagramModel diagram) {
            validateIterationReferences(diagram, newValue, cmd);
        }
    }

    private void validateIterationReferences(IDiagramModel diagram, String newValue, CompoundCommand cmd) {
        if(newValue == null || newValue.isEmpty()) {
            return;
        }

        IDiagramModel referenced = findDiagramByName(diagram, newValue);
        if(referenced == null) {
            return;
        }

        // Prevent circular references
        if(PROPERTY_PREVIOUS_ITERATION.equals(propertyKey)) {
            validateNoPreviousCircularReference(diagram, referenced);
        }
        else if(PROPERTY_NEXT_ITERATION.equals(propertyKey)) {
            validateNoNextCircularReference(diagram, referenced);
        }
    }

    private void validateNoPreviousCircularReference(IDiagramModel current, IDiagramModel previous) {
        // Check if previous diagram points back to this one as next
        com.archimatetool.model.IProperty nextIterProp = previous.getProperties().stream()
            .filter(p -> PROPERTY_NEXT_ITERATION.equals(p.getKey()))
            .findFirst()
            .orElse(null);

        if(nextIterProp != null && current.getName().equals(nextIterProp.getValue())) {
            throw new IllegalArgumentException(
                "Circular reference detected: " + current.getName() +  //$NON-NLS-1$
                " <-> " + previous.getName()); //$NON-NLS-1$
        }
    }

    private void validateNoNextCircularReference(IDiagramModel current, IDiagramModel next) {
        // Check if next diagram points back to this one as previous
        com.archimatetool.model.IProperty prevIterProp = next.getProperties().stream()
            .filter(p -> PROPERTY_PREVIOUS_ITERATION.equals(p.getKey()))
            .findFirst()
            .orElse(null);

        if(prevIterProp != null && current.getName().equals(prevIterProp.getValue())) {
            throw new IllegalArgumentException(
                "Circular reference detected: " + current.getName() +  //$NON-NLS-1$
                " <-> " + next.getName()); //$NON-NLS-1$
        }
    }

    private IDiagramModel findDiagramByName(IDiagramModel current, String name) {
        if(current.getArchimateModel() == null) {
            return null;
        }

        return current.getArchimateModel().getDiagramModels().stream()
            .filter(d -> name.equals(d.getName()))
            .findFirst()
            .orElse(null);
    }

    public static String[] getAvailableDiagrams(IDiagramModel currentDiagram) {
        if(currentDiagram == null || currentDiagram.getArchimateModel() == null) {
            return new String[]{""};
        }
        
        return currentDiagram.getArchimateModel().getDiagramModels().stream()
            .filter(d -> !d.equals(currentDiagram))
            .map(d -> d.getName())
            .toArray(String[]::new);
    }
}