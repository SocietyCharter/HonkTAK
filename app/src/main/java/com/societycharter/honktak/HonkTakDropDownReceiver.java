package com.societycharter.honktak;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Future;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.cot.CotMapComponent;
import com.atakmap.android.dropdown.DropDown.OnStateListener;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.DefaultMapGroup;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.SensorFOV;
import com.atakmap.comms.CotServiceRemote;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.maps.assets.Icon;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;
import com.atakmap.map.AtakMapView;
import com.societycharter.honktak.plugin.R;

/** User-driven local/save and explicit TAK-network share adapter. No background sends. */
public final class HonkTakDropDownReceiver extends DropDownReceiver implements OnStateListener,
        CotServiceRemote.CotEventListener, CotServiceRemote.ConnectionListener,
        MapEventDispatcher.MapEventDispatchListener, AtakMapView.OnMapMovedListener {
    public static final String SHOW_PLUGIN = "com.societycharter.honktak.SHOW_PLUGIN";
    private static final String PREFS_NAME = "honktak_local_observations_v1";
    private static final String PREF_PREFIX = "observation.";
    private static final String IMPORT_OVERRIDE_PREFIX = "import.override.";
    private final Context pluginContext;
    private final View view;
    private final MapGroup group;
    private final MapGroup importedGroup;
    private final Handler handler = new Handler();
    private final List<Record> sightings = new ArrayList<>();
    private final Random random = new Random();
    private final ShareGate shareGate = new ShareGate();
    private final CotServiceRemote cotRemote = new CotServiceRemote();
    private final double gooseReferenceMapScale;
    private String gooseImageUri;
    private int gooseBaseWidth;
    private int gooseBaseHeight;
    private int gooseRenderedWidth = -1;
    private int gooseRenderedHeight = -1;
    private volatile boolean cotConnected;
    private final PlacementSession placement = new PlacementSession();
    private final ExecutorService importExecutor = Executors.newSingleThreadExecutor();
    private final AtomicInteger importGeneration = new AtomicInteger();
    private final ImportedCameraState importedState = new ImportedCameraState();
    private final List<ImportedRecord> imported = new ArrayList<>();
    private String selectedImportedSourceId;
    private String redXImageUri;
    private Future<?> importFuture;
    private String cachedQuery;
    private List<OverpassImport.Camera> cachedCameras;
    private long cacheAtMs;
    private static final long IMPORT_CACHE_TTL_MS = 5 * 60 * 1000L;
    private GeoPoint pendingAnchor;
    private double pendingAzimuth;
    private double pendingRange = PlacementMath.DEFAULT_RANGE_METERS;
    private double pendingFov = PlacementMath.DEFAULT_FOV_DEGREES;
    private Marker previewMarker;
    private SensorFOV previewWedge;

    public HonkTakDropDownReceiver(MapView mapView, Context context) {
        super(mapView);
        pluginContext = context;
        gooseReferenceMapScale = mapView.getMapScale();
        view = PluginLayoutInflater.inflate(context, R.layout.main_layout, null);
        DefaultMapGroup localGroup = new DefaultMapGroup("HonkTAK Camera Observations");
        localGroup.setMetaBoolean("addToObjList", false);
        mapView.getRootGroup().addGroup(localGroup);
        group = localGroup;
        DefaultMapGroup imports = new DefaultMapGroup("HonkTAK OSM ALPR Cameras");
        imports.setMetaBoolean("addToObjList", true);
        mapView.getRootGroup().addGroup(imports);
        importedGroup = imports;
        prepareGooseImage();
        restoreImportOverrides();
        mapView.getMapEventDispatcher().addMapEventListener(MapEvent.ITEM_CLICK, this);
        mapView.getMapEventDispatcher().addMapEventListener(MapEvent.ITEM_LONG_PRESS, this);
        mapView.addOnMapMovedListener(this);
        bindUi();
        restorePersisted();
        cotRemote.setCotEventListener(this);
        cotRemote.connect(this);
    }

    private void bindUi() {
        bindSpinner(R.id.camera_class, new String[]{"fixed", "ptz", "doorbell", "license_plate_reader", "unknown"});
        bindSpinner(R.id.confidence, new String[]{"medium", "low", "high"});
        bindSpinner(R.id.camera_status, new String[]{"active", "inactive", "unknown"});
        CheckBox temporary = view.findViewById(R.id.temporary_marker);
        temporary.setChecked(false);
        temporary.setOnCheckedChangeListener((button, checked) -> {
            View controls = view.findViewById(R.id.expiry_controls);
            controls.setVisibility(checked ? View.VISIBLE : View.GONE);
            view.findViewById(R.id.expiry_minutes).setEnabled(checked);
        });
        view.findViewById(R.id.report_honk).setOnClickListener(v -> beginPlacement());
        view.findViewById(R.id.cancel_placement).setOnClickListener(v -> cancelPlacement("Placement cancelled."));
        view.findViewById(R.id.save_local).setOnClickListener(v -> save(false));
        view.findViewById(R.id.share_team).setOnClickListener(v -> { shareGate.armFromVisibleUserAction(); save(true); });
        view.findViewById(R.id.load_cameras).setOnClickListener(v -> loadCamerasInView());
        view.findViewById(R.id.mark_taken_down).setOnClickListener(v -> setSelectedImportedRemoved(true));
        view.findViewById(R.id.mark_active).setOnClickListener(v -> setSelectedImportedRemoved(false));
    }

    private void resetLifetimeUi() {
        ((CheckBox) view.findViewById(R.id.temporary_marker)).setChecked(false);
        view.findViewById(R.id.expiry_controls).setVisibility(View.GONE);
        view.findViewById(R.id.expiry_minutes).setEnabled(false);
    }

    private void bindSpinner(int id, String[] values) {
        Spinner spinner = view.findViewById(id);
        spinner.setAdapter(new ArrayAdapter<>(pluginContext, android.R.layout.simple_spinner_dropdown_item, values));
    }

    private void save(boolean requestShare) {
        TextView statusView = view.findViewById(R.id.status);
        try {
            if (pendingAnchor == null) throw new IllegalArgumentException("Press REPORT HONK and place the observation on the map first.");
            GeoPoint point = pendingAnchor;
            long observed = System.currentTimeMillis();
            boolean temporary = ((CheckBox) view.findViewById(R.id.temporary_marker)).isChecked();
            int expiryMinutes = temporary
                    ? boundedInt(((EditText) view.findViewById(R.id.expiry_minutes)).getText().toString(), 1, 10080, -1)
                    : -1;
            if (temporary && expiryMinutes < 0) {
                throw new IllegalArgumentException("Temporary expiration must be 1–10080 minutes.");
            }
            String azimuthText = ((EditText) view.findViewById(R.id.azimuth)).getText().toString().trim();
            Integer azimuth = azimuthText.isEmpty() ? (int) Math.round(pendingAzimuth) % 360 : boundedInt(azimuthText, 0, 359, -1);
            if (azimuth != null && azimuth < 0) throw new IllegalArgumentException("Azimuth must be 0–359 or blank.");
            CameraObservation observation = new CameraObservation("honktak-" + UUID.randomUUID(), point.getLatitude(), point.getLongitude(),
                CameraObservation.CameraClass.valueOf(selected(R.id.camera_class).toUpperCase()), azimuth,
                pendingRange, pendingFov,
                CameraObservation.Confidence.valueOf(selected(R.id.confidence).toUpperCase()),
                CameraObservation.Status.valueOf(selected(R.id.camera_status).toUpperCase()),
                ((EditText) view.findViewById(R.id.notes)).getText().toString(), observed,
                temporary ? observed + expiryMinutes * 60L * 1000L : CameraObservation.PERMANENT);
            if (requestShare && !cotConnected) {
                shareGate.consumeForSend();
                statusView.setText("TAK network is disconnected; nothing was saved or sent. Connect ATAK and press SHARE TO TEAM again.");
                return;
            }
            addLocal(observation, true);
            clearPendingPreview();
            view.findViewById(R.id.cancel_placement).setVisibility(View.GONE);
            String sitrep = HonkPolicy.SITREPS[random.nextInt(HonkPolicy.SITREPS.length)];
            long flockExpiry = temporary ? expiryMinutes * 60L * 1000L : -1L;
            if (HonkPolicy.triggersFlockpocalypse(activeSightings(), observed, flockExpiry)) sitrep = "FLOCKPOCALYPSE";
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

    private void addLocal(CameraObservation observation, boolean persist) {
        for (Record existing : sightings) if (existing.observation.uid.equals(observation.uid)) return;
        Marker marker = new Marker(new GeoPoint(observation.latitude, observation.longitude), observation.uid);
        marker.setTitle(HonkPolicy.MARKER_LABEL);
        marker.setType(HonkCotCodec.COT_TYPE);
        marker.setAlwaysShowText(true);
        marker.setMetaBoolean("nevercot", true);
        marker.setMetaBoolean("archive", false);
        marker.setMetaBoolean("honktak.local_only_render", true);
        marker.setMetaBoolean("honktak.permanent", observation.isPermanent());
        if (!observation.isPermanent()) {
            marker.setMetaLong("honktak.expires_at_ms", observation.staleAtMs);
        }
        marker.setMetaString("honktak.camera_class", observation.cameraClass.name().toLowerCase());
        // ATAK's marker adapter otherwise replaces custom icons based on CoT type
        // when the marker enters a map group. This mirrors the ATAK 5.6 SDK
        // custom-marker contract and keeps the packaged goose visible.
        marker.setMetaBoolean("adapt_marker_icon", false);
        marker.setIcon(gooseIcon(getMapView().getMapScale()));
        marker.setIconVisibility(Marker.ICON_VISIBLE);
        SensorFOV wedge = createWedge(observation.uid + "-fov", marker.getPoint(),
                observation.azimuth == null ? 0 : observation.azimuth,
                observation.rangeMeters, observation.fovDegrees);
        group.addItem(marker);
        group.addItem(wedge);
        sightings.add(new Record(marker, wedge, observation));
        if (persist) persist(observation);
        if (!observation.isPermanent()) {
            handler.postDelayed(() -> expire(marker),
                    Math.max(1, observation.staleAtMs - System.currentTimeMillis()));
        }
    }

    @Override public void onCotEvent(CotEvent event, Bundle extra) {
        if (event == null || !HonkCotCodec.COT_TYPE.equals(event.getType())) return;
        try {
            CameraObservation observation = HonkCotCodec.parse(event.toString(), System.currentTimeMillis());
            handler.post(() -> addLocal(observation, true));
        } catch (IllegalArgumentException ignored) { /* malformed, oversized, stale, or out-of-range */ }
    }

    private List<HonkPolicy.Sighting> activeSightings() {
        List<HonkPolicy.Sighting> result = new ArrayList<>();
        for (Record r : sightings) result.add(new HonkPolicy.Sighting(r.observation.latitude, r.observation.longitude, r.observation.observedAtMs));
        return result;
    }
    private void expire(Marker marker) {
        group.removeItem(marker);
        for (int i = sightings.size() - 1; i >= 0; i--) {
            if (sightings.get(i).marker == marker) {
                group.removeItem(sightings.get(i).wedge);
                removePersisted(sightings.get(i).observation.uid);
                sightings.remove(i);
            }
        }
    }
    private SharedPreferences preferences() {
        return pluginContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    private void persist(CameraObservation observation) {
        preferences().edit().putString(PREF_PREFIX + observation.uid,
                HonkCotCodec.serialize(observation)).apply();
    }
    private void removePersisted(String uid) {
        preferences().edit().remove(PREF_PREFIX + uid).apply();
    }
    private void restorePersisted() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, ?> entry : preferences().getAll().entrySet()) {
            if (!entry.getKey().startsWith(PREF_PREFIX) || !(entry.getValue() instanceof String)) continue;
            try {
                CameraObservation observation = HonkCotCodec.parsePersisted((String) entry.getValue(), now);
                addLocal(observation, false);
            } catch (IllegalArgumentException invalidOrExpired) {
                preferences().edit().remove(entry.getKey()).apply();
            }
        }
    }
    private String selected(int id) { return ((Spinner) view.findViewById(id)).getSelectedItem().toString(); }
    private static int boundedInt(String value, int min, int max, int fallback) { try { int n = Integer.parseInt(value); return n < min || n > max ? fallback : n; } catch (NumberFormatException e) { return fallback; } }
    private void prepareGooseImage() {
        Drawable drawable = pluginContext.getResources().getDrawable(R.drawable.ic_goose);
        if (drawable == null) throw new IllegalStateException("Goose icon resource unavailable.");
        gooseBaseWidth = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 64;
        gooseBaseHeight = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 64;
        gooseRenderedWidth = GooseIconScale.dimension(gooseBaseWidth, gooseReferenceMapScale, gooseReferenceMapScale);
        gooseRenderedHeight = GooseIconScale.dimension(gooseBaseHeight, gooseReferenceMapScale, gooseReferenceMapScale);
        Bitmap bitmap = Bitmap.createBitmap(gooseBaseWidth, gooseBaseHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, gooseBaseWidth, gooseBaseHeight);
        drawable.draw(canvas);
        gooseImageUri = "base64://" + Base64.encodeToString(
                BitmapIconEncoder.png(bitmap), Base64.NO_WRAP | Base64.URL_SAFE);
        Bitmap defeatedBitmap = Bitmap.createBitmap(gooseBaseWidth, gooseBaseHeight, Bitmap.Config.ARGB_8888);
        Canvas defeatedCanvas = new Canvas(defeatedBitmap);
        ColorMatrix desaturate = new ColorMatrix();
        desaturate.setSaturation(0.22f);
        Paint defeatedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        defeatedPaint.setColorFilter(new ColorMatrixColorFilter(desaturate));
        defeatedPaint.setAlpha(205);
        defeatedCanvas.drawBitmap(bitmap, 0, 0, defeatedPaint);
        Paint x = new Paint(Paint.ANTI_ALIAS_FLAG); x.setColor(0xFFFF0000); x.setStrokeWidth(Math.max(6, gooseBaseWidth / 10f));
        defeatedCanvas.drawLine(4, 4, gooseBaseWidth-4, gooseBaseHeight-4, x);
        defeatedCanvas.drawLine(gooseBaseWidth-4, 4, 4, gooseBaseHeight-4, x);
        redXImageUri = "base64://" + Base64.encodeToString(BitmapIconEncoder.png(defeatedBitmap), Base64.NO_WRAP | Base64.URL_SAFE);
    }

    private Icon gooseIcon(double mapScale) {
        gooseRenderedWidth = GooseIconScale.dimension(gooseBaseWidth, mapScale, gooseReferenceMapScale);
        gooseRenderedHeight = GooseIconScale.dimension(gooseBaseHeight, mapScale, gooseReferenceMapScale);
        return new Icon.Builder().setImageUri(0, gooseImageUri)
                .setSize(gooseRenderedWidth, gooseRenderedHeight)
                .setAnchor(Icon.ANCHOR_CENTER, Icon.ANCHOR_CENTER).build();
    }

    @Override public void onMapMoved(AtakMapView mapView, boolean animate) {
        int width = GooseIconScale.dimension(gooseBaseWidth, mapView.getMapScale(), gooseReferenceMapScale);
        int height = GooseIconScale.dimension(gooseBaseHeight, mapView.getMapScale(), gooseReferenceMapScale);
        if (width == gooseRenderedWidth && height == gooseRenderedHeight) return;
        Icon icon = new Icon.Builder().setImageUri(0, gooseImageUri).setSize(width, height)
                .setAnchor(Icon.ANCHOR_CENTER, Icon.ANCHOR_CENTER).build();
        gooseRenderedWidth = width;
        gooseRenderedHeight = height;
        for (Record record : sightings) record.marker.setIcon(icon);
        for (ImportedRecord record : imported) record.marker.setIcon(importIcon(importedState.isRemoved(record.camera.sourceId)));
    }

    private void beginPlacement() {
        cancelPlacement(null);
        resetLifetimeUi();
        placement.begin();
        MapEventDispatcher dispatcher = getMapView().getMapEventDispatcher();
        dispatcher.pushListeners();
        dispatcher.clearUserInteractionListeners(false);
        dispatcher.addMapEventListener(MapEvent.MAP_LONG_PRESS, this);
        dispatcher.addMapEventListener(MapEvent.MAP_DRAW, this);
        dispatcher.addMapEventListener(MapEvent.MAP_RELEASE, this);
        view.findViewById(R.id.cancel_placement).setVisibility(View.VISIBLE);
        view.findViewById(R.id.observation_form).setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.status)).setText("Placement active: long-press a camera location, drag to aim, then release.");
        Toast.makeText(pluginContext, "Long-press the camera location, drag to aim, then release.", Toast.LENGTH_LONG).show();
        hideDropDown();
    }

    @Override public void onMapEvent(MapEvent event) {
        if (event != null && (MapEvent.ITEM_CLICK.equals(event.getType()) || MapEvent.ITEM_LONG_PRESS.equals(event.getType())) && event.getItem() instanceof MapItem) {
            String source=((MapItem)event.getItem()).getMetaString("honktak.source_id", null);
            if (source != null) { selectImportedSource(source); return; }
        }
        if (!placement.isActive() || event == null || event.getPointF() == null) return;
        GeoPointMetaData meta = getMapView().inverseWithElevation(event.getPointF().x, event.getPointF().y);
        if (meta == null || meta.get() == null) return;
        if (MapEvent.MAP_LONG_PRESS.equals(event.getType())) {
            pendingAnchor = meta.get();
            pendingAzimuth = 0;
            pendingRange = PlacementMath.DEFAULT_RANGE_METERS;
            showPreview();
        } else if (MapEvent.MAP_DRAW.equals(event.getType()) && pendingAnchor != null) {
            updateAim(meta.get());
        } else if (MapEvent.MAP_RELEASE.equals(event.getType()) && pendingAnchor != null) {
            updateAim(meta.get());
            restorePlacementListeners();
            ((EditText) view.findViewById(R.id.azimuth)).setText(Integer.toString((int) Math.round(pendingAzimuth) % 360));
            view.findViewById(R.id.cancel_placement).setVisibility(View.VISIBLE);
            view.findViewById(R.id.observation_form).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.status)).setText("Pending map observation: review fields, then SAVE LOCALLY or explicitly SHARE TO TEAM.");
            unhideDropDown();
        }
    }

    private void updateAim(GeoPoint drag) {
        pendingAzimuth = PlacementMath.bearing(pendingAnchor.getLatitude(), pendingAnchor.getLongitude(), drag.getLatitude(), drag.getLongitude());
        pendingRange = PlacementMath.clampRange(PlacementMath.distance(pendingAnchor.getLatitude(), pendingAnchor.getLongitude(), drag.getLatitude(), drag.getLongitude()));
        showPreview();
    }

    private void showPreview() {
        if (previewMarker == null) {
            previewMarker = new Marker(pendingAnchor, "honktak-pending-" + UUID.randomUUID());
            previewMarker.setTitle("Pending Unidentified Waterfowl");
            previewMarker.setMetaBoolean("nevercot", true);
            previewMarker.setMetaBoolean("archive", false);
            group.addItem(previewMarker);
            previewWedge = createWedge(previewMarker.getUID() + "-fov", pendingAnchor, pendingAzimuth, pendingRange, pendingFov);
            group.addItem(previewWedge);
        } else {
            previewMarker.setPoint(pendingAnchor);
            previewWedge.setPoint(GeoPointMetaData.wrap(pendingAnchor));
            previewWedge.setMetrics((float) pendingAzimuth, (float) pendingFov, (float) pendingRange);
        }
    }

    private SensorFOV createWedge(String uid, GeoPoint point, double azimuth, double range, double fov) {
        SensorFOV wedge = new SensorFOV(uid);
        wedge.setPoint(GeoPointMetaData.wrap(point));
        wedge.setMetrics((float) azimuth, (float) fov, (float) range);
        wedge.setColor(0xFFFFA000);
        wedge.setAlpha(0.35f);
        wedge.setMetaBoolean("nevercot", true);
        wedge.setMetaBoolean("archive", false);
        wedge.setMetaBoolean("honktak.local_only_render", true);
        return wedge;
    }

    private void restorePlacementListeners() {
        if (placement.shouldRestoreListeners()) getMapView().getMapEventDispatcher().popListeners();
    }

    private void cancelPlacement(String message) {
        restorePlacementListeners();
        clearPendingPreview();
        view.findViewById(R.id.cancel_placement).setVisibility(View.GONE);
        if (message != null) {
            ((TextView) view.findViewById(R.id.status)).setText(message);
            if (!isClosed()) unhideDropDown();
        }
    }

    private void clearPendingPreview() {
        if (previewMarker != null) group.removeItem(previewMarker);
        if (previewWedge != null) group.removeItem(previewWedge);
        previewMarker = null; previewWedge = null; pendingAnchor = null;
    }

    @Override public void onReceive(Context context, Intent intent) { if (intent != null && SHOW_PLUGIN.equals(intent.getAction())) showDropDown(view, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH, HALF_HEIGHT, false, this); }
    @Override protected void disposeImpl() { importGeneration.incrementAndGet(); if(importFuture!=null){OverpassClient.cancelActive();importFuture.cancel(true);} importExecutor.shutdownNow(); getMapView().getMapEventDispatcher().removeMapEventListener(MapEvent.ITEM_CLICK,this); getMapView().getMapEventDispatcher().removeMapEventListener(MapEvent.ITEM_LONG_PRESS,this); getMapView().removeOnMapMovedListener(this); restorePlacementListeners(); clearPendingPreview(); handler.removeCallbacksAndMessages(null); cotRemote.setCotEventListener(null); cotRemote.disconnect(); for (Record r : new ArrayList<>(sightings)) { group.removeItem(r.marker); group.removeItem(r.wedge); } for (ImportedRecord r:new ArrayList<>(imported)) { importedGroup.removeItem(r.marker); if(r.wedge!=null) importedGroup.removeItem(r.wedge); } imported.clear(); sightings.clear(); getMapView().getRootGroup().removeGroup(importedGroup); getMapView().getRootGroup().removeGroup(group); }
    @Override public void onCotServiceConnected(Bundle state) { cotConnected = true; }
    @Override public void onCotServiceDisconnected() { cotConnected = false; }
    @Override public void onDropDownSelectionRemoved() { }
    @Override public void onDropDownVisible(boolean visible) { }
    @Override public void onDropDownSizeChanged(double width, double height) { }
    @Override public void onDropDownClose() { cancelPlacement(null); }

    private static final class Record { final Marker marker; final SensorFOV wedge; final CameraObservation observation; Record(Marker marker, SensorFOV wedge, CameraObservation observation) { this.marker = marker; this.wedge = wedge; this.observation = observation; } }

    private void loadCamerasInView() {
        GeoPointMetaData[] corners={getMapView().inverseWithElevation(0,0),getMapView().inverseWithElevation(getMapView().getWidth(),0),getMapView().inverseWithElevation(0,getMapView().getHeight()),getMapView().inverseWithElevation(getMapView().getWidth(),getMapView().getHeight())};
        double south=90,north=-90,west=180,east=-180; for(GeoPointMetaData corner:corners){ if(corner==null||corner.get()==null){((TextView)view.findViewById(R.id.status)).setText("Cannot read current viewport.");return;} south=Math.min(south,corner.get().getLatitude());north=Math.max(north,corner.get().getLatitude());west=Math.min(west,corner.get().getLongitude());east=Math.max(east,corner.get().getLongitude()); }
        final String query; try { query=OverpassImport.query(south,west,north,east); } catch(IllegalArgumentException e) { ((TextView)view.findViewById(R.id.status)).setText(e.getMessage()); return; }
        long now=System.currentTimeMillis(); if(query.equals(cachedQuery)&&cachedCameras!=null&&now-cacheAtMs<=IMPORT_CACHE_TTL_MS){renderImported(cachedCameras);((TextView)view.findViewById(R.id.status)).setText("Imported "+cachedCameras.size()+" cached OSM ALPR cameras. © OpenStreetMap contributors.");return;}
        int generation=importGeneration.incrementAndGet(); ((TextView)view.findViewById(R.id.status)).setText("Loading OSM ALPR cameras for this viewport…");
        if(importFuture!=null){OverpassClient.cancelActive();importFuture.cancel(true);}
        importFuture=importExecutor.submit(() -> { try { List<OverpassImport.Camera> cameras=OverpassClient.load(query); handler.post(() -> { if(generation!=importGeneration.get()) return; cachedQuery=query;cachedCameras=cameras;cacheAtMs=System.currentTimeMillis();renderImported(cameras); ((TextView)view.findViewById(R.id.status)).setText("Imported "+cameras.size()+" OSM ALPR cameras. © OpenStreetMap contributors."); }); } catch(Exception e) { handler.post(() -> { if(generation==importGeneration.get()) { if(query.equals(cachedQuery)&&cachedCameras!=null){renderImported(cachedCameras);((TextView)view.findViewById(R.id.status)).setText("Offline — showing stale cached OSM cameras.");} else ((TextView)view.findViewById(R.id.status)).setText("Camera import offline/error: "+CameraObservation.sanitize(e.getMessage(),100)); } }); } });
    }
    private void renderImported(List<OverpassImport.Camera> cameras) {
        for(ImportedRecord r:imported){ importedGroup.removeItem(r.marker); if(r.wedge!=null) importedGroup.removeItem(r.wedge); } imported.clear();
        for(OverpassImport.Camera c: importedState.merge(cameras).values()) { boolean removed=importedState.isRemoved(c.sourceId); Marker marker=new Marker(new GeoPoint(c.latitude,c.longitude),"honktak-import-"+c.osmNodeId); marker.setTitle(removed?"DEFEATED — THIS ONE WAS TAKEN DOWN":"OSM ALPR Camera — tap to select"); marker.setMetaString("honktak.source_id",c.sourceId); marker.setMetaString("honktak.provenance","OpenStreetMap/Overpass"); marker.setMetaBoolean("nevercot",true); marker.setMetaBoolean("archive",false); marker.setMetaBoolean("adapt_marker_icon",false); marker.setMetaBoolean("clickable",true); marker.setIcon(importIcon(removed)); marker.setIconVisibility(Marker.ICON_VISIBLE); importedGroup.addItem(marker); SensorFOV wedge=null; if(ImportedCameraState.showWedge(c,removed)){ wedge=createWedge(marker.getUID()+"-fov",marker.getPoint(),c.direction,PlacementMath.DEFAULT_RANGE_METERS,PlacementMath.DEFAULT_FOV_DEGREES); wedge.setMetaString("honktak.source_id",c.sourceId); wedge.setMetaBoolean("clickable",true); importedGroup.addItem(wedge); } imported.add(new ImportedRecord(marker,wedge,c)); }
    }
    private Icon importIcon(boolean removed) { int width=GooseIconScale.importedDimension(gooseRenderedWidth,removed); int height=GooseIconScale.importedDimension(gooseRenderedHeight,removed); return new Icon.Builder().setImageUri(0,removed?redXImageUri:gooseImageUri).setSize(width,height).setAnchor(Icon.ANCHOR_CENTER,Icon.ANCHOR_CENTER).build(); }
    private void restoreImportOverrides(){ for(Map.Entry<String,?> e:preferences().getAll().entrySet()){ if(!e.getKey().startsWith(IMPORT_OVERRIDE_PREFIX)||!(e.getValue() instanceof String))continue; importedState.restoreOverride(e.getKey().substring(IMPORT_OVERRIDE_PREFIX.length()),(String)e.getValue()); } }
    private void selectImportedSource(String source){ selectedImportedSourceId=source; updateImportActionUi(); ((TextView)view.findViewById(R.id.status)).setText("Imported goose selected. Choose MARK DEFEATED — LOCAL ONLY or MARK ACTIVE."); Toast.makeText(pluginContext,"Imported goose selected — action controls opened.",Toast.LENGTH_LONG).show(); if(isClosed()) showDropDown(view,HALF_WIDTH,FULL_HEIGHT,FULL_WIDTH,HALF_HEIGHT,false,this); else unhideDropDown(); }
    private void setSelectedImportedRemoved(boolean removed){ if(selectedImportedSourceId==null)return; long now=System.currentTimeMillis(); if(removed) importedState.markRemoved(selectedImportedSourceId,now); else importedState.markActive(selectedImportedSourceId,now); preferences().edit().putString(IMPORT_OVERRIDE_PREFIX+selectedImportedSourceId,ImportedCameraState.encodeOverride(removed,now)).apply(); List<OverpassImport.Camera> snapshot=new ArrayList<>(); for(ImportedRecord record:imported)snapshot.add(record.camera); renderImported(snapshot); updateImportActionUi(); ((TextView)view.findViewById(R.id.status)).setText(removed?"DEFEATED — stored locally; no network write.":"Marked active locally."); }
    private void updateImportActionUi(){ String action=importedState.availableAction(selectedImportedSourceId); boolean defeated=ImportedCameraState.ACTION_MARK_ACTIVE.equals(action); view.findViewById(R.id.mark_taken_down).setVisibility(ImportedCameraState.ACTION_MARK_DEFEATED.equals(action)?View.VISIBLE:View.GONE); view.findViewById(R.id.mark_active).setVisibility(defeated?View.VISIBLE:View.GONE); ((TextView)view.findViewById(R.id.import_selection)).setText(selectedImportedSourceId==null?"No imported goose selected.":(defeated?"Selected imported goose is DEFEATED (local only).":"Selected imported goose is ACTIVE.")); }
    private static final class ImportedRecord { final Marker marker; final SensorFOV wedge; final OverpassImport.Camera camera; ImportedRecord(Marker m,SensorFOV w,OverpassImport.Camera c){marker=m;wedge=w;camera=c;} }
}
