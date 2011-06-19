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
import org.tyszecki.rozkladpkp.PLN.Train;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class TrainDetailsActivity extends Activity {
	
	private ProgressDialog progressDialog = null; 
	private RouteItemAdapter adapter;
	private Runnable viewTable;
	
	Train t;
	
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
        
        t = pln.connections[conidx].trains[trainidx];
        
        TextView tv = (TextView) v.findViewById(R.id.header);
          
        StringBuilder b = new StringBuilder();
        b.append("Pociąg ");
        b.append(t.number);
        b.append("\n");
        
        for(String s : t.attributes)
        	b.append("-"+s+"\n");
        tv.setText(b.toString());
        lv.addHeaderView(v);
        
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
        
        (new Thread(null, viewTable)).start();
        
        progressDialog = ProgressDialog.show(this,    
              "Czekaj...", "Pobieranie trasy...", true);
	}
	
	protected void getTable() throws ClientProtocolException, IOException, ParserConfigurationException, SAXException {
		
		Bundle extras = getIntent().getExtras();
		final int stId = t.depstation.id;
		
    	final Document doc = RouteFetcher.fetchRoute(t.number, Integer.toString(t.depstation.id), "",
    			extras.getString("StartDate"), t.deptime.toString(), "dep");
    	
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				adapter.setData(doc,stId,t.arrstation.id);
				progressDialog.dismiss();
			}
		});
	}
}
