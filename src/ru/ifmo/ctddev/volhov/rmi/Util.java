package ru.ifmo.ctddev.volhov.rmi;

import java.rmi.RemoteException;
import java.util.function.Predicate;

/**
 * This class represents utility set for operating with lambdas that
 * throw {@link java.rmi.RemoteException}.
 *
 * @author volhovm
 *         Created on 5/15/15
 */
public class Util {
    @FunctionalInterface
    public interface RemotePredicate<T> {
        Boolean apply(T in) throws RemoteException;
    }

    public static <T> Predicate<T> ignored(RemotePredicate<T> in, boolean defaultValue) {
        return p -> {
            boolean result = defaultValue;
            try {
                result = in.apply(p);
            } catch (RemoteException ignored) {}
            return result;
        };
    }

    public static <T> Predicate<T> ignored(RemotePredicate<T> in) {
        return ignored(in, false);
    }
}
