package ru.ifmo.ctddev.volhov.hello;

/**
 * @author volhovm
 *         Created on 5/1/15
 */
public class HeloUDPTesting {
    public static void main(String[] args) throws InterruptedException {
        HelloUDPServer server = new HelloUDPServer();
        server.start(7777, 2);
//        Thread.sleep(3000);
        HelloUDPClient client = new HelloUDPClient();
        client.start("localhost", 7777, "PREFIX__", 5, 2);
        Thread.sleep(1000);
        server.close();
        System.out.println("end");
        server.close();
    }
}
