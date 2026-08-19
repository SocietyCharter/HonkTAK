package com.societycharter.honktak;

import static org.junit.Assert.*;
import org.junit.Test;

public class PlacementMathTest {
    @Test public void bearingNormalizesAndPointsEast() {
        assertEquals(350.0, PlacementMath.normalizeBearing(-10), 0.0001);
        assertEquals(90.0, PlacementMath.bearing(0, 0, 0, 1), 0.01);
    }
    @Test public void rangeIsClampedToVisibleBounds() {
        assertEquals(10.0, PlacementMath.clampRange(1), 0.0);
        assertEquals(500.0, PlacementMath.clampRange(900), 0.0);
        assertEquals(50.0, PlacementMath.clampRange(Double.NaN), 0.0);
    }
}
