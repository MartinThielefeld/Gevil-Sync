package syncZustand;

public class AktuellClientGevilVCard  extends GevilVCard {

	
	public AktuellClientGevilVCard(int androidId, String uid){
		super(androidId, uid);
		mUid = uid;
		mAndroidId=androidId;
	}
	
	
	final private String mUid;
	final private int mAndroidId;
	
	@Override
	  public String toString(){
		return "VCard wurde auf Grund der Etag Überprüfung als aktuell erkannt: ";// + mServerHref.getHref() + " eTag: " + mServerHref.getEtag();
		
	}

	
	@Override public int getAndroidId(){
		return mAndroidId;
	}
	
	@Override public String getUid(){
		return mUid;
		
	}
	
}
