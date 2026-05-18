/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.zest;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.IGraphContentProvider;

import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimateRelationship;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.util.ArchimateModelUtils;
import com.archimatetool.model.viewpoints.IViewpoint;
import com.archimatetool.model.viewpoints.ViewpointManager;
import com.archimatetool.editor.propertysections.IterationPropertyDecorator;
import com.archimatetool.editor.propertysections.IterationPropertyDecorator.IterationConnection;

/**
 * Graph Viewer Content Provider
 * 
 * @author Phillip Beauvoir
 * @author Jean-Baptiste Sarrodie
 */
public class ZestViewerContentProvider implements IGraphContentProvider {
	final static int DIR_BOTH = 1;
	final static int DIR_IN = 2;
	final static int DIR_OUT = 3;
    
    private int fDepth = 0;
    private IViewpoint fViewpoint = ViewpointManager.NONE_VIEWPOINT;
    private Set<EClass> fElementClasses = new LinkedHashSet<>();
    private Set<EClass> fRelationshipClasses = new LinkedHashSet<>();
    private int fDirection = DIR_BOTH;
    private String fLevelFilter = null;
    
    public void setViewpointFilter(IViewpoint vp) {
        assert(vp != null);
        fViewpoint = vp;
    }
    
    public IViewpoint getViewpointFilter() {
        return fViewpoint;
    }
    
    public void addElementFilter(EClass elementClass) {
        if(elementClass == null) {
            fElementClasses.clear();
        }
        else {
            fElementClasses.add(elementClass);
        }
    }
    
    public void removeElementFilter(EClass elementClass) {
        if(elementClass != null) {
            fElementClasses.remove(elementClass);
        }
    }
    
    public Set<EClass> getElementFilters() {
        return fElementClasses;
    }
    
    public void addRelationshipFilter(EClass relationshipClass) {
        if(relationshipClass == null) {
            fRelationshipClasses.clear();
        }
        else {
            fRelationshipClasses.add(relationshipClass);
        }
    }
    
    public void removeRelationshipFilter(EClass relationshipClass) {
        if(relationshipClass != null) {
            fRelationshipClasses.remove(relationshipClass);
        }
    }

    public Set<EClass> getRelationshipFilters() {
        return fRelationshipClasses;
    }
    
    public void setDirection(int direction) {
    	if(direction == DIR_BOTH || direction == DIR_IN || direction == DIR_OUT) {
    	    fDirection = direction;
    	}
    }
    
    public int getDirection() {
        return fDirection;
    }
    
    public void setLevelFilter(String level) {
        fLevelFilter = level;
    }

    public String getLevelFilter() {
        return fLevelFilter;
    }

    public void setDepth(int depth) {
        fDepth = depth;
    }
    
    public int getDepth() {
        return fDepth;
    }
    
    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
    
    @Override
    public Object[] getElements(Object inputElement) {
        // Handle IDiagramModel input (show iteration chain)
        if(inputElement instanceof IDiagramModel) {
            Set<Object> mainList = new HashSet<>();
            getIterationRelations(mainList, new HashSet<IDiagramModel>(), (IDiagramModel)inputElement, 0);
            return mainList.toArray();
        }
        
        if(inputElement instanceof IArchimateConcept) {
            IArchimateConcept archimateConcept = (IArchimateConcept)inputElement;
            
            
            // Check if it was deleted
            if(archimateConcept.eContainer() == null) {
                return new Object[0];
            }
            
            // Relationship
            if(archimateConcept instanceof IArchimateRelationship) {
                return new Object[]  { inputElement };
            }

            // Element - Get its relationships
            if(archimateConcept instanceof IArchimateElement) {
                Set<IArchimateRelationship> mainList = new HashSet<>();
                getRelations(mainList, new HashSet<IArchimateConcept>(), archimateConcept, 0);
                return mainList.toArray();
            }
        }
        
        return new Object[0];
    }
    
