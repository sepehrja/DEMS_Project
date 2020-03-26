package Replica2.com.sepehr.Server;

import Replica2.com.sepehr.DataModel.EventModel;
import Replica2.com.sepehr.Logger.Logger;
import Replica2.com.sepehr.ServerImplementation.EventManagement;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ServerInstance {

    private String serverID;
    private String serverName;
    private int serverUdpPort;

    public ServerInstance(String serverID, String[] args) throws Exception {
        this.serverID = serverID;
        switch (serverID) {
            case "MTL":
                serverName = EventManagement.EVENT_SERVER_MONTREAL;
                serverUdpPort = EventManagement.Montreal_Server_Port;
                break;
            case "QUE":
                serverName = EventManagement.EVENT_SERVER_QUEBEC;
                serverUdpPort = EventManagement.Quebec_Server_Port;
                break;
            case "SHE":
                serverName = EventManagement.EVENT_SERVER_SHERBROOK;
                serverUdpPort = EventManagement.Sherbrooke_Server_Port;
                break;
        }
        try {
            // create and initialize the ORB //// get reference to rootpoa &amp; activate
            // the POAManager
//            ORB orb = ORB.init(args, null);
//            // -ORBInitialPort 1050 -ORBInitialHost localhost
//            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
//            rootpoa.the_POAManager().activate();

            // create servant and register it with the ORB
            EventManagement servant = new EventManagement(serverID, serverName);
//            servant.setORB(orb);
//
//            // get object reference from the servant
//            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
//            ServerObjectInterface href = ServerObjectInterfaceHelper.narrow(ref);
//
//            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
//            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
//
//            NameComponent[] path = ncRef.to_name(serverID);
//            ncRef.rebind(path, href);

            System.out.println(serverName + " Server is Up & Running");
            Logger.serverLog(serverID, " Server is Up & Running");

            addTestData(servant);
            Runnable task = () -> {
                listenForRequest(servant, serverUdpPort, serverName, serverID);
            };
            Thread thread = new Thread(task);
            thread.start();

            // wait for invocations from clients
//            while (true) {
//                orb.run();
//            }
        } catch (Exception e) {
//            System.err.println("Exception: " + e);
            e.printStackTrace(System.out);
            Logger.serverLog(serverID, "Exception: " + e);
        }

//        System.out.println(serverName + " Server Shutting down");
//        Logger.serverLog(serverID, " Server Shutting down");

    }

    private static void listenForRequest(EventManagement obj, int serverUdpPort, String serverName, String serverID) {
        DatagramSocket aSocket = null;
        String sendingResult = "";
        try {
            aSocket = new DatagramSocket(serverUdpPort);
            byte[] buffer = new byte[1000];
            System.out.println(serverName + " UDP Server Started at port " + aSocket.getLocalPort() + " ............");
            Logger.serverLog(serverID, " UDP Server Started at port " + aSocket.getLocalPort());
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String sentence = new String(request.getData(), 0,
                        request.getLength());
                String[] parts = sentence.split(";");
                String method = parts[0];
                String customerID = parts[1];
                String eventType = parts[2];
                String eventID = parts[3];
                if (method.equalsIgnoreCase("removeEvent")) {
                    Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " eventID: " + eventID + " eventType: " + eventType + " ", " ...");
                    String result = obj.removeEventUDP(eventID, eventType, customerID);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("listEventAvailability")) {
                    Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " eventType: " + eventType + " ", " ...");
                    String result = obj.listEventAvailabilityUDP(eventType);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("bookEvent")) {
                    Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " eventID: " + eventID + " eventType: " + eventType + " ", " ...");
                    String result = obj.bookEvent(customerID, eventID, eventType);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("cancelEvent")) {
                    Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " eventID: " + eventID + " eventType: " + eventType + " ", " ...");
                    String result = obj.cancelEvent(customerID, eventID, eventType);
                    sendingResult = result + ";";
                }
                byte[] sendData = sendingResult.getBytes();
                DatagramPacket reply = new DatagramPacket(sendData, sendingResult.length(), request.getAddress(),
                        request.getPort());
                aSocket.send(reply);
                Logger.serverLog(serverID, customerID, " UDP reply sent " + method + " ", " eventID: " + eventID + " eventType: " + eventType + " ", sendingResult);
            }
        } catch (SocketException e) {
            System.err.println("SocketException: " + e);
            e.printStackTrace(System.out);
        } catch (IOException e) {
            System.err.println("IOException: " + e);
            e.printStackTrace(System.out);
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
    }

    private void addTestData(EventManagement remoteObject) {
        switch (serverID) {
            case "MTL":
                remoteObject.addNewEvent("MTLA090620", EventModel.CONFERENCES, 2);
                remoteObject.addNewEvent("MTLA080620", EventModel.TRADE_SHOWS, 2);
                remoteObject.addNewEvent("MTLE230620", EventModel.SEMINARS, 1);
                remoteObject.addNewEvent("MTLA150620", EventModel.TRADE_SHOWS, 12);
                break;
            case "QUE":
                remoteObject.addNewCustomerToClients("QUEC1234");
                remoteObject.addNewCustomerToClients("QUEC4114");
                break;
            case "SHE":
                remoteObject.addNewEvent("SHEE110620", EventModel.CONFERENCES, 1);
                remoteObject.addNewEvent("SHEE080620", EventModel.CONFERENCES, 1);
                break;
        }
    }
}
