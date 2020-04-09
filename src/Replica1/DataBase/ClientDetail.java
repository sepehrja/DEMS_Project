package Replica1.DataBase;

public class ClientDetail {
	public String customerID, eventID, eventType;
	public int outer_city_limit;
	
	public ClientDetail(String customerID, String eventType, String eventID)
	{
		this.customerID = customerID;
		this.eventType = eventType;
		this.eventID = eventID;
	}
}
