package com.mengcraft.permission;

import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
import com.mengcraft.permission.manager.Fetcher;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Created on 15-10-20.
 */
public class Main extends JavaPlugin {

    private boolean offline;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setOffline(getConfig().getBoolean("offline"));

        EbeanHandler db = EbeanManager.DEFAULT.getHandler(this);
        if (!db.isInitialized()) {
            db.define(PermissionUser.class);
            db.define(PermissionZone.class);
            if (offline) {
                db.setDriver("org.sqlite.JDBC");
                db.setUrl("jdbc:sqlite:" + new File(getDataFolder(), "database.db"));
                db.setUserName("");
                db.setPassword("");
            }
            try {
                db.initialize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        db.install();
        db.reflect();

        Fetcher fetcher = new Fetcher(this, db);
        if (!offline) {
            getServer().getMessenger().registerIncomingPluginChannel(this, Fetcher.CHANNEL, fetcher);
            getServer().getMessenger().registerOutgoingPluginChannel(this, Fetcher.CHANNEL);
        }

        getServer().getPluginManager().registerEvents(new Executor(fetcher), this);
        getCommand("permission").setExecutor(new Commander(this, db, fetcher));

        String[] strings = {
                ChatColor.GREEN + "梦梦家高性能服务器出租店",
                ChatColor.GREEN + "shop105595113.taobao.com"
        };
        getServer().getConsoleSender().sendMessage(strings);
    }

    public void execute(Runnable task, boolean b) {
        if (b) {
            getServer().getScheduler().runTaskAsynchronously(this, task);
        } else {
            getServer().getScheduler().runTask(this, task);
        }
    }

    public void execute(Runnable task) {
        execute(task, true);
    }

    public boolean isOffline() {
        return offline;
    }

    private void setOffline(boolean offline) {
        this.offline = offline;
    }

}
