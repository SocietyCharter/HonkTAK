package com.societycharter.honktak;

import static org.junit.Assert.*;
import org.junit.Test;

public class PlacementSessionTest {
    @Test public void cancellationRestoresListenerStackExactlyOnce() {
        PlacementSession session = new PlacementSession();
        session.begin();
        assertTrue(session.isActive());
        assertTrue(session.shouldRestoreListeners());
        assertFalse(session.isActive());
        assertFalse(session.shouldRestoreListeners());
    }
}
