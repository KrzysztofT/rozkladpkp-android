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
package org.tyszecki.rozkladpkp.pln;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import android.util.Log;

public class PLN {
	
	public static final int DATE_GENERATED = 0;
	public static final int DATE_START = 1;
	public static final int DATE_END = 2;
	
	
	
	int attributesStart,attributesEnd;
	int stationsStart;
	int stringStart,availabilitiesStart;

	
	final int TrainSize = 20;
	final int StationSize = 14;
	
	
	private StringManager strings;
	private AttributeManager attributes;
	MessageManager messages;
	
	public UnboundConnection[] connections;
	private Station[] stations;
	private Station dep,arr; 
	private int totalConCnt = -1;
	HashMap<Integer,Availability> availabilities;
	
	private DayUtils dayUtils;
	
	public int conCnt;
	Boolean delayInfo = null;

	public byte[] data;
	public android.text.format.Time sdate,edate,today;
	
	class StringManager{
		HashMap<Integer,String> cache = new HashMap<Integer, String>();
		
		String get(int offset)
		{
			if(cache.containsKey(offset))
				return cache.get(offset);
			
			String t = getString(offset+stringStart);
			cache.put(offset, t);
			return t;
		}
	}
	
	class AttributeManager{
		private HashMap<Integer,String[]> cache = new HashMap<Integer, String[]>();
		
		String[] get(int offset)
		{
			if(cache.containsKey(offset))
				return cache.get(offset);
			
			String[] t = readAttributeList(offset);
			cache.put(offset, t);
			return t;
		}
	}
	
	class MessageManager{
		
		private final int messagesOffset;
		
		private HashMap<Integer,Message[]> cache = new HashMap<Integer, Message[]>();
		private SortedSet<Integer> offsets = new TreeSet<Integer>();
		
		public MessageManager() 
		{
			messagesOffset = readShort(attributesEnd+0x16);
		}
		
		void addOffset(int offset)
		{
			offsets.add(offset);
		}
		
		Message[] get(int offset)
		{			
			if(cache.containsKey(offset))
				return cache.get(offset);
			
			int n = data.length; 
			//Następny offset
			try{n = offsets.tailSet(offset+1).first();}catch (Exception e) {}
			
			int cnt = (n-offset)/18;
			Message[] t = new Message[cnt];
			
			for(int i = 0; i < cnt; ++i)
				t[i] = readMessage(offset+i*18);
			
			cache.put(offset, t);
			return t;
		}
		
		//Zwraca przesunięcie komunikatów dla konkretnego połączenia  
		public int offsetForConnection(int connectionNumber)
		{
			if(messagesOffset <= 0)
				return -1;
			
			int msg = readShort(messagesOffset+2*(connectionNumber+1));
			
			if(msg > 0)
			{	
				msg += messagesOffset;
				addOffset(msg);
				return msg;
			}
			return -1;
		}
	}
	
	public class Availability{
		private int length;
		private int bsLen = -1;
		private int card = 0;
		public Availability(int m,int offset, int dayOffset, int len) {
			msgOffset = m;
			dOffset = dayOffset;
			days	= new BitSet(len*8);
			int ix = 0;
			
			for(int i = 0; i < len; i++)
				for(int j = 7; j >= 0; j--, ix++)
					if(((data[offset+i] >>j) & 1) != 0)
					{
						days.set(ix);
						++card;
					}
			
			length = len*8;
		}
		
		public boolean available(int day)
		{
			day -= dOffset*8;
			if(day >= 0 && day < length)
				return days.get(day);
			return false;
		}
		
		public int length()
		{
			return dOffset*8+days.size();
		}
		
		public int daysCount()
		{
			return card;
		}
		
		public BitSet bitset()
		{
			return days;
		}
		
		public int offset()
		{
			return dOffset;
		}
		
		public int bsLength()
		{
			if(bsLen == -1)
				bsLen = days.length();
			return bsLen;
		}
		
		public String getMessage()
		{
			if(msg == null)
				msg = strings.get(msgOffset);
			return msg;
		}
		private String msg;
		private BitSet days;
		int dOffset;
		private int msgOffset;
	}
	
	public class Station {
		int x,y;
		public int id;
		public String name;
		
		public String toString()
		{
			return name;
		}
	}
	
	public class Train {
		public PLNTimestamp deptime;
		public PLNTimestamp arrtime;
		private int offset;
		public String number;
		public Station depstation;
		public Station arrstation;
		private String attr[] = null;
		private String depplatform, arrplatform;
		
		int changeOffset = -1;
		private int attributesOffset;
		private TrainChange change;
		
		public TrainChange getChange()
		{
			if(changeOffset == -1)
				return null;
			if(change != null)
				return change;
			
			if(changeOffset != -1)
				change = readTrainChanges(changeOffset);
			
			return change;
		}
		
		public int getAttributeCount()
		{
			if(attr == null)
				attr = attributes.get(attributesOffset);
			return attr.length;
		}
		
		public String getDeparturePlatform()
		{
			if(depplatform == null)
				depplatform = strings.get(readShort(offset+12));
			return depplatform;
		}
		
