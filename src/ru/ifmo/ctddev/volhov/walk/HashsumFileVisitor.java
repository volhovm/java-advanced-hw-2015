package ru.ifmo.ctddev.volhov.walk;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author volhovm
 *         Created on 2/18/15
 */

public class HashsumFileVisitor extends SimpleFileVisitor<Path> {
    private OutputStreamWriter out;
    public HashsumFileVisitor(OutputStreamWriter out) {
        this.out = out;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
        System.out.println("Visiting file: " + path.toString());
        try {
            BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
            int seed = 0x811c9dc5;
            while (true) {
                int b = reader.read();
                if (b == -1) break;
                seed = (seed * 0x01000193) ^ (b & 0xff);
            }
            out.write(Integer.toHexString(seed) + " " + path.toString());
        } catch (IOException e) {
            out.write("00000000 " + path.toString());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        out.write("22814880 " + path.toString());
        System.out.println(e.toString());
        System.out.println(path.toAbsolutePath());
        return FileVisitResult.CONTINUE;
    }
}
