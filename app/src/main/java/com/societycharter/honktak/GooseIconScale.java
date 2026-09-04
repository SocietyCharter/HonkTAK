package com.societycharter.honktak;

/** Converts ATAK map scale into a bounded Goose icon dimension. */
final class GooseIconScale {
    private static final double MIN_FACTOR = 0.35;
    static final double GLOBAL_HALF_FACTOR = 0.50;
    static final double DEFEATED_FACTOR = 0.82;

    private GooseIconScale() { }

    static int dimension(int baselinePixels, double mapScale, double referenceMapScale) {
        if (baselinePixels < 1) throw new IllegalArgumentException("Baseline dimension must be positive.");
        if (!Double.isFinite(mapScale) || mapScale <= 0
                || !Double.isFinite(referenceMapScale) || referenceMapScale <= 0) {
            return Math.max(1, (int) Math.round(baselinePixels * GLOBAL_HALF_FACTOR));
        }
        double factor = Math.sqrt(mapScale / referenceMapScale);
        factor = Math.max(MIN_FACTOR, Math.min(1.0, factor));
        return Math.max(1, (int) Math.round(baselinePixels * factor * GLOBAL_HALF_FACTOR));
    }

    static int importedDimension(int activeDimension, boolean defeated) {
        if (activeDimension < 1) throw new IllegalArgumentException("Active dimension must be positive.");
        return defeated ? Math.max(1, (int) Math.round(activeDimension * DEFEATED_FACTOR)) : activeDimension;
    }

    static int v0210Dimension(int baselinePixels, double mapScale, double referenceMapScale) {
        if (baselinePixels < 1) throw new IllegalArgumentException("Baseline dimension must be positive.");
        if (!Double.isFinite(mapScale) || mapScale <= 0
                || !Double.isFinite(referenceMapScale) || referenceMapScale <= 0) {
            return baselinePixels;
        }
        double factor = Math.sqrt(mapScale / referenceMapScale);
        factor = Math.max(MIN_FACTOR, Math.min(1.0, factor));
        return Math.max(1, (int) Math.round(baselinePixels * factor));
    }
}
