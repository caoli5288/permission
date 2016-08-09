package com.mengcraft.permission.lib;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created on 16-8-9.
 */
@SuppressWarnings("unchecked")
public class It<E> implements Iterator<E> {

    private final Object[] handle;
    private int index;

    public It(Object[] handle) {
        this.handle = handle;
    }

    public It(Collection<E> handle) {
        this(handle.toArray());
    }

    @Override
    public boolean hasNext() {
        return index != handle.length;
    }

    public int nextIndex() {
        return index;
    }

    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException("next");
        }
        return (E) handle[index++];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }
}
