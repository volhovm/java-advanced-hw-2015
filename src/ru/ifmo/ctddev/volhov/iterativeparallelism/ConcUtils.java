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
 * @author volhovm
 *         Created on 3/17/15
 */
public class ConcUtils {
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
                                accumulator = joiner.zero.get().get();
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
            N accumulator = joiner.zero.orElse(() -> (N) list.get(0)).get();
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

    public static <T> T foldl1(final Monoid<T> monoid, final List<T> list, int threads) {
        return foldl(monoid, Optional.empty(), list, threads);
    }

    public static <T> T foldl1(BinaryOperator<T> op, List<T> list, int threads) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("List must be nonempty"); // It's your fault
        }
        return foldl1(new Monoid<T>(op), list, threads);
    }

    public static <T, N> List<N> map(Function<? super T, ? extends N> foo, List<? extends T> list, int threads) {
        return ConcUtils.<T, List<N>>foldl(
                Monoid.<N>listConcat(),
                Optional.of(
                        new Pair<BiFunction<List<N>, T, List<N>>, Supplier<List<N>>>((xs, t) -> {
                            LinkedList<N> newList = new LinkedList<N>();
                            newList.addAll(xs);
                            newList.add(foo.apply(t));
                            return newList;
                        }, LinkedList::new)),
                (List<T>) list,
                threads);
    }
}
