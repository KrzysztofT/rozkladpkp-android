package org.tyszecki.rozkladpkp;
import java.util.ArrayList;

import org.tyszecki.rozkladpkp.ConnectionItem.DateItem;
import org.tyszecki.rozkladpkp.ConnectionItem.ScrollItem;
import org.tyszecki.rozkladpkp.ConnectionItem.TripItem;
import org.tyszecki.rozkladpkp.PLN.Connection;
import org.tyszecki.rozkladpkp.PLN.Trip;
import org.tyszecki.rozkladpkp.PLN.TripIterator;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ConnectionItemAdapter extends BaseAdapter {

	final int HEADER = 0;
	final int NORMAL = 1;
	final int SCROLL = 2;
	
	static final int PRELOAD_ITEMS = 30;
	static final String LOG_TAG = "PAGEADAPTER";
	Boolean loading;
	boolean allItemsLoaded;
	
	private ArrayList<ConnectionItem> items;
	private PLN pln;
	TripIterator it;
	
	Context c;
	private String lastDate;	
	

	public ConnectionItemAdapter(Context context) {
		c = context;
		
		items = new ArrayList<ConnectionItem>();
	}
	
	public void setPLN(PLN file, boolean loadAll)
	{
		pln = file;
		it = pln.tripIterator();
	
		lastDate = "";
		loadData(loadAll);
	}
	
	

	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ConnectionItem con = items.get(position);
        if (v == null) {
        	
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            if(con instanceof TripItem) 
            	v = vi.inflate(R.layout.connectionrow, null);
            else if(con instanceof DateItem)
            	v = vi.inflate(R.layout.connectionheaderrow, null);
            else 
            	v = vi.inflate(R.layout.scrollitem, null);
        }
        
        if (con instanceof TripItem) {
        		Connection o = ((TripItem)con).t.con;
        		
                TextView tt = (TextView) v.findViewById(R.id.conn_dep);
                TextView bt = (TextView) v.findViewById(R.id.conn_arr);
                
                int tl = o.trains.length;
                
                if (tt != null) 
                	tt.setText(Html.fromHtml("<b>"+o.trains[0].deptime+"</b>"));
                      
                if(bt != null)
                	bt.setText(Html.fromHtml("<b>"+o.trains[tl-1].arrtime+"</b>"));
                
                ((TextView) v.findViewById(R.id.conn_changes)).setText(Integer.toString(o.changes));
                ((TextView) v.findViewById(R.id.conn_time)).setText(o.journeyTime.toLongString());
                
                LinearLayout lay = (LinearLayout)v.findViewById(R.id.conn_layout);
                
                lay.removeAllViews();
                for(int i = 0; i < tl; i++)
                {
                	String s = CommonUtils.trainType(o.trains[i].number);
                	
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
            head.setText(((ConnectionItem.DateItem)con).date);
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
	public ConnectionItem getItem(int arg0) {
		return items.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public int getItemViewType(int arg0) {
		
		if(items.get(arg0) instanceof ConnectionItem.DateItem)
			return HEADER;
		else if(items.get(arg0) instanceof ConnectionItem.TripItem)
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
		ConnectionItem c = new ConnectionItem();
		
		//FIXME: Zrobic to poprawniej, teraz jest to "skrot myslowy"
		if(loadAll)
			items.add(c.new ScrollItem(true));
		while(it.hasNext()){
			
        	Trip t = it.next();
        	if(!t.date.equals(lastDate))
        	{
        		ConnectionItem.DateItem d = c.new DateItem();
        		d.date = t.date;
        		items.add(d);
        		lastDate = t.date;
        	}
        		
        	TripItem ti = c.new TripItem();
        	ti.t = t;
        	items.add(ti);
        	
        	if(!loadAll)
        		if(items.size() > PRELOAD_ITEMS)
        			break;
    	}	
		items.add(c.new ScrollItem(false));
		notifyDataSetChanged();
	}
	
	void loadMore()
	{
		ConnectionItem c = new ConnectionItem();
		int i = 0;
		int s = items.size()-2;
		
		while(it.hasNext() && i++ < PRELOAD_ITEMS){
			
        	Trip t = it.next();
        	if(!t.date.equals(lastDate))
        	{
        		ConnectionItem.DateItem d = c.new DateItem();
        		d.date = t.date;
        		items.add(s+i,d);
        		s++;
        		lastDate = t.date;
        	}
        		
        	TripItem ti = c.new TripItem();
        	ti.t = t;
        	items.add(s+i,ti);
    	}	
		if(!it.hasNext())
			items.remove(items.size()-1);
		notifyDataSetChanged();
	}
	
}
