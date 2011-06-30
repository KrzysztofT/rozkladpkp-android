package org.tyszecki.rozkladpkp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
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
		if(delays.isEmpty() || nmi-umi > 1000*60*3)
		{
			class DealyTask extends AsyncTask<Void, Void, Void>{

				@Override
				protected Void doInBackground(Void... arg0) {
					
					//Prosty skrypt w pythonie parsuje stronę PR i wysyła mi wyniki.
					//To lepsze niż parsowanie bezpośrednio w aplikacji:
					//- W Pythonie można to napisać łatwiej,
					//- Jeśli PR coś zmienią w formacie danych, wystarczy zmiana skryptu żeby działała ta aplikacja
					//- Mogę dodawać do skryptu dane o opóźnieniach z innych źródeł
					String url  = "http://opoznienia.appspot.com";

					DefaultHttpClient client = new DefaultHttpClient();
					HttpGet request = new HttpGet(url);
					client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
					client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
					request.addHeader("Content-Type", "application/x-www-form-urlencoded");


					HttpResponse response = null;
					InputStream inputStream = null;
					try {
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
					String result = new String(content.toByteArray());
					String[] items = result.split("\n");
					delays.clear();

					for(String s : items)
					{
						String[] t = s.split(":");
						delays.put(t[0], Integer.parseInt(t[1]));
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
