package org.tyszecki.rozkladpkp;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.tyszecki.rozkladpkp.ExternalDelayFetcher.ExternalDelayFetcherCallback;
import org.tyszecki.rozkladpkp.pln.PLN;
import org.tyszecki.rozkladpkp.servers.HafasServer;
import org.tyszecki.rozkladpkp.servers.ServerManager;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

public class ConnectionList extends Observable{

	private PLN pln;
	private int seqnr = 0;
	private boolean isStatic = false, notifyOnAdd = false;
	private Thread thread;
	private int attempts;
	private int serverId = -1;
	private int lastError = HafasServer.DOWNLOAD_OK;
	
	
	private String SID,ZID;
	
	private ArrayList<SerializableNameValuePair> common;
	private byte[] sBuffer = new byte[512];
	private final int MAX_ATTEMPTS = 15;
	
	
	enum CachePolicy{NoCached, CachedIfAvailable, OnlyCached}; 
	
	//CacheID: -1 = brak, 0 = podstawowy, 1 = dla widżetów 
	public static ConnectionList forParameters(Context c, ArrayList<SerializableNameValuePair> commonFields, CachePolicy policy, int cacheID) {
		ConnectionList ret = null;
		
		String S = null,Z = null;
		for(SerializableNameValuePair p : commonFields)
		{
			if(p.name.equals("SID"))
				S = CommonUtils.StationIDfromSID(p.value);
			if(p.name.equals("ZID"))
				Z = CommonUtils.StationIDfromSID(p.value);
		}
		
		if(policy == CachePolicy.OnlyCached)
		{
			Log.i("RozkladPKP", "Pomijam sprawdzenie, wymuszenie zwracania z cache.");
			return ConnectionList.fromFile(CommonUtils.ResultsHash(S, Z, null, cacheID));
		}
		
		boolean cached = false;
		if(policy != CachePolicy.NoCached)
		{
			Log.i("RozkladPKP", "Sprawdzam cache");
			//Sprawdzmy, czy cache jest aktualny
			String t = RememberedManager.cacheValidTime(c, S, Z, cacheID);
			if(t != null)
			{
				try{
					Time ct,n;
					n = new Time();
					n.setToNow();
					ct = new Time();
					ct.parse(t);
        			
        			if(Time.compare(ct, n) > 0)	
        				cached = true;
        			else
        				invalidateCache(c,S,Z, cacheID); //Jeśli cache jest nieważny, usuń go.
				}catch (Exception e) {
					invalidateCache(c,S,Z, cacheID);
				}
			}
			if(!cached)
				Log.i("RozkladPKP", "Cache nieobecny/nieaktualny");
		}
			
		if(!cached)
		{
			Log.i("RozkladPKP", "Pobieranie rozkładu z internetu");
			ret = new ConnectionList();
			ret.common = commonFields;
			ret.SID = S;
			ret.ZID = Z;
			ret.fetch();
			return ret;
		}
		else
			return ConnectionList.fromFile(CommonUtils.ResultsHash(S, Z, null, cacheID));
	}
	
	private static void invalidateCache(Context c, String SID, String ZID, int cacheID) {
		RememberedManager.removeCache(c, SID, ZID, cacheID);
	}

	private static ConnectionList fromFile(String filename){
		ConnectionList ret = new ConnectionList();
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
	    
	    
	    ret.contentReady();	    
		return ret;
	}
	
	public static ConnectionList fromByteArray(ArrayList<SerializableNameValuePair> commonFields, byte[] array, int seqnr)
	{
		ConnectionList ret = new ConnectionList();
		
		ret.common = commonFields;
		ret.seqnr = seqnr;
		ret.pln = new PLN(array);
		
		ret.contentReady();
		return ret;
	}
	
	
	public void fetch() 
	{
		if(isStatic)
			return;
		
		ArrayList<SerializableNameValuePair> data = new ArrayList<SerializableNameValuePair>();
		data.addAll(common);
		
		data.add(new SerializableNameValuePair("ignoreMinuteRound", "yes"));
		data.add(new SerializableNameValuePair("h2g-direct", "1"));
		data.add(new SerializableNameValuePair("start", "1"));
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
		class DownloadTask extends AsyncTask<Void, Void, Void>{
			@Override
			protected Void doInBackground(Void... params) {
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
							lastError = result;
							return null;
						}
						else if(result == HafasServer.DOWNLOAD_ERROR_SERVER_FAULT)
							break;
						
					}while((result != HafasServer.DOWNLOAD_OK) && --tries > 0);
					lastError = result;
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				contentReady();
			}
		}
		new DownloadTask().execute();
	}
	
	private void contentReady()
	{
		if(pln == null || pln.conCnt == 0)
		{
			notifyObservers(false);
			return;
		}
		
		//Dane są aktualne, nie pobieramy znów
		if(ExternalDelayFetcher.isUpToDate())
		{
			pln.addExternalDelayInfo(ExternalDelayFetcher.getDelays());
			notifyObservers(false);
		}
		else
		{
			//Powiadom dwa razy - od razu i po dostniu opóźnień.
			notifyObservers(true);
			
			ExternalDelayFetcher.requestUpdate(new ExternalDelayFetcherCallback() {

				@Override
				public void ready(HashMap<String, Integer> delays, boolean cached) {
					pln.addExternalDelayInfo(delays);
					notifyObservers(false);
				}
			});
		}
	}
	
	@Override
	public void addObserver(Observer observer) {
		super.addObserver(observer);
		
		if(notifyOnAdd)
			observer.update(this, null);
	}
	
	public void notifyObservers(boolean willUpdate) {
		notifyOnAdd = true;

		setChanged(); //Metoda jest wywoływana tylko po modyfikacji
		super.notifyObservers(new Boolean(willUpdate));
	}
	
	public void saveInCache(Context c, int cacheID)
	{
		if(isStatic)
			return;
		
		Intent in = new Intent(c,RememberedService.class);

		if(pln != null)
			in.putExtra("pln", pln.data);

		if(SID == null || ZID == null)
			return;
		in.putExtra("SID", SID);
		in.putExtra("ZID", ZID);
		in.putExtra("cacheID", cacheID);
		
		c.startService(in);
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
	
	public int getLastError()
	{
		return lastError;
	}
}
