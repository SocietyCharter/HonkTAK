package com.societycharter.honktak;

import org.junit.Test;

public final class BitmapIconEncoderTest {
    @Test(expected = IllegalArgumentException.class)
    public void nullBitmapIsRejectedBeforeCompression() {
        BitmapIconEncoder.png(null);
    }
}
