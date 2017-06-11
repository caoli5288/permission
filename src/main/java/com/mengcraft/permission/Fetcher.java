package com.mengcraft.permission;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.mengcraft.permission.entity.PermissionMXBean;
import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
import com.mengcraft.permission.event.PermissionFetchedEvent;
import com.mengcraft.simpleorm.EbeanHandler;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.io.ByteStreams.newDataInput;
import static com.google.common.io.ByteStreams.newDataOutput;
import static com.mengcraft.permission.$.cutHead;
import static com.mengcraft.permission.$.isWithdraw;
import static com.mengcraft.permission.$.isZone;
import static com.mengcraft.permission.$.now;

/**
 * Created on 16-4-8.
 */
public class Fetcher implements PluginMessageListener {

    private final Map<String, Attachment> fetched = new HashMap<>();
    private final Main main;
    private final EbeanHandler db;

    public final static String CHANNEL = "BungeeCord";
    public final static String CHANNEL_SUB = "Permission";

    public Fetcher(Main main, EbeanHandler db) {
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
            List<String> list = lookZoned(cutHead(name, 1));
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
            List<String> list = lookZoned(cutHead(name, 1));
            main.execute(() -> {
                Map<String, Integer> map = fetchZone(value);
                map.put(value, 2);
                main.run(() -> add2fetched(list, map));
            });
            sendMessage(name, '@' + value, 0, b);
        } else if (fetched.containsKey(name)) {
            main.execute(() -> {
                Map<String, Integer> map = fetchZone(value);
                map.put(value, 2);
                main.run(() -> add2fetched(name, map));
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
            List<String> list = lookZoned(cutHead(name, 1));
            for (String user : list) {
                removePerm4User(user, value);
            }
            sendMessage(name, value, 1, b);
        } else if (fetched.containsKey(name)) {
            removePerm4User(name, value);
        } else {
            sendMessage(name, value, 1, b);
        }
    }

    private void removePerm4User(String name, String value) {
        fetched.get(name).removePermission(isWithdraw(value) ? cutHead(value, 1) : value);
    }

    private void removeZone(String name, String value, boolean b) {
        if (isZone(name)) {
            List<String> list = lookZoned(cutHead(name, 1));
            main.execute(() -> {
                Map<String, Integer> map = fetchZone(value);
                map.put(value, 2);
                main.run(() -> remove4fetched(list, map));
            });
            sendMessage(name, '@' + value, 1, b);
        } else if (fetched.containsKey(name)) {
            main.execute(() -> {
                Map<String, Integer> map = fetchZone(value);
                map.put(value, 2);
                main.run(() -> remove4fetched(name, map));
            });
        } else {
            sendMessage(name, '@' + value, 1, b);
        }
    }

    private void attach(Map<String, Integer> attachMap, PermissionMXBean perm) {
        if ($.isZone(perm.getValue())) {
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

    private void process(Attach attach) {
        val list = db.find(PermissionZone.class)
                .where()
                .eq("name", $.cutHead(attach.getValue()))
                .orderBy("type desc")
                .findList();

        val collect = $.collect(list, Attach::build);
        attach.getSubList().addAll(collect);

        $.walk(collect, a -> $.isZone(a.getValue()), this::process);
    }

    public void fetch(Player p) {
        main.execute(() -> {
            val list = db.find(PermissionUser.class)
                    .where()
                    .eq("name", p.getName())
                    .gt("outdated", new Timestamp(now()))
                    .orderBy("type desc")
                    .findList();

            val collect = $.collect(list, Attach::build);
            $.walk(collect, attach -> $.isZone(attach.getValue()), this::process);

            main.run(() -> fetchWith(p, collect));
        });
    }

    private void fetchWith(Player p, List<Attach> list) {
        val old = fetched.remove(p.getName());
        if (!$.nil(old)) old.removePermission();
        val attachment = new Attachment(p.addAttachment(main));
        attachment.handle(list);
        fetched.put(p.getName(), attachment);
        PermissionFetchedEvent.call(p);
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
    private List<String> lookZoned(String zone) {
        List<String> out = new ArrayList<>();
        fetched.forEach((who, attachment) -> {
            if (attachment.hasZone(zone)) {
                out.add(who);
            }
        });
        return out;
    }

    private void add2fetched(List<String> list, Map<String, Integer> attachMap) {
        for (String line : list) {
            add2fetched(line, attachMap);
        }
    }

    private void add2fetched(String name, Map<String, Integer> attachMap) {
        add2fetched(fetched.get(name), attachMap);
    }

    private void add2fetched(Attachment attachment, Map<String, Integer> attachMap) {
        attachMap.forEach((key, value) -> {
            if (value.intValue() == 0) {
                attachment.addPermission(key, false);
            } else if (value.intValue() == 1) {
                attachment.addPermission(key, true);
            } else {
//                attachment.addZone(key);
            }
        });
    }

    private void remove4fetched(List<String> list, Map<String, Integer> attachMap) {
        for (String line : list) {
            remove4fetched(line, attachMap);
        }
    }

    private void remove4fetched(String name, Map<String, Integer> attachMap) {
        remove4fetched(fetched.get(name), attachMap);
    }

    private Attachment remove4fetched(Attachment attachment, Map<String, Integer> attachMap) {
        attachMap.forEach((key, value) -> {
            if (value.intValue() == 2) {
                attachment.removeZone(key);
            } else {
                attachment.removePermission(key);
            }
        });
        return attachment;
    }

    private void sendMessage(String name, String value, int i, boolean b) {
        if (!b && !main.isOffline()) {
            send(name, value, i);
        }
    }

    private void send(String name, String value, int b) {
        val itr = main.getServer().getOnlinePlayers().iterator();
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

    public Map<String, Attachment> getFetched() {
        return fetched;
    }
}
