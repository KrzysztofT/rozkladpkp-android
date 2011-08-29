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
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.tyszecki.rozkladpkp.CommonUtils.StationIDfromNameProgress;
import org.tyszecki.rozkladpkp.TimetableItem.DateItem;
import org.tyszecki.rozkladpkp.TimetableItem.TrainItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class TimetableActivity extends Activity {
	
	
	private ArrayList<TimetableItem> m_items = null;
	private TimetableItemAdapter m_adapter;
	private Runnable viewBoard;
	private static byte[] sBuffer = new byte[512];
	private String SID;
	private boolean dep, inFront = true, showNDDialog = false;
	NodeList destList = null;
	TimetableItem item;
	String startID = null,destID = null, xmlstring;
	
	TrainItem titem;
	Thread loadingThread;
	private ProgressDialog m_ProgressDialog;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable);
        
        SID = getIntent().getExtras().getString("SID");
        
        //Log.i("RozkladPKP",SID);
        dep = getIntent().getExtras().getString("Type").equals("dep");
        
        RememberedManager.addtoHistory(this, CommonUtils.StationIDfromSID(SID), dep);
        
        setTitle((dep?"Odjazdy z ":"Przyjazdy do ")+getIntent().getExtras().getString("Station"));
        
        m_items = new ArrayList<TimetableItem>();
        
        m_adapter = new TimetableItemAdapter(this, m_items);
        m_adapter.setType(dep);
        
        ListView lv = (ListView)findViewById(R.id.timetable);
        lv.setAdapter(this.m_adapter);
        
        viewBoard = new Runnable(){
            @Override
            public void run() {
                getBoard();
            }
        };
        
        loadingThread =  new Thread(null, viewBoard, "MagentoBackground");
        loadingThread.start();
        m_ProgressDialog = ProgressDialog.show(TimetableActivity.this,    
              "Czekaj...", "Pobieranie rozkładu...", true, true, new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					loadingThread.interrupt();
					TimetableActivity.this.finish();
				}
			});
        
        //Włączanie informacji o pociągu - potrzebnego do tego są identyfikatory stacji.
        //Ponieważ nie rozpracowałem jeszcze formatu PLN, używana jest wyszukiwarka.
        lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> arg0, View arg1, int pos,
					long id) {
				
				item = m_items.get(pos);
				if(item instanceof TimetableItem.DateItem)
					return;
				
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
		});
	}
	
	
	private void getBoard(){
		try{
			DefaultHttpClient client = new DefaultHttpClient();

			String s = SID.replaceAll("=", "%3D");
			s = s.replaceAll(" ", "%20");
			String time = getIntent().getExtras().getString("Time");
			String date = getIntent().getExtras().getString("Date");
			String prod = getIntent().getExtras().getString("Products");
			String type = dep?"dep":"arr";		

			String data = "L=vs_java3&productsFilter="+prod+"&inputTripelId="+s+"@&maxJourneys=50&boardType="+type+"&time="+time+"&date="+date+"&start=yes";
			String url  = "http://rozklad.sitkol.pl/bin/stboard.exe/pn" ;

			HttpPost request = new HttpPost(url);
			client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
			client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
			request.addHeader("Content-Type", "text/plain");
			request.setEntity(new StringEntity(data));

			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();
			ByteArrayOutputStream content = new ByteArrayOutputStream();


			int readBytes = 0;
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}


			xmlstring = "<a>"+new String(content.toByteArray())+"</a>";
		} catch (Exception e) { 

		}
		runOnUiThread(returnRes);
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
	
	private Runnable returnRes = new Runnable() {

        @Override
        public void run() {
        	m_adapter.setXML(xmlstring);
            if(m_items != null && m_items.size() == 0)
            {
            	if(inFront)
            		noDataAlert();
            	else
            		showNDDialog = true;
            }
            m_ProgressDialog.dismiss();
        }
	};
	
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.timetable, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case R.id.item_favourite:
			RememberedManager.saveStation(this, CommonUtils.StationIDfromSID(SID), dep);
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

