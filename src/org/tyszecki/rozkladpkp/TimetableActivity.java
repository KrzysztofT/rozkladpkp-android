package org.tyszecki.rozkladpkp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.tyszecki.rozkladpkp.TimetableItem.DateItem;
import org.tyszecki.rozkladpkp.TimetableItem.TrainItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class TimetableActivity extends Activity {
	
	private ProgressDialog m_ProgressDialog = null; 
	private ArrayList<TimetableItem> m_items = null;
	private TimetableItemAdapter m_adapter;
	private Runnable viewBoard;
	private static byte[] sBuffer = new byte[512];
	private String SID;
	private boolean dep;
	NodeList destList = null;
	TimetableItem item;
	String startID = null,destID = null;
	Pattern p;
	Matcher m;
	AdapterView<?> av;
	Runnable showTimetable;
	TrainItem titem;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable);
        
        SID = getIntent().getExtras().getString("SID");
        
        
        
        Log.i("RozkladPKP",SID);
        dep = getIntent().getExtras().getString("Type").equals("dep");
        
        RememberedManager.addtoHistory(this, CommonUtils.StationIDfromSID(SID), dep);
        
        setTitle((dep?"Odjazdy z ":"Przyjazdy do ")+getIntent().getExtras().getString("Station"));
        
        m_items = new ArrayList<TimetableItem>();
        
        m_adapter = new TimetableItemAdapter(this, m_items);
        m_adapter.setType(dep);
        
        ListView lv = (ListView)findViewById(R.id.timetable);
        lv.setAdapter(this.m_adapter);
        
        viewBoard = new Runnable(){
            @Override
            public void run() {
                getBoard();
            }
        };
        
        Thread thread =  new Thread(null, viewBoard, "MagentoBackground");
        thread.start();
        m_ProgressDialog = ProgressDialog.show(TimetableActivity.this,    
              "Czekaj...", "Pobieranie rozkładu...", true);
        
        //Włączanie informacji o pociągu - potrzebnego do tego są identyfikatory stacji.
        //Ponieważ nie rozpracowałem jeszcze formatu PLN, używana jest wyszukiwarka.
        lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long id) {
				p = Pattern.compile(".*L=(\\d+)@.*");
				m = p.matcher(SID);
				startID = null;
				destID = null;
				av = arg0;
				
				if(m.matches())
					startID	= m.group(1);
			
				item = m_items.get(pos);
				if(item instanceof TimetableItem.DateItem)
					return;
				
				titem = (TrainItem)item;
				
				m_ProgressDialog = ProgressDialog.show(TimetableActivity.this,    
			              "Czekaj...", "Wyszukiwanie stacji...", true);
				
				showTimetable = new Runnable() {
					@Override
					public void run() {
						m_ProgressDialog.dismiss();
			             
						if(destList != null)
				            for(int i = 0; i < destList.getLength(); i++)
				            { 
				            	Node n = destList.item(i);
				            	if(n.getAttributes().getNamedItem("n").getNodeValue().equalsIgnoreCase(titem.station))
				            	{
				            		m = p.matcher(n.getAttributes().getNamedItem("i").getNodeValue());
				            		if(m.matches())
				            			destID	= m.group(1);
				            		
				            		break;
				            	}
				            }
						
						//Mamy oba ID, mozna pobrac rozklad
						if(startID != null && destID != null)
						{
							Intent ni = new Intent(av.getContext(),RouteActivity.class);
							ni.putExtra("startID",startID);
							ni.putExtra("destID",destID);
							ni.putExtra("number",titem.number);
							ni.putExtra("date", titem.date);
							ni.putExtra("time", titem.time);
							ni.putExtra("Type", dep?"dep":"arr");
							
							startActivity(ni);
						}
						else
						{
							//Blad
							Log.e("RozkladPKP","Nie mozna pobrac identyfikatora stacji");
						}
						
					}
				};
				
				Runnable destSearch = new Runnable(){
		            @Override
		            public void run() {
						try {
							destList = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new StationSearch().search(titem.station)).getElementsByTagName("MLc");
							runOnUiThread(showTimetable);
						} catch (Exception e) {
							e.printStackTrace();
						}
		            }
		        };
		        Thread thread =  new Thread(null,destSearch, "DestSearch");
		        thread.start();
		        
		        
				
			}
		});
	}
	
	
	private void getBoard(){
        try{
        	
        	DefaultHttpClient client = new DefaultHttpClient();
			//String url = "http://192.168.1.101/test/dane.php";
			String s = SID.replaceAll("=", "%3D");
			s = s.replaceAll(" ", "%20");
			String time = getIntent().getExtras().getString("Time");
			String date = getIntent().getExtras().getString("Date");
			String prod = getIntent().getExtras().getString("Products");
			String type = dep?"dep":"arr";
			
			Log.i("Sitkol",date);
			
			String data = "L=vs_java3&productsFilter="+prod+"&inputTripelId="+s+"@&maxJourneys=50&boardType="+type+"&time="+time+"&date="+date+"&start=yes";
        	String url  = "http://rozklad.sitkol.pl/bin/stboard.exe/pn" ;
        	
			HttpPost request = new HttpPost(url);
			client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestExpectContinue.class);
	        client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestUserAgent.class);
	        request.addHeader("Content-Type", "text/plain");
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
        	
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        	DocumentBuilder db = factory.newDocumentBuilder();
        	InputSource inStream = new InputSource();
        	inStream.setCharacterStream(new StringReader("<a>"+xmlstring+"</a>"));
        	Document doc = db.parse(inStream);
        	
        	NodeList list = doc.getElementsByTagName("Journey");
            
            m_items.clear();
            
            TimetableItem bi = new TimetableItem();
            String pdate = "";
            int j = list.getLength();
            for(int i = 0; i < j; i++)
            { 
            	TrainItem o = bi.new TrainItem();
            	Node n = list.item(i);
            	o.station 	= n.getAttributes().getNamedItem("targetLoc").getNodeValue();
            	o.time 		= n.getAttributes().getNamedItem("fpTime").getNodeValue();
            	o.date 		= n.getAttributes().getNamedItem("fpDate").getNodeValue();
            	o.delay		= n.getAttributes().getNamedItem("delay").getNodeValue();
            	o.number 	= n.getAttributes().getNamedItem("prod").getNodeValue();
            	
            	if(!pdate.equals(o.date)){
            		DateItem d = bi.new DateItem();
            		d.date = o.date;
            		pdate = o.date;
            		m_items.add(d);
            	}
            	
            	NodeList msgs = n.getChildNodes();
            	for(int k = 0; k < msgs.getLength(); k++)
            	{
            		Node c	= msgs.item(k);
            		if(c.getNodeName().equals("HIMMessage"))
            			o.message += c.getAttributes().getNamedItem("header").getNodeValue();
            	}
            	m_items.add(o);
            }
          } catch (Exception e) { 
            Log.e("BACKGROUND_PROC", e.getMessage());
          }
          runOnUiThread(returnRes);
    }
	
	private void noDataAlert()
	{
		AlertDialog alertDialog;
    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Brak połączeń!");
    	if(dep)
    		alertDialog.setMessage("W wybranym terminie nie odjeżdżają ze stacji żadne pociągi.");
    	else
    		alertDialog.setMessage("W wybranym terminie nie przyjeżdżają do stacji żadne pociągi.");
    	alertDialog.setCancelable(false);
    	
    	alertDialog.setButton("Powrót", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				TimetableActivity.this.finish();
			}
		});
    	alertDialog.show();
	}
	
	private Runnable returnRes = new Runnable() {

        @Override
        public void run() {
            if(m_items != null && m_items.size() > 0)
                m_adapter.notifyDataSetChanged();
            else
            	noDataAlert();
            
            m_ProgressDialog.dismiss();
        }
      };
}

