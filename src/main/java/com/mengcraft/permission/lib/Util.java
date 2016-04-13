package com.mengcraft.permission.lib;

/**
 * Created on 16-4-7.
 */
public final class Util {

    public static String cutHead(String line, int head) {
        return line.substring(head, line.length());
    }

    public static String cutHead(String line) {
        return cutHead(line, 1);
    }

    public static boolean isZone(String line) {
        return line.charAt(0) == '@';
    }

    public static boolean isWithdraw(String line) {
        return line.charAt(0) == '-';
    }

    public static long now() {
        return System.currentTimeMillis();
    }

}
