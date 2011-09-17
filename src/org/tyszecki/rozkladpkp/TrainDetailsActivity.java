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

import org.tyszecki.rozkladpkp.PLN.Train;
import org.tyszecki.rozkladpkp.RouteFetcher.RouteParams;
import org.w3c.dom.Document;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TrainDetailsActivity extends Activity {
	
	private ProgressDialog progressDialog = null; 
	private RouteItemAdapter adapter;
	
	Train t;
	RouteTask task;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.train_details);
        
        View v = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.train_details_header, null);
        ListView lv = (ListView)findViewById(R.id.route);
        
        
        setTitle("Informacje o pociągu");
        
        //startDate = getIntent().getExtras().getString("StartDate");
        PLN pln = new PLN(getIntent().getExtras().getByteArray("PLNData"));
        int conidx = getIntent().getExtras().getInt("ConnectionIndex");
        int trainidx = getIntent().getExtras().getInt("TrainIndex");
        
        t = pln.connections[conidx].getTrain(trainidx);
        
        TextView tv = (TextView) v.findViewById(R.id.header);
          
        StringBuilder b = new StringBuilder();
        b.append("Pociąg ");
        b.append(t.number);
        b.append("\n");
        
        for(int i = 0; i < t.getAttributeCount(); i++)
        	b.append("-"+t.getAttribute(i)+"\n");
        tv.setText(b.toString());
        lv.addHeaderView(v);
        
 
        adapter = new RouteItemAdapter(this);
        ((ListView)findViewById(R.id.route)).setAdapter(this.adapter);
        
        
        getRoute(false);
        
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos, long id) {
				if(pos > 0)
				{
					--pos;
					RouteItem it = adapter.getItem(pos);
					
					if(it == null && pos == 0)
						getRoute(true);
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
							ni.putExtra("Time", time);
						
						startActivity(ni);
					}
				}
			}
		});
	}

	private void getRoute(boolean download) 
	{
		RouteParams params = new RouteParams();
        params.date = getIntent().getExtras().getString("StartDate");
        params.deptime = t.deptime.toString();
        params.departure = Integer.toString(t.depstation.id);
        params.arrival = Integer.toString(t.arrstation.id);
        params.arrtime = t.arrtime.toString();
        params.force_download = download;
        
        params.train_number = t.number;
        
        
        task = new RouteTask();
        task.execute(params);
	}
	
	private class RouteTask extends RouteFetcher{
		ProgressDialog progress = null;
		
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			
			progress = ProgressDialog.show(TrainDetailsActivity.this,    
					"Czekaj...", "Pobieranie trasy...", true, true, new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							cancel(true);
							if(progress != null)
								progress.dismiss();
						}
					});
		}
		
		@Override
		protected void onPostExecute(Document result) {
			super.onPostExecute(result);
			if(progress != null)
				progress.dismiss();
			if(result != null)
				adapter.setData(result,t.depstation.id,t.arrstation.id,isCached());
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
}
