package com.archimatetool.designdecision.factory;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.archimatetool.editor.diagram.editparts.ArchimateElementEditPart;
import com.archimatetool.editor.ui.IIconDelegate;
import com.archimatetool.editor.ui.factory.elements.AbstractArchimateElementUIProvider;
import com.archimatetool.designdecision.IDesignDecisionImages;
import com.archimatetool.designdecision.figures.DesignDecisionFigure;
import com.archimatetool.designdecision.model.IDesignDecisionPackage;

public class DesignDecisionUIProvider extends AbstractArchimateElementUIProvider {

    @Override
    public EClass providerFor() {
        return IDesignDecisionPackage.eINSTANCE.getDesignDecision();
    }

    @Override
    public EditPart createEditPart() {
        return new ArchimateElementEditPart(DesignDecisionFigure.class);
    }

    @Override
    public String getDefaultName() {
        return "Design Decision";
    }

    @Override
    public Image getImage() {
        return IDesignDecisionImages.ImageFactory.getImage(IDesignDecisionImages.ICON_DESIGN_DECISION);
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return IDesignDecisionImages.ImageFactory.getImageDescriptor(IDesignDecisionImages.ICON_DESIGN_DECISION);
    }

    @Override
    public Color getDefaultColor() {
        return defaultMotivationColor;
    }

    @Override
    public IIconDelegate getIconDelegate() {
        return DesignDecisionFigure.getIconDelegate();
    }
}