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

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.Button;

public class ProductsButton extends Button {

	//KDP,Ex,D,Os,Bus,Tram,Sub
	boolean[] p = new boolean[7];
	boolean[] dial = new boolean[7];
	
	final CharSequence[] items = {"Koleje dużej prędkości (KDP)", "Ekspresowe (EC,IC,EIC,Ex)", "Pospieszne (TLK,IR)","Osobowe","Autobusy","Tramwaje","Metro"};
	public ProductsButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public Dialog getDialog() {
		

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Środki transportu");
		
		for(int i = 0; i < 7; i++)
			dial[i] = p[i];
		
		builder.setMultiChoiceItems(items, p, new OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
				dial[arg1] = arg2;
			}
		});	
		
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   for(int i = 0; i < 7; i++)
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

	public void setProductString(String s)
	{
		for(int i = 0; i < 7; i++)
			p[i]	= (s.charAt(i) == '1');
		
		updateText();
	}
	
	public String getProductString()
	{
		String t = "";
		for(int i = 0; i < 7; i++)
			t	+= (p[i])? '1' : '0';
		
		t	+= "1111111";
		return t;
	}

	private void updateText() {
		if(p[0] && p[1] && p[2] && p[3] && p[4] && p[5] && p[6])
				setText("Wszystkie środki transportu");
		else if(p[0] && p[1] && p[2] && p[3] && !p[4] && !p[5] && !p[6])
				setText("Wszystkie pociągi");
		else
		{
			ArrayList<String> sel = new ArrayList<String>();
			
			if(p[0])sel.add("KDP");
			if(p[1])sel.add("Ekspresowe");
			if(p[2])sel.add("Pospieszne");
			if(p[3])sel.add("Osobowe");
			if(p[4])sel.add("Autobusy");
			if(p[5])sel.add("Tramwaje");
			if(p[6])sel.add("Metro");
			
			int j = sel.size();
			if(j == 1)
				setText("Tylko " + sel.get(0));
			else
			{
				String t = "";
				for(int i = 0; i < j; i++)
				{
					t+=sel.get(i);
					if(i == j-2)
						t += " i ";
					else if(i < j-1)
						t += ", ";
				}
				setText(t);
			}
		}
	}
}
