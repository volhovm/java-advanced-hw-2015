package ru.ifmo.ctddev.volhov.implementor;

/**
 * @author volhovm
 *         Created on 3/4/15
 */

public abstract class TestAbstractClassB extends TestAbstractClassA {
    @Override
    protected String[][][][] protectedStringMethod(int param) {
        return new String[3][][][];
    }

    public abstract void abstractInB(float[][][][] params);
}
