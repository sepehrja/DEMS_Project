	package Server;

	import java.io.IOException;
	import org.omg.CORBA.ORB;
	import org.omg.CosNaming.*;
	import org.omg.PortableServer.POA;
	import org.omg.PortableServer.POAHelper;
	import java.net.DatagramPacket;
	import java.net.DatagramSocket;
	import java.net.SocketException;
	import ImplementRemoteInterface.ServerClass;
	import ServerModule.*;


	public class Sherbrooke {
		public static void main(String args[]) throws Exception
		{
			ORB orb = ORB.init(args, null);
			try {
				POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
				rootpoa.the_POAManager().activate();
				
				ServerClass ServerImpl = new ServerClass(5555, 6666, 7777, "SHE");
				ServerImpl.setORB(orb);
				org.omg.CORBA.Object ref = rootpoa.servant_to_reference(ServerImpl);
				ServerInterface href = ServerInterfaceHelper.narrow(ref);
				org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
				NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
				
				String name = "SHE";
				NameComponent path[] = ncRef.to_name(name);
				ncRef.rebind(path, href);
				
				System.out.println("Sherbrooke Server Ready and Waiting...");
				
				Runnable task = () -> {
				run_server(ServerImpl);
				};
				Thread thread = new Thread(task);
				thread.start();
				
				orb.run();
				
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	
	private static void run_server(ServerClass stub) {
		DatagramSocket socket = null;
		String response = "";
		try {
			socket = new DatagramSocket(7777);
			byte[] buffer = new byte[5000];
			System.out.println("Sherbrook UDP Server Started at 7777!");
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
					String result = stub.remove_client_event(eventID, eventType);
					response= result;
				}
				else if(function.equals("bookEvent")) {
					String result = stub.bookEvent(customerID, eventID, eventType);
					response= result;
				}
				else if(function.equals("list_events")) {
					String result = stub.list_events(eventType);
					response= result;
				}
				else if(function.equals("cancel_client_event")) {
					String result = stub.cancel_client_event(eventID, eventType);
					response= result;
				}
				else if(function.equals("boook_next_event")) {
					String result = stub.boook_next_event(eventID, eventType);
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
