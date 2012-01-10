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
import org.tyszecki.rozkladpkp.pln.PLN;
import org.tyszecki.rozkladpkp.pln.PLN.Train;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class ConnectionDetailsItemAdapter extends BaseAdapter {

	final int PRICE = 0;
	final int NORMAL = 1;
	final int INFO = 2;
	final int AVAILABILITY = 3;
	
	Context c;
	
	
	private PLN.Connection connection;
	private int trainCount,messageCount;
	
	private String km,k1,k2;
	
	private SpannableStringBuilder spanBuilder = new SpannableStringBuilder();
	private ForegroundColorSpan greenSpan = new ForegroundColorSpan(Color.rgb(73,194,98));
	private ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.rgb(220, 59, 76));
	private ForegroundColorSpan yellowSpan = new ForegroundColorSpan(Color.rgb(197,170,73));
	
	public ConnectionDetailsItemAdapter(Context context, PLN pln, int connectionIndex) {
		c = context;
		
		connection = pln.connections[connectionIndex];
		trainCount = connection.getTrainCount();
		messageCount = connection.hasMessages() ? connection.getMessages().length : 0;
		
		notifyDataSetChanged();
	}
	
	public void setPrice(String km, String k1, String k2)
	{
		this.km = km;
		this.k1 = k1;
		this.k2 = k2;
		
		notifyDataSetChanged();
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        LayoutInflater vi = null;
        
        if(v == null)
        	vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        if(position < trainCount) //Na początku są pociągi
        	v = fillTrainInfo(position, (v == null) ? vi.inflate(R.layout.connection_details_row, null) : v);
        else if(position == trainCount) //Potem jest informacja o dniach kursowania
        	v = fillAvailabilityInfo((v == null) ? vi.inflate(R.layout.warning_item, null) : v);
        else if(position < trainCount+1+messageCount) //Informacje o problemach
        	v = fillWarningInfo(position-trainCount-1, (v == null) ? vi.inflate(R.layout.message_row, null) : v);
        else //I pobranie ceny
        	v = fillPriceInfo((v == null) ? vi.inflate(R.layout.connection_details_price_row, null) : v);
        
        return v;
	}

	private View fillPriceInfo(View view) {
		TextView head = (TextView) view.findViewById(R.id.price);
    	
    	if(km == null)
    		head.setText("Sprawdź cenę");
    	else if(km.equals("-1"))
    		head.setText("Błąd pobierania ceny");
    	else
    	{
    		String discount = PreferenceManager.getDefaultSharedPreferences(RozkladPKPApplication.getAppContext()).getString("discountValue", "0");
    		
    		boolean showDiscount = true;
    		int dval = 0;
    		try{dval = Integer.parseInt(discount);}
    		catch (Exception e) {
				showDiscount = false;
			}
    		
    		if(dval <= 0 || dval >= 100)
    			showDiscount = false;
    		
    		String msg = "Odległość: <b>"+km+"</b>km<br> Klasa 1: <b>"+k1+"</b>zł<br> Klasa 2: <b>"+k2+"</b>zł<br>";
    		String footer = showDiscount ? "Cena nie uwzględnia dodatkowych zniżek i promocji. Cena podana po zniżce ma jedynie charakter orientacyjny." : "Cena nie uwzględnia zniżek i promocji.";
    		
    		if(showDiscount)
    		{
    			dval = 100-dval;
    			msg += "<br>Cena ze zniżką "+discount+"%:<br>";
    			
    			String zk1 = "---";
    			String zk2 = "---";
    			try{
    				float pr = Float.parseFloat(k1.replace(',', '.'));
    				int ipr = (int) (pr*100);
    				ipr *= dval;
    				ipr /= 100;
    				zk1 = Integer.toString(ipr/100)+","+String.format("%02d", ipr%100);
    			}catch (Exception e) {}
    			try{
    				float pr = Float.parseFloat(k2.replace(',', '.'));
    				int ipr = (int) (pr*100);
    				ipr *= dval;
    				ipr /= 100;
    				zk2 = Integer.toString(ipr/100)+","+String.format("%02d", ipr%100);
    			}catch (Exception e) {}
    			
    			msg += "Klasa 1: <b>"+zk1+"</b>zł<br> Klasa 2: <b>"+zk2+"</b>zł<br><br>";
    		}
    		
    		head.setText(Html.fromHtml(msg+footer));
    	}
		return view;
	}

	private View fillWarningInfo(int position, View view) {
		((TextView)view.findViewById(R.id.brief)).setText(connection.getMessages()[position].brief);
		((TextView)view.findViewById(R.id.text)).setText(connection.getMessages()[position].full);
		return view;
	}

	private View fillAvailabilityInfo(View view) {
    	String msg  = connection.availability.getMessage();
    	
    	if(msg == null)
    		msg = "Brak informacji o kursowaniu.";
    	else
    		msg = "Kursuje "+msg.replace(';', '\n');
    	((TextView)view.findViewById(R.id.text)).setText(msg);
    	
		return view;
	}

	private View fillTrainInfo(int position, View view) {
		
		Train t = connection.getTrain(position);
    	
    	String aplatform,dplatform;
    	
    	if(t.getDeparturePlatform().equals("---"))
    		dplatform = "";
    	else 
    		dplatform = ", "+t.getDeparturePlatform().trim();
    	
    	if(t.getChange() != null && t.getChange().realdeptime != null)
    	{
    		spanBuilder.clearSpans();
        	spanBuilder.clear();
        	spanBuilder.append(t.deptime.toString());
        	
        	ForegroundColorSpan span;
        	
        	int delay = t.getChange().realdeptime.difference(t.deptime).intValue();
        	if(delay <= 0)
        		span = greenSpan;
        	else if(delay <= 5)
        		span = yellowSpan;
        	else
        		span = redSpan;
        	
        	spanBuilder.append(" +");
        	spanBuilder.append(Integer.toString(delay));
        	
        	int len = spanBuilder.length();
        	spanBuilder.setSpan(span, 6, len, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        	((TextView) view.findViewById(R.id.departure_time)).setText(spanBuilder);
    	}
    	else
    		((TextView) view.findViewById(R.id.departure_time)).setText(t.deptime.toString());
    	
    	if(t.getArrivalPlatform().equals("---"))
    		aplatform = "";
    	else 
    		aplatform = ", "+t.getArrivalPlatform().trim();
    	
    	if(t.getChange() != null && t.getChange().realarrtime != null)
    	{
    		spanBuilder.clearSpans();
        	spanBuilder.clear();
        	spanBuilder.append(t.arrtime.toString());
        	
        	ForegroundColorSpan span;
        	
        	int delay = t.getChange().realarrtime.difference(t.arrtime).intValue();
        	if(delay <= 0)
        		span = greenSpan;
        	else if(delay <= 5)
        		span = yellowSpan;
        	else
        		span = redSpan;
        	
        	spanBuilder.append(" +");
        	spanBuilder.append(Integer.toString(delay));
        	
        	int len = spanBuilder.length();
        	spanBuilder.setSpan(span, 6, len, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        	
        	((TextView) view.findViewById(R.id.arrival_time)).setText(spanBuilder);
    	}
    	else
    		((TextView) view.findViewById(R.id.arrival_time)).setText(t.arrtime.toString());
    	
    	
    	
    	((TextView) view.findViewById(R.id.departure_station)).setText(t.depstation.name+dplatform);
    	((TextView) view.findViewById(R.id.arrival_station)).setText(t.arrstation.name+aplatform);
    	
    	((TextView) view.findViewById(R.id.train_number)).setText(CommonUtils.trainDisplayName(t.number));
    	((TextView) view.findViewById(R.id.train_number)).setBackgroundResource(CommonUtils.drawableForTrainType(CommonUtils.trainType(t.number)));
    	((TextView) view.findViewById(R.id.train_number)).requestLayout();
    	
    	((TextView) view.findViewById(R.id.duration)).setText(t.arrtime.difference(t.deptime).toString());
    	
    	return view;
	}

	@Override
	public int getCount() {
		return trainCount+1+messageCount+1;
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
	public int getItemViewType(int pos) {
		if(pos < trainCount) return NORMAL;
		else if(pos == trainCount) return AVAILABILITY;
		else if(pos < trainCount+1+messageCount) return INFO;
		return PRICE;
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
		return false;
	}
	
	public boolean areAllItemsSelectable() {  
        return false;  
    }  
	@Override
    public boolean isEnabled(int position) {  
		int type = getItemViewType(position);
			if(type == AVAILABILITY || (type == PRICE && km != null))return false;
        return true;  
    }  
}
