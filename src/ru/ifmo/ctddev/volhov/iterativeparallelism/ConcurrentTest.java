package ru.ifmo.ctddev.volhov.iterativeparallelism;

import java.util.ArrayList;

/**
 * @author volhovm
 *         Created on 3/18/15
 */
public class ConcurrentTest {
    public static void main(String[] args) {
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("test'");
        strings.add("mamkaignata");
        strings.add("your'");
        strings.add("daddy");
        strings.add("sucks");
        strings.add("Itest");
        strings.add("Imamkaignata");
        strings.add("Iyour");
        strings.add("Idaddy");
        strings.add("Isucks");
        IterativeParallelism par = new IterativeParallelism();
        try {
//            System.out.println(par.concat(3, strings));
//            System.out.println(par.all(3, strings, s -> s.length() > 4));
//            System.out.println(par.concat(3, par.map(3, strings, s -> s + "mapped!")));
            System.out.println(par.filter(10, strings, s -> s.length() > 5));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
