package com.archimatetool.editor.propertysections;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;

import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IProperties;
import com.archimatetool.model.IProperty;
import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IterationPropertyDecorator implements IPropertyDecorator {

    public static final String PROPERTY_PREVIOUS_ITERATION = "Previous Iteration"; //$NON-NLS-1$
    public static final String PROPERTY_NEXT_ITERATION = "Next Iteration"; //$NON-NLS-1$
    
    public static final String PROPERTY_NEXT_VERSION = "Next Version";//$NON-NLS-1$
    public static final String PROPERTY_PREVIOUS_VERSION = "Previous Version";//$NON-NLS-1$

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

    /**
     * Get available diagrams dynamically - accepts IProperties for both elements and diagrams
     */
    @Override
    public String[] getRestrictedValues(IProperties element) {
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
        // This is for regular elements - do nothing for iterations
    }
    
    /**
     * Handle IDiagramModel for iteration properties
     */
    @Override
    public void contributeCommands(IProperties element, String newValue, CompoundCommand cmd) {
                
        // Only handle IDiagramModel (views), not regular elements
        if(element instanceof IDiagramModel diagram) {
            
            validateIterationReferences(diagram, newValue, cmd);
        }
        else if(element instanceof IArchimateElement ae) {
            // Delegate to the old method for regular elements
            contributeCommands(ae, newValue, cmd);
        }
    }

    private void validateIterationReferences(IDiagramModel diagram, String newValue, CompoundCommand cmd) {
        
        if(newValue == null || newValue.isEmpty()) {
            // Clear reciprocal for iterations
            if(PROPERTY_NEXT_ITERATION.equals(propertyKey)) {
                IDiagramModel nextDiagram = getReferencedDiagram(diagram, propertyKey);
                if(nextDiagram != null) {
                    cmd.add(new SetPropertyCommand(nextDiagram, PROPERTY_PREVIOUS_ITERATION, ""));
                }
            }
            else if(PROPERTY_PREVIOUS_ITERATION.equals(propertyKey)) {
                IDiagramModel prevDiagram = getReferencedDiagram(diagram, propertyKey);
                if(prevDiagram != null) {
                    cmd.add(new SetPropertyCommand(prevDiagram, PROPERTY_NEXT_ITERATION, ""));
                }
            }
            // Clear reciprocal for versions
            else if(PROPERTY_NEXT_VERSION.equals(propertyKey)) {
                IDiagramModel nextDiagram = getReferencedDiagram(diagram, propertyKey);
                if(nextDiagram != null) {
                    cmd.add(new SetPropertyCommand(nextDiagram, PROPERTY_PREVIOUS_VERSION, ""));
                }
            }
            else if(PROPERTY_PREVIOUS_VERSION.equals(propertyKey)) {
                IDiagramModel prevDiagram = getReferencedDiagram(diagram, propertyKey);
                if(prevDiagram != null) {
                    cmd.add(new SetPropertyCommand(prevDiagram, PROPERTY_NEXT_VERSION, ""));
                }
            }
            return;
        }

        IDiagramModel referenced = findDiagramByName(diagram, newValue);
        if(referenced == null) return;

        // Set reciprocal for iterations
        if(PROPERTY_PREVIOUS_ITERATION.equals(propertyKey)) {
            validateNoPreviousCircularReference(diagram, referenced);
            cmd.add(new SetPropertyCommand(referenced, PROPERTY_NEXT_ITERATION, diagram.getName()));
        }
        else if(PROPERTY_NEXT_ITERATION.equals(propertyKey)) {
            validateNoNextCircularReference(diagram, referenced);
            cmd.add(new SetPropertyCommand(referenced, PROPERTY_PREVIOUS_ITERATION, diagram.getName()));
        }
        // Set reciprocal for versions
        else if(PROPERTY_PREVIOUS_VERSION.equals(propertyKey)) {
            cmd.add(new SetPropertyCommand(referenced, PROPERTY_NEXT_VERSION, diagram.getName()));
        }
        else if(PROPERTY_NEXT_VERSION.equals(propertyKey)) {
            cmd.add(new SetPropertyCommand(referenced, PROPERTY_PREVIOUS_VERSION, diagram.getName()));
        }
    }

    private void validateNoPreviousCircularReference(IDiagramModel current, IDiagramModel previous) {
        IProperty nextIterProp = previous.getProperties().stream()
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
        IProperty prevIterProp = next.getProperties().stream()
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
    
    private IDiagramModel getReferencedDiagram(IDiagramModel diagram, String propertyKey) {
        IProperty prop = diagram.getProperties().stream()
            .filter(p -> propertyKey.equals(p.getKey()))
            .findFirst()
            .orElse(null);
        
        if(prop != null && prop.getValue() != null && !prop.getValue().isEmpty()) {
            return findDiagramByName(diagram, prop.getValue());
        }
        return null;
    }

    /**
     * Command to set a property value
     */
    private static class SetPropertyCommand extends Command {
        private IDiagramModel diagram;
        private String propertyKey;
        private String newValue;
        private String oldValue;
        
        SetPropertyCommand(IDiagramModel diagram, String propertyKey, String newValue) {
            this.diagram = diagram;
            this.propertyKey = propertyKey;
            this.newValue = newValue;
        }
        
        @Override
        public void execute() {
            IProperty prop = diagram.getProperties().stream()
                .filter(p -> propertyKey.equals(p.getKey()))
                .findFirst()
                .orElse(null);
            
            if(prop == null) {
                prop = IArchimateFactory.eINSTANCE.createProperty();
                prop.setKey(propertyKey);
                diagram.getProperties().add(prop);
            }
            
            oldValue = prop.getValue();
            prop.setValue(newValue);
        }
        
        @Override
        public void undo() {
            IProperty prop = diagram.getProperties().stream()
                .filter(p -> propertyKey.equals(p.getKey()))
                .findFirst()
                .orElse(null);
            
            if(prop != null) {
                prop.setValue(oldValue);
            }
        }
    }
    public static IDiagramModel resolveIterationTarget(IDiagramModel sourceDiagram, String targetName) {
        if(sourceDiagram == null || sourceDiagram.getArchimateModel() == null || targetName == null || targetName.isBlank()) {
            return null;
        }
        return sourceDiagram.getArchimateModel().getDiagramModels().stream()
            .filter(d -> !d.equals(sourceDiagram) && targetName.equals(d.getName()))
            .findFirst()
            .orElse(null);
    }

    public static final Set<String> VIEW_LINK_KEYS = Set.of(
            PROPERTY_NEXT_ITERATION,
            PROPERTY_PREVIOUS_ITERATION,
            PROPERTY_NEXT_VERSION,
            PROPERTY_PREVIOUS_VERSION
        );
    
    public static Map<String, IDiagramModel> getIterationLinks(IDiagramModel diagram) {
        Map<String, IDiagramModel> links = new LinkedHashMap<>();

        for(IProperty p : diagram.getProperties()) {
            if(VIEW_LINK_KEYS.contains(p.getKey())
                    && p.getValue() != null
                    && !p.getValue().isBlank()) {
                IDiagramModel target = resolveIterationTarget(diagram, p.getValue());
                if(target != null) {
                    links.put(p.getKey(), target);
                }
            }
        }

        return links;
    }
    
    public static class IterationConnection {
        private final IDiagramModel source;
        private final IDiagramModel target;
        private final String type;

        public IterationConnection(IDiagramModel source, IDiagramModel target, String type) {
            this.source = source;
            this.target = target;
            this.type = type;
        }

        public IDiagramModel getSource() { return source; }
        public IDiagramModel getTarget() { return target; }
        public String getType()          { return type; }

        @Override
        public String toString() {
            return source.getName() + " --[" + type + "]--> " + target.getName();
        }
    }
    public static Set<IDiagramModel> getAllReachableViews(IDiagramModel start) {
        Set<IDiagramModel> visited = new java.util.LinkedHashSet<>();
        collectReachable(start, visited);
        visited.remove(start);
        return visited;
    }

    private static void collectReachable(IDiagramModel diagram, Set<IDiagramModel> visited) {
        if(!visited.add(diagram)) return;
        for(IDiagramModel linked : getIterationLinks(diagram).values()) {
            if(linked != null) {
                collectReachable(linked, visited);
            }
        }
    }
}