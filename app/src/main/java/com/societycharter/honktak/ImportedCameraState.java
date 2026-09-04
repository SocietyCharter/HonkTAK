package com.societycharter.honktak;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Local removal overrides. Refreshes merge observations without changing local truth. */
public final class ImportedCameraState {
    public static final String ACTION_NONE = "NONE";
    public static final String ACTION_MARK_DEFEATED = "MARK_DEFEATED";
    public static final String ACTION_MARK_ACTIVE = "MARK_ACTIVE";
    public static final class Override { public final boolean removed; public final long observedAtMs;
        Override(boolean removed, long observedAtMs) { this.removed=removed; this.observedAtMs=observedAtMs; } }
    private final Map<String, Override> overrides = new LinkedHashMap<>();
    public void markRemoved(String sourceId, long now) { require(sourceId, now); overrides.put(sourceId, new Override(true, now)); }
    public void markActive(String sourceId, long now) { require(sourceId, now); overrides.put(sourceId, new Override(false, now)); }
    public boolean isRemoved(String sourceId) { Override o=overrides.get(sourceId); return o != null && o.removed; }
    public Override get(String sourceId) { return overrides.get(sourceId); }
    public Map<String, OverpassImport.Camera> merge(List<OverpassImport.Camera> incoming) {
        Map<String, OverpassImport.Camera> result=new LinkedHashMap<>();
        for (OverpassImport.Camera camera: incoming) result.put(camera.sourceId, camera);
        return result;
    }
    public static boolean showWedge(OverpassImport.Camera camera, boolean removed) { return !removed && camera.direction != null; }
    public static String iconVariant(boolean removed) { return removed ? "goose_red_x" : "goose"; }
    public String availableAction(String selectedSourceId) {
        if (selectedSourceId == null) return ACTION_NONE;
        return isRemoved(selectedSourceId) ? ACTION_MARK_ACTIVE : ACTION_MARK_DEFEATED;
    }
    public static String encodeOverride(boolean removed, long atMs) {
        if (atMs <= 0) throw new IllegalArgumentException();
        return (removed ? "defeated" : "active") + "|" + atMs;
    }
    public void restoreOverride(String sourceId, String encoded) {
        if (encoded == null) return;
        String[] parts=encoded.split("\\|",2);
        if (parts.length != 2) return;
        try {
            long at=Long.parseLong(parts[1]);
            if ("defeated".equals(parts[0]) || "removed".equals(parts[0])) markRemoved(sourceId,at);
            else if ("active".equals(parts[0])) markActive(sourceId,at);
        } catch (RuntimeException ignored) { }
    }
    private static void require(String id,long now) { if (id==null || !id.matches("osm:node:[1-9][0-9]*") || now<=0) throw new IllegalArgumentException(); }
}
