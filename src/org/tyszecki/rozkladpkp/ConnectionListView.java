package org.tyszecki.rozkladpkp;

import android.widget.ListView;

public class ConnectionListView extends Object {
	
	private ListView lv;
	private ConnectionList clist;
	private ConnectionListItemAdapter adapter;

	public ConnectionListView(ConnectionList list, ListView listView) 
	{
		lv = listView;
		clist = list;
		
	}
}
