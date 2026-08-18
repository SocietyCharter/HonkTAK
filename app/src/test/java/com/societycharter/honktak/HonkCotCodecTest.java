package com.societycharter.honktak;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.UUID;

public class HonkCotCodecTest {
    private CameraObservation sample(long now) { return new CameraObservation("honktak-" + UUID.randomUUID(), 36.06, -95.79,
        CameraObservation.CameraClass.PTZ, 275, CameraObservation.Confidence.HIGH,
        CameraObservation.Status.ACTIVE, "  south & gate <view>  ", now, now + 60000); }
    @Test public void serializeAndReceiveParseRoundTrip() { long now = 1700000000000L; CameraObservation p = HonkCotCodec.parse(HonkCotCodec.serialize(sample(now)), now); assertEquals(275, p.azimuth.intValue()); assertEquals("south & gate <view>", p.notes); }
    @Test(expected=IllegalArgumentException.class) public void malformedInputRejected() { HonkCotCodec.parse("<!DOCTYPE x [<!ENTITY e SYSTEM 'file:///etc/passwd'>]><event>&e;</event>", 1); }
    @Test(expected=IllegalArgumentException.class) public void oversizedInputRejected() { HonkCotCodec.parse(new String(new char[HonkCotCodec.MAX_XML_BYTES + 1]).replace('\0', 'x'), 1); }
    @Test(expected=IllegalArgumentException.class) public void staleInputRejected() { long now = 1700000000000L; HonkCotCodec.parse(HonkCotCodec.serialize(sample(now)), now + 60001); }
    @Test(expected=IllegalArgumentException.class) public void outOfRangeAzimuthRejected() { new CameraObservation("honktak-" + UUID.randomUUID(), 1, 1, CameraObservation.CameraClass.FIXED, 360, CameraObservation.Confidence.LOW, CameraObservation.Status.UNKNOWN, "", 1, 2); }
    @Test public void explicitShareGateIsOneShot() { ShareGate g = new ShareGate(); assertFalse(g.consumeForSend()); g.armFromVisibleUserAction(); assertTrue(g.consumeForSend()); assertFalse(g.consumeForSend()); }
    @Test public void localOnlyConstructionDoesNotArmShare() { ShareGate g = new ShareGate(); sample(1000); assertFalse(g.isArmed()); }
}
