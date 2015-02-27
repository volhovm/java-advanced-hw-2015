package ru.ifmo.ctddev.volhov.arrayset;

import java.util.Comparator;

/**
 * @author volhovm
 *         Created on 2/27/15
 */

public class ArrayWrapper<T> {
    private final T[] array;
    private final Comparator<T> comparator;
    private final int leftBound, rightBound;

    public ArrayWrapper(T[] array, Comparator<T> comparator) {
        this(array, comparator, 0, array.length);
    }

    public ArrayWrapper(T[] array, Comparator<T> comparator, int leftBound, int rightBound) {
        this.array = array;
        this.comparator = comparator;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
    }
}
