package Replica2.com.sepehr.ServerImplementation;

import Replica2.CommonOutput;
import Replica2.com.sepehr.DataModel.ClientModel;
import Replica2.com.sepehr.DataModel.EventModel;
import Replica2.com.sepehr.EventManagementInterface;
import Replica2.com.sepehr.Logger.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventManagement extends UnicastRemoteObject implements EventManagementInterface {
    public static final int Montreal_Server_Port = 8888;
    public static final int Quebec_Server_Port = 7777;
    public static final int Sherbrooke_Server_Port = 6666;
    public static final String EVENT_SERVER_SHERBROOK = "SHERBROOK";
    public static final String EVENT_SERVER_QUEBEC = "QUEBEC";
    public static final String EVENT_SERVER_MONTREAL = "MONTREAL";
    private String serverID;
    private String serverName;
    // HashMap<EventType, HashMap <EventID, Event>>
    private Map<String, Map<String, EventModel>> allEvents;
    // HashMap<CustomerID, HashMap <EventType, List<EventID>>>
    private Map<String, Map<String, List<String>>> clientEvents;
    // HashMap<ClientID, Client>
    private Map<String, ClientModel> serverClients;

    public EventManagement(String serverID, String serverName) throws RemoteException {
        super();
        this.serverID = serverID;
        this.serverName = serverName;
        allEvents = new ConcurrentHashMap<>();
        allEvents.put(EventModel.CONFERENCES, new ConcurrentHashMap<>());
        allEvents.put(EventModel.SEMINARS, new ConcurrentHashMap<>());
        allEvents.put(EventModel.TRADE_SHOWS, new ConcurrentHashMap<>());
        clientEvents = new ConcurrentHashMap<>();
        serverClients = new ConcurrentHashMap<>();
//        addTestData();
    }

    private static int getServerPort(String branchAcronym) {
        if (branchAcronym.equalsIgnoreCase("MTL")) {
            return Montreal_Server_Port;
        } else if (branchAcronym.equalsIgnoreCase("SHE")) {
            return Sherbrooke_Server_Port;
        } else if (branchAcronym.equalsIgnoreCase("QUE")) {
            return Quebec_Server_Port;
        }
        return 1;
    }

    private void addTestData() {
//        ClientModel testManager = new ClientModel(serverID + "M1111");
        ClientModel testCustomer = new ClientModel(serverID + "C1111");
//        serverClients.put(testManager.getClientID(), testManager);
        serverClients.put(testCustomer.getClientID(), testCustomer);
        clientEvents.put(testCustomer.getClientID(), new ConcurrentHashMap<>());

        EventModel sampleConf = new EventModel(EventModel.CONFERENCES, serverID + "M010120", 5);
        sampleConf.addRegisteredClientID(testCustomer.getClientID());
        clientEvents.get(testCustomer.getClientID()).put(sampleConf.getEventType(), new ArrayList<>());
        clientEvents.get(testCustomer.getClientID()).get(sampleConf.getEventType()).add(sampleConf.getEventID());

        EventModel sampleTrade = new EventModel(EventModel.TRADE_SHOWS, serverID + "A020220", 15);
        sampleTrade.addRegisteredClientID(testCustomer.getClientID());
        clientEvents.get(testCustomer.getClientID()).put(sampleTrade.getEventType(), new ArrayList<>());
        clientEvents.get(testCustomer.getClientID()).get(sampleTrade.getEventType()).add(sampleTrade.getEventID());

        EventModel sampleSemi = new EventModel(EventModel.SEMINARS, serverID + "E030320", 20);
        sampleSemi.addRegisteredClientID(testCustomer.getClientID());
        clientEvents.get(testCustomer.getClientID()).put(sampleSemi.getEventType(), new ArrayList<>());
        clientEvents.get(testCustomer.getClientID()).get(sampleSemi.getEventType()).add(sampleSemi.getEventID());

        allEvents.get(EventModel.CONFERENCES).put(sampleConf.getEventID(), sampleConf);
        allEvents.get(EventModel.TRADE_SHOWS).put(sampleTrade.getEventID(), sampleTrade);
        allEvents.get(EventModel.SEMINARS).put(sampleSemi.getEventID(), sampleSemi);
    }

    @Override
    public String addEvent(String eventID, String eventType, int bookingCapacity) throws RemoteException {
        String response;
        if (isEventOfThisServer(eventID)) {
            if (eventExists(eventType, eventID)) {
                if (allEvents.get(eventType).get(eventID).getEventCapacity() <= bookingCapacity) {
                    allEvents.get(eventType).get(eventID).setEventCapacity(bookingCapacity);
                    response = "Success: Event " + eventID + " Capacity increased to " + bookingCapacity;
                    try {
                        Logger.serverLog(serverID, "null", " addEvent ", " eventID: " + eventID + " eventType: " + eventType + " bookingCapacity " + bookingCapacity + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.addEventOutput(true, CommonOutput.addEvent_success_capacity_updated);
                } else {
                    response = "Fail: Event Already Exists, Cannot Decrease Booking Capacity";
                    try {
                        Logger.serverLog(serverID, "null", " addEvent ", " eventID: " + eventID + " eventType: " + eventType + " bookingCapacity " + bookingCapacity + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.addEventOutput(false, CommonOutput.addEvent_fail_cannot_decrease_capacity);
                }
            } else {
                EventModel event = new EventModel(eventType, eventID, bookingCapacity);
                Map<String, EventModel> eventHashMap = allEvents.get(eventType);
                eventHashMap.put(eventID, event);
                allEvents.put(eventType, eventHashMap);
                response = "Success: Event " + eventID + " added successfully";
                try {
                    Logger.serverLog(serverID, "null", " addEvent ", " eventID: " + eventID + " eventType: " + eventType + " bookingCapacity " + bookingCapacity + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.addEventOutput(true, CommonOutput.addEvent_success_added);
            }
        } else {
            response = "Fail: Cannot Add Event to servers other than " + serverName;
            try {
                Logger.serverLog(serverID, "null", " addEvent ", " eventID: " + eventID + " eventType: " + eventType + " bookingCapacity " + bookingCapacity + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.addEventOutput(false, null);
        }
    }

    @Override
    public String removeEvent(String eventID, String eventType) throws RemoteException {
        String response;
        if (isEventOfThisServer(eventID)) {
            if (eventExists(eventType, eventID)) {
                List<String> registeredClients = allEvents.get(eventType).get(eventID).getRegisteredClientIDs();
                allEvents.get(eventType).remove(eventID);
                addCustomersToNextSameEvent(eventID, eventType, registeredClients);
                response = "Success: Event " + eventID + " Removed Successfully";
                try {
                    Logger.serverLog(serverID, "null", " removeEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.removeEventOutput(true, null);
            } else {
                response = "Fail: Event " + eventID + " Does Not Exist";
                try {
                    Logger.serverLog(serverID, "null", " removeEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.removeEventOutput(false, CommonOutput.removeEvent_fail_no_such_event);
            }
        } else {
            response = "Fail: Cannot Remove Event from servers other than " + serverName;
            try {
                Logger.serverLog(serverID, "null", " removeEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.removeEventOutput(false, null);
        }
    }

    @Override
    public String listEventAvailability(String eventType) throws RemoteException {
        List<String> allEventIDsWithCapacity = new ArrayList<>();
        String response;
        Map<String, EventModel> events = allEvents.get(eventType);
        StringBuilder builder = new StringBuilder();
        builder.append(serverName).append(" Server ").append(eventType).append(":\n");
        if (events.size() == 0) {
            builder.append("No Events of Type ").append(eventType).append("\n");
        } else {
            for (EventModel event :
                    events.values()) {
                allEventIDsWithCapacity.add(event.getEventID() + " " + event.getEventRemainCapacity());
                builder.append(event.toString()).append(" || ");
            }
        }
        builder.append("\n=====================================\n");
        String otherServer1, otherServer2;
        if (serverID.equals("MTL")) {
            otherServer1 = sendUDPMessage(Sherbrooke_Server_Port, "listEventAvailability", "null", eventType, "null");
            otherServer2 = sendUDPMessage(Quebec_Server_Port, "listEventAvailability", "null", eventType, "null");
        } else if (serverID.equals("SHE")) {
            otherServer1 = sendUDPMessage(Quebec_Server_Port, "listEventAvailability", "null", eventType, "null");
            otherServer2 = sendUDPMessage(Montreal_Server_Port, "listEventAvailability", "null", eventType, "null");
        } else {
            otherServer1 = sendUDPMessage(Montreal_Server_Port, "listEventAvailability", "null", eventType, "null");
            otherServer2 = sendUDPMessage(Sherbrooke_Server_Port, "listEventAvailability", "null", eventType, "null");
        }
        List<String> otherServ1 = new ArrayList<>();
        List<String> otherServ2 = new ArrayList<>();
        otherServ1 = Arrays.asList(otherServer1.split("@"));
        otherServ2 = Arrays.asList(otherServer2.split("@"));
        allEventIDsWithCapacity.addAll(otherServ1);
        allEventIDsWithCapacity.addAll(otherServ2);
        builder.append(otherServer1).append(otherServer2);
        response = builder.toString();
        try {
            Logger.serverLog(serverID, "null", " listEventAvailability ", " eventType: " + eventType + " ", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CommonOutput.listEventAvailabilityOutput(true, allEventIDsWithCapacity, null);
    }

    @Override
    public String bookEvent(String customerID, String eventID, String eventType) throws RemoteException {
        String response;
        checkClientExists(customerID);
        if (isEventOfThisServer(eventID)) {
            EventModel bookedEvent = allEvents.get(eventType).get(eventID);
            if (bookedEvent == null) {
                response = "Fail: Event " + eventID + " Does not exists";
                try {
                    Logger.serverLog(serverID, customerID, " bookEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.bookEventOutput(false, CommonOutput.bookEvent_fail_no_such_event);
            }
            if (!bookedEvent.isFull()) {
                if (clientEvents.containsKey(customerID)) {
                    if (clientEvents.get(customerID).containsKey(eventType)) {
                        if (!clientHasEvent(customerID, eventType, eventID)) {
                            if (isCustomerOfThisServer(customerID))
                                clientEvents.get(customerID).get(eventType).add(eventID);
                        } else {
                            response = "Fail: Event " + eventID + " Already Booked";
                            try {
                                Logger.serverLog(serverID, customerID, " bookEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return CommonOutput.bookEventOutput(false, null);
                        }
                    } else {
                        if (isCustomerOfThisServer(customerID))
                            addEventTypeAndEvent(customerID, eventType, eventID);
                    }
                } else {
                    if (isCustomerOfThisServer(customerID))
                        addCustomerAndEvent(customerID, eventType, eventID);
                }
                if (allEvents.get(eventType).get(eventID).addRegisteredClientID(customerID) == EventModel.ADD_SUCCESS) {
                    response = "Success: Event " + eventID + " Booked Successfully";
                    response = CommonOutput.bookEventOutput(true, null);
                } else if (allEvents.get(eventType).get(eventID).addRegisteredClientID(customerID) == EventModel.EVENT_FULL) {
                    response = "Fail: Event " + eventID + " is Full";
                    response = CommonOutput.bookEventOutput(false, CommonOutput.bookEvent_fail_no_capacity);
                } else {
                    response = "Fail: Cannot Add You To Event " + eventID;
                    response = CommonOutput.bookEventOutput(false, null);
                }
                try {
                    Logger.serverLog(serverID, customerID, " bookEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            } else {
                response = "Fail: Event " + eventID + " is Full";
                try {
                    Logger.serverLog(serverID, customerID, " bookEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.bookEventOutput(false, CommonOutput.bookEvent_fail_no_capacity);
            }
        } else {
            if (clientHasEvent(customerID, eventType, eventID)) {
                String serverResponse = "Fail: Event " + eventID + " Already Booked";
                try {
                    Logger.serverLog(serverID, customerID, " bookEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", serverResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.bookEventOutput(false, null);
            }
            if (exceedWeeklyLimit(customerID, eventID.substring(4))) {
                String serverResponse = sendUDPMessage(getServerPort(eventID.substring(0, 3)), "bookEvent", customerID, eventType, eventID);
                if (serverResponse.startsWith("Success:")) {
                    if (clientEvents.get(customerID).containsKey(eventType)) {
                        clientEvents.get(customerID).get(eventType).add(eventID);
                    } else {
                        List<String> temp = new ArrayList<>();
                        temp.add(eventID);
                        clientEvents.get(customerID).put(eventType, temp);
                    }
                }
                try {
                    Logger.serverLog(serverID, customerID, " bookEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", serverResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return serverResponse;
            } else {
                response = "Fail: You Cannot Book Event in Other Servers For This Week(Max Weekly Limit = 3)";
                try {
                    Logger.serverLog(serverID, customerID, " bookEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.bookEventOutput(false, CommonOutput.bookEvent_fail_weekly_limit);
            }
        }
    }

    @Override
    public String getBookingSchedule(String customerID) throws RemoteException {
        String response;
        if (!checkClientExists(customerID)) {
            response = "Booking Schedule Empty For " + customerID;
            try {
                Logger.serverLog(serverID, customerID, " getBookingSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.getBookingScheduleOutput(true, new HashMap<>(), null);
        }
        Map<String, List<String>> events = clientEvents.get(customerID);
        if (events.size() == 0) {
            response = "Booking Schedule Empty For " + customerID;
            try {
                Logger.serverLog(serverID, customerID, " getBookingSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.getBookingScheduleOutput(true, events, null);
        }
        StringBuilder builder = new StringBuilder();
        for (String eventType :
                events.keySet()) {
            builder.append(eventType).append(":\n");
            for (String eventID :
                    events.get(eventType)) {
                builder.append(eventID).append(" ||");
            }
            builder.append("\n=====================================\n");
        }
        response = builder.toString();
        try {
            Logger.serverLog(serverID, customerID, " getBookingSchedule ", "null", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CommonOutput.getBookingScheduleOutput(true, events, null);
    }

    @Override
    public String cancelEvent(String customerID, String eventID, String eventType) throws RemoteException {
        String response;
        if (isEventOfThisServer(eventID)) {
            if (isCustomerOfThisServer(customerID)) {
                if (!checkClientExists(customerID)) {
                    response = "Fail: You " + customerID + " Are Not Registered in " + eventID;
                    try {
                        Logger.serverLog(serverID, customerID, " cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.cancelEventOutput(false, CommonOutput.cancelEvent_fail_not_registered_in_event);
                } else {
                    if (removeEventIfExists(customerID, eventType, eventID)) {
                        allEvents.get(eventType).get(eventID).removeRegisteredClientID(customerID);
                        response = "Success: Event " + eventID + " Canceled for " + customerID;
                        try {
                            Logger.serverLog(serverID, customerID, " cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return CommonOutput.cancelEventOutput(true, null);
                    } else {
                        response = "Fail: You " + customerID + " Are Not Registered in " + eventID;
                        try {
                            Logger.serverLog(serverID, customerID, " cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return CommonOutput.cancelEventOutput(false, CommonOutput.cancelEvent_fail_not_registered_in_event);
                    }
                }
            } else {
                if (allEvents.get(eventType).get(eventID).removeRegisteredClientID(customerID)) {
                    response = "Success: Event " + eventID + " Canceled for " + customerID;
                    try {
                        Logger.serverLog(serverID, customerID, " cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.cancelEventOutput(true, null);
                } else {
                    response = "Fail: You " + customerID + " Are Not Registered in " + eventID;
                    try {
                        Logger.serverLog(serverID, customerID, " cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.cancelEventOutput(false, CommonOutput.cancelEvent_fail_not_registered_in_event);
                }
            }
        } else {
            if (isCustomerOfThisServer(customerID)) {
                if (checkClientExists(customerID)) {
                    if (removeEventIfExists(customerID, eventType, eventID)) {
                        response = sendUDPMessage(getServerPort(eventID.substring(0, 3)), "cancelEvent", customerID, eventType, eventID);
                        try {
                            Logger.serverLog(serverID, customerID, " cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return response;
                    }
                }
            }
            response = "Fail: You " + customerID + " Are Not Registered in " + eventID;
            try {
                Logger.serverLog(serverID, customerID, " cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.cancelEventOutput(false, CommonOutput.cancelEvent_fail_not_registered_in_event);
        }
    }

    @Override
    public String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType) throws RemoteException {
        String response;
        if (!checkClientExists(customerID)) {
            response = "Fail: You " + customerID + " Are Not Registered in " + oldEventID;
            try {
                Logger.serverLog(serverID, customerID, " swapEvent ", " oldEventID: " + oldEventID + " oldEventType: " + oldEventType + " newEventID: " + newEventID + " newEventType: " + newEventType + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.swapEventOutput(false, CommonOutput.swapEvent_fail_not_registered_in_event);
        } else {
            if (clientHasEvent(customerID, oldEventType, oldEventID)) {
                String bookResp = "Fail: did not send book request for your newEvent " + newEventID;
                String cancelResp = "Fail: did not send cancel request for your oldEvent " + oldEventID;
                synchronized (this) {
                    if (onTheSameWeek(newEventID.substring(4), oldEventID) && !exceedWeeklyLimit(customerID, newEventID.substring(4))) {
                        cancelResp = cancelEvent(customerID, oldEventID, oldEventType);
                        if (cancelResp.startsWith("Success:")) {
                            bookResp = bookEvent(customerID, newEventID, newEventType);
                        }
                    } else {
                        bookResp = bookEvent(customerID, newEventID, newEventType);
                        if (bookResp.startsWith("Success:")) {
                            cancelResp = cancelEvent(customerID, oldEventID, oldEventType);
                        }
                    }
                }
                if (bookResp.startsWith("Success:") && cancelResp.startsWith("Success:")) {
                    response = "Success: Event " + oldEventID + " swapped with " + newEventID;
                    response = CommonOutput.swapEventOutput(true, null);
                } else if (bookResp.startsWith("Success:") && cancelResp.startsWith("Fail:")) {
                    cancelEvent(customerID, newEventID, newEventType);
                    response = "Fail: Your oldEvent " + oldEventID + " Could not be Canceled reason: " + cancelResp;
                    response = CommonOutput.swapEventOutput(false, null);
                } else if (bookResp.startsWith("Fail:") && cancelResp.startsWith("Success:")) {
                    //hope this won't happen, but just in case.
                    String resp1 = bookEvent(customerID, oldEventID, oldEventType);
                    response = "Fail: Your newEvent " + newEventID + " Could not be Booked reason: " + bookResp + " And your old event Rolling back: " + resp1;
                    response = CommonOutput.swapEventOutput(false, null);
                } else {
                    response = "Fail: on Both newEvent " + newEventID + " Booking reason: " + bookResp + " and oldEvent " + oldEventID + " Canceling reason: " + cancelResp;
                    response = CommonOutput.swapEventOutput(false, null);
                }
                try {
                    Logger.serverLog(serverID, customerID, " swapEvent ", " oldEventID: " + oldEventID + " oldEventType: " + oldEventType + " newEventID: " + newEventID + " newEventType: " + newEventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            } else {
                response = "Fail: You " + customerID + " Are Not Registered in " + oldEventID;
                try {
                    Logger.serverLog(serverID, customerID, " swapEvent ", " oldEventID: " + oldEventID + " oldEventType: " + oldEventType + " newEventID: " + newEventID + " newEventType: " + newEventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.swapEventOutput(false, CommonOutput.swapEvent_fail_not_registered_in_event);
            }
        }
    }

    @Override
    public String shutDown() throws RemoteException {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // ignored
                }
                System.exit(1);
            }
        });
        return "Shutting down";
    }

    /**
     * for udp calls only
     *
     * @param oldEventID
     * @param eventType
     * @param customerID
     * @return
     */
    public String removeEventUDP(String oldEventID, String eventType, String customerID) throws RemoteException {
        String response;
        if (!checkClientExists(customerID)) {
            response = "Fail: You " + customerID + " Are Not Registered in " + oldEventID;
            return CommonOutput.removeEventOutput(false, null);
        } else {
            if (removeEventIfExists(customerID, eventType, oldEventID)) {
                response = "Success: Event " + oldEventID + " Was Removed from " + customerID + " Schedule";
                return CommonOutput.removeEventOutput(true, null);
            } else {
                response = "Fail: You " + customerID + " Are Not Registered in " + oldEventID;
                return CommonOutput.removeEventOutput(false, null);
            }
        }
    }

    /**
     * for UDP calls only
     *
     * @param eventType
     * @return
     */
    public String listEventAvailabilityUDP(String eventType) throws RemoteException {
        Map<String, EventModel> events = allEvents.get(eventType);
        StringBuilder builder = new StringBuilder();
        StringBuilder builder2 = new StringBuilder();
        builder.append(serverName).append(" Server ").append(eventType).append(":\n");
        if (events.size() == 0) {
            builder.append("No Events of Type ").append(eventType);
        } else {
            for (EventModel event :
                    events.values()) {
                builder.append(event.toString()).append(" || ");
                builder2.append(event.getEventID()).append(" ").append(event.getEventRemainCapacity()).append("@");
            }
        }
        builder.append("\n=====================================\n");
        String newResponse = builder2.toString();
        if (newResponse.endsWith("@"))
            newResponse = newResponse.substring(0, newResponse.length() - 1);
        return newResponse;
    }

    private String sendUDPMessage(int serverPort, String method, String customerID, String eventType, String eventId) {
        DatagramSocket aSocket = null;
        String result = "";
        String dataFromClient = method + ";" + customerID + ";" + eventType + ";" + eventId;
        try {
            Logger.serverLog(serverID, customerID, " UDP request sent " + method + " ", " eventID: " + eventId + " eventType: " + eventType + " ", " ... ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            aSocket = new DatagramSocket();
            byte[] message = dataFromClient.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, dataFromClient.length(), aHost, serverPort);
            aSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);
            result = new String(reply.getData()).trim();
            String[] parts = result.split(";");
            result = parts[0];
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
        try {
            Logger.serverLog(serverID, customerID, " UDP reply received" + method + " ", " eventID: " + eventId + " eventType: " + eventType + " ", result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

    private String getNextSameEvent(Set<String> keySet, String eventType, String oldEventID) throws RemoteException {
        List<String> sortedIDs = new ArrayList<String>(keySet);
        sortedIDs.add(oldEventID);
        sortedIDs.sort(new Comparator<String>() {
            @Override
            public int compare(String ID1, String ID2) {
                Integer timeSlot1 = 0;
                switch (ID1.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot1 = 1;
                        break;
                    case "A":
                        timeSlot1 = 2;
                        break;
                    case "E":
                        timeSlot1 = 3;
                        break;
                }
                int timeSlot2 = 0;
                switch (ID2.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot2 = 1;
                        break;
                    case "A":
                        timeSlot2 = 2;
                        break;
                    case "E":
                        timeSlot2 = 3;
                        break;
                }
                Integer date1 = Integer.parseInt(ID1.substring(8, 10) + ID1.substring(6, 8) + ID1.substring(4, 6));
                Integer date2 = Integer.parseInt(ID2.substring(8, 10) + ID2.substring(6, 8) + ID2.substring(4, 6));
                int dateCompare = date1.compareTo(date2);
                int timeSlotCompare = timeSlot1.compareTo(timeSlot2);
                if (dateCompare == 0) {
                    return ((timeSlotCompare == 0) ? dateCompare : timeSlotCompare);
                } else {
                    return dateCompare;
                }
            }
        });
        int index = sortedIDs.indexOf(oldEventID) + 1;
        for (int i = index; i < sortedIDs.size(); i++) {
            if (!allEvents.get(eventType).get(sortedIDs.get(i)).isFull()) {
                return sortedIDs.get(i);
            }
        }
        return "Fail";
    }

    private boolean exceedWeeklyLimit(String customerID, String eventDate) throws RemoteException {
        int limit = 0;
        for (int i = 0; i < 3; i++) {
            List<String> registeredIDs = new ArrayList<>();
            switch (i) {
                case 0:
                    if (clientEvents.get(customerID).containsKey(EventModel.CONFERENCES)) {
                        registeredIDs = clientEvents.get(customerID).get(EventModel.CONFERENCES);
                    }
                    break;
                case 1:
                    if (clientEvents.get(customerID).containsKey(EventModel.SEMINARS)) {
                        registeredIDs = clientEvents.get(customerID).get(EventModel.SEMINARS);
                    }
                    break;
                case 2:
                    if (clientEvents.get(customerID).containsKey(EventModel.TRADE_SHOWS)) {
                        registeredIDs = clientEvents.get(customerID).get(EventModel.TRADE_SHOWS);
                    }
                    break;
            }
            for (String eventID :
                    registeredIDs) {
                if (onTheSameWeek(eventDate, eventID) && !isEventOfThisServer(eventID)) {
                    limit++;
                }
                if (limit == 3)
                    return false;
            }
        }
        return true;
    }

    private void addCustomersToNextSameEvent(String oldEventID, String eventType, List<String> registeredClients) throws RemoteException {
        for (String customerID :
                registeredClients) {
            if (customerID.substring(0, 3).equals(serverID)) {
                removeEventIfExists(customerID, eventType, oldEventID);
                tryToBookNextSameEvent(customerID, eventType, oldEventID);
            } else {
                String res = sendUDPMessage(getServerPort(customerID.substring(0, 3)), "removeEvent", customerID, eventType, oldEventID);
                if (res.startsWith("Success:")) {
                    tryToBookNextSameEvent(customerID, eventType, oldEventID);
                } else {
                    String response = "Acquiring nextSameEvent for Client (" + customerID + "):" + res;
                    try {
                        Logger.serverLog(serverID, customerID, " addCustomersToNextSameEvent ", " oldEventID: " + oldEventID + " eventType: " + eventType + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void tryToBookNextSameEvent(String customerID, String eventType, String oldEventID) throws RemoteException {
        String response;
        String nextSameEventResult = getNextSameEvent(allEvents.get(eventType).keySet(), eventType, oldEventID);
        if (nextSameEventResult.equals("Fail")) {
            response = "Acquiring nextSameEvent for Client (" + customerID + "):" + nextSameEventResult;
            try {
                Logger.serverLog(serverID, customerID, " addCustomersToNextSameEvent ", " oldEventID: " + oldEventID + " eventType: " + eventType + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            bookEvent(customerID, nextSameEventResult, eventType);
        }
    }

    private synchronized boolean eventExists(String eventType, String eventID) {
        return allEvents.get(eventType).containsKey(eventID);
    }

    private synchronized boolean isEventOfThisServer(String eventID) {
        return EventModel.detectEventServer(eventID).equals(serverName);
    }

    private synchronized boolean checkClientExists(String customerID) {
        if (!serverClients.containsKey(customerID)) {
            addNewCustomerToClients(customerID);
            return false;
        } else {
            return true;
        }
    }

    private synchronized boolean clientHasEvent(String customerID, String eventType, String eventID) {
        if (clientEvents.get(customerID).containsKey(eventType)) {
            return clientEvents.get(customerID).get(eventType).contains(eventID);
        } else {
            return false;
        }
    }

    private boolean removeEventIfExists(String customerID, String eventType, String eventID) {
        if (clientEvents.get(customerID).containsKey(eventType)) {
            return clientEvents.get(customerID).get(eventType).remove(eventID);
        } else {
            return false;
        }
    }

    private synchronized void addCustomerAndEvent(String customerID, String eventType, String eventID) {
        Map<String, List<String>> temp = new ConcurrentHashMap<>();
        List<String> temp2 = new ArrayList<>();
        temp2.add(eventID);
        temp.put(eventType, temp2);
        clientEvents.put(customerID, temp);
    }

    private synchronized void addEventTypeAndEvent(String customerID, String eventType, String eventID) {
        List<String> temp = new ArrayList<>();
        temp.add(eventID);
        clientEvents.get(customerID).put(eventType, temp);
    }

    private boolean isCustomerOfThisServer(String customerID) {
        return customerID.substring(0, 3).equals(serverID);
    }

    private boolean onTheSameWeek(String newEventDate, String eventID) {
        if (eventID.substring(6, 8).equals(newEventDate.substring(2, 4)) && eventID.substring(8, 10).equals(newEventDate.substring(4, 6))) {
            int week1 = Integer.parseInt(eventID.substring(4, 6)) / 7;
            int week2 = Integer.parseInt(newEventDate.substring(0, 2)) / 7;
//                    int diff = Math.abs(day2 - day1);
            return week1 == week2;
        } else {
            return false;
        }
    }

    public Map<String, Map<String, EventModel>> getAllEvents() {
        return allEvents;
    }

    public Map<String, Map<String, List<String>>> getClientEvents() {
        return clientEvents;
    }

    public Map<String, ClientModel> getServerClients() {
        return serverClients;
    }

    public void addNewEvent(String eventID, String eventType, int capacity) {
        EventModel sampleConf = new EventModel(eventType, eventID, capacity);
        allEvents.get(eventType).put(eventID, sampleConf);
    }

    public void addNewCustomerToClients(String customerID) {
        ClientModel newCustomer = new ClientModel(customerID);
        serverClients.put(newCustomer.getClientID(), newCustomer);
        clientEvents.put(newCustomer.getClientID(), new ConcurrentHashMap<>());
    }
}
