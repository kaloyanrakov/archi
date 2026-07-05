package com.archimatetool.designdecision.propertysections;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.archimatetool.editor.model.commands.EObjectFeatureCommand;
import com.archimatetool.editor.propertysections.AbstractECorePropertySection;
import com.archimatetool.editor.propertysections.ITabbedLayoutConstants;
import com.archimatetool.editor.propertysections.IObjectFilter;
import com.archimatetool.editor.propertysections.ObjectFilter;
import com.archimatetool.designdecision.model.IDesignDecision;
import com.archimatetool.designdecision.model.IDesignDecisionPackage;
import com.archimatetool.designdecision.model.DesignDecisionType;
import com.archimatetool.model.IDiagramModelArchimateObject;

public class DesignDecisionTypeSection extends AbstractECorePropertySection {

    public static class Filter extends ObjectFilter {
        @Override
        public boolean isRequiredType(Object object) {
            return object instanceof IDiagramModelArchimateObject dmo
                    && dmo.getArchimateElement() instanceof IDesignDecision;
        }

        @Override
        public Class<?> getAdaptableType() {
            return IDiagramModelArchimateObject.class;
        }
    }

    private static final Filter FILTER = new Filter();

    private Combo fComboType;

    private static final String[] COMBO_ITEMS = {
        "Structural",
        "Behavioral",
        "Property"
    };

    @Override
    protected void createControls(Composite parent) {
        createLabel(parent, "Decision Type:", ITabbedLayoutConstants.STANDARD_LABEL_WIDTH, SWT.CENTER);

        fComboType = new Combo(parent, SWT.READ_ONLY);
        getWidgetFactory().adapt(fComboType, true, true);
        fComboType.setItems(COMBO_ITEMS);
        fComboType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fComboType.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompoundCommand result = new CompoundCommand();

                for(EObject eObject : getEObjects()) {
                    if(isAlive(eObject) && !isLocked(eObject)) {
                        IDesignDecision dd = getDesignDecision(eObject);
                        if(dd != null) {
                            DesignDecisionType newType = DesignDecisionType.get(fComboType.getSelectionIndex());
                            Command cmd = new EObjectFeatureCommand("Set Decision Type", dd,
                                    IDesignDecisionPackage.Literals.DESIGN_DECISION__DECISION_TYPE, newType);
                            if(cmd.canExecute()) {
                                result.add(cmd);
                            }
                        }
                    }
                }

                executeCommand(result.unwrap());
            }
        });
    }

    @Override
    protected void update() {
        if(isExecutingCommand()) {
            return;
        }

        IDesignDecision dd = getDesignDecision(getFirstSelectedObject());
        if(dd != null) {
            fComboType.select(dd.getDecisionType().getValue());
        }
    }

    @Override
    protected void notifyChanged(Notification msg) {
        if(msg.getFeature() == IDesignDecisionPackage.Literals.DESIGN_DECISION__DECISION_TYPE) {
            update();
        }
    }

    @Override
    protected IObjectFilter getFilter() {
        return FILTER;
    }

    private IDesignDecision getDesignDecision(Object object) {
        if(object instanceof IDiagramModelArchimateObject dmo
                && dmo.getArchimateElement() instanceof IDesignDecision dd) {
            return dd;
        }
        return null;
    }
}