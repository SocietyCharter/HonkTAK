package com.societycharter.honktak;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class GooseIconScaleTest {
    @Test public void retainsBaselineAtReferenceAndCloserScales() {
        assertEquals(32, GooseIconScale.dimension(64, 0.00001, 0.00001));
        assertEquals(32, GooseIconScale.dimension(64, 0.00002, 0.00001));
    }

    @Test public void progressivelyShrinksAsMapZoomsOut() {
        int near = GooseIconScale.dimension(64, 0.000005, 0.00001);
        int far = GooseIconScale.dimension(64, 0.0000025, 0.00001);
        assertTrue(near < 32);
        assertTrue(far < near);
    }

    @Test public void clampsToReadableMinimum() {
        assertEquals(11, GooseIconScale.dimension(64, 0.000000001, 0.00001));
    }

    @Test public void invalidScaleSafelyRetainsBaseline() {
        assertEquals(32, GooseIconScale.dimension(64, Double.NaN, 0.00001));
        assertEquals(32, GooseIconScale.dimension(64, 0.00001, 0));
    }

    @Test public void everyBaseGooseDimensionIsHalfTheV0210RenderedInput() {
        int v0210 = GooseIconScale.v0210Dimension(64, 0.00001, 0.00001);
        int v0211 = GooseIconScale.dimension(64, 0.00001, 0.00001);
        assertEquals(64, v0210);
        assertEquals(32, v0211);
        double ratio = v0211 / (double) v0210;
        assertTrue(ratio >= 0.48 && ratio <= 0.52);
    }

    @Test public void importedPathsUseHalfSizedBaseAndPreserveDefeatedRelativeStyle() {
        int activeV0211 = GooseIconScale.dimension(64, 0.00001, 0.00001);
        assertEquals(32, GooseIconScale.importedDimension(activeV0211, false));
        assertEquals(26, GooseIconScale.importedDimension(activeV0211, true));
        double defeatedToActive = GooseIconScale.importedDimension(activeV0211, true)
                / (double) GooseIconScale.importedDimension(activeV0211, false);
        assertTrue(defeatedToActive >= 0.80 && defeatedToActive <= 0.84);
    }
}
