package com.mengcraft.permission.manager;

import com.avaje.ebean.EbeanServer;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.mengcraft.permission.Main;
import com.mengcraft.permission.entity.Permission;
import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.io.ByteStreams.newDataInput;
import static com.google.common.io.ByteStreams.newDataOutput;
import static com.mengcraft.permission.lib.Util.cutHead;
import static com.mengcraft.permission.lib.Util.isWithdraw;
import static com.mengcraft.permission.lib.Util.isZone;
import static com.mengcraft.permission.lib.Util.now;
import static java.util.Arrays.asList;

/**
 * Created on 16-4-8.
 */
public class Fetcher implements PluginMessageListener {

    private final Map<String, Attachment> fetched = new HashMap<>();
    private final Main main;
    private final EbeanServer db;

    public final static String CHANNEL = "BungeeCord";
    public final static String CHANNEL_SUB = "Permission";

    public Fetcher(Main main, EbeanServer db) {
        this.main = main;
        this.db = db;
    }

    public void add(String name, String value) {
        add(name, value, false);
    }

    private void add(String name, String value, boolean b) {
        if (isZone(value)) {
            addZone(name, cutHead(value, 1), b);
        } else {
            addPerm(name, value, b);
        }
    }

    private void addPerm(String name, String value, boolean b) {
        if (isZone(name)) {
            List<String> list = fetchZoned(cutHead(name, 1));
            list.forEach(user -> {
                addPermToUser(user, value);
            });
            sendMessage(name, value, 0, b);
        } else if (fetched.containsKey(name)) {
            addPermToUser(name, value);
        } else {
            sendMessage(name, value, 0, b);
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
    private void addZone(String name, String value, boolean b) {
        if (isZone(name)) {
            List<String> list = fetchZoned(cutHead(name, 1));
            main.execute(() -> {
                Map<String, Integer> map = fetchZone(value);
                map.put(value, 2);
                main.execute(() -> ensureAdd(list, map), false);
            });
            sendMessage(name, '@' + value, 0, b);
        } else if (fetched.containsKey(name)) {
            main.execute(() -> {
                Map<String, Integer> map = fetchZone(value);
                map.put(value, 2);
                main.execute(() -> ensureAdd(name, map), false);
            });
        } else {
            sendMessage(name, '@' + value, 0, b);
        }
    }

    public void remove(String name, String value) {
        remove(name, value, false);
    }

    private void remove(String name, String value, boolean b) {
        if (isZone(value)) {
            removeZone(name, cutHead(value, 1), b);
        } else {
            removePerm(name, value, b);
        }
    }

    private void removePerm(String name, String value, boolean b) {
        if (isZone(name)) {
            List<String> list = fetchZoned(cutHead(name, 1));
            for (String user : list) {
                removePermFromUser(user, value);
            }
            sendMessage(name, value, 1, b);
        } else if (fetched.containsKey(name)) {
            removePermFromUser(name, value);
        } else {
            sendMessage(name, value, 1, b);
        }
    }

    private void removePermFromUser(String name, String value) {
        fetched.get(name).removePermission(isWithdraw(value) ? cutHead(value, 1) : value);
    }

    private void removeZone(String name, String value, boolean b) {
        if (isZone(name)) {
            List<String> list = fetchZoned(cutHead(name, 1));
            main.execute(() -> {
                Map<String, Integer> map = fetchZone(value);
                map.put(value, 2);
                main.execute(() -> ensureRemove(list, map), false);
            });
            sendMessage(name, '@' + value, 1, b);
        } else if (fetched.containsKey(name)) {
            main.execute(() -> {
                Map<String, Integer> map = fetchZone(value);
                map.put(value, 2);
                main.execute(() -> ensureRemove(name, map), false);
            });
        } else {
            sendMessage(name, '@' + value, 1, b);
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
     * @param value Zone name without '@' prefix.
     * @return Fetched permission map.
     */
    public Map<String, Integer> fetchZone(String value) {
        List<PermissionZone> founded = db.find(PermissionZone.class)
                .where()
                .eq("name", value)
                .orderBy("type desc")
                .findList();
        Map<String, Integer> fetched = new ConcurrentHashMap<>();
        founded.forEach(line -> {
            attach(fetched, line);
        });
        return fetched;
    }

    /**
     * @param zone Zone name without '@' prefix.
     * @return List contains all user has zoned.
     */
    public List<String> fetchZoned(String zone) {
        List<String> fetched = new ArrayList<>();
        this.fetched.forEach((user, attachment) -> {
            if (attachment.hasZone(zone)) {
                fetched.add(user);
            }
        });
        return fetched;
    }

    public Map<String, Attachment> fetched() {
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

    private void sendMessage(String name, String value, int i, boolean b) {
        if (!b && !main.isOffline()) {
            send(name, value, i);
        }
    }

    private void send(String name, String value, int b) {
        Iterator<Player> itr = asList(main.getServer().getOnlinePlayers()).iterator();
        if (itr.hasNext()) {
            System.out.println("Send channel. " + (b == 0 ? '+' : '-') + ':' + name + ':' + value);
            ByteArrayDataOutput buf = newDataOutput();
            buf.writeUTF("Forward");
            buf.writeUTF("ALL");
            buf.writeUTF(CHANNEL_SUB);

            ByteArrayDataOutput sub = newDataOutput();
            sub.write(b);
            sub.writeUTF(name);
            sub.writeUTF(value);

            byte[] payload = sub.toByteArray();

            buf.writeShort(payload.length);
            buf.write(payload);

            itr.next().sendPluginMessage(main, CHANNEL, buf.toByteArray());
        } else {
            main.getLogger().warning("No handled channel sender!");
        }
    }

    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] data) {
        ByteArrayDataInput buf = newDataInput(data);
        if (CHANNEL_SUB.equals(buf.readUTF())) {
            buf.readShort();
            if (buf.readByte() == 0) {
                add(buf.readUTF(), buf.readUTF(), true);
            } else {
                remove(buf.readUTF(), buf.readUTF(), true);
            }
        }
    }

}
