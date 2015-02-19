package ru.ifmo.ctddev.volhov.walk;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author volhovm
 *         Created on 2/18/15
 */

public class RecursiveWalk {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("format: Walk input output");
            return;
        }
        BufferedReader br;
        OutputStreamWriter out;
        try {
            br = new BufferedReader(new FileReader(args[0]));
        } catch (FileNotFoundException e) {
            System.out.println("Can't open input file" + e.getMessage());
            return;
        }
        try {
            out = new OutputStreamWriter(new FileOutputStream(args[1]));
        } catch (FileNotFoundException e) {
            System.out.println("Can't open output file: " + e.getMessage());
            return;
        }
        System.out.println("Starting with home dir: " + System.getProperty("user.dir"));
        StringBuilder stringBuilder = new StringBuilder();
        int current = 0;
        while (current != -1) {
            try {
                current = br.read();
            } catch (IOException e) {
                System.out.println("Error while reading input file: " + e.getMessage());
            }
            if (current != -1) {
                stringBuilder.append((char) current);
            }
            if ((char) current == '\n' || (current == -1 && stringBuilder.length() != 0)) {
                try {
                    System.out.println("Starting from path: " + Paths.get(stringBuilder.toString())
                            + " that is " + Paths.get(stringBuilder.toString()).toAbsolutePath());
                    Files.walkFileTree(Paths.get(stringBuilder.toString()), new HashsumFileVisitor(out));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    stringBuilder = new StringBuilder();
                }
            }
        }
        try {
            br.close();
            out.close();
        } catch (IOException e) {
            System.out.println("Couldn't close input/output file: " + e.getMessage());
        }
    }

    private static void commit(Path path) {

    }
}
