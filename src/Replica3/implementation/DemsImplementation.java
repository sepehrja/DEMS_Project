package Replica3.implementation;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import Replica3.server.Montreal;
import Replica3.server.Quebec;
import Replica3.server.Sherbrook;
import Replica3.CommonOutput;

public class DemsImplementation  extends UnicastRemoteObject  implements Manager  {

	public DemsImplementation() throws RemoteException {
		super();
	}

	@Override
	public String addEvent(String eventID, String eventType,
			int bookingCapacity, String serv) throws RemoteException {

		if (serv.equalsIgnoreCase("MTL")) {
			Montreal mn = new Montreal();
			String var = mn.getHashMap(eventType);
			// mn.addHashMap(var, eventID, bookingCapacity);
			return (mn.addHashMap(var, eventID, bookingCapacity));
		} else if (serv.equalsIgnoreCase("QUE")) {
			Quebec mn = new Quebec();

			String var = mn.getHashMap(eventType);
			// mn.addHashMap(var, eventID, bookingCapacity);
			return (mn.addHashMap(var, eventID, bookingCapacity));
		} else if (serv.equalsIgnoreCase("SHE")) {
			Sherbrook mn = new Sherbrook();
			String var = mn.getHashMap(eventType);
			// mn.addHashMap(var, eventID, bookingCapacity);
			return (mn.addHashMap(var, eventID, bookingCapacity));
		}
		return CommonOutput.addEventOutput(false, null);

	}

	@Override
	public String removeEvent(String eventID, String eventType, String serv) throws RemoteException {

		if (serv.equalsIgnoreCase("MTL")) {
			Montreal mn = new Montreal();
			String var = mn.getHashMap(eventType);
			return mn.removeHashMap(var, eventID);
		} else if (serv.equalsIgnoreCase("QUE")) {
			Quebec mn = new Quebec();
			String var = mn.getHashMap(eventType);
			return mn.removeHashMap(var, eventID);
		} else if (serv.equalsIgnoreCase("SHE")) {
			Sherbrook mn = new Sherbrook();
			String var = mn.getHashMap(eventType);
			return mn.removeHashMap(var, eventID);
		}
		return CommonOutput.removeEventOutput(false, null);

	}

	@Override
	public String listEventAvailability(String eventType, String serv) throws RemoteException {

		String str = "";
		String temp1 = "";
		String temp2 = "";
		String temp3 = "";

		if (serv.equalsIgnoreCase("MTL")) {
			Montreal mn = new Montreal();
			String var = mn.getHashMap(eventType)+"display";
			
			temp1 = mn.display(var.substring(0, 1));

			temp2 = mn.UDPConnect(7001, var);

			temp3 = mn.UDPConnect(7002, var);
			
			str = temp1.trim() +","+ temp2.trim() +","+ temp3.trim();
			String str1[] = str.split(",");
			List<String> al = new ArrayList<String>();
			al = Arrays.asList(str1);
			/*String str1 = temp1+temp3;
			str=str1+temp2;*/
			//str=temp1.concat(temp2).concat(temp3);
			
			return  CommonOutput.listEventAvailabilityOutput(true,al , null);

		} else if (serv.equalsIgnoreCase("QUE")) {
			Quebec mn = new Quebec();
			String var = mn.getHashMap(eventType)+"display";
			temp1 = mn.display(var.substring(0, 1));

			temp2 = mn.UDPConnect(7000, var);

			temp3 = mn.UDPConnect(7002, var);

			str = temp1.trim() +","+ temp2.trim() +","+ temp3.trim();
			String str1[] = str.split(",");
			List<String> al = new ArrayList<String>();
			al = Arrays.asList(str1);
			
			/*String str1 = temp1+temp3;
			str=str1+temp2;*/
			//str=temp1.concat(temp2).concat(temp3);
			
			return  CommonOutput.listEventAvailabilityOutput(true,al , null);
		} else if (serv.equalsIgnoreCase("SHE")) {
			Sherbrook mn = new Sherbrook();
			String var = mn.getHashMap(eventType)+"display";
			temp1 = mn.display(var.substring(0, 1));

			temp2 = mn.UDPConnect(7001, var);

			temp3 = mn.UDPConnect(7000, var);

			str = temp1.trim() +","+ temp2.trim() +","+ temp3.trim();
			String str1[] = str.split(",");
			List<String> al = new ArrayList<String>();
			al = Arrays.asList(str1);
			
			/*String str1 = temp1+temp3;
			str=str1+temp2;*/
			//str=temp1.concat(temp2).concat(temp3);
			
			return  CommonOutput.listEventAvailabilityOutput(true,al , null);
		}

		else
		
		/*String str1 = temp1+temp3;
		str=str1+temp2;*/
		//str=temp1.concat(temp2).concat(temp3);
		
		return  CommonOutput.listEventAvailabilityOutput(false,null , null);
	}

