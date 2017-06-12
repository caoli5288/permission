package com.mengcraft.permission;

import com.mengcraft.permission.entity.PermissionMXBean;
import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
import com.mengcraft.simpleorm.EbeanHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import static com.mengcraft.permission.$.cutHead;
import static com.mengcraft.permission.$.isZone;
import static com.mengcraft.permission.$.now;

/**
 * Created on 15-10-20.
 */
public class Commander implements CommandExecutor, Permission {

    private static final long DAY_TIME = 86400000;

    private final EbeanHandler db;
    private final Main main;
    private final Fetcher fetcher;

    Commander(Main main, EbeanHandler db, Fetcher fetcher) {
        this.main = main;
        this.db = db;
        this.fetcher = fetcher;
    }

    @Override
    public boolean onCommand(CommandSender p, Command i, String j, String[] input) {
        Iterator<String> it = Arrays.asList(input).iterator();
        if (it.hasNext()) {
            return execute(p, it.next(), it);
        } else if (p instanceof Player) {
            sendTargetInfo(p, p.getName());
            return true;
        }
        return false;
    }

    private boolean execute(CommandSender sender, String name, Iterator<String> it) {
        if (sender.hasPermission("permission.admin")) {
            if (it.hasNext()) {
                return execute(sender, name, it.next(), it);
            } else {
                if (isZone(name)) {
                    sendTargetZoneInfo(sender, name);
                } else {
                    sendTargetInfo(sender, name);
                }
            }
            return true;
        }
        return false;
    }

    private void sendTargetZoneInfo(CommandSender sender, String name) {
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
    }

    private void sendTargetInfo(CommandSender sender, String name) {
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
    }

    private boolean execute(CommandSender sender, String name, String value, Iterator<String> it) {
        if (it.hasNext()) {
            return execute(sender, name, value, it.next());
        } else if (isZone(name)) {
            return addToZone(sender, cutHead(name), value);
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "You must type a daytime!");
        }
        return false;
    }

    /**
     * @param sender The command sender.
     * @param name   Zone name without prefix.
     * @param value  A permission or zone.
     * @return {@code true} if operation ok.
     */
    private boolean addToZone(CommandSender sender, String name, String value) {
        if (isZone(value)) {
            if (name.equals(cutHead(value)) || isLoop(name, cutHead(value))) {
                sender.sendMessage(ChatColor.DARK_RED + "Loop extend permissible!");
            } else {
                try {
                    addToZone(sender, name, value, true);
                } catch (Exception e) {
                    main.getLogger().log(Level.WARNING, "", e);
                }
                return true;
            }
        } else {
            try {
                addToZone(sender, name, value, false);
            } catch (Exception e) {
                main.getLogger().log(Level.WARNING, "", e);
            }
            return true;
        }
        return false;
    }

    /**
     * @param sender The command sender.
     * @param name   Zone name without prefix.
     * @param value  A permission or zone.
     * @param type   {@code true} if zone value.
     */
    private void addToZone(CommandSender sender, String name, String value, boolean type) {
        PermissionZone insert = new PermissionZone();
        insert.setName(name);
        insert.setValue(value);
        main.execute(() -> {
            db.save(insert);
            main.run(() -> fetcher.add('@' + name, value));
        });
        sender.sendMessage(ChatColor.GOLD + "Specific operation done!");
    }

    private boolean execute(CommandSender sender, String name, String value, String label) {
        if (label.equals("cancel")) {
            if (isZone(name)) {
                PermissionZone fetched = db.find(PermissionZone.class)
                        .where()
                        .eq("name", cutHead(name, 1))
                        .eq("value", value)
                        .findUnique();
                if (fetched == null) {
                    sender.sendMessage(ChatColor.GOLD + "Specific operation done!");
                } else {
                    main.execute(() -> {
                        db.delete(fetched);
                        main.run(() -> fetcher.remove(name, value));
                    });
                    sender.sendMessage(ChatColor.GOLD + "Specific operation done!");
                }
            } else {
                PermissionUser fetched = db.find(PermissionUser.class)
                        .where()
                        .eq("name", name)
                        .eq("value", value)
                        .gt("outdated", new Timestamp(now()))
                        .findUnique();
                if (fetched == null) {
                    sender.sendMessage(ChatColor.GOLD + "Specific operation done!");
                } else {
                    main.execute(() -> {
                        db.delete(fetched);
                        main.run(() -> fetcher.remove(name, value));
                    });
                    sender.sendMessage(ChatColor.GOLD + "Specific operation done!");
                }
            }
            return true;
        } else {
            return execute(sender, name, value, Integer.parseInt(label));
        }
    }

    private boolean execute(CommandSender sender, String name, String value, long day) {
        if (isZone(name)) {
            sender.sendMessage(ChatColor.DARK_RED + "Operation except cancel!");
        } else if (day == 0) {
            sender.sendMessage(ChatColor.DARK_RED + "Daytime can not be zero!");
        } else {
            PermissionUser fetched = db.find(PermissionUser.class)
                    .where()
                    .eq("name", name)
                    .eq("value", value)
                    .gt("outdated", new Timestamp(now()))
                    .findUnique();
            if (fetched == null) {
                if (day > 0) {
                    PermissionUser user = new PermissionUser();
                    user.setName(name);
                    user.setValue(value);
                    user.setOutdated(new Timestamp(now() + day * DAY_TIME));
                    main.execute(() -> {
                        db.save(user);
                        main.run(() -> fetcher.add(name, value));
                    });
                }
                sender.sendMessage(ChatColor.GOLD + "Specific operation done!");
            } else {
                fetched.setOutdated(new Timestamp(fetched.getOutdatedTime() + day * DAY_TIME));
                main.execute(() -> db.save(fetched));
                sender.sendMessage(ChatColor.GOLD + "Increased outdated done!");
            }
            return true;
        }
        return false;
    }

    private boolean isLoop(String name, String zone) {
        return !$.reduce(fetcher.fetchZone(zone), (k, v) -> v.equals(2) && k.equals(name)).isEmpty();
    }

    @Override
    public boolean addPermission(Player p, String permission, int time) {
        return !hasPermission(p, permission) && execute(main.getServer().getConsoleSender(), p.getName(), Arrays.asList(permission, String.valueOf(time)).iterator());
    }

    @Override
    public boolean hasPermission(Player p, String permission) {
        if (isZone(permission)) {
            Attachment attachment = fetcher.getFetched().get(p.getName());
            return !$.nil(attachment) && attachment.hasZone(cutHead(permission), true);
        }
        return p.hasPermission(permission);
    }
}
