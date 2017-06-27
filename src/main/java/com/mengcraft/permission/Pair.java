package com.mengcraft.permission;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Created on 17-6-12.
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Pair<K, V> {

    private final K key;
    private final V value;

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }
}
