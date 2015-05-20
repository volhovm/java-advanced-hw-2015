package ru.ifmo.ctddev.volhov.rmi.banksystem;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface, that describes the bank account. Implement {@link java.rmi.Remote},
 * so can be used via rmi. Has it's own identity. Mutable.
 *
 * @see ru.ifmo.ctddev.volhov.rmi.banksystem.Bank
 * @author volhovm
 *         Created on 5/15/15
 */
public interface Account extends Remote {
    /**
     * Return current balance on account.
     * @return balance on account
     * @throws RemoteException
     */
    public long getBalance() throws RemoteException;

    /**
     * Set balance.
     * @param balance  new balance
     * @throws RemoteException
     */
    public void setBalance(long balance) throws RemoteException;

    /**
     * Add given number of credits to current balance.
     * @param delta number of credits to add
     * @return      new balance
     * @throws RemoteException
     */
    public long increaseBalance(long delta) throws RemoteException;

    /**
     * Subtract given number of credits from current balance.
     * @param delta number of credits to subtract
     * @return      new balance
     * @throws RemoteException
     */
    public long decreaseBalance(long delta) throws RemoteException;

    /**
     * Get account identity
     * @return id of account
     * @throws RemoteException
     */
    public String getId() throws RemoteException;
}
