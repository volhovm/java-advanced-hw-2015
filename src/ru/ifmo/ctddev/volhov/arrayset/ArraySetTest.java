package ru.ifmo.ctddev.volhov.arrayset;

import java.util.*;

/**
 * @author volhovm
 *         Created on 2/24/15
 */

public class ArraySetTest {
    public static void main(String[] args) throws InterruptedException {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[]{-1318201610, -1051344301, 1466991001, 1712546160, 343034501, -1954638715, -323163407, 893047611};
        for (int anArr : arr) list.add(anArr);
//        ArraySet<Integer> set = new ArraySet<>(list, new Comparator<Integer>() {
//            @Override
//            public int compare(Integer i1, Integer i2) {
//                final int c = Integer.compare(i1 % 2, i2 % 2);
//                return c != 0 ? c : Integer.compare(i1, i2);
//            }
//        });
        ArraySet<Integer> set = new ArraySet<>(list);
        SortedSet<Integer> headSet = set.headSet(-1318201610, true);
        headSet.forEach(System.out::println);
//        Integer[] arr = new Integer[list.size()];
//        list.toArray(arr);
//        ArraySet<Integer> set = new ArraySet<>(list, new Comparator<Integer>() {
//            @Override
//            public int compare(Integer integer, Integer t1) {
//                return 0;
//            }
//        });
//        System.out.println(set.headSet(-228228).size());
//        System.out.println(set.contains(new Integer(10000)));
//        String[] sarr = new String[]{"aaa", "ab", "long", "verylong", "ab", "ac", "ac", "bc", "zz"};
//        NavigableSet set = new ArraySet(sarr, new Comparator<String>() {
//            @Override
//            public int compare(String s, String t1) {
//                return Integer.compare(s.length(), t1.length());
//            }
//        });
//                .descendingSet();
//        System.out.println(set.lower("ar"));
//        System.out.println(set.higher("aaoeuhtns"));
//        System.out.println(set.comparator());
//        SortedSet subset = set.subSet("aoeuid", "aac");
//        SortedSet subset = set.subSet("aac", "oeuaoeuaoeua");
//        System.out.println(subset.first());
//        System.out.println(subset.last());
    }
}