		public String getArrivalPlatform()
		{
			if(arrplatform == null)
				arrplatform = strings.get(readShort(offset+14));
			return arrplatform;
		}
		
		public String getAttribute(int index)
		{
			if(attr == null)
				attr = attributes.get(attributesOffset);
			return attr[index];
		}
	}
	
	public class Message {
		String start;
		String end;
		public String brief;
		public String full;
	}
	
	public class TrainChange {
		public PLNTimestamp realdeptime;
		public PLNTimestamp realarrtime;
		String realdepplatform,realarrplatform;
	}
	
	public class ConnectionChange {
		public int departureDelay;
	}
	
	public PLN(byte[] byte_data) {
		data = byte_data;
		
		stationsStart	= readShort(0x36);
		attributesStart	= readShort(0x3a);
		attributesEnd	= readShort(0x3e);
		conCnt			= readShort(0x1e);
		
		strings = new StringManager();
		attributes = new AttributeManager();
		messages = new MessageManager();
		
		setupDates();
		readStringTable();
		readStations();
		readHeaderStations();
		readAvailabilities();
		readConnections();
	}
	
	public Station departureStation(){
		return dep;
	}

	public Station arrivalStation(){
		return arr;
	}
	
	public String id()
	{
		return strings.get(readShort(attributesEnd+0xc));
	}
	
	public String ld()
	{
		try{
		return strings.get(readShort(attributesEnd+0xc)+id().length()+1);
		}
		catch(Exception e)
		{
			return "hw1";
		}
	}
	
	public int connectionCount()
	{
		if(totalConCnt == -1)
		{
			++totalConCnt;
			for(UnboundConnection c : connections)
				totalConCnt += c.getAvailability().daysCount();
		}
		return totalConCnt;
	}
	
	
	//Tak naprawdę, to jest to czytanie 'unsigned short' a nie 'int'.
	//Jednak zwracamy int, ponieważ short javowy nie zmieści wszystkich wartości
	//unsigned short
	int readShort(int pos)
	{
		int r =  (int) (data[pos] & 0x000000FF);
		r += ((int) (data[pos+1] & 0x000000FF))*256;
		
		return r;
	}
	
