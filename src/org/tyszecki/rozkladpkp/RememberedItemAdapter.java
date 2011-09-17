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

import org.tyszecki.rozkladpkp.RememberedItem.HeaderItem;
import org.tyszecki.rozkladpkp.RememberedItem.RouteItem;
import org.tyszecki.rozkladpkp.RememberedItem.TimetableItem;
import org.tyszecki.rozkladpkp.RememberedItem.TimetableType;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.opengl.Visibility;
import android.text.format.Time;
import android.util.Log;
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
		Cursor cur = db.rawQuery("SELECT sidFrom,sidTo,fromName,toName,_id,cacheValid FROM storedview WHERE type=2 AND fav=1", null);
		while(cur.moveToNext())
		{
			RouteItem t = new RouteItem();
			t.SIDFrom = cur.getInt(0);
			t.SIDTo = cur.getInt(1);
			t.fromName = cur.getString(2);
			t.toName = cur.getString(3);
			if(t.fromName == null || t.toName == null)
				continue;
			t.id = cur.getInt(4);
			t.cacheValid = cur.getString(5);
			items.add(t);
		}
		cur.close();
		h = new HeaderItem();
		h.text = "Rozkłady";
		items.add(h);
		
		cur = db.rawQuery("SELECT type,sidFrom,fromName,_id,cacheValid FROM storedview WHERE type != 2 AND fav=1", null);
		while(cur.moveToNext())
		{
			TimetableItem t = new TimetableItem();
			t.type = (cur.getInt(0) == 0) ? TimetableType.Departure : TimetableType.Arrival;
			t.SID = cur.getInt(1);
			t.name = cur.getString(2);
			if(t.name == null)
				continue;
			t.id = cur.getInt(3);
			t.cacheValid = cur.getString(4);
			items.add(t);
		}
		cur.close();
		
		h = new HeaderItem();
		h.text = "Ostatnio wyszukiwane";
		items.add(h);
		
		//Zwraca nazwy i SIDy ostatnio wyszukiwanych
		cur = db.rawQuery("SELECT type,sidFrom,sidTo,fromName,toName,_id,cacheValid FROM storedview WHERE fav IS NULL ORDER by _id DESC", null);
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
				
				if(t.fromName == null || t.toName == null)
					continue;
				
				t.id = cur.getInt(5);
				t.cacheValid = cur.getString(6);
				
				items.add(t);
			}
			else
			{
				TimetableItem t = new TimetableItem();
				t.type = (cur.getInt(0) == 0) ? TimetableType.Departure : TimetableType.Arrival;
				t.SID = cur.getInt(1);
				t.name = cur.getString(3);
				if(t.name == null)
					continue;
				t.id = cur.getInt(5);
				t.cacheValid = cur.getString(6);
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
		
		Time now = new Time();
		now.setToNow();
		Time itime = new Time();
		
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
        		
        		boolean showSaved = r.cacheValid != null && r.cacheValid.length() > 0;
        		
        		if(showSaved)
        		{
        			try{
        			itime.parse(r.cacheValid);
        			
        			if(Time.compare(itime, now) < 0)
        			{
        				c.deleteFile(CommonUtils.ResultsHash(Integer.toString(r.SIDFrom), Integer.toString(r.SIDTo), null));
        				showSaved = false;
        				r.cacheValid = null;
        				
        				SQLiteDatabase db = DatabaseHelper.getDbRW(c);
        				db.execSQL("UPDATE stored SET cacheValid='' WHERE _id="+Integer.toString(r.id));
        				db.close();
        			}
        			}catch(Exception e)
        			{}
        		}
        		
        		v.findViewById(R.id.saved_icon).setVisibility(showSaved ? View.VISIBLE : View.INVISIBLE);
        	}
        	else
        	{
        		TimetableItem t = (TimetableItem)b;
        		text.setText(((t.type == TimetableType.Departure) ? "Odjazdy z " : "Przyjazdy do ") + t.name);
        		
        		boolean showSaved = t.cacheValid != null && t.cacheValid.length() > 0;
        		
        		if(showSaved)
        		{
        			try{
        			itime.parse(t.cacheValid);
        			
        			
        			//Log.i("RozkladPKP","NOW: "+now.toString());
        			
        			if(Time.compare(itime, now) < 0)
        			{
        				c.deleteFile(CommonUtils.ResultsHash(Integer.toString(t.SID), null, null));
        				showSaved = false;
        				t.cacheValid = null;
        				
        				SQLiteDatabase db = DatabaseHelper.getDbRW(c);
        				db.execSQL("UPDATE stored SET cacheValid='' WHERE _id="+Integer.toString(t.id));
        				db.close();
        			}
        			}catch(Exception e)
        			{}
        		}
        		
        		v.findViewById(R.id.saved_icon).setVisibility(showSaved ? View.VISIBLE : View.INVISIBLE);
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
