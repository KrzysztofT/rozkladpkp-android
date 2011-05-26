package org.tyszecki.rozkladpkp;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class RozkladPKP extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);

        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
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

        tabHost.setCurrentTab(1);
    }
}