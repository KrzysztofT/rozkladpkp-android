package org.tyszecki.rozkladpkp;

import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationHelper {
	public enum LocationState {Unavailable, Pending, Ready};
	private static LocationHelper instance = null;
	
	public static void init()
	{
		if(instance == null)
			instance = new LocationHelper();
	}
	
	private static LocationState state;
	private static String location = null;
	
	private LocationHelper()
	{
		state = LocationState.Pending;
		try{
		LocationManager lm = (LocationManager) RozkladPKPApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 2000, new LocationListener() {
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			
			@Override
			public void onProviderEnabled(String provider) {}
			
			@Override
			public void onProviderDisabled(String provider) {
				state = LocationState.Unavailable;
			}
			
			@Override
			public void onLocationChanged(Location loc) {
				Geocoder c = new Geocoder(RozkladPKPApplication.getAppContext());
				
				try {
					List<Address> addresses = c.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
					location = addresses.get(0).getLocality();
					state = LocationState.Ready;
					
				} catch (Exception e) {} 
			}

		});
		}catch (Exception e) {
			state = LocationState.Unavailable;
		}
	}
	
	static LocationState getLocationState()
	{
		return state;
	}
	
	static String getLocation()
	{
		return location;
	}
}
