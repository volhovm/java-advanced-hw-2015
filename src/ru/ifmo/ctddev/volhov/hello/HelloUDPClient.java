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
                            final DatagramSocket socket = new DatagramSocket();
                            socket.setSoTimeout(200);
                            for (int j = 0; j < requests; j++) {
                                String msg = (prefix + threadId + "_" + j);
                                System.out.println(msg);
                                socket.send(new DatagramPacket(msg.getBytes(), msg.length(), address, port));
                            }
                            byte[] inputBuffer = new byte[socket.getReceiveBufferSize()];
                            Deque<Integer> missing = new ArrayDeque<>();
                            IntStream.range(0, requests).forEach(missing::add);
                            while (!missing.isEmpty()) {
                                DatagramPacket reply = new DatagramPacket(inputBuffer, inputBuffer.length);
                                try {
                                    socket.receive(reply);
                                    byte[] data = reply.getData();
                                    String response = new String(data, 0, reply.getLength());

                                    if (!response.matches(".*" + prefix + threadId + "_\\d+")) {
                                        System.err.println("FAIL: " + response);
                                        throw new NumberFormatException();
                                    }
                                    String[] splited = response.split("_");
                                    int reqId = Integer.parseInt(splited[splited.length - 1]);
                                    if (reqId >= requests) {
                                        throw new NumberFormatException();
                                    }
//                                    int numlength = 0;
//                                    int threads2 = threads;
//                                    while (threads2 != 0) {
//                                        numlength++;
//                                        threads2 /= 10;
//                                    }
//                                    String prevPart = splited[splited.length - 2];
//                                    if (Integer.parseInt(prevPart.substring(prevPart.length() - numlength,
//                                            prevPart.length())) != threadId) {
//                                        throw new NumberFormatException();
//                                    }
                                    missing.stream().filter(x -> x == reqId).forEach(missing::remove);
                                    System.out.println(response);
                                } catch (SocketTimeoutException | NumberFormatException e) {
                                    int current = missing.removeFirst();
                                    System.err.println("resending " + threadId + "_" + current);
                                    byte[] msg = (prefix + threadId + "_" + current).getBytes();
                                    socket.send(new DatagramPacket(msg, msg.length, address, port));
                                    missing.addLast(current);
                                } catch (Throwable e) {
                                    System.err.println("mda:");
                                    e.printStackTrace();
                                }
                            }
                            semaphore.release();
                            if (semaphore.getQueueLength() == 1) {
                                semaphore.acquire();
                            }
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
//                        System.out.println("Client #" + threadId + " exiting");
                        exiter.decrementAndGet();
                        if (exiter.get() == 0) {
                            threadPool.shutdownNow();
                        }
                    }
                });
            }
            Thread.sleep(100);
            semaphore.acquire(threads);
        } catch (UnknownHostException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
