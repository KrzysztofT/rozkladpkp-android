package org.tyszecki.rozkladpkp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.tyszecki.rozkladpkp.ConnectionItem.ScrollItem;
import org.tyszecki.rozkladpkp.ConnectionItem.TripItem;
import org.tyszecki.rozkladpkp.PLN.Station;
import org.tyszecki.rozkladpkp.PLN.Trip;
import org.tyszecki.rozkladpkp.PLN.TripIterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import org.tyszecki.rozkladpkp.R;

public class ConnectionsActivity extends Activity {
	
	private Runnable viewConn;
	private ProgressDialog m_ProgressDialog;
	private static byte[] sBuffer = new byte[512];
	private byte[] plndata;
	private int seqnr = 0;
	PLN pln;
	private ArrayList<ConnectionItem> items;
	private ConnectionItemAdapter adapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.connections);
        
        items = new ArrayList<ConnectionItem>();
        adapter = new ConnectionItemAdapter(this,  items);
            
        ListView lv = (ListView)findViewById(R.id.connview);
        lv.setAdapter(this.adapter);
        
        setTitle("Połączenia");
        
        if(savedInstanceState != null && savedInstanceState.containsKey("PLNData")){
        	pln = new PLN(savedInstanceState.getByteArray("PLNData"));
        	seqnr = savedInstanceState.getInt("SeqNr");
        	runOnUiThread(loadData);
        }
        else
        {
	        viewConn = new Runnable(){
	            @Override
	            public void run() {
	                try {
						getConnections();
					} catch (Exception e) {
						e.printStackTrace();
					}
	            }
	        };
	        
	        Thread thread =  new Thread(null, viewConn, "MagentoBackground");
	        thread.start();
	        m_ProgressDialog = ProgressDialog.show(ConnectionsActivity.this,    
	              "Czekaj...", "Pobieranie rozkładu...", true);
        }
        
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos, long id) {
				
				final ConnectionItem b =  items.get(pos);
				
				if(b instanceof TripItem){
					Intent ni = new Intent(arg0.getContext(),ConnectionInfoActivity.class);
					
					ni.putExtra("PLNData",plndata);
					ni.putExtra("ConnectionIndex",((TripItem)b).t.conidx);
					ni.putExtra("StartDate",((TripItem)b).t.date);
					
					startActivity(ni);
				}
				else if(b instanceof ScrollItem)
				{
					//Pobierz wcześniejsze/póxniejsze
					 viewConn = new Runnable(){
				            @Override
				            public void run() {
				                try {
									getMore(((ScrollItem)b).up);
								} catch (Exception e) {
									e.printStackTrace();
								}
				            }
				        };
				        
				        Thread thread =  new Thread(null, viewConn, "MagentoBackground");
				        thread.start();
				        m_ProgressDialog = ProgressDialog.show(ConnectionsActivity.this,    
				              "Czekaj...", "Pobieranie rozkładu...", true);
				}
				
			}
		});
	}
	
	private Runnable loadData = new Runnable() {

        @Override
        public void run() {
        	items.clear();
        	Station dep = pln.departureStation();
        	Station arr = pln.arrivalStation();
        	
        	if(arr != null && dep != null)
        		setTitle(dep.name+" - "+arr.name);
        	
        	
        	ConnectionItem c = new ConnectionItem();
        	String lastDate = "";
        	TripIterator it = pln.tripIterator();
        	
        	if(!it.hasNext())
        		noConnectionsAlert();
        	else
        	{
        		items.add(c.new ScrollItem(true));
	        	int i = 0;
	        	while(it.hasNext())
	        	{
	        		Trip t = it.next();
	        		if(!t.date.equals(lastDate))
	        		{
	        			ConnectionItem.DateItem d = c.new DateItem();
	        			d.date = t.date;
	        			items.add(d);
	        			lastDate = t.date;
	        		}
	        		
	        		TripItem ti = c.new TripItem();
	        		ti.t = t;
	        		items.add(ti);
	        		i++;
	        	}
	        	items.add(c.new ScrollItem(false));	
        	}
        	
        	adapter.notifyDataSetChanged();
        	
        	if(m_ProgressDialog != null)
        		m_ProgressDialog.dismiss();	
        }
      };
      
	protected void noConnectionsAlert() {
		//Pokazuje okno dialogowe informujące o braku połączeń i umożliwia powrót do wcześniejszej aktywności.
		AlertDialog alertDialog;
    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Brak połączeń!");
    	alertDialog.setMessage("Nie istnieje połączenie między wybranymi stacjami.");
    	alertDialog.setCancelable(false);
    	
    	alertDialog.setButton("Powrót", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				ConnectionsActivity.this.finish();
			}
		});
    	alertDialog.show();
	}
      
	
	protected void getConnections() throws Exception {
		
		String SID = getIntent().getExtras().getString("SID");
		String ZID = getIntent().getExtras().getString("ZID");
		
		SID = SID.replaceAll("=", "%3D");
		SID = SID.replaceAll(" ", "%20");
		ZID = ZID.replaceAll("=", "%3D");
		ZID = ZID.replaceAll(" ", "%20");
		
		String time = getIntent().getExtras().getString("Time");
		String date = getIntent().getExtras().getString("Date");
		String prod = getIntent().getExtras().getString("Products");
		
		
		String data = "ignoreMinuteRound=yes&REQ0JourneyProduct_prod_list_1="+prod+"&date="+date+"&ZID="+ZID+"&h2g-direct=1&time="+time+"&SID="+SID+"@&start=1";
    	String url  = "http://rozklad.sitkol.pl/bin/query.exe/pn" ;
    	
    	
		PLNRequest(url, data);
	}
	
	public void getMore(boolean earlier) throws Exception
	{
		String dir = earlier ? "2" : "1";
		seqnr++;
		String data = "seqnr="+Integer.toString(seqnr)+"&h2g-direct=1&ident="+pln.id()+"&REQ0HafasScrollDir="+dir+"&hcount=1&ignoreMinuteRound=yes&androidversion=1.1.4";
    	String url  = "http://rozklad.sitkol.pl/bin/query.exe/pn" ;
    	
		PLNRequest(url,data);
	}
	
	private void PLNRequest(String url, String data) throws Exception
	{
		DefaultHttpClient client = new DefaultHttpClient();
		
		HttpPost request = new HttpPost(url);
		client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
        client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
        request.addHeader("Content-Type", "text/plain");
        request.setEntity(new StringEntity(data));
        
        HttpResponse response = client.execute(request);
         
        // Pull content stream from response
        HttpEntity entity = response.getEntity();
        InputStream inputStream = entity.getContent();
        GZIPInputStream in = new GZIPInputStream(inputStream);
        ByteArrayOutputStream content = new ByteArrayOutputStream();

        // Read response into a buffered stream
        int readBytes = 0;
        while ((readBytes = in.read(sBuffer)) != -1) {
            content.write(sBuffer, 0, readBytes);
        }

        // Return result from buffered stream
        plndata = content.toByteArray();
        Log.i("RozkladPKP", "jestPLN");
        pln = new PLN(plndata);
        
        Log.i("RozkladPKP", "pln parsed");
		runOnUiThread(loadData);
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.connections, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case R.id.savepln:
			File f = new File(Environment.getExternalStorageDirectory(),"PLN");
			FileOutputStream w = null;
			try {
				w = new FileOutputStream(f);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				w.write(pln.data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle state){
		super.onSaveInstanceState(state);
		
		state.putByteArray("PLNData", pln.data);
		state.putInt("SeqNr", seqnr);
	}
	
	
}
