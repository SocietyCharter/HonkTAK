package com.societycharter.honktak;

/** Immutable bounded camera-location observation with no personal or device identifiers. */
public final class CameraObservation {
    public enum CameraClass { FIXED, PTZ, DOORBELL, LICENSE_PLATE_READER, UNKNOWN }
    public enum Confidence { LOW, MEDIUM, HIGH }
    public enum Status { ACTIVE, INACTIVE, UNKNOWN }

    public final String uid;
    public final double latitude;
    public final double longitude;
    public final CameraClass cameraClass;
    public final Integer azimuth;
    public final double rangeMeters;
    public final double fovDegrees;
    public final Confidence confidence;
    public final Status status;
    public final String notes;
    public final long observedAtMs;
    public final long staleAtMs;

    public CameraObservation(String uid, double latitude, double longitude, CameraClass cameraClass,
            Integer azimuth, Confidence confidence, Status status, String notes,
            long observedAtMs, long staleAtMs) {
        this(uid, latitude, longitude, cameraClass, azimuth, PlacementMath.DEFAULT_RANGE_METERS,
                PlacementMath.DEFAULT_FOV_DEGREES, confidence, status, notes, observedAtMs, staleAtMs);
    }

    public CameraObservation(String uid, double latitude, double longitude, CameraClass cameraClass,
            Integer azimuth, double rangeMeters, double fovDegrees, Confidence confidence,
            Status status, String notes, long observedAtMs, long staleAtMs) {
        if (uid == null || !uid.matches("honktak-[0-9a-fA-F-]{36}")) throw new IllegalArgumentException("invalid uid");
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) throw new IllegalArgumentException("invalid point");
        if (cameraClass == null || confidence == null || status == null) throw new IllegalArgumentException("missing classification");
        if (azimuth != null && (azimuth < 0 || azimuth > 359)) throw new IllegalArgumentException("invalid azimuth");
        if (!Double.isFinite(rangeMeters) || rangeMeters < PlacementMath.MIN_RANGE_METERS
                || rangeMeters > PlacementMath.MAX_RANGE_METERS) throw new IllegalArgumentException("invalid range");
        if (!Double.isFinite(fovDegrees) || fovDegrees < 1 || fovDegrees > 120) throw new IllegalArgumentException("invalid fov");
        String clean = sanitize(notes, 160);
        if (observedAtMs <= 0 || staleAtMs <= observedAtMs || staleAtMs - observedAtMs > 7L * 24 * 60 * 60 * 1000) throw new IllegalArgumentException("invalid time range");
        this.uid = uid; this.latitude = latitude; this.longitude = longitude;
        this.cameraClass = cameraClass; this.azimuth = azimuth; this.rangeMeters = rangeMeters;
        this.fovDegrees = fovDegrees; this.confidence = confidence;
        this.status = status; this.notes = clean; this.observedAtMs = observedAtMs; this.staleAtMs = staleAtMs;
    }

    public boolean isStale(long nowMs) { return nowMs >= staleAtMs; }

    public static String sanitize(String value, int maxLength) {
        if (value == null) return "";
        String clean = value.replaceAll("[\\p{Cntrl}&&[^\\n\\t]]", "").replaceAll("\\s+", " ").trim();
        return clean.length() <= maxLength ? clean : clean.substring(0, maxLength);
    }
}
