package org.tyszecki.rozkladpkp;

public class TimetableItem {
	
	public class DateItem extends TimetableItem {
		String date;
	}
	
	public class TrainItem extends TimetableItem {
		public String time,number,station,delay,type;
		public String date,message="";
	}

}