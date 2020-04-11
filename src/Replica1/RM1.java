package Replica1;

import java.io.IOException;
import java.rmi.registry.Registry;
import java.util.concurrent.ConcurrentHashMap;
import java.rmi.registry.LocateRegistry;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import Replica1.DataBase.Message;
import Replica1.ServerInterface.EventManagementInterface;

public class RM1 {
    public static int lastSequenceID = 1;
    public static ConcurrentHashMap<Integer, Message> message_q = new ConcurrentHashMap<>();;

    public static void main(String args[]) throws Exception {
        Run();
    }

    private static void Run() throws Exception {
        Runnable task = () -> {
            try {
                receive();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		};
		Thread thread = new Thread(task);
		thread.start();
    }
    private static void receive() throws Exception{
        MulticastSocket socket = null;
		try {

			socket = new MulticastSocket(1234);

			socket.joinGroup(InetAddress.getByName("230.1.1.10"));

			byte[] buffer = new byte[1000];
			System.out.println("RM1 UDP Server Started(port=1234)............");

			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				socket.receive(request);

                String data = new String( request.getData(), 0, request.getLength());
                String[] parts = data.split(";");

                if(parts[2].equals("00"))
                {  
                    Message message=message_obj_create(data);
                    send_sync_message_list(message);
                    if(message.sequenceId - lastSequenceID > 1)
                        initial_message_q(message.toString());
                    message_q.put(message.sequenceId,message);
                    executeAllRequests();
                }
                else if(parts[2].equals("01"))
                {            
                    Message message=message_obj_create(data);   
                    message_q.put(message.sequenceId,message);
                }
                else if(parts[2].equals("02"))
                {
                    initial_message_q(data);
                }
				DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(), request.getPort());
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
    private static Message message_obj_create(String data)
	{
        String[] parts = data.split(";");
        int sequenceId =Integer.parseInt(parts[0]);
        String FrontIpAddress = parts[1];
        String MessageType = parts[2];
        String Function = parts[3];
        String userID = parts[4];
        String newEventID = parts[5];
        String newEventType = parts[6];
        String oldEventID = parts[7];
        String oldEventType = parts[8];   
        int bookingCapacity =Integer.parseInt(parts[9]);     
        Message message = new Message(sequenceId, FrontIpAddress, MessageType, Function, userID, newEventID, newEventType, oldEventID, oldEventType, bookingCapacity);
        return message;
    }

    private static void initial_message_q(String message)
	{
        
    }
    private static void send_sync_message_list(Message message)
	{
        int port=1234;
        message.MessageType = "01";
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            byte[] messages = message.toString().getBytes();
            InetAddress aHost = InetAddress.getByName("230.1.1.10");

            DatagramPacket request = new DatagramPacket(messages, messages.length, aHost, port);
            socket.send(request);

            //To do: wait for responsed and return it.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void executeAllRequests()throws Exception
	{
        int count = 0;
        for (ConcurrentHashMap.Entry<Integer, Message> entry : message_q.entrySet()) 
        {
            if(entry.getKey() > lastSequenceID)
            {
                String response = requestToServers(entry.getValue());
                Message message = new Message(entry.getValue().sequenceId,response , "RM1", 
                                entry.getValue().Function, entry.getValue().userID, entry.getValue().newEventID, 
                                entry.getValue().newEventType, entry.getValue().oldEventID, 
                                entry.getValue().oldEventType, entry.getValue().bookingCapacity);

                messsageToFront(message.toString(), entry.getValue().FrontIpAddress);
            }
        }
        lastSequenceID += count;
    }

    private static String requestToServers(Message input) throws Exception
	{
        int portNumber = serverPort(input.userID.substring(0, 3));
        Registry registry = LocateRegistry.getRegistry(portNumber);
        if(input.userID.equals("M"))
        {
            if(input.Function.equals("addEvent"))
            {
                EventManagementInterface obj = (EventManagementInterface) registry.lookup("addEvent");
                String response = obj.addEvent(input.newEventID, input.newEventType,input.bookingCapacity);
                System.out.println(response);
                return response;
            }
            else if(input.Function.equals("removeEvent"))
            {
                EventManagementInterface obj = (EventManagementInterface) registry.lookup("removeEvent");
                String response = obj.removeEvent(input.newEventID, input.newEventType);
                System.out.println(response);
                return response;
            }
            else if(input.Function.equals("listEventAvailability"))
            {
                EventManagementInterface obj = (EventManagementInterface) registry.lookup("listEventAvailability");
                String response = obj.listEventAvailability(input.newEventType);
                System.out.println(response);
                return response;
            }
        }
        else if(input.userID.equals("C"))
        {
            if(input.Function.equals("bookEvent"))
            {
                EventManagementInterface obj = (EventManagementInterface) registry.lookup("bookEvent");
                String response = obj.bookEvent(input.userID, input.newEventID, input.newEventType);
                System.out.println(response);
                return response;
            }
            else if(input.Function.equals("getBookingSchedule"))
            {
                EventManagementInterface obj = (EventManagementInterface) registry.lookup("getBookingSchedule");
                String response = obj.getBookingSchedule(input.userID);
                System.out.println(response);
                return response;
            }
            else if(input.Function.equals("cancelEvent"))
            {
                EventManagementInterface obj = (EventManagementInterface) registry.lookup("cancelEvent");
                String response = obj.cancelEvent(input.userID, input.newEventID, input.newEventType);
                System.out.println(response);
                return response;
            }
            else if(input.Function.equals("swapEvent"))
            {
                EventManagementInterface obj = (EventManagementInterface) registry.lookup("swapEvent");
                String response = obj.swapEvent(input.userID, input.newEventID, input.newEventType, input.oldEventID, input.oldEventType);
                System.out.println(response);
                return response;
            }
        }
        return "Null response from server" + input.userID.substring(0, 3);
    }
    private static int serverPort(String input)
	{
		String branch = input.substring(0,3);
		int portNumber = -1;
		
		if(branch.equals("que"))
			portNumber=9991;
		else if(branch.equals("mtl"))
			portNumber=9992;
		else if(branch.equals("she"))
			portNumber=9993;
			
		return portNumber;
    }

    public static void messsageToFront(String message, String FrontIpAddress) {
		System.out.println("Message to front:"+message);
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			byte[] bytes = message.getBytes();
			InetAddress aHost = InetAddress.getByName(FrontIpAddress);

			DatagramPacket request = new DatagramPacket(bytes, bytes.length, aHost, 1413);
			socket.send(request);
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
