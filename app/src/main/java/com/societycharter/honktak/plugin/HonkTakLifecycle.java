package com.societycharter.honktak.plugin;

import android.content.Context;
import com.atak.plugins.impl.AbstractPlugin;
import com.atak.plugins.impl.PluginContextProvider;
import com.societycharter.honktak.HonkTakMapComponent;
import gov.tak.api.plugin.IServiceController;

public final class HonkTakLifecycle extends AbstractPlugin {
    public HonkTakLifecycle(IServiceController serviceController) {
        super(serviceController,
            new HonkTakTool(pluginContext(serviceController)),
            new HonkTakMapComponent());
    }

    private static Context pluginContext(IServiceController serviceController) {
        return serviceController.getService(PluginContextProvider.class).getPluginContext();
    }
}
