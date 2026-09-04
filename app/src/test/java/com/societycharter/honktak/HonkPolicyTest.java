package com.societycharter.honktak;

import static org.junit.Assert.*;
import java.util.Arrays;
import org.junit.Test;

public class HonkPolicyTest {
    @Test public void markerExpiresAtConfiguredBoundary() {
        assertFalse(HonkPolicy.isExpired(1000, 1999, 1000));
        assertTrue(HonkPolicy.isExpired(1000, 2000, 1000));
    }
    @Test public void permanentObservationNeverExpires() {
        CameraObservation observation = new CameraObservation("honktak-00000000-0000-0000-0000-000000000001",
                36, -95, CameraObservation.CameraClass.FIXED, 0,
                CameraObservation.Confidence.MEDIUM, CameraObservation.Status.ACTIVE, "",
                1000, CameraObservation.PERMANENT);
        assertTrue(observation.isPermanent());
        assertFalse(observation.isStale(Long.MAX_VALUE));
    }
    @Test public void temporaryObservationExpiresOnlyAtItsExplicitBoundary() {
        CameraObservation observation = new CameraObservation("honktak-00000000-0000-0000-0000-000000000002",
                36, -95, CameraObservation.CameraClass.FIXED, 0,
                CameraObservation.Confidence.MEDIUM, CameraObservation.Status.ACTIVE, "",
                1000, 61000);
        assertFalse(observation.isPermanent());
        assertFalse(observation.isStale(60999));
        assertTrue(observation.isStale(61000));
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
        assertFalse(LocalOnlyBoundary.AUTOMATIC_COT_TRANSMISSION_ALLOWED);
        assertTrue(LocalOnlyBoundary.EXPLICIT_USER_COT_SHARE_ALLOWED);
        assertFalse(LocalOnlyBoundary.MISSION_PACKAGE_WRITES_ALLOWED);
        assertFalse(LocalOnlyBoundary.UAS_CONTROLS_ALLOWED);
    }
}
