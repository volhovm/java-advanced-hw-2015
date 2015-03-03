package ru.ifmo.ctddev.volhov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;

/**
 * @author volhovm
 *         Created on 3/4/15
 */

public abstract class TestAbstractClassA implements TestInterface, Impler {
    protected abstract String[][][][] protectedStringMethod(int param);
    private int some() { return 0; }
}
