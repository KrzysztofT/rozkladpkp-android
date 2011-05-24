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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.tyszecki.rozkladpkp.ConnectionListItem.ScrollItem;
import org.tyszecki.rozkladpkp.ConnectionListItem.TripItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ConnectionListActivity extends Activity {
	
	private ProgressDialog m_ProgressDialog;
	private Runnable viewConn;
	private static byte[] sBuffer = new byte[512];
	private byte[] plndata;
	private int seqnr = 0;
	private final int TIMETABLE_DOWNLOAD_INTERVAL = 5000;
	
	private boolean hasFullTable = false;
	private String timetableUrl = null;
	private ArrayList<SerializableNameValuePair> commonFieldsList;
	private Thread loadingThread;
	
	PLN pln;
	
	private ConnectionListItemAdapter adapter;
	
	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.connection_list);
        
        adapter = new ConnectionListItemAdapter(this);
        ListView lv = (ListView)findViewById(R.id.connection_list);
        lv.setAdapter(this.adapter);
        
        Bundle extras = getIntent().getExtras();
        setTitle("Połączenia "+extras.getString("depName")+" - "+extras.getString("arrName"));
        
        //Pola wykorzystywane przy wszystkich żądaniach
        
        commonFieldsList = (ArrayList<SerializableNameValuePair>) extras.getSerializable("Attributes");
        commonFieldsList.add(new SerializableNameValuePair("SID", extras.getString("SID")));
        commonFieldsList.add(new SerializableNameValuePair("ZID", extras.getString("ZID")));
        commonFieldsList.add(new SerializableNameValuePair("REQ0JourneyProduct_prod_list_1",extras.getString("Products")));
        commonFieldsList.add(new SerializableNameValuePair("start", "1"));
        
        if(savedInstanceState != null && savedInstanceState.containsKey("PLNData")){
        	pln = new PLN(savedInstanceState.getByteArray("PLNData"));
        	plndata = pln.data;
        	seqnr = savedInstanceState.getInt("SeqNr");
        	hasFullTable = savedInstanceState.getBoolean("hasFullTable");
        	timetableUrl = savedInstanceState.getString("timetableURL");
        	
        	updateDisplayedPLN();
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
	        new Thread(null, viewConn, "MagentoBackground").start();
	        showLoader();
        }
        
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos, long id) {
				
				final ConnectionListItem b =  adapter.getItem(pos);
				
				if(b instanceof TripItem){
					Intent ni = new Intent(arg0.getContext(),ConnectionDetailsActivity.class);
					
					ni.putExtra("PLNData",plndata);
					ni.putExtra("ConnectionIndex",((TripItem)b).t.conidx);
					ni.putExtra("StartDate",((TripItem)b).t.date);
					
					startActivity(ni);
				}
				else if(b instanceof ScrollItem)
				{
					if(hasFullTable)
						adapter.loadMore();
					else
					{
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
						//Pobierz wcześniejsze/pózniejsze
						loadingThread = new Thread(null, viewConn, "MagentoBackground");
						loadingThread.start();
				        showLoader();
					}
				}
				
			}
		});
	}

	public void showLoader()
	{
		Runnable uit = new Runnable(){
			@Override
			public void run() {
				m_ProgressDialog = ProgressDialog.show(ConnectionListActivity.this,    
	              "Czekaj...", "Pobieranie rozkładu...", true);
			}
		};
		runOnUiThread(uit);
	}
	
	public void showFullLoader()
	{
		Runnable uit = new Runnable(){
			@Override
			public void run() {
				m_ProgressDialog = ProgressDialog.show(ConnectionListActivity.this,    
	              "Czekaj...", "Pobieranie pełnego rozkładu. Może to zająć więcej czasu...", true, true, new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface arg0) {
						loadingThread.interrupt();
						ConnectionListActivity.this.finish();
					}
				});
			}
		};
		runOnUiThread(uit);
	}
	
	public void hideLoader()
	{
		if(m_ProgressDialog != null)
		{				
    		m_ProgressDialog.dismiss();
    		m_ProgressDialog = null;
		}
	}
	public void updateDisplayedPLN()
	{
		Runnable uit = new Runnable() {
			@Override
			public void run() {
				//Pozwolenie na zmianę orientacji
				if(seqnr == 0 && pln.conCnt == 0)
				{
					hideLoader();
					noConnectionsAlert();
				}
				else
				{
					adapter.setPLN(pln, !hasFullTable);
					hideLoader();
				}
			}
		};
		runOnUiThread(uit);
	}
      
	protected void noConnectionsAlert() {
		//Pokazuje okno dialogowe informujące o braku połączeń i umożliwia powrót do wcześniejszej aktywności.
		AlertDialog alertDialog;
    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Brak połączeń!");
    	alertDialog.setMessage("Nie istnieje połączenie między wybranymi stacjami.");
    	alertDialog.setCancelable(false);
    	
    	alertDialog.setButton("Powrót", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				ConnectionListActivity.this.finish();
			}
		});
    	alertDialog.show();
	}
      
	
	protected void getConnections() throws Exception {
		
		if(!CommonUtils.onlineCheck(getBaseContext()))
			return;
		
		if(timetableUrl == null)
			getFullTimetableUrl();
		
		ArrayList<SerializableNameValuePair> data = new ArrayList<SerializableNameValuePair>();
		data.addAll(commonFieldsList);
		
		data.add(new SerializableNameValuePair("ignoreMinuteRound", "yes"));
		data.add(new SerializableNameValuePair("date", getIntent().getExtras().getString("Date")));
		data.add(new SerializableNameValuePair("time", getIntent().getExtras().getString("Time")));
		data.add(new SerializableNameValuePair("h2g-direct", "1"));
		
    	String url  = "http://rozklad.sitkol.pl/bin/query.exe/pn" ;
    	
		PLNRequest(url, data);
	}
	
	protected void getFullTimetableUrl() throws Exception {
		
		ArrayList<SerializableNameValuePair> data = new ArrayList<SerializableNameValuePair>();
		data.addAll(commonFieldsList);
		
		data.add(new SerializableNameValuePair("pp", "20"));
		data.add(new SerializableNameValuePair("spmo", "1"));
		data.add(new SerializableNameValuePair("output", "pln"));
		data.add(new SerializableNameValuePair("androidversion", "1.1.4"));
		data.add(new SerializableNameValuePair("htype", "google_sdk"));
		data.add(new SerializableNameValuePair("hcount", "0"));
		data.add(new SerializableNameValuePair("L","vs_javapln"));
		
		
		DefaultHttpClient client = new DefaultHttpClient();
		
		HttpPost request = new HttpPost("http://h2g.sitkol.pl/bin/tb/query-p2w.exe/pn");
		client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
        client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
        request.addHeader("Content-Type", "text/plain");
        request.setEntity(new UrlEncodedFormEntity(data));
        
        HttpResponse response = client.execute(request);
         
        // Pull content stream from response
        HttpEntity entity = response.getEntity();
        InputStream inputStream = entity.getContent();
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        
        int readBytes = 0;
        while ((readBytes = inputStream.read(sBuffer)) != -1) {
            content.write(sBuffer, 0, readBytes);
        }
        String[] parts = content.toString().split("\n");
        
        for (String s : parts){
        	if(s.startsWith("url=")){
        		timetableUrl = s.substring(4);
        		break;
        	}
        }
        
        Log.i("RozkladPKP","TT URL" + (timetableUrl != null ? timetableUrl : "NULL"));
	}

	protected void getFullTimetable() throws Exception {
		DefaultHttpClient client = new DefaultHttpClient();
		
		HttpGet request = new HttpGet(timetableUrl);
		client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
        client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
        String url = null;
        
        HttpResponse response;
        HttpEntity entity;
        InputStream inputStream;
        ByteArrayOutputStream content;
        int readBytes;
        
        showFullLoader();
        try{
        while(url == null)
        {
	        response = client.execute(request);
	         
	        // Pull content stream from response
	        entity = response.getEntity();
	        inputStream = entity.getContent();
	        content = new ByteArrayOutputStream();
	        
	        readBytes = 0;
	        while ((readBytes = inputStream.read(sBuffer)) != -1) {
	            content.write(sBuffer, 0, readBytes);
	        }
	        String[] parts = content.toString().split("\n");
	        
	        
	        Log.i("RozkladPKP","GFT:"+content.toString());
	        
	        for (String s : parts){
	        	if(s.startsWith("url=")){
	        		url = s.substring(4);
	        		break;
	        	}
	        }
	        if(url == null)
	        	Thread.sleep(TIMETABLE_DOWNLOAD_INTERVAL);
        }
        if(url != null)
        {
        	Log.i("RozkladPKP","PLN: "+url);
	        //Pobranie PLN
	        request = new HttpGet(url);
	        response = client.execute(request);
	        
	        entity = response.getEntity();
	        inputStream = entity.getContent();
	        GZIPInputStream in = new GZIPInputStream(inputStream);
	        content = new ByteArrayOutputStream();
	        
	        readBytes = 0;
	        while ((readBytes = in.read(sBuffer)) != -1) {
	            content.write(sBuffer, 0, readBytes);
	        }
	        
	        plndata = content.toByteArray();
	        Log.i("RozkladPKP", "jest pełny PLN" + Integer.toString(content.size()));
	        pln = new PLN(plndata);
	        
	        hasFullTable = true;
	        updateDisplayedPLN();
        }
        else
        	Log.i("RozkladPKP","Jeszcze w8");
        }
        catch(InterruptedException e)
        {
        	Log.d("RozkladPKP","Pobieranie pełnego rozkładu anulowane");
        	return;
        }
         
        hideLoader();
        	
        
	}
	
	
	public void getMore(boolean earlier) throws Exception
	{
		seqnr++;
		
		ArrayList<SerializableNameValuePair> data = new ArrayList<SerializableNameValuePair>();
		data.addAll(commonFieldsList);
		
		data.add(new SerializableNameValuePair("seqnr", Integer.toString(seqnr)));
		data.add(new SerializableNameValuePair("ident", pln.id()));
		data.add(new SerializableNameValuePair("hcount", "1"));
		data.add(new SerializableNameValuePair("REQ0HafasScrollDir", earlier ? "2" : "1"));
		data.add(new SerializableNameValuePair("ignoreMinuteRound", "yes"));
		data.add(new SerializableNameValuePair("androidversion", "1.1.4"));
		data.add(new SerializableNameValuePair("h2g-direct", "1"));
	    	
		PLNRequest("http://rozklad.sitkol.pl/bin/query.exe/pn",data);
		
		if(pln.conCnt == 0 && timetableUrl != null)
		{
			Log.i("RozkladPKP","Pobieram pelny");
			getFullTimetable();
		}
	}
	
	private void PLNRequest(String url, ArrayList<SerializableNameValuePair> data) throws Exception
	{
		DefaultHttpClient client = new DefaultHttpClient();
		
		HttpPost request = new HttpPost(url);
		client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
        client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
        request.addHeader("Content-Type", "text/plain");
        request.setEntity(new UrlEncodedFormEntity(data));
        
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
		updateDisplayedPLN();
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.connection_list, menu);
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
		state.putBoolean("hasFullTable", hasFullTable);
		state.putString("timetableURL", timetableUrl);
	}
	
	
}
