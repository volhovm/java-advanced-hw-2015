package ru.ifmo.ctddev.volhov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author volhovm
 *         Created on 3/1/15
 */

public class Implementor implements Impler {
    private static final String TAB = "    ";

    public static void main(String[] args) {
        args = new String[]{
//                "java.lang.Readable"
//                "ru.ifmo.ctddev.volhov.implementor.TestInterface"
                "ru.ifmo.ctddev.volhov.implementor.TestAbstractClassB"
//                "java.util.ListResourceBundle"
//                "java.util.logging.Handler"
//                "java.util.AbstractSet"
        };

        if (args == null || args.length != 1 || args[0] == null) {
            System.out.println("arg[0] should be full name of the class/interface");
            return;
        }

        try {
            Class cls = Class.forName(args[0]);
            new Implementor().implement(cls, new File("src2/"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
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
//            if (!method.getDeclaringClass().equals(cls)) continue;
//            if (!Modifier.isAbstract(method.getModifiers())) continue;
            str.append("\n\n").append(TAB);
            str.append("@Override\n").append(TAB);
            for (Annotation annotation : method.getDeclaredAnnotations()) {
                str.append(annotation.toString())
                        .append("\n").append(TAB);
            }
            str.append(getModifiers(method.getModifiers()));
            str.append(method.getReturnType().getCanonicalName()).append(" ");
            Class[] paramTypes = method.getParameterTypes();
            Annotation[][] annotations = method.getParameterAnnotations();
            str.append(method.getName()).append('(');
            for (int i = 0; i < method.getParameterCount(); i++) {
                str.append(Arrays.stream(annotations[i]).map(Annotation::toString).collect(Collectors.joining(",")));
                str.append(paramTypes[i].getCanonicalName()).append(" p").append(i);
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

    private static Predicate<Method> abstr = a -> Modifier.isAbstract(a.getModifiers());
    private static Predicate<Object> nonnull = a -> a != null;
    private static Function<Method, Method> baseMethod = a -> {
                    try {
                        Method base = (a.getDeclaringClass().getSuperclass().getDeclaredMethod(a.getName()));
                        if (base.equals(a)) return a;
                        return Implementor.baseMethod.apply(base);
                    } catch (Exception e) {
                        return a;
                    }
                };

    private static Method[] getNeededMethods(Class cls) {
        ArrayList<Method> methods = Arrays.stream(cls.getMethods())
                .filter(abstr)
                .map(baseMethod)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        ArrayList<Method> overridden = Arrays.stream(cls.getMethods())
                .filter(abstr.negate())
                .map(baseMethod)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        ArrayDeque<Class> deque = new ArrayDeque<>();
        deque.push(cls);
        while (!deque.isEmpty()) {
            Class parent = deque.pop();
            if (parent.getSuperclass() != null) deque.addFirst(parent.getSuperclass());
            if (parent.getInterfaces().length != 0) Arrays.stream(parent.getInterfaces()).forEach(deque::addLast);
            Arrays.stream(parent.getMethods()).filter(abstr).map(baseMethod)
                    .filter(a -> !Modifier.isFinal(a.getModifiers())
                                    && !overridden.contains(a)
                                    && !methods.contains(a)
                    ) // bullshit
                    .forEach(methods::add);
            Arrays.stream(parent.getMethods()).filter(abstr.negate()).map(baseMethod).forEach(overridden::add);
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
            if (returnType.equals(byte.class)) return "(byte)0";
            if (returnType.equals(short.class)) return "(short)0";
            if (returnType.equals(long.class)) return "0L";
            if (returnType.equals(float.class)) return "0.0f";
            if (returnType.equals(double.class)) return "0.0d";
            if (returnType.equals(char.class)) return "'\u0000'";
            if (returnType.equals(boolean.class)) return "false";
            if (returnType.equals(void.class)) return "";
            return "0";
        } else if (returnType.isArray()) {
            return "new " + returnType.getCanonicalName().replace("[]", "[0]");
        } else return "null";
    }

    @Override
    public void implement(Class<?> token, File root) {
        try {
            String fileDir = root.getAbsolutePath() + "/"
                    + token.getPackage().getName().replace('.', '/')
                    + "/";
            if (!new File(fileDir).exists()) {
                System.out.println("Creating dir: " + fileDir);
                if (!new File(fileDir).mkdirs()) System.out.println("Failed to create dir");
            }
            String file = fileDir + token.getSimpleName() + "Impl.java";
            File fileClass = new File(file);
//            Thread.sleep(10000);
            Thread.sleep(1);
            System.out.println("Writing to " + fileClass);
            PrintWriter cout = new PrintWriter(fileClass);
            cout.write("package " + token.getPackage().getName() + ";\n\n");
            cout.write(getImplication(token));
            cout.close();
        } catch (FileNotFoundException e) {
            System.out.println("Все упало, пичалька");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
