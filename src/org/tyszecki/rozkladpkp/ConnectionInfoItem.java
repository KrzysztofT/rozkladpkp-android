package org.tyszecki.rozkladpkp;

import org.tyszecki.rozkladpkp.PLN.Train;

public class ConnectionInfoItem {
	public class DateItem extends ConnectionInfoItem{
		String date;
	}
	public class TrainItem extends ConnectionInfoItem{
		Train t;
	}
}
