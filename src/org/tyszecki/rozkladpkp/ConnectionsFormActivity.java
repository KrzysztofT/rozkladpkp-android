package org.tyszecki.rozkladpkp;

import java.util.ArrayList;

import org.tyszecki.rozkladpkp.widgets.AttributesButton;
import org.tyszecki.rozkladpkp.widgets.DateButton;
import org.tyszecki.rozkladpkp.widgets.ProductsButton;
import org.tyszecki.rozkladpkp.widgets.StationEdit;
import org.tyszecki.rozkladpkp.widgets.StationSpinner;
import org.tyszecki.rozkladpkp.widgets.TimeButton;
import org.tyszecki.rozkladpkp.widgets.StationSpinner.onDataLoaded;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
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
	private SharedPreferences pref;
	
	private StationEdit depEdit,arrEdit;
	private StationSpinner depSelect, arrSelect;
	private boolean clarify;
	private Resources res;
	private int loading;
	
	private ProgressDialog progressDialog;
	
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
        	attrb.deselectAll();
        attrb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				showDialog(3);
				
			}
		});
 
        if(!clarify)
        {
	        depEdit = (StationEdit) findViewById(R.id.departure_edit);
	        arrEdit = (StationEdit) findViewById(R.id.arrival_edit);
	        
	        depEdit.setHint(res.getText(R.string.hintDepartureStation));
	        arrEdit.setHint(res.getText(R.string.hintArrivalStaton));
	        
	        depEdit.setAutoComplete(pref.getBoolean("EnableStationAC", true));
	        arrEdit.setAutoComplete(pref.getBoolean("EnableStationAC", true));
        }
        else
        {	
        	onDataLoaded dl = new  onDataLoaded(){
        		@Override
        		public void dataLoaded() {
        			loading--;
        			if(loading == 0)
        				progressDialog.dismiss();
        		}
        	};
     
        	depSelect = (StationSpinner) findViewById(R.id.departure_select);
        	arrSelect = (StationSpinner) findViewById(R.id.arrival_select);
            
            depSelect.setOnDataLoaded(dl);
            arrSelect.setOnDataLoaded(dl);
            
            progressDialog = ProgressDialog.show(ConnectionsFormActivity.this, res.getText(R.string.progressTitle), res.getText(R.string.progressSearchingStation), true);
            
            Bundle extra = getIntent().getExtras();
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
					
					//Do dalszych operacji potrzebny internet
					if(!CommonUtils.onlineCheck(getBaseContext()))
						return;
					
					
					String sidd = depEdit.getCurrentSID();
					String sida = arrEdit.getCurrentSID();
					
					//Trzeba doprecyzować nazwę co najmniej jednej stacji
					if(sidd.equals("") || sida.equals(""))
					{
						ni = new Intent(arg0.getContext(),ConnectionsFormActivity.class);
						ni.putExtra("clarify", true);
						if(!sidd.equals(""))
							ni.putExtra("depSID", sidd);
							
						ni.putExtra("depText", depEdit.getText().toString());
							
						
						if(!sida.equals(""))
							ni.putExtra("arrSID", sida);
					
						ni.putExtra("arrText", arrEdit.getText().toString());
					}
					//Wpisano dwie takie same stacje
					else if(arrEdit.getCurrentSID().equals(depEdit.getCurrentSID()))
					{
						Toast.makeText(getApplicationContext(), res.getText(R.string.toastSameStationsError), Toast.LENGTH_SHORT).show();
							return;
					}
					//Nic nie trzeba doprecyzowywać
					else
					{
						ni = new Intent(arg0.getContext(),ConnectionListActivity.class);
						ni.putExtra("ZID", arrEdit.getCurrentSID());
						ni.putExtra("SID", depEdit.getCurrentSID());
						ni.putExtra("depName", depEdit.getText().toString());
						ni.putExtra("arrName", arrEdit.getText().toString());
					}
				}
				else
				{
					//Wybrano diwe takie same stacje z listy
					if(arrSelect.getCurrentSID().equals(depSelect.getCurrentSID()))
					{
						Toast.makeText(getApplicationContext(), res.getText(R.string.toastSameStationsError), Toast.LENGTH_SHORT).show();
							return;
					}
					
					ni = new Intent(arg0.getContext(),ConnectionListActivity.class);
					
					ni.putExtra("ZID", arrSelect.getCurrentSID());
					ni.putExtra("SID", depSelect.getCurrentSID());		
					ni.putExtra("depName", depSelect.getText());
					ni.putExtra("arrName", arrSelect.getText());
				}
				
				ni.putExtra("Time", timeb.getTime());
				ni.putExtra("Date", dateb.getDate());
				ni.putExtra("Products", prodb.getProductString());
				ni.putExtra("Attributes", attrb.getParameters());
				pref.edit().putString("Products", prodb.getProductString()).commit();
				
				startActivity(ni);
			}
		});
        
        if(!clarify)
        {
	        ((ImageButton) findViewById(R.id.location_button)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CommonUtils.currentLocality(ConnectionsFormActivity.this, new CommonUtils.LocationResult() {
						@Override
						public void gotLocality(final String s) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										StationEdit ed = (StationEdit) findViewById(R.id.departure_edit);
										ed.setText(s);
										final Editable etext = ed.getText();
										final int position = etext.length();
										Selection.setSelection(etext, position);
									}
								});
							}
						}
					);		
				}
			});
        }
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
	    }
	    return null;
	}
}
