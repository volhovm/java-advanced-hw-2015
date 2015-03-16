package ru.ifmo.ctddev.volhov.arrayset;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author volhovm
 *         Created on 2/27/15
 */

public class ArrayWrapper<T> {
    private final T[] array;
    public final Comparator<T> comparator;
    public final boolean reversed;

    public <E extends Comparable<? super T>> ArrayWrapper(T[] array) {
        this((T[]) array, new Comparator<T>() {
            @Override
            public int compare(T t, T t1) {
                return ((E) t).compareTo(t1);
            }
        });
    }

    public ArrayWrapper(T[] array, Comparator<T> comparator) {
        this(array, comparator, false);
    }

    private ArrayWrapper(T[] array, Comparator<T> comparator, boolean reversed) {
        this.array = array;
        this.comparator = comparator;
        this.reversed = reversed;
    }

    public T get(int index) {
        return array[revIndex(index)];
    }

    public ArrayWrapper<T> reversedArray() {
        return new ArrayWrapper<T>(array, comparator, !reversed);
    }

    public int binarySearch(int leftBound, int rightBound, T t) {
        int i = Arrays.binarySearch(array, leftBound, rightBound, t, comparator);
        if (i < 0) i = reversed ? i - (2 * i) - 2 : i - (2 * i) - 1;
        return revIndex(i);
    }

    public int size() {
        return array.length;
    }

    public void sort() {
        Arrays.sort(array, comparator);
    }

    public int revIndex(int index) {
        if (reversed) return  size() - index - 1;
        else return index;
    }
}
