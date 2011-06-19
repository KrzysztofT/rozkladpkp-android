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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


class RouteItemAdapter extends BaseAdapter {
	
    private ArrayList<RouteItem> items;
    
    private int startStationID = -1, endStationID = -1;
    private int startStationPos = -1, endStationPos = -1;
    Context c;

    public RouteItemAdapter(Context context) {
    	c = context;
    	this.items = new ArrayList<RouteItem>();    
    }
    
    public void setData(Document doc, int sID, int eID)
    {
    	startStationID = sID;
    	endStationID = eID;
    	loadData(doc);
    }
    
    private void loadData(Document doc) {
    	items.clear();
    	
    	NodeList list = doc.getElementsByTagName("St");
    	int j = list.getLength();
		for(int i = 0; i < j; i++)
        { 
			RouteItem o = new RouteItem();
        	Node n = list.item(i);
        	o.station 	= n.getAttributes().getNamedItem("name").getNodeValue();
        	
        	
        	Log.i("RozkladPKP", o.station);
        	
        	if(n.getAttributes().getNamedItem("arrTime") != null)
        		o.arr = n.getAttributes().getNamedItem("arrTime").getNodeValue();
        	else
        		o.arr = null;
        	if(n.getAttributes().getNamedItem("depTime") != null)
        		o.dep = n.getAttributes().getNamedItem("depTime").getNodeValue();
        	else
        		o.dep = null;
        	
        	o.stid	= n.getAttributes().getNamedItem("evaId").getNodeValue();
        	
        	int id = Integer.parseInt(o.stid); 
        	if(id == startStationID)
        		startStationPos = i;
        	else if(id == endStationID)
        		endStationPos = i;
        	
        	items.add(o);
        }
		
		notifyDataSetChanged();
	}

    
    public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.route_row, null);
            }
            RouteItem o = items.get(position);
            if (o != null) {
            		boolean last = (position == items.size()-1);
                    TextView tt = (TextView) v.findViewById(R.id.arrival_time);
                    TextView bt = (TextView) v.findViewById(R.id.departure_time);
                    TextView ts = (TextView) v.findViewById(R.id.station);
                   
                    boolean hide = (position == 0 || o.arr == null);
                    
                    tt.setText(hide ? "" : o.arr);
                    v.findViewById(R.id.arrival_label).setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
                    
					
					hide = (last || o.dep == null);
					bt.setText(hide ? "" : o.dep);
					v.findViewById(R.id.departure_label).setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
					
		
					ts.setText(o.station);
                    
                    int imgId = R.drawable.sta_gray;
                    
                    //Start
                    if(position == 0)
                    {
                    	if(startStationPos == 0)
                    		imgId = R.drawable.start_green;
                    	else
                    		imgId = R.drawable.start_gray;
                    }
                    else if(last)
                    {
                    	if(endStationPos == items.size() -1)
                    		imgId = R.drawable.end_green;
                    	else
                    		imgId = R.drawable.end_gray;
                    }	
                    else
                    {
                    	if(position == startStationPos)
                    		imgId	= R.drawable.sta_graygreen;
                    	else if(position == endStationPos)
                    		imgId	= R.drawable.sta_greengray;
                    	else if(position > startStationPos && position < endStationPos)
                    		imgId	= R.drawable.sta_green;
                    }
                    
                    v.findViewById(R.id.icon).setBackgroundDrawable(c.getResources().getDrawable(imgId));
            }
            return v;
    }

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
