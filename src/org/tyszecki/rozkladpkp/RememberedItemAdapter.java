package org.tyszecki.rozkladpkp;

import java.util.ArrayList;

import org.tyszecki.rozkladpkp.RememberedItem.HeaderItem;
import org.tyszecki.rozkladpkp.RememberedItem.RouteItem;
import org.tyszecki.rozkladpkp.RememberedItem.TimetableItem;
import org.tyszecki.rozkladpkp.RememberedItem.TimetableType;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RememberedItemAdapter extends BaseAdapter {

	private ArrayList<RememberedItem> items;
	private Context c;
	
	final int HEADER = 0;
	final int NORMAL = 1;
	
	public RememberedItemAdapter(Context context) {
		c = context;
		items = new ArrayList<RememberedItem>();
	}
	
	public void reloadData() {
		items.clear();
		
		HeaderItem h = new HeaderItem();
		h.text = "Trasy";
		items.add(h);
		
		SQLiteDatabase db = DatabaseHelper.getDb(c);
		Cursor cur = db.rawQuery("SELECT sidFrom,sidTo,fromName,toName,_id FROM storedview WHERE type=2 AND fav=1", null);
		while(cur.moveToNext())
		{
			RouteItem t = new RouteItem();
			t.SIDFrom = cur.getInt(0);
			t.SIDTo = cur.getInt(1);
			t.fromName = cur.getString(2);
			t.toName = cur.getString(3);
			t.id = cur.getInt(4);
			items.add(t);
		}
		cur.close();
		h = new HeaderItem();
		h.text = "Rozkłady";
		items.add(h);
		
		cur = db.rawQuery("SELECT type,sidFrom,fromName,_id FROM storedview WHERE type != 2 AND fav=1", null);
		while(cur.moveToNext())
		{
			TimetableItem t = new TimetableItem();
			t.type = (cur.getInt(0) == 0) ? TimetableType.Departure : TimetableType.Arrival;
			t.SID = cur.getInt(1);
			t.name = cur.getString(2);
			t.id = cur.getInt(3);
			items.add(t);
		}
		cur.close();
		
		h = new HeaderItem();
		h.text = "Ostatnio wyszukiwane";
		items.add(h);
		
		//Zwraca nazwy i SIDy ostatnio wyszukiwanych
		cur = db.rawQuery("SELECT type,sidFrom,sidTo,fromName,toName,_id FROM storedview WHERE fav IS NULL", null);
		while(cur.moveToNext())
		{
			//2 = trasa, 1 = przyjazdy, 0 = odjazdy
			if(cur.getInt(0) == 2)
			{
				RouteItem t = new RouteItem();
				t.SIDFrom = cur.getInt(1);
				t.SIDTo = cur.getInt(2);
				t.fromName = cur.getString(3);
				t.toName = cur.getString(4);
				
				t.id = cur.getInt(5);
				
				
				items.add(t);
			}
			else
			{
				TimetableItem t = new TimetableItem();
				t.type = (cur.getInt(0) == 0) ? TimetableType.Departure : TimetableType.Arrival;
				t.SID = cur.getInt(1);
				t.name = cur.getString(3);
				
				t.id = cur.getInt(5);
				
				items.add(t);
			}
		}
		cur.close();
		db.close();
		notifyDataSetChanged();
	} 
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public RememberedItem getItem(int arg0) {
		return items.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        RememberedItem b = items.get(position);
        
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (b instanceof HeaderItem)
            	v = vi.inflate(R.layout.common_date_header_row, null);
            else
            	v = vi.inflate(R.layout.remembered_row, null);
        }
        
        if (b instanceof HeaderItem) {
        	TextView text = (TextView) v.findViewById(R.id.conn_header);
        	text.setText(((HeaderItem)b).text);
        }
        else{
        	TextView text = (TextView) v.findViewById(R.id.text);
        	if(b instanceof RouteItem)
        	{
        		RouteItem r = (RouteItem)b;
        		text.setText(r.fromName + " → " + r.toName);
        	}
        	else
        	{
        		TimetableItem t = (TimetableItem)b;
        		text.setText(((t.type == TimetableType.Departure) ? "Odjazdy z " : "Przyjazdy do ") + t.name);
        	}
        }
        
		return v;
	}

	@Override
	public int getItemViewType(int pos) {
		return items.get(pos) instanceof HeaderItem ? HEADER : NORMAL;
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
		return false;
	}
	
	public boolean areAllItemsSelectable() {  
        return false;  
    }  
	
	@Override
    public boolean isEnabled(int position) {  
        return (getItemViewType(position) != HEADER);  
    }
	
	public void deleteItem(int position)
	{
		RememberedItem item = getItem(position);
		
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);		
		db.delete("stored", "_id=?", new String[]{Integer.toString(item.id)});
		db.close();
		
		reloadData();
	}

}
