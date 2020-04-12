package Replica1.ImplementRemoteInterface;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.*; 
import Replica1.CommonOutput;

import Replica1.ServerInterface.EventManagementInterface;
import Replica1.DataBase.*;

public class ServerClass extends UnicastRemoteObject implements EventManagementInterface{
	
	private final ConcurrentHashMap<String, ConcurrentHashMap<String, EventDetail>> EventMap;
	private final ConcurrentHashMap<String, ConcurrentHashMap<String, ClientDetail>> ClientMap;
	private final int quebec_port, montreal_port, sherbrooke_port;
	private final String serverName;
	public ServerClass(final int quebec_port, final int montreal_port, final int sherbrooke_port, final String serverName) throws RemoteException {
		super();
		
		this.quebec_port = quebec_port;
		this.montreal_port = montreal_port;
		this.sherbrooke_port = sherbrooke_port;
		this.serverName = serverName.toUpperCase().trim();
		EventMap = new ConcurrentHashMap<>();
		ClientMap = new ConcurrentHashMap<>();
		
	}

	@Override
	public synchronized String addEvent(final String eventID, final String eventType, final int bookingCapacity) throws RemoteException {
		if(EventMap.containsKey(eventType.toUpperCase().trim()) && EventMap.get(eventType.toUpperCase().trim()).containsKey(eventID.toUpperCase().trim()))
		{
			final int currentCapacity = EventMap.get(eventType.toUpperCase().trim()).get(eventID.toUpperCase().trim()).bookingCapacity;
			EventMap.get(eventType.toUpperCase().trim()).replace(eventID,new EventDetail(eventType.toUpperCase().trim(), eventID.toUpperCase().trim(), currentCapacity + bookingCapacity));

			try 
			{
				serverLog("Add event", " EventType:"+eventType+ " EventID:"+eventID +
						"bookingCapacity:"+ bookingCapacity,"successfully completed", "Capacity added to event");				
			} catch (final IOException e) {
				e.printStackTrace();
			}
			return CommonOutput.addEventOutput(true, CommonOutput.addEvent_success_capacity_updated);
		}
		else if(EventMap.containsKey(eventType.toUpperCase().trim()))
		{
			EventMap.get(eventType.toUpperCase().trim()).put(eventID.toUpperCase().trim(), new EventDetail(eventType.toUpperCase().trim(), eventID.toUpperCase().trim(), bookingCapacity));
			try 
			{
				serverLog("Add event", " EventType:"+eventType+ " EventID:"+eventID +
						"bookingCapacity:"+ bookingCapacity,"successfully completed", "Event added to" + serverName.toUpperCase().trim());
			} catch (final IOException e) {
				e.printStackTrace();
			}
			return CommonOutput.addEventOutput(true, CommonOutput.addEvent_success_added);
		}	
		else
		{
			final ConcurrentHashMap <String, EventDetail> subHashMap = new ConcurrentHashMap<>();
			subHashMap.put(eventID.toUpperCase().trim(), new EventDetail(eventType.toUpperCase().trim(), eventID.toUpperCase().trim(), bookingCapacity));
			EventMap.put(eventType.toUpperCase().trim(), subHashMap);
			try 
			{
				serverLog("Add event", " EventType:"+eventType+ " EventID:"+eventID +
						"bookingCapacity:"+ bookingCapacity,"successfully completed", "Event added to" + serverName.toUpperCase().trim());
			} catch (final IOException e) {
				e.printStackTrace();
			}
			return CommonOutput.addEventOutput(true, CommonOutput.addEvent_success_added);
		}
	}
	@Override
	public synchronized String removeEvent(final String eventID, final String eventType) throws RemoteException{
		if(EventMap.containsKey(eventType.toUpperCase().trim()) && EventMap.get(eventType.toUpperCase().trim()).containsKey(eventID.toUpperCase().trim()))
		{			
			String response="";
			final String branch = eventID.substring(0,3).toUpperCase().trim();
			EventMap.get(eventType.toUpperCase().trim()).remove(eventID.toUpperCase().trim());
			try {
				serverLog("Remove event", " EventType:"+eventType+ " EventID:"+eventID
						,"successfully completed", "Event removed from server" + serverName.toUpperCase().trim());
			} catch (final IOException e) {
				e.printStackTrace();
			}
			
			response = remove_client_event(eventID.toUpperCase().trim(), eventType.toUpperCase().trim());
			
			if(branch.trim().equals("QUE"))
			{
				send_data_request(montreal_port, "remove_client_event", eventID.toUpperCase().trim(), eventType.toUpperCase().trim(),"-").trim();
				send_data_request(sherbrooke_port, "remove_client_event",eventID.toUpperCase().trim(), eventType.toUpperCase().trim(),"-").trim();
			}
			else if(branch.trim().equals("MTL"))
			{
				send_data_request(quebec_port, "remove_client_event", eventID.toUpperCase().trim(), eventType.toUpperCase().trim(),"-").trim();
				send_data_request(sherbrooke_port, "remove_client_event", eventID.toUpperCase().trim(), eventType.toUpperCase().trim(),"-").trim();

			}
			else if(branch.trim().equals("SHE"))
			{
				send_data_request(montreal_port, "remove_client_event", eventID.toUpperCase().trim(), eventType.toUpperCase().trim(),"-").trim();
				send_data_request(quebec_port, "remove_client_event", eventID.toUpperCase().trim(), eventType.toUpperCase().trim(),"-").trim();
			}
						
			return CommonOutput.removeEventOutput(true, null);
		}
		else
		{
			return CommonOutput.removeEventOutput(false, CommonOutput.removeEvent_fail_no_such_event);
		}
	}
	
