package org.tyszecki.rozkladpkp.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

public class TimetableTypeButton extends Button {

	boolean dep	= true;
	public TimetableTypeButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setText("Odjazdy");
		
		setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				dep = !dep;
				updateText();
			}
		});
	}
	
	public String getType() {
		return dep ? "dep" : "arr";
	}
	
	public void setType(boolean departures) {
		dep = departures;
		updateText();
	}
	
	public void setType(String t)
	{
		setType(t.equals("dep"));
	}

	private void updateText() {
		setText(dep?"Odjazdy":"Przyjazdy");
		
	}
}
