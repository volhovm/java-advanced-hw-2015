package ru.ifmo.ctddev.volhov.arrayset;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

/**
 * @author volhovm
 *         Created on 2/24/15
 */

public class ArraySetTest {
    public static void main(String[] args) throws InterruptedException {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[]{1,2, 7, 8, 9, 3, 18, 18, 2, 43};
        for (int anArr : arr) list.add(anArr);
//        ArraySet<Integer> set = new ArraySet<>(list, new Comparator<Integer>() {
//            @Override
//            public int compare(Integer integer, Integer t1) {
//                return integer.compareTo(t1);
//            }
//        }.reversed());
//        ArraySet<Integer> set = new ArraySet<>(list, new Comparator<Integer>() {
//            @Override
//            public int compare(Integer i1, Integer i2) {
//                final int c = Integer.compare(i1 % 2, i2 % 2);
//                return c != 0 ? c : Integer.compare(i1, i2);
//            }
//        });
        ArraySet<Integer> set = new ArraySet<>(list);
        ArraySet<Integer> dset = set.descendingSet();
        set.forEach(System.out::println);
        dset.forEach(System.out::println);
        ArraySet<Integer> dsubset = dset.headSet(3);
//        SortedSet<Integer> headSet = set.headSet(352252667);
//        headSet.forEach(System.out::println);
    }
}
