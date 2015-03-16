package ru.ifmo.ctddev.volhov.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author volhovm
 *         Created on 3/12/15
 */

public class MethodWrapper {
    private Method method;
    private Class[] types;
    private String name;

    public MethodWrapper(Method method) {
        this.method = method;
        types = method.getParameterTypes();
        name = method.getName();
    }

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
}
