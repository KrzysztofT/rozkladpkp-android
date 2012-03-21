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
package org.tyszecki.rozkladpkp.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.tyszecki.rozkladpkp.DatabaseHelper;
import org.tyszecki.rozkladpkp.R;
import org.tyszecki.rozkladpkp.RozkladPKPApplication;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;

public class StationEdit extends  AutoCompleteTextView {

	static TernaryTree tree;
	
	private boolean enableAC = true;
	
	static class Node
	 {
	    private char m_char;
	    private Node m_left, m_center, m_right;
	    private String v = null;

	    public Node(char ch)
	    {
	        m_char = ch;
	    }
	};
	 
	 static class TernaryTree
	 {
	     private Node m_root = null;
	     private final Map<Character,Character> chmap = new HashMap<Character,Character>(){
	 		private static final long serialVersionUID = 1L;
	 		{
	 			  put('ą','a');
	 			  put('ć','c');
	 			  put('ę','e');
	 			  put('ł','l');
	 			  put('ń','n');
	 			  put('ó','o');
	 			  put('ś','s');
	 			  put('ż','z');
	 			  put('ź','z');
	 		  }
	 		};
	 	
	     private char strip(char in)
	     {
	    	 in = Character.toLowerCase(in);
	    	 if(chmap.containsKey(in))
	    		 in = chmap.get(in);
	    	 return in;
	     }
	     private Node Add(String s, int pos, Node node)
	     {
	    	 char p = strip(s.charAt(pos));
	    	 
	         if (node == null) { node = new Node(p); }

	         if (p < node.m_char) { node.m_left =  Add(s, pos, node.m_left); }
	         else if (p > node.m_char) { node.m_right = Add(s, pos, node.m_right); }
	         else
	         {
	        	 //TODO: teraz zakładamy, że nie ma dwóch stacji różniących się jedynie znakami diakrytycznymi,
	        	 //tj. że nie istnieją np. "Baby" i "Bąby". Jeśli taka sytuacja zdarzy się, tylko jedna stacja będzie
	        	 //dostępna w autouzupełnianiu.
	             if (pos + 1 == s.length()) {node.v = s;}
	             else { node.m_center = Add(s, pos + 1, node.m_center); }
	         }
	         
	         return node;
	     }

	     public void Add(String s)
	     {
	         if (s == null || s == "") 
	        	 return;

	         m_root = Add(s, 0, m_root);
	     }

	     private void Matching(Node n, ArrayList<String> list)
	     {
	    	 if(n.v != null)
	    		 list.add(n.v);
	    	 
	    	 if(n.m_center != null)
	    		 Matching(n.m_center, list);
	    	 if(n.m_left != null)
	    		 Matching(n.m_left, list);
	    	 if(n.m_right != null)
	    		 Matching(n.m_right, list);
	     }
	     
	     public ArrayList<String> Autocomplete(String s)
	     {
	         if (s == null || s == "") 
	        	 return new ArrayList<String>();

	         int pos = 0;
	         Node node = m_root;
	         while (node != null)
	         {
	        	 char p = strip(s.charAt(pos));
	             
	             if (p < node.m_char) { node = node.m_left; }
	             else if (p > node.m_char) { node = node.m_right; }
	             else
	             {
	                 if (++pos == s.length()) 
	                 {
	                	 ArrayList<String> results = new ArrayList<String>();
	                	 
	                	 if(node.m_center != null)	 
	                		 Matching(node.m_center,results); 
	                	
	                	 return results;
	                 }
	                 node = node.m_center;
	             }
	         }

	         return new ArrayList<String>();
	     }
	 }
	
	class StationAdapter extends ArrayAdapter<String>{
		private StationFilter mFilter;
		
		
		public StationAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId, new ArrayList<String>());
		}
		
		private class StationFilter extends Filter{
			
			
			
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
	            if ((constraint == null) || (constraint.length() == 0))
	            {
	                ArrayList<String> list = new ArrayList<String>();
	                results.values = list;
	                results.count = list.size();
	            }
	            else
	            {
	            	ArrayList<String> al = tree.Autocomplete(constraint.toString()); 
	            	//Wywal przystanki ZTM na koniec listy, resztę zostaw tak jak były
	            	Collections.sort(al, new Comparator<String>() {
						@Override
						public int compare(String lhs, String rhs) {
							boolean rc = rhs.contains("ZTM");
							if(lhs.contains("ZTM"))
							{
								if(rc)
									return 0;
								else
									return 1;
							}
							else{
								if(rc)
									return -1;
								else 
									return 0;
							}
						}
					});
	                results.values = al;
	                results.count = al.size();
	            }
	            return results;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence arg0, FilterResults results) {
				if (results.count > 0)
	            {
					clear();
					notifyDataSetChanged();
					for(String t:(ArrayList<String>)results.values)
						add(t);    
	            }
	            else
	                notifyDataSetInvalidated();
			}	
		}
		
		public Filter getFilter()
		{
			if (mFilter == null) 
                mFilter = new StationFilter();
            return mFilter;
		}
	}
	
	
	public StationEdit(Context context, AttributeSet attrs) {
        super(context, attrs); 
        setSingleLine();
        setDropDownHeight(-2);
        
        StationAdapter a = new StationAdapter(getContext(), R.layout.station_edit_item);
        setAdapter(a);  
	};
	
	public static void initTree()
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				SQLiteDatabase db =  DatabaseHelper.getDb(RozkladPKPApplication.getAppContext());
		        Cursor cur = db.query("stations", new String[]{"name"}, null, null, null, null, null);
		        
		        tree = new TernaryTree();
		        
		        while(cur.moveToNext())
		        	tree.Add(cur.getString(0));
		        
		        db.close();
			}
		}).run();
	}
	
	public String getCurrentSID()
	{
		String cstation = getText().toString();
		
		SQLiteDatabase db =  DatabaseHelper.getDb(getContext());
        Cursor cur = db.query("stations", new String[]{"_id","x","y"}, "name LIKE ?", new String[]{cstation}, null, null, null,"1");
		
        if(cur.moveToNext())
        {
        	db.close();
        	return "A=1@O="+cstation+"@X="+cur.getInt(1)+"@Y="+cur.getInt(2)+"@L="+Integer.toString(cur.getInt(0))+"@";
        }
        db.close();
        return "";
        
	}
	
	public void setAutoComplete(boolean en)
	{
		enableAC = en;
	}
	
	public boolean autoComplete()
	{
		return enableAC;
	}
	
	public boolean inputValid()
	{
		return getText().toString().trim().length() > 0;
	}
}
