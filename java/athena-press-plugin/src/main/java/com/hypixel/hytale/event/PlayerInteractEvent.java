package com.hypixel.hytale.event;

import com.hypixel.hytale.entity.PlayerRef;
import com.hypixel.hytale.item.ItemStack;

/** Stub – wird durch das echte Hytale PlayerInteract-Event ersetzt sobald die API verfügbar ist. */
public class PlayerInteractEvent implements IBaseEvent<Void> {
    public PlayerRef getPlayerRef() { return null; }
    public ItemStack getItemInHand() { return null; }
}
