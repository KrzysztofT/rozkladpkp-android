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
package org.tyszecki.rozkladpkp;
import java.util.ArrayList;

import org.tyszecki.rozkladpkp.ConnectionDetailsItem.DateItem;
import org.tyszecki.rozkladpkp.ConnectionDetailsItem.TrainItem;
import org.tyszecki.rozkladpkp.PLN.Connection;
import org.tyszecki.rozkladpkp.PLN.Train;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class ConnectionDetailsItemAdapter extends BaseAdapter {

	final int HEADER = 0;
	final int NORMAL = 1;
	
	private ArrayList<ConnectionDetailsItem> items;
	Context c;
	
	private PLN pln;
	private int conidx;
	private boolean platformInfo,delayInfo;
	
	private SpannableStringBuilder spanBuilder = new SpannableStringBuilder();
	private ForegroundColorSpan greenSpan = new ForegroundColorSpan(Color.rgb(73,194,98));
	private ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.rgb(220, 59, 76));
	private ForegroundColorSpan yellowSpan = new ForegroundColorSpan(Color.rgb(197,170,73));
	
	public ConnectionDetailsItemAdapter(Context context, PLN pln, int connectionIndex) {
		c = context;
		this.items = new ArrayList<ConnectionDetailsItem>();
		
		conidx = connectionIndex;
		this.pln = pln; 
		loadData();
	}
	
	private void loadData()
	{
		items.clear();
    
		platformInfo = false;
		delayInfo = false;
		
    	ConnectionDetailsItem c = new ConnectionDetailsItem();
    	Connection con = pln.connections[conidx];
    	
    	for(int i = 0; i < con.getTrainCount(); ++i)
    	{
    		Train t = con.getTrain(i);
    		
    		if(t.getChange() != null)
    			delayInfo = true;
    		
    		TrainItem ti = c.new TrainItem();
    		ti.t = t;
    		items.add(ti);
    	}
    	
    	notifyDataSetChanged();
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ConnectionDetailsItem con = items.get(position);
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(con instanceof TrainItem) 
            	v = vi.inflate(R.layout.connection_details_row, null);
            
            else if(con instanceof DateItem)
            	v = vi.inflate(R.layout.common_date_header_row, null);
            else 
            	v = vi.inflate(R.layout.scrollitem, null);
        }
        
        if (con instanceof TrainItem) {
        	Train t = ((TrainItem)con).t;
        	
        	
        	String platform;
        	
        	
        	if(t.getDeparturePlatform().equals("---"))
        		platform = "";
        	else 
        		platform = " "+t.getDeparturePlatform().trim();
        	
        	if(t.getChange() != null)
        	{
        		spanBuilder.clearSpans();
            	spanBuilder.clear();
            	spanBuilder.append(t.deptime.toString());
            	
            	ForegroundColorSpan span;
            	
            	int delay = t.getChange().realdeptime.difference(t.deptime).intValue();
            	if(delay <= 0)
            		span = greenSpan;
            	else if(delay <= 5)
            		span = yellowSpan;
            	else
            		span = redSpan;
            	
            	spanBuilder.append(" +");
            	spanBuilder.append(Integer.toString(delay));
            	
            	int len = spanBuilder.length();
            	spanBuilder.setSpan(span, 6, len, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            	spanBuilder.append(platform);
            	
            	((TextView) v.findViewById(R.id.departure_time)).setText(spanBuilder);
        	}
        	else
        		((TextView) v.findViewById(R.id.departure_time)).setText(t.deptime.toString()+platform);
        	
        	if(t.getArrivalPlatform().equals("---"))
        		platform = "";
        	else 
        		platform = " "+t.getArrivalPlatform().trim();
        	
        	if(t.getChange() != null)
        	{
        		spanBuilder.clearSpans();
            	spanBuilder.clear();
            	spanBuilder.append(t.arrtime.toString());
            	
            	ForegroundColorSpan span;
            	
            	int delay = t.getChange().realarrtime.difference(t.arrtime).intValue();
            	if(delay <= 0)
            		span = greenSpan;
            	else if(delay <= 5)
            		span = yellowSpan;
            	else
            		span = redSpan;
            	
            	spanBuilder.append(" +");
            	spanBuilder.append(Integer.toString(delay));
            	
            	int len = spanBuilder.length();
            	spanBuilder.setSpan(span, 6, len, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            	spanBuilder.append(platform);
            	
            	((TextView) v.findViewById(R.id.arrival_time)).setText(spanBuilder);
        	}
        	else
        		((TextView) v.findViewById(R.id.arrival_time)).setText(t.arrtime.toString()+platform);
        	
        	
        	
        	((TextView) v.findViewById(R.id.departure_station)).setText(t.depstation.name);
        	((TextView) v.findViewById(R.id.arrival_station)).setText(t.arrstation.name);
        	
        	((TextView) v.findViewById(R.id.train_number)).setText(CommonUtils.trainDisplayName(t.number));
        	((TextView) v.findViewById(R.id.train_number)).setBackgroundResource(CommonUtils.drawableForTrainType(CommonUtils.trainType(t.number)));
        	((TextView) v.findViewById(R.id.train_number)).requestLayout();
        	
        	((TextView) v.findViewById(R.id.duration)).setText(t.arrtime.difference(t.deptime).toString());
        }
        else if (con instanceof DateItem)
        {
        	//TextView head = (TextView) v.findViewById(R.id.conn_header);
            //head.setText(((ConnectionItem.DateItem)con).date);
        }
        
        return v;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int arg0) {
		return items.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public int getItemViewType(int arg0) {
		
		if(items.get(arg0) instanceof ConnectionDetailsItem.DateItem)
			return HEADER;
		else if(items.get(arg0) instanceof ConnectionDetailsItem.TrainItem)
			return NORMAL;
		return NORMAL;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return items.size() == 0;
	}
	
	public boolean areAllItemsSelectable() {  
        return false;  
    }  
	@Override
    public boolean isEnabled(int position) {  
        return (getItemViewType(position) != HEADER);  
    }  
}
