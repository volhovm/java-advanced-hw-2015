package ru.ifmo.ctddev.volhov.arrayset;

import java.util.Arrays;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.SortedSet;

/**
 * @author volhovm
 *         Created on 2/24/15
 */

public class ArraySetTest {
    public static void main(String[] args) {
        String[] sarr = new String[]{"aaa", "ab", "long", "verylong", "ab", "ac", "ac", "bc", "zz"};
        NavigableSet set = new ArraySet(sarr, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return Integer.compare(s.length(), t1.length());
            }
        });
//                .descendingSet();
        System.out.println(set.lower("ar"));
        System.out.println(set.higher("aaoeuhtns"));
        System.out.println(set.comparator());
//        SortedSet subset = set.subSet("aoeuid", "aac");
        SortedSet subset = set.subSet("aac", "oeuaoeuaoeua");
        System.out.println(subset.first());
        System.out.println(subset.last());
    }
}
