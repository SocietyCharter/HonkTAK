package com.societycharter.honktak;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.cot.CotMapComponent;
import com.atakmap.android.dropdown.DropDown.OnStateListener;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.DefaultMapGroup;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.comms.CotServiceRemote;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.maps.assets.Icon;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;
import com.societycharter.honktak.plugin.R;

/** User-driven local/save and explicit TAK-network share adapter. No background sends. */
public final class HonkTakDropDownReceiver extends DropDownReceiver implements OnStateListener,
        CotServiceRemote.CotEventListener, CotServiceRemote.ConnectionListener {
    public static final String SHOW_PLUGIN = "com.societycharter.honktak.SHOW_PLUGIN";
    private final Context pluginContext;
    private final View view;
    private final MapGroup group;
    private final Handler handler = new Handler();
    private final List<Record> sightings = new ArrayList<>();
    private final Random random = new Random();
    private final ShareGate shareGate = new ShareGate();
    private final CotServiceRemote cotRemote = new CotServiceRemote();
    private volatile boolean cotConnected;

    public HonkTakDropDownReceiver(MapView mapView, Context context) {
        super(mapView);
        pluginContext = context;
        view = PluginLayoutInflater.inflate(context, R.layout.main_layout, null);
        DefaultMapGroup localGroup = new DefaultMapGroup("HonkTAK Camera Observations");
        localGroup.setMetaBoolean("addToObjList", false);
        mapView.getRootGroup().addGroup(localGroup);
        group = localGroup;
        bindUi();
        cotRemote.setCotEventListener(this);
        cotRemote.connect(this);
    }

    private void bindUi() {
        bindSpinner(R.id.camera_class, new String[]{"fixed", "ptz", "doorbell", "license_plate_reader", "unknown"});
        bindSpinner(R.id.confidence, new String[]{"medium", "low", "high"});
        bindSpinner(R.id.camera_status, new String[]{"active", "inactive", "unknown"});
        bindSpinner(R.id.location_source, new String[]{"selected map point (map center)", "explicit current position"});
        LinearLayout form = view.findViewById(R.id.observation_form);
        view.findViewById(R.id.report_honk).setOnClickListener(v -> form.setVisibility(View.VISIBLE));
        view.findViewById(R.id.save_local).setOnClickListener(v -> save(false));
        view.findViewById(R.id.share_team).setOnClickListener(v -> { shareGate.armFromVisibleUserAction(); save(true); });
    }

    private void bindSpinner(int id, String[] values) {
        Spinner spinner = view.findViewById(id);
        spinner.setAdapter(new ArrayAdapter<>(pluginContext, android.R.layout.simple_spinner_dropdown_item, values));
    }

    private void save(boolean requestShare) {
        TextView statusView = view.findViewById(R.id.status);
        try {
            GeoPoint point = selectedPoint();
            long observed = System.currentTimeMillis();
            int expiryMinutes = boundedInt(((EditText) view.findViewById(R.id.expiry_minutes)).getText().toString(), 1, 10080, 30);
            String azimuthText = ((EditText) view.findViewById(R.id.azimuth)).getText().toString().trim();
            Integer azimuth = azimuthText.isEmpty() ? null : boundedInt(azimuthText, 0, 359, -1);
            if (azimuth != null && azimuth < 0) throw new IllegalArgumentException("Azimuth must be 0–359 or blank.");
            CameraObservation observation = new CameraObservation("honktak-" + UUID.randomUUID(), point.getLatitude(), point.getLongitude(),
                CameraObservation.CameraClass.valueOf(selected(R.id.camera_class).toUpperCase()), azimuth,
                CameraObservation.Confidence.valueOf(selected(R.id.confidence).toUpperCase()),
                CameraObservation.Status.valueOf(selected(R.id.camera_status).toUpperCase()),
                ((EditText) view.findViewById(R.id.notes)).getText().toString(), observed, observed + expiryMinutes * 60L * 1000L);
            if (requestShare && !cotConnected) {
                shareGate.consumeForSend();
                statusView.setText("TAK network is disconnected; nothing was saved or sent. Connect ATAK and press SHARE TO TEAM again.");
                return;
            }
            addLocal(observation);
            String sitrep = HonkPolicy.SITREPS[random.nextInt(HonkPolicy.SITREPS.length)];
            if (HonkPolicy.triggersFlockpocalypse(activeSightings(), observed, expiryMinutes * 60L * 1000L)) sitrep = "FLOCKPOCALYPSE";
            if (requestShare) {
                if (!shareGate.consumeForSend()) throw new IllegalStateException("Share requires the visible SHARE TO TEAM action.");
                String xml = HonkCotCodec.serialize(observation);
                CotEvent event = CotEvent.parse(xml);
                if (event == null || !event.isValid()) throw new IllegalArgumentException("Generated CoT failed validation.");
                CotMapComponent.getExternalDispatcher().dispatchToBroadcast(event);
                statusView.setText("Shared to currently connected TAK network. " + sitrep);
            } else {
                statusView.setText("Saved locally; nothing left this device. " + sitrep);
            }
        } catch (RuntimeException e) {
            shareGate.consumeForSend();
            statusView.setText("Observation not saved: " + CameraObservation.sanitize(e.getMessage(), 120));
        }
    }

    private GeoPoint selectedPoint() {
        if (selected(R.id.location_source).startsWith("explicit")) {
            Marker self = getMapView().getSelfMarker();
            if (self == null || self.getPoint() == null) throw new IllegalArgumentException("Current position unavailable.");
            return self.getPoint();
        }
        GeoPointMetaData meta = getMapView().getPointWithElevation();
        if (meta == null || meta.get() == null) throw new IllegalArgumentException("Selected map point unavailable.");
        return meta.get();
    }

    private void addLocal(CameraObservation observation) {
        for (Record existing : sightings) if (existing.observation.uid.equals(observation.uid)) return;
        Marker marker = new Marker(new GeoPoint(observation.latitude, observation.longitude), observation.uid);
        marker.setTitle(HonkPolicy.MARKER_LABEL);
        marker.setType(HonkCotCodec.COT_TYPE);
        marker.setAlwaysShowText(true);
        marker.setMetaBoolean("nevercot", true);
        marker.setMetaBoolean("archive", false);
        marker.setMetaBoolean("honktak.local_only_render", true);
        marker.setMetaString("honktak.camera_class", observation.cameraClass.name().toLowerCase());
        marker.setIcon(gooseIcon());
        group.addItem(marker);
        sightings.add(new Record(marker, observation));
        handler.postDelayed(() -> expire(marker), Math.max(1, observation.staleAtMs - System.currentTimeMillis()));
    }

    @Override public void onCotEvent(CotEvent event, Bundle extra) {
        if (event == null || !HonkCotCodec.COT_TYPE.equals(event.getType())) return;
        try {
            CameraObservation observation = HonkCotCodec.parse(event.toString(), System.currentTimeMillis());
            handler.post(() -> addLocal(observation));
        } catch (IllegalArgumentException ignored) { /* malformed, oversized, stale, or out-of-range */ }
    }

    private List<HonkPolicy.Sighting> activeSightings() {
        List<HonkPolicy.Sighting> result = new ArrayList<>();
        for (Record r : sightings) result.add(new HonkPolicy.Sighting(r.observation.latitude, r.observation.longitude, r.observation.observedAtMs));
        return result;
    }
    private void expire(Marker marker) { group.removeItem(marker); for (int i = sightings.size() - 1; i >= 0; i--) if (sightings.get(i).marker == marker) sightings.remove(i); }
    private String selected(int id) { return ((Spinner) view.findViewById(id)).getSelectedItem().toString(); }
    private static int boundedInt(String value, int min, int max, int fallback) { try { int n = Integer.parseInt(value); return n < min || n > max ? fallback : n; } catch (NumberFormatException e) { return fallback; } }
    private Icon gooseIcon() { Bitmap b = BitmapFactory.decodeResource(pluginContext.getResources(), R.drawable.ic_goose); ByteArrayOutputStream out = new ByteArrayOutputStream(); b.compress(Bitmap.CompressFormat.PNG, 100, out); return new Icon.Builder().setImageUri(0, "base64://" + Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP | Base64.URL_SAFE)).build(); }

    @Override public void onReceive(Context context, Intent intent) { if (intent != null && SHOW_PLUGIN.equals(intent.getAction())) showDropDown(view, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH, HALF_HEIGHT, false, this); }
    @Override protected void disposeImpl() { handler.removeCallbacksAndMessages(null); cotRemote.setCotEventListener(null); cotRemote.disconnect(); for (Record r : new ArrayList<>(sightings)) group.removeItem(r.marker); sightings.clear(); getMapView().getRootGroup().removeGroup(group); }
    @Override public void onCotServiceConnected(Bundle state) { cotConnected = true; }
    @Override public void onCotServiceDisconnected() { cotConnected = false; }
    @Override public void onDropDownSelectionRemoved() { }
    @Override public void onDropDownVisible(boolean visible) { }
    @Override public void onDropDownSizeChanged(double width, double height) { }
    @Override public void onDropDownClose() { }

    private static final class Record { final Marker marker; final CameraObservation observation; Record(Marker marker, CameraObservation observation) { this.marker = marker; this.observation = observation; } }
}
