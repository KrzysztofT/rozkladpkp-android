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
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

public class StationSearch {
	private DefaultHttpClient client;
	
	public StationSearch() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 2000);
		HttpConnectionParams.setSoTimeout(params, 2000);
		
		client = new DefaultHttpClient(params);
		client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
	    client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
	}
	
	public InputStream search(String station) throws IllegalStateException, IOException {
		Log.i("RozkladPKP", "szukam stacji...");
		//Dla tych którzy wpadną na pomysł wpisania cudzysłowów itp. do nazwy stacji
		station = station.replaceAll("[^a-zA-Z0-9]", "");
		
		String url = "http://h2g.sitkol.pl/bin/query.exe/dn";
		//String url = "http://railnavigator.bahn.de/bin/rnav/query.exe/pn";
		String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><ReqC ver=\"1.1\" prod=\"String\" lang=\"PL\"><MLcReq><MLc n=\""+station+"\" t=\"ST\" /></MLcReq></ReqC>";
        HttpPost request = new HttpPost(url);
        
		request.setEntity(new StringEntity(data));	
        request.addHeader("Content-Type", "text/plain;charset=UTF-8");
        
        
        HttpResponse response = null;	
        do{
	        for(int i = 0; i < 5; i++)
	        {
	        	try{response = client.execute(request);}
	        	catch(Exception e){continue;}
	        	break;
	        }
	    }while(response == null);
        // Pull content stream from response
        HttpEntity entity = response.getEntity();
        
        Log.i("RozkladPKP","są wyniki ");
		return entity.getContent();
	}
	public String searchResult(String station) throws IllegalStateException, IOException
	{
		
		InputStream str = search(station);
		byte[] sBuffer = new byte[512];
		
		int readBytes = 0;
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		
        while ((readBytes = str.read(sBuffer)) != -1) {
            content.write(sBuffer, 0, readBytes);
        }
        return new String(content.toByteArray());
	}
}
