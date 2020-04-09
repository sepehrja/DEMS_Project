package FrontEnd;

import FrontEnd.ServerObjectInterfaceApp.ServerObjectInterface;
import FrontEnd.ServerObjectInterfaceApp.ServerObjectInterfaceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.io.IOException;
import java.net.*;

public class FE {
    //TODO: set sequencer IP and Port
    private static final int sequencerPort = 1313;
    private static final String sequencerIP = "127.0.0.1";

    public static void main(String[] args) {
        try {
            FEInterface inter = new FEInterface() {
                @Override
                public void informRmHasBug(int RmNumber) {
                    //TODO: what to do when an Rm bug found
                }

                @Override
                public void informRmIsDown(int RmNumber) {
                    //TODO: what to do when an Rm is down
                }

                @Override
                public void sendRequestToSequencer(MyRequest myRequest) {
                    sendUnicastToSequencer(myRequest);
                }
            };
            FrontEndImplementation servant = new FrontEndImplementation(inter);
            Runnable task = () -> {
                listenForUDPResponses(servant);
            };
            Thread thread = new Thread(task);
            thread.start();
            // create and initialize the ORB //// get reference to rootpoa &amp; activate
            // the POAManager
            ORB orb = ORB.init(args, null);
            // -ORBInitialPort 1050 -ORBInitialHost localhost
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant and register it with the ORB
            servant.setORB(orb);

            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
            ServerObjectInterface href = ServerObjectInterfaceHelper.narrow(ref);

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            NameComponent[] path = ncRef.to_name("FrontEnd");
            ncRef.rebind(path, href);

            System.out.println("FrontEnd Server is Up & Running");
//            Logger.serverLog(serverID, " Server is Up & Running");

//            addTestData(servant);
            // wait for invocations from clients
            while (true) {
                orb.run();
            }
        } catch (Exception e) {
//            System.err.println("Exception: " + e);
            e.printStackTrace(System.out);
//            Logger.serverLog(serverID, "Exception: " + e);
        }

        System.out.println("FrontEnd Server Shutting down");
//        Logger.serverLog(serverID, " Server Shutting down");

    }

    private static void sendUnicastToSequencer(MyRequest requestFromClient) {
        DatagramSocket aSocket = null;
        String dataFromClient = requestFromClient.toString();
        try {
            aSocket = new DatagramSocket();
            byte[] message = dataFromClient.getBytes();
            InetAddress aHost = InetAddress.getByName(sequencerIP);
            DatagramPacket requestToSequencer = new DatagramPacket(message, dataFromClient.length(), aHost, sequencerPort);

            aSocket.send(requestToSequencer);
        } catch (SocketException e) {
            System.out.println("Failed: " + requestFromClient.noRequestSendError());
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Failed: " + requestFromClient.noRequestSendError());
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
//			if (aSocket != null)
//				aSocket.close();
        }
    }

    private static void listenForUDPResponses(FrontEndImplementation servant) {
        MulticastSocket aSocket = null;
        try {

            aSocket = new MulticastSocket(1413);

            aSocket.joinGroup(InetAddress.getByName("230.1.1.5"));

            byte[] buffer = new byte[1000];
            System.out.println("Server Started............");

            while (true) {
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(response);
                String sentence = new String(response.getData(), 0,
                        response.getLength());
                System.out.println("Response received: " + sentence);
                String[] parts = sentence.split(":");
                //TODO: parse the response data
                if (parts.length > 2) {
//                    MessageInfo messageInfo = new MessageInfo();
//                    messageInfo.setResponse(parts[0]);
//                    messageInfo.setRMNo(Integer.parseInt(parts[1]));
//                    String[] partsTwo = parts[2].split(";");
//                    messageInfo.setMessage(parts[2]);
//                    messageInfo.setFunction(partsTwo[0]);
//                    messageInfo.setUserID(partsTwo[1]);
//                    if(partsTwo[2].equals("null")) {
//                        partsTwo[2] = null;
//                    }
//                    messageInfo.setItemName(partsTwo[2]);
//                    if(partsTwo[3].equals("null")) {
//                        partsTwo[3] = null;
//                    }
//                    messageInfo.setItemId(partsTwo[3]);
//                    if(partsTwo[4].equals("null")) {
//                        partsTwo[4] = null;
//                    }
//                    messageInfo.setNewItem(partsTwo[4]);
//
//                    messageInfo.setNumber(Integer.parseInt(partsTwo[5]));
//                    messageInfo.setSequenceId(Integer.parseInt(partsTwo[6]));
                    System.out.println("Adding response to FrontEndImplementation:");
                    servant.addReceivedResponse(sentence);
                }
//                DatagramPacket reply = new DatagramPacket(response.getData(), response.getLength(), response.getAddress(),
//                        response.getPort());
//                aSocket.send(reply);
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
}
