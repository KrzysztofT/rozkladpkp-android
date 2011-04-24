package org.tyszecki.rozkladpkp;

import java.util.ArrayList;

import org.tyszecki.rozkladpkp.ConnectionInfoItem.TrainItem;
import org.tyszecki.rozkladpkp.PLN.Connection;
import org.tyszecki.rozkladpkp.PLN.Train;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

public class ConnectionInfoActivity extends Activity {
	private ConnectionInfoItemAdapter adapter;
	private ArrayList<ConnectionInfoItem> items;
	private PLN pln;
	private String startDate;
	private int conidx;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connectioninfo);
        setTitle("Plan podróży");
        
        startDate = getIntent().getExtras().getString("StartDate");
        pln = new PLN(getIntent().getExtras().getByteArray("PLNData"));
        conidx = getIntent().getExtras().getInt("ConnectionIndex");
        
        items = new ArrayList<ConnectionInfoItem>();
        adapter = new ConnectionInfoItemAdapter(this,  items);
            
        ListView lv = (ListView)findViewById(R.id.conninfoview);
        lv.setAdapter(this.adapter);
        
        loadData();
	}
	
	private void loadData()
	{
		items.clear();
    	
    	ConnectionInfoItem c = new ConnectionInfoItem();
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
