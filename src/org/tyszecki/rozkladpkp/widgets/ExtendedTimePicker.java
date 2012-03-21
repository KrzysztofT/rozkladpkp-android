package org.tyszecki.rozkladpkp.widgets;

import java.util.Calendar;

import org.tyszecki.rozkladpkp.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class ExtendedTimePicker extends AlertDialog implements
		OnTimeChangedListener, OnClickListener {

	public interface OnExtendedTimeChanged{
		void onTimeSet(TimePicker view, int hourOfDay, int minute, boolean arrival);
	};
	
	private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String IS_ARRIVAL = "isArrival";
	
	private final TimePicker picker;
	private final CheckBox checkbox;
	private final OnExtendedTimeChanged callback;
	private final Calendar calendar;
	private final java.text.DateFormat dateFormat;
	
	int initialHourOfDay;
    int initialMinute;
    boolean arrival;
    
	protected ExtendedTimePicker(Context context, OnExtendedTimeChanged callBack, int hourOfDay, int minute, boolean arrival) {
		super(context,R.style.AboutDialog);
		
		callback = callBack;
		initialHourOfDay = hourOfDay;
		initialMinute = minute;
		
		dateFormat = DateFormat.getTimeFormat(context);
		calendar = Calendar.getInstance();
		updateTitle(initialHourOfDay, initialMinute);
		
		setButton("Ustaw", this);
        setButton2("Anuluj", (OnClickListener) null);
        
        View view =  View.inflate(new ContextThemeWrapper(context, R.style.AboutDialog),R.layout.extended_time_picker_dialog, null );
        setView(view);
        
        picker = (TimePicker)view.findViewById(R.id.timePicker);
        checkbox = (CheckBox)view.findViewById(R.id.arrivalCheckbox);
        checkbox.setChecked(arrival);
        
        picker.setCurrentHour(initialHourOfDay);
        picker.setCurrentMinute(initialMinute);
        picker.setIs24HourView(true);
        picker.setOnTimeChangedListener(this);
	}
	

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (callback != null) {
            picker.clearFocus();
            callback.onTimeSet(picker, picker.getCurrentHour(), 
                    picker.getCurrentMinute(),checkbox.isChecked());
        }

	}

	@Override
	public void onTimeChanged(TimePicker arg0, int hourOfDay, int minute) {
		updateTitle(hourOfDay, minute);
	}
	
	public void updateTime(int hourOfDay, int minutOfHour) {
        picker.setCurrentHour(hourOfDay);
        picker.setCurrentMinute(minutOfHour);
    }
	
	private void updateTitle(int hour, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        setTitle(dateFormat.format(calendar.getTime()));
    }
	
	@Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(HOUR, picker.getCurrentHour());
        state.putInt(MINUTE, picker.getCurrentMinute());
        state.putBoolean(IS_ARRIVAL, checkbox.isChecked());
        return state;
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        picker.setCurrentHour(hour);
        picker.setCurrentMinute(minute);
        picker.setIs24HourView(true);
        picker.setOnTimeChangedListener(this);
        checkbox.setChecked(savedInstanceState.getBoolean(IS_ARRIVAL));
        updateTitle(hour, minute);
    }

}
