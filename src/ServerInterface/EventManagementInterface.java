package ServerInterface;

import java.rmi.*;

public interface EventManagementInterface extends Remote{
	//Event Manager Role
	public String addEvent(String eventID,String eventType,int bookingCapacity) throws RemoteException;
	public String removeEvent(String eventID,String eventType) throws RemoteException;
	public String listEventAvailability(String eventType) throws RemoteException;
	//Customer Role
	public String bookEvent(String customerID,String eventID,String eventType) throws RemoteException;
	public String getBookingSchedule(String customerID) throws RemoteException;
	public String cancelEvent(String customerID,String eventID, String eventType) throws RemoteException;
    public String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType) throws RemoteException;
}
