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

public class PropertiesButton extends AttributesButton {
	
	public PropertiesButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		items.add(new Attribute("Tylko bezpośrednie", "REQ0JourneyProduct_opt0", "1", false));
		items.add(new Attribute("Wagon sypialny","SW"));
		items.add(new Attribute("Wagon z miejscami do leżenia","LW"));
		items.add(new Attribute("Przewóz rowerów", "bikeEverywhere", "1", false));
		items.add(new Attribute("Przedział dla niepełnosprawnych","97"));	
	}
	
	protected void updateText() {
		
		for(int i = 0; i < items.size(); ++i)
			if(items.get(i).checked)
			{
				super.updateText();
				return;
			}
		
		setText("Wszystkie połączenia");
	}
}
