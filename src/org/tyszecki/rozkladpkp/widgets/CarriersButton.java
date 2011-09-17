package org.tyszecki.rozkladpkp.widgets;

import java.util.ArrayList;

import org.tyszecki.rozkladpkp.SerializableNameValuePair;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;

public class CarriersButton extends AttributesButton {

	private class Attribute{
		public Attribute(String name, String code)
		{
			this.name = name;
			this.code = code;
		}
		String name;
		String code = null;
	};
	
	
	final int ATTR_CNT = 9;
	boolean[] p = new boolean[ATTR_CNT];
	boolean[] dial = new boolean[ATTR_CNT];
	
	ArrayList<Attribute> items;
	CharSequence[] titles;
	
	
	public CarriersButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		items = new ArrayList<Attribute>();
		
		items.add(new Attribute("PKP Intercity", "P1"));
		items.add(new Attribute("Przewozy Regionalne","P2"));
		items.add(new Attribute("Koleje Mazowieckie","P3"));
		items.add(new Attribute("Koleje Dolnośląskie", "P4"));
		items.add(new Attribute("PKP SKM Trójmiasto","P5"));
		items.add(new Attribute("WKD","P6"));
		items.add(new Attribute("Arriva RP","P7"));
		items.add(new Attribute("SKM Warszawa","P8"));
		items.add(new Attribute("Koleje Wielkopolskie","P9"));
		
		titles = new CharSequence[items.size()];
		for(int i = 0; i < items.size(); ++i)
			titles[i] = items.get(i).name;
		
		selectAll();
	}
	
	public Dialog getDialog() {
		

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Szczegóły połączenia");
		
		for(int i = 0; i < ATTR_CNT; i++)
			dial[i] = p[i];
		
		builder.setMultiChoiceItems(titles, p, new OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
				dial[arg1] = arg2;
			}
		});	
		
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   for(int i = 0; i < ATTR_CNT; i++)
	       				p[i] = dial[i];
	        	   updateText();
	        	   dialog.cancel();
	           }
	       })
	       .setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                dialog.dismiss();
	           }
	       });
		return builder.create();
	}

	public void setParameters(ArrayList<SerializableNameValuePair> list)
	{	
		for(SerializableNameValuePair pair : list)
		{
			for(int j = 0; j < ATTR_CNT; ++j)
				if(items.get(j).code.equals(pair.getValue()))
					p[j] = false;
		}
		updateText();
	}
	
	public void selectAll()
	{
		for(int i = 0; i < ATTR_CNT; ++i)
			p[i] = true;
		updateText();
	}
	
	public ArrayList<SerializableNameValuePair> getParameters()
	{
		ArrayList<SerializableNameValuePair> ret = new ArrayList<SerializableNameValuePair>();
		
		for(int i = 0; i < ATTR_CNT; ++i)
			if(!p[i] && items.get(i).code != null)
				ret.add(new SerializableNameValuePair("REQ0HafasAttrExc", items.get(i).code));
		
		return ret;
	}

	private void updateText() {
		ArrayList<String> l = new ArrayList<String>();
		
		for(int i = 0; i < ATTR_CNT; ++i)
			if(p[i])
				l.add(items.get(i).name);
		
		setText(l.size() == 9?"Wszyscy przewoźnicy":join(l,","));
	}
	
	public int settingsCode()
	{
		int s = 0;
		int f = 1;
		for(int i = 0; i < ATTR_CNT; ++i)
		{
			if(p[i])
				s += f;
			f = f << 1;
		}
		return s;
	}
	
	public void readSettings(int code)
	{
		for(int i = 0; i < ATTR_CNT; ++i)
		{
			p[i] = (code % 2 == 1);
			code /= 2;	
		}
		updateText();
	}
}
