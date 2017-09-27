package com.mengcraft.permission;

import com.google.common.collect.ImmutableMap;
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
    private final Map<String, PermissionValue> handled;

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

    public void removePermission(String permission) {
        val attach = handled.remove(permission);
        if (!$.nil(attach)) attach.cancel(attachment);
    }

    public void removePermission() {
        attachment.remove();
    }

    public void handle(PermissionValue attach) {
        val old = handled.put(attach.getValue(), attach);
        if (!$.nil(old)) old.cancel(attachment);

        attach.handle(attachment);
    }

    public void handle(List<PermissionValue> list) {
        list.forEach(this::handle);
    }

    public Pair<PermissionValue, PermissionValue> look(String key, boolean depth) {
        if (handled.containsKey(key)) return Pair.of(null, handled.get(key));
        if (depth) {
            for (PermissionValue l : handled.values()) {
                val sub = l.lookSub(key);
                if (!$.nil(sub)) return sub;
            }
        }
        return null;
    }

    public PermissionAttachment getAttachment() {
        return attachment;
    }

    public void cleanup() {
        long now = $.now();
        $.walk(ImmutableMap.copyOf(handled), (k, v) -> v.getOutdated() < now, (k, v) -> removePermission(k));
    }

}
