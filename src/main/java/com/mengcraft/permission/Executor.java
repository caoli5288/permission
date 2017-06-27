package com.mengcraft.permission;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created on 15-10-20.
 */
class Executor implements Listener {

    Executor() {
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        Fetcher.INSTANCE.fetch(event.getPlayer());
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        Fetcher.INSTANCE.getFetched().remove(event.getPlayer().getName());
    }

}
