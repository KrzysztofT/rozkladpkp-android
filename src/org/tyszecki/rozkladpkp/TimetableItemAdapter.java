package org.tyszecki.rozkladpkp;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.tyszecki.rozkladpkp.TimetableItem.DateItem;
import org.tyszecki.rozkladpkp.TimetableItem.ScrollItem;
import org.tyszecki.rozkladpkp.TimetableItem.TrainItem;
import org.tyszecki.rozkladpkp.TimetableItem.WarningItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TimetableItemAdapter  extends BaseAdapter {

	boolean arrival,saved = false;
	Time top,bottom,passed;
	String stationID;
	String productString;
	
	final int ITEM_HEADER = 0;
	final int ITEM_NORMAL = 1;
	final int ITEM_SCROLL = 2;
	final int ITEM_WARNING = 3;
	
	
    private ArrayList<TimetableItem> items;
    Context c;
	
	public TimetableItemAdapter(String stationID, String products, Time time, boolean arrival, Context context) {
	
		this.stationID = stationID;
		this.arrival = arrival;
		top = new Time(time);
		bottom = new Time(time);
		passed = time;
		productString = products;
		c = context;
		
		if(!arrival)
			moveBackward();
		
		items = new ArrayList<TimetableItem>();
	}
	
	public TimetableItemAdapter(String stationID, String products, Time time, boolean arrival, Context context, String filename)
	{
		this(stationID, products, time, arrival, context);
	
		saved = true;
		
		FileInputStream fis;
		try {
			fis = RozkladPKPApplication.getAppContext().openFileInput(filename);
		} catch (FileNotFoundException e) {
			return;
		}
		
		ByteArrayOutputStream content = new ByteArrayOutputStream();
	    
		byte[] sBuffer = new byte[256]; 
		
	    int readBytes = 0;
	    try {
			while ((readBytes = fis.read(sBuffer)) != -1)
				content.write(sBuffer, 0, readBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setXML(content.toString(), true);
	}
	
	
	public void fetch()
	{
		items.clear();
		items.add(ScrollItem.progressItem());
		notifyDataSetChanged();
		fetchMore(true);
	}
	
	private Time moveForward(Time t)
	{
		if(t.hour < 23)
			t.hour++;
		else if(t.minute == 59)
		{
			t.hour = 1;
			t.minute = 0;
			t.monthDay++;
		}
		else
			t.minute = 59;
		
		t.normalize(false);
		bottom = t;
		return t;
	}
	
	private void moveBackward() {
		
		if(top.hour != 1)
			top.hour--;
		else if(top.minute != 0)
			top.minute = 0;
		else
		{
			top.hour -= 2;
			top.minute = 59;
		}
		top.normalize(false);
	}
	
	public Time getBottomTime()
	{
		Time t;
		if(items.size() < 3) //Tylko nagłówki
			t = passed;
		else
		{
			TrainItem ti = (TrainItem)getItem(items.size()-2);
			t = CommonUtils.timeFromString(new Time(), ti.date, ti.time);
		}
		
		if(bottom.after(t))
			t = bottom;
		
		if(arrival)
			t = moveForward(t);
		
		
		return t;
	}
	
	public void fetchMore(boolean next)
	{
		if(next)
		{
			((ScrollItem)items.get(getCount()-1)).inProgress = true;
			notifyDataSetChanged();
			
			download(getBottomTime(), next);
		}
		else
		{
			download(new Time(top), next);
			moveBackward();
		}
	}
	
	public void download(Time time, boolean next)
	{
		TimetableTask task = new TimetableTask(productString, time, stationID, arrival, next);
		task.execute();
	}
	
	private class TimetableTask extends TimetableFetcher{
		boolean n;
		public TimetableTask(String products, Time datetime, String stationID,
				boolean arrival, boolean next) {
			super(products, datetime, stationID, arrival);
			n = next;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			((ScrollItem)items.get(getCount()-1)).inProgress = false;
			notifyDataSetChanged();
			
			setXML(result, n);
			

			//Zapisywanie w pamięci
			Intent in = new Intent(RozkladPKPApplication.getAppContext(), RememberedService.class);

			in.putExtra("timetable", result.getBytes());
			in.putExtra("SID", stationID);
			in.putExtra("departure", !arrival);
			in.putExtra("time", getBottomTime().format2445());
			
			RozkladPKPApplication.getAppContext().startService(in);
		}
	}
	
	public void setXML(String xmlstring, boolean next)
    {
    	try{
    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder db = factory.newDocumentBuilder();
    		InputSource inStream = new InputSource();
    		inStream.setCharacterStream(new StringReader(xmlstring));
    		Document doc = db.parse(inStream);
    		

    		NodeList list = doc.getElementsByTagName("Journey");

    		int j = list.getLength();
    		String cdate = null, pdate = "";
    		if(items.size() < 2)
    		{    			
    			items.clear();
    			//items.add(ScrollItem.scrollItem(true));
    			if(saved)
    				items.add(new WarningItem());
    			
    			items.add(ScrollItem.scrollItem(false));
    			
    		}
    		else
    		{
    			if(next)
    				pdate = ((TrainItem)items.get(items.size()-2)).date;
    			else if(j > 0)
    			{
    				cdate = ((DateItem)items.get(0)).date;
    				items.remove(0);
    			}
    		}
    		
    		
    		int lastRes = -1;
    		
    		for(int i = 0; i < j; i++)
    		{ 
    			TrainItem o = new TrainItem();
    			Node n = list.item(i);
    			o.station 	= n.getAttributes().getNamedItem("targetLoc").getNodeValue();
    			o.time 		= n.getAttributes().getNamedItem("fpTime").getNodeValue();
    			o.date 		= n.getAttributes().getNamedItem("fpDate").getNodeValue();
    			o.delay		= n.getAttributes().getNamedItem("delay").getNodeValue();
    			o.number 	= n.getAttributes().getNamedItem("prod").getNodeValue();

    			int hix = o.number.indexOf('#');
    			if(hix > 0)
    				o.number = o.number.substring(0, hix);

    			if(!pdate.equals(o.date)){
    				DateItem d = new DateItem();
    				d.date = o.date;
    				pdate = o.date;
    				
    				lastRes = addItem(d,lastRes,next);
    			}

    			/*NodeList msgs = n.getChildNodes();
    			for(int k = 0; k < msgs.getLength(); k++)
    			{
    				Node c	= msgs.item(k);
    				if(c.getNodeName().equals("HIMMessage"))
    					o.message += c.getAttributes().getNamedItem("header").getNodeValue();
    			}*/
    			lastRes = addItem(o,lastRes,next);
    		}
    		if(cdate != null && !cdate.equals(pdate))
    		{
    			DateItem d = new DateItem();
				d.date = cdate;
				addItem(d,lastRes,next);
    		}
    		
    			
    	}catch (Exception e) {e.printStackTrace();}
    	notifyDataSetChanged();
    }
	
	private int addItem(TimetableItem item, int lastResult, boolean next)
	{
		if(lastResult == -1)
		{
			if(next)
				lastResult = items.size()-1;
			else
				lastResult = 0;
		}
		
		//TODO: Można to ładniej zrobić - wyszukiwanie duplikatów
		if(item instanceof TrainItem)
		{
			TrainItem u = (TrainItem)item;
			for(TimetableItem i : items)
				if(i instanceof TrainItem)
				{
					TrainItem t = (TrainItem)i;
					if(t.number.equals(u.number) && t.date.equals(u.date) && t.time.equals(u.time))
						return lastResult;
				}	
		}
		
		items.add(lastResult, item);

		return ++lastResult;
	}
	
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public TimetableItem getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        TimetableItem b = items.get(position);
        
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (b instanceof TrainItem)
            	v = vi.inflate(R.layout.timetable_row, null);
            else if(b instanceof DateItem)
            	v = vi.inflate(R.layout.common_date_header_row, null);
            else if(b instanceof ScrollItem)
            	v = vi.inflate(R.layout.scrollitem, null);
            else
            	v = vi.inflate(R.layout.warning_item, null);
        }
        
        if (b instanceof TrainItem) {
        		
        		TrainItem o = (TrainItem)b;
        		
                ((TextView) v.findViewById(R.id.time)).setText(Html.fromHtml("<b>"+o.time+"</b> "));
                ((TextView) v.findViewById(R.id.station)).setText(Html.fromHtml(((!arrival)?"do ":"z ")+"<b>"+o.station+"</b> "));
                
                
                TextView type = (TextView) v.findViewById(R.id.train_type);
                type.requestLayout(); //Przeliczenie wymiarów, dzięki tej linijce po kilkukrotnym przewinięciu listy, elementy nie będą tej samej długości
               
                String t = CommonUtils.trainType(o.number);
                type.setText(t.length() > 0 ? t : "Osob");
                type.setBackgroundResource(CommonUtils.drawableForTrainType(t));                    
        }
        else if(b instanceof DateItem){
        	TextView head = (TextView) v.findViewById(R.id.conn_header);
            head.setText(((DateItem)b).date);
        }
        else if(b instanceof ScrollItem)
        {
        	ScrollItem s = (ScrollItem)b;
        	
        	TextView text = (TextView) v.findViewById(R.id.scrollitem_text);
        	ProgressBar progress = (ProgressBar) v.findViewById(R.id.progress);
        	
        	if(s.inProgress)
        	{
        		progress.setVisibility(View.VISIBLE);
        		text.setText("Trwa pobieranie rozkładu...");
        	}
        	else
        	{
        		progress.setVisibility(View.GONE);
        		if(s.up)
        			text.setText("Poprzednie połączenia");
        		else
        			text.setText("Następne połączenia");
        	}
        }
        else
        	((TextView) v.findViewById(R.id.text)).setText("Oglądasz zapisany rozkład. Dotknij tutaj, aby rozpocząć nowe wyszukiwanie");
        
        return v;
	}
	
	@Override
	public int getItemViewType(int arg0) {
		TimetableItem it = items.get(arg0);
		
		if(it instanceof DateItem) return  ITEM_HEADER;
		if(it instanceof TrainItem) return ITEM_NORMAL;
		if(it instanceof ScrollItem) return ITEM_SCROLL;
		return ITEM_WARNING;
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
		int type = getItemViewType(position);
		
		if(type == ITEM_HEADER)
			return false;
		else if(type == ITEM_NORMAL | type == ITEM_WARNING)
			return true;
		
		return !((ScrollItem) getItem(position)).inProgress;
    }  
}
