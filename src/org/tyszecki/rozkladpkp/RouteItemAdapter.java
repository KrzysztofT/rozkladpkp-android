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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


class RouteItemAdapter extends ArrayAdapter<RouteItem> {

	
    private ArrayList<RouteItem> items;
    /*private boolean dep;
    private String station = "";*/
    private int stationPos = -1;

    public RouteItemAdapter(Context context, int textViewResourceId, ArrayList<RouteItem> items) {
            super(context, textViewResourceId, items);
            this.items = items;
    }
    
    public void setCurrentStation(String id, int pos)
    {
    	//station = id;
    	stationPos	= pos;
    }
    
    
    public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                    	if(stationPos == 0)
                    		imgId = R.drawable.start_green;
                    	else
                    		imgId = R.drawable.start_gray;
                    }
                    else if(last)
                    	imgId = R.drawable.end_green;
                    else
                    {
                    	if(position == stationPos)
                    		imgId	= R.drawable.sta_graygreen;
                    	else if(position > stationPos)
                    		imgId	= R.drawable.sta_green;
                    }
                    
                    ImageView icon = (ImageView) v.findViewById(R.id.icon);
                    
                    icon.setImageDrawable(getContext().getResources().getDrawable(imgId));
            }
            return v;
    }
}
