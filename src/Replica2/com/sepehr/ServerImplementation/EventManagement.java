package Replica2.com.sepehr.ServerImplementation;

import Replica2.com.sepehr.DataModel.ClientModel;
import Replica2.com.sepehr.DataModel.EventModel;
import Replica2.com.sepehr.Logger.Logger;
import Replica2.com.sepehr.ServerInterface;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventManagement implements ServerInterface {
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

    public EventManagement(String serverID, String serverName) {
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
    public String addEvent(String eventID, String eventType, int bookingCapacity) {
        String response;
        if (isEventOfThisServer(eventID)) {
            if (eventExists(eventType, eventID)) {
                if (allEvents.get(eventType).get(eventID).getEventCapacity() <= bookingCapacity) {
                    allEvents.get(eventType).get(eventID).setEventCapacity(bookingCapacity);
                    response = "Success: Event " + eventID + " Capacity increased to " + bookingCapacity;
                    try {
                        Logger.serverLog(serverID, "null", " CORBA addEvent ", " eventID: " + eventID + " eventType: " + eventType + " bookingCapacity " + bookingCapacity + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                } else {
                    response = "Failed: Event Already Exists, Cannot Decrease Booking Capacity";
                    try {
                        Logger.serverLog(serverID, "null", " CORBA addEvent ", " eventID: " + eventID + " eventType: " + eventType + " bookingCapacity " + bookingCapacity + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                }
            } else {
                EventModel event = new EventModel(eventType, eventID, bookingCapacity);
                Map<String, EventModel> eventHashMap = allEvents.get(eventType);
                eventHashMap.put(eventID, event);
                allEvents.put(eventType, eventHashMap);
                response = "Success: Event " + eventID + " added successfully";
                try {
                    Logger.serverLog(serverID, "null", " CORBA addEvent ", " eventID: " + eventID + " eventType: " + eventType + " bookingCapacity " + bookingCapacity + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        } else {
            response = "Failed: Cannot Add Event to servers other than " + serverName;
            try {
                Logger.serverLog(serverID, "null", " CORBA addEvent ", " eventID: " + eventID + " eventType: " + eventType + " bookingCapacity " + bookingCapacity + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }

    @Override
    public String removeEvent(String eventID, String eventType) {
        String response;
        if (isEventOfThisServer(eventID)) {
            if (eventExists(eventType, eventID)) {
                List<String> registeredClients = allEvents.get(eventType).get(eventID).getRegisteredClientIDs();
                allEvents.get(eventType).remove(eventID);
                addCustomersToNextSameEvent(eventID, eventType, registeredClients);
                response = "Success: Event Removed Successfully";
                try {
                    Logger.serverLog(serverID, "null", " CORBA removeEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            } else {
                response = "Failed: Event " + eventID + " Does Not Exist";
                try {
                    Logger.serverLog(serverID, "null", " CORBA removeEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        } else {
            response = "Failed: Cannot Remove Event from servers other than " + serverName;
            try {
                Logger.serverLog(serverID, "null", " CORBA removeEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }

    @Override
    public String listEventAvailability(String eventType) {
        String response;
        Map<String, EventModel> events = allEvents.get(eventType);
        StringBuilder builder = new StringBuilder();
        builder.append(serverName + " Server " + eventType + ":\n");
        if (events.size() == 0) {
            builder.append("No Events of Type " + eventType + "\n");
        } else {
            for (EventModel event :
                    events.values()) {
                builder.append(event.toString() + " || ");
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
        builder.append(otherServer1).append(otherServer2);
        response = builder.toString();
        try {
            Logger.serverLog(serverID, "null", " CORBA listEventAvailability ", " eventType: " + eventType + " ", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public String bookEvent(String customerID, String eventID, String eventType) {
        String response;
        checkClientExists(customerID);
        if (isEventOfThisServer(eventID)) {
            EventModel bookedEvent = allEvents.get(eventType).get(eventID);
            if (!bookedEvent.isFull()) {
                if (clientEvents.containsKey(customerID)) {
                    if (clientEvents.get(customerID).containsKey(eventType)) {
                        if (!clientHasEvent(customerID, eventType, eventID)) {
                            clientEvents.get(customerID).get(eventType).add(eventID);
                        } else {
                            response = "Failed: Event " + eventID + " Already Booked";
                            try {
                                Logger.serverLog(serverID, customerID, " CORBA bookEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return response;
                        }
                    } else {
                        addEventTypeAndEvent(customerID, eventType, eventID);
                    }
                } else {
                    addCustomerAndEvent(customerID, eventType, eventID);
                }
                if (allEvents.get(eventType).get(eventID).addRegisteredClientID(customerID) == EventModel.ADD_SUCCESS) {
                    response = "Success: Event " + eventID + " Booked Successfully";
                } else if (allEvents.get(eventType).get(eventID).addRegisteredClientID(customerID) == EventModel.EVENT_FULL) {
                    response = "Failed: Event " + eventID + " is Full";
                } else {
                    response = "Failed: Cannot Add You To Event " + eventID;
                }
                try {
                    Logger.serverLog(serverID, customerID, " CORBA bookEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            } else {
                response = "Failed: Event " + eventID + " is Full";
                try {
                    Logger.serverLog(serverID, customerID, " CORBA bookEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        } else {
            if (!exceedWeeklyLimit(customerID, eventID.substring(4))) {
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
                    Logger.serverLog(serverID, customerID, " CORBA bookEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", serverResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return serverResponse;
            } else {
                response = "Failed: You Cannot Book Event in Other Servers For This Week(Max Weekly Limit = 3)";
                try {
                    Logger.serverLog(serverID, customerID, " CORBA bookEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        }
    }

    @Override
    public String getBookingSchedule(String customerID) {
        String response;
        if (!checkClientExists(customerID)) {
            response = "Booking Schedule Empty For " + customerID;
            try {
                Logger.serverLog(serverID, customerID, " CORBA getBookingSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
        Map<String, List<String>> events = clientEvents.get(customerID);
        if (events.size() == 0) {
            response = "Booking Schedule Empty For " + customerID;
            try {
                Logger.serverLog(serverID, customerID, " CORBA getBookingSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
        StringBuilder builder = new StringBuilder();
        for (String eventType :
                events.keySet()) {
            builder.append(eventType + ":\n");
            for (String eventID :
                    events.get(eventType)) {
                builder.append(eventID + " ||");
            }
            builder.append("\n=====================================\n");
        }
        response = builder.toString();
        try {
            Logger.serverLog(serverID, customerID, " CORBA getBookingSchedule ", "null", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public String cancelEvent(String customerID, String eventID, String eventType) {
        String response;
        if (isEventOfThisServer(eventID)) {
            if (isCustomerOfThisServer(customerID)) {
                if (!checkClientExists(customerID)) {
                    response = "Failed: You " + customerID + " Are Not Registered in " + eventID;
                    try {
                        Logger.serverLog(serverID, customerID, " CORBA cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                } else {
                    if (removeEventIfExists(customerID, eventType, eventID)) {
                        allEvents.get(eventType).get(eventID).removeRegisteredClientID(customerID);
                        response = "Success: Event " + eventID + " Canceled for " + customerID;
                        try {
                            Logger.serverLog(serverID, customerID, " CORBA cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return response;
                    } else {
                        response = "Failed: You " + customerID + " Are Not Registered in " + eventID;
                        try {
                            Logger.serverLog(serverID, customerID, " CORBA cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return response;
                    }
                }
            } else {
                if (allEvents.get(eventType).get(eventID).removeRegisteredClientID(customerID)) {
                    response = "Success: Event " + eventID + " Canceled for " + customerID;
                    try {
                        Logger.serverLog(serverID, customerID, " CORBA cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                } else {
                    response = "Failed: You " + customerID + " Are Not Registered in " + eventID;
                    try {
                        Logger.serverLog(serverID, customerID, " CORBA cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                }
            }
        } else {
            if (isCustomerOfThisServer(customerID)) {
                if (checkClientExists(customerID)) {
                    if (removeEventIfExists(customerID, eventType, eventID)) {
                        response = sendUDPMessage(getServerPort(eventID.substring(0, 3)), "cancelEvent", customerID, eventType, eventID);
                        try {
                            Logger.serverLog(serverID, customerID, " CORBA cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return response;
                    }
                }
            }
            response = "Failed: You " + customerID + " Are Not Registered in " + eventID;
            try {
                Logger.serverLog(serverID, customerID, " CORBA cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }

    @Override
    public String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType) {
        String response;
        if (!checkClientExists(customerID)) {
            response = "Failed: You " + customerID + " Are Not Registered in " + oldEventID;
            try {
                Logger.serverLog(serverID, customerID, " CORBA swapEvent ", " oldEventID: " + oldEventID + " oldEventType: " + oldEventType + " newEventID: " + newEventID + " newEventType: " + newEventType + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        } else {
            if (clientHasEvent(customerID, oldEventType, oldEventID)) {
                String bookResp = "Failed: did not send book request for your newEvent " + newEventID;
                String cancelResp = "Failed: did not send cancel request for your oldEvent " + oldEventID;
                synchronized (this) {
                    if (onTheSameWeek(newEventID.substring(4), oldEventID) && exceedWeeklyLimit(customerID, newEventID.substring(4))) {
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
                } else if (bookResp.startsWith("Success:") && cancelResp.startsWith("Failed:")) {
                    cancelEvent(customerID, newEventID, newEventType);
                    response = "Failed: Your oldEvent " + oldEventID + " Could not be Canceled reason: " + cancelResp;
                } else if (bookResp.startsWith("Failed:") && cancelResp.startsWith("Success:")) {
                    //hope this won't happen, but just in case.
                    bookEvent(customerID, oldEventID, oldEventType);
                    response = "Failed: Your newEvent " + newEventID + " Could not be Booked reason: " + bookResp;
                } else {
                    response = "Failed: on Both newEvent " + newEventID + " Booking reason: " + bookResp + " and oldEvent " + oldEventID + " Canceling reason: " + cancelResp;
                }
                try {
                    Logger.serverLog(serverID, customerID, " CORBA swapEvent ", " oldEventID: " + oldEventID + " oldEventType: " + oldEventType + " newEventID: " + newEventID + " newEventType: " + newEventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            } else {
                response = "Failed: You " + customerID + " Are Not Registered in " + oldEventID;
                try {
                    Logger.serverLog(serverID, customerID, " CORBA swapEvent ", " oldEventID: " + oldEventID + " oldEventType: " + oldEventType + " newEventID: " + newEventID + " newEventType: " + newEventType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        }
    }


    /**
     * for udp calls only
     *
     * @param oldEventID
     * @param eventType
     * @param customerID
     * @return
     */
    public String removeEventUDP(String oldEventID, String eventType, String customerID) {
        if (!checkClientExists(customerID)) {
            return "Failed: You " + customerID + " Are Not Registered in " + oldEventID;
        } else {
            if (removeEventIfExists(customerID, eventType, oldEventID)) {
                return "Success: Event " + oldEventID + " Was Removed from " + customerID + " Schedule";
            } else {
                return "Failed: You " + customerID + " Are Not Registered in " + oldEventID;
            }
        }
    }

    /**
     * for UDP calls only
     *
     * @param eventType
     * @return
     */
    public String listEventAvailabilityUDP(String eventType) {
        Map<String, EventModel> events = allEvents.get(eventType);
        StringBuilder builder = new StringBuilder();
        builder.append(serverName + " Server " + eventType + ":\n");
        if (events.size() == 0) {
            builder.append("No Events of Type " + eventType);
        } else {
            for (EventModel event :
                    events.values()) {
                builder.append(event.toString() + " || ");
            }
        }
        builder.append("\n=====================================\n");
        return builder.toString();
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
            result = new String(reply.getData());
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

    private String getNextSameEvent(Set<String> keySet, String eventType, String oldEventID) {
        List<String> sortedIDs = new ArrayList<String>(keySet);
        sortedIDs.add(oldEventID);
        Collections.sort(sortedIDs, new Comparator<String>() {
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
                Integer timeSlot2 = 0;
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
        return "Failed";
    }

    private boolean exceedWeeklyLimit(String customerID, String eventDate) {
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
                if (onTheSameWeek(eventDate, eventID)) {
                    limit++;
                }
                if (limit == 3)
                    return true;
            }
        }
        return false;
    }

    private void addCustomersToNextSameEvent(String oldEventID, String eventType, List<String> registeredClients) {
        String response;
        for (String customerID :
                registeredClients) {
            if (customerID.substring(0, 3).equals(serverID)) {
                removeEventIfExists(customerID, eventType, oldEventID);
                String nextSameEventResult = getNextSameEvent(allEvents.get(eventType).keySet(), eventType, oldEventID);
                if (nextSameEventResult.equals("Failed")) {
                    response = "Acquiring nextSaneEvent :" + nextSameEventResult;
                    try {
                        Logger.serverLog(serverID, customerID, " addCustomersToNextSameEvent ", " oldEventID: " + oldEventID + " eventType: " + eventType + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                } else {
                    bookEvent(customerID, nextSameEventResult, eventType);
                }
            } else {
                sendUDPMessage(getServerPort(customerID.substring(0, 3)), "removeEvent", customerID, eventType, oldEventID);
            }
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
