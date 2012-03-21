package org.tyszecki.rozkladpkp;

import org.tyszecki.rozkladpkp.ConnectionsFormFragment.onFormSubmitListener;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

public class ConnectionsWidgetConfigActivity extends FragmentActivity implements onFormSubmitListener {

	final int VIEW_ID = 0x1234;
	private int appWidgetId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(RozkladPKPApplication.getThemeId());
		super.onCreate(savedInstanceState);
		
		
		
		FrameLayout frame = new FrameLayout(this);
        frame.setId(VIEW_ID);
        setContentView(frame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Wybierz połączenie");
        
        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
        	appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        	Intent cancelResultValue = new Intent();
        	cancelResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        	setResult(RESULT_CANCELED, cancelResultValue);
        }
        else
        	finish();
        
        if (savedInstanceState == null) {
            Fragment newFragment = new ConnectionsFormFragment();
            extras.putBoolean("hideTime", true);
            newFragment.setArguments(extras);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(VIEW_ID, newFragment).commit();
        }
	}

	@Override
	public void onSubmit(Bundle values) {
		//final Context context = ConnectionsWidgetConfigActivity.this;
		//AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		if(values.containsKey("clarify"))
		{
			Fragment newFragment = new ConnectionsFormFragment();
            newFragment.setArguments(values);
            
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(VIEW_ID, newFragment).commit();
		}
		else
		{
			Editor ed = getSharedPreferences("ConnectionWidget", Context.MODE_PRIVATE).edit();
			String id = Integer.toString(appWidgetId);
			
			for (String i : new String[]{"depName", "arrName", "viaName", "SID", "ZID", "VID1", "Products"/*, "Attributes"*/})
				if(values.containsKey(i))
					ed.putString(i+id, values.getString(i));
			
			ed.commit();
			ConnectionsWidget.update(AppWidgetManager.getInstance(ConnectionsWidgetConfigActivity.this), ConnectionsWidgetConfigActivity.this, appWidgetId, false);
			
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
		}
	}
}
