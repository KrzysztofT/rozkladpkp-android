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

import org.tyszecki.rozkladpkp.R;
import org.tyszecki.rozkladpkp.LocationHelper.LocationState;
import org.tyszecki.rozkladpkp.widgets.AttributesButton;
import org.tyszecki.rozkladpkp.widgets.CarriersButton;
import org.tyszecki.rozkladpkp.widgets.DateButton;
import org.tyszecki.rozkladpkp.widgets.DialogControl;
import org.tyszecki.rozkladpkp.widgets.ProductsButton;
import org.tyszecki.rozkladpkp.widgets.StationEdit;
import org.tyszecki.rozkladpkp.widgets.StationSpinner;
import org.tyszecki.rozkladpkp.widgets.TimeButton;
import org.tyszecki.rozkladpkp.widgets.StationSpinner.onDataLoaded;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class ConnectionsFormFragment extends Fragment {
      
	private TimeButton timeButton;
	private DateButton dateButton;
	private ProductsButton productsButton;
	private AttributesButton attributesButton;
	private CarriersButton carriersButton;
	
	
	private StationEdit depEdit,arrEdit,viaEdit;
	private StationSpinner depSelect, arrSelect,viaSelect;
	private boolean clarify;
	private Resources res;
	private int loading;
	
	private ProgressDialog progressDialog;
	//private GetLocality task;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		return inflater.inflate(clarify ? R.layout.connection_form_clarify : R.layout.connection_form, null);
	}
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	Bundle e = getArguments();
    	if(e != null)
    		clarify = e.containsKey("clarify");
    	else
    		clarify = false;
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		res = getResources();
        initializeGui();
	}

	private void initializeGui() {
		
		setHasOptionsMenu(true);
		loading = 2;
		
		findControls();
		initializeControls();
		setupListeners();
		
		
		
        ((Button) getView().findViewById(R.id.ok_button)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent ni = null;
				
				Bundle arg = new Bundle();
				
				if(!clarify)
				{
					if(!validInput()) return;
					
					String sidd = depEdit.getCurrentSID();
					String sida = arrEdit.getCurrentSID();	
					String sidv = viaEdit.isShown() ? viaEdit.getCurrentSID() : null;
					
					arg.putString("depName", depEdit.getText().toString());
					arg.putString("arrName", arrEdit.getText().toString());
					
					//Trzeba doprecyzować nazwę co najmargej jednej stacji
					if(sidd.equals("") || sida.equals("") || (sidv != null && sidv.equals("")))
					{
						//arg = new Intent(arg0.getContext(),ConnectionsFormActivity.class);
						arg.putBoolean("clarify", true);
						if(!sidd.equals("")) arg.putString("SID", sidd);
						if(!sida.equals("")) arg.putString("ZID", sida);
						
						if(sidv != null)
						{
							if(!sidv.equals("")) arg.putString("VID1", sidv);
							arg.putString("viaName", viaEdit.getText().toString());
						}
					}
					//Wpisano dwie takie same stacje
					else if(sida.equals(sidd) || sida.equals(sidv) || sidd.equals(sidv))
					{
						Toast.makeText(RozkladPKPApplication.getAppContext(), res.getText(R.string.toastSameStationsError), Toast.LENGTH_SHORT).show();
							return;
					}
					//argc arge trzeba doprecyzowywać
					else
					{
						//arg = new Intent(arg0.getContext(),ConnectionListActivity.class);
						arg.putString("ZID", sida);
						arg.putString("SID", sidd);
						
						if(sidv != null) arg.putString("VID1", sidv);
					}
				}
				else
				{
					depSelect.saveInDatabase();
		        	arrSelect.saveInDatabase();
		        	
		        	if(viaSelect != null) viaSelect.saveInDatabase();
		        	
					//Wybrano dwie takie same stacje z listy
					if(arrSelect.getCurrentSID().equals(depSelect.getCurrentSID()))
					{
						Toast.makeText(RozkladPKPApplication.getAppContext(), res.getText(R.string.toastSameStationsError), Toast.LENGTH_SHORT).show();
							return;
					}
					
					//arg = new Intent(arg0.getContext(),ConnectionListActivity.class);
					
					arg.putString("ZID", arrSelect.getCurrentSID());
					arg.putString("SID", depSelect.getCurrentSID());
					
					if(viaSelect != null) arg.putString("VID1", viaSelect.getCurrentSID());
					
					arg.putString("depName", depSelect.getText());
					arg.putString("arrName", arrSelect.getText());
				}
				
				arg.putString("Time", timeButton.getTime());
				arg.putString("Date", dateButton.getDate());
				arg.putString("Products", productsButton.getProductString());
				arg.putSerializable("Attributes", attributesButton.getParameters());
				
				if(carriersButton.isShown()) arg.putSerializable("Carriers", carriersButton.getParameters());
				
				SharedPreferences p = getActivity().getPreferences(Activity.MODE_PRIVATE);
				p.edit().putString("Products", productsButton.getProductString()).putInt("Attributes", attributesButton.settingsCode()).commit();
				
				Intent intent = new Intent(getActivity(),  arg.containsKey("clarify") ? ConnectionsFormActivity.class : ConnectionListActivity.class);
				intent.putExtras(arg);
				startActivity(intent);
			}
		});
        
        if(!clarify)
        {
	        ((ImageButton) getView().findViewById(R.id.location_button)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					LocationState state = LocationHelper.getLocationState();
					if(state == LocationState.Unavailable)
						Toast.makeText(RozkladPKPApplication.getAppContext(), res.getText(R.string.toastLocationError), Toast.LENGTH_SHORT).show();
					else if(state == LocationState.Ready)
						depEdit.setText(LocationHelper.getLocation());
				}
			});
        }
	}

	
	private void findControls() {
		View main = getView();
		
		timeButton	= (TimeButton) main.findViewById(R.id.time_button);
        dateButton = (DateButton) main.findViewById(R.id.date_button);
        productsButton = (ProductsButton) main.findViewById(R.id.products_button);
        attributesButton = (AttributesButton) main.findViewById(R.id.attributes_button);
        carriersButton = (CarriersButton) main.findViewById(R.id.carriers_button);
        
        if(!clarify)
        {
	        depEdit = (StationEdit) main.findViewById(R.id.departure_edit);
	        arrEdit = (StationEdit) main.findViewById(R.id.arrival_edit);
	        viaEdit = (StationEdit) main.findViewById(R.id.via_edit);
        }
        else
        {
        	depSelect = (StationSpinner) getView().findViewById(R.id.departure_select);
        	arrSelect = (StationSpinner) getView().findViewById(R.id.arrival_select);
        	
        	if(getArguments() != null && getArguments().containsKey("viaText"))
        		viaSelect = (StationSpinner) getView().findViewById(R.id.via_select);
        }
	}
	
	@SuppressWarnings("unchecked")
	private void initializeControls()
	{
		EnhancedBundle a = new EnhancedBundle(getArguments());
		SharedPreferences p = getActivity().getPreferences(Activity.MODE_PRIVATE);
			
		//Przyciski
		timeButton.setTime(a.getString("Time", null));
		dateButton.setDate(a.getString("Date", null));
        productsButton.setProductString(p.getString("Products", "11110001111111"));
        
        if(a.containsKey("Attributes"))
        	attributesButton.setParameters((ArrayList<SerializableNameValuePair>) a.getSerializable("Attributes"));
        else 
        	attributesButton.readSettings(p.getInt("Attributes", 0));
        
		
        if(a.containsKey("Carriers"))
        {
        	carriersButton.setVisibility(View.VISIBLE);
        	carriersButton.setParameters((ArrayList<SerializableNameValuePair>) a.getSerializable("Carriers"));
        }
        
        //Pola
        if(!clarify)
        {
	        depEdit.setHint(res.getText(R.string.hintDepartureStation));
	        arrEdit.setHint(res.getText(R.string.hintArrivalStation));
	        viaEdit.setHint(res.getText(R.string.hintViaStation));
	        
	        String arr = a.getString("arrName", null);
	        String dep = a.getString("depName", null);
            
	        if(arr != null) arrEdit.setText(arr);
            if(dep != null) depEdit.setText(dep);
            
            if(arr == null && dep != null) arrEdit.requestFocus();
            else if(arr != null && dep != null) timeButton.forceFocus();
        }
        else
        {
        	if(a.containsKey("viaText"))
        		viaSelect.setVisibility(View.VISIBLE);
        }
        
	}
	
	private void setupListeners()
	{
		ButtonListener onClick = new ButtonListener();
		
		timeButton.setOnClickListener(onClick);
		dateButton.setOnClickListener(onClick);
	    productsButton.setOnClickListener(onClick);
	    attributesButton.setOnClickListener(onClick);
	    carriersButton.setOnClickListener(onClick);
		
		if(clarify)
		{
			Bundle a = getArguments();
			
			onDataLoaded dataLoaded = new SpinnerOnDataLoaded();
			
			depSelect.setOnDataLoaded(dataLoaded);
            arrSelect.setOnDataLoaded(dataLoaded);
            
            progressDialog = ProgressDialog.show(getActivity(), res.getText(R.string.progressTitle), res.getText(R.string.progressSearchingStation), true);
            
            if(a.containsKey("viaText"))
        	{
            	viaSelect.setOnDataLoaded(dataLoaded);
				loading = 3;
				
	            viaSelect.setUserInput(a.getString("viaName"),a.getString("VID1"));
        	}
            
            depSelect.setUserInput(a.getString("depName"),a.getString("SID"));
            arrSelect.setUserInput(a.getString("arrName"),a.getString("ZID"));
		}
	}
	
	boolean validInput()
	{
		//Nie wprowadzono którejś ze stacji
		if(!depEdit.inputValid())
		{
			Toast.makeText(RozkladPKPApplication.getAppContext(), res.getText(R.string.toastDepartureEmpty), Toast.LENGTH_SHORT).show();
			return false;
		}
		else if(!arrEdit.inputValid())
		{
			Toast.makeText(RozkladPKPApplication.getAppContext(), res.getText(R.string.toastArrivalEmpty), Toast.LENGTH_SHORT).show();
			return false;
		}
		else if(viaEdit.isShown() && !viaEdit.inputValid())
		{
			Toast.makeText(RozkladPKPApplication.getAppContext(), res.getText(R.string.toastViaEmpty), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		//Do dalszych operacji potrzebny internet
		if(!CommonUtils.onlineCheck())
			return false;
		
		return true;
	}
	
	@Override
	public void onCreateOptionsMenu(android.support.v4.view.Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.connections_form, menu);
		if(clarify)
		{
			menu.getItem(1).setVisible(false);
			menu.getItem(2).setVisible(false);
		}
	}
	
	@Override
	public void onPrepareOptionsMenu(android.support.v4.view.Menu menu) {
		
		MenuItem via = menu.findItem(R.id.item_via);
		MenuItem car = menu.findItem(R.id.item_carriers);
		
		if(viaEdit == null) via.setVisible(false);
		else via.setTitle(viaEdit.isShown() ? R.string.menuRemoveVia : R.string.menuAddVia);
		
		if(carriersButton == null) car.setVisible(false);
		else car.setTitle(carriersButton.isShown() ? R.string.menuLessOptions : R.string.menuMoreOptions);
	}
	
	@Override
	public boolean onOptionsItemSelected(android.support.v4.view.MenuItem item) {
		Intent ni;
		switch(item.getItemId()){
		case R.id.item_settings:
			ni = new Intent(getActivity(),PreferencesActivity.class); //TODO: Fragmentacja ;)
			startActivity(ni);
			return true;
		case R.id.item_via:
			viaEdit.setVisibility(viaEdit.isShown() ? View.GONE : View.VISIBLE);
			return true;
		case R.id.item_carriers:
			carriersButton.setVisibility(carriersButton.isShown() ? View.GONE : View.VISIBLE);
			return true;
		case R.id.item_return:
			String t = arrEdit.getText().toString();
			arrEdit.setText(depEdit.getText());
			depEdit.setText(t);
			return true;
		case R.id.item_about:
			ni = new Intent(getActivity().getBaseContext(),AboutActivity.class);
			startActivity(ni);
			return true;
		}
		return false;
	}
	
	
	/*
	@Override
	protected void onPause() {
		super.onPause();
		
		if(task != null)
			task.cancel(true);
	}*/
	/*
	private class GetLocality extends CommonUtils.GetLocalityTask{
		ProgressDialog p;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//TODO: Cancel listener
			p = ProgressDialog.show(ConnectionsFormActivity.this, res.getString(R.string.progressTitle), res.getString(R.string.progressBodyLocation));
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			p.dismiss();

			if(result == null)
				Toast.makeText(getApplicationContext(), res.getText(R.string.toastLocationError), Toast.LENGTH_SHORT).show();
			else
			{
				StationEdit ed = (StationEdit) findViewById(R.id.departure_edit);
				ed.setText(result);
				final Editable etext = ed.getText();
				final int position = etext.length();
				Selection.setSelection(etext, position);
			}
		}
	}*/
	
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
			loading--;
			if(loading == 0)
			{
				progressDialog.dismiss();
				if(arrSelect.getStationCount() == 0 || depSelect.getStationCount() == 0)
				{
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							AlertDialog alertDialog;
							alertDialog = new AlertDialog.Builder(getActivity()).create();
							alertDialog.setOnKeyListener(CommonUtils.getOnlyDPadListener());
        			    	
        			    	alertDialog.setTitle("Błąd wyszukiwania!");
        			    	alertDialog.setMessage("Nie można odnaleźć wskazanej stacji.");
        			    	alertDialog.setCancelable(false);
        			    	
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
	}
}
