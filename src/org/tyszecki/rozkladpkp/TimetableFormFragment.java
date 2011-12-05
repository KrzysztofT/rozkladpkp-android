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

import org.tyszecki.rozkladpkp.widgets.DateButton;
import org.tyszecki.rozkladpkp.widgets.DialogControl;
import org.tyszecki.rozkladpkp.widgets.ProductsButton;
import org.tyszecki.rozkladpkp.widgets.StationEdit;
import org.tyszecki.rozkladpkp.widgets.StationSpinner;
import org.tyszecki.rozkladpkp.widgets.TimeButton;
import org.tyszecki.rozkladpkp.widgets.TimetableTypeButton;
import org.tyszecki.rozkladpkp.widgets.StationSpinner.onDataLoaded;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class TimetableFormFragment extends Fragment {
	
	SharedPreferences pref;
	
	TimeButton timeButton;
	DateButton dateButton;
	ProductsButton productsButton;
	StationEdit stationEdit;
	TimetableTypeButton typeButton;
	StationSpinner stationSelect;

	private boolean clarify;
	private Resources res;

	private ProgressDialog progressDialog;

	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(clarify ? R.layout.timetable_form_clarify : R.layout.timetable_form, null);
	}
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
        Bundle e = getArguments();
    	if(e != null)
    		clarify = e.containsKey("clarify");
    	else
    		clarify = false;
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        res = getResources();
        initializeGui();
        
        pref = getActivity().getPreferences(Activity.MODE_PRIVATE);
        
        ((Button) getView().findViewById(R.id.ok_button)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				Bundle arg = new Bundle();
				if(!clarify)
				{
					//Nie wprowadzono nazwy stacji w ogóle
					if(stationEdit.getText().toString().trim().length() == 0)
					{
						Toast.makeText(RozkladPKPApplication.getAppContext(), res.getString(R.string.toastStationEmpty), Toast.LENGTH_SHORT).show();
						return;
					}
					
					//Wprowadzono coś, dalsze akcje wymagają połączenia internetowego
					if(!CommonUtils.onlineCheck())
						return;
					String sid = stationEdit.getCurrentSID();
					
					//Niepełna nazwa, konieczne doprecyzowanie
					if(sid.equals(""))
					{
						arg.putString("userText", stationEdit.getText().toString());
						arg.putBoolean("clarify", true);
					}
					//Pełna nazwa, można wystartować aktywność rozkładu jazdy
					else
					{
						arg.putString("SID", sid);
						arg.putString("Station", stationEdit.getText().toString());
					}
				}
				else
				{
					stationSelect.saveInDatabase();
					
					arg.putString("SID", stationSelect.getCurrentSID());
					arg.putString("Station", stationSelect.getText());
				}
				
				//Wystartowanie wybranej aktywności
				arg.putString("Time", timeButton.getTime());
				arg.putString("Date", dateButton.getDate());
				arg.putString("Type", typeButton.getType());
				arg.putString("Products", productsButton.getProductString());
				
				pref.edit().putString("Products", productsButton.getProductString()).commit();
				
				Intent intent = new Intent(getActivity(),  arg.containsKey("clarify") ? TimetableFormActivity.class : TimetableActivity.class);
				intent.putExtras(arg);
				startActivity(intent);
			}
		});
        
        /*if(!clarify)
        {
	        ((ImageButton)findViewById(R.id.location_button)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					(new GetLocality()).execute();			
				}
			});
        }*/
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.timetable_form, menu);
	}

	
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case R.id.item_settings:
			Intent ni = new Intent(getActivity(),PreferencesActivity.class);
			startActivity(ni);
			return true;
		}
		return false;
	}
	
	private void initializeGui() {
		setHasOptionsMenu(true);
		
		findControls();
		initializeControls();
		setupListeners();
	}


	

	private void findControls() {
		View main = getView();
		
		timeButton	= (TimeButton) main.findViewById(R.id.time_button);
        dateButton = (DateButton) main.findViewById(R.id.date_button);
        productsButton = (ProductsButton) main.findViewById(R.id.products_button);
        typeButton = (TimetableTypeButton) main.findViewById(R.id.type_button);
        
        if(!clarify)
	        stationEdit = (StationEdit) main.findViewById(R.id.station_edit);
        else
        	stationSelect = (StationSpinner) getView().findViewById(R.id.station_select);
	}
	
	private void initializeControls() {
		EnhancedBundle a = new EnhancedBundle(getArguments());
		SharedPreferences p = getActivity().getPreferences(Activity.MODE_PRIVATE);
		
		timeButton.setTime(a.getString("Time", null));
		dateButton.setDate(a.getString("Date", null));
        productsButton.setProductString(p.getString("Products", "11110001111111"));
        typeButton.setType(a.getString("Type", "dep"));
        
	}
	
	private void setupListeners() {
		// TODO Auto-generated method stub
		ButtonListener onClick = new ButtonListener();
		
		timeButton.setOnClickListener(onClick);
		dateButton.setOnClickListener(onClick);
		productsButton.setOnClickListener(onClick);
		
		EnhancedBundle a = new EnhancedBundle(getArguments());
		if(clarify)
		{
			onDataLoaded dataLoaded = new SpinnerOnDataLoaded();
			
			stationSelect.setOnDataLoaded(dataLoaded);
			
			progressDialog = ProgressDialog.show(getActivity(), res.getString(R.string.progressTitle), res.getString(R.string.progressSearchingStation), true);
			stationSelect.setUserInput(a.getString("userText"));
		}
		else
		{
			stationEdit.setHint(res.getString(R.string.hintStation));
			if(a.containsKey("Station"))
	        {
	        	stationEdit.setText(a.getString("Station"));
	        	timeButton.forceFocus();
	        }
		}
	}
	
	private class ButtonListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);

			DialogFragment newFragment = ((DialogControl)v).getDialog();
			newFragment.show(ft, "dialog");
		}
	}
	
	private class SpinnerOnDataLoaded implements StationSpinner.onDataLoaded
	{

		@Override
		public void dataLoaded() {
			progressDialog.dismiss();
			if(stationSelect.getStationCount() == 0)
			{
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						AlertDialog alertDialog;
    			    	alertDialog = new AlertDialog.Builder(getActivity()).create();
    			    	alertDialog.setTitle("Błąd wyszukiwania!");
    			    	alertDialog.setMessage("Nie można odnaleźć wskazanej stacji.");
    			    	alertDialog.setCancelable(false);
    			    	alertDialog.setOnKeyListener(CommonUtils.getOnlyDPadListener());
    			    	
    			    	alertDialog.setButton("Powrót", new DialogInterface.OnClickListener() {
    						public void onClick(DialogInterface arg0, int arg1) {
    							getActivity().finish();
    						}
    					});
    			    	alertDialog.show();		
					}
				});
			}
		}
		
	}
	/*private class GetLocality extends CommonUtils.GetLocalityTask{
		ProgressDialog p;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			p = ProgressDialog.show(TimetableFormActivity.this, res.getString(R.string.progressTitle), res.getString(R.string.progressBodyLocation));
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			p.dismiss();

			if(result == null)
				Toast.makeText(getApplicationContext(), res.getText(R.string.toastLocationError), Toast.LENGTH_SHORT).show();
			else
			{
				StationEdit ed = (StationEdit) findViewById(R.id.station_edit);
				ed.setText(result);
				final Editable etext = ed.getText();
				final int position = etext.length();
				Selection.setSelection(etext, position);
			}
		}
	}*/
}
