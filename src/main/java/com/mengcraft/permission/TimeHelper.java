package com.mengcraft.permission;

import lombok.val;

import java.util.concurrent.TimeUnit;

/**
 * Created on 17-4-26.
 */
public class TimeHelper {

    enum Unit {
        DAY("天", TimeUnit.DAYS.toSeconds(1)),
        HOUR("时", TimeUnit.HOURS.toSeconds(1)),
        MIN("分", TimeUnit.MINUTES.toSeconds(1)),
        SEC("秒", 1);

        final String unit;
        final long l;

        Unit(String unit, long l) {
            this.unit = unit;
            this.l = l;
        }
    }

    public static String encode(long input) {
        val buf = new StringBuilder();
        for (Unit unit : Unit.values()) {
            if (buf.length() > 1 || input > unit.l) {
                buf.append(input / unit.l).append(unit.unit);
                input = input % unit.l;
            }
        }
        return buf.toString();
    }

}
