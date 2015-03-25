package ru.ifmo.ctddev.volhov.iterativeparallelism;

import java.util.*;

/**
 * @author volhovm
 *         Created on 3/18/15
 */
public class ConcurrentTest {
    public static void main(String[] args) throws InterruptedException {
        ParallelMapperImpl impl = new ParallelMapperImpl(5);
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("test");
        strings.add("mamkaIgnata");
        strings.add("your");
        strings.add("memchiki))");
        strings.add("sick");
        strings.add("#");
        try {
            System.out.println(impl.map(s -> "@" + s + "@", strings));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        impl.close();
    }

    private static void iterParTest() {
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
        for (int i = 0; i < 150; i++) {
            integers.add(rand.nextInt(10000));
        }
        IterativeParallelism par = new IterativeParallelism();
        LinkedList<String> linkedList = new LinkedList<>();
        System.out.println(par.map(3, linkedList, s -> s + " "));
        System.out.println(par.concat(3, strings));
        System.out.println(par.all(3, strings, s -> s.length() < 13));
        System.out.println(par.any(3, strings, s -> s.length() > 0));
        System.out.println(par.concat(3, par.map(3, strings, s -> "(" + s + ")")));
    }
}
