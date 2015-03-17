package ru.ifmo.ctddev.volhov.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * This class represents abstraction of immutable method signature over the given method.
 * <p>
 * It can be constructed only from a method, later the given method can be retrieved with {@link #toMethod()}.
 * <p>
 * If there are two methods in the implemented class and superclass, and one overrides another, two instances
 * of {@link ru.ifmo.ctddev.volhov.implementor.MethodWrapper} would be equal and have similar hashcode. The
 * comparator checks if they have equal names and if their argument type lists are item-by-item equal.
 *
 * @author Volkhov Mykhail (volhovm)
 */
public class MethodWrapper {

    /**
     * The wrapped method
     */
    private final Method method;

    /**
     * List of type parameters of {@link #method}
     */
    private final Class[] types;

    /**
     * Name of #method
     */
    private final String name;

    /**
     * Creates the MethodWrapper instance of given method
     * @param method    the method to wrap around
     */
    public MethodWrapper(Method method) {
        this.method = method;
        types = method.getParameterTypes();
        name = method.getName();
    }

    /**
     * Retrieves a method from which this instance of {@link ru.ifmo.ctddev.volhov.implementor.MethodWrapper}
     * was built.
     * @return  method that was used to construct this entry
     */
    public Method toMethod() {
        return method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodWrapper that = (MethodWrapper) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return Arrays.equals(types, that.types);

    }

    @Override
    public int hashCode() {
        int result = types != null ? Arrays.hashCode(types) : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MethodWrapper{" +
                "method=" + method +
                '}';
    }
}
