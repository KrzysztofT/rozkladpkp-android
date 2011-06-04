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

import org.tyszecki.rozkladpkp.PLN.Train;

import android.app.Activity;
import android.os.Bundle;
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
