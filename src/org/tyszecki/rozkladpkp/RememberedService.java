package org.tyszecki.rozkladpkp;

import java.io.FileOutputStream;

import org.tyszecki.rozkladpkp.PLN.Trip;
import org.tyszecki.rozkladpkp.PLN.TripIterator;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;

public class RememberedService extends IntentService {

	public RememberedService() {
		super("RemeberedService");
	}

	@Override
	protected void onHandleIntent(Intent in) {
		Bundle ex = in.getExtras();
		
		//Do poprawienia jest ogólnie większość rzeczy związana z zapamiętywaniem czasu w PLN.
		//FIXME: Tutaj zakładamy, że hafas zwraca wyniki w strefie czasowej użytkownika, co nie jest prawdą.
		//Z drugiej strony, nie wiadomo w jakiej strefie te wyniki są zwracane.
		String t = null;
		
		String Sid = ex.getString("SID");
		String Zid = null;
		
		if(ex.containsKey("ZID"))
			Zid = ex.getString("ZID");
		
		
		if(ex.containsKey("pln"))
		{
			PLN pln = new PLN(ex.getByteArray("pln"));
			try{
				TripIterator p = pln.tripIterator();
				p.moveToLast();
				Trip t1 = p.next();
				Time time = new Time(t1.date);
				time.monthDay++;
				time.normalize(false);
				t = time.format2445();
				
				String s = CommonUtils.ResultsHash(Sid, Zid, null);
				try {
					FileOutputStream fos = openFileOutput(s, Context.MODE_PRIVATE);
					fos.write(pln.data);
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			catch(Exception e){
				return;
			}
			RememberedManager.addtoHistory(this, Sid, Zid, t);
		}
		else if(ex.containsKey("timetable"))
		{
			t = ex.getString("time");
			
			String s = CommonUtils.ResultsHash(Sid, null, ex.getBoolean("departure"));
			
			try {
				FileOutputStream fos = openFileOutput(s, Context.MODE_PRIVATE);
				fos.write(ex.getByteArray("timetable"));
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			RememberedManager.addtoHistory(this, Sid, ex.getBoolean("departure"), t);
		}
		
		
	}

}
