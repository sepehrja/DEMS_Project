package Replica1.Server;

import java.io.IOException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import Replica1.ImplementRemoteInterface.ServerClass;


public class Montreal {
	public static void main(String args[]) throws Exception
	{
		try {
			ServerClass stub = new ServerClass(5555, 6666, 7777, "MTL");
			Registry registry = LocateRegistry.createRegistry(9992);
			registry.bind("addEvent", stub);	
			registry.bind("removeEvent", stub);	
			registry.bind("listEventAvailability", stub);	
			registry.bind("bookEvent", stub);	
			registry.bind("getBookingSchedule", stub);	
			registry.bind("cancelEvent", stub);	
			registry.bind("swapEvent", stub);	
			registry.bind("shutDown", stub);	

			Runnable task = () -> {
			run_server(stub);
			};
			Thread thread = new Thread(task);
			thread.start();				
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void run_server(ServerClass ServerImpl) {
		DatagramSocket socket = null;
		String response = "";
		try {
			socket = new DatagramSocket(6666);
			byte[] buffer = new byte[5000];
			System.out.println("Montreal UDP Server Started at 6666!");
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				socket.receive(request);
				String data = new String( request.getData(), 0, request.getLength() );
				String[] parts = data.split(";");
				String function = parts[0]; 
				String eventID = parts[1]; 
				String eventType = parts[2]; 
				String customerID= parts[3];
				if(function.equals("remove_client_event")) {
					String result = ServerImpl.remove_client_event(eventID, eventType);
					response= result;
				}
				else if(function.equals("bookEvent")) {
					String result = ServerImpl.bookEvent(customerID, eventID, eventType);
					response= result;
				}
				else if(function.equals("list_events")) {
					String result = ServerImpl.list_events(eventType);
					response= result;
				}
				else if(function.equals("cancel_client_event")) {
					String result = ServerImpl.cancel_client_event(eventID, eventType);
					response= result;
				}
				else if(function.equals("boook_next_event")) {
					String result = ServerImpl.boook_next_event(eventID, eventType, customerID);
					response= result;
				}
				byte[] sendData = response.getBytes();
				DatagramPacket reply = new DatagramPacket(sendData, response.length(), request.getAddress(),request.getPort());
				socket.send(reply);
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (socket != null)
				socket.close();
		}
		
	}
}
