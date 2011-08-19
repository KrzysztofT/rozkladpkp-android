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

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.tyszecki.rozkladpkp.TimetableItem.DateItem;
import org.tyszecki.rozkladpkp.TimetableItem.TrainItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class TimetableItemAdapter extends BaseAdapter {

	final int HEADER = 0;
	final int NORMAL = 1;
	
    private ArrayList<TimetableItem> items;
    private boolean dep;
    Context c;

    public TimetableItemAdapter(Context context, ArrayList<TimetableItem> items) {
    		c = context;
            this.items = items;
    }
    
    public void setType(boolean d)
    {
    	dep = d;
    }
    
    public void setXML(String xmlstring)
    {
    	try{
    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder db = factory.newDocumentBuilder();
    		InputSource inStream = new InputSource();
    		inStream.setCharacterStream(new StringReader("<a>"+xmlstring+"</a>"));
    		Document doc = db.parse(inStream);

    		NodeList list = doc.getElementsByTagName("Journey");

    		items.clear();

    		TimetableItem bi = new TimetableItem();
    		String pdate = "";
    		int j = list.getLength();
    		for(int i = 0; i < j; i++)
    		{ 
    			TrainItem o = bi.new TrainItem();
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
    				DateItem d = bi.new DateItem();
    				d.date = o.date;
    				pdate = o.date;
    				items.add(d);
    			}

    			NodeList msgs = n.getChildNodes();
    			for(int k = 0; k < msgs.getLength(); k++)
    			{
    				Node c	= msgs.item(k);
    				if(c.getNodeName().equals("HIMMessage"))
    					o.message += c.getAttributes().getNamedItem("header").getNodeValue();
    			}
    			items.add(o);
    		}
    	}catch (Exception e) {}
    	notifyDataSetChanged();
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            TimetableItem b = items.get(position);
            
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (b instanceof TrainItem)
                	v = vi.inflate(R.layout.timetable_row, null);
                else
                	v = vi.inflate(R.layout.common_date_header_row, null);
            }
            
            if (b instanceof TrainItem) {
            		
            		TrainItem o = (TrainItem)b;
            		
                    ((TextView) v.findViewById(R.id.time)).setText(Html.fromHtml("<b>"+o.time+"</b> "));
                    ((TextView) v.findViewById(R.id.station)).setText(Html.fromHtml(((dep)?"do ":"z ")+"<b>"+o.station+"</b> "));
                    
                    
                    TextView type = (TextView) v.findViewById(R.id.train_type);
                    type.requestLayout(); //Przeliczenie wymiarów, dzięki tej linijce po kilkukrotnym przewinięciu listy, elementy nie będą tej samej długości
                   
                    String t = CommonUtils.trainType(o.number);
                    type.setText(t.length() > 0 ? t : "Osob");
                    type.setBackgroundResource(CommonUtils.drawableForTrainType(t));                    
            }
            else {
            	TextView head = (TextView) v.findViewById(R.id.conn_header);
                head.setText(((DateItem)b).date);
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
		return items.get(arg0) instanceof DateItem ? HEADER : NORMAL;
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
