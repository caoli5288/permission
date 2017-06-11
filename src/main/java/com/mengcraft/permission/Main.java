package com.mengcraft.permission;

import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

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

        Fetcher fetcher = new Fetcher(this, db);
        if (!offline) {
            getServer().getMessenger().registerIncomingPluginChannel(this, Fetcher.CHANNEL, fetcher);
            getServer().getMessenger().registerOutgoingPluginChannel(this, Fetcher.CHANNEL);
        }

        getServer().getPluginManager().registerEvents(new Executor(fetcher), this);

        Commander commander = new Commander(this, db, fetcher);
        getCommand("permission").setExecutor(commander);
        getServer().getServicesManager().register(Permission.class, commander, this, ServicePriority.Normal);

        String[] author = {
                ChatColor.GREEN + "梦梦家高性能服务器出租店",
                ChatColor.GREEN + "shop105595113.taobao.com"
        };
        getServer().getConsoleSender().sendMessage(author);
    }

    public Future<Void> execute(Runnable r) {
        return CompletableFuture.runAsync(r);
    }

    public int run(Runnable r) {
        return getServer().getScheduler().runTask(this, r).getTaskId();
    }

    public boolean isOffline() {
        return offline;
    }

    private void setOffline(boolean offline) {
        this.offline = offline;
    }

}
