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

	public class Attribute{
		String name;
		String field = null;
		String code = null;
		boolean exclude = false;
		public boolean checked = false;
		
		public Attribute(String name, String code, boolean excludeWhenSelected)
		{
			this.name = name;
			this.code = code;
			exclude = excludeWhenSelected;
		}
		
		public Attribute(String name, String code)
		{
			this(name,code,false);
		}
		
		public Attribute(String name, String field, String code, boolean excludeWhenSelected)
		{
			this(name,code,excludeWhenSelected);
			this.field = field;
		}
		
		public SerializableNameValuePair value()
		{
			if(field != null)
				return new SerializableNameValuePair(field, code);
			else
				return new SerializableNameValuePair(exclude ? "REQ0HafasAttrExc" : "REQ0HafasAttrInc", code);
		}
	};
	
	protected ArrayList<Attribute> items = new ArrayList<Attribute>();

	public AttributesButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public Dialog getDialog() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Szczegóły połączenia");
		
		final boolean[] dial = new boolean[items.size()];
		for(int i = 0; i < items.size(); i++)
			dial[i] = items.get(i).checked;
		
		CharSequence[] titles = new CharSequence[items.size()];
		for(int i = 0; i < items.size(); ++i)
			titles[i] = items.get(i).name;
		
		builder.setMultiChoiceItems(titles, dial, new OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
				dial[arg1] = arg2;
			}
		});	
		
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   for(int i = 0; i < items.size(); i++)
	       				items.get(i).checked = dial[i];
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

	public void setParameters(ArrayList<SerializableNameValuePair> list) {	
		for(SerializableNameValuePair pair : list)
		{
			for(int j = 0; j < items.size(); ++j)
			{
				Attribute a = items.get(j);
	
				if(a.field != null)
				{  
					if(a.field.equals(pair.getName()))
						a.checked = true;
				}
				else if(a.code != null && a.code.equals(pair.getValue()))
					a.checked = true;
			}			
		}
		updateText();
	}

	public ArrayList<SerializableNameValuePair> getParameters() {
		ArrayList<SerializableNameValuePair> ret = new ArrayList<SerializableNameValuePair>();
		
		for(int i = 0; i < items.size(); ++i)
			if(items.get(i).checked)
				ret.add(items.get(i).value());
		
		return ret;
	}

	protected void updateText() {
		ArrayList<String> l = new ArrayList<String>();
		
		for(int i = 0; i < items.size(); ++i)
			if(items.get(i).checked)
				l.add(items.get(i).name);
		
		setText(l.size() == 0?"Wszystkie połączenia":join(l,","));
	}

	public int settingsCode() {
		int s = 0;
		int f = 1;
		for(int i = 0; i < items.size(); ++i)
		{
			if(items.get(i).checked)
				s += f;
			f = f << 1;
		}
		return s;
	}

	public void readSettings(int code) {
		for(int i = 0; i < items.size(); ++i)
		{
			items.get(i).checked = (code % 2 == 1);
			code /= 2;	
		}
		updateText();
	}

}