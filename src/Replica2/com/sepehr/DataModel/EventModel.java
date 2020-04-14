package Replica2.com.sepehr.DataModel;

import java.util.ArrayList;
import java.util.List;

import static Replica2.com.sepehr.ServerImplementation.EventManagement.*;


public class EventModel {
    public static final String EVENT_TIME_MORNING = "Morning";
    public static final String EVENT_TIME_AFTERNOON = "Afternoon";
    public static final String EVENT_TIME_EVENING = "Evening";
    public static final String CONFERENCES = "CONFERENCES";
    public static final String SEMINARS = "SEMINARS";
    public static final String TRADE_SHOWS = "TRADESHOWS";
    public static final int EVENT_FULL = -1;
    public static final int ALREADY_REGISTERED = 0;
    public static final int ADD_SUCCESS = 1;
    private String eventType;
    private String eventID;
    private String eventServer;
    private int eventCapacity;
    private String eventDate;
    private String eventTimeSlot;
    private List<String> registeredClients;

    public EventModel(String eventType, String eventID, int eventCapacity) {
        this.eventID = eventID;
        this.eventType = eventType;
        this.eventCapacity = eventCapacity;
        this.eventTimeSlot = detectEventTimeSlot(eventID);
        this.eventServer = detectEventServer(eventID);
        this.eventDate = detectEventDate(eventID);
        registeredClients = new ArrayList<>();
    }

    public static String detectEventServer(String eventID) {
        if (eventID.substring(0, 3).equalsIgnoreCase("MTL")) {
            return EVENT_SERVER_MONTREAL;
        } else if (eventID.substring(0, 3).equalsIgnoreCase("QUE")) {
            return EVENT_SERVER_QUEBEC;
        } else {
            return EVENT_SERVER_SHERBROOK;
        }
    }

    public static String detectEventTimeSlot(String eventID) {
        if (eventID.substring(3, 4).equalsIgnoreCase("M")) {
            return EVENT_TIME_MORNING;
        } else if (eventID.substring(3, 4).equalsIgnoreCase("A")) {
            return EVENT_TIME_AFTERNOON;
        } else {
            return EVENT_TIME_EVENING;
        }
    }

    public static String detectEventDate(String eventID) {
        return eventID.substring(4, 6) + "/" + eventID.substring(6, 8) + "/20" + eventID.substring(8, 10);
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getEventServer() {
        return eventServer;
    }

    public void setEventServer(String eventServer) {
        this.eventServer = eventServer;
    }

    public int getEventCapacity() {
        return eventCapacity;
    }

    public void setEventCapacity(int eventCapacity) {
        this.eventCapacity = eventCapacity;
    }

    public int getEventRemainCapacity() {
        return eventCapacity - registeredClients.size();
    }

//    public void incrementEventCapacity() {
//        this.eventCapacity++;
//    }
//
//    public boolean decrementEventCapacity() {
//        if (!isFull()) {
//            this.eventCapacity--;
//            return true;
//        } else {
//            return false;
//        }
//    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTimeSlot() {
        return eventTimeSlot;
    }

    public void setEventTimeSlot(String eventTimeSlot) {
        this.eventTimeSlot = eventTimeSlot;
    }

    public boolean isFull() {
        return getEventCapacity() == registeredClients.size();
    }

    public List<String> getRegisteredClientIDs() {
        return registeredClients;
    }

    public void setRegisteredClientsIDs(List<String> registeredClientsIDs) {
        this.registeredClients = registeredClientsIDs;
    }

    public int addRegisteredClientID(String registeredClientID) {
        if (!isFull()) {
            if (registeredClients.contains(registeredClientID)) {
                return ALREADY_REGISTERED;
            } else {
                registeredClients.add(registeredClientID);
                return ADD_SUCCESS;
            }
        } else {
            return EVENT_FULL;
        }
    }

    public boolean removeRegisteredClientID(String registeredClientID) {
        return registeredClients.remove(registeredClientID);
    }

    @Override
    public String toString() {
        return " (" + getEventID() + ") in the " + getEventTimeSlot() + " of " + getEventDate() + " Total[Remaining] Capacity: " + getEventCapacity() + "[" + getEventRemainCapacity() + "]";
    }
}
