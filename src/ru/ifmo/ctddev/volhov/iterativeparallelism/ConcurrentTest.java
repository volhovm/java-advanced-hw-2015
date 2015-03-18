package ru.ifmo.ctddev.volhov.iterativeparallelism;

import java.util.ArrayList;
import java.util.Comparator;

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
        IterativeParallelism par = new IterativeParallelism();
        try {
            System.out.println(par.concat(3, strings));
            System.out.println(par.all(3, strings, s -> s.length() > 4));
            System.out.println(par.concat(3, par.map(3, strings, s -> s + "mapped!")));
            System.out.println(par.filter(10, strings, s -> s.length() < 5));
            System.out.println(par.maximum(5, strings, new Comparator<String>() {
                @Override
                public int compare(String s, String t1) {
                    return Integer.compare(s.length(), t1.length());
                }
            }));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
