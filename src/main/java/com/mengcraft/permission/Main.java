package com.mengcraft.permission;

import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Level;

import static com.mengcraft.permission.$.nil;

/**
 * Created on 15-10-20.
 */
public class Main extends JavaPlugin {

    private boolean offline;
    private static Main plugin;

    @Override
    public void onEnable() {
        plugin = this;

        getConfig().options().copyDefaults(true);
        saveConfig();

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

        Fetcher.INSTANCE.init(this, db);
        if (!offline) {
            getServer().getMessenger().registerIncomingPluginChannel(this, Fetcher.CHANNEL, Fetcher.INSTANCE);
            getServer().getMessenger().registerOutgoingPluginChannel(this, Fetcher.CHANNEL);
        }

        getServer().getPluginManager().registerEvents(new Executor(), this);

        Commander commander = new Commander(this, db);
        getCommand("permission").setExecutor(commander);
        getServer().getServicesManager().register(Permission.class, commander, this, ServicePriority.Normal);

        if (!nil(Bukkit.getPluginManager().getPlugin("PlaceholderAPI"))) {
            log("### Hook to PlaceholderAPI");
            val placeholder = new MyPlaceholder(this);
            placeholder.hook();
        }

        String[] author = {
                ChatColor.GREEN + "梦梦家高性能服务器出租店",
                ChatColor.GREEN + "shop105595113.taobao.com"
        };
        getServer().getConsoleSender().sendMessage(author);
    }

    public static Future<Void> execute(Runnable r) {
        return CompletableFuture.runAsync(r).exceptionally(thr -> {
            log(thr);
            return null;
        });
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

    public static void log(String message) {
        plugin.getLogger().log(Level.INFO, message);
    }

    public static void log(Throwable e) {
        plugin.getLogger().log(Level.SEVERE, e.toString(), e);
    }

}
