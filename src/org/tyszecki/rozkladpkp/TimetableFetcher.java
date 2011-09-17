package org.tyszecki.rozkladpkp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

class TimetableFetcher extends AsyncTask<Void, Void, String>{

		boolean arr;
		Time time;
		String productString, station;
		
		
		public TimetableFetcher(String products, Time datetime, String stationID, boolean arrival)
		{
			arr = arrival;
			time = datetime;
			productString = products;
			station = stationID;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			try{
				DefaultHttpClient client = new DefaultHttpClient();
				byte[] sBuffer = new byte[512];
				
				String hour = time.format("%H:%M");
				String date = time.format("%d.%m.%Y");
				
				
				String type = arr?"arr":"dep";		

				String data = "L=vs_java3&productsFilter="+productString+"&input="+station+"&maxJourneys=20&boardType="+type+"&time="+hour+"&date="+date+"&start=yes";
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

				return "<a>"+new String(content.toByteArray())+"</a>";
				
			} catch (Exception e) { 
				return null;
			}
		}
	}