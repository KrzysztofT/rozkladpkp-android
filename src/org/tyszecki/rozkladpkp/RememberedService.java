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
		
		Log.i("RozkladPKP","IntentService startuje");
		//Do poprawienia jest ogólnie większość rzeczy związana z zapamiętywaniem czasu w PLN.
		//FIXME: Tutaj zakładamy, że hafas zwraca wyniki w strefie czasowej użytkownika, co nie jest prawdą.
		//Z drugiej strony, nie wiadomo w jakiej strefie te wyniki są zwracane.
		Time time = new Time();
		String t = null;
		
		String Sid = ex.getString("SID");
		String Zid = ex.getString("ZID");
		
		
		if(ex.containsKey("pln"))
		{
			PLN pln = new PLN(ex.getByteArray("pln"));
			try{
				TripIterator p = pln.tripIterator();
				p.moveToLast();
				Trip t1 = p.next();
				
				String r[] = t1.date.split("\\.");
				String u[] = t1.con.getTrain(0).deptime.toString().split(":");
				String jt[] = t1.con.getJourneyTime().toString().split(":");
				
				
				time.set(0, Integer.parseInt(u[1]), ((Integer.parseInt(u[0])+23)%24)+1, Integer.parseInt(r[0]), Integer.parseInt(r[1])-1, Integer.parseInt(r[2]));
				time.hour += Integer.parseInt(jt[0])+3;
				time.minute += Integer.parseInt(jt[1]);
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
		}
		RememberedManager.addtoHistory(this, Sid, Zid, t);
	}

}
