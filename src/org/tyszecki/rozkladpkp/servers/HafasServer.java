package org.tyszecki.rozkladpkp.servers;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.tyszecki.rozkladpkp.PLN;
import org.tyszecki.rozkladpkp.SerializableNameValuePair;

public abstract class HafasServer {
	public static final int URL_CONNECTIONS = 0;
	public static final int URL_TIMETABLE = 1;
	
	public static final int DOWNLOAD_OK = 0;
	public static final int DOWNLOAD_ERROR_WAIT = 1;
	public static final int DOWNLOAD_ERROR_SERVER_FAULT = 2;
	public static final int DOWNLOAD_ERROR_OTHER = 3;
	
	public abstract String url(int type);
	public abstract String name();
	public abstract ArrayList<SerializableNameValuePair> prepareFields(ArrayList<SerializableNameValuePair> input);
	
	private byte[] sBuffer = new byte[512];
	private PLN pln;
	
	public PLN getPLN()
	{
		return pln;
	}
	
	public int getConnections(ArrayList<SerializableNameValuePair> data, String ld)
	{
		data = prepareFields(data);
		//if(ld != null)
		//!!!! ufff... dodanie tego parametru zwiększa wielokrotnie wydajność systemu.
		//Bez niego program wolniej działa, a serwer Sitkola jest DDOSowany :) 
		//Ah ten HAFAS i jego tajemnice.
		DefaultHttpClient client = new DefaultHttpClient();

		HttpPost request = new HttpPost(url(URL_CONNECTIONS)+((ld == null)?"":"?ld="+ld));

		client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
		client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);


		/*for(int i = 0; i < data.size(); ++i)
	        {
	        	Log.i("RozkladPKP", data.get(i).getName() + "="+ data.get(i).getValue());
	        }*/
		
		request.addHeader("Content-Type", "text/plain");
		try {
			request.setEntity(new UrlEncodedFormEntity(data,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return DOWNLOAD_ERROR_OTHER;
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
			return DOWNLOAD_ERROR_SERVER_FAULT;
		}

		
		try{pln = new PLN(content.toByteArray());}
		catch(Exception e){
			return DOWNLOAD_ERROR_SERVER_FAULT;
		}
		
		if(pln.conCnt > 0)
			return DOWNLOAD_OK;
		
		return DOWNLOAD_ERROR_WAIT;
	}
}
