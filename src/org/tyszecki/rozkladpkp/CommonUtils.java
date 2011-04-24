package org.tyszecki.rozkladpkp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class CommonUtils {
	
	/*
	 * Sprawdza czy aplikacja ma dostęp do internetu, pokazuje Toast z błędem, jeśli nie ma.
	 */
	public static boolean onlineCheck(Context c)
	{
		ConnectivityManager cm = (ConnectivityManager)  c.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }

	    Toast.makeText(c.getApplicationContext(), "Nie można wykonać tej operacji - brak połączenia internetowego.", Toast.LENGTH_SHORT).show();
	    return false;
	}
	
	/*
	 * Zwraca ID drawable'a który odpowiada typowi pociągu, którego numer podano w parametrze
	 */
	public static int drawableForTrainType(String t)
	{
         if(t != null && t.length() > 0)
         {              	
         	if(t.equals("TLK") || t.equals("D"))
         		return R.drawable.back_tlk;
         	
         	else if(t.equals("EC") || t.equals("EIC") || t.equals("EN"))
         		return R.drawable.back_ec;
         	
         	else if(t.equals("IR"))
         		return R.drawable.back_ir;
         	
         	else if(t.equals("RE"))
         		return R.drawable.back_re;
         	
         	else if(t.equals("SKM") || t.equals("SKW") || t.equals("WKD"))
         		return R.drawable.back_skm;		
         }
         
         return R.drawable.back_reg;	
	}
	
	public static String trainType(String number)
	{
        Matcher m = Pattern.compile("([a-zA-Z]*)").matcher(number);   
        return m.find() ? m.group(1) : null;
	}
}
