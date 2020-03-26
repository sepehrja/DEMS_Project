package Replica2.com.sepehr;

public interface ServerInterface {
    /**
     * Only manager
     */
    String addEvent(String eventID, String eventType, int bookingCapacity);

    String removeEvent(String eventID, String eventType);

    String listEventAvailability(String eventType);

    /**
     * Both manager and Customer
     */
    String bookEvent(String customerID, String eventID, String eventType);

    String getBookingSchedule(String customerID);

    String cancelEvent(String customerID, String eventID, String eventType);

    String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType);

}
