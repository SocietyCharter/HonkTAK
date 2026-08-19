package com.societycharter.honktak;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.UUID;

public class HonkCotCodecTest {
    private CameraObservation sample(long now) { return new CameraObservation("honktak-" + UUID.randomUUID(), 36.06, -95.79,
        CameraObservation.CameraClass.PTZ, 275, CameraObservation.Confidence.HIGH,
        CameraObservation.Status.ACTIVE, "  south & gate <view>  ", now, now + 60000); }
    @Test public void serializeAndReceiveParseRoundTrip() { long now = 1700000000000L; CameraObservation p = HonkCotCodec.parse(HonkCotCodec.serialize(sample(now)), now); assertEquals(275, p.azimuth.intValue()); assertEquals("south & gate <view>", p.notes); }
    @Test public void wedgeFieldsRoundTrip() { long now = 1700000000000L; CameraObservation o = new CameraObservation("honktak-" + UUID.randomUUID(), 36, -95, CameraObservation.CameraClass.FIXED, 42, 321.5, 45, CameraObservation.Confidence.MEDIUM, CameraObservation.Status.ACTIVE, "", now, now + 60000); CameraObservation p = HonkCotCodec.parse(HonkCotCodec.serialize(o), now); assertEquals(321.5, p.rangeMeters, 0.001); assertEquals(45, p.fovDegrees, 0.001); }
    @Test public void olderEventsUseSafeWedgeDefaults() { long now = 1700000000000L; String xml = HonkCotCodec.serialize(sample(now)).replaceAll(" range_m=\"[^\"]+\"", "").replaceAll(" fov_deg=\"[^\"]+\"", ""); CameraObservation p = HonkCotCodec.parse(xml, now); assertEquals(PlacementMath.DEFAULT_RANGE_METERS, p.rangeMeters, 0.0); assertEquals(PlacementMath.DEFAULT_FOV_DEGREES, p.fovDegrees, 0.0); }
    @Test(expected=IllegalArgumentException.class) public void malformedInputRejected() { HonkCotCodec.parse("<!DOCTYPE x [<!ENTITY e SYSTEM 'file:///etc/passwd'>]><event>&e;</event>", 1); }
    @Test(expected=IllegalArgumentException.class) public void oversizedInputRejected() { HonkCotCodec.parse(new String(new char[HonkCotCodec.MAX_XML_BYTES + 1]).replace('\0', 'x'), 1); }
    @Test(expected=IllegalArgumentException.class) public void staleInputRejected() { long now = 1700000000000L; HonkCotCodec.parse(HonkCotCodec.serialize(sample(now)), now + 60001); }
    @Test(expected=IllegalArgumentException.class) public void outOfRangeAzimuthRejected() { new CameraObservation("honktak-" + UUID.randomUUID(), 1, 1, CameraObservation.CameraClass.FIXED, 360, CameraObservation.Confidence.LOW, CameraObservation.Status.UNKNOWN, "", 1, 2); }
    @Test public void explicitShareGateIsOneShot() { ShareGate g = new ShareGate(); assertFalse(g.consumeForSend()); g.armFromVisibleUserAction(); assertTrue(g.consumeForSend()); assertFalse(g.consumeForSend()); }
    @Test public void localOnlyConstructionDoesNotArmShare() { ShareGate g = new ShareGate(); sample(1000); assertFalse(g.isArmed()); }
}
