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
import org.tyszecki.rozkladpkp.PLN.Train;

import android.content.Context;
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

	public ConnectionDetailsItemAdapter(Context context, ArrayList<ConnectionDetailsItem> objects) {
		c = context;
		this.items = objects;
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
        	
        	((TextView) v.findViewById(R.id.departure_time)).setText(t.deptime.toString());
        	((TextView) v.findViewById(R.id.arrival_time)).setText(t.arrtime.toString());
        	
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
