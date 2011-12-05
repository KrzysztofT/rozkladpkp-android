package org.tyszecki.rozkladpkp;

import java.io.Serializable;

import android.os.Bundle;

public class EnhancedBundle extends Object {
	
	Bundle b;
	public EnhancedBundle(Bundle source) {
		b = source;
	}
	
	String getString(String key)
	{
		if (b == null) return null;
		else return b.getString(key);
	}
	
	String getString(String key, String defaultValue)
	{
		if (b == null) return defaultValue;
		else return b.containsKey(key) ? b.getString(key) : defaultValue;
	}
	
	Serializable getSerializable(String key)
	{
		if (b == null) return null;
		else return b.getSerializable(key);
	}
	
	boolean containsKey(String key)
	{
		if (b == null) return false;
		else return b.containsKey(key);
	}
}
