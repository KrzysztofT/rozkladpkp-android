package org.tyszecki.rozkladpkp;

import org.tyszecki.rozkladpkp.widgets.DateButton;
import org.tyszecki.rozkladpkp.widgets.ProductsButton;
import org.tyszecki.rozkladpkp.widgets.StationEdit;
import org.tyszecki.rozkladpkp.widgets.StationSpinner;
import org.tyszecki.rozkladpkp.widgets.TimeButton;
import org.tyszecki.rozkladpkp.widgets.TimetableTypeButton;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class TimetableFormActivity extends Activity {
	
	SharedPreferences pref;
	
	TimeButton timeb;
	DateButton dateb;
	ProductsButton prodb;

	private Resources res;
	
	/*
	 * Zwraca wartość true, jeśli obecnie należy wyświetlić formularz precyzujący
	 * (z listą stacji zamiast pola edycji) 
	 */
	private boolean clarifyingForm()
	{
		Bundle e = getIntent().getExtras();
		if(e != null)
			return e.containsKey("userText");
		return false;
	}
	
	/*
	 * Metoda zwraca Stringa zapisanego w Extras.
	 * TODO: DRY (metoda jest powtórzona w ConnectionsFormActivity) 
	 */
	private String safeExtras(String key)
	{
		Bundle e = getIntent().getExtras();
		if(e != null && e.containsKey(key))
			return e.getString(key);
		return null;
	}
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Czy potrzeba sprecyzować nazwę stacji?
        final boolean clarify = clarifyingForm();
        
        setContentView(clarify ? R.layout.timetable_form_clarify : R.layout.timetable_form);
        res = getResources();
        pref = getPreferences(MODE_PRIVATE);
        
        if(!clarify)
        {
        	StationEdit autoComplete = (StationEdit)  findViewById(R.id.station_edit);
	        autoComplete.setHint(res.getString(R.string.hintStation));
	        autoComplete.setAutoComplete(pref.getBoolean("EnableStationAC", true));
        }
        else
        {
        	ProgressDialog progressDialog = ProgressDialog.show(TimetableFormActivity.this, 
        			res.getString(R.string.progressTitle), res.getString(R.string.progressSearchingStation), true);
        	
            StationSpinner sp = (StationSpinner)  findViewById(R.id.station_select);
            sp.setProgressDialog(progressDialog);
            sp.setUserInput(getIntent().getExtras().getString("userText"));
        }
        
        String ex;
        
        timeb	= (TimeButton) findViewById(R.id.time_button);
        
        ex 		= safeExtras("Time");
        if(ex == null)
        	timeb.setToNow();
        else
        	timeb.setTime(ex);
        
        timeb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDialog(0);
			}
		});
        
        dateb = (DateButton) findViewById(R.id.date_button);
        
        ex 		= safeExtras("Date");
        if(ex == null)
        	dateb.setToNow();
        else
        	dateb.setDate(ex);
        
        dateb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDialog(1);
			}
		});
        
        prodb = (ProductsButton) findViewById(R.id.products_button);
        prodb.setProductString(pref.getString("Products", "11110001111111"));
        prodb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				showDialog(2);
				
			}
		});
        
        ex 		= safeExtras("Type");
        if(ex == null)
        	ex = "dep";
        
        ((TimetableTypeButton)findViewById(R.id.type_button)).setType(ex);
        
        ((Button) findViewById(R.id.ok_button)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent ni;
				if(!clarify)
				{
					//Nie wprowadzono nazwy stacji w ogóle
					StationEdit autoComplete = (StationEdit)  findViewById(R.id.station_edit);
					if(autoComplete.getText().toString().trim().length() == 0)
					{
						Toast.makeText(getApplicationContext(), res.getString(R.string.toastStationEmpty), Toast.LENGTH_SHORT).show();
						return;
					}
					
					//Wprowadzono coś, dalsze akcje wymagają połączenia internetowego
					if(!CommonUtils.onlineCheck(getBaseContext()))
						return;
					String sid = autoComplete.getCurrentSID();
					
					//Niepełna nazwa, konieczne doprecyzowanie
					if(sid.equals(""))
					{
						ni = new Intent(arg0.getContext(),TimetableFormActivity.class);
						ni.putExtra("userText", autoComplete.getText().toString());
					}
					//Pełna nazwa, można wystartować aktywność rozkładu jazdy
					else
					{
						ni = new Intent(arg0.getContext(),TimetableActivity.class);
						ni.putExtra("SID", sid);
						ni.putExtra("Station", autoComplete.getText().toString());
					}
				}
				else
				{
					//Wybrano stację z dostępnej listy
					ni = new Intent(arg0.getContext(),TimetableActivity.class);
					StationSpinner sp = (StationSpinner)  findViewById(R.id.station_select);
					sp.saveInDatabase();
					
					ni.putExtra("SID", sp.getCurrentSID());
					ni.putExtra("Station", sp.getText());
				}
				
				//Wystartowanie wybranej aktywności
				ni.putExtra("Time", timeb.getTime());
				ni.putExtra("Date", dateb.getDate());
				ni.putExtra("Type", ((TimetableTypeButton)findViewById(R.id.type_button)).getType());
				ni.putExtra("Products", prodb.getProductString());
				
				pref.edit().putString("Products", prodb.getProductString()).commit();
				startActivity(ni);
			}
		});
        
        if(!clarify)
        {
	        ((ImageButton)findViewById(R.id.location_button)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CommonUtils.currentLocality(TimetableFormActivity.this, new CommonUtils.LocationResult() {
						@Override
						public void gotLocality(final String s) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if(s == null)
											Toast.makeText(getApplicationContext(), res.getText(R.string.toastLocationError), Toast.LENGTH_SHORT).show();
										else
										{
											StationEdit ed = (StationEdit) findViewById(R.id.station_edit);
											ed.setText(s);
											final Editable etext = ed.getText();
											final int position = etext.length();
											Selection.setSelection(etext, position);
										}
									}
								});
							}
						}
					);					
				}
			});
        }
	}
	
	
	public boolean onCreateOptionsMenu(Menu menu){
		
		if(clarifyingForm())
			return false;
		
		StationEdit autoComplete = (StationEdit)  findViewById(R.id.station_edit);
		getMenuInflater().inflate(R.menu.timetable_form, menu);
		menu.getItem(0).setTitle(res.getString((autoComplete.autoComplete() ? R.string.menuDisableAC : R.string.menuEnableAC)));
		return true;
	}
	
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case R.id.item01:
			StationEdit ed = (StationEdit)  findViewById(R.id.station_edit);
			boolean ac = ed.autoComplete();
			ed.setAutoComplete(!ac);
			SharedPreferences.Editor e = pref.edit();
			e.putBoolean("EnableStationAC", !ac);
			e.commit();
			return true;
		}
		return false;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    case 0:
	        return timeb.timeDialog();
	    case 1:
	    	return dateb.dateDialog();
	    case 2:
	    	return prodb.getDialog();
	    }
	    return null;
	}
}
