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
import java.util.HashMap;

import org.tyszecki.rozkladpkp.ConnectionListItem.DateItem;
import org.tyszecki.rozkladpkp.ConnectionListItem.ScrollItem;
import org.tyszecki.rozkladpkp.ConnectionListItem.TripItem;
import org.tyszecki.rozkladpkp.ExternalDelayFetcher.ExternalDelayFetcherCallback;
import org.tyszecki.rozkladpkp.PLN.Connection;
import org.tyszecki.rozkladpkp.PLN.Train;
import org.tyszecki.rozkladpkp.PLN.TrainChange;
import org.tyszecki.rozkladpkp.PLN.Trip;
import org.tyszecki.rozkladpkp.PLN.TripIterator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ConnectionListItemAdapter extends BaseAdapter {

	final int HEADER = 0;
	final int NORMAL = 1;
	final int SCROLL = 2;
	
	static final int PRELOAD_ITEMS = 30;
	static final String LOG_TAG = "PAGEADAPTER";
	Boolean loading;
	boolean allItemsLoaded;
	
	private ArrayList<ConnectionListItem> items;
	private PLN pln;
	TripIterator it;
	
	Context c;
	private String lastDate;
	private boolean scrolling = true;
	private boolean delayInfo = false;
	
	private int dep_width,arr_width;
	private int textSize;
	private SpannableStringBuilder spanBuilder = new SpannableStringBuilder();
	private ForegroundColorSpan greenSpan = new ForegroundColorSpan(Color.rgb(73,194,98));
	private ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.rgb(220, 59, 76));
	private ForegroundColorSpan yellowSpan = new ForegroundColorSpan(Color.rgb(197,170,73));

	/*Drawable d;
	private class mySpan implements LineBackgroundSpan {

		@Override
		public void drawBackground (Canvas c, Paint p, int left, int right, int top, 
				int baseline, int bottom, CharSequence text, int start, int end, int lnum) {
			
			Log.i("RozkladPKP","maluje");
            d.setBounds(left, top, right, bottom);
				d.draw(c);
		}
		
	}*/
	
	public ConnectionListItemAdapter(Context context) {
		c = context;
		TypedArray t = c.obtainStyledAttributes(new int[]{android.R.attr.textSize});
		textSize = t.getDimensionPixelSize(0, -1);
		if(textSize == -1)
			textSize = (int) ((new TextView(c)).getTextSize());
		
		items = new ArrayList<ConnectionListItem>();
	}
	
	public void setPLN(PLN file, boolean loadAll, boolean delays)
	{
		pln = file;
		
		delayInfo = delays;
		if(delays)
			calculateTextSizes();
			
		it = pln.tripIterator();
	
		lastDate = "";
		loadData(loadAll);
		
		ExternalDelayFetcher.requestUpdate(new ExternalDelayFetcherCallback() {
			
			@Override
			public void ready(HashMap<String, Integer> delays, boolean cached) {
				lastDate = "";
				it = pln.tripIterator();
				
				pln.addExternalDelayInfo(delays);
				
				if(pln.hasDelayInfo())
				{
					delayInfo = true;
					calculateTextSizes();
				}
				
				loadData(true);
			}
		});
	}
	
	

	private void calculateTextSizes() {
		 TextPaint tp = new TextPaint();
         tp.setTypeface(Typeface.DEFAULT_BOLD);
         tp.setTextSize(textSize+3);
         
         //Obliczenie wielkości pola tekstowego dla odjazdów
         int t = 0;
         for(int i = 0; i < pln.connectionCount(); ++i)
         {
        	 Connection c =  pln.connections[i];
        	 if(c.getChange() != null && Math.abs(c.getChange().departureDelay) > t)
        		 t = Math.abs(c.getChange().departureDelay);
         }
         
         float timew = tp.measureText("23:55 +"+Integer.toString(t));
         dep_width = (int) (timew+1);
         
         //Obliczenie wielkości pola tekstowego dla przyjazdów
         t = -1000;
         for(int i = 0; i < pln.connectionCount(); ++i)
         {
        	 Train tr =  pln.connections[i].getTrain(pln.connections[i].getTrainCount()-1);
        	 TrainChange c =  tr.getChange();
        	 
        	 if(c != null && c.realarrtime != null && c.realarrtime.difference(tr.arrtime).intValue() > t)
        		 t = c.realarrtime.difference(tr.arrtime).intValue();
        	 
         }
         
         if(t != -1000)
         {
        	 timew = tp.measureText("23:55 +"+Integer.toString(t));
        	 arr_width = (int) (timew+1);
         }
         else
        	 arr_width = -1;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        
        ConnectionListItem con = items.get(position);
        if (v == null) {
        	
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            if(con instanceof TripItem) 
            	v = vi.inflate(R.layout.connection_list_row, null);
            else if(con instanceof DateItem)
            	v = vi.inflate(R.layout.common_date_header_row, null);
            else 
            	v = vi.inflate(R.layout.scrollitem, null);
        }
        
        if (con instanceof TripItem) {
        		Connection o = ((TripItem)con).t.con;
        		
                TextView tt = (TextView) v.findViewById(R.id.departure_time);
                TextView bt = (TextView) v.findViewById(R.id.arrival_time);

                ((ImageView) v.findViewById(R.id.info_icon)).setVisibility(o.hasMessages() ? View.VISIBLE : View.GONE);
                
                int tl = o.getTrainCount();
                
                if(delayInfo)
                {
                	tt.setWidth(dep_width);
                	if(arr_width > 0)
                		bt.setWidth(arr_width);
                }
                
                String deptime = o.getTrain(0).deptime.toString(); 
                if(o.getChange() != null && o.getChange().departureDelay != -1)
                {
                	
                	int delay = o.getChange().departureDelay;
                	ForegroundColorSpan span;
                	
                	if(delay <= 0)
                		span = greenSpan;
                	else if(delay <= 5)
                		span = yellowSpan;
                	else
                		span = redSpan;
                	
                	 
                	deptime += (delay >= 0) ? " +" : " ";
                	deptime += Integer.toString(delay);
                	
                	spanBuilder.clearSpans();
                	spanBuilder.clear();
                	spanBuilder.append(deptime);
                	spanBuilder.setSpan(span, 6, deptime.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                	tt.setText(spanBuilder);
                }
                else 
                	tt.setText(deptime);
          
                Train last = o.getTrain(tl-1);
                String arrtime = last.arrtime.toString();
                if(last.getChange() != null && last.getChange().realarrtime != null)
                {
                	int delay = last.getChange().realarrtime.difference(last.arrtime).intValue();
                	ForegroundColorSpan span;
                	
                	if(delay <= 0)
                		span = greenSpan;
                	else if(delay <= 5)
                		span = yellowSpan;
                	else
                		span = redSpan;
                	
                	
                	arrtime += (delay >= 0) ? " +" : " ";
                	arrtime += Integer.toString(delay);
                	
                	spanBuilder.clearSpans();
                	spanBuilder.clear();
                	spanBuilder.append(arrtime);
                	spanBuilder.setSpan(span, 6, arrtime.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                	
                	bt.setText(spanBuilder);
                }
                else 
                	bt.setText(arrtime);
                	
                
                /*spanBuilder.clearSpans();
            	spanBuilder.clear();
            	
            	
            	LineBackgroundSpan ispan = new LineBackgroundSpan() {
					
					@Override
					public void drawBackground(Canvas c, Paint p, int left, int right, int top,
							int baseline, int bottom, CharSequence text, int start, int end,
							int lnum) {
						Log.i("RozkladPKP","maluje");
			            d.setBounds(left, top, right, bottom);
			            c.skew(10, 10);
							d.draw(c);
						
					}
				};
            	String ts = Integer.toString(o.changes);
            	spanBuilder.append("bla ");
            	spanBuilder.append(ts);
            	spanBuilder.append("bla");
            	spanBuilder.setSpan(ispan, 1, 5, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
*/            	
                ((TextView) v.findViewById(R.id.changes)).setText(Integer.toString(o.changes));
            	//((TextView) v.findViewById(R.id.changes)).setText(spanBuilder);
                ((TextView) v.findViewById(R.id.duration)).setText(o.getJourneyTime().toLongString());
                
                LinearLayout lay = (LinearLayout)v.findViewById(R.id.type_icons);
                
                lay.removeAllViews();
                for(int i = 0; i < tl; i++)
                { 
                	if(o.getTrain(i).number.equals("Fußweg") || o.getTrain(i).number.equals("Übergang"))
                		continue;
                	
                	String s = CommonUtils.trainType(o.getTrain(i).number);
                	
                	TextView t = new TextView(c);
                	t.setText(s.length() > 0 ? s : "OS");
                	t.setTextColor(Color.BLACK);
                	t.setPadding(0, 0, 6, 0);
                	t.setGravity(Gravity.CENTER_VERTICAL);
                	t.setSingleLine();
                    
                    t.setBackgroundResource(CommonUtils.drawableForTrainType(s));
                	lay.addView(t);
                }
                
        }
        else if (con instanceof DateItem)
        {
        	TextView head = (TextView) v.findViewById(R.id.conn_header);
            head.setText(((ConnectionListItem.DateItem)con).date);
        }
        else
        {
        	TextView head = (TextView) v.findViewById(R.id.scrollitem_text);
	        if(((ScrollItem)con).up)
	        	head.setText("Wcześniejsze połączenia");
	        else
	        	head.setText("Późniejsze połączenia");
        }
        return v;
        
	}

	@Override
	public int getCount() {
        return items.size();
	}

	@Override
	public ConnectionListItem getItem(int arg0) {
		if(arg0 < 0 || arg0 >= items.size())
			return null;
		return items.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
	
	//TODO: Przypadek w którym it nie jest w items
	public int getTripId(TripItem it)
	{
		int c = 0;
		
		for(ConnectionListItem i : items)
		{
			if(i == it)
				return c;
			else if(i instanceof TripItem)
				++c;
		}
		
		return -1;
	}

	@Override
	public int getItemViewType(int arg0) {
		
		if(items.get(arg0) instanceof ConnectionListItem.DateItem)
			return HEADER;
		else if(items.get(arg0) instanceof ConnectionListItem.TripItem)
			return NORMAL;
		else
			return SCROLL;
	}

	@Override
	public int getViewTypeCount() {
		return 3;
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
	
	private void loadData(boolean loadAll) {
		items.clear();
		
		//FIXME: Zrobic to poprawniej, teraz jest to "skrot myslowy"
		if(loadAll && scrolling)
			items.add(new ConnectionListItem.ScrollItem(true));
		while(it.hasNext()){
			
        	Trip t = it.next();
        	if(!t.date.equals(lastDate))
        	{
        		ConnectionListItem.DateItem d = new ConnectionListItem.DateItem();
        		
        		
        		d.date = t.date;
        		items.add(d);
        		lastDate = t.date;
        	}
        		
        	TripItem ti = new ConnectionListItem.TripItem();
        	ti.t = t;
        	items.add(ti);
        	
        	if(!loadAll)
        		if(items.size() > PRELOAD_ITEMS)
        			break;
    	}	
		if(scrolling)
			items.add(new ConnectionListItem.ScrollItem(false));
		
		notifyDataSetChanged();
	}
	
	void loadMore()
	{
		int i = 0;
		int s = items.size()-2;
		
		while(it.hasNext() && i++ < PRELOAD_ITEMS){
			
        	Trip t = it.next();
        	if(!t.date.equals(lastDate))
        	{
        		ConnectionListItem.DateItem d = new ConnectionListItem.DateItem();
        		d.date = t.date;
        		items.add(s+i,d);
        		s++;
        		lastDate = t.date;
        	}
        		
        	TripItem ti = new ConnectionListItem.TripItem();
        	ti.t = t;
        	items.add(s+i,ti);
    	}	
		if(!it.hasNext())
			items.remove(items.size()-1);
		notifyDataSetChanged();
	}
	
	String getContentForSharing()
	{
		String msg = pln.departureStation().name + " - " +  pln.arrivalStation().name+" ";
		
		for(ConnectionListItem it : items)
		{
			if(it instanceof ConnectionListItem.DateItem)
				msg += ((ConnectionListItem.DateItem)it).date + ":\n";
			else if(it instanceof ConnectionListItem.TripItem)
			{
				TripItem t = (ConnectionListItem.TripItem)it;
				Connection con = t.t.con;
				
				if(con.changes == 0)
					msg += con.getTrain(0).deptime.toString()+",\n";
				
				else
				{
					msg += con.getTrain(0).deptime.toString();
					msg += con.getTrainCount() > 2 ? " z przesiadkami (" :  " z przesiadką (";
					
					for(int i = 1; i < con.getTrainCount(); ++i)
					{
						Train tr = con.getTrain(i);
						msg += tr.depstation.name + " "+tr.deptime.toString();
						
						if(i < con.getTrainCount() -1 )
							msg += ", ";
					}
					msg += "),\n";
				}
			}
		}
		
		return msg.substring(0,msg.length()-2)+'.';
	}

	public void setScrollingEnabled(boolean b) {
		scrolling = false;		
	}
	
}
