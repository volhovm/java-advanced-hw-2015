package ru.ifmo.ctddev.volhov.rmi.banksystem;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author volhovm
 *         Created on 5/14/15
 */
public class LocalPerson implements Person {
    private static final long serialVersionUID = 123123L;
    private final String name, surname, id;

    public LocalPerson(String name, String surname, String id) throws RemoteException {
        super();
        this.name = name;
        this.surname = surname;
        this.id = id;
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
    public String getId() throws RemoteException {
        return id;
    }

    @Override
    public PersonType getType() throws RemoteException {
        return PersonType.Local;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LocalPerson that = (LocalPerson) o;

        if (!id.equals(that.id)) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }
        if (!surname.equals(that.surname)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + surname.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "LocalPerson{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
