package com.mengcraft.permission;

/**
 * Created on 16-4-7.
 */
final class Util {

    static String cutHead(String line, int head) {
        return line.substring(head, line.length());
    }

    static boolean isZone(String line) {
        return line.charAt(0) == '@';
    }

    static boolean isWithdraw(String line) {
        return line.charAt(0) == '-';
    }

    static long now() {
        return System.currentTimeMillis();
    }

}
