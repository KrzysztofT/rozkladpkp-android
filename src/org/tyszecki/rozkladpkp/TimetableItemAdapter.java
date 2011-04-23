package org.tyszecki.rozkladpkp;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


class TimetableItemAdapter extends ArrayAdapter<TimetableItem> {

	
    private ArrayList<TimetableItem> items;
    private boolean dep;
    private String station = "";
    private int stationPos = -1;

    public TimetableItemAdapter(Context context, int textViewResourceId, ArrayList<TimetableItem> items) {
            super(context, textViewResourceId, items);
            this.items = items;
    }
    
    public void setCurrentStation(String id, int pos)
    {
    	station = id;
    	stationPos	= pos;
    }
    
    
    public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.timetablerow, null);
            }
            TimetableItem o = items.get(position);
            if (o != null) {
            		boolean last = (position == items.size()-1);
                    TextView tt = (TextView) v.findViewById(R.id.timetable_arr);
                    TextView bt = (TextView) v.findViewById(R.id.timetable_dep);
                    TextView ts = (TextView) v.findViewById(R.id.timetable_station);
                    if (tt != null) {
                    		if(position == 0 || o.arr == null)
                    			tt.setText("");
                    		else
                    			tt.setText(Html.fromHtml("P: <b>"+o.arr+"</b>"));
                          }
                    if(bt != null){
                    		if(last || o.dep == null)
                    			bt.setText("");
                    		else
                    			bt.setText(Html.fromHtml("O: <b>"+o.dep+"</b>"));
                    }
                    if(ts != null){
                        ts.setText(o.station);
                    }
                    int imgId = R.drawable.sta_gray;
                    
                    //Start
                    if(position == 0)
                    {
                    	if(stationPos == 0)
                    		imgId = R.drawable.start_green;
                    	else
                    		imgId = R.drawable.start_gray;
                    }
                    else if(last)
                    {
                    	imgId = R.drawable.end_green;
                    }
                    else
                    {
                    	if(position == stationPos)
                    		imgId	= R.drawable.sta_graygreen;
                    	else if(position > stationPos)
                    		imgId	= R.drawable.sta_green;
                    }
                    
                    ImageView icon = (ImageView) v.findViewById(R.id.timetable_icon);
                    

                    icon.setImageDrawable(getContext().getResources().getDrawable(imgId));
            }
            return v;
    }
}