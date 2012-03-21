package org.tyszecki.rozkladpkp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.tyszecki.rozkladpkp.ConnectionList.CachePolicy;
import org.tyszecki.rozkladpkp.pln.Connection;
import org.tyszecki.rozkladpkp.pln.UnboundConnection;
import org.tyszecki.rozkladpkp.pln.PLN;
import org.tyszecki.rozkladpkp.pln.PLN.Train;
import org.tyszecki.rozkladpkp.pln.PLN.TrainChange;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.Preference;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.format.Time;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.TextView;

public class ConnectionsWidget extends AppWidgetProvider {
	static final String ACTION_CONTROL = "org.tyszecki.rozkladpkp.ConnectionsWidget.WIDGET_CONTROL"; 
	public static final String URI_SCHEME = "connections_widget";
	
	final int CONNECTIONS_CACHED = 20;
	
	private enum FieldType{DepartureTime,ArrivalTime,Duration,Changes,Row}
	static int getViewID(FieldType field, int row)
	{
		//Możemy użyć do tego albo odbić i cache'ować wyniki, albo zahardcodować. Na razie niech będą zahardcodowane
		switch(field)
		{
		case DepartureTime:
			switch(row)
			{
			case 0: return R.id.departure_time_0;
			case 1: return R.id.departure_time_1;
			case 2: return R.id.departure_time_2;
			case 3: return R.id.departure_time_3;
			case 4: return R.id.departure_time_4;
			}
		case ArrivalTime:
			switch(row)
			{
			case 0: return R.id.arrival_time_0;
			case 1: return R.id.arrival_time_1;
			case 2: return R.id.arrival_time_2;
			case 3: return R.id.arrival_time_3;
			case 4: return R.id.arrival_time_4;
			}
		case Duration:
			switch(row)
			{
			case 0: return R.id.journey_time_0;
			case 1: return R.id.journey_time_1;
			case 2: return R.id.journey_time_2;
			case 3: return R.id.journey_time_3;
			case 4: return R.id.journey_time_4;
			}
		case Changes:
			switch(row)
			{
			case 0: return R.id.changes_0;
			case 1: return R.id.changes_1;
			case 2: return R.id.changes_2;
			case 3: return R.id.changes_3;
			case 4: return R.id.changes_4;
			}
		case Row:
			switch(row)
			{
			case 0: return R.id.row_0;
			case 1: return R.id.row_1;
			case 2: return R.id.row_2;
			case 3: return R.id.row_3;
			case 4: return R.id.row_4;
			}
		default:
			return -1;
		}
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		
		for(int widgetID : appWidgetIds)
		{
			//Anulowanie alarmu
			Intent clickIntent = new Intent();
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetID);
			clickIntent.setAction(ACTION_CONTROL);
			clickIntent.setData(Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/#scrollDown"), Integer.toString(widgetID)));
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).cancel(pendingIntent);
			
			//Usunięcie ustawień
			final SharedPreferences pref = context.getSharedPreferences("ConnectionWidget", Context.MODE_PRIVATE);
			String tID = Integer.toString(widgetID);
			
			Editor ed = pref.edit();
			
			for (String i : new String[]{"depName", "arrName", "viaName", "SID", "ZID", "VID1", "Products"/*, "Attributes"*/})
				if(pref.contains(i))
					ed.remove(i+tID);
		}
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		for (int widgetId : appWidgetIds)
			update(appWidgetManager, context, widgetId, false);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		if(intent != null)
			Log.w("RozkladPKP", intent.getAction());
		if(intent.getAction().equals(ACTION_CONTROL))
		{
			String frag = null;
			if(intent.getData() != null)
				frag = intent.getData().getFragment();
			if(frag == null)
				return;
			
			int _id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			if(_id == -1)
				return;
			String id = Integer.toString(_id);
			
			if(frag.equals("return"))
			{
				SharedPreferences pref = context.getSharedPreferences("ConnectionWidget", Context.MODE_PRIVATE);
				
				String sid = pref.getString("SID"+id,"");
				String zid = pref.getString("ZID"+id,"");
				
				pref.edit().putString("SID"+id, zid).putString("ZID"+id, sid).commit();
				update(AppWidgetManager.getInstance(context), context, _id, false);	
			}
			else if(frag.equals("scrollDown"))
			{
				SharedPreferences pref = context.getSharedPreferences("ConnectionWidget", Context.MODE_PRIVATE);
				pref.edit().putInt("ScrollPos"+id, pref.getInt("ScrollPos"+id, 0)+1).commit();
				update(AppWidgetManager.getInstance(context), context, _id, false);	
			}
			else if(frag.equals("reload"))
			{
				update(AppWidgetManager.getInstance(context), context, _id, true);	
			}
		}
	}
	static Spannable delayText(String time, int delay)
	{
		ForegroundColorSpan span;
		if(delay <= 0)
    		span = new ForegroundColorSpan(Color.rgb(73,194,98));
    	else if(delay <= 5)
    		span = new ForegroundColorSpan(Color.rgb(197,170,73));
    	else
    		span = new ForegroundColorSpan(Color.rgb(220, 59, 76));
    	
    	 
    	time += (delay >= 0) ? " +" : " ";
    	time += Integer.toString(delay);
    	
    	SpannableStringBuilder spanBuilder = new SpannableStringBuilder();
    	spanBuilder.clearSpans();
    	spanBuilder.clear();
    	spanBuilder.append(time);
    	spanBuilder.setSpan(span, 6, time.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    	
    	return spanBuilder;
	}
	
	static class CTSResult{
		public int arr_width,dep_width;
	}
	
	static private CTSResult calculateTextSizes(Context cx, PLN pln) {
		
		TextPaint tp = new TextPaint();
        tp.setTypeface(Typeface.DEFAULT_BOLD);
        tp.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 23, cx.getResources().getDisplayMetrics()));
        
        //Obliczenie wielkości pola tekstowego dla odjazdów
        int t = 0;
        for(int i = 0; i < pln.connectionCount(); ++i)
        {
       	 UnboundConnection c =  pln.connections[i];
       	 if(c.getChange() != null && Math.abs(c.getChange().departureDelay) > t)
       		 t = Math.abs(c.getChange().departureDelay);
        }
        
        float timew = tp.measureText("23:55 +"+Integer.toString(t));
        int dep_width = (int) (timew);
        
        //Obliczenie wielkości pola tekstowego dla przyjazdów
        t = -1000;
        for(int i = 0; i < pln.connectionCount(); ++i)
        {
       	 Train tr =  pln.connections[i].getTrain(pln.connections[i].trainCount-1);
       	 TrainChange c =  tr.getChange();
       	 
       	 if(c != null && c.realarrtime != null && c.realarrtime.difference(tr.arrtime).intValue() > t)
       		 t = c.realarrtime.difference(tr.arrtime).intValue();
       	 
        }
        int arr_width = -1;
        if(t != -1000)
        {
       	 timew = tp.measureText("23:55 +"+Integer.toString(t));
       	 arr_width = (int) (timew);
        }
        else
       	 arr_width = -1;
        
        CTSResult ret = new CTSResult();
        ret.arr_width = arr_width;
        ret.dep_width = dep_width;
        
        return ret;
	}
	
	public static void update(final AppWidgetManager appWidgetManager, final Context c, final int widgetID, boolean forceUpdate)
	{
		final String pkg = c.getPackageName();
		
		//Na szybko wyświetlamy ładowanie...
		appWidgetManager.updateAppWidget(widgetID, new RemoteViews(pkg, R.layout.connection_list_widget_loading));
		
		
		final SharedPreferences pref = c.getSharedPreferences("ConnectionWidget", Context.MODE_PRIVATE);
		final String id = Integer.toString(widgetID);
		if(!pref.contains("depName"+id))
			return;
		Log.d("RozkladPKP", "Wywołanie aktualizacji");
		Log.d("RozkladPKP", "Widżet "+id+"wyświetla połączenia: "+pref.getString("depName"+id, ":(") + " → "+ pref.getString("arrName"+id, ":("));
		
		final int scrollPos = pref.getInt("ScrollPos"+id, 0);
		
		ArrayList<SerializableNameValuePair> params = new ArrayList<SerializableNameValuePair>();
		
		for (String i : new String[]{"SID", "ZID", "VID1"/*, "Products", "Attributes"*/})
			if(pref.contains(i+id))
				params.add(new SerializableNameValuePair(i, pref.getString(i+id, "")));
		
		final String productString = pref.getString("Products"+id,"");
		params.add(new SerializableNameValuePair("REQ0JourneyProduct_prod_list_1", productString));
		
		Time t = new Time();
		t.setToNow();
		
		params.add(new SerializableNameValuePair("date",t.format("%d.%m.%Y")));
		params.add(new SerializableNameValuePair("time",t.format("%H:%M")));
		
		if(scrollPos >= 5)
			forceUpdate = true; //Gwarantuje co najmniej 5 połączeń TODO: zależne od wielkości widżeta
		
		ConnectionList clist = ConnectionList.forParameters(c, params, forceUpdate ? CachePolicy.NoCached : CachePolicy.CachedIfAvailable, 1);
		clist.addObserver(new Observer() {
			
			@Override
			public void update(Observable observable, Object data) {
			
				//To pole będzie zawierać prawdę, jeśli wywołanie jest pierwszym z dwóch.
				if(data != null && (Boolean)data)
					return;
				
				Log.w("RozkladPKP", "Wywołanie obserwatora listy");
				
				ConnectionList cl = (ConnectionList)observable;
				
				PLN pln = cl.getPLN();
				
				//Czy są opóźnienia?
				boolean delays = pln.hasDelayInfo();
				CTSResult dims = null;
				if(delays)
					dims = calculateTextSizes(c, pln);
				
				if(pln.days().totalConnectionCount() < 10 && cl.scrollable())
				{
					Log.w("RozkladPKP", "Mało..."+Integer.toString(pln.days().totalConnectionCount()));
					cl.fetchMore(false);
					return;
				}
				int sp2 = scrollPos;
				if(cl.scrollable())
				{
					Log.w("RozkladPKP", "Nowa lista");
					//Nowo pobrana lista, trzeba ustalić do jakiej pozycji przewinąć scroll
					//Pokazujemy tylko co najwyżej jedno połączenie z przeszłości 
					
					Time now = new Time();
					now.setToNow();
					sp2 = 0;
					
					for(Connection connection : pln.days().getConnectionIterator())
						if(Time.compare(connection.getDate(true), now) < 0)
							sp2++;
						else 
							break;
					
					if(sp2 > 0)
						sp2--;
					
					pref.edit().putInt("ScrollPos"+id, sp2).commit();
				}
				Log.w("RozkladPKP", "SP2: "+Integer.toString(sp2));
				cl.saveInCache(c, 1);
				RemoteViews views = new RemoteViews(pkg, R.layout.connection_list_widget);
				Time upalarm = null;
				int cn = 0;
				int rownum = 0;
				int depw = 0,arrw = 0;
				
				if(delays)
				{
					arrw = dims.arr_width;
					depw = dims.dep_width;
				}
				
				for(Connection connection : pln.days().getConnectionIterator())
				{
					if(cn++ < sp2) continue;
					if(rownum > 4) break;

					UnboundConnection con = connection.connection;

					if(cn == sp2+2)
					{
						upalarm = connection.getDate(true);
						Log.i("RozkladPKP", "Ustawię alarm na: "+upalarm.format2445());
					}


					if(delays)
					{
						views.setInt(getViewID(FieldType.DepartureTime, rownum), "setWidth", depw);
						if(arrw > 0)
							views.setInt(getViewID(FieldType.ArrivalTime, rownum), "setWidth", arrw);
					}

					if(con.getChange() != null && con.getChange().departureDelay != -1)
						views.setTextViewText(getViewID(FieldType.DepartureTime, rownum), delayText(con.getTrain(0).deptime.toString(),con.getChange().departureDelay));
					else
						views.setTextViewText(getViewID(FieldType.DepartureTime, rownum), con.getTrain(0).deptime.toString());

					Train last = con.getTrain(con.trainCount-1);

					if(last.getChange() != null && last.getChange().realarrtime != null)
						views.setTextViewText(getViewID(FieldType.ArrivalTime, rownum), delayText(last.arrtime.toString(),last.getChange().realarrtime.difference(last.arrtime).intValue()));
					else
						views.setTextViewText(getViewID(FieldType.ArrivalTime, rownum), last.arrtime.toString());


					views.setTextViewText(getViewID(FieldType.Duration, rownum), con.getJourneyTime().toString());
					views.setTextViewText(getViewID(FieldType.Changes, rownum), Integer.toString(con.changesCount));

					Intent intent = new Intent(c, ConnectionDetailsActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setData(Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/#connection"+Integer.toString(con.number)), Integer.toString(widgetID)));

					intent.putExtra("PLNData", cl.getPLN().data);
					intent.putExtra("ConnectionIndex", con.number);

					intent.putExtra("StartDate", connection.getDate(false).format("%d.%m.%Y"));
					//ni.putExtra("Attributes", extras.getSerializable("Attributes"));
					intent.putExtra("Products", productString);

					views.setOnClickPendingIntent(getViewID(FieldType.Row, rownum), PendingIntent.getActivity(c, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
					rownum++;
				}

				
				views.setTextViewText(R.id.arrival_station, pln.arrivalStation().name);
				views.setTextViewText(R.id.departure_station, pln.departureStation().name+ " →");
				
				Intent clickIntent = new Intent();
				clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetID);
				clickIntent.setAction(ACTION_CONTROL);
				clickIntent.setData(Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/#reload"), Integer.toString(widgetID)));
				PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				views.setOnClickPendingIntent(R.id.reload_button, pendingIntent);
				
				
				clickIntent = new Intent();
				clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetID);
				clickIntent.setAction(ACTION_CONTROL);
				clickIntent.setData(Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/#return"), Integer.toString(widgetID)));
				pendingIntent = PendingIntent.getBroadcast(c, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				views.setOnClickPendingIntent(R.id.return_button, pendingIntent);
				
				clickIntent = new Intent();
				clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetID);
				clickIntent.setAction(ACTION_CONTROL);
				clickIntent.setData(Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/#scrollDown"), Integer.toString(widgetID)));
				pendingIntent = PendingIntent.getBroadcast(c, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				Log.i("RozkladPKP", "Ustawiłem alarm na: "+upalarm.format2445());
				((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC, upalarm.toMillis(false), pendingIntent);
				
				appWidgetManager.updateAppWidget(widgetID, views);
			}
		});
	}

	
}
