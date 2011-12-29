package org.tyszecki.rozkladpkp.servers;

import java.util.ArrayList;

import org.tyszecki.rozkladpkp.CommonUtils;
import org.tyszecki.rozkladpkp.SerializableNameValuePair;

public class DBServer extends HafasServer {

	@Override
	public String name() {
		return "Deutsche Bahn";
	}

	@Override
	public ArrayList<SerializableNameValuePair> prepareFields(ArrayList<SerializableNameValuePair> input) {
		//Serwer DB nie lubi polskich znaków
		ArrayList<SerializableNameValuePair> ret = new ArrayList<SerializableNameValuePair>();
		
		for(SerializableNameValuePair p : input)
		{
			if(p.getName().endsWith("ID"))
				ret.add(new SerializableNameValuePair(p.getName(), CommonUtils.depol(p.getValue())));
			else
				ret.add(p);
		}
		return ret;
	}

	@Override
	public String url(int type) {
		if(type == HafasServer.URL_CONNECTIONS)
			return "http://reiseauskunft.bahn.de/bin/query.exe/pn";
		return null;
	}

}
