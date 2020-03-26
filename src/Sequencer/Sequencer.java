package Sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Sequencer {
    private static int sequencerId = 1;

    public static void main(String[] args) {
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(1333);
            byte[] buffer = new byte[1000];
            System.out.println("Sequencer UDP Server 1313 Started............");
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String sentence = new String(request.getData(), 0,
                        request.getLength());

                String[] parts = sentence.split(";");
                String userID = parts[1];
                System.out.println(sentence);
                sendMessage(sentence);
            }

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
    }

    public static void sendMessage(String message) {
        int port = 1412;
        message = message + sequencerId;
        port = 1412;
        sequencerId++;

        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            byte[] messages = message.getBytes();
            InetAddress aHost = InetAddress.getByName("230.1.1.10");

            DatagramPacket request = new DatagramPacket(messages, messages.length, aHost, port);
            aSocket.send(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
