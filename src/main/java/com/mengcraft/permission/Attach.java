package com.mengcraft.permission;

import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
import lombok.val;
import org.bukkit.permissions.PermissionAttachment;

import java.util.LinkedList;
import java.util.List;

/**
 * Created on 17-6-12.
 */
public class Attach {

    private final String value;
    private final long outdated;
    private final List<Attach> sublist;

    private Attach(String value, long outdated, List<Attach> sublist) {
        this.value = value;
        this.outdated = outdated;
        this.sublist = sublist;
    }

    public String getValue() {
        return value;
    }

    public long getOutdated() {
        return outdated;
    }

    public List<Attach> getSublist() {
        return sublist;
    }

    public void handle(PermissionAttachment attachment) {
        if ($.isZone(value)) {
            for (Attach attach : sublist) {
                attach.handle(attachment);
            }
        } else {
            if ($.isWithdraw(value)) {
                attachment.setPermission($.cutHead(value), false);
            } else {
                attachment.setPermission(value, true);
            }
        }
    }

    public void cancel(PermissionAttachment attachment) {
        if ($.isZone(value)) {
            for (Attach attach : sublist) {
                attach.cancel(attachment);
            }
        } else {
            if ($.isWithdraw(value)) {
                attachment.unsetPermission($.cutHead(value));
            } else {
                attachment.unsetPermission(value);
            }
        }
    }

    public Pair<Attach, Attach> look(String key) {
        if ($.nil(sublist)) return null;
        for (Attach l : sublist) {
            if (l.value.equals(key)) return Pair.of(this, l);
            val sub = l.look(key);
            if (!$.nil(sub)) return sub;
        }
        return null;
    }

    public void removeSub(String key) {
        if (!$.nil(sublist)) {
            sublist.removeIf(attach -> attach.value.equals(key));
        }
    }

    public static Attach build(PermissionZone p) {
        return new Attach(p.getValue(), -1, $.isZone(p.getValue()) ? new LinkedList<>() : null);
    }

    public static Attach build(PermissionUser p) {
        return new Attach(p.getValue(), p.getOutdatedTime(), $.isZone(p.getValue()) ? new LinkedList<>() : null);
    }
}
