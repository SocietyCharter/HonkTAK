package com.societycharter.honktak;

/** Compile-time boundary: the plugin core exposes no CoT/network/mission-package output API. */
public final class LocalOnlyBoundary {
    public static final boolean COT_TRANSMISSION_ALLOWED = false;
    public static final boolean MISSION_PACKAGE_WRITES_ALLOWED = false;
    public static final boolean UAS_CONTROLS_ALLOWED = false;
    private LocalOnlyBoundary() { }
}
