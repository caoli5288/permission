package com.mengcraft.permission;

import com.mengcraft.permission.entity.Permission;
import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
import com.mengcraft.simpleorm.EbeanHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.mengcraft.permission.Util.cutHead;
import static com.mengcraft.permission.Util.isZone;
import static com.mengcraft.permission.Util.now;

/**
 * Created on 15-10-20.
 */
class Commander implements CommandExecutor {

    private static final long DAY_TIME = 86400000;

    private final EbeanHandler db;
    private final Main main;

    Commander(Main main, EbeanHandler db) {
        this.db = db;
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        Iterator<String> it = Arrays.asList(arguments).iterator();
        if (it.hasNext()) {
            return execute(sender, it.next(), it);
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "/permission $name $permission <$day|remove>");
        }
        return false;
    }

    private boolean execute(CommandSender sender, String name, Iterator<String> it) {
        if (it.hasNext()) {
            return execute(sender, name, it.next(), it);
        } else {
            if (isZone(name)) {
                List<PermissionZone> fetched = db.find(PermissionZone.class)
                        .where()
                        .eq("name", cutHead(name, 1))
                        .orderBy("type desc")
                        .findList();
                sender.sendMessage(ChatColor.GOLD + ">>> Permission info of " + name);
                for (Permission zone : fetched) {
                    sender.sendMessage(ChatColor.GOLD + zone.toString());
                }
                sender.sendMessage(ChatColor.GOLD + "<<<");
            } else {
                List<PermissionUser> fetched = db.find(PermissionUser.class)
                        .where()
                        .eq("name", name)
                        .gt("outdated", new Timestamp(now()))
                        .orderBy("type desc")
                        .findList();
                sender.sendMessage(ChatColor.GOLD + ">>> Permission info of " + name);
                for (Permission zone : fetched) {
                    sender.sendMessage(ChatColor.GOLD + zone.toString());
                }
                sender.sendMessage(ChatColor.GOLD + "<<<");
            }
            return true;
        }
    }

    private boolean execute(CommandSender sender, String name, String value, Iterator<String> it) {
        if (it.hasNext()) {
            return execute(sender, name, value, it.next());
        } else {
            if (isZone(name)) {
                PermissionZone fetched = db.find(PermissionZone.class)
                        .where()
                        .eq("name", cutHead(name, 1))
                        .eq("value", value)
                        .findUnique();
                if (fetched == null) {
                    PermissionZone zone = new PermissionZone();
                    zone.setName(cutHead(name, 1));
                    zone.setValue(value);
                    zone.setType(isZone(value));
                    main.execute(() -> {
                        db.save(zone);
                    });
                    sender.sendMessage(ChatColor.GOLD + "Specific operation done!");
                } else {
                    sender.sendMessage(ChatColor.GOLD + "Specific operation done!");
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "You must type a daytime!");
            }
            return false;
        }
    }

    private boolean execute(CommandSender sender, String name, String value, String label) {
        if (label.equals("remove")) {
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
            sender.sendMessage(ChatColor.DARK_RED + "Operation except remove!");
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
                    user.setType(isZone(value));
                    user.setOutdated(new Timestamp(now() + day * DAY_TIME));
                    main.execute(() -> {
                        db.save(user);
                    });
                }
                sender.sendMessage(ChatColor.GOLD + "Specific operation done!");
            } else {
                fetched.setOutdated(new Timestamp(fetched.getOutdatedTime() + day * DAY_TIME));
                main.execute(() -> {
                    db.save(fetched);
                });
                sender.sendMessage(ChatColor.GOLD + "Increased outdated done!");
            }
            return true;
        }
        return false;
    }

}
