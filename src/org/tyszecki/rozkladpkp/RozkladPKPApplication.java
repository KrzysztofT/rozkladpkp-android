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

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.tyszecki.rozkladpkp.widgets.StationEdit;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

@ReportsCrashes(formKey = "dFlJOVYyS1hYbENUWEVmQnE5azlKNFE6MQ")

public class RozkladPKPApplication extends Application {
	
	private static Context context;
	private static int themeId = R.style.Theme_RozkladPKP;
	private static String themeSetting;
	@Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        super.onCreate();
        
        context = getApplicationContext();
        reloadTheme();
       
        StationEdit.initTree();
        LocationHelper.init();
    }
	
	public static void reloadTheme()
	{
		themeSetting = PreferenceManager.getDefaultSharedPreferences(context).getString("defaultTheme", "0");
        if(themeSetting.equals("0"))
        	themeId = R.style.Theme_RozkladPKP;
        else
        	themeId = R.style.Theme_RozkladPKP_Dark;
	}
	
	public static Context getAppContext()
	{
		return context;
	}
	
	public static int getThemeId()
	{
		return themeId;
	}
	
	public static String getThemeSetting()
	{
		return themeSetting;
	}
	
}
