package com.societycharter.honktak;

/** Pure geometry and bounds for map placement. */
public final class PlacementMath {
    public static final double MIN_RANGE_METERS = 10.0;
    public static final double MAX_RANGE_METERS = 500.0;
    public static final double DEFAULT_RANGE_METERS = 50.0;
    public static final double DEFAULT_FOV_DEGREES = 45.0;
    private static final double EARTH_RADIUS_METERS = 6371008.8;
    private PlacementMath() { }
    public static double normalizeBearing(double value) { double result = value % 360.0; return result < 0 ? result + 360.0 : result; }
    public static double clampRange(double value) { if (!Double.isFinite(value)) return DEFAULT_RANGE_METERS; return Math.max(MIN_RANGE_METERS, Math.min(MAX_RANGE_METERS, value)); }
    public static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double p1 = Math.toRadians(lat1), p2 = Math.toRadians(lat2), dl = Math.toRadians(lon2 - lon1);
        double y = Math.sin(dl) * Math.cos(p2);
        double x = Math.cos(p1) * Math.sin(p2) - Math.sin(p1) * Math.cos(p2) * Math.cos(dl);
        return normalizeBearing(Math.toDegrees(Math.atan2(y, x)));
    }
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double dp = Math.toRadians(lat2 - lat1), dl = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dp / 2) * Math.sin(dp / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dl / 2) * Math.sin(dl / 2);
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
