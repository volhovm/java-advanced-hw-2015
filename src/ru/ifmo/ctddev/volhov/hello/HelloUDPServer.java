package ru.ifmo.ctddev.volhov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class implements basic UDP server that can listen up to 65536 ports, receive UDP packages
 * and simply send an answer with the request data prefixed by "Hello, ". It's also possible to
 * receive and send UDP-packages simultaneously, with number of threads specified in the {@code threads}
 * parameter of method {@link ru.ifmo.ctddev.volhov.hello.HelloUDPServer#start}.
 * <p>
 * The server can be stopped at all ports, all sockets will be closed. Server is OK to be reused after it's
 * closed.
 *
 * @see info.kgeorgiy.java.advanced.hello.HelloServer
 * @author volhovm
 *         Created on 4/29/15
 */
public class HelloUDPServer implements HelloServer {
    private ConcurrentHashMap<Integer, ExecutorService> pools = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, DatagramSocket> sockets = new ConcurrentHashMap<>();
    private boolean[] running = new boolean[Short.MAX_VALUE];

    /**
     * This methods starts UDP-server on specified port. All received requests will be answered, response text
     * will be "Hello, [request_text]". There will be {@code threads} number of threads used for receiving
     * and sending requests.
     * @param port      port to listen for UDP packages on
     * @param threads   threads to process data
     */
    @Override
    public synchronized void start(int port, int threads) {
        if (running[port]) {
            throw new IllegalStateException("Can't run server that is already started");
        }
        running[port] = true;
        if (pools.contains(port)) pools.get(port).shutdownNow();
        pools.put(port, Executors.newFixedThreadPool(threads));
        try {
            sockets.put(port, new DatagramSocket(port, InetAddress.getByName("localhost")));
            DatagramSocket socket = sockets.get(port);
            socket.setSoTimeout(100);
            for (int i = 0; i < threads; i++) {
                final int threadId = i;
                pools.get(port).submit(() -> {
                    try {
                        byte[] buf = new byte[socket.getReceiveBufferSize()];
                        DatagramPacket income = new DatagramPacket(buf, buf.length);
                        while (running[port]) {
                            try {
                                socket.receive(income);
                                byte[] data = income.getData();
                                String result = new String(data, 0, income.getLength(), Charset.forName("UTF-8"));
                                String replyText = "Hello, " + result;
                                DatagramPacket reply = new DatagramPacket(replyText.getBytes("UTF-8"),
                                        replyText.getBytes().length,
                                        income.getAddress(), income.getPort());
                                socket.send(reply);
                            } catch (SocketTimeoutException ignored) {
                            } catch (SocketException e) {
                                System.err.println("Socket on port " + port + " closed.");
                                break;
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("SERVER: fatal error port#" + port + " thread #" + threadId);
                        e.printStackTrace();
                    }
                });
            }
        } catch (SocketException | UnknownHostException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * This method closes this server, resetting it's state to initial. Server can be reused
     * after this method invocation.
     */
    @Override
    public synchronized void close() {
        Arrays.fill(running, false);
        sockets.values().forEach(DatagramSocket::close);
        sockets.clear();
        pools.values().forEach(ExecutorService::shutdown);
        pools.values().forEach(ExecutorService::shutdownNow);
        pools.clear();
    }
}
