package org.tyszecki.rozkladpkp;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tyszecki.rozkladpkp.BoardItem.DateItem;
import org.tyszecki.rozkladpkp.BoardItem.TrainItem;

import org.tyszecki.rozkladpkp.R;

import android.content.Context;
import android.opengl.Visibility;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class BoardItemAdapter extends BaseAdapter {

	final int HEADER = 0;
	final int NORMAL = 1;
	
    private ArrayList<BoardItem> items;
    private boolean dep;
    Context c;

    public BoardItemAdapter(Context context, ArrayList<BoardItem> items) {
    		c = context;
            this.items = items;
    }
    
    public void setType(boolean d)
    {
    	dep = d;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            BoardItem b = items.get(position);
            
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (b instanceof TrainItem)
                	v = vi.inflate(R.layout.row, null);
                else
                	v = vi.inflate(R.layout.connectionheaderrow, null);
            }
            
            if (b instanceof TrainItem) {
            		
            		TrainItem o = (TrainItem)b;
            		
                    ((TextView) v.findViewById(R.id.boardrow_time)).setText(Html.fromHtml("<b>"+o.time+"</b> "));
                    ((TextView) v.findViewById(R.id.boardrow_city)).setText(Html.fromHtml(((dep)?"do ":"z ")+"<b>"+o.station+"</b> "));
                    //((TextView) v.findViewById(R.id.boardrow_train_number)).setText(o.number);
                    
                    
                    TextView type = (TextView) v.findViewById(R.id.boardrow_train_type);
                   
                    Pattern p = Pattern.compile("(TLK|EC|KD|IR|RE|EIC|SKM|D|EN|KM|SKW|WKD).*");
                    Matcher m = p.matcher(o.number);
                    
                    int iid = R.drawable.back_reg;
                    
                    if(m.matches())
                    {              	
                    	String s = m.group(1);
                    	if(s.equals("TLK"))iid = R.drawable.back_tlk;
                    	else if(s.equals("D"))iid = R.drawable.back_tlk;
                    	
                    	else if(s.equals("EC"))iid = R.drawable.back_ec;
                    	else if(s.equals("EIC"))iid = R.drawable.back_eic;
                    	else if(s.equals("EN"))iid = R.drawable.back_eic;
                    	
                    	else if(s.equals("KD"))iid = R.drawable.back_reg;
                    	
                    	else if(s.equals("IR"))iid = R.drawable.back_ir;
                    	else if(s.equals("RE"))iid = R.drawable.back_re;
                    	
                    	else if(s.equals("SKM"))iid = R.drawable.back_skm;
                    	else if(s.equals("SKW"))iid = R.drawable.back_skm;
                    	else if(s.equals("WKD"))iid = R.drawable.back_skm;

                    	else iid = R.drawable.back_reg;
                    	
                    	type.setText(s);
                    }
                    else
                    	type.setText("Osob");
                    
                    type.setBackgroundResource(iid);                    
            }
            else {
            	TextView head = (TextView) v.findViewById(R.id.conn_header);
                head.setText(((DateItem)b).date);
            }
            return v;
    }
    @Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public int getItemViewType(int arg0) {
		return items.get(arg0) instanceof DateItem ? HEADER : NORMAL;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return items.size() == 0;
	}
	
	public boolean areAllItemsSelectable() {  
        return false;  
    }  
	@Override
    public boolean isEnabled(int position) {  
        return (getItemViewType(position) != HEADER);  
    }  
}