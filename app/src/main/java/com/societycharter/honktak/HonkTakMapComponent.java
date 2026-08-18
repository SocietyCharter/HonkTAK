package com.societycharter.honktak;

import android.content.Context;
import android.content.Intent;
import com.atakmap.android.dropdown.DropDownMapComponent;
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;
import com.atakmap.android.maps.MapView;
import com.societycharter.honktak.plugin.R;

public final class HonkTakMapComponent extends DropDownMapComponent {
    private HonkTakDropDownReceiver receiver;
    @Override public void onCreate(Context context, Intent intent, MapView view) {
        context.setTheme(R.style.ATAKPluginTheme);
        super.onCreate(context, intent, view);
        receiver = new HonkTakDropDownReceiver(view, context);
        DocumentedIntentFilter filter = new DocumentedIntentFilter();
        filter.addAction(HonkTakDropDownReceiver.SHOW_PLUGIN);
        registerDropDownReceiver(receiver, filter);
    }
    @Override protected void onDestroyImpl(Context context, MapView view) { super.onDestroyImpl(context, view); }
}
