package ru.ifmo.ctddev.volhov.walk;

import com.sun.istack.internal.NotNull;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author volhov
 *         Created on 2/18/15
 */

public class RecursiveWalk {
    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.out.println("format: Walk input output");
            return;
        }

        BufferedReader br;
        OutputStreamWriter out;
        try {
            br = new BufferedReader(new FileReader(args[0]));
        } catch (FileNotFoundException e) {
            System.out.println("Can't open input file " + args[0] + ": " + e.getMessage());
            return;
        }
        try {
            out = new OutputStreamWriter(new FileOutputStream(args[1]));
        } catch (FileNotFoundException e) {
            System.out.println("Can't open output file " + args[1] + ": " + e.getMessage());
            try {
                br.close();
            } catch (IOException e1) {
                System.out.println("Couldn't close input file: " + e.getMessage());
            }
            return;
        }

        while (true) {
            String curr = null;
            try {
                curr = br.readLine();
            } catch (IOException e) {
                System.out.println("Error while reading input file: " + e.getMessage());
            }
            if (curr == null) break;
            try {
                Files.walkFileTree(Paths.get(curr), new HashsumFileVisitor(out));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            br.close();
        } catch (IOException e) {
            System.out.println("Couldn't close input file: " + e.getMessage());
        }
        try {
            out.close();
        } catch (IOException e) {
            System.out.println("Couldn't close output file: " + e.getMessage());
        }
    }
}
