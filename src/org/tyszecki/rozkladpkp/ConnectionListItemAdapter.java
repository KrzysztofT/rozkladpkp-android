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
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.tyszecki.rozkladpkp.pln.UnboundConnection;
import org.tyszecki.rozkladpkp.pln.PLN;
import org.tyszecki.rozkladpkp.pln.PLN.Train;
import org.tyszecki.rozkladpkp.pln.PLN.TrainChange;
import org.tyszecki.rozkladpkp.servers.HafasServer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.format.Time;
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


public class ConnectionListItemAdapter extends BaseAdapter implements Observer{

	public final static int HEADER = 0;
	public final static int NORMAL = 1;
	public final static int SCROLL = 2;
	public final static int WARNING = 3;
	
	public final static int WARNING_STATIC = 1;
	public final static int WARNING_SERVER = 2;
	
	
	private ArrayList<Integer> items;
	private PLN pln;
	
	Context c;
	private boolean backupServerWarning = false;
	private boolean scrolling = false;
	private boolean delayInfo = false;
	private String depStation;
	private ConnectionList clist;
	
	private int dep_width,arr_width;
	private int textSize;
	private SpannableStringBuilder spanBuilder = new SpannableStringBuilder();
	private ForegroundColorSpan greenSpan = new ForegroundColorSpan(Color.rgb(73,194,98));
	private ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.rgb(220, 59, 76));
	private ForegroundColorSpan yellowSpan = new ForegroundColorSpan(Color.rgb(197,170,73));
	
	public ConnectionListItemAdapter(Context context) {
		c = context;
		items = new ArrayList<Integer>();
		
		TypedArray t = c.obtainStyledAttributes(new int[]{android.R.attr.textSize});
		textSize = t.getDimensionPixelSize(0, -1);
		if(textSize == -1)
			textSize = (int) ((new TextView(c)).getTextSize());
	}
	
	public void setConnectionList(ConnectionList list)
	{
		clist = list;
		list.addObserver(this);
	}
	
	private void calculateTextSizes() {
		 TextPaint tp = new TextPaint();
         tp.setTypeface(Typeface.DEFAULT_BOLD);
         tp.setTextSize(textSize+3);
         
         //Obliczenie wielkości pola tekstowego dla odjazdów
         int t = 0;
         for(int i = 0; i < pln.connectionCount(); ++i)
         {
        	 UnboundConnection c =  pln.connections[i];
        	 if(c.getChange() != null && Math.abs(c.getChange().departureDelay) > t)
        		 t = Math.abs(c.getChange().departureDelay);
         }
         
         float timew = tp.measureText("23:55 +"+Integer.toString(t));
         dep_width = (int) (timew+1);
         
         //Obliczenie wielkości pola tekstowego dla przyjazdów
         t = -1000;
         for(int i = 0; i < pln.connectionCount(); ++i)
         {
        	 Train tr =  pln.connections[i].getTrain(pln.connections[i].trainCount-1);
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
	
	Spannable delayText(String time, int delay)
	{
		ForegroundColorSpan span;
		if(delay <= 0)
    		span = greenSpan;
    	else if(delay <= 5)
    		span = yellowSpan;
    	else
    		span = redSpan;
    	
    	 
    	time += (delay >= 0) ? " +" : " ";
    	time += Integer.toString(delay);
    	
    	spanBuilder.clearSpans();
    	spanBuilder.clear();
    	spanBuilder.append(time);
    	spanBuilder.setSpan(span, 6, time.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    	
    	return spanBuilder;
	}

	public View getView(int position, View v, ViewGroup parent) {
         LayoutInflater vi = null;
		if (v == null) 
            vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
		if(scrolling && (position == 0 || position == getCount()-1))
			v = fillScrollingItem(position == 0, (v == null) ? vi.inflate(R.layout.scrollitem, null) : v); //Nast/Pop
		else if(position < headerItems())
			v = fillWarningItem(scrolling ? WARNING_SERVER : WARNING_STATIC, (v == null) ? vi.inflate(R.layout.warning_item, null) : v); //Ostrzeżenia
		else
		{
			//Data lub połączenie
			position = items.get(position - headerItems());
			if(position < 0) //Data 
				v = fillDateItem(-position-1,  (v == null) ? vi.inflate(R.layout.common_date_header_row, null) : v);
			else //Połączenie
				v = fillConnectionItem(position, (v == null) ? vi.inflate(R.layout.connection_list_row, null) : v);
		}
		
        return v;
	}

	private View fillConnectionItem(int connectionIndex, View view) {
		UnboundConnection o = pln.connections[connectionIndex];
		
        TextView tt = (TextView) view.findViewById(R.id.departure_time);
        TextView bt = (TextView) view.findViewById(R.id.arrival_time);

        ((ImageView) view.findViewById(R.id.info_icon)).setVisibility(o.hasMessages() ? View.VISIBLE : View.GONE);
        ((ImageView) view.findViewById(R.id.walk_icon)).setVisibility(o.getTrain(0).depstation.name.equals(depStation) ? View.GONE : View.VISIBLE);
        
        int tl = o.trainCount;
        
        if(delayInfo)
        {
        	tt.setWidth(dep_width);
        	if(arr_width > 0)
        		bt.setWidth(arr_width);
        }
        
        String deptime = o.getTrain(0).deptime.toString(); 
        if(o.getChange() != null && o.getChange().departureDelay != -1)
        	tt.setText(delayText(deptime, o.getChange().departureDelay));
        else 
        	tt.setText(deptime);
  
        Train last = o.getTrain(tl-1);
        String arrtime = last.arrtime.toString();
        if(last.getChange() != null && last.getChange().realarrtime != null)
        	bt.setText(delayText(arrtime, last.getChange().realarrtime.difference(last.arrtime).intValue()));
        else 
        	bt.setText(arrtime);
      
        ((TextView) view.findViewById(R.id.changes)).setText(Integer.toString(o.changesCount));
        ((TextView) view.findViewById(R.id.duration)).setText(o.getJourneyTime().toLongString());
        
        LinearLayout lay = (LinearLayout)view.findViewById(R.id.type_icons);
        
        
        lay.removeAllViews();
        for(int i = 0; i < tl; i++)
        { 
        	if(o.getTrain(i).number.equals("Fußweg") || o.getTrain(i).number.equals("Übergang"))
        		continue;
        	
        	String s = CommonUtils.trainType(o.getTrain(i).number);
        	s = (s.length() > 0 ? s : "OS");
        	TextView t = new TextView(c);
        	
        	t.setText(s);
        	t.setTextColor(Color.BLACK);
        	
        	t.setGravity(Gravity.CENTER_VERTICAL);
        	t.setSingleLine();
        	
            t.setBackgroundResource(CommonUtils.drawableForTrainType(s));
        	lay.addView(t);
        }

        return view;
	}

	private View fillDateItem(int dayNumber, View view) {
		TextView head = (TextView) view.findViewById(R.id.conn_header);
        head.setText(pln.days().getDay(dayNumber).format("%A, %d.%m.%Y"));
        return view;
	}

	private View fillWarningItem(int type, View view) {
		if(type == WARNING_SERVER)
			((TextView) view.findViewById(R.id.text)).setText("Uwaga! Z powodu problemów z serwerem systemu SITKOL, zwrócono wyniki z serwera kolei niemieckich. Mogą one zawierać mniej szczegółów i być mniej nieaktualne. Proszę zachować ostrożność.");
		else 
			((TextView) view.findViewById(R.id.text)).setText("Oglądasz w tej chwili zapisane wyniki wyszukiwania. Dotknij tutaj, aby rozpocząć nowe wyszukiwanie.");
		return view;
	}

	private View fillScrollingItem(boolean top, View view) {
		TextView head = (TextView) view.findViewById(R.id.scrollitem_text);
        if(top)
        	head.setText("Wcześniejsze połączenia");
        else
        	head.setText("Późniejsze połączenia");
        return view;
	}

	@Override
	public int getCount() {
        return items.size()+staticItems();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
	
	public int getConnectionAt(int position)
	{
		Log.i("RozkladPKP", Integer.toString(position));	
		int c = -1;
		try{
			c = items.get(position - headerItems());
		}
		catch(Exception e){}
		
		if(c < 0)
			c = -1;
		
		return c;
	}
	
	public Time getDateForConnectionAt(int position)
	{
		position -= headerItems();
		int p = 0;
		do{
			p = items.get(position--);
		}while(position >= 0 && p >= 0);
		
		return pln.days().getDay(-p-1);
	}

	@Override
	public int getItemViewType(int position) {

        if(scrolling && position == 0)
        	return SCROLL;
        else if(position < headerItems())
        	return WARNING;
        else if(position - headerItems() < items.size())
        {
        	//Data lub połączenie
        	position = items.get(position - headerItems());
        	if(position < 0) //Data 
        		return HEADER;
        	else //Połączenie
        		return NORMAL;
        }
        else
        	return SCROLL;
        
		//TODO: DRY!
	}

	@Override
	public int getViewTypeCount() {
		return 4;
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
	
	public int headerItems()
	{
		//+1, ponieważ jeśli scrolling, to 1, a jeśli !scrolling, to też 1 (ostrzeżenie o statycznym)
		return (backupServerWarning ? 1 : 0) + 1;
	}
	
	public int footerItems()
	{
		return (scrolling ? 1 : 0);
	}
	
	public int staticItems()
	{
		return headerItems() + footerItems();
	}
	
	String getContentForSharing()
	{
		String msg = pln.departureStation().name + " - " +  pln.arrivalStation().name+" ";
		
		for(Map.Entry<Integer, List<Integer>> l : pln.days().getDaysMap().entrySet())
		{
			
			msg += pln.days().getDay(l.getKey()).format("%d.%m.%Y") + ":\n";
			
			for(Integer c : l.getValue())
			{
				UnboundConnection con = pln.connections[c];
				
				if(con.changesCount == 0)
					msg += con.getTrain(0).deptime.toString()+",\n";
				
				else
				{
					msg += con.getTrain(0).deptime.toString();
					msg += con.trainCount > 2 ? " z przesiadkami (" :  " z przesiadką (";
					
					for(int i = 1; i < con.trainCount; ++i)
					{
						Train tr = con.getTrain(i);
						msg += tr.depstation.name + " "+tr.deptime.toString();
						
						if(i < con.trainCount -1 )
							msg += ", ";
					}
					msg += "),\n";
				}
			}
		}
		return msg.substring(0,msg.length()-2)+'.';
	}

	@Override
	public void update(Observable observable, Object data) {
		if(clist.getLastError() != HafasServer.DOWNLOAD_OK || clist.getPLN() == null || clist.getPLN().conCnt == 0)
			return;
		
		pln = clist.getPLN();
		depStation = pln.departureStation().name;
		scrolling = clist.scrollable();

		backupServerWarning = clist.getServerId() > 0;

		delayInfo = pln.hasDelayInfo();
		if(delayInfo)
			calculateTextSizes();
		
		
		items.clear();
			
		for(Map.Entry<Integer, List<Integer>> l : pln.days().getDaysMap().entrySet())
		{
			//Dni są oznaczone liczbami ujemnymi, zaczynając od -1
			//Pierwszy dzień to -1, następny to -2, itd.
			items.add(-l.getKey()-1);

			//Dodanie numerów połączeń
			for(Integer i : l.getValue())
				items.add(i);
		}

		notifyDataSetChanged();
	}
}
