package com.societycharter.honktak;

import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;

/** Enforces a non-null boundary before any Bitmap compression call. */
final class BitmapIconEncoder {
    private BitmapIconEncoder() { }

    static byte[] png(Bitmap bitmap) {
        if (bitmap == null) {
            throw new IllegalArgumentException("Icon bitmap must not be null.");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
            throw new IllegalStateException("Goose icon encoding failed.");
        }
        return out.toByteArray();
    }
}
