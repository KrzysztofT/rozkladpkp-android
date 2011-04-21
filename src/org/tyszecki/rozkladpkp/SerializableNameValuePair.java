package org.tyszecki.rozkladpkp;

import java.io.Serializable;
import org.apache.http.NameValuePair;


/**
 * @author Krzysztof Tyszecki
 * Google nie chciało się update'ować tej biblioteki. Więc potrzebna kolejna klasa, tylko po to
 * żeby sobie serializację zrobić...
 */
public class SerializableNameValuePair implements NameValuePair, Serializable {

	private static final long serialVersionUID = -3076858765770770356L;

	String name,value;
	
	public SerializableNameValuePair(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
}
