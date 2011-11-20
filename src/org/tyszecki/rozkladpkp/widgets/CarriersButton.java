package org.tyszecki.rozkladpkp.widgets;

import java.util.ArrayList;

import org.tyszecki.rozkladpkp.SerializableNameValuePair;

import android.content.Context;
import android.util.AttributeSet;

public class CarriersButton extends AttributesButton {

	
	public CarriersButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		items.add(new Attribute("PKP Intercity", "P1", true));
		items.add(new Attribute("Przewozy Regionalne","P2", true));
		items.add(new Attribute("Koleje Mazowieckie","P3", true));
		items.add(new Attribute("Koleje Dolnośląskie", "P4", true));
		items.add(new Attribute("PKP SKM Trójmiasto","P5", true));
		items.add(new Attribute("WKD","P6", true));
		items.add(new Attribute("Arriva RP","P7", true));
		items.add(new Attribute("SKM Warszawa","P8", true));
		items.add(new Attribute("Koleje Wielkopolskie","P9", true));
		items.add(new Attribute("Koleje Śląskie","P10", true));
		
		selectAll();
	}
	
	public void selectAll()
	{
		for(int i = 0; i < items.size(); ++i)
			items.get(i).checked = true;
		updateText();
	}

	public ArrayList<SerializableNameValuePair> getParameters() {
		ArrayList<SerializableNameValuePair> ret = new ArrayList<SerializableNameValuePair>();
		
		for(int i = 0; i < items.size(); ++i)
			if(!items.get(i).checked)
				ret.add(items.get(i).value());
		
		return ret;
	}
	
	public void setParameters(ArrayList<SerializableNameValuePair> list) {	
		for(SerializableNameValuePair pair : list)
		{
			for(int j = 0; j < items.size(); ++j)
			{
				Attribute a = items.get(j);
	
				if(a.code != null && a.code.equals(pair.getValue()))
					a.checked = false;
			}			
		}
		updateText();
	}
	
	protected void updateText() {
		
		for(int i = 0; i < items.size(); ++i)
			if(!items.get(i).checked)
			{
				super.updateText();
				return;
			}
		
		setText("Wszyscy przewoźnicy");
	}
}
