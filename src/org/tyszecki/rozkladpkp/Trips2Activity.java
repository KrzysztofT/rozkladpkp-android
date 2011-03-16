package org.tyszecki.rozkladpkp;

import org.tyszecki.rozkladpkp.StationSpinner.onDataLoaded;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import org.tyszecki.rozkladpkp.R;

public class Trips2Activity extends Activity {
	private TimeButton timeb;
	private DateButton dateb;
	private ProductsButton prodb;
	private int loading;
	private ProgressDialog progressDialog;
	
	private StationSpinner depEdit,arrEdit;
	private AttributesButton attrb;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.trips2);
        
        initializeGui();
	}

	

	private void initializeGui() {
		
		loading = 2;
		
		timeb	= (TimeButton) findViewById(R.id.trips2TimeBut);
        timeb.setTime(getIntent().getExtras().getString("Time"));
        timeb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDialog(0);
			}
		});
        
        dateb = (DateButton) findViewById(R.id.trips2DateBut);
        dateb.setDate(getIntent().getExtras().getString("Date"));
        dateb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDialog(1);
			}
		});
        
        prodb = (ProductsButton) findViewById(R.id.trips2ProdBut);
        prodb.setProductString(getIntent().getExtras().getString("Products"));
        prodb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				showDialog(2);
				
			}
		});
        
        attrb = (AttributesButton) findViewById(R.id.trips2AttrBut);
        attrb.setRequestString(getIntent().getExtras().getString("Attributes"));
        attrb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				showDialog(3);
				
			}
		});
        
        onDataLoaded dl = new  onDataLoaded(){
    		@Override
    		public void dataLoaded() {
    			loading--;
    			if(loading == 0)
    				progressDialog.dismiss();
    		}
    	};
 
        depEdit = (StationSpinner) findViewById(R.id.trips2Dep);
        arrEdit = (StationSpinner) findViewById(R.id.trips2Arr);
        
        depEdit.setOnDataLoaded(dl);
        arrEdit.setOnDataLoaded(dl);
        
        progressDialog = ProgressDialog.show(Trips2Activity.this,    
                "Czekaj...", "Wyszukiwanie stacji...", true);
        
        Bundle extra = getIntent().getExtras();
        if(extra.containsKey("depSID"))
        	depEdit.setUserInput(extra.getString("depText"),extra.getString("depSID"));
        else
        	depEdit.setUserInput(extra.getString("depText"));
        
        if(extra.containsKey("arrSID"))
        	arrEdit.setUserInput(extra.getString("arrText"),extra.getString("arrSID"));
        else
        	arrEdit.setUserInput(extra.getString("arrText"));
        
        Button ok	= (Button) findViewById(R.id.trips2OK);
        ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent ni = new Intent(arg0.getContext(),ConnectionsActivity.class);
				
				ni.putExtra("Time", timeb.getTime());
				ni.putExtra("Date", dateb.getDate());
				ni.putExtra("Products", prodb.getProductString());
				ni.putExtra("ZID", arrEdit.getCurrentSID());
				ni.putExtra("SID", depEdit.getCurrentSID());
				ni.putExtra("Attributes", attrb.getRequestString());
				
				startActivity(ni);
			}
		});
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
