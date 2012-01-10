package org.tyszecki.rozkladpkp.pln;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.tyszecki.rozkladpkp.pln.PLN.Availability;

import android.text.format.Time;

public class DayUtils {

	private PLN pln;
	private int count = -1;
	
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
			//TreeMap gwarantuje posortowanie wg klucza
			//TODO: Tutaj można dużo zoptymalizować, zmieniając sposób iteracji (po dostępnościach)
			daysMap = new TreeMap<Integer, List<Integer>>();
			
			for(int c = 0; c < pln.conCnt; ++c)
			{
				Availability a = pln.connections[c].availability;
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
					}		
				}
			}
		}
		count =  daysMap.size();
		return count;
	}
}
