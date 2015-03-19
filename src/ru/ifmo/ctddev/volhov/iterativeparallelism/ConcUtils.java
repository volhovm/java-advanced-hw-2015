package ru.ifmo.ctddev.volhov.iterativeparallelism;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class represents a set of static à la functional methods, that look almost like their analogs
 * from functional languages as Scala or Haskell, but also support parallel operations on them.
 * <p>
 * Any function accepting parameter {@code threads}, meaning the number of threads to do parallel work on, will
 * limit this parameter to maximal size of the given list, as the abstraction of running {@code m} parallel
 * threads on {@code n} items, where {@code m > n} is simply impractical, as some of them would do nothing by
 * Dirichlet's principle.
 * <p>
 * The main function is the private foldl with lots of arguments which simply does the following: it joins (with
 * {@link ru.ifmo.ctddev.volhov.iterativeparallelism.Monoid} joiner) the results of tasks performed in background:
 * each thread tries to fold the list of it's argument from left to right, using the {@code transition} argument
 * if it is {@code Optional#Some}, or uses the {@code joiner} argument (first monoid), assuming that {@code N=T} in
 * this case.
 * As the conditions of a given task (homework #6) do not usually supply the list of {@code T} with the neutral
 * element {@code T}, the {@code foldl} function imitates {@code reduce} function from Scala or {@code foldl1} from
 * Haskell -- it tries to interpret the first element of {@code List<T>} as {@code List<N>}. If the cast is not
 * successful, the {@link java.lang.ClassCastException} will be thrown. If the arguments of a function force
 * it to do impossible cast, than the {@link java.lang.IllegalArgumentException} will be thrown.
 * <p>
 * The other functions in this class are just implications of inner {@code foldl}. They rarely throw any exceptions,
 * excepting the cases when you want, for example, to foldl the empty list of type {@code T}, without giving the
 * function any id element. Another example is number of threads, less than 1.
 *
 * @author volhovm
 * @see ru.ifmo.ctddev.volhov.iterativeparallelism.Monoid
 */
public class ConcUtils {
    private ConcUtils() {
    }

    /**
     * Returns a usual left fold of the list, paralleled within number of threads given. If monoid is not
     * complete (it does not have the id element), then function tries to interpret the first parameter
     * in list as id. If this is the main use case, using the {@link #foldl1} would be simpler, as it
     * requires no monoid to create.
     *
     * @param monoid  the monoid, that's used to fold over list, function
     *                uses it's operation and mzero, if it's present
     * @param list    the list that this function folds
     * @param threads number of threads
     * @param <T>     type of elements containing in the list
     *
     * @return folded list
     * @see ru.ifmo.ctddev.volhov.iterativeparallelism.Monoid
     */
    public static <T> T foldl(final Monoid<T> monoid, final List<T> list, int threads) {
        return foldl(monoid, Optional.empty(), list, threads);
    }

    /**
     * Returns a left fold which doesn't use the id element of type {@code T}. See {@link #foldl} for more
     * detailed description.
     *
     * @param op      operation to fold list with
     * @param list    list to fold
     * @param threads number of threads
     * @param <T>     type of elements containing in the list
     *
     * @return folded list
     */
    public static <T> T foldl1(BinaryOperator<T> op, List<T> list, int threads) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("List must be nonempty"); // It's your fault
        }
        return foldl(new Monoid<T>(op), list, threads);
    }

    /**
     * Returns the given list mapped with the given function (that means result will be the list of initial list
     * elements on each one of which the given function was applied). Empty list will map into empty list. Number
     * of threads specify, how many threads will be mapping the sublists of this list simultaneously.
     *
     * @param foo     function that specifies the map transformation
     * @param list    list to map
     * @param threads number of threads
     * @param <T>     the initial type of elements in the list
     * @param <N>     the desired type of elements in the returned list
     *
     * @return mapped list
     */
    public static <T, N> List<N> map(Function<? super T, ? extends N> foo, List<? extends T> list, int threads) {
        return ConcUtils.<T, List<N>>foldl(
                Monoid.<N>listConcat(),
                Optional.of(
                        new Pair<BiFunction<List<N>, T, List<N>>, Supplier<List<N>>>((xs, t) -> {
//                            LinkedList<N> newList = new LinkedList<N>();
//                            newList.addAll(xs);
//                            newList.add(foo.apply(t));
//                            return newList;
                            xs.add(foo.apply(t));
                            return xs;
                        }, LinkedList::new)),
                (List<T>) list,
                threads);
    }

    private static <T, N> N foldl(final Monoid<N> joiner, Optional<Pair<BiFunction<N, T, N>, Supplier<N>>> transition,
                                  final List<T> list, int threads) {
        final int n = list.size();
        final boolean deep = transition.isPresent();
        if (threads < 1) {
            throw new IllegalArgumentException("Number of threads must be greater than zero");
        } else {
            if (threads > n) {
                threads = n;
            }
            N[] linearOrder = (N[]) new Object[threads];
            ArrayList<Thread> threadList = new ArrayList<>(threads);
            for (int i = 0; i < threads; i++) {
                final int finalI = i;
                final int flBound = i * (n / threads);
                final int frBound = i == threads - 1 ? n : (i + 1) * (n / threads);
                threadList.add(i, new Thread(new Runnable() {
                    final int index = finalI;
                    final int lBound = flBound;
                    final int rBound = frBound;

                    @Override
                    public void run() {
                        List<T> sublist = list.subList(lBound, rBound);
                        N accumulator = null;
                        if (deep) {
                            accumulator = transition.get().getValue().get();
                            for (int i = 0; i < sublist.size(); i++) {
                                accumulator = transition.get().getKey().apply(accumulator, sublist.get(i));
                            }
                        } else {
                            if (joiner.isComplete()) {
                                accumulator = joiner.id.get().get();
                            } else {
                                accumulator = (N) sublist.get(0);
                            }
                            for (int i = joiner.isComplete() ? 0 : 1; i < sublist.size(); i++) {
                                accumulator = joiner.op.apply(accumulator, (N) sublist.get(i));
                            }
                        }
                        linearOrder[index] = accumulator;
                    }
                }));
            }
            threadList.stream().forEach(Thread::start);
            N accumulator = joiner.id.orElse(() -> (N) list.get(0)).get();
            for (int i = 0; i < threads; i++) {
                try {
                    threadList.get(i).join();
                    accumulator = joiner.op.apply(accumulator, linearOrder[i]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return accumulator;
        }
    }


}
