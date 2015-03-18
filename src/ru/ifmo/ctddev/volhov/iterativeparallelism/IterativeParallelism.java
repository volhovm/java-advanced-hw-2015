package ru.ifmo.ctddev.volhov.iterativeparallelism;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author volhovm
 *         Created on 3/17/15
 */
public class IterativeParallelism implements ListIP {

    @Override
    public String concat(int threads, List<?> values) throws InterruptedException {
        return ConcUtils.foldl(
                Monoid.stringConcat(),
                ConcUtils.map(Object::toString, values, threads),
                threads
        );
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return ConcUtils.<List<T>>foldl(Monoid.<T>listConcatWithPred((a, b) -> !b.isEmpty() && predicate.test(b.get(0))), map(threads, values, a -> {
            List<T> ret = new LinkedList<T>();
            ret.add((T) a);
            return ret;
        }), threads);
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f)
            throws InterruptedException {
        return ConcUtils.map(f, values, threads);
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return ConcUtils.<T>foldl1((a, b) -> comparator.compare(a, b) < 0 ? b : a, (List<T>) values, threads);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return ConcUtils.<Boolean>foldl(
                Monoid.boolAnd(true),
                ConcUtils.<T, Boolean>map(predicate::test, (List<T>) values, threads),
                threads);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }
}
