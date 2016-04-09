package com.mengcraft.permission;

import com.mengcraft.permission.manager.Fetcher;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created on 15-10-20.
 */
class Executor implements Listener {

    private final Main main;
    private final Fetcher fetcher;

    Executor(Main main, Fetcher fetcher) {
        this.main = main;
        this.fetcher = fetcher;
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        fetcher.fetch(event.getPlayer());
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        fetcher.drop(event.getPlayer());
    }

}
