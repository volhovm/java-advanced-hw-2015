package ru.ifmo.ctddev.volhov.rmi.banksystem;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * @author volhovm
 *         Created on 5/5/15
 */
public class Account implements Serializable {
    private static final long serialVersionUID = 88888888L;
    private long amount;

    public Account(int initAmount) {
        amount = initAmount;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long newAmount) {
        amount = newAmount;
    }

    public long increaseAmount(long delta) {
        amount += delta;
        return amount;
    }

    public long decreaseAmount(long delta) {
        amount -= delta;
        return amount;
    }
}
