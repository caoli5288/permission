package com.mengcraft.permission;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created on 15-10-20.
 */
class Executor implements Listener {

    private final Fetcher fetcher;

    Executor(Fetcher fetcher) {
        this.fetcher = fetcher;
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        fetcher.fetch(event.getPlayer());
    }

}
