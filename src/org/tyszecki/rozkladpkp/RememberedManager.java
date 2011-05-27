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
		Cursor cur = db.rawQuery("SELECT _id FROM lastqueries ORDER BY _id DESC LIMIT -1 OFFSET 10", null);
		
		if(cur.getCount() > 0)
		{
			String SQL = "DELETE FROM lastqueries WHERE _id IN (";
			
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
		
		Cursor cur = db.query("lastqueries", new String[]{"_id"}, "sid=? AND type=?", new String[]{stationSID,departure?"0":"1"}, null, null, null);
		
		String id = null;
		
		if(cur.moveToNext())
			id = cur.getString(0);
		cur.close();
		
		if(id != null)
		{
			Log.i("RozkladPKP","Zmiana ID"+id);
			db.execSQL("UPDATE lastqueries SET _id=(SELECT _id+1 FROM lastqueries ORDER BY _id DESC LIMIT 1) WHERE _id="+id);
		}
		else
		{
			Log.i("RozkladPKP","Nowa pozycja");
			ContentValues val = new ContentValues();
			val.put("sid", stationSID);
			val.put("type", departure?0:1);
			
			db.insert("lastQueries", null, val);
			cleanupHistory(db);	
		}
		db.close();
	}
	
	public static void addtoHistory(Context c, String fromSID, String toSID)
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		
		Cursor cur = db.query("lastqueries", new String[]{"_id"}, "sid=? AND toSid=?", new String[]{fromSID,toSID}, null, null, null);
		String id = null;
		
		if(cur.moveToNext())
			id = cur.getString(0);
		cur.close();
		
		if(id != null)
		{
			Log.i("RozkladPKP","Zmiana ID"+id);
			db.execSQL("UPDATE lastqueries SET _id=(SELECT _id+1 FROM lastqueries ORDER BY _id DESC LIMIT 1) WHERE _id="+id);
		}
		else
		{
			ContentValues val = new ContentValues();
			val.put("sid", fromSID);
			val.put("toSid", toSID);
			val.put("type", 2);
			db.insert("lastQueries", null, val);
			cleanupHistory(db);
		}
		db.close();
	}
	
	public static void saveStation(Context c, String stationSID, boolean departure)
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		
		Cursor cur = db.query("favtimetables", new String[]{"_id"}, "sid=? AND type=?", new String[]{stationSID,departure?"0":"1"}, null, null, null);
		
		boolean add = cur.getCount() == 0;
		cur.close();
		
		if(add)
		{
			ContentValues val = new ContentValues();
			val.put("sid", stationSID);
			val.put("type", departure?0:1);
			
			db.insert("favtimetables", null, val);
		}
		Toast.makeText(c, add?"Stację dodano do zapamiętanych":"Stacja już jest na liście zapamiętanych", Toast.LENGTH_SHORT).show();
		db.close();
	}
	
	public static void saveRoute(Context c, String fromSID, String toSID)
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		
		Cursor cur = db.query("favroutes", new String[]{"_id"}, "sidFrom=? AND sidTo=?", new String[]{fromSID,toSID}, null, null, null);
		
		boolean add = cur.getCount() == 0;
		cur.close();
		
		if(add)
		{
			ContentValues val = new ContentValues();
			val.put("sidFrom", fromSID);
			val.put("sidTo", toSID);
			db.insert("favroutes", null, val);
		}
		Toast.makeText(c, add?"Trasę dodano do zapamiętanych":"Trasa już jest na liście zapamiętanych", Toast.LENGTH_SHORT).show();
		db.close();
	}
}
