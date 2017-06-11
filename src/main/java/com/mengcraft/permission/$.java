package com.mengcraft.permission;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Created on 16-4-7.
 */
public final class $ {

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

    public static <K, V> Map<K, V> reduce(Map<K, V> input, BiPredicate<K, V> p) {
        Map<K, V> output = new HashMap<>();
        input.forEach((k, v) -> {
            if (p.test(k, v)) output.put(k, v);
        });
        return output;
    }

}
