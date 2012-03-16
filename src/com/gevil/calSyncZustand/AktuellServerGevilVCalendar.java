package com.gevil.calSyncZustand;

import syncZustand.GevilVCard;
import syncZustand.GevilVCard.serverHref;

public class AktuellServerGevilVCalendar extends GevilVCalendar {
	
	
	@Override
	  public int compareTo(GevilVCalendar c) {
		// AktuellGevilVCards sind bereits aktuell. D.h. sie sind im Sync-Zustand auch aktuell und
		// müssen nciht aktualisiert werden.		
		return 0;
	}
	
	
	private serverHref mServerHref;
	public AktuellServerGevilVCalendar(serverHref serverHref) {
		super(serverHref);
		this.mServerHref = serverHref;		
	}

	
	@Override
	  public String toString(){
		
		return "VCard wurde auf Grund der Etag Überprüfung als aktuell erkannt: " + mServerHref.getHref() + " eTag: " + mServerHref.getEtag();
		
	}
	
	
	@Override public String getPropValue(String pName){
		return "Calendar Dummy";
	}
	
	private String dummyUID=null;
	@Override public void setUid(String Uid){
		dummyUID = Uid;
	}
	@Override public String getUid(){
		return dummyUID;
	}

	
	public String getHref(){
		return mServerHref.getHref().replace("UID:", "");
	}

	public String[] getN(){
		return new String[]{"VCard","Dummy"};
	}
	
	
}
