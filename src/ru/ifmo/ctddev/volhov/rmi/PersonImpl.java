package ru.ifmo.ctddev.volhov.rmi;

import java.rmi.RemoteException;

/**
 * @author volhovm
 *         Created on 5/5/15
 */
public class PersonImpl implements Person {
    private final String name, surname;
    private PersonId id;

    public PersonImpl(String name, String surname, String id) {
        this.name = name;
        this.surname = surname;
        this.id = new PersonId(id);
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public String getSurname() throws RemoteException {
        return surname;
    }

    @Override
    public PersonId getId() throws RemoteException {
        return id;
    }
}
