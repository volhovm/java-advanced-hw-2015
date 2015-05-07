package ru.ifmo.ctddev.volhov.rmi;

import info.kgeorgiy.java.advanced.implementor.InterfaceImplementorTest;

/**
 * @author volhovm
 *         Created on 5/5/15
 */
public class PersonId {
    private int series;
    private int number;

    public PersonId(String id) {
        String[] splitted = id.split(" ");
        series = Integer.parseInt(splitted[0]);
        number = Integer.parseInt(splitted[1]);
        if (series > 9999 || number > 999999) {
            throw new IllegalArgumentException("Malformed person ID");
        }
    }

    @Override
    public String toString() {
        return series + " " + number;
    }
}
