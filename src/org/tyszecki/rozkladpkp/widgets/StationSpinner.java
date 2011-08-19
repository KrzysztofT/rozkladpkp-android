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
package org.tyszecki.rozkladpkp.widgets;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.tyszecki.rozkladpkp.CommonUtils;
import org.tyszecki.rozkladpkp.DatabaseHelper;
import org.tyszecki.rozkladpkp.StationSearch;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class StationSpinner extends Spinner {

	public interface onDataLoaded{
		void dataLoaded();
	}
	
	private String station;
	private Handler acHandler;
	private String[][] stations;
	private StationSpinner s = this;
	private ProgressDialog pdialog = null;
	private onDataLoaded callback = null;
	
	public StationSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		acHandler	= new Handler();
		
	}

	public void setProgressDialog(ProgressDialog dial)
	{
		pdialog	= dial;
	}
	
	public void setOnDataLoaded(onDataLoaded dl)
	{
		callback = dl;
	}
	
	public void setUserInput(String s)
	{
		station = s;
		Updater u	= new Updater();		
		u.start();
	}
	
	public void setUserInput(String s, String id)
	{
		stations = new String[1][2];
		stations[0][0] = s;
		stations[0][1] = id;
		acHandler.post(showUpdate);
		if(pdialog != null && pdialog.isShowing()) pdialog.dismiss();
        if(callback != null)callback.dataLoaded();
	}

	
	public String getCurrentSID()
	{
		//FIXME: Komuś tutaj scrashowało :)
		return stations[getSelectedItemPosition()][1];
	}
	
	public int getStationCount()
	{
		return stations.length;
	}
	
	public String getText()
	{
		return stations[getSelectedItemPosition()][0];
	}
	
	public void saveInDatabase()
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(getContext());
		
		try{
			ContentValues val = new ContentValues();
			
			val.put("_id",CommonUtils.StationIDfromSID(getCurrentSID()));
			val.put("name", getText());
			
			db.insert("stations", null, val);
		}
		catch (Exception e) {
			//TODO: Dowiedzieć się czemu tutaj wywala.
		}
		db.close();
	}
	
	private Runnable showUpdate = new Runnable(){
		
	    public void run(){
	    	ArrayAdapter<String> a = new ArrayAdapter<String>(s.getContext(), android.R.layout.simple_spinner_item);
	    	for(int i = 0; i < stations.length; i++)
	    			a.add(stations[i][0]);

	    	a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    	s.setAdapter(a);
	    	
	    }
	};
	
	private class Updater extends Thread
	{
		public void run()
		{
	        try {
	            InputStream inputStream = new StationSearch().search(station);
	       
	            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	            DocumentBuilder db = dbf.newDocumentBuilder();
	            Document doc = db.parse(inputStream);
	            
	            NodeList list = doc.getElementsByTagName("MLc");
	            
	            int j = list.getLength();
	            stations = new String[j][2];
	            
	            for(int i = 0; i < j; i++)
	            { 
	            	Node n = list.item(i);
	            	stations[i][0] = n.getAttributes().getNamedItem("n").getNodeValue();
	            	stations[i][1] = n.getAttributes().getNamedItem("i").getNodeValue();
	            }

	            acHandler.post(showUpdate);
	            if(pdialog != null && pdialog.isShowing()) pdialog.dismiss();
	            if(callback != null)callback.dataLoaded();
	        } 
	        catch (IOException e) {
	        	//throw new Exception("Problem communicating with API", e);
	        	if(pdialog != null && pdialog.isShowing()) pdialog.dismiss();
	        } catch (ParserConfigurationException e) {
				e.printStackTrace();
				if(pdialog != null && pdialog.isShowing()) pdialog.dismiss();
			} catch (SAXException e) {
				e.printStackTrace();
				if(pdialog != null && pdialog.isShowing()) pdialog.dismiss();
			}
			
		}
	};
}
