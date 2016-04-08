package com.mengcraft.permission;

import com.mengcraft.permission.entity.Permission;
import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
import com.mengcraft.simpleorm.EbeanHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachment;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mengcraft.permission.Util.cutHead;
import static com.mengcraft.permission.Util.isWithdraw;
import static com.mengcraft.permission.Util.now;

/**
 * Created on 15-10-20.
 */
class Executor implements Listener {

    private final Main main;
    private final EbeanHandler db;

    Executor(Main main, EbeanHandler db) {
        this.main = main;
        this.db = db;
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        main.execute(() -> {
            List<PermissionUser> fetched = db.find(PermissionUser.class)
                    .where()
                    .eq("name", player.getName())
                    .gt("outdated", new Timestamp(now()))
                    .orderBy("type desc")
                    .findList();
            Map<String, Boolean> attachMap = new HashMap<>();
            fetched.forEach(line -> {
                attach(attachMap, line);
            });
            PermissionAttachment attached = player.addAttachment(main);
            attachMap.forEach((key, value) -> {
                attached.setPermission(key, value);
            });
        });
    }

    private void attach(Map<String, Boolean> attachMap, Permission perm) {
        if (perm.isType()) {
            List<PermissionZone> fetched = db.find(PermissionZone.class)
                    .where()
                    .eq("name", cutHead(perm.getValue(), 1))
                    .orderBy("type desc")
                    .findList();
            fetched.forEach(line -> attach(attachMap, line));
        } else if (isWithdraw(perm.getValue())) {
            attachMap.put(cutHead(perm.getValue(), 1), false);
        } else {
            attachMap.put(perm.getValue(), true);
        }
    }

}
