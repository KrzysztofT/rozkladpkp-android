package org.tyszecki.rozkladpkp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class RememberedManager {
	private void cleanupHistory(SQLiteDatabase db)
	{
		
	}
	
	public static void addtoHistory(Context c, String stationSID, boolean departure)
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		ContentValues val = new ContentValues();
		val.put("sid", stationSID);
		val.put("type", departure?0:1);
		
		db.insert("lastQueries", null, val);
		db.close();
	}
	
	public static void addtoHistory(Context c, String fromSID, String toSID)
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		ContentValues val = new ContentValues();
		val.put("sid", fromSID);
		val.put("toSid", toSID);
		val.put("type", 2);
		db.insert("lastQueries", null, val);
		db.close();
	}
	
	public static void saveStation(Context c, String stationSID, boolean departure)
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		ContentValues val = new ContentValues();
		val.put("sid", stationSID);
		val.put("type", departure?0:1);
		
		db.insert("favtimetables", null, val);
		db.close();
	}
	
	public static void saveRoute(Context c, String fromSID, String toSID)
	{
		SQLiteDatabase db = DatabaseHelper.getDbRW(c);
		ContentValues val = new ContentValues();
		val.put("sidFrom", fromSID);
		val.put("sidTo", toSID);
		db.insert("favroutes", null, val);
		db.close();
	}
}
