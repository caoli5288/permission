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
    public boolean hasZone(String zone, boolean depth) {
        return !$.nil(look("@" + zone, depth));
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

    public void removeZone(String zone, boolean depth) {
        val look = look("@" + zone, depth);
        if (!$.nil(look)) {
            look.getValue().cancel(attachment);
            if ($.nil(look.getKey())) {
                handled.remove("@" + zone);
            } else {
                look.getKey().removeSub("@" + zone);
            }
        }
    }

    public Pair<Attach, Attach> look(String key, boolean depth) {
        if (handled.containsKey(key)) return Pair.of(null, handled.get(key));
        if (depth) {
            for (Attach l : handled.values()) {
                val sub = l.look(key);
                if (!$.nil(sub)) return sub;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Attachment(handled=" + handled + ", attachment=" + attachment.getPermissions() + ")";
    }

}
