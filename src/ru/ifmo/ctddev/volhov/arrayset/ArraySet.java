package ru.ifmo.ctddev.volhov.arrayset;

import java.util.*;
import java.util.function.IntPredicate;

/**
 * @author volhovm
 *         Created on 2/24/15
 */

@SuppressWarnings({"NullableProblems"})
public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private boolean comparatorNative;
    //    final private Comparator<T> comparator;
    final private int leftBound, rightBound; // [..)
    final private ArrayWrapper<T> array;


    public ArraySet() {
        this(new ArrayWrapper((T[]) new Object[0], (t1, t2) -> 0), 0, 0, false);
    }

    public ArraySet(Collection<T> collection, Comparator<T> comparator) {

        TreeSet<T> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(collection);
        T[] tempArray = (T[]) new Object[treeSet.size()];
        treeSet.toArray(tempArray);

        this.array = new ArrayWrapper<T>(tempArray, comparator);
        array.sort();
        leftBound = 0;
        rightBound = array.size();
        comparatorNative = false;
    }

    public <E extends Comparable<T>> ArraySet(Collection<T> collection) {
        this(collection, (t, t1) -> ((E) t).compareTo((T) t1));
        comparatorNative = true;
    }

    public ArraySet(T[] array, Comparator<T> comparator) {
        this(new ArrayWrapper<T>(array, comparator), 0, array.length, false);
    }

    private ArraySet(ArrayWrapper<T> array, int leftBound, int rightBound, boolean isCompNative) {
        this.array = array;
        this.leftBound = leftBound;
        this.rightBound = leftBound >= rightBound ? leftBound : rightBound;
        this.comparatorNative = isCompNative;
    }

    private int search(T t, IntPredicate predicate) {
        int i = array.binarySearch(leftBound, rightBound, t);
        if (array.reversed) {
            predicate = predicate.negate();
        }

        if (predicate.test(-1)) {
            if (i >= rightBound) i = rightBound - 1;
            for (; i >= 0; i--) {
                if (predicate.test(array.comparator.compare(array.get(i), t))) return i;
            }
            return -1;
        } else if (predicate.test(1)) {
            for (; i < array.size(); i++) {
                if (predicate.test(array.comparator.compare(array.get(i), t))) return i;
            }
            return rightBound;
        } else return i;
    }

    private T getOrNull(int index) {
        if (inBounds(index)) return array.get(index);
        else return null;
    }

    @Override
    public T lower(T t) {
        return getOrNull(search(t, i -> i < 0));
    }

    @Override
    public T floor(T t) {
        return getOrNull(search(t, i -> i <= 0));
    }

    @Override
    public T ceiling(T t) {
        return getOrNull(search(t, i -> i >= 0));
    }

    @Override
    public T higher(T t) {
        return getOrNull(search(t, i -> i > 0));
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    @Override
    public int size() {
        return rightBound - leftBound;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) throw new NullPointerException();
        T item = (T) o; //ClassCastException
        int index = search(item, i -> i == 0);
        return inBounds(index) && array.comparator.compare(array.get(index), item) == 0;
    }

    private boolean inBounds(int index) {
        return index >= leftBound && index < rightBound;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int pointer = leftBound - 1;

            @Override
            public boolean hasNext() {
                return pointer < rightBound - 1;
            }

            @Override
            public T next() {
                return array.get(++pointer);
            }
        };
    }

    @Override
    public ArraySet<T> descendingSet() {
        return new ArraySet<T>(array.reversedArray(),
                leftBound, rightBound, comparatorNative);
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    private ArraySet<T> subSet(int from, int to) {
        if (from > to) throw new IllegalArgumentException("'from' is greater than 'to'");
        if (from < leftBound || to > rightBound)
            throw new IllegalArgumentException("Given element is outside the range");
        return new ArraySet<T>(array, from, to, comparatorNative);
    }

    @Override
    public ArraySet<T> subSet(T t, boolean b, T e1, boolean b1) {
        if (t == null || e1 == null) throw new NullPointerException();
        if (array.comparator.compare(t, e1) > 0)
            throw new IllegalArgumentException("First element must be less than the second one");
        int from = search(t, i -> b ? (i >= 0) : (i > 0));
        int to = search(e1, i -> b1 ? (i <= 0) : (i < 0));
//        if (!contains(e1)) to--;
        try {
            return new ArraySet<>(array, from, to + 1, comparatorNative);
        } catch (Exception e) {
            System.out.println("Exception in subset: " + t + " " + e1 + " " + b + " " + b1);
            System.out.println(dumpS());
            throw e;
        }
    }

    @Override
    public ArraySet<T> headSet(T t, boolean b) {
        int to = search(t, i -> b ? (i <= 0) : (i < 0));
        return subSet(leftBound, to + 1);
    }

    @Override
    public ArraySet<T> tailSet(T t, boolean b) {
        int from = search(t, i -> b ? (i >= 0) : (i > 0));
        return subSet(from, rightBound);
    }

    @Override
    public Comparator<? super T> comparator() {
        if (!comparatorNative) return array.comparator;
        else return null;
    }

    @Override
    public ArraySet<T> subSet(T t, T e1) {
        return subSet(t, true, e1, false);
    }

    @Override
    public ArraySet<T> headSet(T t) {
        return headSet(t, false);
    }

    @Override
    public SortedSet<T> tailSet(T t) {
        return tailSet(t, true);
    }

    @Override
    public T first() {
        if (isEmpty()) throw new NoSuchElementException();
        return array.get(leftBound);
    }

    @Override
    public T last() {
        if (isEmpty()) throw new NoSuchElementException();
        return array.get(rightBound - 1);
    }

    private String dumpS() {
        String ret = "";
        ret += "Arrayset dump: size " + size() + ", content:\n";
        for (T t : this) ret += t.toString() + "\n";
        return ret;
    }
}
