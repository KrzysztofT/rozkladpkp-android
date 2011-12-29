package org.tyszecki.rozkladpkp.servers;

public class ServerManager {
	/**
	 *  Zwraca serwer o podanym priorytecie (0 = najwyższy) lub null, jeśli podano liczbę spoza zakresu
	 */
	public static HafasServer getServer(int priority)
	{
		switch (priority) {
		case 0: return new SitkolServer();
		case 1: return new DBServer();	
		default: return null;
		}
	}
}
