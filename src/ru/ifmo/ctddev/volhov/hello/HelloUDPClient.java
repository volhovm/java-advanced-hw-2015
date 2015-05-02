package ru.ifmo.ctddev.volhov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author volhovm
 *         Created on 4/29/15
 */
public class HelloUDPClient implements HelloClient {
    @Override
    public void start(String host, int port, String prefix, int requests, int threads) {
        ExecutorService threadPool = Executors.newFixedThreadPool(threads);
        AtomicInteger exiter = new AtomicInteger(threads);
        try {
            Semaphore semaphore = new Semaphore(threads + 1);
            semaphore.acquire();
            final InetAddress address = InetAddress.getByName(host);
            for (int i = 0; i < threads; i++) {
                final int threadId = i;
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            semaphore.acquire();
                            try (final DatagramSocket socket = new DatagramSocket()) {
                                socket.setSoTimeout(100);
                                byte[] inputBuffer = new byte[socket.getReceiveBufferSize()];
                                int current = 0;
                                while (current != requests) {
//                                    System.err.println("resending " + threadId + "_" + current);
                                    String message = (prefix + threadId + "_" + current);
                                    byte[] msg = message.getBytes();
                                    System.out.println(message);
                                    socket.send(new DatagramPacket(msg, msg.length, address, port));

                                    DatagramPacket reply = new DatagramPacket(inputBuffer, inputBuffer.length);
                                    try {
                                        socket.receive(reply);
                                        byte[] data = reply.getData();
                                        String response = new String(data, 0, reply.getLength());
                                        if (!response.equals("Hello, " + message)) {
//                                            System.err.println("Wrong format: " + response);
                                            throw new NumberFormatException();
                                        }
                                        String[] splited = response.split("_");
                                        int reqId = Integer.parseInt(splited[splited.length - 1]);
                                        if (reqId >= requests) {
                                            throw new NumberFormatException();
                                        }
                                        System.out.println(response);
                                        current++;
                                    } catch (SocketTimeoutException | NumberFormatException e) {
                                        // it's ok, just resend
                                    } catch (Throwable thr) {
                                        System.err.println("MDAAAA");
                                        thr.printStackTrace();
                                    }
                                }
                            } catch (IOException ignored) {
                            }
                            semaphore.release();
                            if (semaphore.availablePermits() == threads) {
                                System.err.println("releasing the last semaphore entry");
                                semaphore.release();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.err.println("Client #" + threadId + " exiting");
                        exiter.decrementAndGet();
                        if (exiter.get() == 0) {
                            System.err.println("Client #" + threadId + " shutting down the pool");
                            threadPool.shutdownNow();
                        }
                        System.err.println("Client #" + threadId + " out");
                    }
                });
            }
            Thread.sleep(100);
            semaphore.acquire(threads + 1);
        } catch (UnknownHostException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