	@Override
	public String bookEvent(String customerID, String eventID,
			String eventType, String serv) throws RemoteException {
		char[] ch = eventID.toCharArray();
		char[] ch2 = { ch[0], ch[1], ch[2] };
		String bookingServ = new String(ch2);

		if (serv.equalsIgnoreCase("MTL")) {
			Montreal mn = new Montreal();
			String var = mn.getHashMap(eventType)+"booked "+customerID+eventID;

			if (serv.equalsIgnoreCase(bookingServ)) {
				if (mn.checkAvailabilityOfEvent(var.substring(0, 1), eventID).equalsIgnoreCase(
						"Available ")) {//it checks both condition capacity and existence
					String r = mn.bookedEvent(var.substring(0, 1),eventID, customerID);

					return CommonOutput.bookEventOutput(true, null);
				} else if (mn.checkAvailabilityOfEvent(var.substring(0, 1), eventID).equalsIgnoreCase(
						"No Capacity ")) {//it checks both condition capacity and existence
					 return CommonOutput.bookEventOutput(false, CommonOutput.bookEvent_fail_no_capacity);
				} else{
					 return CommonOutput.bookEventOutput(false, CommonOutput.bookEvent_fail_no_such_event);
				}
			}
			else if(bookingServ.equalsIgnoreCase("QUE")){
				String count=mn.UDPConnect(7001, ("checkCount"+customerID+ eventID));
				String count1=mn.UDPConnect(7002, ("checkCount"+customerID+ eventID));
				int counter=Integer.parseInt(count.substring(0, 1))+Integer.parseInt(count1.substring(0, 1));
				if(counter==3){
					//return "Cannot book.You already have 3 booking in the servers";
					return CommonOutput.bookEventOutput(false, CommonOutput.bookEvent_fail_weekly_limit);
				}
				/*if (mn.checkAvailabilityOfEvent(var.substring(0, 1), eventID).equalsIgnoreCase(
						"Available ")) {*/
					String temp2;
					temp2 = mn.UDPConnect(7001, var);
					return temp2.trim();
				/*} else {
					return ("No such event is available");
				}*/
				
				
			}else if(bookingServ.equalsIgnoreCase("SHE")){
				String count=mn.UDPConnect(7001, ("checkCount"+customerID+ eventID));
				String count1=mn.UDPConnect(7002, ("checkCount"+customerID+ eventID));
				int counter=Integer.parseInt(count.substring(0, 1))+Integer.parseInt(count1.substring(0, 1));
				if(counter==3){
					return CommonOutput.bookEventOutput(false, CommonOutput.bookEvent_fail_weekly_limit);
				}
				
				
			/*	if (mn.checkAvailabilityOfEvent(var.substring(0, 1), eventID).equalsIgnoreCase(
						"Available ")) {*/
					String temp3;
					temp3 = mn.UDPConnect(7002, var);
					return temp3.trim();
				/*} else {
					return ("No such event is available");
				}*/
			}
			
			
		} else if (serv.equalsIgnoreCase("QUE")) {
			Quebec mn = new Quebec();

			String var = mn.getHashMap(eventType)+"booked "+customerID+eventID;
			if (serv.equalsIgnoreCase(bookingServ)) {
				if (mn.checkAvailabilityOfEvent(var.substring(0, 1), eventID).equalsIgnoreCase(
						"Available ")) {
					String r = mn.bookedEvent(var.substring(0, 1),eventID, customerID);

					return CommonOutput.bookEventOutput(true, null);
				} else if (mn.checkAvailabilityOfEvent(var.substring(0, 1), eventID).equalsIgnoreCase(
						"No Capacity ")) {//it checks both condition capacity and existence
					 return CommonOutput.bookEventOutput(false, CommonOutput.bookEvent_fail_no_capacity);
				} else{
					 return CommonOutput.bookEventOutput(false, CommonOutput.bookEvent_fail_no_such_event);
				}
			}
			else if(bookingServ.equalsIgnoreCase("MTL")){
				String count=(mn.UDPConnect(7000, ("checkCount"+customerID+ eventID)));
				String count1=(mn.UDPConnect(7002, ("checkCount"+customerID+ eventID)));
				int counter=Integer.parseInt(count.substring(0, 1))+Integer.parseInt(count1.substring(0, 1));
				if(counter==3){
					return CommonOutput.bookEventOutput(false, CommonOutput.bookEvent_fail_weekly_limit);
				}
				
		/*		if (mn.checkAvailabilityOfEvent(var.substring(0, 1), eventID).equalsIgnoreCase(
						"Available ")) {*/
					String temp2;
					
					temp2 = mn.UDPConnect(7000, var);
					return temp2.trim();
				/*} else {
					return ("No such event is available");
				}*/
			}else if(bookingServ.equalsIgnoreCase("SHE")){
				String count=mn.UDPConnect(7000, ("checkCount"+customerID+ eventID));
				String count1=mn.UDPConnect(7002, ("checkCount"+customerID+ eventID));
				int counter=Integer.parseInt(count.substring(0, 1))+Integer.parseInt(count1.substring(0, 1));
				if(counter==3){
					return CommonOutput.bookEventOutput(false, CommonOutput.bookEvent_fail_weekly_limit);
				}
				/*if (mn.checkAvailabilityOfEvent(var.substring(0, 1), eventID).equalsIgnoreCase(
						"Available ")) {*/
					String temp3;
					temp3 = mn.UDPConnect(7002, var);
					return temp3.trim();
				/*} else {
					return ("No such event is available");
				}*/
				
			}
			
		} else if (serv.equalsIgnoreCase("SHE")) {
			Sherbrook mn = new Sherbrook();

			String var = mn.getHashMap(eventType)+"booked "+customerID+eventID;
			if (serv.equalsIgnoreCase(bookingServ)) {
				if (mn.checkAvailabilityOfEvent(var.substring(0, 1), eventID).equalsIgnoreCase(
						"Available ")) {
					String r = mn.bookedEvent(var.substring(0, 1),eventID, customerID);

					return CommonOutput.bookEventOutput(true, null);
				}else if (mn.checkAvailabilityOfEvent(var.substring(0, 1), eventID).equalsIgnoreCase(
						"No Capacity ")) {//it checks both condition capacity and existence
					 return CommonOutput.bookEventOutput(false, CommonOutput.bookEvent_fail_no_capacity);
				} else{
					 return CommonOutput.bookEventOutput(false, CommonOutput.bookEvent_fail_no_such_event);
				}
			}
			else if(bookingServ.equalsIgnoreCase("QUE")){
				String count=mn.UDPConnect(7000, ("checkCount"+customerID+ eventID));
				String count1=mn.UDPConnect(7001, ("checkCount"+customerID+ eventID));
				int counter=Integer.parseInt(count.substring(0, 1))+Integer.parseInt(count1.substring(0, 1));
				if(counter==3){
					return CommonOutput.bookEventOutput(false, CommonOutput.bookEvent_fail_weekly_limit);
				}
				/*
				if (mn.checkAvailabilityOfEvent(var.substring(0, 1), eventID).equalsIgnoreCase(
						"Available ")) {*/
					String temp2;
					temp2 = mn.UDPConnect(7001, var);
					return temp2.trim();
				/*} else {
					return ("No such event is available");
				}*/
				
			}else if(bookingServ.equalsIgnoreCase("MTL")){
				String count=mn.UDPConnect(7000, ("checkCount"+customerID+ eventID));
				String count1=mn.UDPConnect(7001, ("checkCount"+customerID+ eventID));
				int counter=Integer.parseInt(count.substring(0, 1))+Integer.parseInt(count1.substring(0, 1));
				if(counter==3){
					return CommonOutput.bookEventOutput(false, CommonOutput.bookEvent_fail_weekly_limit);
				}
				/*if (mn.checkAvailabilityOfEvent(var.substring(0, 1), eventID).equalsIgnoreCase(
						"Available ")) {*/
					String temp3;
					temp3 = mn.UDPConnect(7000, var);
					return temp3.trim();
			/*	} else {
					return ("No such event is available");
				}*/
				
			}
			
		}
		return CommonOutput.bookEventOutput(false, null);

	}

