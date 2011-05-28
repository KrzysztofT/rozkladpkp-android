package org.tyszecki.rozkladpkp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper{

	//The Android's default system path of your application database.
	private static String DB_PATH = "/data/data/org.tyszecki.rozkladpkp/databases/";

	private static String DB_NAME = "rozkladpkp";

	private final static int DB_VERSION = 2;
	
	private SQLiteDatabase myDataBase; 

	private final Context myContext;

	/**
	 * Constructor
	 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
	 * @param context
	 */
	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.myContext = context;
		
		/*try {
			File sd = Environment.getExternalStorageDirectory();
			File data = Environment.getDataDirectory();

			if (sd.canWrite()) {
				File currentDB = new File(DB_PATH+DB_NAME);
				File backupDB = new File(sd, "rozklad.db");

				if (currentDB.exists()) {
					FileChannel src = new FileInputStream(currentDB).getChannel();
					FileChannel dst = new FileOutputStream(backupDB).getChannel();
					dst.transferFrom(src, 0, src.size());
					src.close();		
					dst.close();
				}
			}
			} catch (Exception e) {}*/
	}	

	//TODO: Ograniczenie liczby wywołań openDatabase.
	
	/**
	 * Creates a empty database on the system and rewrites it with your own database.
	 * */
	public void createDataBase() throws IOException{

		boolean dbExist = checkDataBase();
		
		if(dbExist){
			//do nothing - database already exist
		}else{
			//By calling this method and empty database will be created into the default system path
			//of your application so we are gonna be able to overwrite that database with our database.
			this.getReadableDatabase().close();
			try {
				copyDataBase();
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}

	}

	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase(){

		SQLiteDatabase checkDB = null;

		try{
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);

		}catch(SQLiteException e){
			//database does't exist yet.
		}

		if(checkDB != null){
			//getWriteableDatabase nie wywoła onUpgrade kiedy wersja starej bazy będzie równa 0
			if(DB_VERSION > checkDB.getVersion())
			{
				myUpgrade(checkDB, checkDB.getVersion(), DB_VERSION);
				checkDB.setVersion(DB_VERSION);
			}
			checkDB.close(); 
		}

		return checkDB != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created empty database in the
	 * system folder, from where it can be accessed and handled.
	 * This is done by transfering bytestream.
	 * */
	
	private void copyDataBase() throws IOException{
		Log.i("RozkladPKP","kopiowanie!");
		//Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		//Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		//transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer))>0){
			myOutput.write(buffer, 0, length);
		}

		//Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
		
		myCreate();

	}

	public SQLiteDatabase openDataBase(int mode){
		try {
			createDataBase();
			String myPath = DB_PATH + DB_NAME;
			myDataBase = SQLiteDatabase.openDatabase(myPath, null, mode);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return myDataBase;
	}
	
	public static SQLiteDatabase getDbRW(Context c)
	{
		DatabaseHelper d = new DatabaseHelper(c);
		return d.openDataBase(SQLiteDatabase.OPEN_READWRITE);
	}

	public static SQLiteDatabase getDb(Context c)
	{
		DatabaseHelper d = new DatabaseHelper(c);
		return d.openDataBase(SQLiteDatabase.OPEN_READONLY);
	}
	@Override
	public synchronized void close() {

		if(myDataBase != null)
			myDataBase.close();

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
	
	/*
	 * Te metody utworzono, aby ominąć wewnętrzny mechanizm SQLiteOpenHelpera. 
	 * Metoda onCreate zostałaby wywołana przed skopiowaniem bazy z katalogu assets,
	 * co sprawiłoby że zmiany przez nią wprowadzone zostałyby cofnięte. 
	 */
	public void myCreate() {
		Log.i("RozkladPKP","myCreate called!");
		SQLiteDatabase db = SQLiteDatabase.openDatabase(DB_PATH+DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
		createFavTables(db);
		
		db.setVersion(DB_VERSION);
		db.close();
	}
	
	public void myUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i("RozkladPKP","myUpgrade called!");
		if(oldVersion < 2)
			createFavTables(db);
	}
	
	private void createFavTables(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE 'stored' ('_id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,'sidFrom' INTEGER NOT NULL,'sidTo' INTEGER,'type' INTEGER,'fav' INTEGER);");
		db.execSQL("CREATE VIEW storedview AS SELECT stored._id AS _id,sidFrom,sidTo,s.name AS fromName,stations.name AS toName,type,fav FROM stored LEFT JOIN stations AS s on s._id=sidFrom LEFT join stations on stations._id=sidTo ORDER BY stored._id DESC");
	}

}