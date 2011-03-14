package org.tyszecki.rozkladpkp.listeners;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class PKPLocationListener implements LocationListener{
	private Location mCurrentLocation;
	private Context mContext;
	
	public PKPLocationListener(Context context, Location currentLocation) {
		this.mContext = context;
		mCurrentLocation = currentLocation;
	}
	
	@Override
	public void onLocationChanged(Location location) {
		if(mCurrentLocation == null) {
			mCurrentLocation = location;
		} else {
			if(isBetterLocation(location, mCurrentLocation)) {
				mCurrentLocation = location;
			}
		}
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}

	public Location getCurrentLocation() {
		return mCurrentLocation;
	}
	/**
	 * Function which returns current location 
	 * @return String with patern "City, Address"
	 * @throws IOException
	 */
	public String getCurrentAddresss() throws IOException {
		if(mCurrentLocation != null) {
			Geocoder gc = new Geocoder(mContext);
			List<Address> addresses = gc.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
			String city = "";
			if(addresses.get(0).getAddressLine(1) != null &&
					addresses.get(0).getAddressLine(1) != "" ) {
				String[] cityAdd = (addresses.get(0).getAddressLine(1)).split(" ");
				if(cityAdd.length ==2) {
					city = cityAdd[1];
				}
			}
			if(!city.equals("")) {
				if(addresses.get(0).getAddressLine(0) != null)
					city += ", " + addresses.get(0).getAddressLine(0);
				return city;
			} else {
				return null;
			}
			
		} 
		return null;
	}
	public void setCurrentLocation(Location loc) {
		mCurrentLocation = loc;
	}
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}

}
