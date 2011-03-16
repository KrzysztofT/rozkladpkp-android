package org.tyszecki.rozkladpkp;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.Html;
import android.text.format.Time;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TimePicker;

public class TimeButton extends Button {

	private String txt = "";
	Integer minute,hour;
	Time time;
	
	public TimeButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.clock), null, null, null);
		time	= new Time();
	}
	
	public Dialog timeDialog() {
	    return new TimePickerDialog(getContext(),mTimeSetListener, hour, minute, true);
	}
	
	private TimePickerDialog.OnTimeSetListener mTimeSetListener =
	    new TimePickerDialog.OnTimeSetListener() {
	        public void onTimeSet(TimePicker view, int hourOfDay, int sminute) {
	            hour = hourOfDay;
	            minute = sminute;
	            updateTime();
	        }
	    };
	
	private void updateTime()
	{
		String min	= (minute < 10) ? "0" + minute.toString() : minute.toString();
		setText(Html.fromHtml(txt+"<b>"+hour.toString()+":"+min+"</b>"));
	}
	
	public void setToNow()
	{
		time.setToNow();
		minute 	= time.minute;
		hour	= time.hour;
		
		updateTime();
	}

	public void setTime(int hour, int minute)
	{
		this.minute = minute;
		this.hour	= hour;
		
		updateTime();
	}
	
	public void setTime(String time)
	{
		String[] t = time.split(":"); 
		if(t.length >= 2)
			setTime(Integer.parseInt(t[0]), Integer.parseInt(t[1]));
	}
	
	public void setPrefix(String pref)
	{
		txt = pref;
		updateTime();
	}
	
	public String getPrefix()
	{
		return txt;
	}
	
	public String getTime()
	{
		return hour.toString()+":"+minute.toString();
	}
}
