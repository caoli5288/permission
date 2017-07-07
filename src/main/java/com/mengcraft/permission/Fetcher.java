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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.io.ByteStreams.newDataInput;
import static com.google.common.io.ByteStreams.newDataOutput;
import static com.mengcraft.permission.$.now;

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
                val attach = Attach.build(value, outdated);
                if ($.isZone(value)) {
                    fetchZone(attach);
                }
                attachment.handle(attach);
            }
        }
        sendMessage(name, value, outdated, true, b);
    }

    private void addToZone(String name, String value) {
        val attach = Attach.build(value, -1);
        if ($.isZone(value)) {
            fetchZone(attach);
        }
        $.walk(fetched, (k, v) -> {
            val look = v.look(name, true);
            if (!$.nil(look)) {
                look.getValue().getSublist().add(attach.mirror());
                look.getValue().handle(v.getAttachment());
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

    private void remove4Zone(String name, String value) {
        $.walk(fetched, (k, v) -> {
            val look = v.look(name, true);
            if (!$.nil(look)) {
                val sub = look.getValue().removeSub(value);
                if (!$.nil(sub)) {
                    sub.cancel(v.getAttachment());
                }
            }
        });
    }

    public void fetch(Player p) {
        main.execute(() -> {
            val list = db.find(PermissionUser.class)
                    .where()
                    .eq("name", p.getName())
                    .gt("outdated", new Timestamp(now()))
                    .orderBy("type desc")
                    .findList();

            val collect = $.map(list, Attach::build);
            $.walk(collect, attach -> $.isZone(attach.getValue()), this::fetchZone);

            main.run(() -> handle(p, collect));
        });
    }

    public void fetchZone(Attach attach) {
        val list = db.find(PermissionZone.class)
                .where()
                .eq("name", $.cutHead(attach.getValue()))
                .orderBy("type desc")
                .findList();

        val collect = $.map(list, Attach::build);
        attach.getSublist().addAll(collect);

        $.walk(collect, a -> $.isZone(a.getValue()), this::fetchZone);
    }

    private void handle(Player p, List<Attach> list) {
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
