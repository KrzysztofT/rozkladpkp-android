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

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class RozkladPKP extends TabActivity {
    TabHost tabHost;
    SharedPreferences sp;
    
    /** Called when the activity is first created. */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	
    	
        Resources res = getResources(); // Resource object to get Drawables
        tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Reusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, RememberedActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("remembered").setIndicator("Zapamiętane",
                          res.getDrawable(R.drawable.ic_love))
                      .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, ConnectionsFormActivity.class);
        spec = tabHost.newTabSpec("trips").setIndicator("Połączenia",
                          res.getDrawable(R.drawable.ic_menu_change_order))
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, TimetableFormActivity.class);
        spec = tabHost.newTabSpec("boards").setIndicator("Rozkłady",
                          res.getDrawable(R.drawable.ic_menu_show_list))
                      .setContent(intent);
        tabHost.addTab(spec);
        
        tabHost.setOnTabChangedListener( new OnTabChangeListener() {
			
			@Override
			public void onTabChanged(String tabId) {
					int cur = 1;
					
					if(tabId.equals("remembered"))cur=0;
					if(tabId.equals("trips"))cur=1;
					if(tabId.equals("boards"))cur=2;
					
					sp.edit().putInt("lastSelectedTab", cur).commit();
				
			}
		});

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        
        int t = Integer.parseInt(sp.getString("defaultTab", "-1"));
        if(t == -1)
        	t = sp.getInt("lastSelectedTab", 1); 
        	
        tabHost.setCurrentTab(t);
    }
}
