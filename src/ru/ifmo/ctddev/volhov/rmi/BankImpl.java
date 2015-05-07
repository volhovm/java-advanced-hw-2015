package ru.ifmo.ctddev.volhov.rmi;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author volhovm
 *         Created on 5/5/15
 */
public class BankImpl implements Bank {
    private ConcurrentHashMap<String, Account> database;

    public BankImpl() {
        this.database = new ConcurrentHashMap<>();
    }

    @Override
    public Account createAccount(String id, Person person) {
        Account account = new AccountImpl(id, person);
        database.put(id, account);
        return account;
    }

    @Override
    public Account getAccount(String id) throws RemoteException {
        return database.get(id);
    }
}
