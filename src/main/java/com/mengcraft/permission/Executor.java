package com.mengcraft.permission;

import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
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
        this.db = db;
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
            for (PermissionUser user : permissionList) {
                apply(player, user);
            }
            permissionList.clear();
        };
    }

    private void apply(Player player, PermissionUser user) {
        if (user.isType()) {
            List<PermissionZone> list = db.find(PermissionZone.class)
                    .where()
                    .eq("name", user.getValue())
                    .findList();
            for (PermissionZone line : list) {
                apply(player, line);
            }
        } else {
            player.addAttachment(main, user.getValue(), true);
        }
    }

    private void apply(Player player, PermissionZone zone) {
        if (zone.isType()) {
            List<PermissionZone> list = db.find(PermissionZone.class)
                    .where()
                    .eq("name", zone.getValue())
                    .findList();
            for (PermissionZone line : list) {
                apply(player, line);
            }
        } else {
            player.addAttachment(main, zone.getValue(), true);
        }
    }

}
