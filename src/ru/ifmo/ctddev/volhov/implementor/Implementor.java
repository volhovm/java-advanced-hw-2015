package ru.ifmo.ctddev.volhov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import org.omg.CORBA_2_3.ORB;

import javax.annotation.processing.Completions;
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

@SuppressWarnings({"NullableProblems", "unchecked"})
public class Implementor implements Impler {
    private static final String TAB = "    ";

    public static void main(String[] args) throws ImplerException, NoSuchMethodException {
        Class cls =
//                Completions.class
//                java.lang.Readable.class
//                ru.ifmo.ctddev.volhov.implementor.TestInterface.class
                void.class
//                ru.ifmo.ctddev.volhov.implementor.TestAbstractClassB.class
//                java.util.ListResourceBundle.class
//                java.util.logging.Handler.class
//                javax.xml.bind.Element.class
//                BMPImageWriteParam.class
//                RelationNotFoundException.class
//                IIOException.class
//                ImmutableDescriptor.class
//                CachedRowSet.class
//                java.util.AbstractSet.class
//                javax.naming.ldap.LdapReferralException.class
//                ORB.class
                ;
        new Implementor().implement(cls, new File("src2/"));
    }


    private static String getImplication(Class cls) throws ImplerException {
        StringBuilder str = new StringBuilder();

        // Pre-checks
        if (Modifier.isFinal(cls.getModifiers())) throw new ImplerException("Can't implement final class");
        if (cls.getPackage() == null) throw new ImplerException("null package, maybe void.class or like that");
        if (cls.getDeclaredConstructors().length > 0 &&
                Arrays.stream(cls.getDeclaredConstructors())
                        .filter(i -> Modifier.isPrivate(i.getModifiers())).count() == cls.getDeclaredConstructors().length) {
            throw new ImplerException("All constructors are private, can't implement the class " + cls.getName());
        }

        // Header
        String className = cls.getSimpleName() + "Impl";
        str.append("@SuppressWarnings({").append('"').append("unchecked").append('"').append("})\n");
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
            str.append(") throws java.lang.Throwable {\n").append(TAB).append(TAB);
            str.append("super(");
            for (int i = 0; i < constructor.getParameterCount(); i++) {
                str.append("p").append(i);
                if (i != constructor.getParameterCount() - 1) str.append(", ");
            }
            str.append(");\n").append(TAB).append("}\n");
        }

        // Methods
        for (Method method : getNeededMethods(cls)) {
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
            str.append(") {").append("\n").append(TAB).append(TAB);
            str.append("return ").append(defaultValue(method.getReturnType()))
                    .append(";\n").append(TAB).append("}");
        }

        str.append("\n}\n");
        return str.toString();
    }

    private static Predicate<Method> abstr = a -> Modifier.isAbstract(a.getModifiers());
    private static Predicate<Method> nonFinal = a -> !Modifier.isFinal(a.getModifiers());

    private static Method[] getNeededMethods(Class cls) {
        HashSet<MethodWrapper> methods = Arrays.stream(cls.getDeclaredMethods())
                .filter(abstr)
                .filter(nonFinal)
                .map(MethodWrapper::new)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
        HashSet<MethodWrapper> overridden = Arrays.stream(cls.getDeclaredMethods())
                .filter(abstr.negate())
                .map(MethodWrapper::new)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
        ArrayDeque<Class> deque = new ArrayDeque<>();
        deque.push(cls);
        while (!deque.isEmpty()) {
            Class parent = deque.pop();
            if (parent.getSuperclass() != null) deque.addFirst(parent.getSuperclass());
            if (parent.getInterfaces().length != 0) Arrays.stream(parent.getInterfaces()).forEach(deque::addLast);
            Arrays.stream(parent.getDeclaredMethods())
                    .filter(abstr)
                    .filter(nonFinal)
                    .map(MethodWrapper::new)
                    .filter(a -> !overridden.contains(a) && !methods.contains(a))
                    .forEach(methods::add);
            Arrays.stream(parent.getDeclaredMethods())
                    .filter(abstr.negate())
                    .filter(nonFinal)
                    .map(MethodWrapper::new)
                    .forEach(overridden::add);
        }
        Method[] ret = new Method[methods.size()];
        methods.stream().map(MethodWrapper::toMethod).collect(HashSet::new, HashSet::add, HashSet::addAll).toArray(ret);
        return ret;
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
            if (token.getPackage() == null) throw new ImplerException(token.getCanonicalName() + " has null package and can't be implemented");
            String fileDir = root.getAbsolutePath() + "/"
                    + token.getPackage().getName().replace('.', '/')
                    + "/";
            if (!new File(fileDir).exists()) {
                if (!new File(fileDir).mkdirs()) System.out.println("Failed to create dir");
            }
            String file = fileDir + token.getSimpleName() + "Impl.java";
            File fileClass = new File(file);
            try (PrintWriter cout = new PrintWriter(fileClass)) {
                String out = "package " + token.getPackage().getName() + ";\n\n";
                out += getImplication(token);
                cout.write(out);
            } catch (ImplerException e) {
                fileClass.delete();
                throw e;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Все упало, пичалька");
            e.printStackTrace();
        }
    }

    //    @Override
    public void implementJar(Class<?> token, File jarFile) throws ImplerException {
        // FIXME implement implementJar method
    }
}
