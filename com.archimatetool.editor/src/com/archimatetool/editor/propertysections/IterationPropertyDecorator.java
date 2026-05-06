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
        return new String[]{""};
    }

    @Override
    public void contributeCommands(IArchimateElement element, String newValue, CompoundCommand cmd) {
        // Only handle IDiagramModel (views), not regular elements
        if(element instanceof IDiagramModel diagram) {
            validateIterationReferences(diagram, newValue, cmd);
        }
    }

    private void validateIterationReferences(IDiagramModel diagram, String newValue, CompoundCommand cmd) {
        // TODO: add validation/linking logic here
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