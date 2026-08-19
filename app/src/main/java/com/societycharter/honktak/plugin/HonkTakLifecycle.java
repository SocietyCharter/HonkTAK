package com.societycharter.honktak.plugin;

import android.content.Context;
import com.atak.plugins.impl.AbstractPluginLifecycle;
import com.societycharter.honktak.HonkTakMapComponent;

public final class HonkTakLifecycle extends AbstractPluginLifecycle {
    public HonkTakLifecycle(Context context) { super(context, new HonkTakMapComponent()); }
}
