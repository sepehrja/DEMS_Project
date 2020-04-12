package Replica3.implementation;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Manager extends Remote{

	//public int add(int x, int y) throws RemoteException;
	public String addEvent (String eventID,String eventType,int bookingCapacity,String serv) throws RemoteException;
	public String removeEvent (String eventID,String eventType,String serv) throws RemoteException;
	public String listEventAvailability (String eventType,String serv) throws RemoteException;
	       
	public String bookEvent (String customerID,String eventID,String eventType,String serv) throws RemoteException;
	public String getBookingSchedule (String customerID,String serv) throws RemoteException;
	public String cancelEvent (String customerID,String eventID,String eventType,String serv)  throws RemoteException;
	public String swapEvent(String customerID, String newEventID,
			String newEventType, String oldEventID, String oldEventType,
			String serv) throws RemoteException;
}
