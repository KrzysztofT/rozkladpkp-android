package org.tyszecki.rozkladpkp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class StationSearch {
	private DefaultHttpClient client;
	
	public StationSearch() {
		 client = new DefaultHttpClient();
		 client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
	     client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
	}
	
	public InputStream search(String station) throws IllegalStateException, IOException {
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
