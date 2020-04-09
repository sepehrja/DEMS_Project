package Replica1.DataBase;

public class EventDetail {
	public String eventType, eventID;
	public int bookingCapacity;
	
	public EventDetail(String eventType, String eventID, int bookingCapacity)
	{
		this.eventType = eventType;
		this.eventID = eventID;
		this.bookingCapacity = bookingCapacity;
	}
//	
//    @Override
//    public String toString() {
//    	return eventType + ":" + eventID;
//    }
}
