package com.mengcraft.permission;

import lombok.val;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by on 16-4-9.
 */
public class Attachment {

    private final PermissionAttachment attachment;
    private final Map<String, Attach> handled;

    public Attachment(PermissionAttachment attachment) {
        this.attachment = attachment;
        handled = new HashMap<>();
    }

    /**
     * @param zone Zone name without '@'.
     * @return True if contains the zone.
     */
    public boolean hasZone(String zone) {
        return handled.containsKey("@" + zone);
    }

    public void addPermission(String permission, boolean value) {
        attachment.setPermission(permission, value);
    }

    public void removePermission(String permission) {
        val attach = handled.remove(permission);
        if (!$.nil(attach)) attach.cancel(attachment);
    }

    public void removePermission() {
        attachment.remove();
    }

    public void handle(Attach attach) {
        handled.put(attach.getValue(), attach);
        attach.handle(attachment);
    }

    public void handle(List<Attach> list) {
        list.forEach(this::handle);
    }

    public void removeZone(String zone) {
        val attach = handled.remove("@" + zone);
        if (!$.nil(attach)) attach.cancel(attachment);
    }

    @Override
    public String toString() {
        return "Attachment(handled=" + handled + ", attachment=" + attachment.getPermissions() + ")";
    }

}
