package com.mengcraft.permission;

import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.simpleorm.EbeanHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Timestamp;
import java.util.List;

import static java.lang.System.currentTimeMillis;

/**
 * Created on 15-10-20.
 */
public class Executor implements Listener {

    private final Main main;
    private final EbeanHandler db;

    public Executor(Main main, EbeanHandler db) {
        this.main = main;
        this.db   = db;
    }

    @EventHandler
    public void handle(final PlayerJoinEvent event) {
        main.asyncTask(query(event.getPlayer()));
    }

    private Runnable query(Player player) {
        return () -> {
            List<PermissionUser> permissionList = db.find(PermissionUser.class)
                    .where()
                    .eq("name", player.getName())
                    .gt("outdated", new Timestamp(currentTimeMillis()))
                    .findList();
            for (PermissionUser line : permissionList) {
                player.addAttachment(main, line.getValue(), true);
            }
            permissionList.clear();
        };
    }

}
