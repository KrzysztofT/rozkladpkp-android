package org.tyszecki.rozkladpkp;
import java.util.ArrayList;

import org.tyszecki.rozkladpkp.ConnectionInfoItem.DateItem;
import org.tyszecki.rozkladpkp.ConnectionInfoItem.TrainItem;
import org.tyszecki.rozkladpkp.PLN.Train;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class ConnectionInfoItemAdapter extends BaseAdapter {

	final int HEADER = 0;
	final int NORMAL = 1;
	
	private ArrayList<ConnectionInfoItem> items;
	Context c;

	public ConnectionInfoItemAdapter(Context context, ArrayList<ConnectionInfoItem> objects) {
		c = context;
		this.items = objects;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ConnectionInfoItem con = items.get(position);
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(con instanceof TrainItem) 
            	v = vi.inflate(R.layout.connectioninforow, null);
            else if(con instanceof DateItem)
            	v = vi.inflate(R.layout.connectionheaderrow, null);
            else 
            	v = vi.inflate(R.layout.scrollitem, null);
        }
        
        if (con instanceof TrainItem) {
        	Train t = ((TrainItem)con).t;
        	
        	((TextView) v.findViewById(R.id.conninfo_dep_time)).setText(t.deptime.toString());
        	((TextView) v.findViewById(R.id.conninfo_arr_time)).setText(t.arrtime.toString());
        	
        	((TextView) v.findViewById(R.id.conninfo_dep_station)).setText(t.depstation.name);
        	((TextView) v.findViewById(R.id.conninfo_arr_station)).setText(t.arrstation.name);
        	
        	((TextView) v.findViewById(R.id.conninfo_train)).setText(t.number);
        	//((TextView) v.findViewById(R.id.conninfo_time)).setText(t.);
        }
        else if (con instanceof DateItem)
        {
        	TextView head = (TextView) v.findViewById(R.id.conn_header);
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
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public int getItemViewType(int arg0) {
		
		if(items.get(arg0) instanceof ConnectionInfoItem.DateItem)
			return HEADER;
		else if(items.get(arg0) instanceof ConnectionInfoItem.TrainItem)
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
