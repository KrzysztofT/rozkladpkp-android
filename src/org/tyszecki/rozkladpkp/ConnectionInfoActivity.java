package org.tyszecki.rozkladpkp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ConnectionInfoActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TextView textview = new TextView(this);
        textview.setText("This is the connection info");
        setContentView(textview);
	}
}
