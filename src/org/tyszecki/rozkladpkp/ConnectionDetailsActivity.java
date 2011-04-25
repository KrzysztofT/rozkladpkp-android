package org.tyszecki.rozkladpkp;

import java.util.ArrayList;

import org.tyszecki.rozkladpkp.ConnectionDetailsItem.TrainItem;
import org.tyszecki.rozkladpkp.PLN.Connection;
import org.tyszecki.rozkladpkp.PLN.Train;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ConnectionDetailsActivity extends Activity {
	private ConnectionDetailsItemAdapter adapter;
	private ArrayList<ConnectionDetailsItem> items;
	private PLN pln;
	//private String startDate;
	private int conidx;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_details);
        setTitle("Plan podróży");
        
        //startDate = getIntent().getExtras().getString("StartDate");
        pln = new PLN(getIntent().getExtras().getByteArray("PLNData"));
        conidx = getIntent().getExtras().getInt("ConnectionIndex");
        
        items = new ArrayList<ConnectionDetailsItem>();
        adapter = new ConnectionDetailsItemAdapter(this,  items);
            
        ListView lv = (ListView)findViewById(R.id.connection_details);
        lv.setAdapter(this.adapter);
        
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
				
				Intent ni = new Intent(arg0.getContext(),TrainDetailsActivity.class);
				
				ni.putExtra("PLNData",pln.data);
				ni.putExtra("ConnectionIndex",conidx);
				ni.putExtra("TrainIndex", pos);
				
				startActivity(ni);
			}
		});
        
        loadData();
	}
	
	private void loadData()
	{
		items.clear();
    	
    	ConnectionDetailsItem c = new ConnectionDetailsItem();
    	Connection con = pln.connections[conidx];
    	
    	
    	for(int i = 0; i < con.trains.length; ++i)
    	{
    		Train t = con.trains[i];
    		Log.i("RozkladPKP", t.arrstation.name);
    		TrainItem ti = c.new TrainItem();
    		ti.t = t;
    		items.add(ti);
    	}
    	
    	
    	adapter.notifyDataSetChanged();
	}
}
