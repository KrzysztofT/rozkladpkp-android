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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    static SharedPreferences sp = null;
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
        
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        
        if (savedInstanceState == null) {
            int t = Integer.parseInt(sp.getString("defaultTab", "-1"));
            if(t == -1)
            	t = sp.getInt("lastSelectedTab", 1);
        	getSupportActionBar().setSelectedNavigationItem(t);
        }
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
    @Override
    protected void onPause() {
    	super.onPause();
    	if(sp != null)
			sp.edit().putInt("lastSelectedTab", getSupportActionBar().getSelectedNavigationIndex()).commit();
    }
}
