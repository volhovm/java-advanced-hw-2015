package ru.ifmo.ctddev.volhov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;

/**
 * @author volhovm
 *         Created on 3/4/15
 */

public abstract class TestAbstractClassA implements TestInterface, Impler {
    private int hm;

    public TestAbstractClassA() {
        hm = 0;
    }

    public TestAbstractClassA(int hm) {
        this.hm = hm;
    }

    protected abstract String[][][][] protectedStringMethod(int param);

    private int some() {
        return 0;
    }

    @Override
    public void twiceOverriden() {
        ;
        ;
        ;
        ;
        ;
        ;
        ;
        ;//ebin :-DDDD
    }
}
