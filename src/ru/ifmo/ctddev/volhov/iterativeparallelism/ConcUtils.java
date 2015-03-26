package ru.ifmo.ctddev.volhov.iterativeparallelism;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public static <T> T foldl(final Monoid<T> monoid, final List<? extends T> list, int threads)
            throws InterruptedException {
        return foldl(monoid, lst -> {
            T accumulator;
            if (monoid.isComplete()) {
                accumulator = monoid.id.get().get();
            } else {
                accumulator = list.get(0);
            }
            for (int i = monoid.isComplete() ? 0 : 1; i < list.size(); i++) {
                accumulator = monoid.op.apply(accumulator, list.get(i));
            }
            return accumulator;
        }, Optional.empty(), list, threads);
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
    public static <T> T foldl1(BinaryOperator<T> op, List<? extends T> list, int threads) throws InterruptedException {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("List must be nonempty"); // It's your fault
        }
        return foldl(new Monoid<>(op), list, threads);
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
    public static <T, N> List<N> map(Function<? super T, ? extends N> foo, List<? extends T> list, int threads)
            throws InterruptedException {
        return ConcUtils.foldl(
                Monoid.<N>listConcat(), //List<N>
                (List<T> lst) -> lst.stream().map(foo).collect(Collectors.toList()),
                Optional.empty(),
                list,
                threads);
    }

    /**
     * Folds sublists of given list, got from applying function {@code mapper}, with the monoid function and
     * identity element (if monoid is complete). It also uses the given number of threads to operate over sublists
     * simultaneously.
     *
     * @param joiner  monoid for joining sublists
     * @param mapper  function to process sublist
     * @param list    list to map
     * @param threads number of threads
     * @param <T>     type of elements of given list
     * @param <N>     type or functions range
     *
     * @return folded list
     */
    public static <T, N> N concatmap(Monoid<N> joiner, Function<List<T>, N> mapper, List<? extends T> list,
                                     int threads) throws InterruptedException {
        return foldl(joiner, mapper, Optional.empty(), list, threads);
    }

    @SuppressWarnings("unchecked")
    static <T, N> N foldl(final Monoid<N> joiner,
                          Function<List<T>, N> transition,
                          Optional<ParallelMapper> mapper,
                          final List<? extends T> list,
                          int threads) throws InterruptedException {
        final int n = list.size();
        if (threads < 1) {
            throw new IllegalArgumentException("Number of threads must be greater than zero");
        } else {
            if (threads > n) {
                threads = n;
            }

            final List<N> linearOrder = new ArrayList<N>(threads);    // ochen' jal'
            class SubWorker {
                final int i;
                final List<? extends T> sublist;

                SubWorker(int i, List<? extends T> sublist) {
                    this.i = i;
                    this.sublist = sublist;
                }

                public N apply() {
                    return transition.apply((List<T>) this.sublist);
                }
            }
            ArrayList<SubWorker> workers = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                int flBound = i * (n / threads);
                int frBound = i == threads - 1 ? n : (i + 1) * (n / threads);
                workers.add(i, new SubWorker(i, list.subList(flBound, frBound)));
            }
            if (mapper.isPresent()) {
                List<N> temp = mapper.get().<List<T>, N>map(transition,
                        workers.stream().map(a -> (List<T>) a.sublist).collect(Collectors.toList()));
                linearOrder.clear();
                linearOrder.addAll(temp);
            } else {
                ArrayList<Thread> threadList = new ArrayList<>(threads);
                for (int i = 0; i < threads; i++) {
                    final int finalI = i;
                    threadList.add(i, new Thread(new Runnable() {
                        final int fi = finalI;

                        @Override
                        public void run() {
                            N res = workers.get(fi).apply();
                            synchronized (linearOrder) {
                                linearOrder.add(fi, res);
                            }
                        }
                    }));
                }
                threadList.stream().forEach(Thread::start);
                for (int i = 0; i < threads; i++) {
                    threadList.get(i).join();
                }
            }

            N accumulator = joiner.id.orElse(() -> (N) list.get(0)).get();
            for (int i = 0; i < threads; i++) {
                accumulator = joiner.op.apply(accumulator, linearOrder.get(i));
            }
            return accumulator;
        }
    }


}
