package ru.ifmo.ctddev.volhov.iterativeparallelism;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

/**
 * This class represents an abstraction of Monoid -- the set with one selected element,
 * called identity, and one binary operation, closed on this set.
 * <p>
 * It does not check the associativity of given operation. It does not also check, that id
 * is the real id, that is for any element {@code a} of type {@code T},
 * {@code op(a, id) = op(id, a) = op}.
 * <p>
 * The monoid can contain no identity element, what is needed to deal with given functions
 * without it in functions like {@code foldl1}. This can be checked directly (id has the type
 * {@link java.util.Optional}) or with the call of the method {@link #isComplete}.
 * <p>
 * There is also static methods that return basic realisation of monoids, such as monoid over Strings,
 * monoid over Lists, monoid over boolean value and operations "or", "and", and so on.
 *
 * @author volhovm
 *         Created on 3/17/15
 */
public class Monoid<T> {
    /**
     * The identity element of monoid.
     */
    final Optional<Supplier<T>> id;

    /**
     * The binary operation of monoid.
     */
    final BinaryOperator<T> op;

    /**
     * Constructor for creating complete monoid.
     *
     * @param id identity element of monoid
     * @param op binary operation of monoid
     */
    public Monoid(Supplier<T> id, BinaryOperator<T> op) {
        this.id = Optional.of(id);
        this.op = op;
    }

    /**
     * Constructor for creating incomplete monoid.
     *
     * @param op binary operation of monoid
     */
    public Monoid(BinaryOperator<T> op) {
        this.id = Optional.empty();
        this.op = op;
    }

    /**
     * This methods indicates if identity element is specified
     *
     * @return true, if monoid has identity element
     */
    public boolean isComplete() {
        return id.isPresent();
    }

    /**
     * Returns default monoid on {@link java.util.List}. The id is empty list, and
     * operation is list concatenation.
     *
     * @param <T> the parameter of list
     *
     * @return monoid on list
     */
    public static <T> Monoid<List<T>> listConcat() {
        return new Monoid<List<T>>(LinkedList::new, (a, b) -> {
            LinkedList<T> ret = new LinkedList<>();
            ret.addAll(a);
            ret.addAll(b);
            return ret;
        });
    }

    /**
     * Returns monoid on {@link java.util.List} that has empty list as id element and
     * function that returns concatenation of lists if predicate returns true and the
     * first list otherwise.
     *
     * @param pred the predicate to tests lists on
     * @param <T>  parameter of list elements
     *
     * @return monoid on list
     */
    public static <T> Monoid<List<T>> listConcatWithPred(BiPredicate<List<T>, List<T>> pred) {
        return new Monoid<List<T>>(LinkedList::new, (a, b) -> {
            if (!pred.test(a, b)) {
                return a;
            }
            LinkedList<T> ret = new LinkedList<>();
            ret.addAll(a);
            ret.addAll(b);
            return ret;
        });
    }

    /**
     * Default monoid on strings -- id is empty string, operation is string concatenation.
     *
     * @return monoid on strings
     */
    public static Monoid<String> stringConcat() {
        return new Monoid<>(() -> "", String::concat);
    }

    /**
     * Monoid on boolean with operation || and identity element given.
     *
     * @param init identity element
     *
     * @return monoid on boolean
     */
    public static Monoid<Boolean> boolOr(boolean init) {
        return new Monoid<>(() -> init, (a, b) -> a || b);
    }

    /**
     * Monoid on boolean with operation && and identity element given.
     *
     * @param init identity element
     *
     * @return monoid on boolean
     */
    public static Monoid<Boolean> boolAnd(boolean init) {
        return new Monoid<>(() -> init, (a, b) -> a && b);
    }
}
