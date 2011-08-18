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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class CommonUtils {
	
	/*
	 * Sprawdza czy aplikacja ma dostęp do internetu, pokazuje Toast z błędem, jeśli nie ma.
	 */
	public static boolean onlineCheck(Context c)
	{
		return onlineCheck(c, "Nie można wykonać tej operacji - brak połączenia internetowego.");
	}
	
	public static boolean onlineCheck(Context c, String msgError)
	{
		ConnectivityManager cm = (ConnectivityManager)  c.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }

	    Toast.makeText(c.getApplicationContext(), msgError, Toast.LENGTH_SHORT).show();
	    return false;
	}
	
	/*
	 * Zwraca ID drawable'a który odpowiada typowi pociągu, którego numer podano w parametrze
	 */
	
	private static final HashMap<String, Integer> typeDrawables = new HashMap<String, Integer>(){
		
		private static final long serialVersionUID = 1L;
		{
			put("IR", R.drawable.back_ir);
			put("RE", R.drawable.back_re);
			
			for(String a : new String[]{"Fußweg","Übergang"})
				put(a,R.drawable.back_foot);
			
			for(String a : new String[]{"TGV","ES","KDP"}) //;)
				put(a,R.drawable.back_kdp);
			
			for(String a : new String[]{"TLK","D"})
				put(a,R.drawable.back_tlk);
			
			for(String a : new String[]{"EC","EIC", "EN", "EX"})
				put(a,R.drawable.back_ec);
			
			for(String a : new String[]{"SKM","SKW", "WKD"})
				put(a,R.drawable.back_skm);	
			
			for(String a : new String[]{"Bus","Tra", "Metro"})
				put(a,R.drawable.back_bus);
		}
	};
	
	public static int drawableForTrainType(String t)
	{
         if(t != null && t.length() > 0 && typeDrawables.containsKey(t))
        	 return typeDrawables.get(t);
         else
        	 return R.drawable.back_reg;	
	}
	
	public static String trainType(String number)
	{
		if(number.equals("Fußweg") || number.equals("Übergang"))
			return number;
		
        Matcher m = Pattern.compile("([a-zA-Z]*)").matcher(number);   
        return m.find() ? m.group(1) : null;
	}
	
	/*
	 * Metoda zwraca tekst jaki zostanie wyświetlony jako typ pociągu.
	 * Przydaje się do zamiany niemieckiego "Fussweg" i "Ubergang" i usuwania podwojonych spacji.
	 */
	public static String trainDisplayName(String number)
	{
		if(number.equals("Fußweg"))
			return "Pieszo";
		if(number.equals("Übergang"))
			return "Przejście";
		
		else return number.replaceAll("\\s+", " ");
	}
	
	/*
	 * Zwraca miejscowość, na podstawie pamiętanej przez urządzenie lokalizacji,
	 * podczas operacji, pokazuje wiadomość o postępie.
	 * Przekazywanie aktywności może nie jest najelegantsze, ale w obecnym wypadku,
	 * jest to sensowne rozwiązanie.
	 */
	public static void currentLocality(final Activity cx, final LocationResult callback)
	{
		Resources res = cx.getResources();
		final ProgressDialog p = ProgressDialog.show(cx, res.getString(R.string.progressTitle), res.getString(R.string.progressBodyLocation));
		new Thread( new Runnable() {
			
			@Override
			public void run() {
				LocationManager lm = (LocationManager) cx.getSystemService(Context.LOCATION_SERVICE);
				Location l = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				Geocoder c = new Geocoder(cx);
				try {
					List<Address> addresses = c.getFromLocation(l.getLatitude(), l.getLongitude(), 1);
					callback.gotLocality(addresses.get(0).getLocality());
				} catch (Exception e) {
					callback.gotLocality(null);
				} 
				//TODO: Czy to wywołanie może w ogóle zawieść? 
				cx.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
							p.dismiss();
					}
				});
			}
		}).start();
	}
	
	public static abstract class LocationResult{
        public abstract void gotLocality(String s);
    }
	
	public static abstract class StationIDfromNameProgress{
		public abstract void downloadStarted();
		public abstract void finished(String ID);
	}
	
	public static String StationIDfromName(final String name, final StationIDfromNameProgress prog) throws IllegalStateException, SAXException, IOException, ParserConfigurationException
	{
		SQLiteDatabase db =  DatabaseHelper.getDb(RozkladPKPApplication.getAppContext());
        Cursor cur = db.query("stations", new String[]{"_id"}, "name = ?", new String[]{name}, null, null, null);
        
        String res = "";
        if(cur.moveToNext())
        	res = cur.getString(0);
        
        db.close();
        
        if(prog != null)
        {
        	if(res.equals(""))
        	{
        		prog.downloadStarted();
        		
        		new Thread(new Runnable() {
					
					@Override
					public void run() {
						NodeList destList = null;
						try {
							destList = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new StationSearch().search(name)).getElementsByTagName("MLc");
						} catch (Exception e) {} 
						
		        		if(destList != null)
		        		{
		        			for(int i = 0; i < destList.getLength(); i++)
				            { 
				            	Node n = destList.item(i);
				            	if(n.getAttributes().getNamedItem("n").getNodeValue().equalsIgnoreCase(name))
				            	{
				            		String ID = StationIDfromSID(n.getAttributes().getNamedItem("i").getNodeValue());
				            		prog.finished(ID);
				            		return;
				            	}
				            }
		        		}
		        		prog.finished(null);
					}
				}).start();
        	}
        	else
        		prog.finished(res);
        }
        return res;
	}
	
	public static String StationIDfromSID(String ID)
	{
		for(String t: ID.split("@"))
			if(t.startsWith("L="))
				try{
					return Integer.toString((Integer.parseInt(t.split("=")[1])));
				}catch(Exception e){}
				
		return null;
	}
	
	public static String SIDfromStationID(int ID, String name)
	{
		SQLiteDatabase db =  DatabaseHelper.getDb(RozkladPKPApplication.getAppContext());
        Cursor cur = db.query("stations", new String[]{"_id","x","y"}, "_id = "+ID, null, null, null, null,"1");
		
        if(cur.moveToNext())
        {
        	db.close();
        	return "A=1@O="+name+"@X="+cur.getInt(1)+"@Y="+cur.getInt(2)+"@L="+Integer.toString(cur.getInt(0))+"@";
        }
        db.close();
        return "";
	}
	
	//Metoda używana do wygenerowania nazwy pliku z wynikami
	public static String ResultsHash(String stationFrom, String stationTo, Boolean departure)
	{
		StringBuilder b = new StringBuilder();
		
		b.append((stationFrom != null) ? stationFrom : "");
		b.append((stationTo != null) ? stationTo : "");
		b.append((departure != null) ? ((departure) ? "-1" : "-2") : "");
		
		return b.toString();
	}
	 private final static Map<Character,Character> chmap = new HashMap<Character,Character>(){
	 		private static final long serialVersionUID = 1L;
	 		{
	 			  put('ą','a');
	 			  put('ć','c');
	 			  put('ę','e');
	 			  put('ł','l');
	 			  put('ń','n');
	 			  put('ó','o');
	 			  put('ś','s');
	 			  put('ż','z');
	 			  put('ź','z');
	 		  }
	 		};
	 	
	     private static char strip(char in)
	     {
	    	 in = Character.toLowerCase(in);
	    	 if(chmap.containsKey(in))
	    		 in = chmap.get(in);
	    	 return in;
	     }
	     
	     public static String depol(String t)
	     {
	    	 String r = "";
	    	 for(int i = 0; i < t.length(); ++i)
	    		 r += strip(t.charAt(i));
	    	 
	    	 return r;
	     }
}
