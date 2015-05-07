package ru.ifmo.ctddev.volhov.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author volhovm
 *         Created on 5/5/15
 */
public interface Account extends Remote {
    public String getId() throws RemoteException;

    public int getAmount() throws RemoteException;

    public void setAmount(int newAmount) throws RemoteException;
}
