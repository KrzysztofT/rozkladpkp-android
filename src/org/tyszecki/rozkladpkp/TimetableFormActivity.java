package org.tyszecki.rozkladpkp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

public class TimetableFormActivity extends FragmentActivity {
	final int VIEW_ID = 0x1234;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(RozkladPKPApplication.getThemeId());
		super.onCreate(savedInstanceState);
		
		FrameLayout frame = new FrameLayout(this);
        frame.setId(VIEW_ID);
        setContentView(frame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Przeglądanie rozkładów");
        
        
        if (savedInstanceState == null) {
            Fragment newFragment = new TimetableFormFragment();
            newFragment.setArguments(getIntent().getExtras());
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(VIEW_ID, newFragment).commit();
        }
	}
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	        	finish();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
