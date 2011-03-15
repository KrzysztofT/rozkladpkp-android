package org.tyszecki.rozkladpkp;

import java.io.IOException;

import org.tyszecki.rozkladpkp.listeners.PKPLocationListener;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;

public class PKPApplication extends Application{

	LocationManager mLocationManager;
	PKPLocationListener mLocationListener;
	
	@Override
	public void onCreate() {
		setLocationManager();
	}
	
	public String getLocation() throws IOException {
		return mLocationListener.getCurrentAddresss();
	}

	/**
     * Sets all necesary variables for providing location
     */
    private void setLocationManager() {
		// Acquire a reference to the system Location Manager
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		mLocationListener = new PKPLocationListener(this,
				mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
				0, 0, mLocationListener);
	
	}
}
