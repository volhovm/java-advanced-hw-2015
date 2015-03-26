package ru.ifmo.ctddev.volhov.iterativeparallelism;

/**
 * @author volhovm
 *         Created on 3/26/15
 */
@FunctionalInterface
public interface ConcurrentFunction<T, R> {
    R apply(T t) throws InterruptedException;
}
