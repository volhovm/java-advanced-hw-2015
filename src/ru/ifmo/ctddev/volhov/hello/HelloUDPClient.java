package ru.ifmo.ctddev.volhov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class implements basic client capable to send UDP-requests in specified number of threads with
 * desired text, wait for package receiving and output response. It's also manages situations when there's
 * no possibility to get response at this very moment.
 *
 * @see info.kgeorgiy.java.advanced.hello.HelloClient
 * @author volhovm
 *         Created on 4/29/15
 */
public class HelloUDPClient implements HelloClient {
    /**
     * This method starts sending UDP-requests (threads * requests) on specified host/port with text
     * "Prefix[thread_number]_[request_number]", receiving responses and printing response on
     * the standard output. If UDP-request had no response after timeout (100ms), UDP-response
     * is resent. In this way UDP-requests are sent while all of them will not receive layout of
     * form "Hello, [request]".
     * Number of threads and requests are specified in method parameters.
     *
     * @param host      server host
     * @param port      server port
     * @param prefix    prefix of text message sent in pa
     * @param requests  number of requests to send from every thread
     * @param threads   number of threads to send requests from
     */
    @Override
    public void start(String host, int port, String prefix, int requests, int threads) {
        ExecutorService threadPool = Executors.newFixedThreadPool(threads);
        try {
            final InetAddress address = InetAddress.getByName(host);
            for (int i = 0; i < threads; i++) {
                final int threadId = i;
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        try (final DatagramSocket socket = new DatagramSocket()) {
                            socket.setSoTimeout(100);
                            byte[] inputBuffer = new byte[socket.getReceiveBufferSize()];
                            int current = 0;
                            while (current != requests) {
                                String message = (prefix + threadId + "_" + current);
                                byte[] msg = message.getBytes("UTF-8");
                                System.out.println(message);
                                socket.send(new DatagramPacket(msg, msg.length, address, port));
                                DatagramPacket reply = new DatagramPacket(inputBuffer, inputBuffer.length);
                                try {
                                    socket.receive(reply);
                                    byte[] data = reply.getData();
                                    String response = new String(data, 0, reply.getLength(), Charset.forName("UTF-8"));
                                    if (!response.equals("Hello, " + message)) {
                                        throw new NumberFormatException();
                                    }
                                    String[] splited = response.split("_");
                                    int reqId = Integer.parseInt(splited[splited.length - 1]);
                                    if (reqId >= requests) {
                                        throw new NumberFormatException();
                                    }
                                    System.out.println(response);
                                    current++;
                                } catch (SocketTimeoutException | NumberFormatException ignored) {}
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.err.println("Client #" + threadId + " exiting");
                    }
                });
            }
            threadPool.shutdown();
            threadPool.awaitTermination(1, TimeUnit.DAYS);
        } catch (UnknownHostException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdownNow();
        }
    }
}
