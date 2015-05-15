package ru.ifmo.ctddev.volhov.rmi;

import ru.ifmo.ctddev.volhov.rmi.banksystem.*;

import static ru.ifmo.ctddev.volhov.rmi.Util.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author volhovm
 *         Created on 5/13/15
 */
@SuppressWarnings("ConstantConditions")
public class RMIClient {
    private static final String usage = "Usage: name surname id accountId [+|-]num\nid and accountId without spaces";

    private static void dumpState(Bank bank, Person person) throws RemoteException {
        System.out.println(person.getName() + " " + person.getSurname());
        System.out.println("Your current account list: \nID, Balance");
        bank.getAccounts(person).forEach(p -> {
            String balance = "Cannot retrieve balance";
            String id = "??";
            try {
                balance = String.valueOf(p.getBalance());
                id = p.getId();
            } catch (RemoteException ignored) {}
            System.out.println(id + "   " + balance);
        });
        System.out.println();
    }

    public static void main(String[] args) {
        if (args == null || args.length != 5
                || Arrays.stream(args).anyMatch(a -> a == null)
                || Arrays.stream(args).anyMatch(a -> a.length() == 0)
                || args[4].length() < 2) {
            System.err.println(usage);
            return;
        }
        String name = args[0];
        String surname = args[1];
        String id = args[2];
        String accountId = args[3];
        char deltaChar;
        long delta;
        try {
            deltaChar = args[4].charAt(0);
            if (deltaChar != '+' && deltaChar != '-') {
                throw new NumberFormatException();
            }
            delta = Long.parseLong(args[4].substring(1, args[4].length()));
        } catch (NumberFormatException e) {
            System.out.println("WRONG NUMBER FORMAT");
            System.err.println(usage);
            return;
        }
        try {
//            HashSet<Person> persons = new HashSet<>();
//            persons.add(new RemotePerson("a", "b", "c"));
//            System.out.println(persons.contains(new RemotePerson("a", "b", "c")));
            // Create bank and person
            PersonType currentType = PersonType.Local;
            Bank bank = (Bank) Naming.lookup("rmi://localhost/bank");
            Person person;
            if (currentType == PersonType.Remote) {
                person = new RemotePerson(name, surname, id);
            } else {
                person = new LocalPerson(name, surname, id);
            }
            // Retrieve list of persons having this name, surname, from bank
            List<Person> personList = bank.searchPersonByName(name, surname, currentType);
            // Create new Account with 0 and exit if list is empty
            if (personList.isEmpty()) {
                bank.addAccount(person, accountId);
                System.out.println("Created new account with id " + accountId + " with balance 0");
                if (currentType == PersonType.Remote && !UnicastRemoteObject.unexportObject(person, false)) {
                    System.err.println("Failed to unexport person");
                }
                return;
            }
            // Check if there's person with id matching given, else exit
            if (personList.stream().noneMatch(ignored(p -> p.getId().equals(person.getId())))) {
                System.out.println("Your id doesn't match, sorry");
                if (currentType == PersonType.Remote && !UnicastRemoteObject.unexportObject(person, false)) {
                    System.err.println("Failed to unexport person");
                }
                return;
            }
            // Update balance
            List<Account> accounts = bank.getAccounts(person);
            Optional<Account> current = accounts.stream().filter(ignored(p -> p.getId().equals(accountId))).findFirst();
            if (!current.isPresent()) {
                bank.addAccount(person, accountId);
                System.out.println("Created new account with id " + accountId + " with balance 0");
                if (currentType == PersonType.Remote && !UnicastRemoteObject.unexportObject(person, false)) {
                    System.err.println("Failed to unexport person");
                }
                return;
            }
            Account account = current.get();
            Long balance = account.getBalance();
            System.out.println("Balance on account " + accountId + " before update: " + balance);
            if (deltaChar == '+') {
                account.increaseBalance(delta);
            } else {
                account.decreaseBalance(delta);
            }
            System.out.println("Current balance: " + bank.getBalance(person, accountId));

            dumpState(bank, person);

            if (currentType == PersonType.Remote && !UnicastRemoteObject.unexportObject(person, false)) {
                System.err.println("Failed to unexport person");
            }
        } catch (RemoteException | MalformedURLException | NotBoundException e) {
            e.printStackTrace();
        }
//        System.out.println("=== Done ===");
    }
}
