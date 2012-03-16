package syncZustand;

import java.net.URISyntaxException;

public class AktuellServerGevilVCard extends GevilVCard {

	private serverHref mServerHref;
	
	@Override
	  public int compareTo(GevilVCard c) {
		// AktuellGevilVCards sind bereits aktuell. D.h. sie sind im Sync-Zustand auch aktuell und
		// müssen nciht aktualisiert werden.		
		return 0;
	}
	
	
	/**
	 * Konstruktor
	 * @param h
	 */
	public AktuellServerGevilVCard(serverHref h) {
		super(h);
		mServerHref = h;
		
	}
	
	
	/*
	@Override
	public void setUid(String uid)throws URISyntaxException{ 
		System.out.println("folgende UID wird der AktuallGevilVCard gesetzt: " + uid);
		//if(!"4ea958de-b25-1".equals(uid)) 
		//if(!"AndroidId:3".equals(uid))
		//if("AndroidId=3A3".equals(uid))throw new ArrayIndexOutOfBoundsException();

		//if(uid.contains("3A3"))throw new ArrayIndexOutOfBoundsException();
		
	}
	*/
	
	
	@Override
	  public String toString(){
		return "VCard wurde auf Grund der Etag Überprüfung als aktuell erkannt: " + mServerHref.getHref() + " eTag: " + mServerHref.getEtag();
		
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
