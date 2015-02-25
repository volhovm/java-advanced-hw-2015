package ru.ifmo.ctddev.volhov.arrayset;

import java.util.*;
import java.util.function.Function;

/**
 * @author volhovm
 *         Created on 2/24/15
 */

@SuppressWarnings({"unchecked", "NullableProblems"})
public class ArraySet<T> implements NavigableSet<T> {
    final private Comparator<T> comparator;
    private boolean comparatorNative;
    final private int leftBound, rightBound; // [..)
    final private T[] array;

    // if value is found, @return is index of it, otherwise the place to insert is given
    private int binSearch(T t, int l, int r) {
        if (t == null) throw new NullPointerException();
        if (l + 1 == r) return
//                l;
                comparator.compare(t, array[l]) > 0 ? r : l;
        if (l == r) return l; //empty set?
        if (l > r) throw new IllegalArgumentException("binsearch l > r!");
        int median = (l + r) / 2;
        if (comparator.compare(array[median], t) > 0) return binSearch(t, l, median);
        else return binSearch(t, median, r);
    }

    private int search(T t, Function<Integer, Boolean> predicate) {
        int i = binSearch(t, leftBound, rightBound);
        if (predicate.apply(-1)) {
            if (i >= rightBound) i = rightBound - 1;
            for (; i >= 0; i--) {
                if (predicate.apply(comparator.compare(array[i], t))) return i;
            }
            return -1;
        } else if (predicate.apply(1)) {
            for (; i < array.length; i++) {
                if (predicate.apply(comparator.compare(array[i], t))) return i;
            }
            return rightBound;
        } else return i;
    }

    public ArraySet() {
        this((T[]) new Object[0], (t1, t2) -> 0, 0, 0, false);
    }

    public ArraySet(Collection<T> collection, Comparator<T> comparator) {
        this.comparator = comparator;

        // FIXME get rid of TreeSet (Do I really need it?)
        TreeSet<T> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(collection);
        T[] tempArray = (T[]) new Object[treeSet.size()];
        treeSet.toArray(tempArray);

        this.array = tempArray;
        Arrays.sort(this.array, comparator);
        leftBound = 0;
        rightBound = array.length;
        comparatorNative = false;
    }

    public <E extends Comparable<T>> ArraySet(Collection<T> collection) {
        this(collection, (t, t1) -> ((E) t).compareTo((T) t1));
        comparatorNative = true;
    }

    public ArraySet(T[] array, Comparator<T> comparator) {
        this(array, comparator, 0, array.length, false);
    }

    private ArraySet(T[] array, Comparator<T> comparator, int leftBound, int rightBound, boolean isCompNative) {
        this.array = array;
//        if (leftBound > rightBound)
//            throw new IllegalArgumentException("Can't construct set with leftBound > rightBound: " +
//                    leftBound + " " + rightBound);
        this.leftBound = leftBound;
        this.rightBound = leftBound >= rightBound ? leftBound : rightBound;
        this.comparator = comparator;
        this.comparatorNative = isCompNative;
    }

    private T getOrNull(int index) {
        if (inBounds(index)) return array[index];
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
        return inBounds(index) && comparator.compare(array[index], item) == 0;
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
                return array[++pointer];
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] ret = new Object[rightBound - leftBound];
        System.arraycopy(array, leftBound, ret, 0, ret.length);
        return ret;
    }

    @Override
    public <T1> T1[] toArray(T1[] t1s) throws NullPointerException, ArrayStoreException {
        if (t1s == null) throw new NullPointerException();
        try {
            return (T1[]) toArray();
        } catch (ClassCastException exc) {
            ArrayStoreException e = new ArrayStoreException();
            e.addSuppressed(exc);
            throw e;
        }
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        if (collection == null) throw new NullPointerException();
        T[] tempArray = (T[]) collection.toArray();
        for (int i = leftBound; i < rightBound; i++) {
            if (!this.contains(tempArray[i])) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(array, new Comparator<T>() {
            @Override
            public int compare(T t, T t1) {
                if (comparator.compare(t, t1) > 0) return -1;
                else if (comparator.compare(t, t1) < 0) return 1;
                else return 0;
            }
        }, leftBound, rightBound, comparatorNative);
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    private NavigableSet<T> subSet(int from, int to) {
        if (from > to) throw new IllegalArgumentException("'from' is greater than 'to'");
        if (from < leftBound || to > rightBound)
            throw new IllegalArgumentException("Given element is outside the range");
        return new ArraySet<>(array, comparator, from, to, comparatorNative);
    }

    @Override
    public NavigableSet<T> subSet(T t, boolean b, T e1, boolean b1) {
        if (t == null || e1 == null) throw new NullPointerException();
        if (comparator.compare(t, e1) > 0)
            throw new IllegalArgumentException("First element must be less than the second one");
        int from = search(t, i -> b ? (i >= 0) : (i > 0));
        int to = search(e1, i -> b1 ? (i <= 0) : (i < 0));
//        if (!contains(e1)) to--;
        try {
            return new ArraySet<>(array, comparator, from, to + 1, comparatorNative);
        } catch (Exception e) {
            System.out.println("Exception in subset: " + t + " " + e1 + " " + b + " " + b1);
            System.out.println(dumpS());
            throw e;
        }
    }

    @Override
    public NavigableSet<T> headSet(T t, boolean b) {
        int to = search(t, i -> b ? (i <= 0) : (i < 0));
        return subSet(leftBound, to + 1);
    }

    @Override
    public NavigableSet<T> tailSet(T t, boolean b) {
        int from = search(t, i -> b ? (i >= 0) : (i > 0));
        return subSet(from, rightBound);
    }

    @Override
    public Comparator<? super T> comparator() {
        if (!comparatorNative) return comparator;
        else return null;
    }

    @Override
    public SortedSet<T> subSet(T t, T e1) {
        return subSet(t, true, e1, false);
    }

    @Override
    public SortedSet<T> headSet(T t) {
        return headSet(t, false);
    }

    @Override
    public SortedSet<T> tailSet(T t) {
        return tailSet(t, true);
    }

    @Override
    public T first() {
        if (isEmpty()) throw new NoSuchElementException();
        return array[leftBound];
    }

    @Override
    public T last() {
        if (isEmpty()) throw new NoSuchElementException();
        return array[rightBound - 1];
    }

    private String dumpS() {
        String ret = "";
        ret += "Arrayset dump: size " + size() + ", content:\n";
        for (int i = leftBound; i < rightBound; i++) ret += array[i].toString() + "\n";
        return ret;
    }
}