	@Override
	public String getBookingSchedule(String customerID, String serv) throws RemoteException {

		if (serv.equalsIgnoreCase("MTL")) {
			Montreal mn = new Montreal();
			String var ="Userdat"+customerID;

			String temp1 = mn.getUserData(customerID);
			String temp2 = mn.UDPConnect(7001, var);

			String temp3 = mn.UDPConnect(7002, var);

			String str = temp1.trim() +","+ temp2.trim() +","+ temp3.trim();
			
			String str2[] = str.split(",");
	    	List<String> al = new ArrayList<String>();
	    	al = Arrays.asList(str2);
	      HashMap<String, List<String>> a = new HashMap<String, List<String>>();
	    //  Map<String, List<String>>=
	    	for(String s: al){
	    		List<String> users = new ArrayList<String>();
	    	   String[] str3=s.split(" ");
	    	   users.add(str2[1]);
	    	   a.put(str2[0], users);
	    	
	}

	    	return CommonOutput.getBookingScheduleOutput(true, a, null);

		} else if (serv.equalsIgnoreCase("QUE")) {
			Quebec mn = new Quebec();
			String var = "Userdat"+customerID;

			String temp1 = mn.getUserData(customerID);
			String temp2 = mn.UDPConnect(7000, var);

			String temp3 = mn.UDPConnect(7002, var);

			String str = temp1.trim() +","+ temp2.trim() +","+ temp3.trim();

			String str2[] = str.split(",");
	    	List<String> al = new ArrayList<String>();
	    	al = Arrays.asList(str2);
	      HashMap<String, List<String>> a = new HashMap<String, List<String>>();
	    //  Map<String, List<String>>=
	    	for(String s: al){
	    		List<String> users = new ArrayList<String>();
	    	   String[] str3=s.split(" ");
	    	   users.add(str2[1]);
	    	   a.put(str2[0], users);
	    	
	}

	    	return CommonOutput.getBookingScheduleOutput(true, a, null);
		} else if (serv.equalsIgnoreCase("SHE")) {
			Sherbrook mn = new Sherbrook();
			String var = "Userdat"+customerID;

			String temp1 = mn.getUserData(customerID);
			String temp2 = mn.UDPConnect(7001, var);

			String temp3 = mn.UDPConnect(7000, var);

			String str = temp1.trim() +","+ temp2.trim() +","+ temp3.trim();

			String str2[] = str.split(",");
	    	List<String> al = new ArrayList<String>();
	    	al = Arrays.asList(str2);
	      HashMap<String, List<String>> a = new HashMap<String, List<String>>();
	    //  Map<String, List<String>>=
	    	for(String s: al){
	    		List<String> users = new ArrayList<String>();
	    	   String[] str3=s.split(" ");
	    	   users.add(str2[1]);
	    	   a.put(str2[0], users);
	    	
	}

	    	return CommonOutput.getBookingScheduleOutput(true, a, null);
		}

		else
			return CommonOutput.getBookingScheduleOutput(false, null, null);
	}

