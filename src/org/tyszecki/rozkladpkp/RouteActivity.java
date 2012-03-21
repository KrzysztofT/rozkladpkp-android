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

import org.tyszecki.rozkladpkp.R;
import org.tyszecki.rozkladpkp.RouteFetcher.RouteParams;
import org.w3c.dom.Document;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class RouteActivity extends FragmentActivity {
	
	private RouteItemAdapter adapter;
	private RouteTask task = null;
	
	public void onCreate(Bundle savedInstanceState) {
		setTheme(RozkladPKPApplication.getThemeId());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route);
        
        final Bundle extras = getIntent().getExtras();
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
		adapter = new RouteItemAdapter(this);
        ListView lv = ((ListView)findViewById(R.id.route));
        lv.setAdapter(this.adapter);
        
        getRoute(extras,false);
        
        lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View v, int pos, long id) {
				RouteItem it = adapter.getItem(pos);

				if(it == null && pos == 0)
					getRoute(extras, true);
				else
				{
					if(it.station == null)
						return;

					Intent ni = new Intent(arg0.getContext(), TimetableFormActivity.class);
					ni.putExtra("Station", it.station);

					String time = null;
					while(pos >= 0)
					{
						if(it.arr != null && it.arr != null)
						{
							time = it.arr;
							break;
						}
						if(it.dep != null && it.dep != null)
						{
							time = it.dep;
							break;
						}
						--pos;
						it = adapter.getItem(pos);
					}
					if(time != null)
						ni.putExtra("PLNTimestamp", time);

					startActivity(ni);
				}
			}
		});
	}

	private void getRoute(Bundle extras, boolean download) {
		
		RouteParams params = new RouteParams();
        params.date = extras.getString("date");
        params.deptime = extras.getString("time");
        params.departure = extras.getString("startID");
        params.arrival = extras.getString("destID");
        params.train_number = extras.getString("number");
        params.type = extras.getString("Type");
        params.force_download = download;
        
        task = new RouteTask();
        task.execute(params);
	}
	
	private class RouteTask extends RouteFetcher{
		ProgressDialog progress = null;
		
		
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			if(isCancelled())
				return;
			
			progress = ProgressDialog.show(RouteActivity.this,    
					"Czekaj...", "Pobieranie trasy...", true, true, new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							cancel(true);
							if(progress != null)
								progress.dismiss();
							RouteActivity.this.finish();
						}
					});
		}
		
		@Override
		protected void onPostExecute(Document result) {
			super.onPostExecute(result);
			if(progress != null)
				progress.dismiss();
			if(result != null)
			{
				Bundle extras = RouteActivity.this.getIntent().getExtras();
				adapter.setData(result,Integer.parseInt(extras.getString("startID")),Integer.parseInt(extras.getString("destID")),isCached());
			}
			else
				CommonUtils.onlineCheck("Nie można pobrać trasy, brak połączenia internetowego.");
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(task != null)
			task.cancel(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case android.R.id.home:
            // app icon in action bar clicked; go home
            /*Intent intent = new Intent(this, RozkladPKP.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);*/
        	finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
    }
	}
}
