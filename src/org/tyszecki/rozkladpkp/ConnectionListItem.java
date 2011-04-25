package org.tyszecki.rozkladpkp;

import org.tyszecki.rozkladpkp.PLN.Trip;

public class ConnectionListItem {
	public class DateItem extends ConnectionListItem{
		String date;
	}
	public class TripItem extends ConnectionListItem{
		Trip t;
	}
	public class ScrollItem extends ConnectionListItem{
		public ScrollItem(boolean up)
		{
			this.up = up;
		}
		boolean up;
	}
}
