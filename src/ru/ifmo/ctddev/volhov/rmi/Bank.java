package ru.ifmo.ctddev.volhov.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author volhovm
 *         Created on 5/5/15
 */
public interface Bank extends Remote {
    public Account createAccount(String id, Person person) throws RemoteException;

    public Account getAccount(String id) throws RemoteException;
}
