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
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class RouteFetcher {
	public static Document fetchRoute(String train_number, String station_s, String station_d, String date, String time, String type) throws SAXException, IOException, ParserConfigurationException{
		
		String data = "start=yes&REQTrain_name="+train_number+"&date="+date+"&time="+time+"&sTI=1&dirInput="+station_d+"&L=vs_java3&input="+station_s+"&boardType="+type;
    	String url  = "http://rozklad.sitkol.pl/bin/stboard.exe/pn";
    	
    	DefaultHttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url);
		client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
        client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
        request.addHeader("Content-Type", "text/plain");
        
        //Log.i("RozkladPKP",data);
        
		request.setEntity(new StringEntity(data));
		
        HttpResponse response = client.execute(request);
         
        // Pull content stream from response
        HttpEntity entity = response.getEntity();
        InputStream inputStream = entity.getContent();
        ByteArrayOutputStream content = new ByteArrayOutputStream();

        byte[] sBuffer = new byte[512];
        // Read response into a buffered stream
        int readBytes = 0;
        while ((readBytes = inputStream.read(sBuffer)) != -1) {
            content.write(sBuffer, 0, readBytes);
        }

        // Return result from buffered stream
        String xmlstring = new String(content.toByteArray());
        xmlstring	= xmlstring.replace("< ", "<");
    	
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder db = factory.newDocumentBuilder();
    	InputSource inStream = new InputSource();
    	inStream.setCharacterStream(new StringReader("<a>"+xmlstring+"</a>"));
    	return db.parse(inStream);
	}
}
