package com.mengcraft.permission;

import org.bukkit.entity.Player;

/**
 * Created on 16-6-12.
 */
public interface Permission {

    boolean addPermission(Player p, String permission, int time);

    boolean hasPermission(Player p, String permission);

}
