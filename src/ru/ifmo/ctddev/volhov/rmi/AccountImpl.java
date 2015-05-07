package ru.ifmo.ctddev.volhov.rmi;

import java.rmi.RemoteException;

/**
 * @author volhovm
 *         Created on 5/5/15
 */
public class AccountImpl implements Account {
    private int amount;
    private final String id;
    private Person owner;

    public AccountImpl(String id, Person owner) {
        this.id = id;
        this.owner = owner;
    }

    @Override
    public String getId() throws RemoteException {
        return id;
    }

    @Override
    public int getAmount() throws RemoteException {
        return amount;
    }

    @Override
    public void setAmount(int newAmount) throws RemoteException {
        amount = newAmount;
    }
}
