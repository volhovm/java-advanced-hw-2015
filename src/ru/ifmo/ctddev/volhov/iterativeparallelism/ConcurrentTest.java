package ru.ifmo.ctddev.volhov.iterativeparallelism;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
import junit.framework.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author volhovm
 *         Created on 3/18/15
 */
public class ConcurrentTest {
    public static void main(String[] args) throws InterruptedException {
        iterParTest();
    }

    private static void parTest() throws InterruptedException {
        ParallelMapperImpl impl = new ParallelMapperImpl(5);
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("test");
        strings.add("mamkaIgnata");
        strings.add("your");
        strings.add("memchiki))");
        strings.add("sick");
        strings.add("#");
        System.out.println(impl.map(s -> "@" + s + "@", strings));
        impl.close();
    }

    private static void iterParTest() throws InterruptedException {
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("test");
        strings.add("mamkaIgnata");
        strings.add("your");
        strings.add("memchiki))");
        strings.add("sick");
        strings.add("#");
        strings.add("Itesting");
        strings.add("Imamkaignata");
        strings.add("Iyour");
        strings.add("Idaddy#");
        strings.add("Ivkontaba");
        ArrayList<Integer> integers = new ArrayList<>();
        Random rand = new Random(0x0BEEFDEAD);
        for (int i = 0; i < 10; i++) {
            integers.add(rand.nextInt(10));
        }
//        integers.stream().map(Object::toString).forEach(strings::add);
        ParallelMapper mapper = new ParallelMapperImpl(2);
        IterativeParallelism par = new IterativeParallelism(mapper);
        List<Integer> expected = integers.stream().map(i -> i * 2 + 3).collect(Collectors.toList());
        List<Integer> real = par.map(2, integers, i -> i * 2 + 3);
        for (int i = 0; i < expected.size(); i++) {
            if (!expected.get(i).equals(real.get(i))) {
                System.out.println(expected.get(i) + " != " + real.get(i));
            }
        }
        Assert.assertEquals(expected, real);
//        IterativeParallelism par = new IterativeParallelism();

//        long start = System.currentTimeMillis();
//        for (int threads = 1; threads < 15; threads++) {
//        System.out.println(par.map(4, strings, s -> "@" + s + "@"));
//        System.out.println(par.concat(4, strings));
//        System.out.println(par.maximum(4, strings, new Comparator<String>() {
//            @Override
//            public int compare(String s, String t1) {
//                return Integer.compare(s.length(), t1.length());
//            }
//        }));
//        System.out.println(par.all(4, strings, s -> s.length() < 13));
//        System.out.println(par.any(4, strings, s -> s.length() > 0));
//        System.out.println(par.concat(4, par.map(4, strings, s -> "(" + s + ")")));
//            long now = System.currentTimeMillis();
//            System.out.println(now - start);
//            start = now;
//        }
        mapper.close();
    }
}
