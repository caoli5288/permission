package com.mengcraft.permission.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Created on 16-6-12.
 */
public class PermissionFetchedEvent extends PlayerEvent {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    public PermissionFetchedEvent(Player p) {
        super(p);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public static PermissionFetchedEvent of(Player p) {
        return new PermissionFetchedEvent(p);
    }
}