	public String remove_client_event(final String eventID, final String eventType) 
	{
		String data = "";
		String new_eventID = "";
		for(final Entry<String, ConcurrentHashMap<String, ClientDetail>> customer : ClientMap.entrySet())
		{
			final ConcurrentHashMap<String, ClientDetail> eventDetail = customer.getValue();
			final String branch = eventID.substring(0,3).toUpperCase().trim();

			if(eventDetail.containsKey(eventType.toUpperCase().trim() +";"+ eventID.toUpperCase().trim()+""))
			{	
				eventDetail.remove(eventType.toUpperCase().trim() +";"+ eventID.toUpperCase().trim());

				for (final ConcurrentHashMap.Entry<String,ClientDetail> entry : customer.getValue().entrySet()) 
				{
					data +=(entry.getValue().eventID.toUpperCase().trim()+":");
				}
				if(branch.trim().equals("QUE"))
				{
					new_eventID = send_data_request(quebec_port, "boook_next_event", data,eventID.toUpperCase().trim() ,eventType.toUpperCase().trim()).trim();
				}
				else if(branch.trim().equals("MTL"))
				{
					new_eventID = send_data_request(montreal_port, "boook_next_event", data, eventID.toUpperCase().trim() ,eventType.toUpperCase().trim()).trim();

				}
				else if(branch.trim().equals("SHE"))
				{
					new_eventID = send_data_request(sherbrooke_port, "boook_next_event", data, eventID.toUpperCase().trim() ,eventType.toUpperCase().trim()).trim();
				}

				try 
				{
					if(new_eventID.trim().equals(""))
					{
						serverLog("Remove event", " EventType:"+eventType+ " EventID:"+eventID,"successfully completed", 
							"Event removed for client:" + customer.getKey().toUpperCase().trim());
					
						clientLog(customer.getKey().toUpperCase().trim(), "Remove event", "eventType:" + eventType+" eventID:"+eventID+" has been removed");
					}
					else
					{
						add_book_customer(customer.getKey().toUpperCase().trim(),new_eventID, eventType);
						serverLog("Remove event", " EventType:"+eventType+ " EventID:"+new_eventID,"successfully completed", 
								"Event has been replaced for client:" + customer.getKey().toUpperCase().trim());
						
							clientLog(customer.getKey().toUpperCase().trim(), "Remove event", "eventType:" + eventType+" eventID:"+new_eventID+" has been replaced");
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return "Event with eventID:"+ eventID.toUpperCase().trim() +" and eventType: "+eventType.toUpperCase().trim() +" for clients has been removed for server\n";
	}
	
	public String boook_next_event(final String temp,final String removedEventID, final String eventType) {
		
		final String response="";
		final String [] data = temp.split(":");
		String eventID="";
		int capacity =0;
        List<String> sortedEventIDs = new ArrayList<String>();
		if(EventMap.containsKey(eventType.toUpperCase().trim()) && EventMap.get(eventType.toUpperCase().trim()).values().size() != 0)
		{
			sortedEventIDs = getSortedEventID(eventType, removedEventID);
			if(data.length!= 0)
			{
				for (int count = 0; count < sortedEventIDs.size(); count++) { 		      
					boolean check = false;
					for (int i =0; i< data.length; ++i)
					{
						capacity = EventMap.get(eventType).get(sortedEventIDs.get(count)).bookingCapacity;

						if(!(data[i].indexOf(EventMap.get(eventType).get(sortedEventIDs.get(count)).eventID)!=-1) && capacity!=0)
						{
							check = true;
						}
						else
						{
							check = false;
							break;
						}
					}
					if(check == true)
					{
						eventID = EventMap.get(eventType).get(sortedEventIDs.get(count)).eventID.toUpperCase().trim();
						EventMap.get(eventType.toUpperCase().trim()).replace(eventID,new EventDetail(eventType.toUpperCase().trim(), eventID.toUpperCase().trim(), capacity - 1));
						try 
						{
							serverLog("Remove event", " EventType:"+eventType+ " EventID:"+eventID,"successfully completed", 
									"Next available event replaced for client:");
						} catch (final IOException e) {
							e.printStackTrace();
						}
						return eventID;
					}
				}
			}
			else
			{
				capacity = EventMap.get(eventType).get(sortedEventIDs.get(0)).bookingCapacity;
				if(capacity!=0)
				{
					eventID = EventMap.get(eventType).get(sortedEventIDs.get(0)).eventID.toUpperCase().trim();
					EventMap.get(eventType.toUpperCase().trim()).replace(eventID,new EventDetail(eventType.toUpperCase().trim(), eventID.toUpperCase().trim(), capacity - 1));
					try 
					{
						serverLog("Remove event", " EventType:"+eventType+ " EventID:"+eventID,"successfully completed", 
								"Next available event replaced for client:");
					} catch (final IOException e) {
						e.printStackTrace();
					}
					return eventID;
				}
			}
		}

		return response;	
	}

	private List<String> getSortedEventID(final String eventType, final String removedEventID) {
        final List<String> sortedEventIDs = new ArrayList<String>();
        final List<String> morningEventIDs = new ArrayList<String>();
        final List<String> afternoonEventIDs = new ArrayList<String>();
        final List<String> eveningEventIDs = new ArrayList<String>();

		for (final ConcurrentHashMap.Entry<String,EventDetail> entry : EventMap.get(eventType).entrySet()) 
		{
			if(entry.getValue().eventID.substring(3,4).equals("M") && !removedEventID.substring(3,4).equals("A")&& !removedEventID.substring(3,4).equals("E"))
			{
				if(Integer.parseInt(entry.getValue().eventID.substring(8))>Integer.parseInt(removedEventID.substring(8))&& Integer.parseInt(entry.getValue().eventID.substring(6,8))>Integer.parseInt(removedEventID.substring(6,8))&& Integer.parseInt(entry.getValue().eventID.substring(4,6))>Integer.parseInt(removedEventID.substring(4,6)))
				{
					morningEventIDs.add(entry.getValue().eventID);
				}
			}
			else if(entry.getValue().eventID.substring(3,4).equals("A") && !removedEventID.substring(3,4).equals("E"))
			{
				if(Integer.parseInt(entry.getValue().eventID.substring(8))>Integer.parseInt(removedEventID.substring(8))&& Integer.parseInt(entry.getValue().eventID.substring(6,8))>Integer.parseInt(removedEventID.substring(6,8))&& Integer.parseInt(entry.getValue().eventID.substring(4,6))>Integer.parseInt(removedEventID.substring(4,6)))
				{
					afternoonEventIDs.add(entry.getValue().eventID);
				}
			}
			else if(entry.getValue().eventID.substring(3,4).equals("E"))
			{
				if(Integer.parseInt(entry.getValue().eventID.substring(8))>Integer.parseInt(removedEventID.substring(8))&& Integer.parseInt(entry.getValue().eventID.substring(6,8))>Integer.parseInt(removedEventID.substring(6,8))&& Integer.parseInt(entry.getValue().eventID.substring(4,6))>Integer.parseInt(removedEventID.substring(4,6)))
				{
					eveningEventIDs.add(entry.getValue().eventID);
				}
			}
		}
		
		sortByDate(morningEventIDs);
		sortByDate(afternoonEventIDs);
		sortByDate(eveningEventIDs);

		sortedEventIDs.addAll(morningEventIDs);
		sortedEventIDs.addAll(afternoonEventIDs);
		sortedEventIDs.addAll(eveningEventIDs);

		return sortedEventIDs;
	}
	
	private List<String> sortByDate(final List<String> list) {
		final int n = list.size(); 
		//sort by year
		for (int i = 0; i < n-1; i++) 
			for (int j = 0; j < n-i-1; j++) 
			{
				final int a = Integer.parseInt(list.get(j).substring(8));
				final int b = Integer.parseInt(list.get(j+1).substring(8));

				if (a > b) 
				{ 
			        Collections.swap(list, j, j+1);
				} 
			}
		//sort by month
		for (int i = 0; i < n-1; i++) 
			for (int j = 0; j < n-i-1; j++) 
			{
				final int a = Integer.parseInt(list.get(j).substring(6,8));
				final int b = Integer.parseInt(list.get(j+1).substring(6,8));

				if (a > b) 
				{ 
			        Collections.swap(list, j, j+1);
				} 
			}
		//sort by day
		for (int i = 0; i < n-1; i++) 
			for (int j = 0; j < n-i-1; j++) 
			{
				final int a = Integer.parseInt(list.get(j).substring(4,6));
				final int b = Integer.parseInt(list.get(j+1).substring(4,6));

				if (a > b) 
				{ 
			        Collections.swap(list, j, j+1);
				} 
			}
		return list;
	}

	@Override
	public String listEventAvailability(final String eventType) throws RemoteException{
		List<String> allEventIDsWithCapacity = new ArrayList<>();
		String response1="" ,response2="";
		List<String> server1 = new ArrayList<>();
        List<String> server2 = new ArrayList<>();
		if(EventMap.containsKey(eventType.toUpperCase().trim()))
		{
			for (final Map.Entry<String, EventDetail> entry : EventMap.get(eventType.toUpperCase().trim()).entrySet()) 
			{
				allEventIDsWithCapacity.add(entry.getKey() + " " + entry.getValue().bookingCapacity);
			}
		}
		if(serverName.trim().equals("QUE"))
		{
			response1 = send_data_request(montreal_port, "list_events", "-", eventType.toUpperCase().trim(),"-").trim();
			response2 = send_data_request(sherbrooke_port, "list_events", "-", eventType.toUpperCase().trim(),"-").trim();

		}
		else if(serverName.trim().equals("MTL"))
		{
			response1 = send_data_request(quebec_port, "list_events", "-", eventType.toUpperCase().trim(),"-").trim();
			response2 = send_data_request(sherbrooke_port, "list_events", "-", eventType.toUpperCase().trim(),"-").trim();
		}
		else if(serverName.trim().equals("SHE"))
		{
			response1 = send_data_request(montreal_port, "list_events", "-", eventType.toUpperCase().trim(),"-").trim();
			response2 = send_data_request(quebec_port, "list_events", "-", eventType.toUpperCase().trim(),"-").trim();
		}
		if(response1.contains("@"))
			server1 = Arrays.asList(response1.split("@"));
		if(response1.contains("@"))
        	server2 = Arrays.asList(response2.split("@"));
		allEventIDsWithCapacity.addAll(server1);
        allEventIDsWithCapacity.addAll(server2);
		return CommonOutput.listEventAvailabilityOutput(true, allEventIDsWithCapacity, null);
	}
	public String list_events(final String eventType) 
	{
		String response = "";

		if(EventMap.containsKey(eventType.toUpperCase().trim()))
		{	
			for (final ConcurrentHashMap.Entry<String, EventDetail> entry : EventMap.get(eventType).entrySet()) 
			{
				response += entry.getKey() + " " + entry.getValue().bookingCapacity+"@";
			}
		}
		if (response.endsWith("@"))
			response = response.substring(0, response.length() - 1);
		return response;
	}
	
	@Override
	public synchronized String bookEvent(final String customerID, final String eventID, final String eventType) throws RemoteException{
		String response="";
		final String city = eventID.substring(0,3).toUpperCase().trim();
		final String eventDetail = eventType.toUpperCase().trim()+ ";" + eventID.toUpperCase().trim();

		if(city.trim().equals(serverName))
		{
			response = book_accepted_event(customerID, eventID, eventType);
				
			if(response.indexOf("ERR_NO_CAPACITY")!=-1)
			{
				try 
				{
					serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID +" CustomerID:"+ customerID,"failed","There is no capacity for this event");
					clientLog(customerID, "Book an event", "There is no capacity for eventType:" + eventType+" eventID:"+eventID);

				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			else if(response.indexOf("ERR_NO_RECORD")!=-1)
			{
				try 
				{
					serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID+" CustomerID:"+ customerID,"failed","There is no such an event");
					clientLog(customerID, "Book an event", "There is no such an event --> eventType:" + eventType+" eventID:"+eventID);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			else
			{
				try 
				{
					final int capacity = EventMap.get(eventType.toUpperCase().trim()).get(eventID.toUpperCase().trim()).bookingCapacity;
					if(serverName.trim().equals(customerID.substring(0,3).toUpperCase().trim()))
					{
						if(ClientMap.containsKey(customerID.toUpperCase().trim()) && ClientMap.get(customerID.toUpperCase().trim()).containsKey(eventDetail))			
						{
							try 
							{
								serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID +" CustomerID:"+ customerID,"failed","This event has already been booked");
								clientLog(customerID, "Book an event", "This event has already been booked --> eventType:" + eventType+" eventID:"+eventID);

							} catch (final IOException e) {
								e.printStackTrace();
							}
							
							return "ERR_RECORD_EXISTS";
						}
						EventMap.get(eventType.toUpperCase().trim()).replace(eventID,new EventDetail(eventType.toUpperCase().trim(), eventID.toUpperCase().trim(), capacity - 1));
						add_book_customer(customerID, eventID, eventType);
					}
					else
						EventMap.get(eventType.toUpperCase().trim()).replace(eventID,new EventDetail(eventType.toUpperCase().trim(), eventID.toUpperCase().trim(), capacity - 1));
					
					serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID+" CustomerID:"+ customerID,"successfully completed","Booking request has been approved");
					clientLog(customerID, "Book an event", "Booking request has been approved --> eventType:" + eventType+" eventID:"+eventID);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		else
		{
			if(city.trim().equals("QUE"))
			{
				if(ClientMap.containsKey(customerID.toUpperCase().trim()) && ClientMap.get(customerID.toUpperCase().trim()).containsKey(eventDetail))			
				{
					try 
					{
						serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID +" CustomerID:"+ customerID,"failed","This event has already been booked");
						clientLog(customerID, "Book an event", "This event has already been booked --> eventType:" + eventType+" eventID:"+eventID);

					} catch (final IOException e) {
						e.printStackTrace();
					}
					
					return "ERR_RECORD_EXISTS";
				}
				if(ClientMap.containsKey(customerID.toUpperCase().trim()))
				{
					if(!week_limit_check(customerID.toUpperCase().trim(), eventID.substring(4)))
					{
						try 
						{
							serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID+" CustomerID:"+ customerID,"failed","This customer has already booked 3 times from other cities!");
							clientLog(customerID, "Book an event", "This customer has already booked 3 times from other cities --> eventType:" + eventType+" eventID:"+eventID);
						} catch (final IOException e) {
							e.printStackTrace();
						}
						
						return "This customer has already booked 3 times from other cities!";
					}
				}
				response = send_data_request(quebec_port, "bookEvent", eventID.toUpperCase().trim(), eventType.toUpperCase().trim(),customerID.toUpperCase().trim()).trim();
				if(response.indexOf("BOOKING_APPROVED")!=-1)
				{
					try 
					{
						serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID+" CustomerID:"+ customerID,"successfully completed","Booking request has been approved");
						clientLog(customerID, "Book an event", "Booking request has been approved --> eventType:" + eventType+" eventID:"+eventID);
					} catch (final IOException e) {
						e.printStackTrace();
					}
					add_book_customer(customerID, eventID, eventType);
				}
				else if(response.indexOf("ERR_NO_CAPACITY")!=-1)
				{
					try 
					{
						serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID +" CustomerID:"+ customerID,"failed","There is no capacity for this event");
						clientLog(customerID, "Book an event", "There is no capacity for eventType:" + eventType+" eventID:"+eventID);

					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
				else if(response.indexOf("ERR_NO_RECORD")!=-1)
				{
					try 
					{
						serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID+" CustomerID:"+ customerID,"failed","There is no such an event");
						clientLog(customerID, "Book an event", "There is no such an event --> eventType:" + eventType+" eventID:"+eventID);
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			}
			else if(city.trim().equals("MTL"))
			{
				if(ClientMap.containsKey(customerID.toUpperCase().trim()) && ClientMap.get(customerID.toUpperCase().trim()).containsKey(eventDetail))			
				{
					try 
					{
						serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID +" CustomerID:"+ customerID,"failed","This event has already been booked");
						clientLog(customerID, "Book an event", "This event has already been booked --> eventType:" + eventType+" eventID:"+eventID);
					} catch (final IOException e) {
						e.printStackTrace();
					}
					
					return "ERR_RECORD_EXISTS";
				}
				if(ClientMap.containsKey(customerID.toUpperCase().trim()))
				{
					if(!week_limit_check(customerID.toUpperCase().trim(), eventID.substring(4)))
					{
						try 
						{
							serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID+" CustomerID:"+ customerID,"failed","This customer has already booked 3 times from other cities!");
							clientLog(customerID, "Book an event", "This customer has already booked 3 times from other cities --> eventType:" + eventType+" eventID:"+eventID);
						} catch (final IOException e) {
							e.printStackTrace();
						}
						return "This customer has already booked 3 times from other cities!";
					}
				}
				response = send_data_request(montreal_port, "bookEvent", eventID.toUpperCase().trim(), eventType.toUpperCase().trim(),customerID.toUpperCase().trim()).trim();
				if(response.indexOf("BOOKING_APPROVED")!=-1)
				{
					try 
					{
						serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID+" CustomerID:"+ customerID,"successfully completed","Booking request has been approved");
						clientLog(customerID, "Book an event", "Booking request has been approved --> eventType:" + eventType+" eventID:"+eventID);
					} catch (final IOException e) {
						e.printStackTrace();
					}
					add_book_customer(customerID, eventID, eventType);
				}
				else if(response.indexOf("ERR_NO_CAPACITY")!=-1)
				{
					try 
					{
						serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID +" CustomerID:"+ customerID,"failed","There is no capacity for this event");
						clientLog(customerID, "Book an event", "There is no capacity for eventType:" + eventType+" eventID:"+eventID);
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
				else if(response.indexOf("ERR_NO_RECORD")!=-1)
				{
					try 
					{
						serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID+" CustomerID:"+ customerID,"failed","There is no such an event");
						clientLog(customerID, "Book an event", "There is no such an event --> eventType:" + eventType+" eventID:"+eventID);
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			}
			else if(city.trim().equals("SHE"))
			{
				if(ClientMap.containsKey(customerID.toUpperCase().trim()) && ClientMap.get(customerID.toUpperCase().trim()).containsKey(eventDetail))			
				{
					try 
					{
						serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID +" CustomerID:"+ customerID,"failed","This event has already been booked");
						clientLog(customerID, "Book an event", "This event has already been booked --> eventType:" + eventType+" eventID:"+eventID);
					} catch (final IOException e) {
						e.printStackTrace();
					}
					
					return "ERR_RECORD_EXISTS";
				}
				if(ClientMap.containsKey(customerID.toUpperCase().trim()))
				{
					if(!week_limit_check(customerID.toUpperCase().trim(), eventID.substring(4)))
					{
						try 
						{
							serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID+" CustomerID:"+ customerID,"failed","This customer has already booked 3 times from other cities!");
							clientLog(customerID, "Book an event", "This customer has already booked 3 times from other cities --> eventType:" + eventType+" eventID:"+eventID);
						} catch (final IOException e) {
							e.printStackTrace();
						}
						return "This customer has already booked 3 times from other cities!";
					}
				}
				response = send_data_request(sherbrooke_port, "bookEvent", eventID.toUpperCase().trim(), eventType.toUpperCase().trim(),customerID.toUpperCase().trim()).trim();
				if(response.indexOf("BOOKING_APPROVED")!=-1)
				{
					try 
					{
						serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID+" CustomerID:"+ customerID,"successfully completed","Booking request has been approved");
						clientLog(customerID, "Book an event", "Booking request has been approved --> eventType:" + eventType+" eventID:"+eventID);
					} catch (final IOException e) {
						e.printStackTrace();
					}
					add_book_customer(customerID, eventID, eventType);
				}
				else if(response.indexOf("ERR_NO_CAPACITY")!=-1)
				{
					try 
					{
						serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID +" CustomerID:"+ customerID,"failed","There is no capacity for this event");
						clientLog(customerID, "Book an event", "There is no capacity for eventType:" + eventType+" eventID:"+eventID);
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
				else if(response.indexOf("ERR_NO_RECORD")!=-1)
				{
					try 
					{
						serverLog("Book an event", " EventType:"+eventType+ " EventID:"+eventID+" CustomerID:"+ customerID,"failed","There is no such an event");
						clientLog(customerID, "Book an event", "There is no such an event --> eventType:" + eventType+" eventID:"+eventID);
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
		return response;
	}
	
	public boolean week_limit_check(final String customerID,final String eventDate)
	{
        int limit = 0;

		for(final Entry<String, ClientDetail> events : ClientMap.get(customerID).entrySet())
		{
			if(!events.getValue().eventID.substring(0, 3).equals(serverName) && same_week_check(events.getValue().eventID.substring(4), eventDate))
			{
				limit++;
			}
		}
		if (limit < 3)
			return true;
		else
			return false;
	}
	
	private boolean same_week_check(final String newEventDate, final String eventID) 
	{
        if (eventID.substring(2, 4).equals(newEventDate.substring(2, 4)) && eventID.substring(4, 6).equals(newEventDate.substring(4, 6))) 
        {
            final int w1 = Integer.parseInt(eventID.substring(0, 2)) / 7;
            final int w2 = Integer.parseInt(newEventDate.substring(0, 2)) / 7;
            
            if(w1 == w2)
            	return true;
            else
            	return false;
        } 
        else 
            return false;

	}
	public String book_accepted_event(final String customerID, final String eventID, final String eventType)
	{
		String response="";

		if(EventMap.containsKey(eventType.toUpperCase().trim()) && EventMap.get(eventType.toUpperCase().trim()).containsKey(eventID.toUpperCase().trim()))
		{
			final int capacity = EventMap.get(eventType.toUpperCase().trim()).get(eventID.toUpperCase().trim()).bookingCapacity;
			
			if( capacity == 0)
				return "ERR_NO_CAPACITY";
			else
			{
				return "BOOKING_APPROVED";
			}
		}
		else
		{
			response = "ERR_NO_RECORD!";
		}

		return response;
	}

	public String add_book_customer(final String customerID, final String eventID, final String eventType)
	{
		String response = "";
		final String eventDetail = eventType.toUpperCase().trim()+ ";" + eventID.toUpperCase().trim();

		if(ClientMap.containsKey(customerID.toUpperCase().trim()))			
		{	
			ClientMap.get(customerID.toUpperCase().trim()).put(eventDetail, new ClientDetail(customerID.toUpperCase().trim(), eventType.toUpperCase().trim(), eventID.toUpperCase().trim()));
			response = "BOOKED";
		}
		else
		{
			final ConcurrentHashMap <String, ClientDetail> subHashMap = new ConcurrentHashMap<>();
			subHashMap.put(eventDetail, new ClientDetail(customerID.toUpperCase().trim(), eventType.toUpperCase().trim(), eventID.toUpperCase().trim()));
			ClientMap.put(customerID.toUpperCase().trim(), subHashMap);
			response = "BOOKED";
		}
		return response;
	}

	@Override
	public String getBookingSchedule(final String customerID) throws RemoteException{
		String response = "";
		
		if(ClientMap.containsKey(customerID.toUpperCase().trim()))			
		{
			for (final ConcurrentHashMap.Entry<String, ClientDetail> entry : ClientMap.get(customerID.toUpperCase().trim()).entrySet()) 
			{
				final String [] data = entry.getKey().split(";");
				response += data[0] + " " + data[1]+"@";
			}
			return response;
		}
		else
			return "No record for this customer";
	}

	@Override
	public String cancelEvent(final String customerID, final String eventID, final String eventType) throws RemoteException{
		final String eventDetail = eventType.toUpperCase().trim()+ ";" + eventID.toUpperCase().trim();
		final String branch = eventID.substring(0,3).toUpperCase().trim();
		
		if(ClientMap.containsKey(customerID.toUpperCase().trim()) && ClientMap.get(customerID.toUpperCase().trim()).containsKey(eventDetail))		
		{
			ClientMap.get(customerID.toUpperCase().trim()).remove(eventDetail);

			if(branch.trim().equals(serverName))
			{
				final int currentCapacity = EventMap.get(eventType.toUpperCase().trim()).get(eventID.toUpperCase().trim()).bookingCapacity;
				EventMap.get(eventType.toUpperCase().trim()).replace(eventID,new EventDetail(eventType.toUpperCase().trim(), eventID.toUpperCase().trim(), currentCapacity + 1));
				try 
				{
					serverLog("Cancel an event", " EventType:"+eventType+ " EventID:"+eventID+" CustomerID:"+ customerID,"successfully completed","Event has been canceled");
					clientLog(customerID, "Cancel an event", "Event has been canceled --> eventType:" + eventType+" eventID:"+eventID);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			else if(branch.trim().equals("QUE"))
			{
				send_data_request(quebec_port, "cancel_client_event", eventID.toUpperCase().trim(), eventType.toUpperCase().trim(),"-").trim();
				try 
				{
					serverLog("Cancel an event", " EventType:"+eventType+ " EventID:"+eventID+" CustomerID:"+ customerID,"successfully completed","Event has been canceled");
					clientLog(customerID, "Cancel an event", "Event has been canceled --> eventType:" + eventType+" eventID:"+eventID);
				} catch (final IOException e) {
					e.printStackTrace();
				}
				
			}
			else if(branch.trim().equals("MTL"))
			{
				send_data_request(montreal_port, "cancel_client_event", eventID.toUpperCase().trim(), eventType.toUpperCase().trim(),"-").trim();
				try 
				{
					serverLog("Cancel an event", " EventType:"+eventType+ " EventID:"+eventID+" CustomerID:"+ customerID,"successfully completed","Event has been canceled");
					clientLog(customerID, "Cancel an event", "Event has been canceled --> eventType:" + eventType+" eventID:"+eventID);
				} catch (final IOException e) {
					e.printStackTrace();
				}

			}
			else if(branch.trim().equals("SHE"))
			{
				send_data_request(sherbrooke_port, "cancel_client_event", eventID.toUpperCase().trim(), eventType.toUpperCase().trim(),"-").trim();
				try 
				{
					serverLog("Cancel an event", " EventType:"+eventType+ " EventID:"+eventID+" CustomerID:"+ customerID,"successfully completed","Event has been canceled");
					clientLog(customerID, "Cancel an event", "Event has been canceled --> eventType:" + eventType+" eventID:"+eventID);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			return "Event for customer cancelled";
		}
		else
			return "No record event";
	}
	
	public String cancel_client_event(final String eventID, final String eventType)
	{
		if(EventMap.containsKey(eventType.toUpperCase().trim()) && EventMap.get(eventType.toUpperCase().trim()).containsKey(eventID.toUpperCase().trim()))
		{
			final int currentCapacity = EventMap.get(eventType.toUpperCase().trim()).get(eventID.toUpperCase().trim()).bookingCapacity;
			EventMap.get(eventType.toUpperCase().trim()).replace(eventID,new EventDetail(eventType.toUpperCase().trim(), eventID.toUpperCase().trim(), currentCapacity + 1));
		}	
		return "CANCELED";
		
	}

	private static String getDirectory(final String ID, final String type) {
        final String dir = System.getProperty("user.dir");
		String fileName = dir;
		if(type == "Server")
		{
			if (ID.equals("MTL")) {
				fileName = dir + "/src/Replica1/Logs/Server/Montreal_logs.txt";
			} else if (ID.equals("SHE")) {
				fileName = dir + "/src/Replica1/Logs/Server/Sherbrooke_logs.txt";
			} else if (ID.equals("QUE")) {
				fileName = dir + "/src/Replica1/Logs/Server/Quebec_logs.txt";
			}
		}
		else
		{
			fileName = dir + "/src/Replica1/Logs/Clients/"+ ID +"_logs.txt";
		}
        return fileName;
	}
	
	public void serverLog(final String acion, final String peram, final String requestResult, final String response) throws IOException {
		final String city = serverName;
		final Date date = new Date();
		final String strDateFormat = "yyyy-MM-dd hh:mm:ss a";
		final DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
		final String formattedDate= dateFormat.format(date);

		final FileWriter fileWriter = new FileWriter(getDirectory(city.trim().toUpperCase(), "Server"),true);
		final PrintWriter printWriter = new PrintWriter(fileWriter);
		printWriter.println("DATE: "+formattedDate+"| Request type: "+acion+" | Request parameters: "+ peram +" | Request result: "+requestResult+" | Server resonse: "+ response);

		printWriter.close();

	}
	
	public void clientLog(final String ID, final String acion, final String response) throws IOException {
		final FileWriter fileWriter = new FileWriter(getDirectory(ID, "Clients"),true);
		final PrintWriter printWriter = new PrintWriter(fileWriter);
		printWriter.println("Request type: "+acion+" | Resonse: "+ response);

		printWriter.close();

	}
	
	private static String send_data_request(final int serverPort,final String function,final String eventID, final String eventType, final String customerID) {
		DatagramSocket socket = null;
		String result ="";
		final String clientRequest = function+";"+eventID.toUpperCase().trim()+";"+eventType.toUpperCase().trim()+";" + customerID.toUpperCase().trim();
		try {
			socket = new DatagramSocket();
			final byte[] data = clientRequest.getBytes();
			final InetAddress host = InetAddress.getByName("localhost");
			final DatagramPacket request = new DatagramPacket(data, clientRequest.length(), host, serverPort);
			socket.send(request);

			final byte[] buffer = new byte[1000];
			final DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

			socket.receive(reply);
			result = new String(reply.getData());
		} catch (final SocketException e) {
			System.out.println("Socket exception: " + e.getMessage());
		} catch (final IOException e) {
			e.printStackTrace();
			System.out.println("IO Error: " + e.getMessage());
		} finally {
			if (socket != null)
				socket.close();
		}
		return result;

	}

	@Override
	public synchronized String swapEvent(final String customerID, final String newEventID, final String newEventType, final String oldEventID, final String oldEventType) throws RemoteException{
		final String eventDetail = oldEventType.toUpperCase().trim()+ ";" + oldEventID.toUpperCase().trim();
		String response = "";
		if(!week_limit_check(customerID.toUpperCase().trim(), newEventID.substring(4)))
		{
			if(!oldEventID.substring(0, 3).equals(serverName.trim()))
			{
				response = cancelEvent(customerID, oldEventID, oldEventType);
				if(response.trim().equals("Event for customer cancelled"))
				{
					response = bookEvent(customerID, newEventID, newEventType);
					return "Event for customer swapped";
				}
			}
			else
			{
				try 
				{
					serverLog("Swap an event", " oldEventType:"+oldEventType+ " oldEventID:"+oldEventID+"newEventType:"+ newEventType+" newEventID:"+newEventID+"CustomerID:"+ customerID,"failed","This customer has already booked 3 times from other cities!");
					clientLog(customerID, "Book an event", "This customer has already booked 3 times from other cities");
				} catch (final IOException e) {
					e.printStackTrace();
				}
				
				return "This customer has already booked 3 times from other cities!";
			}	
		}
		else if(ClientMap.containsKey(customerID.toUpperCase().trim()) && ClientMap.get(customerID.toUpperCase().trim()).containsKey(eventDetail))		
		{
			response = bookEvent(customerID, newEventID, newEventType);
			if(response.trim().equals("BOOKING_APPROVED"))
			{
				response = cancelEvent(customerID, oldEventID, oldEventType);
				return "Event for customer swapped";
			}
		}
		else
			response = "The new EventId or EventType does not exists!";
		return response;
	}

	@Override
	public String shutDown() throws RemoteException 
	{
		new Thread(new Runnable() {
			public void run() {
				try {
				   Thread.sleep(100);
				} catch (InterruptedException e) {
				   // ignored
				}
				System.exit(1);
			}
		});
		return "Shutting down";
	}
}