	@Override
	public String cancelEvent(String customerID, String eventID,
			String eventType, String serv) throws RemoteException {

		char[] ch = eventID.toCharArray();
		char[] ch2 = { ch[0], ch[1], ch[2] };
		String bookingServ = new String(ch2);

		if (serv.equalsIgnoreCase("MTL")) {
			Montreal mn = new Montreal();
			String var = mn.getHashMap(eventType)+"cancel "+customerID+eventID;

			if (serv.equalsIgnoreCase(bookingServ)) {
				
				if (mn.checkAvailabilityOfEvent(var.substring(0, 1), eventID).equalsIgnoreCase(
						"available ")) {
					if (mn.checkUserBooking(eventID, customerID)) {
						String c = mn.canceledEvent(var.substring(0, 1),eventID, customerID);

						 return CommonOutput.cancelEventOutput(true, null);
					} else
						return CommonOutput.cancelEventOutput(false, CommonOutput.cancelEvent_fail_not_registered_in_event);
				} else {
					return CommonOutput.cancelEventOutput(false, CommonOutput.cancelEvent_fail_no_such_event);
				}
			}
			else if(bookingServ.equalsIgnoreCase("QUE")){
				
				String temp2;
				temp2 = mn.UDPConnect(7001, var);
				return temp2.trim();
				
			}else if(bookingServ.equalsIgnoreCase("SHE")){
				
				String temp3;
				temp3 = mn.UDPConnect(7002, var);
				return temp3.trim();
			}
			
			
		} else if (serv.equalsIgnoreCase("QUE")) {
			Quebec mn = new Quebec();

			String var = mn.getHashMap(eventType)+"cancel "+customerID+eventID;
			if (serv.equalsIgnoreCase(bookingServ)) {
				if (mn.checkAvailabilityOfEvent(var.substring(0, 1), eventID).equalsIgnoreCase(
						"available ")) {
					if (mn.checkUserBooking(eventID, customerID)) {
						String c = mn.canceledEvent(var.substring(0, 1),eventID, customerID);

						 return CommonOutput.cancelEventOutput(true, null);
					} else
						return CommonOutput.cancelEventOutput(false, CommonOutput.cancelEvent_fail_not_registered_in_event);
				} else {
					return CommonOutput.cancelEventOutput(false, CommonOutput.cancelEvent_fail_no_such_event);
				}
			}
			else if(bookingServ.equalsIgnoreCase("MTL")){
				
				String temp2;
				temp2 = mn.UDPConnect(7000, var);
				return temp2.trim();
				
			}else if(bookingServ.equalsIgnoreCase("SHE")){
				
				String temp3;
				temp3 = mn.UDPConnect(7002, var);
				return temp3.trim();
			}
			
		} else if (serv.equalsIgnoreCase("SHE")) {
			Sherbrook mn = new Sherbrook();

			String var = mn.getHashMap(eventType)+"cancel "+customerID+eventID;
			if (serv.equalsIgnoreCase(bookingServ)) {
				if (mn.checkAvailabilityOfEvent(var.substring(0, 1), eventID).equalsIgnoreCase(
						"available ")) {
					if (mn.checkUserBooking(eventID, customerID)) {
						String c = mn.canceledEvent(var.substring(0, 1),eventID, customerID);

						 return CommonOutput.cancelEventOutput(true, null);
					} else
						return CommonOutput.cancelEventOutput(false, CommonOutput.cancelEvent_fail_not_registered_in_event);
				} else {
					return CommonOutput.cancelEventOutput(false, CommonOutput.cancelEvent_fail_no_such_event);
				}
			}
			else if(bookingServ.equalsIgnoreCase("QUE")){
				
				String temp2;
				temp2 = mn.UDPConnect(7001, var);
				return temp2.trim();
				
			}else if(bookingServ.equalsIgnoreCase("MTL")){
				
				String temp3;
				temp3 = mn.UDPConnect(7000, var);
				return temp3.trim();
			}
			
		}
		return CommonOutput.cancelEventOutput(false, null);
		

	}
	public String swapEvent(String customerID, String newEventID,
			String newEventType, String oldEventID, String oldEventType,
			String serv) throws RemoteException {
		DemsImplementation d1 = new DemsImplementation();
		DemsImplementation d2 = new DemsImplementation();
		StringBuffer str = new StringBuffer();

		char[] ch = newEventID.toCharArray();
		char[] ch2 = { ch[0], ch[1], ch[2] };
		String newServ = new String(ch2);

		char[] ch1 = oldEventID.toCharArray();
		char[] ch21 = { ch1[0], ch1[1], ch1[2] };
		String oldServ = new String(ch21);

		String Lowest_EventId = new String();

		int month1 = Integer.parseInt(newEventID.substring(6, 8));
		int month2 = Integer.parseInt(oldEventID.substring(6, 8));
		int month = month1 - month2;
		int date1 = Integer.parseInt(newEventID.substring(4, 6));
		int date2 = Integer.parseInt(oldEventID.substring(4, 6));
		int date = 0;
		if (month == 0) {
			date = date1 - date2;
			if (date < 0) {
				date = date * -1;
				Lowest_EventId = newEventID;
			} else {
				Lowest_EventId = oldEventID;
			}
		} else if (month == 1) {
			date = (date1 + 30) - date2;
			Lowest_EventId = oldEventID;
		} else if (month == -1) {
			date = (date2 + 30) - date1;
			Lowest_EventId = newEventID;
		}

		int numberOfBooking = 0;
		int count = 0;
		if (serv.equalsIgnoreCase("MTL")) {
			Montreal mn = new Montreal();
			if (oldServ.equalsIgnoreCase("MTL")) {

				String var = mn.getHashMap(oldEventType);

				// String variable="isBooked"+customerID;
				String bookingexistence = mn.isbooked(customerID);
				if (bookingexistence.equalsIgnoreCase("False ")) {
					return CommonOutput.swapEventOutput(false, CommonOutput.swapEvent_fail_not_registered_in_event);
				}

				if (mn.checkAvailabilityOfEvent(var, oldEventID)
						.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(oldEventID)) {
						String count1 = mn.UDPConnect(7001, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7002, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));

					}
				}

			} else if (oldServ.equalsIgnoreCase("QUE")) {
				String var = oldEventType.substring(0, 1) + "getExistence"
						+ oldEventID;
				String ans = (mn.UDPConnect(7001, var)).substring(0, 10);

				String variable = "isBooked" + customerID;
				String bookingexistence = (mn.UDPConnect(7001, variable))
						.substring(0, 6);
				if (bookingexistence.equalsIgnoreCase("False ")) {
					return CommonOutput.swapEventOutput(false, CommonOutput.swapEvent_fail_not_registered_in_event);
				}
				if (ans.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(oldEventID)) {
						String count1 = mn.UDPConnect(7001, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7002, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));
					}
				}

			} else if (oldServ.equalsIgnoreCase("SHE")) {
				String var = oldEventType.substring(0, 1) + "getExistence"
						+ oldEventID;
				String ans = (mn.UDPConnect(7002, var)).substring(0, 10);

				String variable = "isBooked" + customerID;
				String bookingexistence = (mn.UDPConnect(7002, variable))
						.substring(0, 6);
				if (bookingexistence.equalsIgnoreCase("False ")) {
					return CommonOutput.swapEventOutput(false, CommonOutput.swapEvent_fail_not_registered_in_event);
				}
				if (ans.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(oldEventID)) {
						String count1 = mn.UDPConnect(7001, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7002, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));
					}
				}

			}

			if (newServ.equalsIgnoreCase("MTL")) {

				String var = mn.getHashMap(newEventType);

				if (mn.checkAvailabilityOfEvent(var, newEventID)
						.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(newEventID)) {
						String count1 = mn.UDPConnect(7001, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7002, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));

					}
				}

			} else if (newServ.equalsIgnoreCase("QUE")) {
				String var = newEventType.substring(0, 1) + "getExistence"
						+ newEventID;
				String ans = (mn.UDPConnect(7001, var)).substring(0, 10);
				if (ans.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(newEventID)) {
						String count1 = mn.UDPConnect(7001, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7002, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));
					}
				}

			} else if (newServ.equalsIgnoreCase("SHE")) {
				String var = newEventType.substring(0, 1) + "getExistence"
						+ newEventID;
				String ans = (mn.UDPConnect(7002, var)).substring(0, 10);
				if (ans.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(newEventID)) {
						String count1 = mn.UDPConnect(7001, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7002, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));
					}
				}

			}
		} else if (serv.equalsIgnoreCase("QUE")) {
			Quebec mn = new Quebec();
			if (oldServ.equalsIgnoreCase("QUE")) {

				String var = mn.getHashMap(oldEventType);

				// String variable="isBooked"+customerID;
				String bookingexistence = mn.isbooked(customerID);
				if (bookingexistence.equalsIgnoreCase("False ")) {
					return CommonOutput.swapEventOutput(false, CommonOutput.swapEvent_fail_not_registered_in_event);
				}

				if (mn.checkAvailabilityOfEvent(var, oldEventID)
						.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(oldEventID)) {
						String count1 = mn.UDPConnect(7000, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7002, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));

					}
				}

			} else if (oldServ.equalsIgnoreCase("MTL")) {
				String var = oldEventType.substring(0, 1) + "getExistence"
						+ oldEventID;
				String ans = (mn.UDPConnect(7000, var)).substring(0, 10);

				String variable = "isBooked" + customerID;
				String bookingexistence = (mn.UDPConnect(7000, variable))
						.substring(0, 6);
				if (bookingexistence.equalsIgnoreCase("False ")) {
					return CommonOutput.swapEventOutput(false, CommonOutput.swapEvent_fail_not_registered_in_event);
				}
				if (ans.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(oldEventID)) {
						String count1 = mn.UDPConnect(7000, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7002, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));
					}
				}

			} else if (oldServ.equalsIgnoreCase("SHE")) {
				String var = oldEventType.substring(0, 1) + "getExistence"
						+ oldEventID;
				String ans = (mn.UDPConnect(7002, var)).substring(0, 10);

				String variable = "isBooked" + customerID;
				String bookingexistence = (mn.UDPConnect(7002, variable))
						.substring(0, 6);
				if (bookingexistence.equalsIgnoreCase("False ")) {
					return CommonOutput.swapEventOutput(false, CommonOutput.swapEvent_fail_not_registered_in_event);
				}
				if (ans.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(oldEventID)) {
						String count1 = mn.UDPConnect(7000, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7002, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));
					}
				}

			}

			if (newServ.equalsIgnoreCase("QUE")) {

				String var = mn.getHashMap(newEventType);

				if (mn.checkAvailabilityOfEvent(var, newEventID)
						.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(newEventID)) {
						String count1 = mn.UDPConnect(7000, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7002, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));

					}
				}

			} else if (newServ.equalsIgnoreCase("MTL")) {
				String var = newEventType.substring(0, 1) + "getExistence"
						+ newEventID;
				String ans = (mn.UDPConnect(7000, var)).substring(0, 10);
				if (ans.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(newEventID)) {
						String count1 = mn.UDPConnect(7000, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7002, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));
					}
				}

			} else if (newServ.equalsIgnoreCase("SHE")) {
				String var = newEventType.substring(0, 1) + "getExistence"
						+ newEventID;
				String ans = (mn.UDPConnect(7002, var)).substring(0, 10);
				if (ans.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(newEventID)) {
						String count1 = mn.UDPConnect(7000, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7002, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));
					}
				}

			}
		} else if (serv.equalsIgnoreCase("SHE")) {
			Sherbrook mn = new Sherbrook();
			if (oldServ.equalsIgnoreCase("SHE")) {

				String var = mn.getHashMap(oldEventType);

				// String variable="isBooked"+customerID;
				String bookingexistence = mn.isbooked(customerID);
				if (bookingexistence.equalsIgnoreCase("False ")) {
					return CommonOutput.swapEventOutput(false, CommonOutput.swapEvent_fail_not_registered_in_event);
				}

				if (mn.checkAvailabilityOfEvent(var, oldEventID)
						.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(oldEventID)) {
						String count1 = mn.UDPConnect(7000, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7001, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));

					}
				}

			} else if (oldServ.equalsIgnoreCase("MTL")) {
				String var = oldEventType.substring(0, 1) + "getExistence"
						+ oldEventID;
				String ans = (mn.UDPConnect(7000, var)).substring(0, 10);

				String variable = "isBooked" + customerID;
				String bookingexistence = (mn.UDPConnect(7000, variable))
						.substring(0, 6);
				if (bookingexistence.equalsIgnoreCase("False ")) {
					return CommonOutput.swapEventOutput(false, CommonOutput.swapEvent_fail_not_registered_in_event);
				}
				if (ans.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(oldEventID)) {
						String count1 = mn.UDPConnect(7000, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7001, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));
					}
				}

			} else if (oldServ.equalsIgnoreCase("QUE")) {
				String var = oldEventType.substring(0, 1) + "getExistence"
						+ oldEventID;
				String ans = (mn.UDPConnect(7001, var)).substring(0, 10);

				String variable = "isBooked" + customerID;
				String bookingexistence = (mn.UDPConnect(7001, variable))
						.substring(0, 6);
				if (bookingexistence.equalsIgnoreCase("False ")) {
					return CommonOutput.swapEventOutput(false, CommonOutput.swapEvent_fail_not_registered_in_event);
				}
				if (ans.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(oldEventID)) {
						String count1 = mn.UDPConnect(7000, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7001, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));
					}
				}

			}

			if (newServ.equalsIgnoreCase("SHE")) {

				String var = mn.getHashMap(newEventType);

				if (mn.checkAvailabilityOfEvent(var, newEventID)
						.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(newEventID)) {
						String count1 = mn.UDPConnect(7000, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7001, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));

					}
				}

			} else if (newServ.equalsIgnoreCase("MTL")) {
				String var = newEventType.substring(0, 1) + "getExistence"
						+ newEventID;
				String ans = (mn.UDPConnect(7000, var)).substring(0, 10);
				if (ans.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(newEventID)) {
						String count1 = mn.UDPConnect(7000, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7001, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));
					}
				}

			} else if (newServ.equalsIgnoreCase("QUE")) {
				String var = newEventType.substring(0, 1) + "getExistence"
						+ newEventID;
				String ans = (mn.UDPConnect(7001, var)).substring(0, 10);
				if (ans.equalsIgnoreCase("Available ")) {
					count++;
					if (Lowest_EventId.equals(newEventID)) {
						String count1 = mn.UDPConnect(7000, ("checkCount"
								+ customerID + Lowest_EventId));
						String count2 = mn.UDPConnect(7001, ("checkCount"
								+ customerID + Lowest_EventId));
						numberOfBooking = Integer.parseInt(count1.substring(0,
								1)) + Integer.parseInt(count2.substring(0, 1));
					}
				}

			}
		}

		if (count < 2) {
			return CommonOutput.swapEventOutput(false, CommonOutput.swapEvent_fail_no_such_event);
			//return ("Event Id doesn't exist or no capacity for the event");
		}

		if (date <= 3
				&& date >= 0
				&& !(oldServ.equalsIgnoreCase(newServ)
						&& newServ.equalsIgnoreCase(serv) && oldServ
							.equalsIgnoreCase(serv))) {
			if (numberOfBooking == 3) {
				String str2 = d1.cancelEvent(customerID, oldEventID,
						oldEventType, serv);
				str.append(str2.trim());

				if (str2.contains("SUCCESS")) {
					String str1 = d1.bookEvent(customerID, newEventID,
							newEventType, serv);
					str.append(str1);
					if (!(str1.contains("SUCCESS"))) {
						String str3 = d1.bookEvent(customerID, oldEventID,
								oldEventType, serv);
						str.append(". Failed to swap event because booking was not availablle");
					}
				}
			} else {
				String str2 = d1.bookEvent(customerID, newEventID,
						newEventType, serv);
				str.append(str2.trim());

				if (str2.contains("SUCCESS")) {
					String str1 = d1.cancelEvent(customerID, oldEventID,
							oldEventType, serv);
					str.append(str1);
					if (!(str1.contains("SUCCESS"))) {
						String str3 = d1.cancelEvent(customerID, newEventID,
								newEventType, serv);
						str.append(". Failed to swap event because booking was not availablle");
					}
				}
			}
		} else {
			String str2 = d1.bookEvent(customerID, newEventID, newEventType,
					serv);
			str.append(str2.trim());

			if (str2.contains("SUCCESS")) {
				String str1 = d1.cancelEvent(customerID, oldEventID,
						oldEventType, serv);
				str.append(str1);
				if (!(str1.contains("SUCCESS"))) {
					String str3 = d1.cancelEvent(customerID, newEventID,
							newEventType, serv);
					str.append(". Failed to swap event because booking was not availablle");
				}
			}
		}

		/*
		 * ExecutorService executor = Executors.newFixedThreadPool(2);
		 * executor.execute(runnableTask); executor.execute(runnableTask2);
		 * 
		 * executor.shutdownNow();
		 */
		if((str.toString()).contains("Failed to swap")){
			return CommonOutput.swapEventOutput(false, null);
		}
		else
		return CommonOutput.swapEventOutput(true, null);
	}

	
	public String shutDown() throws RemoteException 
	{
		System.exit(0); 
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
