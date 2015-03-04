package ru.ifmo.ctddev.volhov.implementor;

import java.lang.reflect.Method;

/**
 * @author volhovm
 *         Created on 3/4/15
 */

public interface TestInterface {
    public int[][] doSomething(double[] a, Long b, Object c);
    String[] getStrings();
    static String staticMethod() { return "lol"; }
    default Method mdaaa(int input) { return null; }
    void voidMethod(Void heh);
    void twiceOverriden();
}
