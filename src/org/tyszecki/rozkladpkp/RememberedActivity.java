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

import org.tyszecki.rozkladpkp.RememberedItem.TimetableItem;
import org.tyszecki.rozkladpkp.RememberedItem.TimetableType;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class RememberedActivity extends Activity {
	RememberedItemAdapter adapter;
	boolean showForm;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remembered_list);
        
        ListView lv = (ListView)findViewById(R.id.remembered_list);
        
        adapter = new RememberedItemAdapter(this);
        lv.setAdapter(adapter);
        
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos, long id) {
				
				final RememberedItem b =  adapter.getItem(pos);
				Intent ni = null;
				
				if(b instanceof RememberedItem.TimetableItem)
				{
					TimetableItem t = (TimetableItem)b;
					
					boolean showSaved = (t.cacheValid != null && t.cacheValid.length() > 0);
					
					if(!showSaved && !CommonUtils.onlineCheck())
						return;
					
					ni = new Intent(arg0.getContext(), showForm ? TimetableFormActivity.class : TimetableActivity.class);
					
					if(showSaved)
						ni.putExtra("Filename", CommonUtils.ResultsHash(Integer.toString(t.SID), null, t.type == TimetableType.Departure));
					
					ni.putExtra("Station", t.name);
					ni.putExtra("SID",CommonUtils.SIDfromStationID(t.SID, t.name));
					
					ni.putExtra("Type", t.type == TimetableType.Arrival ? "arr" : "dep");
					
				}
				else if(b instanceof RememberedItem.RouteItem)
				{
					RememberedItem.RouteItem t = (RememberedItem.RouteItem)b;
					
					boolean showSaved = (t.cacheValid != null && t.cacheValid.length() > 0);
					
					if(!showSaved && !CommonUtils.onlineCheck())
						return;
						
					ni = new Intent(arg0.getContext(),(showForm && !showSaved) ? ConnectionsFormActivity.class : ConnectionListActivity.class);
					
					if(showSaved)
						ni.putExtra("PLNFilename", CommonUtils.ResultsHash(Integer.toString(t.SIDFrom), Integer.toString(t.SIDTo), null));
					
					ni.putExtra("ZID", CommonUtils.SIDfromStationID(t.SIDTo,t.toName));
					ni.putExtra("SID", CommonUtils.SIDfromStationID(t.SIDFrom,t.fromName));
					ni.putExtra("arrName", t.toName);
					ni.putExtra("depName", t.fromName);
					ni.putExtra("Attributes", new ArrayList<SerializableNameValuePair>());
					
				}
				if(ni != null)
				{
					ni.putExtra("Products", getPreferences(MODE_PRIVATE).getString("Products", "11110001111111"));
					Time time = new Time();
					time.setToNow();
					
					ni.putExtra("Time", time.format("%H:%M"));
					ni.putExtra("Date", time.format("%d.%m.%Y"));
					startActivity(ni);
				}
			}
		});
        
        registerForContextMenu(lv);
    }
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.remembered_list_context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		if(item.getItemId() == R.id.item_delete)
		{
			adapter.deleteItem(info.position);
			return true;
		}
		else
			return super.onContextItemSelected(item);
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		
		getMenuInflater().inflate(R.menu.remembered, menu);
		
		return true;
	}
	
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case R.id.item_settings:
			Intent ni = new Intent(getBaseContext(),PreferencesActivity.class);
			startActivity(ni);
			return true;
		}
		return false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		adapter.setAutoDelete(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("autoDeleteTables", true));
		
		adapter.reloadData();
		
		showForm = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("showFormFromRemembered", false);
	}
}
