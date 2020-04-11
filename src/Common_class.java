

public class Common_class {

	public String addEventGeneralization(String output) {
		String var = new String();
		if ((output.substring(0, 19)).equalsIgnoreCase("Added Successfully ")
				) {
			var = "successfully"+output;
		}
		//If new event is added
		else if ((output.substring(0, 18)).equalsIgnoreCase("Value updated for ")
				) {
			var = "successfully"+output;
		}
		else
			var="unsuccessful"+output;
		//If new event already exists it returns updated
		return var;

	}

	public String removeEventGeneralization(String output) {
		String var = new String();
		if ((output.substring(0, 8)).equalsIgnoreCase("Removed ")
				) {
			var = "successfully"+output;
			//if this event was removed by user
		} else
			var="unsuccessful"+output;
		//if this event was not removed by user
		return var;
	}



	public String bookEventGeneralization(String output) {
		String var = new String();
		if ((output.substring(0, 13)).equalsIgnoreCase("booked event ")
				) {
			var = "successfully"+output;
			//if this event was booked by user
		} else if ((output.substring(0, 13)).equalsIgnoreCase("No such event")
				) {
			var = "unsuccessful"+output;
			//if this event doesn't exist
		} else if ((output.substring(0, 13)).equalsIgnoreCase("Cannot book")
				) {
			var = "unsuccessful"+output;
			//if user has 3 bookings
		}
			
		return var;
	}

	public String cancelEventGeneralization(String output) {
		String var = new String();
		if ((output.substring(0, 16)).equalsIgnoreCase("cancelled event ")
				) {
			var = "successfully"+output;
			//if this event was cancelled for user
		} else if((output.substring(0, 22)).equalsIgnoreCase("EventId not registered")
				) {
			var = "unsuccessful"+output;
			//if this event was not booked for user
		} else if((output.substring(0, 15)).equalsIgnoreCase("No such eventid")
				) {
			var = "unsuccessful"+output;
			//if this event doesn't exist
		} 
			
		return var;
	}


	public String swapEventGeneralization(String output) {
		String var = new String();
		if ((output.substring(0, 23)).equalsIgnoreCase("Event Id not registered")
				) {
			var = "unsuccessful"+output;;
			//if this previous event was not booked by user
		} else if((output.substring(0, 22)).equalsIgnoreCase("Event Id doesn't exist")
				) {
			var = "unsuccessful"+output;
			//if new event has no capacity or doesn't exist
		} else if(output.length()>=83 && output.contains("Failed to swap event")
				) {
			var = "unsuccessful"+output;
			//if it fails because of either booking or cancellation operation failure
		}else 
			var = "successfully"+output;
		return var;
	}
}
