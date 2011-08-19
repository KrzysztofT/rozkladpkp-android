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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
					c.deleteFile(CommonUtils.ResultsHash(cur.getString(1), cur.getString(2), null));
			}
			SQL += ")";
			
			db.execSQL(SQL);
		}
		cur.close();
	}
	
	//Przy dodawaniu do hstorii, należy sprawdzić czy już nie ma takiej pozycji.
	//Jeśli jest, zwiększamy jej ID na największe w tabeli, dzięki czemu powędruje na górę listy
	public static void addtoHistory(Context c, String stationSID, boolean departure)
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		
		Cursor cur = db.query("stored", new String[]{"_id"}, "sidFrom=? AND type=?", new String[]{stationSID,departure?"0":"1"}, null, null, null);
		
		String id = null;
		
		if(cur.moveToNext())
			id = cur.getString(0);
		cur.close();
		
		if(id != null)
			db.execSQL("UPDATE stored SET _id=(SELECT _id+1 FROM stored ORDER BY _id DESC LIMIT 1) WHERE _id="+id);
		
		else
		{
			ContentValues val = new ContentValues();
			val.put("sidFrom", stationSID);
			val.put("type", departure?0:1);
			
			db.insert("stored", null, val);
			cleanupHistory(db,c);	
		}
		db.close();
	}
	
	public static void addtoHistory(Context c, String fromSID, String toSID, String cValid)
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		
		Cursor cur = db.query("stored", new String[]{"_id"}, "sidFrom=? AND sidTo=?", new String[]{fromSID,toSID}, null, null, null);
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
			val.put("type", 2);
			val.put("cacheValid", cValid);
			db.insert("stored", null, val);
			cleanupHistory(db,c);
		}
		db.close();
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
}
