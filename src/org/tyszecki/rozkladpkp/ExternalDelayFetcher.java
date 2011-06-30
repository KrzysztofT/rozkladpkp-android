package org.tyszecki.rozkladpkp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

public class ExternalDelayFetcher {
	private static HashMap<String, Integer> delays = new HashMap<String, Integer>();
	private static Time lastUpdate = null; 
	
	public static interface ExternalDelayFetcherCallback{
		void ready(HashMap<String, Integer> delays, boolean cached);
	}
	
	static void requestUpdate(final ExternalDelayFetcherCallback callback)
	{
		if(lastUpdate == null)
		{
			lastUpdate = new Time();
			lastUpdate.setToNow();
		}
		//Nie aktualizujmy za często.
		long umi = lastUpdate.toMillis(false);
		lastUpdate.setToNow();
		long nmi = lastUpdate.toMillis(false);

		//Cy minuty!
		if(true || delays.isEmpty() || nmi-umi > 1000*60*3)
		{
			class DealyTask extends AsyncTask<Void, Void, Void>{

				@Override
				protected Void doInBackground(Void... arg0) {
					String data = "fil_p1=&funkcja=generowanie_tab";
					String url  = "http://82.160.42.14/opoznienia/zapytanie.php";

					DefaultHttpClient client = new DefaultHttpClient();
					HttpPost request = new HttpPost(url);
					client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
					client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
					request.addHeader("Content-Type", "application/x-www-form-urlencoded");


					HttpResponse response = null;
					InputStream inputStream = null;
					try {
						request.setEntity(new StringEntity(data));
						response = client.execute(request);

						HttpEntity entity = response.getEntity();
						inputStream = entity.getContent();
					} catch (Exception e) {
						e.printStackTrace();
					}

					ByteArrayOutputStream content = new ByteArrayOutputStream();

					byte[] sBuffer = new byte[512];
					// Read response into a buffered stream
					int readBytes = 0;

					try {
						while ((readBytes = inputStream.read(sBuffer)) != -1) {
							content.write(sBuffer, 0, readBytes);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					// Return result from buffered stream
					String html = new String(content.toByteArray());

					//Parsowanie HTML w Androidzie wymaga chyba zewnętrznych bibliotek, a to
					//jest zdecydowanie za prosty przypadek żeby atakować parserem.
					//Jeśli to nawali, to z parserem też by nawaliło 
					//TODO: można jakiś trywialny parser napisać zamiast tej brzydkiej pętli
					int pos = html.indexOf("<tr>")+4;
					pos = html.indexOf("<tr>",pos)+4;
					delays.clear();

					while(pos != -1)
					{
						pos += 4;
						//Numer pociągu jest w pierwszym <a>
						pos = html.indexOf("<a", pos);
						if(pos == -1)
							break;
						pos += 2;
						int startpos = html.indexOf(">", pos)+1;
						int endpos = html.indexOf("</a>", startpos);

						if(startpos == -1 || endpos == -1)
							break;

						String num = html.substring(startpos, endpos);

						pos = endpos+4;
						//Opóźnienie w trzecim <td>
						pos = html.indexOf("<td>", pos);
						if(pos == -1)
							break;
						pos += 4;


						startpos = html.indexOf("<td>", pos)+4;
						endpos = html.indexOf("</td>", startpos);

						if(startpos == 3 || endpos == -1)
							break;

						String del = html.substring(startpos,endpos);

						pos = endpos;
						pos = html.indexOf("<tr>",pos);

						int slpos = num.indexOf('/');
						if(slpos != -1)
							num = num.substring(0,slpos);

						int delay = 0;
						
						del = del.trim();
						
						if(del.contains("planowo"))
							delay = 0;
						else
						{
							String[] parts = del.split(" ");
							for(int i = 0; i < parts.length; ++i)
							{
								try{
									delay = Integer.parseInt(parts[i]);
									Log.i("RozkladPKP", "parsuje ok: "+parts[i]);
									break;
								}catch (Exception e) {
									Log.i("RozkladPKP", "wywala sie na: "+parts[i]);
								}
							}
						}
						
						Log.i("RozkladPKP","PR: "+num+" - "+Integer.toString(delay)+", pos:"+Integer.toString(pos));
						delays.put(num, delay);
					}
					return null;
				}

				protected void onPostExecute(Void result) {
					Log.i("RozkladPKP","Calling callback");
					callback.ready(delays, false);
				}
			}
			new DealyTask().execute(null,null,null);
		}
		else
		{
			lastUpdate.set(umi);
			callback.ready(delays, true);
		}

	}
}
