package org.tyszecki.rozkladpkp;
import java.util.ArrayList;

import org.tyszecki.rozkladpkp.ConnectionItem.TripItem;
import org.tyszecki.rozkladpkp.PLN.Connection;

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

import org.tyszecki.rozkladpkp.R;


public class ConnectionItemAdapter extends BaseAdapter {

	final int HEADER = 0;
	final int NORMAL = 1;
	
	private ArrayList<ConnectionItem> items;
	Context c;

	public ConnectionItemAdapter(Context context, ArrayList<ConnectionItem> objects) {
		c = context;
		this.items = objects;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ConnectionItem con = items.get(position);
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(con instanceof TripItem) 
            	v = vi.inflate(R.layout.connectionrow, null);
            else
            	v = vi.inflate(R.layout.connectionheaderrow, null);
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
                	String s = o.trains[i].getType();
                	
                	TextView t = new TextView(c);
                	t.setText(s);
                	t.setTextColor(Color.BLACK);
                	t.setPadding(0, 0, 6, 0);
                	t.setGravity(Gravity.CENTER_VERTICAL);
                	t.setSingleLine();
                	
                	
                    int iid = R.drawable.back_reg;
                    
                    if(s.equals("TLK"))iid = R.drawable.back_tlk;
                    else if(s.equals("D"))iid = R.drawable.back_tlk;
                    
                    else if(s.equals("EC"))iid = R.drawable.back_ec;
                    else if(s.equals("EIC"))iid = R.drawable.back_eic;
                    else if(s.equals("EN"))iid = R.drawable.back_eic;
                    
                    else if(s.equals("KD"))iid = R.drawable.back_reg;
                    
                    else if(s.equals("IR"))iid = R.drawable.back_ir;
                    else if(s.equals("RE"))iid = R.drawable.back_re;
                    
                    else if(s.equals("SKM"))iid = R.drawable.back_skm;
                    else if(s.equals("SKW"))iid = R.drawable.back_skm;
                    else if(s.equals("WKD"))iid = R.drawable.back_skm;

                    else iid = R.drawable.back_reg;
                    
                    t.setBackgroundResource(iid);
                    	
                	lay.addView(t);
                }
                
        }
        else
        {
        	TextView head = (TextView) v.findViewById(R.id.conn_header);
            head.setText(((ConnectionItem.DateItem)con).date);
        }
        return v;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public int getItemViewType(int arg0) {
		return items.get(arg0) instanceof ConnectionItem.DateItem ? HEADER : NORMAL;
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
