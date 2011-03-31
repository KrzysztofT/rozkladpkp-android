package org.tyszecki.rozkladpkp;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class PLN {
	
	final int ConnectionOffset = 0x4a;
	final int ConnectionSize = 12;
	int attributesStart,attributesEnd;
	int stationsStart;
	int stringStart,availabilitiesStart;

	
	final int TrainSize = 20;
	final int StationSize = 14;
	
	private HashMap<Integer,String> strings;
	private HashMap<Integer,String[]> attributes;
	
	public Connection[] connections;
	private Station[] stations;
	private Station dep,arr; 
	private int totalConCnt = -1;
	private int actualDaysCount = -1;
	private HashMap<Integer,Availability> availabilities;
	
	Pattern p = Pattern.compile("(TLK|D|EN|EC|KD|IR|RE|EIC).*");
	
	public int conCnt;

	byte[] data;
	private Calendar sdate,edate,today;
	
	public static String combine(String[] s, String glue)
	{
		if (s == null)
			return "[null]";
		int k = s.length;
		
		if (k==0)
			return null;
		
		StringBuilder out = new StringBuilder();
		out.append(s[0]);
		
		for (int x=1;x<k;++x)
			out.append(glue).append(s[x]);
		
		return out.toString();
	}
	
	class Time{
		int val;
		int days;
		public Time(int v) {
			val = v;
			days = val/2400;
			val %= 2400;
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
	}
	
	class Availability{
		public Availability(String m,int offset, int dayOffset, int len) {
			msg = m;
			dOffset = dayOffset;
			days	= new BitSet(len*8);
			int ix = 0;
			
			for(int i = 0; i < len; i++)
				for(int j = 7; j >= 0; j--)
					days.set(ix++, ((data[offset+i] >>j) & 1) != 0);
			
		}
		
		public boolean available(int day)
		{
			day -= dOffset*8;
			if(day >= 0 && day < days.length())
				return days.get(day);
			return false;
		}
		
		public int length()
		{
			return dOffset*8+days.size();
		}
		
		public int daysCount()
		{
			return days.cardinality();
		}
		
		public BitSet bitset()
		{
			return days;
		}
		
		public int offset()
		{
			return dOffset;
		}
		String msg;
		private BitSet days;
		private int dOffset;
	}
	
	public class Station {
		int x,y,id;
		String name;
		
		public String toString()
		{
			return name;
		}
	}
	
	public class Train {
		Time deptime,arrtime;
		String number;
		Station depstation,arrstation;
		String attributes[];
		
		public String toString()
		{
			return depstation+" ("+deptime+") "+arrstation+" ("+arrtime+"); "+combine(attributes,",");
		}
		
		public String getType()
		{
            Matcher m = p.matcher(number);
            
            if(m.matches())
            	return m.group(1);
            return "OS";
		}
	}
	
	public class Message {
		Station start,end;
		String brief,full;
	}
	
	public class TrainChange {
		Time realdeptime,realarrtime;
		String realdepplatform,realarrplatform;
	}
	
	public class Connection {
		public int changes,trOffset;
		Time journeyTime;
		Train[] trains;
		Message[] messages;
		Availability availability;
		
		public String toString()
		{
			String str = journeyTime+" ("+Integer.toString(changes)+") ch";
			for(int i = 0; i < trains.length; i++)
				str += "\n"+trains[i];
			return str;
		}
	}
	
	public class Trip {
		public Trip(Connection connection, int idx, String d) {
			con = connection;
			date = d; 
			conidx = idx;
		}
		String date;
		Connection con;
		//FIXME: Wymyśleć, jak zastąpić to, żeby było ładnie.
		int conidx;
	}
	
	public class TripIterator implements Iterator<Trip>{
		int pos = 0;
		int max = -1;
		
		public TripIterator()
		{
			for(int i = 0; i < conCnt; ++i)
				if(connections[i].availability.length() > max)
					max = connections[i].availability.length();
		}
		
		@Override
		public boolean hasNext() {
			
			//Nie ma żadnych połączeń
			if(conCnt == 0) 
				return false;
			
			int cix = pos%conCnt;
			int dix = pos/conCnt;
			
			while(dix <= max)
			{
				if(connections[cix].availability != null && connections[cix].availability.available(dix))
					return true;
				else
				{
					pos++;
					cix = pos%conCnt;
					dix = pos/conCnt;
				}
			}
			
			return false;
		}

		@Override
		public Trip next() {
			if(hasNext())
			{
				int cix = pos%conCnt;
				int dix = pos/conCnt;
				GregorianCalendar cal = (GregorianCalendar) sdate.clone();
				cal.add(Calendar.DAY_OF_MONTH, dix);
				pos++;
				return new Trip(connections[cix], cix, new SimpleDateFormat("dd.MM.yyyy").format(cal.getTime()));
			}
			else
				return null;
		}

		@Override
		public void remove() {			
		}
		
	}
	
	public PLN(byte[] byte_data) {
		data = byte_data;
		strings = new HashMap<Integer, String>();
		attributes = new HashMap<Integer, String[]>();
		

		stationsStart	= readint(0x36);
		attributesStart	= readint(0x3a);
		attributesEnd	= readint(0x3e);
		
		Log.i("PLN","Daty..."); 
		setupDates();
		Log.i("PLN","Stringi...");
		readStringTable();
		Log.i("PLN","Stacje...");
		readStations();
		readHeaderStations();
		Log.i("PLN","Atrybuty...");
		readAttributes();
		Log.i("PLN","Dostępność...");
		readAvailabilities();
		Log.i("PLN","Połączenia...");
		readConnections();
		//readMessages();
	}

	public TripIterator tripIterator(){
		return new TripIterator();
	}
	
	public Station departureStation(){
		return dep;
	}

	public Station arrivalStation(){
		return arr;
	}
	
	public String id()
	{
		return strings.get(10);
	}
	
	public int connectionCount()
	{
		if(totalConCnt == -1)
		{
			++totalConCnt;
			for(Connection c : connections)
				totalConCnt += c.availability.daysCount();
		}
		return totalConCnt;
	}
	
	public int daysCount()
	{
		if(actualDaysCount == -1)
		{
			BitSet res = new BitSet();
			for(Connection c : connections)
			{
				Availability a = c.availability;
				int j = a.dOffset*8;
				BitSet t = a.bitset();
				
				for(int i = 0; i < t.length(); i++,j++)
					res.set(j,t.get(i));
				
			}
			actualDaysCount = res.cardinality();
		}
		return actualDaysCount;
	}

	private int readint(int pos)
	{
		int r =  (int) (data[pos] & 0x000000FF);
		r += ((int) (data[pos+1] & 0x000000FF))*256;
		
		return r;
	}
	
	//In theory, this method should return 'long', but IDs and gps coordinates never go so big
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	private Connection readConnection(int pos)
	{
		Connection ret = new Connection();
		int tcnt	= readint(pos+6);
		int toff	= readint(pos+2);
		ret.changes	= readint(pos+8);
		ret.journeyTime	= new Time(readint(pos+10));

		//Log.i("PLN","Trains: "+Integer.toString(tcnt)+" offset: "+Integer.toString(toff)+" changes: "+Integer.toString(ret.changes)+" time: "+ret.journeyTime);
		
		ret.trains = readTrains(toff,tcnt);
		ret.availability = availabilities.get(readint(pos));
			
		return ret;
	}
	
	private void setupDates()
	{
		sdate = new GregorianCalendar();
		sdate.set(1979, 11, 31);
		edate = (Calendar) sdate.clone();
		today = (Calendar) sdate.clone();
		
		sdate.add(Calendar.DATE, readint(0x28));
		edate.add(Calendar.DATE, readint(0x2a));
		today.add(Calendar.DATE, readint(0x2c));
	}
	
	private Train[] readTrains(int toff, int tcnt) {
		Train[] ret = new Train[tcnt];
		int pos = ConnectionOffset+toff;
		for(int i = 0; i < tcnt; i++)
		{
			ret[i] = new Train();
			ret[i].deptime	= new Time(readint(pos));
			ret[i].depstation = stations[readint(pos+2)];
			ret[i].arrtime	= new Time(readint(pos+4));
			ret[i].arrstation = stations[readint(pos+6)];
			ret[i].number	= strings.get(readint(pos+10));
			
			ret[i].attributes = attributes.get(readint(pos+18));
			pos+=TrainSize;
		}
		return ret;
	}



	private void readStringTable()
	{
		stringStart = readint(0x24);
		availabilitiesStart	  = readint(0x20);
		
		int pos   = stringStart;
		
		while(pos < availabilitiesStart)
		{
			String t = getString(pos);
			
			strings.put(pos-stringStart, t);
			//Log.i("PLN","String: "+Integer.toString(pos)+" "+t);
			pos += t.length()+1;
		}
	}
	
	private void readHeaderStations()
	{
		dep = readHeaderStation(2);
		arr = readHeaderStation(0x10);
	}
	
	private Station readHeaderStation(int offset)
	{
		int nameLoc = readint(offset);
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
			stations[i].name = strings.get(readint(pos));
			stations[i].id	= readLong(pos+2);
			stations[i].x	= readLong(pos+6);
			stations[i].y	= readLong(pos+10);
			
			Log.i("PLN","Station: "+stations[i].name+"["+readint(pos)+"] ("+Integer.toString(stations[i].id)+")");
		}
	}
	
	private void readAvailabilities() {
		availabilities	= new HashMap<Integer, Availability>();
		
		for(int i = 0, pos = availabilitiesStart; pos < stationsStart; i++, pos += 6)
		{
			int len = readint(pos+4);
			availabilities.put(pos-availabilitiesStart, new Availability(strings.get(readint(pos)),pos+6,readint(pos+2),len));
			pos += len;
		}
	}
	
	private void readAttributes() {
		
		for(int pos = attributesStart; pos < attributesEnd;)
		{
			int cnt = readint(pos);
			String[] tab = new String[cnt];
			
			int p = pos+2;
			for(int i = 0; i < cnt; i++,p+=2)
				tab[i] = strings.get(readint(p));
			
			attributes.put(pos-attributesStart, tab);
			pos = p;
		}

	}
	
	private void readMessages() {
		int pos = readint(attributesEnd+0x16);
		//TODO: investigate how these messages are connected with trains
		//No messages
		if(pos == 0)
			return;
	}

	private void readConnections() {
		conCnt	= readint(0x1e);
		//Log.i("PLN","Connections: "+Integer.toString(conCnt));
		connections = new Connection[conCnt];
		
		for(int i = 0; i < conCnt; i++)
		{
			//Log.i("PLN","Connection: "+Integer.toString(i));
			connections[i] = readConnection(ConnectionOffset+ConnectionSize*i);
		}
	}
}