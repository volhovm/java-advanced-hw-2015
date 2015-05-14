package ru.ifmo.ctddev.volhov.rmi.banksystem;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This class is the basic implementation of {@link ru.ifmo.ctddev.volhov.rmi.banksystem.Bank} interface.
 * It uses hashmaps for adding/retrieving data, so insertions and searches have {@code O(1)} time complexity.
 *
 * @author volhovm
 *         Created on 5/5/15
 */
public class BankImpl extends UnicastRemoteObject implements Bank, Serializable {
    private static final long serialVersionUID = 6666L;
    HashMap<Person, HashMap<String, Account>> database = new HashMap<>();

    public BankImpl() throws RemoteException {
        super();
    }

    public BankImpl(int i) throws RemoteException {
        super(i);
    }

    private boolean containsPair(Person person, String accountId) throws RemoteException {
        return database.containsKey(person) && database.get(person).containsKey(accountId);
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
            database.get(person1).put(accountId, new Account(0));
        }
    }

    @Override
    public List<Person> searchPersonByName(String name, String surname, PersonType type) throws RemoteException {
        return database.keySet().stream().filter(p -> {
            boolean result = false;
            try {
                result = p.getName().equals(name) ||
                        p.getSurname().equals(surname) ||
                        p.getType() == type;
            } catch (RemoteException ignored) {}
            return result;
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> getAccounts(Person person) throws RemoteException {
        if (!database.containsKey(person)) {
            return null;
        }
        return new ArrayList<>(database.get(person).keySet());
    }

    @Override
    public Long getBalance(Person person, String accountId) throws RemoteException {
        if (!containsPair(person, accountId)) {
            return null;
        }
        return database.get(person).get(accountId).getAmount();
    }

    @Override
    public Long increaseBalance(long delta, Person person, String accountId) throws RemoteException {
        if (!containsPair(person, accountId)) {
            return null;
        }
        Account curr = database.get(person).get(accountId);
        return curr.increaseAmount(delta);
    }

    @Override
    public Long decreaseBalance(long delta, Person person, String accountId) throws RemoteException {
        if (!containsPair(person, accountId)) {
            return null;
        }
        Account curr = database.get(person).get(accountId);
        return curr.decreaseAmount(delta);
    }
}
