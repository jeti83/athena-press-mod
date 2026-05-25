package com.hypixel.hytale.event;

import com.hypixel.hytale.entity.PlayerRef;

/** Stub – wird durch das echte Hytale PlayerChat-Event ersetzt sobald die API verfügbar ist. */
public class PlayerChatEvent implements IBaseEvent<Void> {
    public PlayerRef getPlayerRef() { return null; }
    public String getMessage() { return ""; }
    public void setCancelled(boolean cancelled) {}
}
