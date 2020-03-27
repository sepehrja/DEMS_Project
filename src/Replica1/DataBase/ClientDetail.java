package DataBase;

public class ClientDetail {
	public String customerID, eventID, eventType;
	public int outer_city_limit;
	
	public ClientDetail(String customerID, String eventType, String eventID, int outer_city_limit)
	{
		this.customerID = customerID;
		this.eventType = eventType;
		this.eventID = eventID;
		this.outer_city_limit = outer_city_limit;
	}
}
