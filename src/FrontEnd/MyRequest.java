package FrontEnd;


public class MyRequest {
    private String function = "null";
    private String clientID = "null";
    private String eventType = "null";
    private String OldEventType = "null";
    private String eventID = "null";
    private String OldEventID = "null";
    private String FeIpAddress = FE.FE_IP_Address;
    private int bookingCapacity = -1;
    private int sequenceNumber = 0;
    private String MessageType = "00";
    private int retryCount = 1;

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

    public String getOldEventType() {
        return OldEventType;
    }

    public void setOldEventType(String OldEventType) {
        this.OldEventType = OldEventType;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getOldEventID() {
        return OldEventID;
    }

    public void setOldEventID(String OldEventID) {
        this.OldEventID = OldEventID;
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

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getFeIpAddress() {
        return FeIpAddress;
    }

    public void setFeIpAddress(String feIpAddress) {
        FeIpAddress = feIpAddress;
    }

    public String getMessageType() {
        return MessageType;
    }

    public void setMessageType(String messageType) {
        MessageType = messageType;
    }

    public boolean haveRetries() {
        return retryCount > 0;
    }

    public void countRetry() {
        retryCount--;
    }

    //Message Format: Sequence_id;FrontIpAddress;Message_Type;function(addEvent,...);userID; newEventID;newEventType; oldEventID; oldEventType;bookingCapacity
    @Override
    public String toString() {
        return getSequenceNumber() + ";" +
                getFeIpAddress().toUpperCase() + ";" +
                getMessageType().toUpperCase() + ";" +
                getFunction().toUpperCase() + ";" +
                getClientID().toUpperCase() + ";" +
                getEventID().toUpperCase() + ";" +
                getEventType().toUpperCase() + ";" +
                getOldEventID().toUpperCase() + ";" +
                getOldEventType().toUpperCase() + ";" +
                getBookingCapacity() + ";";
    }
}
