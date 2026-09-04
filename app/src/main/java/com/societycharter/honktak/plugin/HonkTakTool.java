package com.societycharter.honktak.plugin;

import android.content.Context;
import com.atak.plugins.impl.AbstractPluginTool;
import com.societycharter.honktak.HonkTakDropDownReceiver;
import gov.tak.api.util.Disposable;

public final class HonkTakTool extends AbstractPluginTool implements Disposable {
    public HonkTakTool(Context context) {
        super(context, context.getString(R.string.app_name), context.getString(R.string.app_desc),
            context.getResources().getDrawable(R.drawable.ic_goose), HonkTakDropDownReceiver.SHOW_PLUGIN);
    }
    @Override public void dispose() { }
}
