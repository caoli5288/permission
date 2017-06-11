package com.mengcraft.permission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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

    public static boolean nil(Object any) {
        return any == null;
    }

    public static <K, V> Map<K, V> reduce(Map<K, V> input, BiPredicate<K, V> p) {
        Map<K, V> output = new HashMap<>();
        input.forEach((k, v) -> {
            if (p.test(k, v)) output.put(k, v);
        });
        return output;
    }

    public static <R, E> List<R> collect(List<E> in, Function<E, R> func) {
        List<R> out = new ArrayList<>(in.size());
        for (E i : in) {
            out.add(func.apply(i));
        }
        return out;
    }

    public static <E> void walk(Collection<E> i, Predicate<E> p, Consumer<E> c) {
        for (E e : i) {
            if (p.test(e)) {
                c.accept(e);
            }
        }
    }

}
