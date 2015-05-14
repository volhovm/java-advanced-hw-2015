package ru.ifmo.ctddev.volhov.rmi.banksystem;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * This interface provides basic methods to implement bank system. The bank contains persons,
 * each person can have any number of accounts. Interface allows to add persons, add accounts
 * and operate with balances on accounts. It also implements {@link java.rmi.Remote} interface,
 * so implementations can be accessed via rmi.
 *
 * @author volhovm
 *         Created on 5/5/15
 */
public interface Bank extends Remote {
    /**
     * Add a person to bank. If the person is not registered in the bank, then
     * new empty account with zero balance. If person is registered and account
     * is not found, it's created with zero balance. If there's already an account
     * with specified id, nothing happens.
     *
     * @param person    person to add to bank system
     * @param accountId account id to add
     *
     * @throws RemoteException
     */
    public void addAccount(Person person, String accountId) throws RemoteException;

    /**
     * This method search for the person with specified personal name, surname and type.
     *
     * @param name          name of a person
     * @param surname       surname of a personz
     * @param type          type of a person
     *
     * @return list of persons that have specified name, surname and type
     * @throws RemoteException
     */
    public List<Person> searchPersonByName(String name, String surname, PersonType type) throws RemoteException;

    /**
     * This method returns bunch of id related to accounts specified person has.
     *
     * @param person person to retrieve accounts from
     *
     * @return list of id of accounts that person has or null if person is not registered
     * @throws RemoteException
     */
    public List<String> getAccounts(Person person) throws RemoteException;

    /**
     * This method returns current balance on account if found.
     *
     * @param accountId id of account
     * @param person    person that owns the account
     *
     * @return balance on account or null if account/person was not found
     * @throws RemoteException
     */
    public Long getBalance(Person person, String accountId) throws RemoteException;

    /**
     * This method increases balance on account specified by id.
     *
     * @param delta     how much credits should be added
     * @param accountId account id
     * @param person    person that owns the account
     *
     * @return new balance or null if account/person was not found
     * @throws RemoteException
     */
    public Long increaseBalance(long delta, Person person, String accountId) throws RemoteException;

    /**
     * This method decreases balance on account specified by id.
     *
     * @param delta     how much credits should be subtracted
     * @param accountId account id
     * @param person    person that owns the account
     *
     * @return new balance or null if account/person was not found
     * @throws RemoteException
     */
    public Long decreaseBalance(long delta, Person person, String accountId) throws RemoteException;
}
