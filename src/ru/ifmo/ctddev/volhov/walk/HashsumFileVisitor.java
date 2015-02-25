package ru.ifmo.ctddev.volhov.walk;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author volhovm
 *         Created on 2/18/15
 */

public class HashsumFileVisitor extends SimpleFileVisitor<Path> {
    final private OutputStreamWriter out;

    public HashsumFileVisitor(OutputStreamWriter out) {
        this.out = out;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
//        System.out.println("Visiting file: " + path.toString());
        try (FileInputStream inputStream
                     = new FileInputStream(path.toAbsolutePath().toString())) {
            int hash = 0x811c9dc5;
            while (true) {
                int b = inputStream.read();
                if (b == -1) break;
                hash *= 0x01000193;
                hash ^= b & 0xff;
            }
            out.write(String.format("%8s", Integer.toHexString(hash)).replace(' ', '0') + " " + path.toString() + "\n");
        } catch (IOException e) {
            System.err.println("Error while calculating hashsum: " + e.getMessage());
            out.write("00000000 " + path.toString() + "\n");
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        out.write("00000000 " + path.toString() + "\n");
        if (Files.notExists(path)) {
            System.err.println("File does not exist: " + path);
        } else if (Files.isReadable(path)) {
            System.err.println("Can't read file: " + path + " :" + e.getMessage());
        } else {
            System.err.println("File visit error: " + e.getMessage());
        }
        return FileVisitResult.CONTINUE;
    }
}
