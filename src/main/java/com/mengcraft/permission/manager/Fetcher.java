package com.mengcraft.permission.manager;

import com.avaje.ebean.EbeanServer;
import com.mengcraft.permission.Main;
import com.mengcraft.permission.entity.Permission;
import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.mengcraft.permission.lib.Util.cutHead;
import static com.mengcraft.permission.lib.Util.isWithdraw;
import static com.mengcraft.permission.lib.Util.isZone;
import static com.mengcraft.permission.lib.Util.now;

/**
 * Created on 16-4-8.
 */
public class Fetcher {

    private final Map<String, Attachment> cached = new HashMap<>();
    private final Main main;
    private final EbeanServer db;

    public Fetcher(Main main, EbeanServer db) {
        this.main = main;
        this.db = db;
    }

    public void add(String name, String value) {
        if (isZone(value)) {
            addZone(name, cutHead(value, 1));
        } else {
            addPerm(name, value);
        }
    }

    public void addPerm(String name, String perm) {
        if (isZone(name)) {
            List<String> list = fetchZoned(cutHead(name, 1));
            list.forEach(line -> {
                addPerm(line, perm);
            });
        } else if (cached.containsKey(name)) {
            if (isWithdraw(perm)) {
                cached.get(name).addPermission(cutHead(perm, 1), false);
            } else {
                cached.get(name).addPermission(perm, true);
            }
        }
    }

    /**
     * @param what This maybe a user or zone.
     * @param name The zone name without '@'.
     */
    public void addZone(String what, String name) {
        if (isZone(what)) {
            List<String> list = fetchZoned(cutHead(what, 1));
            main.execute(() -> {
                List<PermissionZone> fetched = db.find(PermissionZone.class)
                        .where()
                        .eq("name", name)
                        .orderBy("type desc")
                        .findList();
                Map map = new ConcurrentHashMap();
                fetched.forEach(line ->
                        attach(map, line)
                );
                main.execute(() -> ensure(list, map), false);
            });
        } else if (cached.containsKey(what)) {
            main.execute(() -> {
                List<PermissionZone> fetched = db.find(PermissionZone.class)
                        .where()
                        .eq("name", name)
                        .orderBy("type desc")
                        .findList();
                Map map = new ConcurrentHashMap();
                map.put(name, 2);
                fetched.forEach(line -> {
                    attach(map, line);
                });
                main.execute(() -> ensure(what, map), false);
            });
        }
    }

    public void drop(Player player) {
        cached.remove(player.getName());
    }

    public void fetch(Player player) {
        main.execute(() -> {
            List<PermissionUser> fetched = db.find(PermissionUser.class)
                    .where()
                    .eq("name", player.getName())
                    .gt("outdated", new Timestamp(now()))
                    .orderBy("type desc")
                    .findList();
            Map<String, Integer> attachMap = new ConcurrentHashMap<>();
            fetched.forEach(line -> {
                attach(attachMap, line);
            });
            main.execute(() -> ensurePurged(player, attachMap), false);
        });
    }

    /**
     * @param zone Zone name without '@' prefix.
     * @return List contains all user has zoned.
     */
    public List<String> fetchZoned(String zone) {
        List<String> fetched = new ArrayList<>();
        cached.forEach((user, attachment) -> {
            if (attachment.hasZone(zone)) {
                fetched.add(user);
            }
        });
        return fetched;
    }

    private void ensure(List<String> list, Map<String, Integer> attachMap) {
        for (String line : list) {
            ensure(line, attachMap);
        }
    }

    private void ensure(String name, Map<String, Integer> attachMap) {
        ensure(cached.get(name), attachMap);
    }

    private Attachment ensure(Attachment attachment, Map<String, Integer> attachMap) {
        attachMap.forEach((key, value) -> {
            if (value.intValue() == 0) {
                attachment.addPermission(key, false);
            } else if (value.intValue() == 1) {
                attachment.addPermission(key, true);
            } else {
                attachment.addZone(key);
            }
        });
        return attachment;
    }

    private void ensurePurged(Player player, Map<String, Integer> attachMap) {
        String name = player.getName();
        if (cached.containsKey(name)) {
            cached.remove(name).removePermission();
        }
        cached.put(name, ensure(new Attachment(player.addAttachment(main)), attachMap));
    }

    private void attach(Map<String, Integer> attachMap, Permission perm) {
        if (perm.isType()) {
            String name = cutHead(perm.getValue(), 1);
            List<PermissionZone> fetched = db.find(PermissionZone.class)
                    .where()
                    .eq("name", name)
                    .orderBy("type desc")
                    .findList();
            fetched.forEach(line -> {
                attach(attachMap, line);
            });
            attachMap.put(name, 2);
        } else if (isWithdraw(perm.getValue())) {
            attachMap.put(cutHead(perm.getValue(), 1), 0);
        } else {
            attachMap.put(perm.getValue(), 1);
        }
    }

}
