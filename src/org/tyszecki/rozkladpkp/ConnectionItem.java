package org.tyszecki.rozkladpkp;

import org.tyszecki.rozkladpkp.PLN.Trip;

public class ConnectionItem {
	public class DateItem extends ConnectionItem{
		String date;
	}
	public class TripItem extends ConnectionItem{
		Trip t;
	}
	public class ScrollItem extends ConnectionItem{
		public ScrollItem(boolean up)
		{
			this.up = up;
		}
		boolean up;
	}
}
