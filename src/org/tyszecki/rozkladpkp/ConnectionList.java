package org.tyszecki.rozkladpkp;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.tyszecki.rozkladpkp.pln.PLN;
import org.tyszecki.rozkladpkp.servers.HafasServer;
import org.tyszecki.rozkladpkp.servers.ServerManager;

public class ConnectionList {

	private PLN pln;
	private int seqnr = 0;
	private boolean isStatic = false;
	private Thread thread;
	private int attempts;
	private int serverId = -1;
	
	private String date,time;
	
	private ArrayList<SerializableNameValuePair> common;
	private byte[] sBuffer = new byte[512];
	
	
	
	private final int MAX_ATTEMPTS = 15;
	
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
	
	public static ConnectionList fromByteArray(ConnectionListCallback callback, ArrayList<SerializableNameValuePair> commonFields, byte[] array, int seqnr)
	{
		ConnectionList ret = new ConnectionList(callback);
		
		ret.common = commonFields;
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
		
		attempts = 1;
		download(data);
	}
	
	public void fetchMore(boolean next) 
	{
		if(isStatic)
			return;
		
		seqnr++;
		
		ArrayList<SerializableNameValuePair> data = new ArrayList<SerializableNameValuePair>();
		//data.addAll(common);
		data.add(new SerializableNameValuePair("REQ0HafasOptimize1", "1"));
		data.add(new SerializableNameValuePair("seqnr", Integer.toString(seqnr)));
		
		data.add(new SerializableNameValuePair("clientSystem", "android14"));
		data.add(new SerializableNameValuePair("existOptimizePrice", "1"));
		data.add(new SerializableNameValuePair("hcount", "0"));
		data.add(new SerializableNameValuePair("ignoreMinuteRound", "yes"));
		data.add(new SerializableNameValuePair("androidversion", "2.0.8"));
		
		data.add(new SerializableNameValuePair("ident", pln.id()));
		data.add(new SerializableNameValuePair("REQ0HafasScrollDir", next ? "2" : "1"));
		
		//data.add(new SerializableNameValuePair("androidversion", "1.1.4"));
		data.add(new SerializableNameValuePair("h2g-direct", "1"));
		data.add(new SerializableNameValuePair("clientDevice", "google_sdk"));
		data.add(new SerializableNameValuePair("clientDevice", "ANDROID"));
		data.add(new SerializableNameValuePair("htype", "google_sdk"));
		
		
		attempts = MAX_ATTEMPTS;
		download(data);		
	}
	
	private void download(final ArrayList<SerializableNameValuePair> in)
	{
		thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				for(int i = 0;;++i)
				{
					HafasServer s = ServerManager.getServer(i);
					
					if(s == null)
						break; //Koniec serwerów
					
					String ld = (pln != null) ? pln.ld() : null; 
					//!!!! ufff... dodanie tego parametru zwiększa wielokrotnie wydajność systemu.
					//Bez niego program wolniej działa, a serwer Sitkola jest DDOSowany :) 
					//Ah ten HAFAS i jego tajemnice.
		
					int tries = attempts;
					int result;
					do
					{	
						result = s.getConnections(in, ld);
						if(result == HafasServer.DOWNLOAD_OK)
						{
							pln = s.getPLN();
							ConnectionList.this.serverId = i;
							callback.contentReady(ConnectionList.this, false);
							return;
						}
						else if(result == HafasServer.DOWNLOAD_ERROR_SERVER_FAULT)
							break; //Następny serwer, jeśli ten nie działa.
						
					}while((result != HafasServer.DOWNLOAD_OK) && --tries > 0);
				}
				callback.contentReady(ConnectionList.this, true);
			}
		});
		thread.start();
	}
	
	/**
	 * 
	 * @return Numer serwera, -1 jeśli nieznany.
	 */
	public int getServerId()
	{
		return serverId;
	}
	
	public boolean scrollable()
	{
		return !isStatic;
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
		if(thread != null && thread.isAlive())
		{
			thread.interrupt();
			thread = null;
		}
	}
}
