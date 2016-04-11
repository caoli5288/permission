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

    private final Map<String, Attachment> fetched = new HashMap<>();
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

    private void addPerm(String name, String value) {
        if (isZone(name)) {
            List<String> list = fetchZoned(cutHead(name, 1));
            list.forEach(user -> {
                addPermToUser(user, value);
            });
        } else if (fetched.containsKey(name)) {
            addPermToUser(name, value);
        }
    }

    private void addPermToUser(String name, String value) {
        if (isWithdraw(value)) {
            fetched.get(name).addPermission(cutHead(value, 1), false);
        } else {
            fetched.get(name).addPermission(value, true);
        }
    }

    /**
     * @param name  This maybe a user or zone.
     * @param value The zone name without '@'.
     */
    private void addZone(String name, String value) {
        if (isZone(name)) {
            List<String> list = fetchZoned(cutHead(name, 1));
            main.execute(() -> {
                List<PermissionZone> fetched = db.find(PermissionZone.class)
                        .where()
                        .eq("name", value)
                        .orderBy("type desc")
                        .findList();
                Map map = new ConcurrentHashMap();
                map.put(value, 2);
                fetched.forEach(line -> {
                    attach(map, line);
                });
                main.execute(() -> ensureAdd(list, map), false);
            });
        } else if (fetched.containsKey(name)) {
            main.execute(() -> {
                List<PermissionZone> fetched = db.find(PermissionZone.class)
                        .where()
                        .eq("name", value)
                        .orderBy("type desc")
                        .findList();
                Map map = new ConcurrentHashMap();
                map.put(value, 2);
                fetched.forEach(line -> {
                    attach(map, line);
                });
                main.execute(() -> ensureAdd(name, map), false);
            });
        }
    }

    public void remove(String name, String value) {
        if (isZone(value)) {
            removeZone(name, cutHead(value, 1));
        } else {
            removePerm(name, value);
        }
    }

    private void removePerm(String name, String value) {
        if (isZone(name)) {
            List<String> list = fetchZoned(cutHead(name, 1));
            for (String user : list) {
                removePermFromUser(user, value);
            }
        } else if (fetched.containsKey(name)) {
            removePermFromUser(name, value);
        }
    }

    private void removePermFromUser(String name, String value) {
        fetched.get(name).removePermission(isWithdraw(value) ? cutHead(value, 1) : value);
    }

    private void removeZone(String name, String value) {
        if (isZone(name)) {
            List<String> list = fetchZoned(cutHead(name, 1));
            main.execute(() -> {
                List<PermissionZone> fetched = db.find(PermissionZone.class)
                        .where()
                        .eq("name", value)
                        .orderBy("type desc")
                        .findList();
                Map map = new ConcurrentHashMap();
                map.put(value, 2);
                fetched.forEach(line -> {
                    attach(map, line);
                });
                main.execute(() -> ensureRemove(list, map), false);
            });
        } else if (fetched.containsKey(name)) {
            main.execute(() -> {
                List<PermissionZone> fetched = db.find(PermissionZone.class)
                        .where()
                        .eq("name", value)
                        .orderBy("type desc")
                        .findList();
                Map map = new ConcurrentHashMap();
                map.put(value, 2);
                fetched.forEach(line -> {
                    attach(map, line);
                });
                main.execute(() -> ensureRemove(name, map), false);
            });
        }
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
            main.execute(() -> fetchWith(player, attachMap), false);
        });
    }

    private void fetchWith(Player player, Map<String, Integer> attachMap) {
        String name = player.getName();
        if (fetched.containsKey(name)) {
            fetched.remove(name).removePermission();
        }
        fetched.put(name, ensureAdd(new Attachment(player.addAttachment(main)), attachMap));
    }

    /**
     * @param zone Zone name without '@' prefix.
     * @return List contains all user has zoned.
     */
    private List<String> fetchZoned(String zone) {
        List<String> fetched = new ArrayList<>();
        this.fetched.forEach((user, attachment) -> {
            if (attachment.hasZone(zone)) {
                fetched.add(user);
            }
        });
        return fetched;
    }

    private void ensureAdd(List<String> list, Map<String, Integer> attachMap) {
        for (String line : list) {
            ensureAdd(line, attachMap);
        }
    }

    private void ensureAdd(String name, Map<String, Integer> attachMap) {
        ensureAdd(fetched.get(name), attachMap);
    }

    private Attachment ensureAdd(Attachment attachment, Map<String, Integer> attachMap) {
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

    private void ensureRemove(List<String> list, Map<String, Integer> attachMap) {
        for (String line : list) {
            ensureRemove(line, attachMap);
        }
    }

    private void ensureRemove(String name, Map<String, Integer> attachMap) {
        ensureRemove(fetched.get(name), attachMap);
    }

    private Attachment ensureRemove(Attachment attachment, Map<String, Integer> attachMap) {
        attachMap.forEach((key, value) -> {
            if (value.intValue() == 2) {
                attachment.removeZone(key);
            } else {
                attachment.removePermission(key);
            }
        });
        return attachment;
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

    public Map<String, Attachment> fetched() {
        return fetched;
    }

}
