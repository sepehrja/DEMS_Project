package Replica2.com.sepehr;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    /**
     * Only manager
     */
    String addEvent(String eventID, String eventType, int bookingCapacity) throws RemoteException;

    String removeEvent(String eventID, String eventType) throws RemoteException;

    String listEventAvailability(String eventType) throws RemoteException;

    /**
     * Both manager and Customer
     */
    String bookEvent(String customerID, String eventID, String eventType) throws RemoteException;

    String getBookingSchedule(String customerID) throws RemoteException;

    String cancelEvent(String customerID, String eventID, String eventType) throws RemoteException;

    String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType) throws RemoteException;

}
