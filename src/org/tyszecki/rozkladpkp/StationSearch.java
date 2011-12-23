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

import android.util.Log;

public class StationSearch {
	private DefaultHttpClient client;
	
	public StationSearch() {
		 client = new DefaultHttpClient();
		 client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
	     client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
	}
	
	public InputStream search(String station) throws IllegalStateException, IOException {
		Log.i("RozkladPKP", "szukam stacji...");
		//Dla tych którzy wpadną na pomysł wpisania cudzysłowów itp. do nazwy stacji
		station = station.replaceAll("[^a-zA-Z0-9]", "");
		
		String url = "http://h2g.sitkol.pl/bin/query.exe/dn";
		String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><ReqC ver=\"1.1\" prod=\"String\" lang=\"DE\"><MLcReq><MLc n=\""+station+"\" t=\"ST\" /></MLcReq></ReqC>";
        HttpPost request = new HttpPost(url);
        
		request.setEntity(new StringEntity(data));	
        request.addHeader("Content-Type", "text/plain");
        
        HttpResponse response = null;	
		response = client.execute(request);
		 
        // Pull content stream from response
        HttpEntity entity = response.getEntity();
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
