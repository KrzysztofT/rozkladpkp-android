package org.tyszecki.rozkladpkp;

import java.util.ArrayList;

import org.tyszecki.rozkladpkp.RememberedItem.TimetableItem;
import org.tyszecki.rozkladpkp.RememberedItem.TimetableType;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class RememberedActivity extends Activity {
	RememberedItemAdapter adapter;
	
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
				Intent ni;
				
				if(b instanceof RememberedItem.TimetableItem)
				{
					TimetableItem t = (TimetableItem)b;
					
					ni = new Intent(arg0.getContext(),TimetableActivity.class);
					
					ni.putExtra("Station", t.name);
					ni.putExtra("SID",CommonUtils.SIDfromStationID(t.SID, t.name));
					
					ni.putExtra("Type", t.type == TimetableType.Arrival ? "arr" : "dep");
					
				}
				else
				{
					RememberedItem.RouteItem t = (RememberedItem.RouteItem)b;
					ni = new Intent(arg0.getContext(),ConnectionListActivity.class);
					
					ni.putExtra("ZID", CommonUtils.SIDfromStationID(t.SIDTo,t.toName));
					ni.putExtra("SID", CommonUtils.SIDfromStationID(t.SIDFrom,t.fromName));
					ni.putExtra("arrName", t.toName);
					ni.putExtra("depName", t.fromName);
					ni.putExtra("Attributes", new ArrayList<SerializableNameValuePair>());
					
				}
				
				ni.putExtra("Products", getPreferences(MODE_PRIVATE).getString("Products", "11110001111111"));
				Time time = new Time();
				time.setToNow();
				
				ni.putExtra("Time", time.format("%H:%M"));
				ni.putExtra("Date", time.format("%d.%m.%Y"));
				startActivity(ni);
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
	
	@Override
	protected void onResume() {
		super.onResume();
		adapter.reloadData();
		
	}
}
