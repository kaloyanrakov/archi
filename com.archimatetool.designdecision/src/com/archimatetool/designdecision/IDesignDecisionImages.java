package com.archimatetool.designdecision;

import com.archimatetool.editor.ui.ImageFactory;

public interface IDesignDecisionImages {

    ImageFactory ImageFactory = new ImageFactory(DesignDecisionPlugin.getInstance());

    String IMGPATH = "img/";
    String ICON_DESIGN_DECISION = IMGPATH + "design-decision.png";
}