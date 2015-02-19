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
        while (true) {
            String curr = null;
            try {
                curr = br.readLine();
            } catch (IOException e) {
                System.out.println("Error while reading input file: " + e.getMessage());
            }
            if (curr == null) break;
            try {
//                    System.out.println("Starting from path: `" + Paths.get(stringBuilder.toString())
//                            + "` that is " + Paths.get(stringBuilder.toString()).toAbsolutePath());
                Files.walkFileTree(
                        Paths.get(curr),
                        new HashsumFileVisitor(out));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            br.close();
            out.close();
        } catch (IOException e) {
            System.out.println("Couldn't close input/output file: " + e.getMessage());
        }
    }
}
