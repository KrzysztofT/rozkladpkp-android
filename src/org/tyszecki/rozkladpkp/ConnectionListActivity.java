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

import java.util.ArrayList;

import org.tyszecki.rozkladpkp.ConnectionList.ConnectionListCallback;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ConnectionListActivity extends FragmentActivity {
	
	private ProgressDialog m_ProgressDialog;
	private boolean hasFullTable = false;
	private boolean inFront = true, showNCDialog = false, showERDialog;
	private String timetableUrl = null;
	private ArrayList<SerializableNameValuePair> commonFieldsList;
	
	private Bundle extras;
	
	
	private ConnectionListItemAdapter adapter;
	private ConnectionList clist;
	private ConnectionListCallback clistCallback;
	
	public void onCreate(Bundle savedInstanceState) {
		setTheme(RozkladPKPApplication.getThemeId());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connection_list);
		
        adapter = new ConnectionListItemAdapter(this); 
        extras = getIntent().getExtras();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        clistCallback = new ConnectionListCallback() {
			@Override
			public void contentReady(ConnectionList list, final boolean error) {
				if(clist == null)
					clist = list;
				
				Runnable uit = new Runnable() {
					@Override
					public void run() {
						updateDisplayedPLN(error);
					}
				};
				runOnUiThread(uit);
			}
		};
		
		getSupportActionBar().setTitle(extras.getString("depName")+" →");
		getSupportActionBar().setSubtitle(extras.getString("arrName"));
        
        setupCommonFields();
		setupContents(savedInstanceState);
        setupListView();
	}

	private void setupListView() {
		ListView lv = (ListView)findViewById(R.id.connection_list);

		lv.setAdapter(this.adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos, long id) {
				int type = adapter.getItemViewType(pos);
				
				if(type == ConnectionListItemAdapter.NORMAL){
					int cix = adapter.getConnectionAt(pos);

					//Błąd - nic nie robimy
					if(cix == -1)
						return;
					
					Intent ni = new Intent(arg0.getContext(),ConnectionDetailsActivity.class);

					ni.putExtra("seqnr", clist.getSeqNr());
					ni.putExtra("PLNData", clist.getPLN().data);
					ni.putExtra("ConnectionIndex",cix);
					
					ni.putExtra("StartDate",adapter.getDateForConnectionAt(pos).format("%d.%m.%Y"));
					ni.putExtra("Attributes", extras.getSerializable("Attributes"));
					ni.putExtra("Products", extras.getString("Products"));
					startActivity(ni);
				}
				else if(type == ConnectionListItemAdapter.SCROLL)
				{
					showLoader();
					clist.fetchMore(!(pos == adapter.getCount()-1));
				}
				else if(type == ConnectionListItemAdapter.WARNING && !clist.scrollable())
					newSearch();
			}
		});
	}

	private void setupContents(Bundle savedInstanceState) {
		if(extras.containsKey("PLNFilename"))
			clist = ConnectionList.fromFile(clistCallback, extras.getString("PLNFilename"));
		
		else if(savedInstanceState != null && savedInstanceState.containsKey("PLNData")){

			clist = ConnectionList.fromByteArray(clistCallback, commonFieldsList, savedInstanceState.getByteArray("PLNData"), savedInstanceState.getInt("SeqNr"));
			hasFullTable = savedInstanceState.getBoolean("hasFullTable");
			timetableUrl = savedInstanceState.getString("timetableURL");
		}
		else
		{
			clist = ConnectionList.fromNetwork(clistCallback, commonFieldsList, extras.getString("Date"), extras.getString("Time"));	
			getConnections();
		}
	}

	@SuppressWarnings("unchecked")
	private void setupCommonFields()
	{
		//Pola wykorzystywane przy wszystkich żądaniach
        commonFieldsList = (ArrayList<SerializableNameValuePair>) extras.getSerializable("Attributes");
        commonFieldsList.add(new SerializableNameValuePair("SID", extras.getString("SID")));
        
        if(extras.containsKey("VID1"))
        	commonFieldsList.add(new SerializableNameValuePair("VID1", extras.getString("VID1")));
        
        if(extras.containsKey("Carriers"))
        	commonFieldsList.addAll((ArrayList<SerializableNameValuePair>)extras.getSerializable("Carriers"));
        
        commonFieldsList.add(new SerializableNameValuePair("ZID", extras.getString("ZID")));
        commonFieldsList.add(new SerializableNameValuePair("REQ0JourneyProduct_prod_list_1",extras.getString("Products")));
        commonFieldsList.add(new SerializableNameValuePair("start", "1"));
	}
	
	public void newSearch()
	{
		Bundle extras = getIntent().getExtras();
		Intent ni = new Intent(getBaseContext(),ConnectionsFormActivity.class);
		ni.putExtra("arrName",extras.getString("arrName"));
		ni.putExtra("depName",extras.getString("depName"));
		startActivity(ni);
	}
	
	public void showLoader()
	{
		Runnable uit = new Runnable(){
			@Override
			public void run() {
				m_ProgressDialog = ProgressDialog.show(ConnectionListActivity.this,    
	              "Czekaj...", "Pobieranie rozkładu...", true, true, new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						clist.abort();
						hideLoader();
						ConnectionListActivity.this.finish();
					}
				});
			}
		};
		runOnUiThread(uit);
	}
	
	
	public void hideLoader()
	{
		if(m_ProgressDialog != null)
		{				
    		m_ProgressDialog.dismiss();
    		m_ProgressDialog = null;
		}
	}
	public void updateDisplayedPLN(boolean error)
	{
		hideLoader();
		
		if(error)
		{
			if(inFront)
				serverErrorAlert();
			else
				showERDialog = true;
			
			return;
		}
		
		if(clist == null || clist.getPLN() == null)
			return;
		
		if(clist.getSeqNr() == 0 && clist.getPLN().conCnt == 0)
		{
			if(inFront)
				noConnectionsAlert();
			else
				showNCDialog = true;
		}
		else if(clist.getSeqNr() > 0 && clist.getPLN().conCnt == 0)
		{
			if(inFront)
				noMoreAlert();
		}
		else
		{
			adapter.setConnectionList(clist);

			//Zapisanie wyników
			Bundle extras = ConnectionListActivity.this.getIntent().getExtras(); 
			Intent in = new Intent(ConnectionListActivity.this,RememberedService.class);

			if(clist.getPLN() != null && !extras.containsKey("PLNFilename"))
				in.putExtra("pln", clist.getPLN().data);

			in.putExtra("SID", CommonUtils.StationIDfromSID(extras.getString("SID")));
			in.putExtra("ZID", CommonUtils.StationIDfromSID(extras.getString("ZID")));

			startService(in);
		}
	}
      
	protected void noConnectionsAlert() {
		//Pokazuje okno dialogowe informujące o braku połączeń i umożliwia powrót do wcześniejszej aktywności.
		AlertDialog alertDialog;
    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Brak połączeń!");
    	alertDialog.setMessage("Nie istnieje połączenie między wybranymi stacjami które spełnia obecne kryteria wyszukiwania.");
    	alertDialog.setOnKeyListener(CommonUtils.getOnlyDPadListener());
    	alertDialog.setCancelable(false);
    	
    	alertDialog.setButton("Powrót", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				ConnectionListActivity.this.finish();
			}
		});
    	alertDialog.show();
	}
	protected void serverErrorAlert() {
		//Pokazuje okno dialogowe informujące o braku połączeń i umożliwia powrót do wcześniejszej aktywności.
		AlertDialog alertDialog;
    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Błąd serwera!");
    	alertDialog.setMessage("Serwer systemu SITKOL obecnie nie działa. Odczekaj chwilę i ponów próbę.");
    	alertDialog.setOnKeyListener(CommonUtils.getOnlyDPadListener());
    	alertDialog.setCancelable(false);
    	
    	alertDialog.setButton("Powrót", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				ConnectionListActivity.this.finish();
			}
		});
    	alertDialog.show();
	}
	
	
	protected void getConnections(){
		
		if(!CommonUtils.onlineCheck())
			return;
		
		showLoader();
		clist.fetch();
	}
	
	private void noMoreAlert() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				AlertDialog alertDialog;
		    	alertDialog = new AlertDialog.Builder(ConnectionListActivity.this).create();
		    	alertDialog.setTitle("Brak połączeń!");
		    	alertDialog.setMessage("Nie można pobrać informacji o kolejnych połączeniach.");
		    	alertDialog.setCancelable(true);
		    	alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		    	alertDialog.show();		
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(android.support.v4.view.Menu menu) {
		getMenuInflater().inflate(R.menu.connection_list, menu);
		return true;
	}
	

	public boolean onOptionsItemSelected (android.support.v4.view.MenuItem item){
		Bundle extras = getIntent().getExtras();
		Intent ni = null;
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
		    return true;
		/*case R.id.savepln:
			File f = new File(Environment.getExternalStorageDirectory(),"PLN");
			FileOutputStream w = null;
			try {
				w = new FileOutputStream(f);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			try {
				w.write(clist.getPLN().data);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		*/case R.id.item_favourite:
			RememberedManager.addtoHistory(ConnectionListActivity.this, CommonUtils.StationIDfromSID(extras.getString("SID")), CommonUtils.StationIDfromSID(extras.getString("ZID")),RememberedManager.getCacheString(clist.getPLN()));
			RememberedManager.saveRoute(ConnectionListActivity.this, CommonUtils.StationIDfromSID(extras.getString("SID")), CommonUtils.StationIDfromSID(extras.getString("ZID")));
			return true;
		
		case R.id.item_return_journey:
			
			ni = new Intent(getBaseContext(),ConnectionsFormActivity.class);
			ni.putExtra("arrName",extras.getString("depName"));
			
		case R.id.item_continue_journey:
			if(ni == null)
				ni = new Intent(getBaseContext(),ConnectionsFormActivity.class);
			
			ni.putExtra("depName",extras.getString("arrName"));
			
			startActivity(ni);
			return true;
			
		case R.id.item_taxity:
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.taxity")));
			return true;
		case R.id.share:
			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, adapter.getContentForSharing());
			startActivity(Intent.createChooser(sharingIntent, "Udostępnij przez..."));
			return true;
		}
		
		
			
		
		return false;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle state){
		super.onSaveInstanceState(state);
		
		if(clist.getPLN() != null)
			state.putByteArray("PLNData", clist.getPLN().data);
		state.putInt("SeqNr", clist.getSeqNr());
		state.putBoolean("hasFullTable", hasFullTable);
		state.putString("timetableURL", timetableUrl);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		inFront = true;
		if(showNCDialog)
		{
			noConnectionsAlert();
			showNCDialog = false;
		}
		else if(showERDialog)
		{
			serverErrorAlert();
			showERDialog = false;
		}
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		hideLoader();
		inFront = false;
	}
}
