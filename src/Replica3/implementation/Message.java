package Replica3.implementation;

public class Message {
	public String FrontIpAddress,Function , MessageType, userID, newEventID, newEventType, oldEventID, oldEventType; 
	public int bookingCapacity, sequenceId; 
		  
	public Message(int sequenceId, String FrontIpAddress,String MessageType, String Function, String userID, String newEventID,
					String newEventType,String oldEventID,String oldEventType,int bookingCapacity) 
	{ 
		this.sequenceId = sequenceId; 
		this.FrontIpAddress = FrontIpAddress; 
		this.MessageType = MessageType; 
		this.Function = Function; 
		this.userID = userID; 
		this.newEventID = newEventID; 
		this.newEventType = newEventType; 
		this.oldEventID = oldEventID; 
		this.oldEventType = oldEventType; 
		this.bookingCapacity = bookingCapacity; 
	}
    @Override
    public String toString() {
		return sequenceId + ";" + FrontIpAddress + ";" +MessageType + ";" +Function + ";" +userID + ";" +newEventID + 
		";" +newEventType + ";" +oldEventID + ";" +oldEventType + ";" +bookingCapacity;
    }
}
