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
	private static void cleanupHistory(SQLiteDatabase db)
	{
		Cursor cur = db.rawQuery("SELECT _id FROM stored WHERE fav IS null ORDER BY _id DESC LIMIT -1 OFFSET 10", null);
		
		if(cur.getCount() > 0)
		{
			String SQL = "DELETE FROM stored WHERE _id IN (";
			
			while(cur.moveToNext())
			{
				SQL += cur.getString(0);
				if(!cur.isLast())
					SQL += ",";
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
			cleanupHistory(db);	
		}
		db.close();
	}
	
	public static void addtoHistory(Context c, String fromSID, String toSID)
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		
		Cursor cur = db.query("stored", new String[]{"_id"}, "sidFrom=? AND sidTo=?", new String[]{fromSID,toSID}, null, null, null);
		String id = null;
		
		if(cur.moveToNext())
			id = cur.getString(0);
		cur.close();
		
		if(id != null)
			db.execSQL("UPDATE stored SET _id=(SELECT _id+1 FROM stored ORDER BY _id DESC LIMIT 1) WHERE _id="+id);
		else
		{
			ContentValues val = new ContentValues();
			val.put("sidFrom", fromSID);
			val.put("sidTo", toSID);
			val.put("type", 2);
			db.insert("stored", null, val);
			cleanupHistory(db);
		}
		db.close();
	}
	
	public static void saveStation(Context c, String stationSID, boolean departure)
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		
		Cursor cur = db.rawQuery("SELECT _id,fav FROM stored WHERE sidFrom=? AND type=?",new String[]{stationSID,departure?"0":"1"});
		
		cur.moveToNext();
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
	
	public static void saveRoute(Context c, String fromSID, String toSID)
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		
		Cursor cur = db.query("stored", new String[]{"_id,fav"}, "sidFrom=? AND sidTo=?", new String[]{fromSID,toSID}, null, null, null);
		
		cur.moveToNext();
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
