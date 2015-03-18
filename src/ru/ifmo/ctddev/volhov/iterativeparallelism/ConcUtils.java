package ru.ifmo.ctddev.volhov.iterativeparallelism;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author volhovm
 *         Created on 3/17/15
 */
public class ConcUtils {
    public static <T> T foldl(final Monoid<T> monoid, final List<T> list, int threads) {
        final int n = list.size();
        if (threads == 1) {
            T accumulator = null;
            if (monoid.isComplete()) {
                accumulator = monoid.zero.orElseGet(() -> null).get();
            } else {
                accumulator = list.get(0);
            }
            for (int i = monoid.isComplete() ? 0 : 1; i < list.size(); i++) {
                accumulator = monoid.op.apply(accumulator, list.get(i));
            }
            return accumulator;
        } else {
            if (threads > n) {
                threads = n;
            }
            T[] linearOrder = (T[]) new Object[threads];
            ArrayList<Thread> threadList = new ArrayList<>(threads);
            for (Integer i = 0; i < threads; i++) {
                final int finalI = i;
                final int flBound = i * (n / (threads - 1));
                final int frBound = (i + 1) * (n / (threads - 1)) >= n ? n : (i + 1) * (n / (threads - 1));
                threadList.add(i, new Thread(new Runnable() {
                    final int index = finalI;
                    final int lBound = flBound;
                    final int rBound = frBound;

                    @Override
                    public void run() {
                        T current;
                        current = foldl(monoid, list.subList(lBound, rBound), 1);
                        linearOrder[index] = current;
                    }
                }));
            }
            threadList.stream().forEach(Thread::start);
            T accumulator = monoid.zero.orElse(() -> list.get(0)).get();
            for (int i = monoid.isComplete() ? 0 : 1; i < threads; i++) {
                try {
                    threadList.get(i).join();
                    System.out.println("Joined thread #" + i);
                    System.out.println("Applying lambda to (acc/l[i]): " + accumulator + " " + linearOrder[i]);
                    accumulator = monoid.op.apply(accumulator, linearOrder[i]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return accumulator;
        }
    }

    public static <T> T foldl1(BinaryOperator<T> op, List<T> list, int threads) {
        if (list.isEmpty()) throw new IllegalArgumentException("List must be nonempty"); // It's your fault
        return foldl(new Monoid<T>(op), list, threads);
    }

    public static <T, N> List<N> map(Function<? super T, ? extends N> foo, List<? extends T> list, int threads) {
        return foldl(
                Monoid.listConcat(),
                list.stream().map(a -> {
                    ArrayList<N> ret = new ArrayList<>();
                    ret.add(foo.apply(a));
                    return ret;
                }).collect(Collectors.toList()),
                threads);
    }
}
