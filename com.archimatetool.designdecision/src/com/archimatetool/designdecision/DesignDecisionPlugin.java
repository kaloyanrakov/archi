package com.archimatetool.designdecision;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class DesignDecisionPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "com.archimatetool.designdecision";
    
    private static DesignDecisionPlugin instance;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        instance = this;
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