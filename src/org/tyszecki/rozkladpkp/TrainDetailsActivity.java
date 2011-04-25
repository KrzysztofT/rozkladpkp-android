package org.tyszecki.rozkladpkp;

import org.tyszecki.rozkladpkp.PLN.Train;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class TrainDetailsActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.train_details);
        setTitle("Informacje o pociągu");
        
        //startDate = getIntent().getExtras().getString("StartDate");
        PLN pln = new PLN(getIntent().getExtras().getByteArray("PLNData"));
        int conidx = getIntent().getExtras().getInt("ConnectionIndex");
        int trainidx = getIntent().getExtras().getInt("TrainIndex");
        
        Train t = pln.connections[conidx].trains[trainidx];
        
        TextView tv = (TextView) findViewById(R.id.header);
        tv.setText("Pociąg "+t.number);
        
        tv = (TextView) findViewById(R.id.content);
        
        for(String s : t.attributes)
        	tv.append("-"+s+"\n");
	}
}
