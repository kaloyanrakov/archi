package com.archimatetool.editor.propertysections;

import org.eclipse.gef.commands.CompoundCommand;

import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IProperties;

public interface IPropertyDecorator {

    String getPropertyKey();

    String[] getRestrictedValues();

    default String[] getRestrictedValues(IArchimateElement element) {
        return getRestrictedValues();
    }
    
    default String[] getRestrictedValues(IProperties element) {
        if(element instanceof IArchimateElement ae) {
            return getRestrictedValues(ae);
        }
        return getRestrictedValues();
    }

    void contributeCommands(IArchimateElement element, String newValue, CompoundCommand cmd);
    
    default void contributeCommands(IProperties element, String newValue, CompoundCommand cmd) {
        if(element instanceof IArchimateElement ae) {
            contributeCommands(ae, newValue, cmd);
        }
    }
}