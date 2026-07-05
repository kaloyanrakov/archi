package com.archimatetool.designdecision;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.archimatetool.model.IArchimatePackage;
import com.archimatetool.model.util.ArchimateModelUtils;

public class DesignDecisionPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "com.archimatetool.designdecision"; //$NON-NLS-1$

    private static DesignDecisionPlugin instance;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        instance = this;

        ArchimateModelUtils.registerExternalRelationshipValidator(new ArchimateModelUtils.IExternalRelationshipValidator() {

            private boolean isDesignDecision(EClass eClass) {
                return "DesignDecision".equals(eClass.getName()); //$NON-NLS-1$
            }

            @Override
            public boolean isValidRelationshipStart(EClass sourceType, EClass relationshipType) {
                if(!isDesignDecision(sourceType)) {
                    return false;
                }
                return relationshipType == IArchimatePackage.eINSTANCE.getInfluenceRelationship()
                    || relationshipType == IArchimatePackage.eINSTANCE.getAssociationRelationship()
                    || relationshipType == IArchimatePackage.eINSTANCE.getRealizationRelationship()
                    || relationshipType == IArchimatePackage.eINSTANCE.getSpecializationRelationship();
            }

            @Override
            public boolean isValidRelationship(EClass sourceType, EClass targetType, EClass relationshipType) {
                if(!isDesignDecision(sourceType)) {
                    return false;
                }
                // DD -> DD: Influence, Association, Specialization
                if(isDesignDecision(targetType)) {
                    return relationshipType == IArchimatePackage.eINSTANCE.getInfluenceRelationship()
                        || relationshipType == IArchimatePackage.eINSTANCE.getAssociationRelationship()
                        || relationshipType == IArchimatePackage.eINSTANCE.getSpecializationRelationship();
                }
                // DD -> Capability: Realization, Influence, Association
                if(targetType == IArchimatePackage.eINSTANCE.getCapability()) {
                    return relationshipType == IArchimatePackage.eINSTANCE.getRealizationRelationship()
                        || relationshipType == IArchimatePackage.eINSTANCE.getInfluenceRelationship()
                        || relationshipType == IArchimatePackage.eINSTANCE.getAssociationRelationship();
                }
                // DD -> anything else: Influence, Association
                return relationshipType == IArchimatePackage.eINSTANCE.getInfluenceRelationship()
                    || relationshipType == IArchimatePackage.eINSTANCE.getAssociationRelationship();
            }
        });
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        instance = null;
        super.stop(context);
    }

    public static DesignDecisionPlugin getInstance() {
        return instance;
    }
}