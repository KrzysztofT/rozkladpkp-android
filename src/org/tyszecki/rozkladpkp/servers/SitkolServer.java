package org.tyszecki.rozkladpkp.servers;

import java.util.ArrayList;

import org.tyszecki.rozkladpkp.SerializableNameValuePair;

public class SitkolServer extends HafasServer{

	@Override
	public String name() {
		return "Sitkol";
	}

	@Override
	public ArrayList<SerializableNameValuePair> prepareFields(ArrayList<SerializableNameValuePair> input) {
		return input;
	}

	@Override
	public String url(int type) {
		if(type == HafasServer.URL_CONNECTIONS)
			return "http://rozklad.sitkol.pl/bin/query.exe/pn";
		return null;
	}

}
