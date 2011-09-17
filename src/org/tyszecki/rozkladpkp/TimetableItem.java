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
package org.tyszecki.rozkladpkp;

public class TimetableItem {
	
	public static class DateItem extends TimetableItem {
		String date;
	}
	
	public static class TrainItem extends TimetableItem {
		public String time,number,station,delay,type;
		public String date,message="";
	}

	public static class ScrollItem extends TimetableItem {
		public static ScrollItem progressItem()
		{
			ScrollItem si = new ScrollItem();
			si.inProgress = true;
			
			return si;
		}
		public static ScrollItem scrollItem(boolean up)
		{
			ScrollItem si = new ScrollItem();
			si.up = up;
			
			return si;
		}
		public boolean up, inProgress;
	}
	public static class WarningItem extends TimetableItem{}
}
