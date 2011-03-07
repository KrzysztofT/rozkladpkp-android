package org.tyszecki.rozkladpkp;

public class BoardItem {
	
	public class DateItem extends BoardItem {
		String date;
	}
	
	public class TrainItem extends BoardItem {
		public String time,number,station,delay,type;
		public String date,message="";
	}

}