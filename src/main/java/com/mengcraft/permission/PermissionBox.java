package com.mengcraft.permission;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.val;

/**
 * Created by on 2017/9/27.
 */
@Data
@Setter(value = AccessLevel.NONE)
public class PermissionBox {

    private String value;

    private boolean alter;
    private boolean zone;
    private boolean withdraw;

    public String flat() {
        return (alter ? "?" : "") + (zone ? "@" : "") + (withdraw ? "-" : "") + value;
    }

    public static String b(PermissionBox box, String value) {
        if ($.isAlter(value)) {
            box.alter = true;
            return b(box, $.cutHead(value));
        }
        if ($.isZone(value)) {
            box.zone = true;
            return b(box, $.cutHead(value));
        }
        if ($.isWithdraw(value)) {
            box.withdraw = true;
            return b(box, $.cutHead(value));
        }
        return value;
    }

    public static PermissionBox of(String value) {
        val box = new PermissionBox();
        box.value = b(box, value);
        $.thr(box.isZone() && box.isWithdraw(), "Illegal argument");
        return box;
    }
}
