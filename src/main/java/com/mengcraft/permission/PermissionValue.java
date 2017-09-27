package com.mengcraft.permission;

import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.permission.entity.PermissionZone;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created on 17-6-12.
 */
@Data
@EqualsAndHashCode(of = {"value", "outdated"})
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PermissionValue {

    private final String value;
    private final long outdated;
    private final List<PermissionValue> sublist;

    public void handle(PermissionAttachment attachment) {
        if ($.isZone(value)) {
            for (PermissionValue attach : sublist) {
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
            for (PermissionValue attach : sublist) {
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

    public Pair<PermissionValue, PermissionValue> lookSub(String key) {
        if ($.nil(sublist)) return null;
        for (PermissionValue l : sublist) {
            if (l.value.equals(key)) return Pair.of(this, l);
            val sub = l.lookSub(key);
            if (!$.nil(sub)) return sub;
        }
        return null;
    }

    public PermissionValue removeSub(String key) {
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

    protected PermissionValue mirror() {
        return new PermissionValue(value, outdated, new ArrayList<>(sublist));
    }

    public static PermissionValue build(String value, long outdated) {
        return new PermissionValue(value, outdated, $.isZone(value) ? new LinkedList<>() : null);
    }

    public static PermissionValue build(PermissionZone p) {
        return new PermissionValue(p.getValue(), -1, $.isZone(p.getValue()) ? new LinkedList<>() : null);
    }

    public static PermissionValue build(PermissionUser p) {
        return new PermissionValue(p.getValue(), p.getOutdated().getTime(), $.isZone(p.getValue()) ? new LinkedList<>() : null);
    }

}
