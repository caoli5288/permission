package com.mengcraft.permission;

import com.google.common.collect.ImmutableMap;
import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
import org.bukkit.permissions.PermissionAttachment;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created on 17-6-12.
 */
public class Attach {

    private final String value;
    private final long outdated;
    private final List<Attach> subList;

    private Attach(String value, long outdated, List<Attach> subList) {
        this.value = value;
        this.outdated = outdated;
        this.subList = subList;
    }

    public String getValue() {
        return value;
    }

    public long getOutdated() {
        return outdated;
    }

    public List<Attach> getSubList() {
        return subList;
    }

    public void handle(PermissionAttachment attachment) {
        if ($.isZone(value)) {
            for (Attach attach : subList) {
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
            for (Attach attach : subList) {
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

    public Map<String, Attach> unfold() {
        if ($.nil(subList)) return ImmutableMap.of(value, this);
        ImmutableMap.Builder<String, Attach> b = ImmutableMap.builder();
        b.put(value, this);
        for (Attach attach : subList) {
            b.putAll(attach.unfold());
        }
        return b.build();
    }

    public static Attach build(PermissionZone p) {
        return new Attach(p.getValue(), -1, $.isZone(p.getValue()) ? new LinkedList<>() : null);
    }

    public static Attach build(PermissionUser p) {
        return new Attach(p.getValue(), p.getOutdatedTime(), $.isZone(p.getValue()) ? new LinkedList<>() : null);
    }

}
