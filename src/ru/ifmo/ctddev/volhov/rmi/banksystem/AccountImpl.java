package ru.ifmo.ctddev.volhov.rmi.banksystem;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author volhovm
 *         Created on 5/5/15
 */
public class AccountImpl extends UnicastRemoteObject implements Account {
    private long balance;
    private final String accountId;

    public AccountImpl(String id) throws RemoteException {
        super();
        accountId = id;
        balance = 0;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long newAmount) {
        balance = newAmount;
    }

    public long increaseBalance(long delta) {
        balance += delta;
        return balance;
    }

    public long decreaseBalance(long delta) {
        balance -= delta;
        return balance;
    }

    @Override
    public String getId() throws RemoteException {
        return accountId;
    }
}
