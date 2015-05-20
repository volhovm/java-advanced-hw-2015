package ru.ifmo.ctddev.volhov.rmi.banksystem;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Interface for operating with person as with bank user.
 *
 * @see ru.ifmo.ctddev.volhov.rmi.banksystem.Bank
 * @author volhovm
 *         Created on 5/5/15
 */
public interface Person extends Remote {
    /**
     * Returns person's name
     * @return name of the person
     * @throws RemoteException
     */
    String getName() throws RemoteException;

    /**
     * Returns second person's name (surname)
     * @return surname of the person
     * @throws RemoteException
     */
    String getSurname() throws RemoteException;

    /**
     * Returns person's identity (passport)
     * @return identity of the person
     * @throws RemoteException
     */
    String getId() throws RemoteException;

    /**
     * Returns the type of person object -- local or remote
     * @return person's type
     * @throws RemoteException
     */
    PersonType getType() throws RemoteException;
}
