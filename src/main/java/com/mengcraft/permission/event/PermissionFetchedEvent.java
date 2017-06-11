package com.mengcraft.permission.event;

import lombok.val;
import org.bukkit.Bukkit;
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

    public static PermissionFetchedEvent call(Player p) {
        val event = new PermissionFetchedEvent(p);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
}
