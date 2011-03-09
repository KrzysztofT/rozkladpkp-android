package org.tyszecki.rozkladpkp;

import org.tyszecki.rozkladpkp.R;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Boards2Activity extends Activity{
	
	private TimeButton timeb;
	private DateButton dateb;
	private ProductsButton prodb;
	private ProgressDialog progressDialog;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boards2);
        
        progressDialog = ProgressDialog.show(Boards2Activity.this,    
                "Czekaj...", "Wyszukiwanie stacji...", true);
        
        final StationSpinner sp = (StationSpinner)  findViewById(R.id.selectStation);
        sp.setProgressDialog(progressDialog);
        sp.setUserInput(getIntent().getExtras().getString("userText"));
        
        
        
        timeb	= (TimeButton) findViewById(R.id.boards2TimeBut);
        timeb.setTime(getIntent().getExtras().getString("Time"));
        timeb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDialog(0);
			}
		});
        
        dateb = (DateButton) findViewById(R.id.boards2DateBut);
        dateb.setDate(getIntent().getExtras().getString("Date"));
        dateb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDialog(1);
			}
		});
		
        prodb = (ProductsButton) findViewById(R.id.boards2ProdBut);
        prodb.setProductString(getIntent().getExtras().getString("Products"));
        prodb.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View arg0) {
				showDialog(2);
			}
		});
        
        ((BoardTypeButton)findViewById(R.id.boards2TypeBut)).setType(getIntent().getExtras().getString("Type"));
        
        Button but = (Button) findViewById(R.id.ok);
        but.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent ni = new Intent(arg0.getContext(),BoardActivity.class);
				ni.putExtra("SID", sp.getCurrentSID());
				ni.putExtra("Station", sp.getText());
				
				ni.putExtra("Time", timeb.getTime());
				ni.putExtra("Date", dateb.getDate());
				ni.putExtra("Type", ((BoardTypeButton)findViewById(R.id.boards2TypeBut)).getType());
				ni.putExtra("Products", prodb.getProductString());
				
				getPreferences(MODE_PRIVATE).edit().putString("Products", prodb.getProductString());
				
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
	    }
	    return null;
	}
	
}
