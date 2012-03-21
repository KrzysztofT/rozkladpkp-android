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

import org.tyszecki.rozkladpkp.pln.PLN;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class RememberedManager {
	/*
	 * Metoda usuwa najstarsze elementy, tak aby było ich co najwyżej 10
	 */
	private static void cleanupHistory(SQLiteDatabase db, Context c)
	{
		//TODO: Usuwanie zapamiętanych odjazdów/przyjazdów
		
		Cursor cur = db.rawQuery("SELECT _id, sidFrom, sidTo, type FROM stored WHERE fav IS null ORDER BY _id DESC LIMIT -1 OFFSET 10", null);
		
		if(cur.getCount() > 0)
		{
			String SQL = "DELETE FROM stored WHERE _id IN (";
			
			while(cur.moveToNext())
			{
				SQL += cur.getString(0);
				if(!cur.isLast())
					SQL += ",";
				
				if(cur.getInt(3) == 2)
					c.deleteFile(CommonUtils.ResultsHash(cur.getString(1), cur.getString(2), null, 0));
			}
			SQL += ")";
			
			db.execSQL(SQL);
		}
		cur.close();
	}
	
	public static void removeCache(Context c, String sidFrom, String sidTo, int cacheID)
	{
		try{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		if(db != null)
			db.execSQL("UPDATE stored SET cacheValid='' WHERE sidFrom='"+sidFrom+"' AND sidTo='"+sidTo+"'");
		}catch (Exception e) {}
		
		c.deleteFile(CommonUtils.ResultsHash(sidFrom, sidTo, null, cacheID));
	}
	//Przy dodawaniu do hstorii, należy sprawdzić czy już nie ma takiej pozycji.
	//Jeśli jest, zwiększamy jej ID na największe w tabeli, dzięki czemu powędruje na górę listy
	public static void addtoHistory(Context c, String stationSID, boolean departure, String cValid, int cacheID)
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		
		String type = Integer.toString(((cacheID > 0) ? cacheID : 0)*10 + (departure ? 0 : 1));
		
		Cursor cur = db.query("stored", new String[]{"_id"}, "sidFrom=? AND type=?", new String[]{stationSID,type}, null, null, null);
		
		String id = null;
		
		if(cur.moveToNext())
			id = cur.getString(0);
		cur.close();
		
		if(id != null)
			db.execSQL("UPDATE stored SET _id=(SELECT _id+1 FROM stored ORDER BY _id DESC LIMIT 1)"+((cValid != null)?", cacheValid='"+cValid+"'":"")+" WHERE _id="+id);
		
		else
		{
			ContentValues val = new ContentValues();
			val.put("sidFrom", stationSID);
			val.put("type", type);
			val.put("cacheValid", cValid);
			
			db.insert("stored", null, val);
			cleanupHistory(db,c);	
		}
		db.close();
	}
	
	public static void addtoHistory(Context c, String fromSID, String toSID, String cValid, int cacheID)
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		
		String type = Integer.toString(((cacheID > 0) ? cacheID : 0)*10 + 2);
		
		Cursor cur = db.query("stored", new String[]{"_id"}, "sidFrom=? AND sidTo=? AND type=?", new String[]{fromSID,toSID,type}, null, null, null);
		String id = null;
		
		if(cur.moveToNext())
			id = cur.getString(0);
		cur.close();
		
		if(id != null)
			db.execSQL("UPDATE stored SET _id=(SELECT _id+1 FROM stored ORDER BY _id DESC LIMIT 1)"+((cValid != null)?", cacheValid='"+cValid+"'":"")+" WHERE _id="+id);
		else
		{
			ContentValues val = new ContentValues();
			val.put("sidFrom", fromSID);
			val.put("sidTo", toSID);
			val.put("type", type);
			val.put("cacheValid", cValid);
			db.insert("stored", null, val);
			cleanupHistory(db,c);
		}
		db.close();
	}
	
	public static String cacheValidTime(Context c, String fromSID, String toSID, int cacheID)
	{
		if(fromSID == null || toSID == null)
			return "";
		
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		
		String p1 = (cacheID > 0) ? Integer.toString(cacheID*10+2) : "2";
		String p2 = (cacheID > 0) ? Integer.toString(cacheID*10+2) : "3";
		
		Cursor cur = db.query("stored", new String[]{"cacheValid"}, "sidFrom=? AND sidTo=? AND type IN (?,?)", new String[]{fromSID,toSID,p1,p2}, null, null, null);
		String t = null;
		
		if(cur.moveToNext())
			t = cur.getString(0);
		cur.close();
		db.close();
		
		return t;
	}
	
	public static void saveStation(Context c, String stationSID, boolean departure)
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		
		Cursor cur = db.rawQuery("SELECT _id,fav FROM stored WHERE sidFrom=? AND type=?",new String[]{stationSID,departure?"0":"1"});
		
		if(cur.moveToNext())
		{
			boolean add = cur.getInt(1) != 1;
			int id = cur.getInt(0);
			cur.close();
			
			if(add)
			{
				ContentValues val = new ContentValues();
				val.put("fav", 1);
				db.update("stored", val, "_id="+Integer.toString(id),null);
			}
			Toast.makeText(c, add?"Stację dodano do zapamiętanych":"Stacja już jest na liście zapamiętanych", Toast.LENGTH_SHORT).show();
			db.close();
		}
	}
	
	public static void saveRoute(Context c, String fromSID, String toSID)
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		
		Cursor cur = db.query("stored", new String[]{"_id,fav"}, "sidFrom=? AND sidTo=?", new String[]{fromSID,toSID}, null, null, null);
		
		if(cur.moveToNext())
		{
			boolean add = cur.getInt(1) != 1;
			int id = cur.getInt(0);
			cur.close();

			if(add)
			{
				ContentValues val = new ContentValues();
				val.put("fav", 1);
				db.update("stored", val, "_id="+Integer.toString(id),null);
			}
			Toast.makeText(c, add?"Trasę dodano do zapamiętanych":"Trasa już jest na liście zapamiętanych", Toast.LENGTH_SHORT).show();
			db.close();
		}
	}
	
	public static String getCacheString(PLN pln)
	{
		try{
			Time result = new Time(pln.edate);
			result.allDay = false;
			result.monthDay++;
			result.normalize(false);
			
			return result.format2445();
		}
		catch(Exception e){}
		return null;
	}
}
