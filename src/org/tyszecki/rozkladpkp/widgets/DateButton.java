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

import java.util.GregorianCalendar;

import org.tyszecki.rozkladpkp.R;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.format.Time;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.DatePicker;

public class DateButton extends Button implements DialogControl {

	Integer day,month,year;
	Time time;
	private String txt = "";
	
	public DateButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.ic_menu_today), null, null, null);
		time	= new Time();
	}

	public DialogFragment getDialog() {
	    return new DatePickerFragment();
	}
	
	private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int dyear, 
                                  int dmonth, int dday) {
                year = dyear;
                month = dmonth+1;
                day = dday;
                updateDate();
            }
        };
        
	private void updateDate()
	{
		GregorianCalendar date = new GregorianCalendar(year,month-1,day);
		java.text.DateFormat df = android.text.format.DateFormat.getLongDateFormat(getContext());
		setText(Html.fromHtml(txt+"<b>"+df.format(date.getTime())+"</b>"));
	}
	
	public void setToNow()
	{
		time.setToNow();
		day 	= time.monthDay;
		month	= time.month+1;
		year	= time.year;
		
		updateDate();
	}

	public void setDate(int day, int month, int year)
	{
		this.day = day;
		this.month	= month;
		this.year	= year;
		
		
		updateDate();
	}
	
	public void setDate(String date)
	{
		if(date == null)
			setToNow();
		else
		{
			String[] t = date.split("\\."); 
			if(t.length >= 3)
				setDate(Integer.parseInt(t[0]), Integer.parseInt(t[1]),Integer.parseInt(t[2]));
		}
	}
	
	public void setPrefix(String pref)
	{
		txt = pref;
		updateDate();
	}
	
	public String getPrefix()
	{
		return txt;
	}
	
	public String getDate()
	{
		return day.toString()+"."+month.toString()+"."+year.toString();
	}
	
	private class DatePickerFragment extends DialogFragment{
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new DatePickerDialog(getActivity(), mDateSetListener, year, month-1, day);
		}
	}
}
