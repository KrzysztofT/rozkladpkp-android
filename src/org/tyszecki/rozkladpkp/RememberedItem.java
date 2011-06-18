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

public class RememberedItem {
	
	public enum TimetableType {Departure, Arrival};
	
	int id;
	
	public static class HeaderItem extends RememberedItem {
		String text;
	}
	
	public static class RouteItem extends RememberedItem {
		int SIDFrom,SIDTo;
		String fromName, toName;
		String cacheValid;
	}
	
	public static class TimetableItem extends RememberedItem {
		TimetableType type;
		int SID;
		String name,cacheValid;
	}	
}
