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

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ActionBar.Tab;
import android.support.v4.view.ViewPager;

public class RozkladPKP extends FragmentActivity  {
    
    ViewPager  mViewPager;
    TabsAdapter mTabsAdapter;
    
    /** Called when the activity is first created. */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	setContentView(R.layout.actionbar_tabs_pager);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        
        
        ActionBar.Tab tab1 = getSupportActionBar().newTab().setText("Zapamiętane");
        ActionBar.Tab tab2 = getSupportActionBar().newTab().setText("Połączenia");
        ActionBar.Tab tab3 = getSupportActionBar().newTab().setText("Rozkłady");

        mViewPager = (ViewPager)findViewById(R.id.pager);
        mTabsAdapter = new TabsAdapter(this, getSupportActionBar(), mViewPager);
        mTabsAdapter.addTab(tab1, RememberedFragment.class);
        mTabsAdapter.addTab(tab2, ConnectionsFormFragment.class);
        mTabsAdapter.addTab(tab3, TimetableFormFragment.class);
        //mTabsAdapter.addTab(tab2, ConnectionsFormActivity.class);
        //mTabsAdapter.addTab(tab3, TimetableFormActivity.class);

        if (savedInstanceState != null) {
        	getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt("index"));
        }
        
    	
    	
        /*Resources res = getResources(); // Resource object to get Drawables
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
        	
        tabHost.setCurrentTab(t);*/
    }
    public static class TabsAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener, ActionBar.TabListener {
        private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<String> mTabs = new ArrayList<String>();

        public TabsAdapter(FragmentActivity activity, ActionBar actionBar, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mActionBar = actionBar;
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(ActionBar.Tab tab, Class<?> clss) {
            mTabs.add(clss.getName());
            mActionBar.addTab(tab.setTabListener(this));
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            return Fragment.instantiate(mContext, mTabs.get(position), null);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

    	@Override
    	public void onTabSelected(Tab tab, FragmentTransaction ft) {
    		mViewPager.setCurrentItem(tab.getPosition());
    	}

    	@Override
    	public void onTabReselected(Tab tab, FragmentTransaction ft) {
    	}

    	@Override
    	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    	}
    }
}
