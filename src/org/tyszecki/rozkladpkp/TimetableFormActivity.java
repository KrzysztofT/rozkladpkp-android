package org.tyszecki.rozkladpkp;

import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
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
	        autoComplete.setHint("Stacja kolejowa");
	        autoComplete.setAutoComplete(pref.getBoolean("EnableStationAC", true));
        }
        else
        {
        	ProgressDialog progressDialog = ProgressDialog.show(TimetableFormActivity.this, "Czekaj...", "Wyszukiwanie stacji...", true);
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
						Toast.makeText(getApplicationContext(), "Wprowadź nazwę stacji", Toast.LENGTH_SHORT).show();
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
					new GetLocationTask().execute();
				}
			});
        }
	}
	
	
	public boolean onCreateOptionsMenu(Menu menu){
		
		if(clarifyingForm())
			return false;
		
		StationEdit autoComplete = (StationEdit)  findViewById(R.id.station_edit);
		getMenuInflater().inflate(R.menu.timetable, menu);
		menu.getItem(0).setTitle((autoComplete.autoComplete() ? "Wyłącz" : "Włącz") + " autouzupełnianie");
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
	
	
	private class GetLocationTask extends AsyncTask<Void, Void, String> {
		
		private ProgressDialog mProgress;
		
		@Override
		protected void onPostExecute(String result) {
			StationEdit ed = (StationEdit)  findViewById(R.id.station_edit);
			mProgress.dismiss();
			if(result == null || result == "") {
				Toast.makeText(TimetableFormActivity.this, 
						res.getText(R.string.toastLocationError), 
						Toast.LENGTH_LONG)
						.show();
			} else {
				ed.setText(result);
				//set cursor at end of text
				final Editable etext = ed.getText();
				final int position = etext.length();  // end of buffer, for instance
				Selection.setSelection(etext, position);
			}
		}


		@Override
		protected void onPreExecute() {
			mProgress = ProgressDialog.show(TimetableFormActivity.this, 
					res.getText(R.string.progressTitle), 
					res.getText(R.string.progressBodyLocation));
		}


		@Override
		protected String doInBackground(Void... params) {
			try {
				return ((PKPApplication)getApplication()).getLocation();
			} catch (IOException e) {
				return null;
			}
		}
		
	}
	
}
