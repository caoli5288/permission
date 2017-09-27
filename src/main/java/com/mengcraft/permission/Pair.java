package com.mengcraft.permission;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Created on 17-6-12.
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Pair<L, R> {

    private final L left;
    private final R right;

    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }
}
