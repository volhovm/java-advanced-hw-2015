package ru.ifmo.ctddev.volhov.walk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

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

        try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
            try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(args[1]))) {
                while (true) {
                    String curr = null;
                    try {
                        curr = br.readLine();
                    } catch (IOException e) {
                        System.out.println("Error while reading input file: " + e.getMessage());
                    }
                    if (curr == null) break;
                    Files.walkFileTree(Paths.get(curr), new HashsumFileVisitor(out));
                }
            } catch (FileNotFoundException e) {
                System.out.println("Can't open output file " + args[1] + ": " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Couldn't close output file: " + e.getMessage());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Can't open input file " + args[0] + ": " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Couldn't close input file: " + e.getMessage());
        }
    }
}
