package com.societycharter.honktak;

/** Compile-time boundary: no automatic send; only a consumed explicit user share may dispatch CoT. */
public final class LocalOnlyBoundary {
    public static final boolean AUTOMATIC_COT_TRANSMISSION_ALLOWED = false;
    public static final boolean EXPLICIT_USER_COT_SHARE_ALLOWED = true;
    public static final boolean MISSION_PACKAGE_WRITES_ALLOWED = false;
    public static final boolean UAS_CONTROLS_ALLOWED = false;
    private LocalOnlyBoundary() { }
}
