package ru.ifmo.ctddev.volhov.iterativeparallelism;

import com.sun.jmx.remote.internal.ArrayQueue;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author volhovm
 *         Created on 3/25/15
 */
public class ParallelMapperImpl implements ParallelMapper {
    private class Pair<K, V> {
        K _1;
        V _2;
        public Pair(K _1, V _2) {
            this._1 = _1;
            this._2 = _2;
        }
    }
    // (task, isTaken)
    private volatile ArrayDeque<Consumer<Void>> queue;
    private Thread[] threads;
    public ParallelMapperImpl(int threads) {
        this.threads = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            this.threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (!queue.isEmpty()) {
                            Consumer<Void> data = queue.pop();
                            data.accept(null);
                        } else {
                            try {
                                wait();
                            } catch (InterruptedException ignored) {}
                        }
                    }
                }
            });
        }
    }
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        final int argsize = args.size();
        AtomicInteger counter = new AtomicInteger(0);
        ArrayList<R> retList = new ArrayList<>(args.size());
        for (int i = 0; i < argsize; i++) {
            final int ind = i;
            synchronized (queue) { //sync on volatile, do I neet that?
                queue.push((whatever) -> {
                    T elem;
                    synchronized (args) {
                        elem = args.get(ind);
                    }
                    R res = f.apply(elem);
                    synchronized (retList) {
                        retList.set(ind, res);
                    }
                    counter.incrementAndGet();
                });
            }
        }
        queue.notifyAll();
        while (true) {
            synchronized (counter) {
                if (counter.get() == argsize) break;
                queue.wait();
            }
        }
        return retList;
    }

    @Override
    public void close() throws InterruptedException {

    }
}
