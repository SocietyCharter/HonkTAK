package com.societycharter.honktak;

import static org.junit.Assert.*;
import java.util.Arrays;
import org.junit.Test;

public class HonkPolicyTest {
    @Test public void markerExpiresAtConfiguredBoundary() {
        assertFalse(HonkPolicy.isExpired(1000, 1999, 1000));
        assertTrue(HonkPolicy.isExpired(1000, 2000, 1000));
    }
    @Test public void threeNearbyActiveSightingsTrigger() {
        assertTrue(HonkPolicy.triggersFlockpocalypse(Arrays.asList(
            new HonkPolicy.Sighting(36.0600, -95.7900, 1000),
            new HonkPolicy.Sighting(36.0605, -95.7900, 1100),
            new HonkPolicy.Sighting(36.0610, -95.7900, 1200)), 1500, 5000));
    }
    @Test public void expiredOrDistantSightingsDoNotTrigger() {
        assertFalse(HonkPolicy.triggersFlockpocalypse(Arrays.asList(
            new HonkPolicy.Sighting(36.0600, -95.7900, 0),
            new HonkPolicy.Sighting(36.0605, -95.7900, 1100),
            new HonkPolicy.Sighting(37.0000, -95.7900, 1200)), 1500, 1000));
    }
    @Test public void audioIsDisabledByDefault() { assertFalse(HonkPolicy.DEFAULT_AUDIO_ENABLED); }
    @Test public void localOnlyBoundaryIsHardFalse() {
        assertFalse(LocalOnlyBoundary.COT_TRANSMISSION_ALLOWED);
        assertFalse(LocalOnlyBoundary.MISSION_PACKAGE_WRITES_ALLOWED);
        assertFalse(LocalOnlyBoundary.UAS_CONTROLS_ALLOWED);
    }
}
