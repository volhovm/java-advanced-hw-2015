package ru.ifmo.ctddev.volhov.hello;

/**
 * @author volhovm
 *         Created on 5/1/15
 */
public class HeloUDPTesting {
    public static void main(String[] args) throws InterruptedException {
        HelloUDPServer server = new HelloUDPServer();
        server.start(7777, 1);
//        Thread.sleep(3000);
        HelloUDPClient client = new HelloUDPClient();
        client.start("localhost", 7777, "My_Prefix_", 1, 1);
        server.close();
        System.out.println("end");
    }
}
