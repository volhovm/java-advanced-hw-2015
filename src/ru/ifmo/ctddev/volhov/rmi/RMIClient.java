package ru.ifmo.ctddev.volhov.rmi;

import ru.ifmo.ctddev.volhov.rmi.banksystem.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

/**
 * @author volhovm
 *         Created on 5/13/15
 */
public class RMIClient {
    private static final String usage = "Usage: name surname id accountId [+|-]num\nid and accountId without spaces";

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
            System.err.println(usage);
            return;
        }
        try {
            Bank bank = (Bank) Naming.lookup("rmi://localhost/bank");
            Person person = new LocalPerson(name, surname, id);
            List<Person> personList = bank.searchPersonByName(name, surname, PersonType.Local);
            if (personList.isEmpty()) {
                bank.addAccount(person, accountId);
                System.out.println("Created new account with id " + accountId + " with balance 0");
                return;
            }
            if (personList.stream().noneMatch(p -> {
                boolean ret = false;
                try {
                    ret = p.getId().equals(person.getId());
                } catch (RemoteException ignored) {}
                return ret;
            })) {
                System.out.println("Your id doesn't match, sorry");
                return;
            }
            System.out.println("Your current account list: \nID, Balance");
            bank.getAccounts(person).forEach(p -> {
                String balance = "Cannot retrieve balance";
                try {
                    balance = bank.getBalance(person, p).toString();
                } catch (RemoteException ignored) {}
                System.out.println(p + "   " + balance);
            });
            System.out.println();
            if (!bank.getAccounts(person).contains(accountId)) {
                bank.addAccount(person, accountId);
                System.out.println("Created new account with id " + accountId + " with balance 0");
                return;
            }
            Long balance = bank.getBalance(person, accountId);
            System.out.println("Balance on account " + accountId + " before update: " + balance);
            if (deltaChar == '+') {
                bank.increaseBalance(delta, person, accountId);
            } else {
                bank.decreaseBalance(delta, person, accountId);
            }
            System.out.println("Current balance: " + bank.getBalance(person, accountId));
        } catch (RemoteException | MalformedURLException | NotBoundException e) {
            e.printStackTrace();
        }
//        System.out.println("=== Done ===");
    }
}
