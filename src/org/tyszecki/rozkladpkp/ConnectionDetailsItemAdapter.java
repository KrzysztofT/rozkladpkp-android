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

import org.tyszecki.rozkladpkp.ConnectionDetailsItem.PriceItem;
import org.tyszecki.rozkladpkp.ConnectionDetailsItem.TrainItem;
import org.tyszecki.rozkladpkp.PLN.Connection;
import org.tyszecki.rozkladpkp.PLN.Train;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
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

	final int PRICE = 0;
	final int NORMAL = 1;
	
	private ArrayList<ConnectionDetailsItem> items;
	Context c;
	
	private PLN pln;
	private int conidx;
	private boolean platformInfo,delayInfo;
	private String km,k1,k2;
	
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
		
    	
    	Connection con = pln.connections[conidx];
    	
    	for(int i = 0; i < con.getTrainCount(); ++i)
    	{
    		Train t = con.getTrain(i);
    		
    		if(t.getChange() != null)
    			delayInfo = true;
    		
    		TrainItem ti = new TrainItem();
    		ti.t = t;
    		items.add(ti);
    	}
    	items.add(new PriceItem());
    	
    	notifyDataSetChanged();
	}
	
	public void setPrice(String km, String k1, String k2)
	{
		this.km = km;
		this.k1 = k1;
		this.k2 = k2;
		
		notifyDataSetChanged();
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ConnectionDetailsItem con = items.get(position);
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(con instanceof TrainItem) 
            	v = vi.inflate(R.layout.connection_details_row, null);
            else if(con instanceof PriceItem)
            	v = vi.inflate(R.layout.connection_details_price_row, null);
        }
        
        if (con instanceof TrainItem) {
        	Train t = ((TrainItem)con).t;
        	
        	
        	String aplatform,dplatform;
        	
        	if(t.getDeparturePlatform().equals("---"))
        		dplatform = "";
        	else 
        		dplatform = ", "+t.getDeparturePlatform().trim();
        	
        	if(t.getChange() != null && t.getChange().realdeptime != null)
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
            	((TextView) v.findViewById(R.id.departure_time)).setText(spanBuilder);
        	}
        	else
        		((TextView) v.findViewById(R.id.departure_time)).setText(t.deptime.toString());
        	
        	if(t.getArrivalPlatform().equals("---"))
        		aplatform = "";
        	else 
        		aplatform = ", "+t.getArrivalPlatform().trim();
        	
        	if(t.getChange() != null && t.getChange().realarrtime != null)
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
            	
            	((TextView) v.findViewById(R.id.arrival_time)).setText(spanBuilder);
        	}
        	else
        		((TextView) v.findViewById(R.id.arrival_time)).setText(t.arrtime.toString());
        	
        	
        	
        	((TextView) v.findViewById(R.id.departure_station)).setText(t.depstation.name+dplatform);
        	((TextView) v.findViewById(R.id.arrival_station)).setText(t.arrstation.name+aplatform);
        	
        	((TextView) v.findViewById(R.id.train_number)).setText(CommonUtils.trainDisplayName(t.number));
        	((TextView) v.findViewById(R.id.train_number)).setBackgroundResource(CommonUtils.drawableForTrainType(CommonUtils.trainType(t.number)));
        	((TextView) v.findViewById(R.id.train_number)).requestLayout();
        	
        	((TextView) v.findViewById(R.id.duration)).setText(t.arrtime.difference(t.deptime).toString());
        }
        else if (con instanceof PriceItem)
        {
        	PriceItem i = (PriceItem)con;
        	
        	TextView head = (TextView) v.findViewById(R.id.price);
        	
        	if(km == null)
        		head.setText("Sprawdź cenę");
        	else if(km.equals("-1"))
        		head.setText("Błąd pobierania ceny");
        	else
        		head.setText(Html.fromHtml("Odległość: <b>"+km+"</b>km<br> Klasa 1: <b>"+k1+"</b>zł<br> Klasa 2: <b>"+k2+"</b>zł<br>Cena nie uwzględnia zniżek i promocji."));
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
		if(items.get(arg0) instanceof ConnectionDetailsItem.TrainItem)
			return NORMAL;
		else return PRICE;
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
		if(position == items.size() -1 && km != null)
			return false;
        return true;  
    }  
}
