package com.mengcraft.permission;

import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 15-10-20.
 */
public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        EbeanHandler db = EbeanManager.DEFAULT.getHandler(this);
        if (!db.isInitialized()) {
            db.define(PermissionUser.class);
            try {
                db.initialize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        db.install();
        db.reflect();

        getServer().getPluginManager().registerEvents(new Executor(this, db), this);
        getCommand("permission").setExecutor(new Commander(this, db));
    }

    public void asyncTask(Runnable task) {
        getServer().getScheduler().runTaskAsynchronously(this, task);
    }

}
