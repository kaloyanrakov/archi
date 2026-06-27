package com.archimatetool.editor.propertysections;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;

import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IProperties;
import com.archimatetool.model.IProperty;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IterationPropertyDecorator implements IPropertyDecorator {

    public static final String PROPERTY_PREVIOUS_ITERATION = "Previous Iteration"; //$NON-NLS-1$
    public static final String PROPERTY_NEXT_ITERATION     = "Next Iteration"; //$NON-NLS-1$
    public static final String PROPERTY_NEXT_VERSION       = "Next Version"; //$NON-NLS-1$
    public static final String PROPERTY_PREVIOUS_VERSION   = "Previous Version"; //$NON-NLS-1$

    public static final Set<String> VIEW_LINK_KEYS = Set.of(
        PROPERTY_NEXT_ITERATION,
        PROPERTY_PREVIOUS_ITERATION,
        PROPERTY_NEXT_VERSION,
        PROPERTY_PREVIOUS_VERSION
    );

    // Encodes reciprocal relationships as data — no if/else chains needed
    private static final Map<String, String> RECIPROCALS = Map.of(
        PROPERTY_PREVIOUS_ITERATION, PROPERTY_NEXT_ITERATION,
        PROPERTY_NEXT_ITERATION,     PROPERTY_PREVIOUS_ITERATION,
        PROPERTY_PREVIOUS_VERSION,   PROPERTY_NEXT_VERSION,
        PROPERTY_NEXT_VERSION,       PROPERTY_PREVIOUS_VERSION
    );

    private final String propertyKey;

    public IterationPropertyDecorator(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    @Override
    public String getPropertyKey() {
        return propertyKey;
    }

    @Override
    public boolean appliesTo(IProperties target) {
        return target instanceof IDiagramModel;
    }

    @Override
    public String[] getRestrictedValues() {
        return new String[] { "" }; //$NON-NLS-1$
    }

    @Override
    public String[] getRestrictedValues(IProperties element) {
        if(!(element instanceof IDiagramModel currentDiagram)
                || currentDiagram.getArchimateModel() == null) {
            return new String[] { "" }; //$NON-NLS-1$
        }

        List<String> diagramNames = new java.util.ArrayList<>();
        diagramNames.add(""); // Empty option for "none" //$NON-NLS-1$

        for(IDiagramModel diagram : currentDiagram.getArchimateModel().getDiagramModels()) {
            if(!diagram.equals(currentDiagram) && diagram.getName() != null) {
                diagramNames.add(diagram.getName());
            }
        }

        return diagramNames.toArray(new String[0]);
    }

    @Override
    public void contributeCommands(IArchimateElement element, String newValue, CompoundCommand cmd) {
        // Iteration properties only apply to views — nothing to do for elements
    }

    @Override
    public void contributeCommands(IProperties element, String newValue, CompoundCommand cmd) {
        if(element instanceof IDiagramModel diagram) {
            validateIterationReferences(diagram, newValue, cmd);
        }
        else if(element instanceof IArchimateElement ae) {
            contributeCommands(ae, newValue, cmd);
        }
    }

    private void validateIterationReferences(IDiagramModel diagram, String newValue, CompoundCommand cmd) {
        String reciprocalKey = RECIPROCALS.get(propertyKey);
        if(reciprocalKey == null) return;

        if(newValue == null || newValue.isEmpty()) {
            IDiagramModel linked = getReferencedDiagram(diagram, propertyKey);
            if(linked != null) {
                cmd.add(new SetPropertyCommand(linked, reciprocalKey, "")); //$NON-NLS-1$
            }
            return;
        }

        IDiagramModel referenced = findDiagramByName(diagram, newValue);
        if(referenced == null) return;

        validateNoCircularReference(diagram, referenced, reciprocalKey);
        cmd.add(new SetPropertyCommand(referenced, reciprocalKey, diagram.getName()));
    }

    private void validateNoCircularReference(IDiagramModel current, IDiagramModel referenced, String reciprocalKey) {
        referenced.getProperties().stream()
            .filter(p -> reciprocalKey.equals(p.getKey()))
            .findFirst()
            .ifPresent(p -> {
                if(current.getName().equals(p.getValue())) {
                    throw new IllegalArgumentException(
                        "Circular reference detected: " + current.getName() + //$NON-NLS-1$
                        " <-> " + referenced.getName()); //$NON-NLS-1$
                }
            });
    }

    private IDiagramModel findDiagramByName(IDiagramModel current, String name) {
        if(current.getArchimateModel() == null) return null;

        return current.getArchimateModel().getDiagramModels().stream()
            .filter(d -> name.equals(d.getName()))
            .findFirst()
            .orElse(null);
    }

    private IDiagramModel getReferencedDiagram(IDiagramModel diagram, String propertyKey) {
        return diagram.getProperties().stream()
            .filter(p -> propertyKey.equals(p.getKey()))
            .filter(p -> p.getValue() != null && !p.getValue().isEmpty())
            .findFirst()
            .map(p -> findDiagramByName(diagram, p.getValue()))
            .orElse(null);
    }

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
            diagram.getProperties().stream()
                .filter(p -> propertyKey.equals(p.getKey()))
                .findFirst()
                .ifPresent(p -> p.setValue(oldValue));
        }

        @Override
        public void dispose() {
            diagram = null;
            propertyKey = null;
        }
    }

    public static IDiagramModel resolveIterationTarget(IDiagramModel sourceDiagram, String targetName) {
        if(sourceDiagram == null || sourceDiagram.getArchimateModel() == null
                || targetName == null || targetName.isBlank()) {
            return null;
        }
        return sourceDiagram.getArchimateModel().getDiagramModels().stream()
            .filter(d -> !d.equals(sourceDiagram) && targetName.equals(d.getName()))
            .findFirst()
            .orElse(null);
    }

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
            return source.getName() + " --[" + type + "]--> " + target.getName(); //$NON-NLS-1$ //$NON-NLS-2$
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