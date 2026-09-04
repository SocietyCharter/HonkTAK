package com.societycharter.honktak;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/** Bounded, deterministic Overpass query and response handling. */
public final class OverpassImport {
    public static final int MAX_RESPONSE_BYTES = 1_048_576;
    public static final int MAX_RESULTS = 500;
    public static final double MAX_LAT_SPAN = 1.0;
    public static final double MAX_LON_SPAN = 1.0;
    private OverpassImport() { }

    public static String query(double south, double west, double north, double east) {
        validateBounds(south, west, north, east);
        return "[out:json][timeout:15];node[\"man_made\"=\"surveillance\"]"
                + "[\"surveillance:type\"=\"ALPR\"](" + south + "," + west + ","
                + north + "," + east + ");out body " + MAX_RESULTS + ";";
    }

    public static void validateBounds(double south, double west, double north, double east) {
        if (!Double.isFinite(south) || !Double.isFinite(west) || !Double.isFinite(north)
                || !Double.isFinite(east) || south < -90 || north > 90 || west < -180
                || east > 180 || south >= north || west >= east
                || north - south > MAX_LAT_SPAN || east - west > MAX_LON_SPAN) {
            throw new IllegalArgumentException("Viewport is invalid or too large; zoom in and retry.");
        }
    }

    public static List<Camera> parse(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length > MAX_RESPONSE_BYTES) {
            throw new IllegalArgumentException("Overpass response size is invalid.");
        }
        final JSONObject root;
        try { root = new JSONObject(new String(bytes, java.nio.charset.StandardCharsets.UTF_8)); }
        catch (org.json.JSONException e) { throw new IllegalArgumentException("Malformed Overpass JSON.", e); }
        JSONArray elements = root.optJSONArray("elements");
        if (elements == null) throw new IllegalArgumentException("Missing Overpass elements.");
        Map<Long, Camera> deduped = new LinkedHashMap<>();
        for (int i = 0; i < elements.length(); i++) {
            if (deduped.size() >= MAX_RESULTS) break;
            JSONObject element = elements.optJSONObject(i);
            if (element == null || !"node".equals(element.optString("type"))) continue;
            long id = element.optLong("id", -1);
            double lat = element.optDouble("lat", Double.NaN);
            double lon = element.optDouble("lon", Double.NaN);
            JSONObject tags = element.optJSONObject("tags");
            if (id <= 0 || !Double.isFinite(lat) || !Double.isFinite(lon) || tags == null
                    || lat < -90 || lat > 90 || lon < -180 || lon > 180
                    || !"surveillance".equals(tags.optString("man_made"))
                    || !"ALPR".equals(tags.optString("surveillance:type"))) continue;
            Integer direction = parseDirection(tags.optString("direction",
                    tags.optString("camera:direction", "")));
            if (!deduped.containsKey(id)) {
                deduped.put(id, new Camera(id, lat, lon, direction,
                        bounded(tags.optString("operator", "")), bounded(tags.optString("manufacturer", "")),
                        bounded(tags.optString("brand", ""))));
            }
        }
        return new ArrayList<>(deduped.values());
    }

    static Integer parseDirection(String value) {
        try { int n = Integer.parseInt(value.trim()); return n >= 0 && n <= 359 ? n : null; }
        catch (RuntimeException ignored) { return null; }
    }
    private static String bounded(String value) { return CameraObservation.sanitize(value, 80); }

    public static final class Camera {
        public final long osmNodeId; public final String sourceId; public final double latitude, longitude;
        public final Integer direction; public final String operator, manufacturer, brand;
        Camera(long id, double lat, double lon, Integer dir, String op, String maker, String brand) {
            this.osmNodeId=id; this.sourceId="osm:node:"+id; this.latitude=lat; this.longitude=lon;
            this.direction=dir; this.operator=op; this.manufacturer=maker; this.brand=brand;
        }
    }
}
