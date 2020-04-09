package FrontEnd;


public class MyRequest {
    private String function = "null";
    private String clientID = "null";
    private String eventType = "null";
    private String newEventType = "null";
    private String eventID = "null";
    private String newEventID = "null";
    private int bookingCapacity = -1;

    public MyRequest(String function, String clientID) {
        setFunction(function);
        setClientID(clientID);
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getNewEventType() {
        return newEventType;
    }

    public void setNewEventType(String newEventType) {
        this.newEventType = newEventType;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getNewEventID() {
        return newEventID;
    }

    public void setNewEventID(String newEventID) {
        this.newEventID = newEventID;
    }

    public int getBookingCapacity() {
        return bookingCapacity;
    }

    public void setBookingCapacity(int bookingCapacity) {
        this.bookingCapacity = bookingCapacity;
    }

    public String noRequestSendError() {
        return "request: " + getFunction() + " from " + getClientID() + " not sent";
    }

    @Override
    public String toString() {
        return getFunction() + ";" + getClientID() + ";" + getEventID() + ";" + getEventType() + ";" + getBookingCapacity() + ";" + getNewEventID() + ";" + getNewEventType() + ";";
    }
}
