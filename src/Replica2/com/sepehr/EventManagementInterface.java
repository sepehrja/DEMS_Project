package Replica2.com.sepehr;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EventManagementInterface extends Remote {
    //Event Manager Role
    String addEvent(String eventID, String eventType, int bookingCapacity) throws RemoteException;

    String removeEvent(String eventID, String eventType) throws RemoteException;

    String listEventAvailability(String eventType) throws RemoteException;

    //Customer Role
    String bookEvent(String customerID, String eventID, String eventType) throws RemoteException;

    String getBookingSchedule(String customerID) throws RemoteException;

    String cancelEvent(String customerID, String eventID, String eventType) throws RemoteException;

    String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType) throws RemoteException;

    String shutDown() throws RemoteException;

}
