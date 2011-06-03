package org.tyszecki.rozkladpkp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.tyszecki.rozkladpkp.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ListView;

public class RouteActivity extends Activity {
	
	private ArrayList<RouteItem> m_items = null;
	private ProgressDialog m_ProgressDialog = null; 
	private RouteItemAdapter m_adapter;
	private Runnable viewTable;
	private static byte[] sBuffer = new byte[512];
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route);
        
        

        viewTable = new Runnable(){
            @Override
            public void run() {
                try {
					getTable();
				} catch (Exception e) {
					e.printStackTrace();
				} 
            }
        };
        
        m_items = new ArrayList<RouteItem>();
        
        m_adapter = new RouteItemAdapter(this, R.layout.timetable_row, m_items);
        
        
        ListView lv = (ListView)findViewById(R.id.route);
                lv.setAdapter(this.m_adapter);
        
        Thread thread =  new Thread(null, viewTable, "MagentoBackground");
        thread.start();
        m_ProgressDialog = ProgressDialog.show(RouteActivity.this,    
              "Czekaj...", "Pobieranie rozkładu...", true);
	}

	protected void getTable() throws ClientProtocolException, IOException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub
		DefaultHttpClient client = new DefaultHttpClient();
		//String url = "http://192.168.1.101/test/dane.php";
		
		String time = getIntent().getExtras().getString("time");
		String date = getIntent().getExtras().getString("date");
		String number = getIntent().getExtras().getString("number");
		String dID = getIntent().getExtras().getString("destID");
		String sID = getIntent().getExtras().getString("startID");
		String bType = getIntent().getExtras().getString("Type");
		
		
		
		
		String data = "start=yes&REQTrain_name="+number+"&date="+date+"&time="+time+"&boardType="+bType+"&sTI=1&dirInput="+dID+"&L=vs_java3&input="+sID+"&productsFilter=11111111111111";
    	String url  = "http://rozklad.sitkol.pl/bin/stboard.exe/pn" ;
    	
		HttpPost request = new HttpPost(url);
		client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
        client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
        request.addHeader("Content-Type", "text/plain");
        
        Log.i("RozkladPKP",data);
        
		request.setEntity(new StringEntity(data));
		
        
        HttpResponse response = client.execute(request);
         
        // Pull content stream from response
        HttpEntity entity = response.getEntity();
        InputStream inputStream = entity.getContent();
        ByteArrayOutputStream content = new ByteArrayOutputStream();

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
    	Document doc = db.parse(inStream);
    	
    	NodeList list = doc.getElementsByTagName("St");
        
        m_items = new ArrayList<RouteItem>();
        
        int j = list.getLength();
        
        int isID	= Integer.parseInt(sID);
		for(int i = 0; i < j; i++)
        { 
        	RouteItem o = new RouteItem();
        	Node n = list.item(i);
        	o.station 	= n.getAttributes().getNamedItem("name").getNodeValue();
        	if(n.getAttributes().getNamedItem("arrTime") != null)
        		o.arr = n.getAttributes().getNamedItem("arrTime").getNodeValue();
        	else
        		o.arr = null;
        	if(n.getAttributes().getNamedItem("depTime") != null)
        		o.dep = n.getAttributes().getNamedItem("depTime").getNodeValue();
        	else
        		o.dep = null;
        	
        	o.stid	= n.getAttributes().getNamedItem("evaId").getNodeValue();
        	
        	if(Integer.parseInt(o.stid) == isID)
        		m_adapter.setCurrentStation(sID,i);
        	
        	m_items.add(o);
        }
		runOnUiThread(returnRes);
	}
	
	private Runnable returnRes = new Runnable() {

        @Override
        public void run() {
            if(m_items != null && m_items.size() > 0){
                m_adapter.notifyDataSetChanged();
                for(int i=0;i<m_items.size();i++)
                m_adapter.add(m_items.get(i));
            }
            m_ProgressDialog.dismiss();
            m_adapter.notifyDataSetChanged();
        }
      };
	
}