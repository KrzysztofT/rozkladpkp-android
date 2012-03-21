package org.tyszecki.rozkladpkp.pln;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.tyszecki.rozkladpkp.pln.PLN.Availability;

import android.text.format.Time;
import android.util.Log;

public class DayUtils {

	private PLN pln;
	private int count = -1;
	private int connections = -1;
	//Mapa: Numer dnia → Lista połączeń w dniu (identyfikatory połączeń) 
	private Map<Integer, List <Integer>> daysMap = null; 
	
	public DayUtils(PLN input) {
		pln = input;
	}
	
	public Map<Integer, List <Integer>> getDaysMap()
	{
		if(daysMap == null)
			count();
		return daysMap;
	}
	
	public Time getDay(int number)
	{
		Time ret = new Time(pln.sdate);
		ret.monthDay += number;
		ret.normalize(false);
		return ret;
	}
	
	/**
	 * Zwraca liczbę dni o których są dane w rozkładzie.
	 * Tylko tych dni, w które jeżdżą pociągi.
	 */
	public int count()
	{
		if(count == -1)
		{
			connections = 0;
			
			//TreeMap gwarantuje posortowanie wg klucza
			//TODO: Tutaj można dużo zoptymalizować, zmieniając sposób iteracji (po dostępnościach)
			daysMap = new TreeMap<Integer, List<Integer>>();
			
			for(int c = 0; c < pln.conCnt; ++c)
			{
				Availability a = pln.connections[c].getAvailability();
				int j = a.dOffset*8;
				BitSet t = a.bitset();
				int len = a.bsLength();
				
				for(int i = 0; i < len; i++,j++)
				{
					boolean av = t.get(i);
					if(av)
					{
						if(!daysMap.containsKey(j))
							daysMap.put(j, new ArrayList<Integer>());
						daysMap.get(j).add(c);
						connections++;
					}		
				}
			}
		}
		count =  daysMap.size();
		return count;
	}
	
	public int totalConnectionCount()
	{
		if(connections == -1)
			count();
		return connections;
	}
	
	public Iterable<Connection> getConnectionIterator()
	{
		return new Iterable<Connection>() {
			@Override
			public Iterator<Connection> iterator() {
				return new ConnectionIterator();
			}
		};
	}
	
	public class ConnectionIterator implements Iterator<Connection>
	{
		private Object[] array;
		private int connectionIndex, dayIndex, connectionsPresentDay = 0;
		Entry<Integer, List<Integer>> item = null;

		public ConnectionIterator() {
			array = getDaysMap().entrySet().toArray();
			dayIndex = 0;
			connectionIndex = 0;
			
			if(hasNext())
				nextDay();
		}
		@Override
		public boolean hasNext() {
			//Zwróć prawdę, jeśli jest jeszcze jakiś dzień, lub nie skończyliśmy iterować obecnego dnia
			return dayIndex < array.length || connectionIndex < connectionsPresentDay;
		}
		
		private void nextDay()
		{
			item = (Entry<Integer, List<Integer>>) array[dayIndex++];
			connectionsPresentDay = item.getValue().size();
		}

		@Override
		public Connection next() {
			
			if(connectionIndex >= connectionsPresentDay)
			{
				connectionIndex = 0;
				nextDay();
			}
			
			Log.i("RozkladPKP", "IDX: "+Integer.toString(connectionIndex)+", D:"+item.getKey().toString());
			return new Connection(pln.connections[item.getValue().get(connectionIndex++)], item.getKey());
		}

		@Override
		public void remove() {}
	}
}
