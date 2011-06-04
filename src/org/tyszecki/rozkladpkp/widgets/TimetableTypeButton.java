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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

public class TimetableTypeButton extends Button {

	boolean dep	= true;
	public TimetableTypeButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setText("Odjazdy");
		
		setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				dep = !dep;
				updateText();
			}
		});
	}
	
	public String getType() {
		return dep ? "dep" : "arr";
	}
	
	public void setType(boolean departures) {
		dep = departures;
		updateText();
	}
	
	public void setType(String t)
	{
		setType(t.equals("dep"));
	}

	private void updateText() {
		setText(dep?"Odjazdy":"Przyjazdy");
		
	}
}
