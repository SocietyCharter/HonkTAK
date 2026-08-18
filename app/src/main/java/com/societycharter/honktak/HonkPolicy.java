package com.societycharter.honktak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pure local policy used by the ATAK adapter and host-side tests. */
public final class HonkPolicy {
    public static final int FLOCK_THRESHOLD = 3;
    public static final double FLOCK_RADIUS_METERS = 500.0;
    public static final long DEFAULT_EXPIRY_MS = 30L * 60L * 1000L;
    public static final boolean DEFAULT_AUDIO_ENABLED = false;
    public static final String MARKER_LABEL = "Unidentified Waterfowl";
    public static final String[] SITREPS = {
        "Goose has achieved air superiority.",
        "Hostile bread acquisition detected.",
        "Negotiations failed. Goose remains belligerent.",
        "Flock-sized element moving north."
    };

    private HonkPolicy() { }

    public static boolean isExpired(long createdAtMs, long nowMs, long expiryMs) {
        return expiryMs >= 0 && nowMs - createdAtMs >= expiryMs;
    }

    public static boolean triggersFlockpocalypse(List<Sighting> sightings, long nowMs, long expiryMs) {
        List<Sighting> active = new ArrayList<>();
        for (Sighting sighting : sightings) {
            if (!isExpired(sighting.createdAtMs, nowMs, expiryMs)) active.add(sighting);
        }
        for (Sighting origin : active) {
            int nearby = 0;
            for (Sighting candidate : active) {
                if (distanceMeters(origin.latitude, origin.longitude, candidate.latitude, candidate.longitude) <= FLOCK_RADIUS_METERS) nearby++;
            }
            if (nearby >= FLOCK_THRESHOLD) return true;
        }
        return false;
    }

    static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371000.0;
        double p1 = Math.toRadians(lat1);
        double p2 = Math.toRadians(lat2);
        double dp = Math.toRadians(lat2 - lat1);
        double dl = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dp / 2) * Math.sin(dp / 2) + Math.cos(p1) * Math.cos(p2) * Math.sin(dl / 2) * Math.sin(dl / 2);
        return 2 * r * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public static final class Sighting {
        public final double latitude;
        public final double longitude;
        public final long createdAtMs;
        public Sighting(double latitude, double longitude, long createdAtMs) {
            this.latitude = latitude; this.longitude = longitude; this.createdAtMs = createdAtMs;
        }
    }
}