    /**
     * Get all iteration relations from previous/next iterations and add to list, no more than DEPTH
     */
    private void getIterationRelations(Set<Object> mainList, Set<IDiagramModel> checkList, IDiagramModel diagram, int count) {
        if(checkList.contains(diagram)) {
            return;
        }
        
        checkList.add(diagram);
        
        if(count > fDepth) {
            return;
        }
        
        count++;
        
        // Get previous iteration
        com.archimatetool.model.IProperty prevIterProp = diagram.getProperties().stream()
            .filter(p -> "Previous Iteration".equals(p.getKey())) //$NON-NLS-1$
            .findFirst()
            .orElse(null);
        
        if(prevIterProp != null && prevIterProp.getValue() != null && !prevIterProp.getValue().isEmpty()) {
            IDiagramModel previousDiagram = findDiagramByName(diagram, prevIterProp.getValue());
            if(previousDiagram != null && !checkList.contains(previousDiagram)) {
            	mainList.add(new IterationConnection(diagram, previousDiagram, IterationPropertyDecorator.PROPERTY_PREVIOUS_ITERATION));                getIterationRelations(mainList, checkList, previousDiagram, count);
            }
        }
        
        // Get next iteration
        com.archimatetool.model.IProperty nextIterProp = diagram.getProperties().stream()
            .filter(p -> "Next Iteration".equals(p.getKey())) //$NON-NLS-1$
            .findFirst()
            .orElse(null);
        
        if(nextIterProp != null && nextIterProp.getValue() != null && !nextIterProp.getValue().isEmpty()) {
            IDiagramModel nextDiagram = findDiagramByName(diagram, nextIterProp.getValue());
            if(nextDiagram != null && !checkList.contains(nextDiagram)) {
            	mainList.add(new IterationConnection(diagram, nextDiagram, IterationPropertyDecorator.PROPERTY_NEXT_ITERATION));
                getIterationRelations(mainList, checkList, nextDiagram, count);
            }
        }
     //Get previous version
        com.archimatetool.model.IProperty prevVersionProp = diagram.getProperties().stream()
            .filter(p -> IterationPropertyDecorator.PROPERTY_PREVIOUS_VERSION.equals(p.getKey()))
            .findFirst()
            .orElse(null);
        
        if(prevVersionProp != null && prevVersionProp.getValue() != null && !prevVersionProp.getValue().isEmpty()) {
            IDiagramModel previousVersion = findDiagramByName(diagram, prevVersionProp.getValue());
            if(previousVersion != null && !checkList.contains(previousVersion)) {
            	mainList.add(new IterationConnection(diagram, previousVersion, IterationPropertyDecorator.PROPERTY_PREVIOUS_VERSION));
                getIterationRelations(mainList, checkList, previousVersion, count);
            }
        }
        
        //Get next version
        com.archimatetool.model.IProperty nextVersionProp = diagram.getProperties().stream()
            .filter(p -> IterationPropertyDecorator.PROPERTY_NEXT_VERSION.equals(p.getKey()))
            .findFirst()
            .orElse(null);
        
        if(nextVersionProp != null && nextVersionProp.getValue() != null && !nextVersionProp.getValue().isEmpty()) {
            IDiagramModel nextVersion = findDiagramByName(diagram, nextVersionProp.getValue());
            if(nextVersion != null && !checkList.contains(nextVersion)) {
                mainList.add(new IterationConnection(diagram, nextVersion, IterationPropertyDecorator.PROPERTY_NEXT_VERSION));
                getIterationRelations(mainList, checkList, nextVersion, count);
            }
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
    
    /**
     * Get all relations from source and target of concept and add to list, no more than DEPTH
     */
    private void getRelations(Set<IArchimateRelationship> mainList, Set<IArchimateConcept> checkList, IArchimateConcept concept, int count) {
        if(checkList.contains(concept)) {
            return;
        }
        
        checkList.add(concept);
        
        if(count > fDepth) {
            return;
        }
        
        count++;
        
        for(IArchimateRelationship relationship : ArchimateModelUtils.getAllRelationshipsForConcept(concept)) {
            IArchimateConcept other = relationship.getSource().equals(concept) ? relationship.getTarget() : relationship.getSource();
            int direction = relationship.getSource().equals(concept) ? DIR_OUT : DIR_IN;

            if(!mainList.contains(relationship) && fViewpoint.isAllowedConcept(other.eClass()) && isVisible(relationship)) {
                if(direction == fDirection || fDirection == DIR_BOTH) {
                    // If the other concept is an element and is selected to be shown
                    if(other instanceof IArchimateElement && isVisible((IArchimateElement)other)) {
                        mainList.add(relationship);
                    }
                }
            }

            if(fViewpoint.isAllowedConcept(other.eClass()) && isVisible(relationship)) {
                if(direction == fDirection || fDirection == DIR_BOTH) {
                    getRelations(mainList, checkList, other, count);
                }
            }
        }
    }
    
    @Override
    public Object getSource(Object rel) {
        if(rel instanceof IArchimateRelationship) {
            return ((IArchimateRelationship)rel).getSource();
        }
        if(rel instanceof IterationConnection ic) {
            return ic.getSource();
        }
        return null;
    }

    @Override
    public Object getDestination(Object rel) {
        if(rel instanceof IArchimateRelationship) {
            return ((IArchimateRelationship)rel).getTarget();
        }
        if(rel instanceof IterationConnection ic) {
            return ic.getTarget();
        }
        return null;
    }
    
    private boolean isVisible(IArchimateElement element) {
        // Element type filter
        if(!fElementClasses.isEmpty() && !fElementClasses.contains(element.eClass())) {
            return false;
        }
        
        // Level filter
        if(fLevelFilter != null) {
            boolean hasMatchingLevel = element.getProperties().stream()
                .anyMatch(p -> "Model Level".equals(p.getKey()) && fLevelFilter.equals(p.getValue())); //$NON-NLS-1$
            if(!hasMatchingLevel) {
                return false;
            }
        }
        
        return true;
    }

	private boolean isVisible(IArchimateRelationship relation) {
	    if(fRelationshipClasses.isEmpty()) {
            return true;
        }
        
        return fRelationshipClasses.contains(relation.eClass());
	}
}