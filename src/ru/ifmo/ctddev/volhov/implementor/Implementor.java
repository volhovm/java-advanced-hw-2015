package ru.ifmo.ctddev.volhov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author volhovm
 *         Created on 3/1/15
 */

public class Implementor implements Impler {
    private static final String TAB = "    ";

    public static void main(String[] args) throws ImplerException, NoSuchMethodException {
        Class cls =
//                    java.lang.Readable.class
//                    ru.ifmo.ctddev.volhov.implementor.TestInterface.class
//                    void.class
//                    ru.ifmo.ctddev.volhov.implementor.TestAbstractClassB.class
//                    java.util.ListResourceBundle.class
//                    java.util.logging.Handler.class
//                    javax.xml.bind.Element.class
//                    BMPImageWriteParam.class
//                RelationNotFoundException.class
//                IIOException.class
                ImmutableDescriptor.class
//                    CachedRowSet.class
//                    java.util.AbstractSet.class
//                    javax.naming.ldap.LdapReferralException.class
                ;
        HashSet<MethodWrapper> hashSet = new HashSet<>();
        hashSet.add(new MethodWrapper(ImmutableDescriptor.class.getDeclaredMethod("getFieldNames")));
        boolean contains = hashSet.contains(new MethodWrapper(Descriptor.class.getMethod("getFieldNames")));
        new Implementor().implement(cls, new File("src2/"));
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
        str.append(" {");

        // Constructors
        for (Constructor constructor : cls.getConstructors()) {
            str.append("\n\n").append(TAB);
            str.append(getModifiers(constructor.getModifiers()));
            str.append(className).append("(");
            Class[] paramTypes = constructor.getParameterTypes();
            for (int i = 0; i < constructor.getParameterCount(); i++) {
                str.append(paramTypes[i].getCanonicalName()).append(" p").append(i);
                if (i != constructor.getParameterCount() - 1) str.append(", ");
            }
            str.append(") {\n").append(TAB).append(TAB);
            str.append("super(");
            for (int i = 0; i < constructor.getParameterCount(); i++) {
                str.append("p").append(i);
                if (i != constructor.getParameterCount() - 1) str.append(", ");
            }
            str.append(");\n").append(TAB).append("}\n");
        }

        Method[] methods = getNeededMethods(cls);
        // Methods
        for (Method method : methods) {
            str.append("\n\n").append(TAB);
            str.append("@Override\n").append(TAB);
            str.append(getModifiers(method.getModifiers()));
            str.append(method.getReturnType().getCanonicalName()).append(" ");
            Class[] paramTypes = method.getParameterTypes();
            str.append(method.getName()).append('(');
            for (int i = 0; i < method.getParameterCount(); i++) {
                str.append(paramTypes[i].getCanonicalName()).append(" p").append(i);
                if (i != method.getParameterCount() - 1) str.append(", ");
            }
            str.append(")");
//            if (method.getExceptionTypes().length != 0) {
//                str.append(" throws ");
//            }
//            str.append(Arrays.stream(method.getExceptionTypes()).map(Class::getName)
//                    .collect(Collectors.joining(", ")))
            str.append(" {").append("\n").append(TAB).append(TAB);
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
    private static Predicate<Method> nonFinal = a -> !Modifier.isFinal(a.getModifiers());
    private static Predicate<Object> nonnull = a -> a != null;

    private static Method[] getNeededMethods(Class cls) {
        HashSet<MethodWrapper> methods = Arrays.stream(cls.getDeclaredMethods())
                .filter(abstr)
                .filter(nonFinal)
                .map(MethodWrapper::new)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
        HashSet<MethodWrapper> overridden = Arrays.stream(cls.getMethods())
                .filter(abstr.negate())
                .map(MethodWrapper::new)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
        ArrayDeque<Class> deque = new ArrayDeque<>();
        deque.push(cls);
        while (!deque.isEmpty()) {
            Class parent = deque.pop();
            if (parent.getSuperclass() != null) deque.addFirst(parent.getSuperclass());
            if (parent.getInterfaces().length != 0) Arrays.stream(parent.getInterfaces()).forEach(deque::addLast);
            Arrays.stream(parent.getMethods())
                    .filter(abstr)
                    .filter(nonFinal)
                    .map(MethodWrapper::new)
                    .filter(a -> !overridden.contains(a) && !methods.contains(a))
                    .forEach(methods::add);
            Arrays.stream(parent.getMethods())
                    .filter(abstr.negate())
                    .filter(nonFinal)
                    .map(MethodWrapper::new)
                    .forEach(overridden::add);
        }
        Method[] ret = new Method[methods.size()];
        methods.stream().map(MethodWrapper::toMethod).collect(HashSet::new, HashSet::add, HashSet::addAll).toArray(ret);
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
    public void implement(Class<?> token, File root) throws ImplerException {
        try {
            if (token.getPackage() == null) throw new ImplerException("null package, maybe void.class or like that");
            String fileDir = root.getAbsolutePath() + "/"
                    + token.getPackage().getName().replace('.', '/')
                    + "/";
            if (!new File(fileDir).exists()) {
//                System.out.println("Creating dir: " + fileDir);
                if (!new File(fileDir).mkdirs()) System.out.println("Failed to create dir");
            }
            String file = fileDir + token.getSimpleName() + "Impl.java";
            File fileClass = new File(file);
//            Thread.sleep(10000);
            Thread.sleep(1);
//            System.out.println("Writing to " + fileClass);
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

    //    @Override
    public void implementJar(Class<?> token, File jarFile) throws ImplerException {
        // FIXME implement implementJar method
    }
}
