package ru.ifmo.ctddev.volhov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author volhovm
 *         Created on 4/29/15
 */
public class HelloUDPServer implements HelloServer {
    private volatile boolean running;
    private ExecutorService pool;
    private DatagramSocket socket;

    @Override
    public synchronized void start(int port, int threads) {
        if (running) {
            throw new IllegalStateException("Can't run server that is already started");
        }
        running = true;
        pool = Executors.newFixedThreadPool(threads);
        try {
            socket = new DatagramSocket(port, InetAddress.getByName("localhost"));
            socket.setSoTimeout(100);
            for (int i = 0; i < threads; i++) {
                final int threadId = i;
                pool.submit(() -> {
//                    System.out.println("SERVER: starting thread #" + threadId);
                    try {
                        byte[] buf = new byte[socket.getSendBufferSize()];
                        DatagramPacket income = new DatagramPacket(buf, buf.length);
                        while (running) {
                            try {
                                socket.receive(income);
                                byte[] data = income.getData();
                                String result = new String(data, 0, income.getLength());
//                                System.out.println("SERVER: got income: " + result);
                                Random rand = new Random();
                                if (rand.nextFloat() > 0.2) {
                                    byte[] temp = result.getBytes();
                                    for (int j = 0; j < rand.nextInt(5); j++) {
                                        temp[rand.nextInt(temp.length)] = (byte) rand.nextInt(256);
                                    }
                                    result = new String(temp, 0, result.length());
                                }
                                String replyText = "Hello, " + result;
                                DatagramPacket reply = new DatagramPacket(replyText.getBytes(),
                                        replyText.getBytes().length,
                                        income.getAddress(), income.getPort());
                                socket.send(reply);
                            } catch (SocketTimeoutException ignored) {
                            } catch (Throwable thr) {
                                thr.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("SERVER: fatal error thread #" + threadId);
                        e.printStackTrace();
                    }
//                    System.out.println("SERVER: thread #" + threadId + " exited");
                });
            }
        } catch (SocketException | UnknownHostException exc) {
            exc.printStackTrace();
        }
    }

    @Override
    public synchronized void close() {
        running = false;
//        System.out.println("-------- SERVER CLOSING ------");
        pool.shutdown();
        pool.shutdownNow();
    }
}
