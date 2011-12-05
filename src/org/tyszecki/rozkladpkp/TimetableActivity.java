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

import org.tyszecki.rozkladpkp.CommonUtils.StationIDfromNameProgress;
import org.tyszecki.rozkladpkp.TimetableItem.ScrollItem;
import org.tyszecki.rozkladpkp.TimetableItem.TrainItem;
import org.tyszecki.rozkladpkp.TimetableItem.WarningItem;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class TimetableActivity extends FragmentActivity {

	private TimetableItemAdapter m_adapter;

	private String SID;
	private boolean dep, inFront = true, showNDDialog = false;
	NodeList destList = null;
	TimetableItem item;
	String startID = null,destID = null, xmlstring;
	
	TrainItem titem;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable);
        Bundle extras = getIntent().getExtras();
        
        SID = extras.getString("SID");
        dep = extras.getString("Type").equals("dep");
        
        String stationID =  CommonUtils.StationIDfromSID(SID);
        
        RememberedManager.addtoHistory(this, stationID, dep, null);
        
        getSupportActionBar().setTitle(extras.getString("Station"));
        getSupportActionBar().setSubtitle(dep?"Odjazdy":"Przyjazdy");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
     
        if(extras.containsKey("Filename"))
        	m_adapter = new TimetableItemAdapter(stationID, extras.getString("Products"), 
            		CommonUtils.timeFromString(new Time(), extras.getString("Date"), extras.getString("Time")), !dep, this, extras.getString("Filename"));
        	
        else
        {
	        m_adapter = new TimetableItemAdapter(stationID, extras.getString("Products"), 
	        		CommonUtils.timeFromString(new Time(), extras.getString("Date"), extras.getString("Time")), !dep, this);
	        m_adapter.fetch();
        }
        		
        
        ListView lv = (ListView)findViewById(R.id.timetable);
        lv.setAdapter(this.m_adapter);
        
        
        //Włączanie informacji o pociągu - potrzebnego do tego są identyfikatory stacji.
        //Ponieważ nie rozpracowałem jeszcze formatu PLN, używana jest wyszukiwarka.
        lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> arg0, View arg1, int pos,
					long id) {
				
				item = m_adapter.getItem(pos);
				if(item instanceof TimetableItem.DateItem)
					return;
				
				else if(item instanceof ScrollItem)
					m_adapter.fetchMore(!((ScrollItem)item).up);
				
				else if(item instanceof WarningItem)
				{
					Intent ni = new Intent(TimetableActivity.this, TimetableFormActivity.class);
					ni.putExtra("Station",getIntent().getExtras().getString("Station"));
					startActivity(ni);
				}
				
				else
				{
					titem = (TrainItem)item;
					
					startID = CommonUtils.StationIDfromSID(SID);
					try {
						CommonUtils.StationIDfromName(titem.station, new StationIDfromNameProgress() {
							
							ProgressDialog dialog = null;
							
							@Override
							public void finished(final String ID) {
								
								runOnUiThread(new Runnable(){
									@Override
									public void run() {
										if(dialog != null)
											dialog.dismiss();
										if(ID != null)
										{
											Intent ni = new Intent(arg0.getContext(),RouteActivity.class);
											ni.putExtra("startID",startID);
											ni.putExtra("destID",ID);
											ni.putExtra("number",titem.number);
											ni.putExtra("date", titem.date);
											ni.putExtra("time", titem.time);
											ni.putExtra("Type", dep?"dep":"arr");
											
											startActivity(ni);
										}
										else
										{
											//Blad
											Log.e("RozkladPKP","Nie mozna pobrac identyfikatora stacji");
										}
									}
								});
							}
							
							@Override
							public void downloadStarted() {
								dialog = ProgressDialog.show(TimetableActivity.this,"Czekaj...", "Wyszukiwanie stacji...", true);
							}
						});
					} catch (Exception e) {}
				}
			}
		});
	}
	
	
	
	
	private void noDataAlert()
	{
		AlertDialog alertDialog;
    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Brak połączeń!");
    	if(dep)
    		alertDialog.setMessage("W wybranym terminie nie odjeżdżają ze stacji żadne pociągi.");
    	else
    		alertDialog.setMessage("W wybranym terminie nie przyjeżdżają do stacji żadne pociągi.");
    	alertDialog.setCancelable(false);
    	alertDialog.setOnKeyListener(CommonUtils.getOnlyDPadListener());
    	
    	alertDialog.setButton("Powrót", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				TimetableActivity.this.finish();
			}
		});
    	alertDialog.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(android.support.v4.view.Menu menu) {
		getMenuInflater().inflate(R.menu.timetable, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(android.support.v4.view.MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
		    return true;
		case R.id.item_favourite:
			RememberedManager.saveStation(this, CommonUtils.StationIDfromSID(SID), dep);
			return true;
		case R.id.item_taxity:
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.taxity")));
			return true;
		}
		return false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		inFront = true;
		if(showNDDialog)
		{
			noDataAlert();
			showNDDialog = false;
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		inFront = false;
	}
}

