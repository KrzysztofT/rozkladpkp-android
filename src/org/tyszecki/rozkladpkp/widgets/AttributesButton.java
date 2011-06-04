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

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;

import org.tyszecki.rozkladpkp.SerializableNameValuePair;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.Button;

public class AttributesButton extends Button {

	public static String join(AbstractCollection<String> s, String delimiter) {
	    if (s.isEmpty()) return "";
	    Iterator<String> iter = s.iterator();
	    StringBuffer buffer = new StringBuffer(iter.next());
	    while (iter.hasNext()) buffer.append(delimiter).append(iter.next());
	    return buffer.toString();
	}
	
	private class Attribute{
		public Attribute(String name, String code)
		{
			this.name = name;
			this.code = code;
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

	public void setParameters(ArrayList<SerializableNameValuePair> list)
	{	
		for(SerializableNameValuePair pair : list)
		{
			if(pair.getName().contains("Product_opt"))
				p[0] = true;
			else if(pair.getName().contains("bike"))
				p[3] = true;
			//FIXME: Hash tu musi być...
			else
				for(int j = 0; j < ATTR_CNT; ++j)
					if(items.get(j).code != null && items.get(j).code.compareTo(pair.getValue()) == 0)
						p[j] = true;
		}
		updateText();
	}
	
	public void deselectAll()
	{
		for(int i = 0; i < ATTR_CNT; ++i)
			p[i] = false;
		updateText();
	}
	
	public ArrayList<SerializableNameValuePair> getParameters()
	{
		ArrayList<SerializableNameValuePair> ret = new ArrayList<SerializableNameValuePair>();
		if(p[0])
			ret.add(new SerializableNameValuePair("REQ0JourneyProduct_opt_section_0_list","1:100000"));
		if(p[3])
			ret.add(new SerializableNameValuePair("bikeEverywhere","1"));
		for(int i = 0; i < ATTR_CNT; ++i)
			if(p[i] && items.get(i).code != null)
				ret.add(new SerializableNameValuePair("REQ0HafasAttrInc", items.get(i).code));
		
		return ret;
	}

	private void updateText() {
		ArrayList<String> l = new ArrayList<String>();
		
		for(int i = 0; i < ATTR_CNT; ++i)
			if(p[i])
				l.add(items.get(i).name);
		
		setText(l.size() == 0?"Wszystkie połączenia":join(l,","));
		
		//setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.bezp), null, null, null);
	}
}
