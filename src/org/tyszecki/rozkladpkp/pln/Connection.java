package org.tyszecki.rozkladpkp.pln;

import android.text.format.Time;

public class Connection {
	
	public final int day;
	public final UnboundConnection connection;
	
	public Connection(UnboundConnection unboundConnection, int dayNumber) 
	{
		day = dayNumber;
		connection = unboundConnection;
	}
	
	public Time getDate(boolean withTime)
	{
		Time ret = new Time(connection.pln.sdate);
		ret.monthDay += day;
		ret.allDay = !withTime;
		
		if(withTime)
		{
			int tv = connection.getTrain(0).deptime.intValue();
			
			ret.monthDay += tv / 2400;
			tv %= 2400;
			ret.hour = tv / 100;
			tv %= 100;
			ret.minute += tv;
		}
		
		ret.normalize(false);
		return ret;
	}
}
