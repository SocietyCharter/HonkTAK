package com.societycharter.honktak;

/** Pure lifecycle guard used to make listener restoration idempotent. */
public final class PlacementSession {
    private boolean active;
    private boolean listenersPushed;
    public void begin() { active = true; listenersPushed = true; }
    public boolean isActive() { return active; }
    public boolean shouldRestoreListeners() { boolean result = listenersPushed; listenersPushed = false; active = false; return result; }
}
