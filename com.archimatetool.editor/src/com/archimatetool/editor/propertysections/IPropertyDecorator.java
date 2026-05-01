package com.archimatetool.editor.propertysections;

import org.eclipse.gef.commands.CompoundCommand;

import com.archimatetool.model.IArchimateElement;

public interface IPropertyDecorator {

    String getPropertyKey();

    String[] getRestrictedValues();

    void contributeCommands(IArchimateElement element, String newValue, CompoundCommand cmd);
}
