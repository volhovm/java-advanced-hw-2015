package ru.ifmo.ctddev.volhov.iterativeparallelism;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author volhovm
 *         Created on 3/17/15
 */
public class Monoid<T> {
    // {elements of type T} set
    final Optional<Supplier<T>> zero;
    final BinaryOperator<T> op;

    public Monoid(Supplier<T> zero, BinaryOperator<T> op) {
        this.zero = Optional.of(zero);
        this.op = op;
    }

    public Monoid(BinaryOperator<T> op) {
        this.zero = Optional.empty();
        this.op = op;
    }

    public boolean isComplete() {
        return zero.isPresent();
    }
    
    public static <T> Monoid<List<T>> listConcat() {
        return listConcatWithPred((a, b) -> true);
    }

    public static <T> Monoid<List<T>> listConcatWithPred(BiPredicate<List<T>, List<T>> pred) {
        return new Monoid<List<T>>(LinkedList::new, (a, b) -> {
            if (!pred.test(a, b)) return a;
            LinkedList<T> ret = new LinkedList<>();
            ret.addAll(a);
            ret.addAll(b);
            return ret;
        });
    }

    public static Monoid<String> stringConcat() {
        return new Monoid<>(() -> "", String::concat);
    }

    public static Monoid<Boolean> boolOr(boolean init) {
        return new Monoid<>(() -> init, (a, b) -> a || b);
    }

    public static Monoid<Boolean> boolAnd(boolean init) {
        return new Monoid<>(() -> init, (a, b) -> a && b);
    }
}
