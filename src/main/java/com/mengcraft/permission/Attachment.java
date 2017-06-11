package com.mengcraft.permission;

import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by on 16-4-9.
 */
public class Attachment {

    private final List<String> zoneList = new ArrayList<>();
    private final PermissionAttachment attachment;

    public Attachment(PermissionAttachment attachment) {
        this.attachment = attachment;
    }

    /**
     * @param zone Zone name without '@'.
     * @return True if contains the zone.
     */
    public boolean hasZone(String zone) {
        return zoneList.contains(zone);
    }

    /**
     * @param zone Zone name without '@'.
     */
    public void addZone(String zone) {
        zoneList.add(zone);
    }

    public void addPermission(String permission, boolean value) {
        attachment.setPermission(permission, value);
    }

    public void removePermission(String permission) {
        attachment.unsetPermission(permission);
    }

    public void removePermission() {
        attachment.remove();
    }

    public void removeZone(String zone) {
        zoneList.remove(zone);
    }

    @Override
    public String toString() {
        return "zoneList=" + zoneList + ", attachment=" + attachment.getPermissions();
    }

}
