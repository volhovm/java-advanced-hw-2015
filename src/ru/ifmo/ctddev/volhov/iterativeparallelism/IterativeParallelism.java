package ru.ifmo.ctddev.volhov.iterativeparallelism;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This class specifies the functions over list that can be executed simultaneously in the given
 * number of distinct threads. It does not use any high-abstract tools implemented in {@link java.util.concurrent}
 * class.
 * <p>
 * The inner nature of class is based on {@link ru.ifmo.ctddev.volhov.iterativeparallelism.ConcUtils} class,
 * that gives an access to parallel functions, similar to {@code foldl} and {@code map}. As many of operations
 * are associative ({@link #minimum}, {@link #all}), it also uses the
 * {@link ru.ifmo.ctddev.volhov.iterativeparallelism.Monoid} class to represent this abstraction.
 *
 * @author volhovm
 * @see ru.ifmo.ctddev.volhov.iterativeparallelism.ConcUtils
 * @see ru.ifmo.ctddev.volhov.iterativeparallelism.Monoid
 */
public class IterativeParallelism implements ListIP {

    @Override
    public String concat(int threads, List<?> values) {
        return ConcUtils.foldl(
                Monoid.stringConcat(),
                ConcUtils.map(Object::toString, values, threads),
                threads
        );
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) {
        return ConcUtils.<List<T>>foldl(
                Monoid.<T>listConcatWithPred((a, b) -> !b.isEmpty() && predicate.test(b.get(0))),
                map(threads, values, a -> {
                    List<T> ret = new LinkedList<T>();
                    ret.add((T) a);
                    return ret;
                }), threads);
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) {
        return ConcUtils.map(f, values, threads);
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) {
        return ConcUtils.<T>foldl1((a, b) -> comparator.compare(a, b) < 0 ? b : a, (List<T>) values, threads);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) {
        return ConcUtils.<Boolean>foldl(
                Monoid.boolAnd(true),
                ConcUtils.<T, Boolean>map(predicate::test, (List<T>) values, threads),
                threads);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) {
        return !all(threads, values, predicate.negate());
    }
}
