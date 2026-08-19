package com.societycharter.honktak;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.dropdown.DropDown.OnStateListener;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.DefaultMapGroup;
import com.atakmap.android.maps.Icon;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;
import com.societycharter.honktak.plugin.R;

/** UI adapter that creates only ephemeral items in a private local map group. */
public final class HonkTakDropDownReceiver extends DropDownReceiver implements OnStateListener {
    public static final String SHOW_PLUGIN = "com.societycharter.honktak.SHOW_PLUGIN";
    private static final String PREF_AUDIO = "honktak.audio_enabled";
    private static final String PREF_EXPIRY_MINUTES = "honktak.expiry_minutes";
    private final Context pluginContext;
    private final View view;
    private final MapGroup group;
    private final Handler handler = new Handler();
    private final List<Record> sightings = new ArrayList<>();
    private final Random random = new Random();

    public HonkTakDropDownReceiver(MapView mapView, Context context) {
        super(mapView);
        pluginContext = context;
        view = PluginLayoutInflater.inflate(context, R.layout.main_layout, null);
        DefaultMapGroup localGroup = new DefaultMapGroup("HonkTAK Sightings");
        localGroup.setMetaBoolean("addToObjList", false);
        mapView.getRootGroup().addGroup(localGroup);
        group = localGroup;
        bindUi();
    }

    private void bindUi() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pluginContext);
        final CheckBox audio = view.findViewById(R.id.audio_enabled);
        audio.setChecked(prefs.getBoolean(PREF_AUDIO, HonkPolicy.DEFAULT_AUDIO_ENABLED));
        audio.setOnCheckedChangeListener((button, checked) -> prefs.edit().putBoolean(PREF_AUDIO, checked).apply());
        final EditText expiry = view.findViewById(R.id.expiry_minutes);
        expiry.setText(String.valueOf(prefs.getInt(PREF_EXPIRY_MINUTES, 30)));
        view.findViewById(R.id.report_honk).setOnClickListener(v -> reportHonk(expiry, prefs));
    }

    private void reportHonk(EditText expiryInput, SharedPreferences prefs) {
        int minutes = parseExpiry(expiryInput.getText().toString());
        prefs.edit().putInt(PREF_EXPIRY_MINUTES, minutes).apply();
        long created = System.currentTimeMillis();
        GeoPointMetaData pointMeta = getMapView().getPointWithElevation();
        GeoPoint point = pointMeta == null ? null : pointMeta.get();
        if (point == null) {
            TextView status = view.findViewById(R.id.status);
            status.setText("Unable to report: map position is not available.");
            return;
        }
        Marker marker = new Marker(point, "honktak-" + UUID.randomUUID());
        marker.setTitle(HonkPolicy.MARKER_LABEL);
        marker.setType("honktak-waterfowl");
        marker.setAlwaysShowText(true);
        marker.setTouchable(true);
        marker.setMetaBoolean("nevercot", true);
        marker.setMetaBoolean("archive", false);
        marker.setMetaBoolean("honktak.local_only", true);
        marker.setIcon(gooseIcon());
        group.addItem(marker);
        sightings.add(new Record(marker, new HonkPolicy.Sighting(point.getLatitude(), point.getLongitude(), created)));
        long expiryMs = minutes * 60L * 1000L;
        handler.postDelayed(() -> expire(marker), expiryMs);
        TextView status = view.findViewById(R.id.status);
        status.setText(HonkPolicy.SITREPS[random.nextInt(HonkPolicy.SITREPS.length)]);
        if (HonkPolicy.triggersFlockpocalypse(activeSightings(), created, expiryMs)) status.setText("FLOCKPOCALYPSE");
    }

    private List<HonkPolicy.Sighting> activeSightings() {
        List<HonkPolicy.Sighting> result = new ArrayList<>();
        for (Record record : sightings) result.add(record.sighting);
        return result;
    }

    private void expire(Marker marker) {
        group.removeItem(marker);
        for (int i = sightings.size() - 1; i >= 0; i--) if (sightings.get(i).marker == marker) sightings.remove(i);
    }

    private Icon gooseIcon() {
        Bitmap bitmap = BitmapFactory.decodeResource(pluginContext.getResources(), R.drawable.ic_goose);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        String encoded = "base64://" + Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP | Base64.URL_SAFE);
        return new Icon.Builder().setImageUri(0, encoded).build();
    }

    private static int parseExpiry(String input) {
        try { return Math.max(1, Math.min(1440, Integer.parseInt(input))); }
        catch (NumberFormatException ignored) { return 30; }
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (intent != null && SHOW_PLUGIN.equals(intent.getAction())) showDropDown(view, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH, HALF_HEIGHT, false, this);
    }
    @Override protected void disposeImpl() {
        handler.removeCallbacksAndMessages(null);
        for (Record record : new ArrayList<>(sightings)) group.removeItem(record.marker);
        sightings.clear();
        getMapView().getRootGroup().removeGroup(group);
    }
    @Override public void onDropDownSelectionRemoved() { }
    @Override public void onDropDownVisible(boolean visible) { }
    @Override public void onDropDownSizeChanged(double width, double height) { }
    @Override public void onDropDownClose() { }

    private static final class Record {
        final Marker marker; final HonkPolicy.Sighting sighting;
        Record(Marker marker, HonkPolicy.Sighting sighting) { this.marker = marker; this.sighting = sighting; }
    }
}
