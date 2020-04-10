package sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Sequencer {
	private static int sequencerId = 0;

	public static void main(String[] args) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(1333, InetAddress.getByName("192.168.2.1"));
			byte[] buffer = new byte[1000];
			System.out.println("Sequencer UDP Server Started");
			while (true) {
				// DatagramPacket request = new DatagramPacket(buffer,
				// buffer.length);
				DatagramPacket request = new DatagramPacket(buffer,
						buffer.length);

				aSocket.receive(request);

				String sentence = new String(request.getData(), 0,
						request.getLength());

				String[] parts = sentence.split(";");
				String sequencerId1 = parts[0];
				String ip = request.getAddress().getHostAddress();
				
				String sentence1=sequencerId1+";"+ip+";"+
						parts[2]+";"+
						parts[3]+";"+
						parts[4]+";"+
						parts[5]+";"+
						parts[6]+";"+
						parts[7]+";"+
						parts[8]+";"+
						parts[9]+";";
				sequencerId = Integer.parseInt(sequencerId1);
				System.out.println(sentence1);
				sendMessage(sentence1);

				byte[] SeqId = (Integer.toString(sequencerId)).getBytes();
				InetAddress aHost1 = request.getAddress();
				int port1 = request.getPort();

				DatagramPacket request1 = new DatagramPacket(SeqId,
						SeqId.length, aHost1, port1);
				aSocket.send(request1);
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
		
		sequencerId++;

		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] messages = message.getBytes();
			InetAddress aHost = InetAddress.getByName("230.1.1.10");

			DatagramPacket request = new DatagramPacket(messages,
					messages.length, aHost, port);
			aSocket.send(request);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
