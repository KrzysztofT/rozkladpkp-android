package org.tyszecki.rozkladpkp;

import org.tyszecki.rozkladpkp.PLN.Train;

public class ConnectionDetailsItem {
	public class DateItem extends ConnectionDetailsItem{
		String date;
	}
	public class TrainItem extends ConnectionDetailsItem{
		Train t;
	}
}
