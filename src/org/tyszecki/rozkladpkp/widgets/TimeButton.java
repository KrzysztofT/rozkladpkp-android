/*******************************************************************************
 * This file is part of the RozkladPKP project.
 * 
 *     RozkladPKP is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     RozkladPKP is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License 
 *     along with RozkladPKP.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.tyszecki.rozkladpkp.widgets;

import org.tyszecki.rozkladpkp.R;

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