	private void saveShort(int pos, int value)
	{
		data[pos+1] = (byte) ((value & 0x0000FF00L) >> 8);
		data[pos] = (byte) ((value & 0x000000FFL));
	}
	
	
	private int readLong(int pos)
	{
		int r =  (int) (data[pos] & 0x000000FF);
		r += ((int) (data[pos+1] & 0x000000FF))*256;
		r += ((int) (data[pos+2] & 0x000000FF))*65536;
		r += ((int) (data[pos+3] & 0x000000FF))*16777216;
		
		return r;
	}
	
	
	private String getString(int pos)
	{
		int start = pos; 
			
		while(data[pos] != 0)
			pos++;
		
		byte[] b = new byte[pos-start];
		
		pos = start;
		start = 0;
		
		while(data[pos] != 0)
			b[start++] = data[pos++];
		
		try {
			return new String(b,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private void setupDates()
	{
		sdate = new android.text.format.Time();
		sdate.year = 1979;
		sdate.month = 11;
		sdate.monthDay = 31;
		
		edate = new android.text.format.Time(sdate);
		today = new android.text.format.Time(sdate);
		
		sdate.monthDay += readShort(0x28);
		edate.monthDay += readShort(0x2a);
		today.monthDay += readShort(0x2c);
		
		sdate.normalize(false);
		edate.normalize(false);
		today.normalize(false);
	}
	
	Train readTrain(int pos)
	{
		Train r = new Train();
		
		r.offset = pos;
		r.deptime	= new PLNTimestamp(readShort(pos));
		r.depstation = stations[readShort(pos+2)];
		r.arrtime	= new PLNTimestamp(readShort(pos+4));
		r.arrstation = stations[readShort(pos+6)];
		r.number	= strings.get(readShort(pos+10));
		
		r.attributesOffset = readShort(pos+18);
		
		return r;
	}



	private void readStringTable()
	{
		stringStart = readShort(0x24);
		availabilitiesStart	  = readShort(0x20);
	}
	
	private void readHeaderStations()
	{
		dep = readHeaderStation(2);
		arr = readHeaderStation(0x10);
	}
	
	private Station readHeaderStation(int offset)
	{
		int nameLoc = readShort(offset);
		if(nameLoc == 0)
			return null;
		
		Station ret = new Station();
		
		ret.name = strings.get(nameLoc);
		ret.x = readLong(offset+6);
		ret.y = readLong(offset+10);
		
		return ret;
	}
	
	private void readStations() {
			
		stations = new Station[(attributesStart-stationsStart)/StationSize];
	
		for(int i = 0, pos = stationsStart; pos < attributesStart; pos += StationSize, i++)
		{
			stations[i] = new Station();
			stations[i].name = strings.get(readShort(pos));
			stations[i].id	= readLong(pos+2);
			stations[i].x	= readLong(pos+6);
			stations[i].y	= readLong(pos+10);
		}
	}
	
	private void readAvailabilities() {
		
		availabilities	= new HashMap<Integer, Availability>();
		
		for(int i = 0, pos = availabilitiesStart; pos < stationsStart; i++, pos += 6)
		{
			int len = readShort(pos+4);
			int dOffset = readShort(pos+2);
			availabilities.put(pos-availabilitiesStart, new Availability(readShort(pos),pos+6,dOffset,len));
			
			pos += len;
		}
		
		
	}
	
	private String[] readAttributeList(int offset) {
		int pos = offset + attributesStart;
		
		int cnt = readShort(pos);
		String[] tab = new String[cnt];

		int p = pos+2;
		for(int i = 0; i < cnt; i++,p+=2)
			tab[i] = strings.get(readShort(p));

		return tab;
	}

	
	private void readConnections() {
		
		connections = new UnboundConnection[conCnt];
		
		int chinfo = readShort(attributesEnd+0xe);
		
		boolean hasChanges = (chinfo != 0);
		if(hasChanges)
			chinfo += conCnt*2+4;
		
		for(int i = 0; i < conCnt; i++)
		{
			connections[i] = new UnboundConnection(this, i);
			if(hasChanges)
			{
				connections[i].changeOffset = chinfo;
				chinfo += (connections[i].trainCount+1)*8;
			}
		}
	}
	
	private TrainChange readTrainChanges(int offset) {
		
		int rd = readShort(offset);
		int ra = readShort(offset+2);
		int rdp = readShort(offset+4);
		int rap = readShort(offset+6);
		
		if(ra == 0xffff && rd == 0xffff && rdp == 0 && rap == 0)
			return null;
		
		delayInfo = true;
		
		TrainChange tc = new TrainChange();
		tc.realdeptime = (rd == 0xffff) ? null : new PLNTimestamp(rd);
		tc.realarrtime = (ra == 0xffff) ? null : new PLNTimestamp(ra);
		tc.realdepplatform = (rdp == 0) ? null : strings.get(rdp);
		tc.realarrplatform = (rap == 0) ? null : strings.get(rap);
		
		return tc;
	}
	

	public Message readMessage(int offset) {
		
		Message r = new Message();
		
		int t = readShort(offset+8);
		r.end = (t == 0) ? null : strings.get(t);
		
		t = readShort(offset+6);
		r.start = (t == 0) ? null : strings.get(t);
		
		r.brief = strings.get(readShort(offset+12));
		r.full = strings.get(readShort(offset+14));
		
		return r;
	}

	ConnectionChange readConnectionChanges(int offset) {
		int delay = readShort(offset+2);
		if(delay == 255)
			return null;
		
		ConnectionChange ch = new ConnectionChange();
		ch.departureDelay = delay;	
	
		delayInfo = true;
		
		return ch;
	}

	public boolean hasDelayInfo()
	{
		if(delayInfo == null)
		{
			int chinfo = readShort(attributesEnd+0xe);
			if(chinfo == 0)
				delayInfo = false;
			else
				delayInfo = hasDelay(chinfo + conCnt*2+4);
		}
		return delayInfo;
	}
	
	boolean hasDelay(int p)
	{
		//0,0,xff,0,xff,xff,xff,xff <- puste polaczenie
		//xff,xff,xff,xff,0,0,0,0 <- pusty pociag
		for(int i = 0; i < conCnt; ++i)
		{
			if(data[p++] != 0) return true;
			if(data[p++] != 0) return true;
			if(data[p++] != -1) return true;
			if(data[p++] != 0) return true;
			
			for(int k = 0; k < 4; ++k)
				if(data[p++] != -1) return true;
			
			for(int j = 0; j < connections[i].trainCount; ++j)
			{
				for(int k = 0; k < 4; ++k)
					if(data[p++] != -1) return true;
				for(int k = 0; k < 4; ++k)
					if(data[p++] != 0) return true;
			}
		}	
		return false;
	}
	
	public DayUtils days()
	{
		if(dayUtils == null)
			dayUtils = new DayUtils(this);
		return dayUtils;
	}
	
	public void addExternalDelayInfo(HashMap<String,Integer> delays)
	{
		for(int i = 0; i < conCnt; i++)
		{
			UnboundConnection c = connections[i];
			for(int j = 0; j < c.trainCount; ++j)
			{
				Train t = c.getTrain(j);
				if(delays.containsKey(t.number))
				{
					delayInfo = true;
					
					int arr = t.arrtime.intValue();
					int mindel = delays.get(t.number);
					int hrs = mindel/60; 
					arr += hrs*100;
					mindel -= hrs*60;
					arr += mindel;
					
					PLNTimestamp real = new PLNTimestamp(arr);
					real.normalize();
					
					saveShort(t.changeOffset+2, real.intValue());
					
					int dep = t.deptime.intValue();
					dep += hrs*100;
					dep += mindel;
					
					real = new PLNTimestamp(dep);
					real.normalize();
					
					saveShort(t.changeOffset, real.intValue());
					
					//Ew. opoznienie polaczenia
					if(j == 0)
						saveShort(c.changeOffset+2, mindel);
				}
			}
		}
	}
}
