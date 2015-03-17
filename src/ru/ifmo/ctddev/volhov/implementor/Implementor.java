package ru.ifmo.ctddev.volhov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * The class represents the basic implementation of {@link info.kgeorgiy.java.advanced.implementor.JarImpler}
 * interface.
 * <p>
 * It can generate implementation (the <code>.java</code> file) for those interfaces/classes that can be
 * implemented/extended. All methods that should be overridden are present, their default return value
 * corresponds to javadoc primitive types description
 * (see <a href="http://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">Primitive Data Types</a>).
 * The class name consists of the name of class that is implemented plus "Impl" suffix.
 * <p>
 * It can also generate <code>.jar</code> archive with that certain class.
 * <p>
 * If it's impossible to generate implementation or jar for some reason, than
 * {@link info.kgeorgiy.java.advanced.implementor.ImplerException} is thrown.
 *
 * @author  Volkhov Mykhail (volhovm)
 * @see     info.kgeorgiy.java.advanced.implementor.Impler
 * @see     info.kgeorgiy.java.advanced.implementor.JarImpler
 * @see     info.kgeorgiy.java.advanced.implementor.ImplerException
 */
public class Implementor implements JarImpler {

    /**
     * Default value for indentation when generating {@code .java} file.
     */
    private static final String TAB = "    ";

    /**
     * Creates a simple entry of {@link Implementor} class.
     */
    public Implementor() {
    }

    /**
     * Produces jar archive with implementation of a class.
     * <p>
     * If flag in {@code args[0]} is "-jar", then {@link Implementor#main} writes to file, specified by string
     * in {@code args[2]}, the jar, which contains implementation of class given in {@code args[1]}.
     * <p>
     *
     * @param args  arguments for main corresponding to description.
     * @see         #implement(Class, File)
     * @see         #implementJar(Class, File)
     */
    public static void main(String[] args) {
        if (args != null && args.length == 3 && args[0] != null
                && args[0].equals("-jar") && args[1] != null && args[2] != null) {
            try {
                Class called = Class.forName(args[1]);
                File jarFile = new File(args[2]);
                if (!jarFile.exists()) {
                    System.err.println("jar not found: " + jarFile.getAbsolutePath());
                }
                new Implementor().implementJar(called, jarFile);
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found");
                e.printStackTrace();
            } catch (ImplerException e) {
                System.err.println("Exception while building jar:");
                e.printStackTrace();
            }
        } else {
            System.err.println("Format: -jar classname jarfile.jar");
        }
    }

    /**
     * Transforms given class to the string representation of it's implementation.
     * The name of new class is the old one, suffixed with "Impl".
     *
     * @param cls               class to get implementation of.
     * @return                  string, representing the implementation.
     * @throws ImplerException  when class can't be implemented (final, primitive, all constructors are private).
     */
    private static String getImplication(Class cls) throws ImplerException {
        StringBuilder str = new StringBuilder();

        // Pre-checks
        if (cls.isPrimitive()) throw new ImplerException(cls.getCanonicalName() + " is primitive - can't implement");
        if (Modifier.isFinal(cls.getModifiers())) throw new ImplerException("Can't implement final class");
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

    /**
     * Predicate, that returns true if given method is abstract.
     */
    private static Predicate<Method> abstr = a -> Modifier.isAbstract(a.getModifiers());

    /**
     * Predicate, that returns true if given method is not final.
     */
    private static Predicate<Method> nonFinal = a -> !Modifier.isFinal(a.getModifiers());

    /**
     * Retrieves an array of methods to override in the class, which has the given class as base.
     * <p>
     * It uses DFS to visit all superclasses and implemented interfaces (classes have the higher
     * priority) and collects the needed methods, using the {@link ru.ifmo.ctddev.volhov.implementor.MethodWrapper}
     * class as it represents the abstraction of method signature (that is needed to set the order "overridden by"
     * on the set of methods from different classes (in the inheritance tree) with the same signature).
     *
     * @param cls   class which non-implemented methods are needed to get.
     * @return      array of methods, that are needed to be implemented in the derivation of {@code cls}.
     * @see         ru.ifmo.ctddev.volhov.implementor.MethodWrapper
     */
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

    /**
     * Gets a compilable (that is can be used in java code) string of modifiers, divided by space character.
     *
     * @param mods  int, containing modifiers
     * @return      string, containing string representations of modifiers, divided by space character
     * @see         Method#getModifiers
     */
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

    /**
     * Gets a compilable (that is can be used in java code) string representation of default value,
     * specified by {@code returnType}.
     *
     * @param returnType    the given class
     * @return              string, containing the default value, that can be inserted into code and compiled.
     */
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
            if (token.getPackage() == null)
                throw new ImplerException(token.getCanonicalName() + " has null package and can't be implemented");
            String fileDir = root.getAbsolutePath() + File.separator
                    + token.getPackage().getName().replace('.', File.separatorChar)
                    + File.separator;
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
            ImplerException exception = new ImplerException("Can't create file/dir while implementing class");
            exception.addSuppressed(e);
            throw exception;
        }
    }

    @Override
    public void implementJar(Class<?> token, File jarFile) throws ImplerException {
        if (token == null) throw new NullPointerException("token is null");
        if (jarFile == null) throw new NullPointerException("jarFile is null");
        File currentDir = new File("./ImplTemp/");
        if (!currentDir.exists() && !currentDir.mkdir()) {
            throw new ImplerException("Can't create the ./ImplTemt/: " +
                    "currentdir " + (currentDir.exists() ? "" : "not ") + "exists, currentDir.mkdir() returns false");
        }
        implement(token, currentDir);
        String newTokenName = token.getSimpleName() + "Impl";
        String newTokenPackagePath = token.getPackage().getName().replace('.', File.separatorChar) + File.separator;
        System.out.println("newTokenPackagePath: " + newTokenPackagePath);
        int res = ToolProvider.getSystemJavaCompiler().run(null, null, null,
                currentDir + File.separator + newTokenPackagePath + newTokenName + ".java",
                "-cp",
                currentDir.getPath() + File.pathSeparator + System.getProperty("java.class.path"));
        if (res != 0) {
            currentDir.delete();
            throw new ImplerException("Can't compile the file, aborting");
        }
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, newTokenName);
        try (JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(jarFile.getAbsoluteFile()), manifest)) {
            try (FileInputStream inputStream = new FileInputStream(currentDir.getAbsolutePath() + File.separator + newTokenPackagePath + newTokenName + ".class")) {
                outputStream.putNextEntry(new JarEntry(newTokenPackagePath + newTokenName + ".class"));
                byte[] buffer = new byte[4096];
                int size;
                while (true) {
                    size = inputStream.read(buffer);
                    if (size <= 0) break;
                    outputStream.write(buffer, 0, size);
                }
                outputStream.closeEntry();
            }
        } catch (IOException e) {
            ImplerException exception = new ImplerException("Can't create jar output stream");
            exception.addSuppressed(e);
            throw exception;
        }
    }
}
