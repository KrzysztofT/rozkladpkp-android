package org.tyszecki.rozkladpkp.pln;

public class PLNTimestamp{
	int val;
	int days;
	public PLNTimestamp(int v) {
		val = v;
		days = val/2400;
		val %= 2400;
	}
	
	public void normalize() {
		if(val % 100 > 59)
		{
			val += 100;
			val -= 60;
			
			if(val > 2400)
			{
				days++;
				val -= 2400;
			}
		}
	}
	
	public String toString() {
		String t   = Integer.toString(val);
		while(t.length() < 4)
			t = '0'+t;
		return t.substring(0, 2)+":"+t.substring(2);
	}
	
	public String toLongString() {
		return ((days>0)?(Integer.toString(days)+" dni "):"")+toString();
	}
	
	public int intValue()
	{
		return val+days*2400;
	}
	
	public PLNTimestamp difference(PLNTimestamp b)
	{
		int va = val+days*2400;
		int oldh, oldm, newh, newm, resh, resm;
		
		oldh = va / 100;
		oldm = va % 100;
		
		va = b.val+b.days*2400;
		
		newh = va / 100;
		newm = va % 100;
		
		resh = oldh - newh;
		resm = oldm - newm;
		
		if(resm < 0)
		{
			resh--;
			resm = 60+resm;
		}
		
		return new PLNTimestamp(resh*100+resm);		
	}
}