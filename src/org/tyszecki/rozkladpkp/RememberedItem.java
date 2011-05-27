package org.tyszecki.rozkladpkp;

public class RememberedItem {
	
	public enum TimetableType {Departure, Arrival};
	
	int id;
	
	public static class HeaderItem extends RememberedItem {
		String text;
	}
	
	public static class RouteItem extends RememberedItem {
		int SIDFrom,SIDTo;
		String fromName, toName;
	}
	
	public static class TimetableItem extends RememberedItem {
		TimetableType type;
		int SID;
		String name;
	}	
}
