package ru.ifmo.ctddev.volhov.rmi.banksystem;

import static ru.ifmo.ctddev.volhov.rmi.Util.*;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is the basic implementation of {@link ru.ifmo.ctddev.volhov.rmi.banksystem.Bank} interface.
 * It uses hashmaps for adding/retrieving data, so insertions and searches have {@code O(1)} time complexity.
 *
 * @author volhovm
 *         Created on 5/5/15
 */
public class BankImpl extends UnicastRemoteObject implements Bank {
    HashMap<Person, HashMap<String, Account>> database = new HashMap<>();

    public BankImpl() throws RemoteException { super(); }

    private Person local(Person maybeRemote) throws RemoteException {
        if (maybeRemote.getType() == PersonType.Remote) {
            return new RemotePerson(maybeRemote.getName(), maybeRemote.getSurname(), maybeRemote.getId());
        }
        return maybeRemote;
    }

    private boolean containsPair(Person person, String accountId) throws RemoteException {
        return database.containsKey(local(person)) && database.get(local(person)).containsKey(accountId);
    }

    @Override
    public void addAccount(Person person, String accountId) throws RemoteException {
        Person person1;
        if (person.getType() == PersonType.Local) {
            person1 = new LocalPerson(person.getName(), person.getSurname(), person.getId());
        } else {
            person1 = new RemotePerson(person.getName(), person.getSurname(), person.getId());
        }
        database.putIfAbsent(person1, new HashMap<>());
        if (!database.get(person1).containsKey(accountId)) {
            database.get(person1).put(accountId, new AccountImpl(accountId));
        }
    }

    @Override
    public List<Person> searchPersonByName(String name, String surname, PersonType type) throws RemoteException {
        return database.keySet().stream().filter(ignored(p -> p.getName().equals(name) &&
                p.getSurname().equals(surname) &&
                p.getType() == type)).collect(Collectors.toList());
    }

    @Override
    public List<Account> getAccounts(Person person) throws RemoteException {
        if (!database.keySet().contains(local(person))) {
            return null;
        }
        return new ArrayList<>(database.get(local(person)).values());
    }

    @Override
    public Long getBalance(Person person, String accountId) throws RemoteException {
        if (!containsPair(local(person), accountId)) {
            return null;
        }
        return database.get(local(person)).get(accountId).getBalance();
    }
}
