package com.mengcraft.permission;

import com.avaje.ebean.EbeanServer;
import com.mengcraft.permission.entity.Permission;
import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.mengcraft.permission.Util.cutHead;
import static com.mengcraft.permission.Util.isWithdraw;
import static com.mengcraft.permission.Util.now;

/**
 * Created on 16-4-8.
 */
class Fetcher {

    private final Map<String, PermissionAttachment> cached = new ConcurrentHashMap<>();
    private final Main main;
    private final EbeanServer db;

    public Fetcher(Main main) {
        this.main = main;
        this.db = main.getDatabase();
    }

    void fetch(Player player) {
        main.execute(() -> {
            List<PermissionUser> fetched = db.find(PermissionUser.class)
                    .where()
                    .eq("name", player.getName())
                    .gt("outdated", new Timestamp(now()))
                    .orderBy("type desc")
                    .findList();
            Map<String, Boolean> attachMap = new ConcurrentHashMap<>();
            fetched.forEach(line -> {
                apply(attachMap, line);
            });
            main.execute(() -> {
                if (cached.containsKey(player.getName())) {
                    cached.remove(player.getName()).remove();
                }
                PermissionAttachment attached = player.addAttachment(main);
                attachMap.forEach((key, value) -> {
                    attached.setPermission(key, value);
                });
                cached.put(player.getName(), attached);
            }, false);
        });
    }

    void apply(Map<String, Boolean> attachMap, Permission perm) {
        if (perm.isType()) {
            List<PermissionZone> fetched = db.find(PermissionZone.class)
                    .where()
                    .eq("name", cutHead(perm.getValue(), 1))
                    .orderBy("type desc")
                    .findList();
            fetched.forEach(line -> apply(attachMap, line));
        } else if (isWithdraw(perm.getValue())) {
            attachMap.put(cutHead(perm.getValue(), 1), false);
        } else {
            attachMap.put(perm.getValue(), true);
        }
    }

}
