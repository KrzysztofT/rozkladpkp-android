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

import org.tyszecki.rozkladpkp.widgets.AttributesButton;
import org.tyszecki.rozkladpkp.widgets.CarriersButton;
import org.tyszecki.rozkladpkp.widgets.DateButton;
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
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class ConnectionsFormActivity extends Activity {
      
	private TimeButton timeb;
	private DateButton dateb;
	private ProductsButton prodb;
	private AttributesButton attrb;
	private CarriersButton carrb;
	private SharedPreferences pref;
	
	private StationEdit depEdit,arrEdit,viaEdit;
	private StationSpinner depSelect, arrSelect,viaSelect;
	private boolean clarify;
	private Resources res;
	private int loading;
	
	private ProgressDialog progressDialog;
	private GetLocality task;
	
	private String safeExtras(String key)
	{
		Bundle e = getIntent().getExtras();
		if(e != null && e.containsKey(key))
			return e.getString(key);
		return null;
	}
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	Bundle e = getIntent().getExtras();
    	if(e != null)
    		clarify = e.containsKey("clarify");
    	else
    		clarify = false;
    	
        setContentView(clarify ? R.layout.connection_form_clarify : R.layout.connection_form);
        
        res = getResources();
        initializeGui();
    }

	@SuppressWarnings("unchecked")
	private void initializeGui() {
		loading = 2;
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
        
        pref = getPreferences(MODE_PRIVATE);
        prodb = (ProductsButton) findViewById(R.id.products_button);
        prodb.setProductString(pref.getString("Products", "11110001111111"));
        prodb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				showDialog(2);
				
			}
		});
        
        attrb = (AttributesButton) findViewById(R.id.attributes_button);
        
        if(getIntent().getExtras() != null && getIntent().getExtras().containsKey("Attributes"))
        	attrb.setParameters((ArrayList<SerializableNameValuePair>) getIntent().getExtras().getSerializable("Attributes"));
        else 
        	attrb.readSettings(pref.getInt("Attributes", 0));
        
        attrb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				showDialog(3);
				
			}
		});
        
        carrb = (CarriersButton) findViewById(R.id.carriers_button);
        carrb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDialog(4);
			}
		});
        
        Bundle extra = getIntent().getExtras();
        if(extra != null && extra.containsKey("Carriers"))
        {
        	carrb.setVisibility(View.VISIBLE);
        	carrb.setParameters((ArrayList<SerializableNameValuePair>) extra.getSerializable("Carriers"));
        }
 
        if(!clarify)
        {
	        depEdit = (StationEdit) findViewById(R.id.departure_edit);
	        arrEdit = (StationEdit) findViewById(R.id.arrival_edit);
	        viaEdit = (StationEdit) findViewById(R.id.via_edit);
	        
	        depEdit.setHint(res.getText(R.string.hintDepartureStation));
	        arrEdit.setHint(res.getText(R.string.hintArrivalStation));
	        viaEdit.setHint(res.getText(R.string.hintViaStation));
	        
	        depEdit.setAutoComplete(pref.getBoolean("EnableStationAC", true));
	        arrEdit.setAutoComplete(pref.getBoolean("EnableStationAC", true));
	        viaEdit.setAutoComplete(pref.getBoolean("EnableStationAC", true));
	        
	        String s = safeExtras("arrName"); 
            if(s != null)
            	arrEdit.setText(s);
            	
            String t = safeExtras("depName");
            if(t != null)
            	depEdit.setText(t);
            
            if(s == null && t != null)
            	arrEdit.requestFocus();
            else if(s != null && t != null)
            {
            	timeb.setFocusable(true);
            	timeb.setFocusableInTouchMode(true);
            	timeb.requestFocus();
            	timeb.requestFocusFromTouch();
            }
            	
        }
        else
        {	
        	onDataLoaded dl = new  onDataLoaded(){
        		@Override
        		public void dataLoaded() {
        			loading--;
        			if(loading == 0)
        			{
        				progressDialog.dismiss();
        				if(arrSelect.getStationCount() == 0 || depSelect.getStationCount() == 0)
        				{
        					runOnUiThread(new Runnable() {
								public void run() {
									AlertDialog alertDialog;
									alertDialog = new AlertDialog.Builder(ConnectionsFormActivity.this).create();
									alertDialog.setOnKeyListener(CommonUtils.getOnlyDPadListener());
		        			    	
		        			    	alertDialog.setTitle("Błąd wyszukiwania!");
		        			    	alertDialog.setMessage("Nie można odnaleźć wskazanej stacji.");
		        			    	alertDialog.setCancelable(false);
		        			    	
		        			    	alertDialog.setButton("Powrót", new DialogInterface.OnClickListener() {
		        						public void onClick(DialogInterface arg0, int arg1) {
		        							ConnectionsFormActivity.this.finish();
		        						}
		        					});
		        			    	alertDialog.show();		
								}
							});
        				}
        			}
        		}
        	};
        	
        	
        	depSelect = (StationSpinner) findViewById(R.id.departure_select);
        	arrSelect = (StationSpinner) findViewById(R.id.arrival_select);
        	
        	if(extra.containsKey("viaText"))
        	{
        		viaSelect = (StationSpinner) findViewById(R.id.via_select);
        		viaSelect.setVisibility(View.VISIBLE);
        		viaSelect.setOnDataLoaded(dl);
        		loading = 3;
        		
        		if(extra.containsKey("viaSID"))
                	viaSelect.setUserInput(extra.getString("viaText"),extra.getString("viaSID"));
                else
                	viaSelect.setUserInput(extra.getString("viaText"));
        	}
            
            depSelect.setOnDataLoaded(dl);
            arrSelect.setOnDataLoaded(dl);
            
            progressDialog = ProgressDialog.show(ConnectionsFormActivity.this, res.getText(R.string.progressTitle), res.getText(R.string.progressSearchingStation), true);
            
            
            if(extra.containsKey("depSID"))
            	depSelect.setUserInput(extra.getString("depText"),extra.getString("depSID"));
            else
            	depSelect.setUserInput(extra.getString("depText"));
            
            if(extra.containsKey("arrSID"))
            	arrSelect.setUserInput(extra.getString("arrText"),extra.getString("arrSID"));
            else
            	arrSelect.setUserInput(extra.getString("arrText"));
        }
        
        ((Button) findViewById(R.id.ok_button)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent ni = null;
				if(!clarify)
				{
					//Nie wprowadzono którejś ze stacji
					if(depEdit.getText().toString().trim().length() == 0)
					{
						Toast.makeText(getApplicationContext(), res.getText(R.string.toastDepartureEmpty), Toast.LENGTH_SHORT).show();
						return;
					}
					else if(arrEdit.getText().toString().trim().length() == 0)
					{
						Toast.makeText(getApplicationContext(), res.getText(R.string.toastArrivalEmpty), Toast.LENGTH_SHORT).show();
						return;
					}
					else if(viaEdit.isShown() && viaEdit.getText().toString().trim().length() == 0)
					{
						Toast.makeText(getApplicationContext(), res.getText(R.string.toastViaEmpty), Toast.LENGTH_SHORT).show();
						return;
					}
					
					//Do dalszych operacji potrzebny internet
					if(!CommonUtils.onlineCheck())
						return;
					
					
					String sidd = depEdit.getCurrentSID();
					String sida = arrEdit.getCurrentSID();
					
					String sidv = viaEdit.isShown() ? viaEdit.getCurrentSID() : null;
					
					//Trzeba doprecyzować nazwę co najmniej jednej stacji
					if(sidd.equals("") || sida.equals("") || (sidv != null && sidv.equals("")))
					{
						ni = new Intent(arg0.getContext(),ConnectionsFormActivity.class);
						ni.putExtra("clarify", true);
						if(!sidd.equals(""))
							ni.putExtra("depSID", sidd);
							
						ni.putExtra("depText", depEdit.getText().toString());
							
						if(!sida.equals(""))
							ni.putExtra("arrSID", sida);
						
						if(sidv != null)
						{
							if(!sidv.equals(""))
								ni.putExtra("viaSID", sidv);
							
							ni.putExtra("viaText", viaEdit.getText().toString());
						}
					
						ni.putExtra("arrText", arrEdit.getText().toString());
					}
					//Wpisano dwie takie same stacje
					else if(sida.equals(sidd) || sida.equals(sidv) || sidd.equals(sidv))
					{
						Toast.makeText(getApplicationContext(), res.getText(R.string.toastSameStationsError), Toast.LENGTH_SHORT).show();
							return;
					}
					//Nic nie trzeba doprecyzowywać
					else
					{
						ni = new Intent(arg0.getContext(),ConnectionListActivity.class);
						ni.putExtra("ZID", sida);
						ni.putExtra("SID", sidd);
						
						if(sidv != null)
							ni.putExtra("VID1", sidv);
						
						ni.putExtra("depName", depEdit.getText().toString());
						ni.putExtra("arrName", arrEdit.getText().toString());
					}
				}
				else
				{
					depSelect.saveInDatabase();
		        	arrSelect.saveInDatabase();
		        	
		        	if(viaSelect != null)
		        		viaSelect.saveInDatabase();
		        	
					//Wybrano dwie takie same stacje z listy
					if(arrSelect.getCurrentSID().equals(depSelect.getCurrentSID()))
					{
						Toast.makeText(getApplicationContext(), res.getText(R.string.toastSameStationsError), Toast.LENGTH_SHORT).show();
							return;
					}
					
					ni = new Intent(arg0.getContext(),ConnectionListActivity.class);
					
					ni.putExtra("ZID", arrSelect.getCurrentSID());
					ni.putExtra("SID", depSelect.getCurrentSID());
					
					if(viaSelect != null)
						ni.putExtra("VID1", viaSelect.getCurrentSID());
					
					ni.putExtra("depName", depSelect.getText());
					ni.putExtra("arrName", arrSelect.getText());
				}
				
				ni.putExtra("Time", timeb.getTime());
				ni.putExtra("Date", dateb.getDate());
				ni.putExtra("Products", prodb.getProductString());
				ni.putExtra("Attributes", attrb.getParameters());
				
				if(carrb.isShown())
					ni.putExtra("Carriers", carrb.getParameters());
				
				pref.edit().putString("Products", prodb.getProductString()).putInt("Attributes", attrb.settingsCode()).commit();
				
				startActivity(ni);
			}
		});
        
        if(!clarify)
        {
	        ((ImageButton) findViewById(R.id.location_button)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					task = new GetLocality();
					task.execute();
				}
			});
        }
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.connections_form, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		if(viaEdit == null)
			menu.findItem(R.id.item_via).setVisible(false);
		else if(viaEdit.isShown())
			menu.findItem(R.id.item_via).setTitle(R.string.menuRemoveVia);
		else
			menu.findItem(R.id.item_via).setTitle(R.string.menuAddVia);
		
		if(carrb == null)
			menu.findItem(R.id.item_carriers).setVisible(false);
		else if(carrb.isShown())
			menu.findItem(R.id.item_carriers).setTitle(R.string.menuLessOptions);
		else
			menu.findItem(R.id.item_carriers).setTitle(R.string.menuMoreOptions);
		
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case R.id.item_settings:
			Intent ni = new Intent(getBaseContext(),PreferencesActivity.class);
			startActivity(ni);
			return true;
		case R.id.item_via:
			viaEdit.setVisibility(viaEdit.isShown() ? View.GONE : View.VISIBLE);
		case R.id.item_carriers:
			carrb.setVisibility(carrb.isShown() ? View.GONE : View.VISIBLE);
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
	    case 3:
	    	return attrb.getDialog();
	    case 4:
	    	return carrb.getDialog();
	    }
	    return null;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d("RozkladPKP", "Pause");
		if(task != null)
		{
			Log.e("RozkladPKP","Anulowanie...");
			task.cancel(true);
		}
		else
			Log.wtf("RozkladPKP","NULL...");
	}
	
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
			Log.i("RozkladPKP", "Post");
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
	}
}
