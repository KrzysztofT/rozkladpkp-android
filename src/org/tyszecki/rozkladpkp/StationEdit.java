package org.tyszecki.rozkladpkp;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.tyszecki.rozkladpkp.R;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class StationEdit extends  AutoCompleteTextView {

	private String[] stationsArr;
	
	private boolean enableAC = true;
	
	
	public StationEdit(Context context, AttributeSet attrs) {
        super(context, attrs); 
        setSingleLine();
        setDropDownHeight(-2);
        
        stationsArr = getResources().getString(R.string.stations).split(",");
        setAdapter(new ArrayAdapter<String>(getContext(), R.layout.stationedititem, stationsArr));  
        
	
	};
	
	public String getCurrentSID()
	{
		String cstation = getText().toString();
		int j = stationsArr.length;
		int i;
		for(i = 0; i < j; i++)
			if(cstation.equals(stationsArr[i]))
			{
				String a = getResources().getString(R.string.stationids).split(",")[i];
				String [] parts = a.split(";");
				
				a = "A=1@O="+cstation+"@X="+parts[0]+"@Y="+parts[1]+"@L="+parts[2]+"@";
				Log.i("RozkladPKP",a);
				return a;
			}
			
		return "";
	}
	
	public void setAutoComplete(boolean en)
	{
		enableAC = en;
	}
	
	public boolean autoComplete()
	{
		return enableAC;
	}
}