package com.mengcraft.permission;

import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
import lombok.val;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;
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

    public Pair<Attach, Attach> lookSub(String key) {
        if ($.nil(sublist)) return null;
        for (Attach l : sublist) {
            if (l.value.equals(key)) return Pair.of(this, l);
            val sub = l.lookSub(key);
            if (!$.nil(sub)) return sub;
        }
        return null;
    }

    public Attach removeSub(String key) {
        if ($.nil(sublist)) return null;
        val i = sublist.iterator();
        while (i.hasNext()) {
            val attach = i.next();
            if (attach.value.equals(key)) {
                i.remove();
                return attach;
            }
        }
        return null;
    }

    protected Attach mirror() {
        return new Attach(value, outdated, new ArrayList<>(sublist));
    }

    public static Attach build(String value, long outdated) {
        return new Attach(value, outdated, $.isZone(value) ? new LinkedList<>() : null);
    }

    public static Attach build(PermissionZone p) {
        return new Attach(p.getValue(), -1, $.isZone(p.getValue()) ? new LinkedList<>() : null);
    }

    public static Attach build(PermissionUser p) {
        return new Attach(p.getValue(), p.getOutdatedTime(), $.isZone(p.getValue()) ? new LinkedList<>() : null);
    }
}
