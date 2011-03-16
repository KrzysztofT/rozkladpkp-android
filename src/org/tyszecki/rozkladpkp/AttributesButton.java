package org.tyszecki.rozkladpkp;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

public class AttributesButton extends Button {

	private class Attribute{
		public Attribute(String name, String code)
		{
			this.name = name;
			this.code = code;
		}
		public String getString(boolean enabled)
		{
			if(enabled)
				return "REQ0HafasAttrInc="+code;
			else
				return "REQ0HafasAttrExc="+code;
		}
		String name;
		String code = null;
	};
	
	
	final int ATTR_CNT = 5;
	boolean[] p = new boolean[ATTR_CNT];
	boolean[] dial = new boolean[ATTR_CNT];
	
	ArrayList<Attribute> items;
	CharSequence[] titles;
	//final CharSequence[] items = {"Tylko bezpośrednie", "Wagon sypialny", "Wagon z miejscami do leżenia","Przewóz rowerów","Przedział dla niepełnosprawnych","Bez obowiązku rejestracji"};
	
	public static String combine(ArrayList<String> s, String glue)
	{
		if (s == null)
			return "[null]";
		int k = s.size();
		
		if (k==0)
			return null;
		
		StringBuilder out = new StringBuilder();
		out.append(s.get(0));
		
		for (int x=1;x<k;++x)
			out.append(glue).append(s.get(x));
		
		return out.toString();
	}
	
	public AttributesButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		items = new ArrayList<Attribute>();
		
		items.add(new Attribute("Tylko bezpośrednie", null));
		items.add(new Attribute("Wagon sypialny","SW"));
		items.add(new Attribute("Wagon z miejscami do leżenia","LW"));
		items.add(new Attribute("Przewóz rowerów", null));
		items.add(new Attribute("Przedział dla niepełnosprawnych","97"));
		
		titles = new CharSequence[items.size()];
		for(int i = 0; i < items.size(); ++i)
			titles[i] = items.get(i).name;
		
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

	public void setRequestString(String s)
	{
		String[] arr = s.split("&");
		
		for(int i = 0; i < arr.length; ++i)
		{
			if(arr[i].contains("Product_opt"))
				p[0] = true;
			else if(arr[i].contains("bike"))
				p[3] = true;
			
			//FIXME: Hash tu musi być...
			else
			{
				String[] f = arr[i].split("=");
				for(int j = 0; j < ATTR_CNT; ++j)
					if(f.length > 1 && items.get(j).code != null && items.get(j).code.compareTo(f[1]) == 0)
						p[j] = true;
			}
		}
		
		updateText();
	}
	
	public String getRequestString()
	{
		ArrayList<String> list = new ArrayList<String>();
		
		if(p[0])
			list.add("REQ0JourneyProduct_opt_section_0_list=1:100000");
		if(p[3])
			list.add("bikeEverywhere=1");
		
		for(int i = 0; i < ATTR_CNT; ++i)
			if(p[i] && items.get(i).code != null)
				list.add(items.get(i).getString(true));		
		
		return ""+combine(list, "&");
	}

	private void updateText() {
		String t = "";
		for(int i = 0; i < ATTR_CNT; ++i)
			t+=p[i]?"1":"0";
		setText(t);
		Log.i("RozkladPKP",getRequestString());
	}
}
