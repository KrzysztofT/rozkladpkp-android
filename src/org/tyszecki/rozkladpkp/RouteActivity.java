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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.widget.ListView;

public class RouteActivity extends Activity {
	
	private ProgressDialog progressDialog = null; 
	private RouteItemAdapter adapter;
	private Runnable viewTable;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route);
        
        viewTable = new Runnable(){
            @Override
            public void run() {
                try {
                	getTable();
				} catch (Exception e) {
					e.printStackTrace();
				} 
            }
        };
        
        adapter = new RouteItemAdapter(this);
        ((ListView)findViewById(R.id.route)).setAdapter(this.adapter);
        
        final Thread th = new Thread(null, viewTable);
        th.start();
        
        progressDialog = ProgressDialog.show(RouteActivity.this,    
              "Czekaj...", "Pobieranie rozkładu...", true, true, new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					th.interrupt();
					progressDialog.dismiss();
					RouteActivity.this.finish();
				}
			});
	}

	protected void getTable() throws ClientProtocolException, IOException, ParserConfigurationException, SAXException {
		
		Bundle extras = getIntent().getExtras();
		final String sID = extras.getString("startID");
		final String dID = extras.getString("destID");
		
    	final Document doc = RouteFetcher.fetchRoute(extras.getString("number"), sID, dID,
    			extras.getString("date"), extras.getString("time"), extras.getString("Type"));
    	
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				adapter.setData(doc,Integer.parseInt(sID),Integer.parseInt(dID));
				progressDialog.dismiss();
			}
		});
	}
	
	
}
