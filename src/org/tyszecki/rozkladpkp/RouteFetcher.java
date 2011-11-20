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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import android.content.Context;
import android.os.AsyncTask;

public class RouteFetcher extends AsyncTask<RouteFetcher.RouteParams, Void, Document> {
	private static byte[] sBuffer = new byte[512];

	public static class RouteParams{
		String departure,arrival;
		String deptime,arrtime;
		String type = "dep";
		String train_number;
		String date;
		boolean force_download = false;
	}
	
	private boolean isCached = true;
	
	protected boolean isCached()
	{
		return isCached;
	}
	
	private static String getCached(String trainNumber)
	{
		FileInputStream fis;
		try {
			fis = RozkladPKPApplication.getAppContext().openFileInput("route_"+trainNumber);
		} catch (FileNotFoundException e) {
			return null;
		}
		
		ByteArrayOutputStream content = new ByteArrayOutputStream();
	        
	    int readBytes = 0;
	    try {
			while ((readBytes = fis.read(sBuffer)) != -1)
				content.write(sBuffer, 0, readBytes);
		} catch (IOException e) {
			return null;
		}
		
		return content.toString();
	}
	
	/*private static void removeCached(String trainNumber)
	{
		RozkladPKPApplication.getAppContext().deleteFile("route_"+trainNumber);
	}*/
	
	private static void saveInCache(String trainNumber, String xml)
	{
		try {
			FileOutputStream fos = RozkladPKPApplication.getAppContext().openFileOutput("route_"+trainNumber, Context.MODE_PRIVATE);
			fos.write(xml.getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static boolean checkTable(String xml, String station, String time)
	{
		if(time == null || station == null)
			return true; //Pozwala na zapisywanie stacji z "rozkładów jazdy", gdzie znany jest tylko odjazd.
		
		int poss = xml.indexOf(station);
		int post = xml.indexOf(time);
		
		if(poss != -1 && post != -1)
		{
			int posa = xml.lastIndexOf('<',poss);
			int posb = xml.lastIndexOf('<',post);
			
			if(posa == posb)
				return true;
		}
		return false;
	}
	
	@Override
	protected Document doInBackground(RouteParams... params) {
		
		if(params.length == 0)
			return null;
		
		
		
		
		RouteParams par = params[0];
		
		String cached = null, xmlstring = null;
		
		if(!par.force_download)
			cached = getCached(par.train_number);
		
		if(cached != null && checkTable(cached, par.departure, par.deptime) && checkTable(cached, par.arrival, par.arrtime))
			xmlstring = cached;
		else
		{
			if(!CommonUtils.onlineCheckSilent())
				return null;
			
			isCached = false;
			publishProgress();
			
			String data = "start=yes&REQTrain_name="+par.train_number+"&date="+par.date+"&time="+par.deptime+"&sTI=1&dirInput="+par.arrival+"&L=vs_java3&input="+par.departure+"&boardType="+par.type;
			String url  = "http://rozklad.sitkol.pl/bin/stboard.exe/pn";

			try{
				DefaultHttpClient client = new DefaultHttpClient();
				HttpPost request = new HttpPost(url);
				client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
				client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
				request.addHeader("Content-Type", "text/plain");
				request.setEntity(new StringEntity(data));


				InputStream inputStream = client.execute(request).getEntity().getContent();
				
				
				ByteArrayOutputStream content = new ByteArrayOutputStream();

				int readBytes = 0;
				while ((readBytes = inputStream.read(sBuffer)) != -1) {
					content.write(sBuffer, 0, readBytes);
				}

				xmlstring = new String(content.toByteArray());
				xmlstring	= xmlstring.replace("< ", "<");

				saveInCache(par.train_number, xmlstring);
			}
			catch (Exception e) {
				return null;
			}
		}
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = factory.newDocumentBuilder();
			InputSource inStream = new InputSource();
			inStream.setCharacterStream(new StringReader("<a>"+xmlstring+"</a>"));
			return db.parse(inStream);
		}catch (Exception e) {
			return null;
		}
	}
}
