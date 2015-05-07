package ru.ifmo.ctddev.volhov.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author volhovm
 *         Created on 5/5/15
 */
public interface Person extends Remote {
    String getName() throws RemoteException;

    String getSurname() throws RemoteException;

    PersonId getId() throws RemoteException;
}
