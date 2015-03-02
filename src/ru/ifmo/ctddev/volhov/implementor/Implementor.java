package ru.ifmo.ctddev.volhov.implementor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.RandomAccess;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author volhovm
 *         Created on 3/1/15
 */

public class Implementor {
    private static final String TAB = "    ";

    public static void main(String[] args) {
//        args = new String[]{"java.lang.Readable"};
//        args = new String[]{"java.util.ListResourceBundle"};
//        args = new String[]{"java.util.logging.Handler"};
        args = new String[]{"java.util.Collections"};


        if (args == null || args.length != 1 || args[0] == null) {
            System.out.println("arg[0] should be full name of the class/interface");
            return;
        }

        String className = args[0];
        try {
            Class argClass = Class.forName(className);
            PrintWriter cout = new PrintWriter("src/ru/ifmo/ctddev/volhov/implementor/" + argClass.getSimpleName() + "Impl.java");
            cout.write("package ru.ifmo.ctddev.volhov.implementor; \n\n");
            cout.write(getImplication(argClass));
            cout.close();
        } catch (ClassNotFoundException | FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("OK");
    }

    private static String getModifiers(int mods) {
        StringBuilder str = new StringBuilder();
        if (Modifier.isPrivate(mods)) str.append("private ");
        else if (Modifier.isProtected(mods)) str.append("protected ");
        else if (Modifier.isPublic(mods)) str.append("public ");
        if (Modifier.isSynchronized(mods)) str.append("synchronized ");
        if (Modifier.isStatic(mods)) str.append("static ");
        if (Modifier.isFinal(mods)) str.append("final ");
        return str.toString();
    }

    private static String getImplication(Class cls) {
        StringBuilder str = new StringBuilder();

        // Header
        String className = cls.getSimpleName() + "Impl";
        str.append("class ").append(className);
        if (!cls.isInterface()) str.append(" extends ").append(cls.getName());
        else str.append(" implements ").append(cls.getName());
//        if (cls.getSuperclass() != null) {
//            str.append(" extends ").append(cls.getSuperclass().getName());
//        }
//        if (cls.getInterfaces().length != 0) {
//            str.append(" implements ");
//            str.append(Arrays.stream(cls.getInterfaces())
//                    .map(Class::getName)
//                    .collect(Collectors.joining(", ")));
//        }
        str.append(" {");

        // Fields
//        for (Field field : cls.getFields()) {
//            str.append(getModifiers(field.getModifiers()))
//                    .append(field.getType().getName()).append(" ")
//                    .append(field.getName()).append(";\n").append(TAB);
//        }

        // Constructors
//        for (Constructor constructor : cls.getConstructors()) {
//
//        }

        Method[] methods = getNeededMethods(cls);
        // Methods
        for (Method method : methods) {
            if (!method.getDeclaringClass().equals(cls)) continue;
            if (!Modifier.isAbstract(method.getModifiers())) continue;
            str.append("\n\n").append(TAB);
            str.append("@Override\n").append(TAB);
            for (Annotation annotation : method.getDeclaredAnnotations()) {
                str.append(annotation.toString())
                        .append("\n").append(TAB);
            }
            str.append(getModifiers(method.getModifiers()));
            str.append(normalizeType(method.getReturnType())).append(" ");
            Class[] paramTypes = method.getParameterTypes();
            Annotation[][] annotations = method.getParameterAnnotations();
            str.append(method.getName()).append('(');
            for (int i = 0; i < method.getParameterCount(); i++) {
                str.append(Arrays.stream(annotations[i]).map(Annotation::toString).collect(Collectors.joining(",")));
                str.append(paramTypes[i].getName()).append(" p").append(i);
                if (i != method.getParameterCount() - 1) str.append(", ");
            }
            str.append(")");
            if (method.getExceptionTypes().length != 0) {
                str.append(" throws ");
            }
            str.append(Arrays.stream(method.getExceptionTypes()).map(Class::getName)
                    .collect(Collectors.joining(", "))).append(" {")
                    .append("\n").append(TAB).append(TAB);
            str.append("return ").append(defaultValue(method.getReturnType()))
                    .append(";\n").append(TAB).append("}");
        }

        for (Class inner : cls.getClasses()) {
            if (!Modifier.isAbstract(inner.getModifiers())) continue;
            str.append("\n\n").append(TAB);
            str.append(getImplication(inner));
        }

        str.append("\n}\n");
        return str.toString();
    }

    private static Method[] getNeededMethods(Class cls) {
        ArrayList<Method> methods = Arrays.stream(cls.getDeclaredMethods())
                .filter(a -> Modifier.isAbstract(a.getModifiers())).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        Class parent = cls;
        while (true) {
            parent = parent.getSuperclass();
            if (parent == null) break;
            Arrays.stream(parent.getDeclaredMethods())
                    .filter(a -> Modifier.isAbstract(a.getModifiers()) && Modifier.isProtected(a.getModifiers()))
                    .forEach(methods::add);
        }
        Method[] ret = new Method[methods.size()];
        methods.toArray(ret);
        return ret;
    }

    private static String normalizeType(Class<?> returnType) {
        if (returnType.isArray()) {
//            Class<Array> arrayclass = (Class<Array>) returnType;
//            Array.
//            return returnType.getComponentType().getName();
            return returnType.getCanonicalName();
        } else if (returnType.isPrimitive()) {
            return returnType.getCanonicalName();
        } else return returnType.getName();
    }

    private static String defaultValue(Class<?> returnType) {
        if (returnType.isPrimitive()) {
            if (returnType.equals(char.class)) return "'0'";
            if (returnType.equals(boolean.class)) return "false";
            if (returnType.equals(void.class)) return "";
            return "0";
        } else if (returnType.isArray()) {
            return "Object[0]";
        } else return "null";

    }

}
