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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.tyszecki.rozkladpkp.ConnectionDetailsItem.TrainItem;
import org.tyszecki.rozkladpkp.PLN.Connection;
import org.tyszecki.rozkladpkp.PLN.Train;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ConnectionDetailsActivity extends Activity {
	private ConnectionDetailsItemAdapter adapter;
	private ArrayList<ConnectionDetailsItem> items;
	private PLN pln;
	//private String startDate;
	private int conidx;
	private static byte[] sBuffer = new byte[512];
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_details);
        setTitle("Plan podróży");
        
        //startDate = getIntent().getExtras().getString("StartDate");
        pln = new PLN(getIntent().getExtras().getByteArray("PLNData"));
        conidx = getIntent().getExtras().getInt("ConnectionIndex");
        
        items = new ArrayList<ConnectionDetailsItem>();
        adapter = new ConnectionDetailsItemAdapter(this,  items);
            
        ListView lv = (ListView)findViewById(R.id.connection_details);
        lv.setAdapter(this.adapter);
        
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
				
				Intent ni = new Intent(arg0.getContext(),TrainDetailsActivity.class);
				
				ni.putExtra("PLNData",pln.data);
				ni.putExtra("ConnectionIndex",conidx);
				ni.putExtra("TrainIndex", pos);
				ni.putExtra("StartDate",getIntent().getExtras().getString("StartDate"));
				startActivity(ni);
			}
		});
        
        loadData();
	}
	
	private void loadData()
	{
		items.clear();
    	
    	ConnectionDetailsItem c = new ConnectionDetailsItem();
    	Connection con = pln.connections[conidx];
    	
    	
    	for(int i = 0; i < con.trains.length; ++i)
    	{
    		Train t = con.trains[i];
    		Log.i("RozkladPKP", t.arrstation.name);
    		TrainItem ti = c.new TrainItem();
    		ti.t = t;
    		items.add(ti);
    	}
    	
    	
    	adapter.notifyDataSetChanged();
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.connection_details, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected (MenuItem item){
		
		final Bundle b = getIntent().getExtras();
		
		switch(item.getItemId()){
		case R.id.item_price:
			
			new Runnable(){
	            @Override
	            public void run() {
	                try {
						
	                	ArrayList<SerializableNameValuePair> data = new ArrayList<SerializableNameValuePair>();
	                	data.add(new SerializableNameValuePair("ident",pln.id()));
	                	data.add(new SerializableNameValuePair("seqnr",Integer.toString(b.getInt("seqnr"))));
	                	data.add(new SerializableNameValuePair("tnumber",Integer.toString(b.getInt("ConnectionIndex"))));
	                	
	                	DefaultHttpClient client = new DefaultHttpClient();
	            		
	            		HttpGet request = new HttpGet("http://kalesonybogawojny.ath.cx/test/test.py?seqnr="+Integer.toString(b.getInt("seqnr")+1)+"&ident="+pln.id()+"&tnumber="+Integer.toString(b.getInt("ConnectionIndex")));
	            		client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
	                    client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
	                    //request.addHeader("Content-Type", "text/plain");
	                    //request.setEntity(new UrlEncodedFormEntity(data,"UTF-8"));
	                    
	                    
	                    HttpResponse response = client.execute(request);
	                     
	                    // Pull content stream from response
	                    HttpEntity entity = response.getEntity();
	                    InputStream inputStream = entity.getContent();
	                    final ByteArrayOutputStream content = new ByteArrayOutputStream();
	                    
	                    int readBytes = 0;
	                    while ((readBytes = inputStream.read(sBuffer)) != -1) {
	                        content.write(sBuffer, 0, readBytes);
	                    }
	                    
	                    runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(ConnectionDetailsActivity.this, content.toString(), Toast.LENGTH_SHORT).show();
							}
						});
	                	
					} catch (Exception e) {
						e.printStackTrace();
					}
	            }
			}.run();
			
			return true;
		}
		return false;
	}
}
