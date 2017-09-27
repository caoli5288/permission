package com.mengcraft.permission;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
import com.mengcraft.permission.event.PermissionFetchedEvent;
import com.mengcraft.simpleorm.EbeanHandler;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.io.ByteStreams.newDataInput;
import static com.google.common.io.ByteStreams.newDataOutput;

/**
 * Created on 16-4-8.
 */
public enum Fetcher implements PluginMessageListener, Runnable {

    INSTANCE;

    private final Map<String, Attachment> fetched = new HashMap<>();
    private Main main;
    private EbeanHandler db;

    public final static String CHANNEL = "BungeeCord";
    public final static String CHANNEL_SUB = "Permission";

    void init(Main main, EbeanHandler db) {
        this.main = main;
        this.db = db;
    }

    public void add(String name, String value, long outdated) {
        add(name, value, outdated, false);
    }

    private void add(String name, String value, long outdated, boolean b) {
        if ($.isZone(name)) {
            addToZone(name, value);
        } else {
            val attachment = fetched.get(name);
            if (!$.nil(attachment)) {
                val attach = PermissionValue.build(value, outdated);
                if ($.isZone(value)) {
                    fetchSub(new HashMap<>(), attach);
                }
                attachment.handle(attach);
            }
        }
        sendMessage(name, value, outdated, true, b);
    }

    private void addToZone(String zone, String add) {
        val attach = PermissionValue.build(add, -1);
        if ($.isZone(add)) {
            fetchSub(new HashMap<>(), attach);
        }
        $.walk(fetched, (who, attachment) -> {
            val look = attachment.look(zone, true);
            if (!$.nil(look)) {
                val mirror = attach.mirror();
                look.getRight().getSublist().add(mirror);
                mirror.handle(attachment.getAttachment());
            }
        });
    }

    public void remove(String name, String value) {
        remove(name, value, false);
    }

    private void remove(String name, String value, boolean b) {
        if ($.isZone(name)) {
            remove4Zone(name, value);
        } else {
            val attachment = fetched.get(name);
            if (!$.nil(attachment)) {
                attachment.removePermission(value);
            }
        }
        sendMessage(name, value, -1, false, b);
    }

    private void remove4Zone(String zone, String value) {
        $.walk(fetched, (who, attachment) -> {
            val look = attachment.look(zone, true);
            if (!$.nil(look)) {
                look.getLeft().removeSub(value).cancel(attachment.getAttachment());
            }
        });
    }

    public void fetch(Player p) {
        Main.runAsync(() -> {
            val list = db.find(PermissionUser.class)
                    .where("name = :name and outdated > now()")
                    .setParameter("name", p.getName())
                    .orderBy("type desc")
                    .findList();

            val collect = $.map(list, PermissionValue::build);

            val mapping = new HashMap<String, List<PermissionZone>>();
            $.walk(collect, attach -> $.isZone(attach.getValue()), v -> fetchSub(mapping, v)
            );

            main.run(() -> handle(p, collect));
        });
    }

    public void fetchSub(Map<String, List<PermissionZone>> mapping, PermissionValue value) {
        $.thr(!$.isZone(value.getValue()), "Illegal argument");

        val list = mapping.computeIfAbsent($.cutHead(value.getValue()), name -> db.find(PermissionZone.class)
                .where("name = :name")
                .setParameter("name", name)
                .orderBy("type desc")
                .findList()
        );

        val collect = $.map(list, PermissionValue::build);
        value.getSublist().addAll(collect);

        $.walk(collect, i -> $.isZone(i.getValue()), v -> fetchSub(mapping, v));
    }

    private void handle(Player p, List<PermissionValue> list) {
        val old = fetched.remove(p.getName());
        if (!$.nil(old)) old.removePermission();
        val attachment = new Attachment(p.addAttachment(main));
        attachment.handle(list);
        fetched.put(p.getName(), attachment);
        PermissionFetchedEvent.call(p);
    }

    private void sendMessage(String name, String value, long outdated, boolean add, boolean b) {
        if (!b && !main.isOffline()) {
            send(name, value, outdated, add);
        }
    }

    private void send(String name, String value, long outdated, boolean add) {
        val itr = main.getServer().getOnlinePlayers().iterator();
        if (itr.hasNext()) {
            Main.log("Send channel. " + (add ? '+' : '-') + ':' + name + ':' + value);
            ByteArrayDataOutput buf = newDataOutput();
            buf.writeUTF("Forward");
            buf.writeUTF("ALL");
            buf.writeUTF(CHANNEL_SUB);

            ByteArrayDataOutput sub = newDataOutput();
            sub.writeBoolean(add);
            sub.writeUTF(name);
            sub.writeUTF(value);
            sub.writeLong(outdated);

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
            if (buf.readBoolean()) {
                add(buf.readUTF(), buf.readUTF(), buf.readLong(), true);
            } else {
                remove(buf.readUTF(), buf.readUTF(), true);
            }
        }
    }

    @Override
    public void run() {
        $.walk(fetched, (k, v) -> v.cleanup());
    }

    public Map<String, Attachment> getFetched() {
        return fetched;
    }

    public Attachment getFetched(String name) {
        return fetched.get(name);
    }

}
