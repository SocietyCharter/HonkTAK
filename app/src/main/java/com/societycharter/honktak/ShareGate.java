package com.societycharter.honktak;

/** One-shot explicit consent gate. Merely saving or constructing an observation never sends it. */
public final class ShareGate {
    private boolean armed;
    public void armFromVisibleUserAction() { armed = true; }
    public boolean consumeForSend() { boolean allowed = armed; armed = false; return allowed; }
    public boolean isArmed() { return armed; }
}
