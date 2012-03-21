package org.tyszecki.rozkladpkp.pln;

import org.tyszecki.rozkladpkp.pln.PLN.Availability;
import org.tyszecki.rozkladpkp.pln.PLN.ConnectionChange;
import org.tyszecki.rozkladpkp.pln.PLN.Message;
import org.tyszecki.rozkladpkp.pln.PLN.Train;

/*
 * 
 * Klasa opisuje połączenie niepowiązane z momentem w czasie. Każde takie połączenie posiada listę dni, w jakich jest dostępne.
 * W przypadku formatu PLN jest to jednak rozwiązane nieco dziwnie - informacje o opóźnieniach i komunikaty powiązane są właśnie z tą klasą.
 * Przez to, obecność tych informacji oznacza, że każde połączenie dostępne jest tylko w jednym dniu.
 * 
 */

public class UnboundConnection {

	static final int ConnectionOffset = 0x4a; 	//Przesunięcie od którego zaczynają się dane o połączeniach
	static final int ConnectionSize = 12;		//Rozmiar struktury połączenia w bajtach
	
	static final int JourneyTimeOffset = 10;	//Offset czasu trwania podróży
	
	public  final PLN pln;			//obiekt PLN
	private final int fileOffset;   //Pozycja tego wpisu w pliku
	private final int trainsOffset; //Przesunięcie pociągów
	public  final int changesCount; //Liczba przesiadek
	public  final int trainCount; 	//Liczba pociągów w połączeniu
	public  final int number;		//Numer połączenia
	
	private final int messagesOffset; //Przesunięcie dodatkowych komunikatów
	public final Availability availability; //Określa dostępność pociągów w kolejnych dniach
	
	private PLNTimestamp journeyTime 	= null; //Czas podróży
	private Train[] trains 				= null; //Lista pociągów w połączeniu
	
	int changeOffset = -1;			 //Przesunięćie opóźnień
	private ConnectionChange change; //Opóźnienie połączenia
	
	
	UnboundConnection(PLN sourcePLN, int number) {
		pln 		= sourcePLN;
		fileOffset 	= ConnectionOffset+ConnectionSize*number;
		this.number = number;
		
		//Odczytaj i ustaw podstawowe pola
		changesCount	= pln.readShort(fileOffset+8);
		trainsOffset 	= ConnectionOffset+pln.readShort(fileOffset+2);
		trainCount 		= pln.readShort(fileOffset+6);
		
		//Powiąż z dostępnością
		availability 	= pln.availabilities.get(pln.readShort(fileOffset));
		
		//Sprawdź, czy z tym połączeniem powiązane są jakieś wiadomości
		messagesOffset  = pln.messages.offsetForConnection(number);
	}
	
	public PLNTimestamp getJourneyTime()
	{
		if(journeyTime == null)
			journeyTime = new PLNTimestamp(pln.readShort(fileOffset+JourneyTimeOffset));
		return journeyTime;
	}
	
	public Train getTrain(int index)
	{
		if(trains == null)
			trains = new Train[trainCount];
		
		else if(trains[index] != null)
			return trains[index];
		
		Train t = pln.readTrain(trainsOffset + index*pln.TrainSize);
		trains[index] = t;
		
		if(changeOffset != -1)
			t.changeOffset = changeOffset+8*(index+1);
		
		return t;
	}
	
	public Availability getAvailability() 
	{
		return availability;
	}

	public ConnectionChange getChange()
	{
		if(changeOffset == -1) 
			return null;
		if(change != null) 
			return change;
		
		if(changeOffset != -1) 
			change = pln.readConnectionChanges(changeOffset);
		
		return change;
	}
	
	public boolean hasMessages()
	{
		return (messagesOffset > 0);
	}
	
	public Message[] getMessages()
	{
		if(hasMessages()) 
			return pln.messages.get(messagesOffset);
		else 
			return null;
	}
}