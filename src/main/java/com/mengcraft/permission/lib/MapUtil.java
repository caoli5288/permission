package com.mengcraft.permission.lib;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Created on 15-12-17.
 */
public final class MapUtil {

    public static <K, V> Map<K, V> reduce(Map<K, V> input, BiPredicate<K, V> p) {
        Map<K, V> output = new HashMap<>();
        input.forEach((k, v) -> {
            if (p.test(k, v)) output.put(k, v);
        });
        return output;
    }

}
