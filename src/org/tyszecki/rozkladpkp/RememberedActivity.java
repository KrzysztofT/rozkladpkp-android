package org.tyszecki.rozkladpkp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class RememberedActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remembered_list);
        
        ListView lv = (ListView)findViewById(R.id.remembered_list);
        lv.setAdapter(new RememberedItemAdapter(this));
    }
}
