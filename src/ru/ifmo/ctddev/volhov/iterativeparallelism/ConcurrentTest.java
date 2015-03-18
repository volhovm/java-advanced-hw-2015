package ru.ifmo.ctddev.volhov.iterativeparallelism;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.Random;

/**
 * @author volhovm
 *         Created on 3/18/15
 */
public class ConcurrentTest {
    public static void main(String[] args) {
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
        for (int i = 0; i < 100000; i++) {
            integers.add(rand.nextInt());
        }
        try {
            IterativeParallelism par = new IterativeParallelism();
//            System.out.println(par.concat(3, strings));
//            System.out.println(par.all(3, strings, s -> s.length() > 4));
//            System.out.println(par.concat(3, par.map(3, strings, s -> s + "mapped!")));
//            System.out.println(par.filter(10, strings, s -> s.length() < 5));
//            System.out.println(par.maximum(5, strings, new Comparator<String>() {
//                @Override
//                public int compare(String s, String t1) {
//                    return Integer.compare(s.length(), t1.length());
//                }
//            }));
            for (int i = 1; i < 100; i++) {
                Integer realMin = integers.stream().reduce(Math::min).get();
                Integer realMax = integers.stream().reduce(Math::max).get();
                Integer parMin = par.minimum(i, integers, Integer::compare);
                Integer parMax = par.maximum(i, integers, Integer::compare);
                if (!Objects.equals(realMax, parMax)) {
                    System.out.println("ERROR_MAX on " + i + ": " + realMax + " != " + parMax);
                }
                if (!Objects.equals(realMin, parMin)) {
                    System.out.println("ERROR_MIN on " + i + ": " + realMin + " != " + parMin);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
