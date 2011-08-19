package org.tyszecki.rozkladpkp;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class ConnectionList {

	private PLN pln;
	private int seqnr = 0;
	private boolean isStatic = false;
	private boolean aborted = false;
	private Thread thread;
	private int attempts;
	
	private String date,time;
	
	private ArrayList<SerializableNameValuePair> common;
	private byte[] sBuffer = new byte[512];
	
	
	private final String URL = "http://rozklad.sitkol.pl/bin/query.exe/pn"; //http://mobile.bahn.de/bin/mobil/query.exe
	private final int MAX_ATTEMPTS = 20;
	
	public interface ConnectionListCallback{
		void contentReady(ConnectionList list, boolean error);
	}
	
	ConnectionListCallback callback;
	
	private ConnectionList(ConnectionListCallback callback) {
		this.callback = callback;
	}
	
	public static ConnectionList fromNetwork(ConnectionListCallback callback, ArrayList<SerializableNameValuePair> commonFields, String date, String time) {
		
		ConnectionList ret = new ConnectionList(callback);
		
		ret.common = commonFields;
		ret.date = date;
		ret.time = time;
		
		return ret;
	}
	
	public static ConnectionList fromFile(ConnectionListCallback callback, String filename){
		
		ConnectionList ret = new ConnectionList(callback);
		
		FileInputStream fis;
		try {
			fis = RozkladPKPApplication.getAppContext().openFileInput(filename);
		} catch (FileNotFoundException e) {
			return ret;
		}
		
		ByteArrayOutputStream content = new ByteArrayOutputStream();
	        
	    int readBytes = 0;
	    try {
			while ((readBytes = fis.read(ret.sBuffer)) != -1)
				content.write(ret.sBuffer, 0, readBytes);
		} catch (IOException e) {
			return ret;
		}
	    
	    ret.pln = new PLN(content.toByteArray());
	    ret.isStatic = true;
	    
	    
	    if(ret.callback != null)
	    	ret.callback.contentReady(ret, false);
	    
		return ret;
	}
	
	public static ConnectionList fromByteArray(ConnectionListCallback callback, byte[] array, int seqnr)
	{
		ConnectionList ret = new ConnectionList(callback);
		
		ret.seqnr = seqnr;
		ret.pln = new PLN(array);
		
		if(ret.callback != null)
	    	ret.callback.contentReady(ret, false);
		
		return ret;
	}
	
	
	public void fetch() 
	{
		if(isStatic)
			return;
		
		ArrayList<SerializableNameValuePair> data = new ArrayList<SerializableNameValuePair>();
		data.addAll(common);
		
		data.add(new SerializableNameValuePair("ignoreMinuteRound", "yes"));
		data.add(new SerializableNameValuePair("date", date));
		data.add(new SerializableNameValuePair("time", time));
		data.add(new SerializableNameValuePair("h2g-direct", "1"));
		
		attempts = 0;
		download(data);
	}
	
	public void fetchMore(boolean next) 
	{
		if(isStatic)
			return;
		
		seqnr++;
		
		ArrayList<SerializableNameValuePair> data = new ArrayList<SerializableNameValuePair>();
		data.addAll(common);
		
		data.add(new SerializableNameValuePair("seqnr", Integer.toString(seqnr)));
		data.add(new SerializableNameValuePair("ident", pln.id()));
		data.add(new SerializableNameValuePair("hcount", "1"));
		data.add(new SerializableNameValuePair("REQ0HafasScrollDir", next ? "2" : "1"));
		data.add(new SerializableNameValuePair("ignoreMinuteRound", "yes"));
		data.add(new SerializableNameValuePair("androidversion", "1.1.4"));
		data.add(new SerializableNameValuePair("h2g-direct", "1"));
	    	
		int tries = 0;
		attempts = MAX_ATTEMPTS;
		download(data);		
	}
	
	private void download(final ArrayList<SerializableNameValuePair> data)
	{
		aborted = false;
		thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				int tries = attempts;
				do
				{
					DefaultHttpClient client = new DefaultHttpClient();
					
					HttpPost request = new HttpPost(URL);
					client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
			        client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
			        
			        request.addHeader("Content-Type", "text/plain");
			        try {
						request.setEntity(new UrlEncodedFormEntity(data,"UTF-8"));
					} catch (UnsupportedEncodingException e) {
						if(!aborted)
							callback.contentReady(ConnectionList.this, true);
						return;
					}
					
					ByteArrayOutputStream content = new ByteArrayOutputStream();
			        HttpResponse response;
			        
					try {
						response = client.execute(request);
				        HttpEntity entity = response.getEntity();
				        InputStream inputStream = entity.getContent();
				        GZIPInputStream in = new GZIPInputStream(inputStream);
				        
				        int readBytes = 0;
				        while ((readBytes = in.read(sBuffer)) != -1) {
				            content.write(sBuffer, 0, readBytes);
				        }
					} catch (Exception e) {
						if(!aborted)
							callback.contentReady(ConnectionList.this, true);
						return;
					}
					
			        pln = new PLN(content.toByteArray());
			        if(callback != null && (tries == 0 || pln.conCnt > 0) && !aborted)
				    	callback.contentReady(ConnectionList.this, false);
			        
				}while(pln.conCnt == 0 && --tries > 0);
			}
		});
		thread.start();
	}
	
	public PLN getPLN()
	{
		return pln;
	}
	
	public int getSeqNr()
	{
		return seqnr;
	}
	
	public void abort()
	{
		Log.i("RozkladPKP", "Aborcja!");
		aborted = true;
		if(thread != null && thread.isAlive())
		{
			thread.interrupt();
			thread = null;
		}
	}
}
