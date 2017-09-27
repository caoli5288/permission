package com.mengcraft.permission;

import com.mengcraft.permission.entity.PermissionMXBean;
import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
import com.mengcraft.simpleorm.EbeanHandler;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.mengcraft.permission.$.cutHead;
import static com.mengcraft.permission.$.isZone;
import static com.mengcraft.permission.$.nil;
import static com.mengcraft.permission.$.now;

/**
 * Created on 15-10-20.
 */
public class MainCommand implements CommandExecutor, Permission {

    private final EbeanHandler db;
    private final Main main;
    private final Fetcher fetcher = Fetcher.INSTANCE;

    MainCommand(Main main, EbeanHandler db) {
        this.main = main;
        this.db = db;
    }

    @Override
    public boolean onCommand(CommandSender p, Command i, String j, String[] input) {
        Iterator<String> it = Arrays.asList(input).iterator();
        if (it.hasNext()) {
            return execute(p, it.next(), it);
        } else if (p instanceof Player) {
            sendInfo(p, p.getName());
            return true;
        }
        return false;
    }

    private boolean execute(CommandSender sender, String name, Iterator<String> it) {
        if (sender.hasPermission("permission.admin")) {
            if (it.hasNext()) {
                return execute(sender, name, it.next(), it);
            } else if (isZone(name)) {
                sendInfoZone(sender, name);
            } else {
                sendInfo(sender, name);
            }
            return true;
        }
        return false;
    }

    private void sendInfoZone(CommandSender sender, String name) {
        Main.runAsync(() -> {
            List<PermissionZone> fetched = db.find(PermissionZone.class)
                    .where()
                    .eq("name", cutHead(name, 1))
                    .orderBy("type desc")
                    .findList();
            sender.sendMessage(ChatColor.GOLD + ">>> Permission info call " + name);
            for (PermissionMXBean zone : fetched) {
                sender.sendMessage(ChatColor.GOLD + zone.toString());
            }
            sender.sendMessage(ChatColor.GOLD + "<<<");
        });
    }

    private void sendInfo(CommandSender sender, String name) {
        Main.runAsync(() -> {
            List<PermissionUser> fetched = db.find(PermissionUser.class)
                    .where()
                    .eq("name", name)
                    .gt("outdated", new Timestamp(now()))
                    .orderBy("type desc")
                    .findList();
            sender.sendMessage(ChatColor.GOLD + ">>> Permission info call " + name);
            for (PermissionMXBean zone : fetched) {
                sender.sendMessage(ChatColor.GOLD + zone.toString());
            }
            sender.sendMessage(ChatColor.GOLD + "<<<");
        });
    }

    private boolean execute(CommandSender sender, String name, String value, Iterator<String> it) {
        val box = PermissionBox.of(value);
        if (it.hasNext()) {
            return execute(sender, name, box, it.next());
        } else if (isZone(name)) {
            return addToZone(sender, cutHead(name), box);
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "You must type a daytime!");
        }
        return false;
    }

    /**
     * @param sender the command sender
     * @param name   zone name without prefix
     * @param box    a permission value box
     * @return {@code true} only if all operation okay
     */
    private boolean addToZone(CommandSender sender, String name, PermissionBox box) {
        $.thr(box.isAlter() || box.isWithdraw(), "Illegal argument");
        Main.runAsync(() -> {
            if (box.isZone()) {
                $.thr(name.equals(box.getValue()) || isLoop(name, box.getValue()), "Loop inherit");
            }
            val zone = new PermissionZone();
            zone.setName(name);
            val value = box.flat();
            zone.setValue(value);
            zone.setType(box.isZone());
            db.save(zone);
            sender.sendMessage(ChatColor.GOLD + "Specific operation done!");
            main.run(() -> fetcher.add('@' + name, value, -1));
        });
        return true;
    }

    private boolean execute(CommandSender sender, String name, PermissionBox box, String label) {
        if (label.equals("cancel") || label.equals("remove")) {
            Main.runAsync(() -> {
                remove(name, box.flat());
                sender.sendMessage(ChatColor.GOLD + "Specific operation done!");
            });
            return true;
        } else {
            $.thr($.isZone(name), "");
            int day = Integer.parseInt(label);
            return addToUser(sender, name, box, day);
        }
    }

    private void remove(String name, String value) {
        if (isZone(name)) {
            val fetched = db.find(PermissionZone.class)
                    .where("name = :name and value = :value")
                    .setParameter("name", cutHead(name))
                    .setParameter("value", value)
                    .findUnique();
            if (!nil(fetched)) {
                db.delete(fetched);
                main.run(() -> fetcher.remove(name, value));
            }
        } else {
            val fetched = db.find(PermissionUser.class)
                    .where("name = :name and value = :value and outdated > now()")
                    .setParameter("name", name)
                    .setParameter("value", value)
                    .findUnique();
            if (!$.nil(fetched)) {
                db.delete(fetched);
                main.run(() -> fetcher.remove(name, value));
            }
        }
    }

    private boolean addToUser(CommandSender sender, String name, PermissionBox box, long day) {
        $.thr(day == 0x00, "Add daytime can't be zero");
        Main.runAsync(() -> {
            val value = box.flat();
            val ext = db.find(PermissionUser.class)
                    .where("name = :name and value = :value and outdated > now()")
                    .setParameter("name", name)
                    .setParameter("value", value)
                    .findUnique();
            if ($.nil(ext)) {
                if (!(box.isAlter() || day < 1)) {
                    val add = new PermissionUser();
                    add.setName(name);
                    add.setValue(value);
                    add.setType(box.isZone());
                    if (main.getConfig().getBoolean("day.fully")) {
                        add.setOutdated($.next(day));
                    } else {
                        val now = LocalDate.now().plusDays(day).atStartOfDay();
                        add.setOutdated(Timestamp.from(now.atZone(ZoneId.systemDefault()).toInstant()));
                    }
                    db.save(add);
                    main.run(() -> fetcher.add(name, value, add.getOutdated().getTime()));
                    sender.sendMessage(ChatColor.GOLD + "Okay!");
                }
            } else {
                ext.setOutdated($.next(ext.getOutdated().toLocalDateTime(), day));
                fetcher.add(name, value, ext.getOutdated().getTime());
                db.save(ext);
                main.run(() -> fetcher.add(name, value, ext.getOutdated().getTime()));
                sender.sendMessage(ChatColor.GOLD + "Okay!");
            }
        });
        return true;
    }

    private boolean isLoop(String name, String zone) {
        val attach = PermissionValue.build("@" + zone, -1);
        fetcher.fetchZone(attach);
        return !$.nil(attach.lookSub("@" + name));
    }

    @Override
    public boolean addPermission(Player p, String permission, int time) {
        return addToUser(Bukkit.getConsoleSender(), p.getName(), PermissionBox.of(permission), time);
    }

    @Override
    public boolean hasPermission(Player p, String permission) {
        if (isZone(permission)) {
            Attachment attachment = fetcher.getFetched(p.getName());
            return !$.nil(attachment) && attachment.hasZone(cutHead(permission), true);
        }
        return p.hasPermission(permission);
    }
}
