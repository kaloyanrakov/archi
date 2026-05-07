/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.zest;

import com.archimatetool.model.IDiagramModel;

/**
 * Virtual relationship representing diagram iteration links
 * Works just like IArchimateRelationship but for diagrams
 */
public class IterationRelationship {
    private IDiagramModel source;
    private IDiagramModel target;
    
    public IterationRelationship(IDiagramModel source, IDiagramModel target) {
        this.source = source;
        this.target = target;
    }
    
    public IDiagramModel getSource() {
        return source;
    }
    
    public IDiagramModel getTarget() {
        return target;
    }
    
    @Override
    public String toString() {
        return source.getName() + " → " + target.getName(); //$NON-NLS-1$
    }
    
    @Override
    public int hashCode() {
        return (source.getId() + target.getId()).hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null) return false;
        if(getClass() != obj.getClass()) return false;
        IterationRelationship other = (IterationRelationship) obj;
        return source.equals(other.source) && target.equals(other.target);
    }
}