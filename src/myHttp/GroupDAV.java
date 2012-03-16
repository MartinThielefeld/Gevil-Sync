package myHttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;

import org.apache.commons.lang.NotImplementedException;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import syncZustand.AktuellServerGevilVCard;
import syncZustand.GevilVCard;
import syncZustand.GevilVCard.serverHref;
import syncZustand.TypeBiMap;


import com.gevil.OptionsLoader;
import com.gevil.calSyncZustand.AktuellServerGevilVCalendar;
import com.gevil.calSyncZustand.GevilVCalendar;
import com.gevil.syncadapter.Constants;

import WorkarroundExternalBugs.Workarounds;
import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;



public class GroupDAV {
	
	private final String TAG = "GroupDAV";	
	
	TypeBiMap mMap;
	
	
	
	WebDAV mWebDAV;

	private String mUsername;

	private Context mContext;
		
	/**
	 * Konstruktor
	 * @param username
	 * @param password
	 */
	public GroupDAV(String username, String password, TypeBiMap map, Context ctx){
		this.mUsername = username;
		this.mContext = ctx;
		mWebDAV = new WebDAV(username,password, mMap);
		mMap = map;
		
	}
		
		
		
	public void testmain(TextView tf1){
		WebDAV http = new WebDAV("Canton", "Chrono", mMap);		
		tf1.setText("");
		
		/*System.out.println("+starte hochladen: ");
		String URL = ConstantsServerAdressen.CONTACTS_SERVER_URL + "8271-1281-2871-2714.vcf";
		try {
			http.PutVCard(BSPVcard, URL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("+ Exception in "+ TAG + " testmain :" + e);
			e.printStackTrace();
		}
		
		
	
		ArrayList<GevilVCard> cal = this.getContacts();
		
		if(cal!=null){
			for(int i = 0; i<cal.size(); i++){
				if(cal.get(i) != null) tf1.setText(tf1.getText() + "\n \n: " + cal.get(i).toString() );
			}
		}else{
			tf1.setText(tf1.getText() + "cal==null:)" );
		}

		*/
		

		
		
		
		/*
		 * herunterladen eines Beispielkontaktes.
		 * 
		try {
			String c = http.Get("http://192.168.239.128/groupdav/Contacts/4e70c94b-9fc-1", http.getCredentials());			
			tf1.setText(tf1.getText() + "\nvom Server erhalten: \n" + c);			
			
		} catch (Exception e) {
			tf1.setText(tf1.getText() + "\nExc in httpGet: " + e );
		}
		*/
		
		//VcardTEST t = new VcardTEST();
		//t.vcTEST();
		//getCalendar(http);
	}
	
	
	/*
	public void oldgetCalendar(WebDAV http){		
		try {	
			String URL = Constants.CALENDAR_SERVER_URL;
			BufferedReader xmlBR = http.PROPFIND(URL, http.getCredentials());
			//String[] hrefs = this.getHref(xmlBR);
			String[] hrefs = null;
			
			for(int i = 0; i<hrefs.length;i++){
				System.out.println("+@getCalendar: " + hrefs[i]);
				// Entsprechende hrefs mit get runterladen => vcalendar oder sowas...
				try{					
					if(URL != hrefs[i]){
						String c = http.Get(hrefs[i], http.getCredentials());						
						System.out.println("+Kontakt: \n");
						System.out.print(c);
					}
					// TODO aufpassen dass keine Dateien auf dem Server überschrieben werden.
				}catch(Exception e){
					System.out.println("Exc in getContacts: " + e);
				}
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
	
	/*
	public ArrayList<GevilVCard> getCalendar(WebDAV http){
		ArrayList<GevilVCard> ret = new ArrayList<GevilVCard>(0);
		
		ContactsFromServerReader reader = this.getContactsIterator();
		while(reader.hasNext()){
			GevilVCard VC = reader.next();
			ret.add(VC);
		}
		
		return ret;
		
	}
	*/
	
	/*
	public net.fortuna.ical4j.model.Calendar[] getCalendar(WebDAV http){	
		
		try {	
			String URL = Constants.CALENDAR_SERVER_URL;
			BufferedReader xmlBR = http.PROPFIND(URL, http.getCredentials());
			String[] hrefs = this.getHref(BufferReaderToString(xmlBR,false));
			
			net.fortuna.ical4j.model.Calendar[] ret = new  net.fortuna.ical4j.model.Calendar[hrefs.length-1];
			int j=0;
			
			for(int i = 0; i<hrefs.length;i++){
				System.out.println("+@getCalendar: " + hrefs[i]);				
				System.out.println("+@getCalendar xml File:\n" + xmlBR);
				 //Entsprechende hrefs mit get runterladen und eine VCard erstellen
				try{					
					if(URL != hrefs[i]){
						String c = http.Get(hrefs[i], http.getCredentials());						
						System.out.println("+Kontakt: \n");
						System.out.print(c);
						
						// in Kalenderarray ret einfügen
						ret[j] = new net.fortuna.ical4j.model.Calendar();
						 // TODO Kontakt in Vcalendar konvertieren une einfügen.
						j++;
						
					}
					// TODO aufpassen dass keine Dateien auf dem Server überschrieben werden.
				}catch(Exception e){
					System.out.println("Exc in getCalendar: " + e);
				}
			}
			return ret;
			
			
		} catch (Exception e) {
			System.out.println("Exc in getCalendar: " + e);
		}
		return null;
	}
	
	*/
	

	
	
	public ArrayList<serverHref> getParsedPROPFIND(){
		try {
			String serverURL = OptionsLoader.getInstance().getContactsServerUrl(mContext, mUsername);
			BufferedReader answer = mWebDAV.PROPFIND(serverURL, mWebDAV.getCredentials());
			ArrayList<serverHref> ret = readHrefsFromPROPFINDresponse(BufferReaderToString(answer,false));
			return ret;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}		
	}
	

	
	private GevilVCard getVCardByHref(serverHref href){
		////// Jeder Aufruf entspricht einem Schleifendurchlauf			
		//for(int i = 0; i<hrefs.length;i++){
			
			
			//System.out.println("+@getContacts: " + hrefs[i]);
			// Entsprechende hrefs mit get runterladen => vcalendar oder sowas...
				try{
					String serverURL = OptionsLoader.getInstance().getContactsServerUrl(mContext, mUsername);
					if(href.getHref().equals("The object you requested was not found.")){
						
						System.out.println("\n \n============= BEGIN EXCEPTION ==================");
						System.out.println("\n Objekt nicht gefunden.");
						System.out.println("\n@Adresse: " + href);
						System.out.println("\n ============== END EXCEPTION ===================\n \n");						
						
					}else if(!serverURL.equals(href.getHref())){
						GevilVCard gVCard = mWebDAV.httpGetVCard(href.getHref(), mWebDAV.getCredentials());		
						gVCard.setServerPath(href.getHref());
						if(gVCard.getUid() == null){
							
							System.out.println(gVCard.toString());
							throw new ArrayIndexOutOfBoundsException();
						}
						
						return gVCard;
						
					}
				}catch(Exception e){
					System.out.println("Exc(1) in getContacts verm. von getHREF href: " + href + " Exception: " + e);
					e.printStackTrace();
				}
		
			//} for Schleife
			
				return null;
	}
	
	
	private GevilVCalendar getVCalendarByHref(serverHref href){
		////// Jeder Aufruf entspricht einem Schleifendurchlauf			
		//for(int i = 0; i<hrefs.length;i++){
			
			
			//System.out.println("+@getContacts: " + hrefs[i]);
			// Entsprechende hrefs mit get runterladen => vcalendar oder sowas...
				try{	
					String serverURL = OptionsLoader.getInstance().getContactsServerUrl(mContext, mUsername);
					if(href.getHref().equals("The object you requested was not found.")){
						
						System.out.println("\n \n============= BEGIN EXCEPTION ==================");
						System.out.println("\n Objekt nicht gefunden.");
						System.out.println("\n@Adresse: " + href);
						System.out.println("\n ============== END EXCEPTION ===================\n \n");
						
					}else if(!serverURL.equals(href.getHref())){
						GevilVCalendar gVCalendar = mWebDAV.httpGetCalendar(href.getHref(), mWebDAV.getCredentials());		
						gVCalendar.setServerPath(href.getHref());
						
						if(gVCalendar.getUid() == null){							
							System.out.println("*** Exception Card: " + gVCalendar.toString());
							throw new ArrayIndexOutOfBoundsException();
						}
						
						return gVCalendar;						
					}
				}catch(Exception e){
					System.out.println("Exc(1) in getContacts verm. von getHREF href: " + href + " Exception: " + e);
					e.printStackTrace();
				}
		
			//} for Schleife
			
				return null;
	}
	
	
	
	
	
	public CalendarFromServerReader getCalendarIterator(){
		return new CalendarFromServerReader();
	}
	
	public class CalendarFromServerReader implements java.util.Iterator<GevilVCalendar>{
		
		private ArrayList<serverHref> hrefs;
		private int etag;
		int position = 0;
		boolean dbg = true;
		private String uidOfUndownloadedCalendar = null;
		GevilVCalendar nextCalendar;
		
		
		public GevilVCalendar nextWatchEtag(Hashtable<String, GevilVCalendar> serverPathHash, Hashtable<String, GevilVCalendar> uidHash){
			// auslesen der UID aus dem VCard Dateiname, welcher in hrefs vorliegt.
			String serverPath = hrefs.get(position).getHref();
			
			//cUID ist nicht die UID, sondern der ServerPath.
			
			// An Hand der UID kann die korrespondierende VCard im Zustand ausgelesen werden.
			GevilVCalendar zustandCard = serverPathHash.get(serverPath);
			
			if(zustandCard == null){
				serverPath = getUidFromHref();
				
				zustandCard = uidHash.get(serverPath);
				
				//System.out.println("Exception, gesuchte _UID: " + cUID);
				// 			eigentlich kommt das nicht vor, daher::
				//throw new ArrayIndexOutOfBoundsException();
			}
			
			if(zustandCard != null){
				//System.out.println("nextWatchEtag, korr.VCard gefunden, ihr etag: " + zustandCard.getEtag());
				
				
				
				int zustandEtag = zustandCard.getEtag();
				
				// den Iterator informieren, welchen Etag die korrespondierende VCard im Zustand hat.
				etag = zustandEtag;
				
				// Sollte der Etag sich nicht geändert haben, wird die VCard nicht heruntergeladen. Es wird lediglich eine 
				// AktuellGevilVCard erstellt, die signalisiert dass nichts aktualisiert werden muss.
				
				uidOfUndownloadedCalendar=zustandCard.getUid();
				GevilVCalendar ret = next();		
				uidOfUndownloadedCalendar = null;
				return ret;
				
			}else{
				
				System.out.println("nextWatchEtag, KEINE korr.VCard gefunden. VCard muss in Android eingefügt werden, _uid: " + serverPath);
				String keySet = "";
				//String valueSet = "";
				Iterator<String> keyIt = serverPathHash.keySet().iterator();
				while(keyIt.hasNext()){
					String key = keyIt.next();
					keySet += "_" + key;					
				}
				System.out.println("nextWatchEtag, keySet: " + keySet);
				
				
				return next();
			}
		}
		
		
		
		CalendarFromServerReader(){
			try {
				String URL  = OptionsLoader.getInstance().getCalendarServerUrl(mContext, mUsername);
				
				BufferedReader xmlBR = mWebDAV.PROPFIND(URL, mWebDAV.getCredentials());				
				
				if(xmlBR!=null){
				hrefs = readHrefsFromPROPFINDresponse(BufferReaderToString(xmlBR,false));
				}else{
					System.out.println("Unter dem Server konnte nichts gefunden werden.");
					throw new ArrayIndexOutOfBoundsException();
				}
				
			} catch (Exception e) {
				//
				// Hrefs konnten nicht heruntergeladen werden. Auslesevorgang abbrechen.
				//				
				System.out.println("Exc in GroupDAV.GetContacsFromServer.read: " + e);
				e.printStackTrace();				
			}
		}
		
		
		@Override
		public boolean hasNext() {
			
			etag = -1;
			if(hrefs.size() > position){			
				
				// Prüfen, ob es sich wirklich um eine VCard, oder nur um den Ersten Eintrag im XML, der ServerURL handelt.
				String thisHref = hrefs.get(position).getHref().toLowerCase();
				String calendarURL = OptionsLoader.getInstance().getCalendarServerUrl(mContext, mUsername);
				String thisServerURL = calendarURL.toLowerCase();
				if(!thisHref.endsWith("/"))thisHref += "/";				
				if(!thisServerURL.endsWith("/"))thisServerURL += "/";
				
				if( thisServerURL.contains( thisHref )){
					//System.out.println("============== folgnder HREF ist keine VCard: " + thisHref);			
					position++;
					// rekursiver Aufruf
					return hasNext();
				}else{
					//System.out.println("============== folgnder HREF IST eine VCard: " + thisHref);
					return true;
				}
				
				
			}else{
				return false;
			}			
		}
		
		
		
		private String getUidFromHref(){
			
			String href = hrefs.get(position).getHref();
			
			// Der Dateiname ist die UID.
			int ind=href.lastIndexOf("/");		
			String ret = href.substring(ind+1).replace("UID:", "");
			
			//System.out.println("**************************************************************************");
			//System.out.println("extrahiere UID aus: " + href + " Extraktionsergebnis: " + ret + "\n");
			return ret;
	}
		
		
		
		

		@Override
		public GevilVCalendar next() {			
			
			// Falls der Etag sich nicht verändert hat, wird die VCard nicht heruntergeladen.
			// Es wird lediglich ein Dummy erstellt, um darzustellen, dass die VCard nicht verändert wurde.
			
			if(etag != -1 && hrefs.get(position).getEtag() == etag){
				if(dbg)System.out.println(hrefs.get(position).getHref().substring(32) + " ********* wird nicht heruntergeladen, weil der ETAG unverändert ist. GevilS. etag: " + etag + " Citadel etag: " + hrefs.get(position).getEtag());
			
				AktuellServerGevilVCalendar aktuell = new AktuellServerGevilVCalendar(hrefs.get(position));
				position++;
				if(aktuell.getUid()==null){
					aktuell.setUid(uidOfUndownloadedCalendar);		
				}else if(aktuell.getUid()!=uidOfUndownloadedCalendar){
					System.out.println("VCard Dummy hat schon eine UID: " + aktuell.getUid() + " uidOfUndownloadedVCard: " + uidOfUndownloadedCalendar);
					throw new ArrayIndexOutOfBoundsException();
				}
								
					return aktuell;	

							
			}else{
				if(dbg)System.out.println(hrefs.get(position).getHref().substring(32) + " ******** wird SOFORT heruntergeladen, weil der ETAG anderst ist. GevilS. etag: " + etag + " Citadel etag: " + hrefs.get(position).getEtag());
				nextCalendar = getVCalendarByHref(hrefs.get(position));
				position++;
				return nextCalendar;
			}
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	
	
	
	public ContactsFromServerReader getContactsIterator(){
		return new ContactsFromServerReader();
		
	}
	
	public class ContactsFromServerReader implements java.util.Iterator<GevilVCard>{
		
		
		/*
		 * entspricht Constants.CONTACTS_SERVER_URL
		 
		private String URL;
		*/
		
		
		/*
		 * in hrefs wird für jede VCard auf Citadel ihr Zugrifslink gespeichert.
		 */
		private ArrayList<serverHref> hrefs;
		
		/*
		 * speichert ob die erste Ausführung shon erledigt ist.
		 */
		private boolean initialized = false;		
		private int position = 0;		
		int etag = -1;		
		private String uidOfUndownloadedVCard = null;
		
		public GevilVCard nextWatchEtag(Hashtable<String, GevilVCard> serverPathHash, Hashtable<String, GevilVCard> uidHash){
			// auslesen der UID aus dem VCard Dateiname, welcher in hrefs vorliegt.
			String cUID = hrefs.get(position).getHref();
			
			//cUID ist nicht die UID, sondern der ServerPath.
			
			// An Hand der UID kann die korrespondierende VCard im Zustand ausgelesen werden.
			GevilVCard zustandCard = serverPathHash.get(cUID);
			
			if(zustandCard == null){
				cUID = getUidFromHref();
				
				zustandCard = uidHash.get(cUID);
				
				//System.out.println("Exception, gesuchte _UID: " + cUID);
				// 			eigentlich kommt das nicht vor, daher::
				//throw new ArrayIndexOutOfBoundsException();
			}
			
			if(zustandCard != null){
				//System.out.println("nextWatchEtag, korr.VCard gefunden, ihr etag: " + zustandCard.getEtag());
				
				
				
				int zustandEtag = zustandCard.getEtag();
				
				// den Iterator informieren, welchen Etag die korrespondierende VCard im Zustand hat.
				setEtag(zustandEtag);
				
				// Sollte der Etag sich nicht geändert haben, wird die VCard nicht heruntergeladen. Es wird lediglich eine 
				// AktuellGevilVCard erstellt, die signalisiert dass nichts aktualisiert werden muss.
				
				uidOfUndownloadedVCard=zustandCard.getUid();
				GevilVCard ret = next();		
				uidOfUndownloadedVCard = null;
				return ret;
				
			}else{
				
				System.out.println("nextWatchEtag, KEINE korr.VCard gefunden. VCard muss in Android eingefügt werden, _uid: " + cUID);
				String keySet = "";
				//String valueSet = "";
				Iterator<String> keyIt = serverPathHash.keySet().iterator();
				while(keyIt.hasNext()){
					String key = keyIt.next();
					keySet += "_" + key;					
				}
				System.out.println("nextWatchEtag, keySet: " + keySet);
				
				
				return next();
			}
		}
		
		private void setEtag(int sEtag){
			etag = sEtag;
		}
		 
		private String getUidFromHref(){
				
				String href = hrefs.get(position).getHref();
				
				// Der Dateiname ist die UID.
				int ind=href.lastIndexOf("/");		
				String ret = href.substring(ind+1).replace("UID:", "");
				
				//System.out.println("**************************************************************************");
				//System.out.println("extrahiere UID aus: " + href + " Extraktionsergebnis: " + ret + "\n");
				return ret;
		}
				
		
		
		/**
		 * Zwischenspeicher für die ausgelesene GevilVCard.
		 */
		GevilVCard nextVCard;		

		/*
		public int nextUID(){
			throw new NotImplementedException();
		}
		*/
		
		
		ContactsFromServerReader(){			
	
				try {
					String serverURL = OptionsLoader.getInstance().getContactsServerUrl(mContext, mUsername);
					String URL = serverURL;
					BufferedReader xmlBR = mWebDAV.PROPFIND(URL, mWebDAV.getCredentials());						
					
					
					// Teste die Funktionalität der neuen Version.
					hrefs = readHrefsFromPROPFINDresponse(BufferReaderToString(xmlBR,false));
					//System.out.println(".\n");
					
					//hrefs = getHref(BufferReaderToString(xmlBR,false));					

					
				} catch (Exception e) {
					//
					// Hrefs konnten nicht heruntergeladen werden. Auslesevorgang abbrechen.
					//
					System.out.println("Exc in GroupDAV.GetContacsFromServer.read: " + e);
					e.printStackTrace();
					
				}
		}
		
		
		/**
		 * Prüft, ob noch HREFs vorhanden sind.
		 */
		@Override public boolean hasNext(){
			
			etag = -1;
			if(hrefs.size() > position){			
				
				// Prüfen, ob es sich wirklich um eine VCard, oder nur um den Ersten Eintrag im XML, der ServerURL handelt.
				String thisHref = hrefs.get(position).getHref().toLowerCase();
				String serverURL = OptionsLoader.getInstance().getContactsServerUrl(mContext, mUsername);
				String thisServerURL = serverURL.toLowerCase();
				if(!thisHref.endsWith("/"))thisHref += "/";				
				if(!thisServerURL.endsWith("/"))thisServerURL += "/";
				
				if( thisServerURL.contains( thisHref )){
					//System.out.println("============== folgnder HREF ist keine VCard: " + thisHref);			
					position++;
					// rekursiver Aufruf
					return hasNext();
				}else{
					//System.out.println("============== folgnder HREF IST eine VCard: " + thisHref);
					return true;
				}				
				
			}else{
				return false;
			}			
		}
		
		
		/**
		 * liest eine GevilVCard von Citadel in nextVCard ein und gibt true zurück,
		 * bzw. false wenn keine weitere VCard mehr ausgelesen werden kann.
		 */
		@Override public GevilVCard next(){
			boolean dbg = false;
			
			// Falls der Etag sich nicht verändert hat, wird die VCard nicht heruntergeladen.
			// Es wird lediglich ein Dummy erstellt, um darzustellen, dass die VCard nicht verändert wurde.
			if(etag != -1 && hrefs.get(position).getEtag() == etag){
				if(dbg)System.out.println(hrefs.get(position).getHref().substring(32) + " ********* wird nicht heruntergeladen, weil der ETAG unverändert ist. GevilS. etag: " + etag + " Citadel etag: " + hrefs.get(position).getEtag());
								
			
				AktuellServerGevilVCard aktuell = new AktuellServerGevilVCard(hrefs.get(position));
				position++;
				if(aktuell.getUid()==null){
					aktuell.setUid(uidOfUndownloadedVCard);		
				}else if(aktuell.getUid()!=uidOfUndownloadedVCard){
					System.out.println("VCard Dummy hat schon eine UID: " + aktuell.getUid() + " uidOfUndownloadedVCard: " + uidOfUndownloadedVCard);
					throw new ArrayIndexOutOfBoundsException();
				}
				
					
								
					return aktuell;	

							
			}else{
				if(dbg)System.out.println(hrefs.get(position).getHref().substring(32) + " ******** wird SOFORT heruntergeladen, weil der ETAG anderst ist. GevilS. etag: " + etag + " Citadel etag: " + hrefs.get(position).getEtag());
				nextVCard = getVCardByHref(hrefs.get(position));
				position++;
				return nextVCard;
			}
		}


		@Override
		public void remove() {			
			/*
			 *   TODO was macht remove??
			 */
			
		}		
	}
	
	/** 
	 * Gibt eine ArrayList mit allen Kontakten von Server zurück.
	 * @return alle Kontakte werden vom Server geladen.
	 */
	public ArrayList <GevilVCard> getContacts(){		
		ArrayList <GevilVCard> ret = new ArrayList <GevilVCard>(0);		
		Iterator<GevilVCard> it = this.getContactsIterator();		
		
		while(it.hasNext()){
			GevilVCard card = it.next();
			ret.add(card);
		}
		return ret;
		
	}
	
	
	/** 
	 * Gibt eine ArrayList mit allen Kalendereinträgen vom Server zurück.
	 * @return alle Kontakte werden vom Server geladen.
	 */
	public ArrayList <GevilVCalendar> getCalendar(){		
		ArrayList <GevilVCalendar> ret = new ArrayList <GevilVCalendar>(0);		
		Iterator<GevilVCalendar> it = this.getCalendarIterator();
		
		while(it.hasNext()){
			GevilVCalendar card = it.next();
			ret.add(card);
		}
		return ret;
		
	}
	
	/*
	public ArrayList <GevilVCard> getContacts(){		
		WebDAV http = mWebDAV;
		
		try {			
			String URL = Constants.CONTACTS_SERVER_URL;
			BufferedReader xmlBR = http.PROPFIND(URL, http.getCredentials());
			
			String[] hrefs = this.getHref(BufferReaderToString(xmlBR,false));
			ArrayList <GevilVCard> ret = new ArrayList<GevilVCard>(hrefs.length-1);
			int j=0; /* 
					  * Zähler für Speicherposition im Array ret.
					  * Falls unter einem Href keine Parsbare VCard vorhanden ist, 
					  * wird i hochgezählt, da in ret nix gespeichert wird, wird j 
					  * nicht erhöt.
					  * 
					  *//*					
			
			for(int i = 0; i<hrefs.length;i++){
				//System.out.println("+@getContacts: " + hrefs[i]);
				// Entsprechende hrefs mit get runterladen => vcalendar oder sowas...
					try{				
						if(hrefs[i].equals("The object you requested was not found.")){
							System.out.println("\n \n============= BEGIN EXCEPTION ==================");
							System.out.println("\n Objekt nicht gefunden.");
							System.out.println("\n@Adresse: " + hrefs[i]);
							System.out.println("\n ============== END EXCEPTION ===================\n \n");
						}else if(!URL.equals(hrefs[i])){
							String c = http.Get(hrefs[i], http.getCredentials());

							// TODO Bug sollte von Hersteller behoben werden.
							c = Workarounds.fixVCardForical4j_vcard(c);
							
							VCardBuilder builder;
					        StringReader sr = new StringReader(c);       
					        builder = new VCardBuilder(sr);					        
								try{
							        VCard card = builder.build();
									//System.out.println(card.toString() + "\n \n");
									
									// VCard in Return Variable speichern.
									//ret[j] = card;
									ret.add(j, new GevilVCard(card));
									j++;
									
								}catch(net.fortuna.ical4j.data.ParserException pe){
									System.out.println("\n \n============= BEGIN EXCEPTION ==================");
									System.out.println("\n Fehler bei: \n" + c);
									System.out.println("\nParserExc in getContacts: " + pe);
									System.out.println("\n ============== END EXCEPTION ===================\n \n");
								}catch(Exception e){
									System.out.println("Exc(2)[oben] in getContacts verm. von builder.build: " + hrefs[i] + " Exception: " + e);
								}	
						}
					}catch(Exception e){
						System.out.println("Exc(2) in getContacts verm. von getHREF href: " + hrefs[i] + " Exception: " + e);
					}
			
				}
				return ret;
				
			} catch (Exception e) {
				System.out.println("Exc in getContacts, vermutl. von PROPFIND: " + e);
			}
			return null;
		}
	*/
	
	
	String BSP = "<?xml version=\"1.0\"?>\n" + "<company>\n" + "<staff>\n"
			+ "<firstname>yong</firstname>\n" + "<lastname>mook kim"
			+ "</lastname>\n" + "<nickname>mkyong</nickname>\n"
			+ "<salary>100000</salary>\n" + "</staff>\n" + "<staff>\n"
			+ "<firstname>low</firstname>\n"
			+ "<lastname>yin fong</lastname>\n"
			+ "<nickname>fong fong</nickname>" + "<salary>200000</salary>\n"
			+ "</staff>\n" + "</company>";

	// TUTORIAL @
	// http://openbook.galileodesign.de/javainsel5/javainsel13_005.htm
	/*
	public void xmlExample(BufferedReader BR) {
		SAXBuilder builder = new SAXBuilder();
		// File xmlFile = new File("xml.xml");
		// StringReader xmlFile = new StringReader(BSP);

		try {
			Document document = (Document) builder.build(BR);

			Element rootNode = document.getRootElement();

			/*
			 * System.out.println("++====================================");
			 * XMLOutputter out = new XMLOutputter( Format.getPrettyFormat() );
			 * out.output( document, System.out );
			 * System.out.println("++====================================");
			 *//*

			// Gibt Liste mit <response>...</response> zurück
			List list = rootNode.getChildren();
			
			
			for (int j = 0; j < list.size(); j++) {
				Element node2 = (Element) list.get(j);

				// überspringe <response> um an das was darin ist zu gelangen
				List items = node2.getChildren();

				// zuerst kommt die href
				Element href = (Element) items.get(0);

				// TODO href weiterverarbeiten
				System.out.println("++++href:: " + href.getText());

				// TODO getlastmodified auslesen und weiter verarbeiten...
				Element prop = (Element) items.get(1);
				/*
				 * getlastmodified sollte auch wichtig sein, für die
				 * Synchronisation... <propstat> <status>HTTP/1.1 200
				 * OK</status> <prop> <displayname>GroupDAV</displayname>
				 * <resourcetype><collection/></resourcetype>
				 * <getlastmodified>Mon, 29 Aug 2011 17:49:15
				 * +0500</getlastmodified> </prop> </propstat>
				 *//*
			

			}
			

		} catch (Exception e) {
			System.out.println("+Exc: " + e);
			
		}

		// xml direkt auf Konsole ausgeben
		// XMLOutputter out = new XMLOutputter( Format.getPrettyFormat() );
		// System.out.print("\n\n " + j + "-\n");
		// out.output( href, System.out );
	}
	*/
	
	/*	
	 * Hilfsfunktion,
	 * Bricht einen String in einzelne Zeilen um, wobei jede Zeile
	 * maxZeilenlaenge lang ist.
	 * 
	 * @param String Text: dieser Text soll umgebrochen werden
	 * @param int maxZeilenlaenge: gibt an wie lange jede Zeile sein soll.
	 */
	public String inZeilenUmbrechen(String Text, int maxZeilenlaenge) {
		int l = Text.length();

		if (l < maxZeilenlaenge) {
			return Text;
		} else {
			// int maxZeilenlaenge = 150;
			int t = l / maxZeilenlaenge; // t: Anzahl Zeilen. Nach Umstellen
											// ergibt sich: Nenner = Anzahl
											// Zeichen pro Zeile

			String Ausgabe = "\n";
			for (int i = 1; i <= t; i++) {
				Ausgabe += Text.substring((i - 1) * l / t, i * l / t)
						+ "\n";
			}
			return Ausgabe;
		}
	}
	
		private void printBufferedReader(BufferedReader in, TextView tf1){		
	        try {
				String c;
				while ((c = in.readLine()) != null) {	
					
					tf1.setText(tf1.getText() + "\n" + inZeilenUmbrechen(c, 30 ));
					//System.out.println( );
				}
	        } catch (IOException e) {
	        	tf1.setText(tf1.getText() + "\n" + "+Exec in printBufferedReader " + e);
				System.out.println("+Exec in printBufferedReader " + e);
			}	     
	}
		
		private String BufferReaderToString(BufferedReader in, Boolean Zeilenumbruch){		
	        String ret = "";
			try {
				String c;
				while ((c = in.readLine()) != null) {	
					ret += c;
					if(Zeilenumbruch) ret += "\n";					
				}				
	        } catch (IOException e) {
				System.out.println("+Exec in BufferReaderToString " + e);
			}
	        return ret;
	}	

	
	/**
	 * neue Version von getHref, welche zusätzlich den lastModified Value auslesen soll.
	 * @param BR
	 * @return
	 */
	private ArrayList<serverHref> readHrefsFromPROPFINDresponse(String BR) {		
		
		SAXBuilder builder = new SAXBuilder();		
		
		try {
			Reader r = new StringReader(BR);
			Document document = builder.build(r);
			Element rootNode = document.getRootElement();
			
			// 			
			// auslesen des Datums der letzten Änderung
			//
			
			List childrenList = document.getRootElement().getChildren();
			Iterator<Element> it = childrenList.iterator();
			ArrayList<serverHref> ret = new ArrayList<serverHref>(childrenList.size()-1);			
			
			
			while(it.hasNext()){
				Element response = it.next();				
				
				if(response.getName().equals("response")){
					//System.out.println("gelesenes response Element: <" + response.getName() + ">  " + response.getText());
					
					// wichtig: responseHref und responseEtag dürfen nicht auserhalb der Schleife über <response> Elemente
					// definiert werden, weil der Href nur zurück gegeben wird, wenn ihr etag sich geändert hat.
					String responseHref = null;
					int responseEtag = -1;

					
					
					//
					// <href> und <getetag> jeder Response auslesen
					//
					Iterator<Element> responseContents = response.getChildren().iterator();
					while(responseContents.hasNext()){
						Element responseContent = responseContents.next();
						if(responseContent.getName().equals("href")){
						
							//System.out.println("gelesener href: " + responseContent.getName() + "  .getText(): " + responseContent.getText());
							responseHref = responseContent.getText();

						}else if(responseContent.getName().equals("propstat")){
							
							
							//
							// Etag auslesen.
							//
							Iterator<Element> propstatContents = responseContent.getChildren().iterator();							
							while(propstatContents.hasNext()){
								Element propstatContent = propstatContents.next();
								if(propstatContent.getName().equals("prop")){
									Iterator<Element> props = propstatContent.getChildren().iterator();
									while(props.hasNext()){
										Element prop = props.next();
										if(prop.getName().equals("getetag")){											 
											
											String etg = prop.getText();
											
											// Der Etag Wert auf dem Server ist in Anführungszeichen (") eingeschlossen.
											// Diese müssen entfernt werden, vor der String in einen Integer konvertiert wird
											etg = etg.replace("\"", "");											
											responseEtag = Integer.valueOf(etg);
											 
											
											//System.out.println("etag: " + etag);
										}
									}									
								}
							}
						}
					}  // auslesen von <href> und <getetag>
					
					GevilVCard geht_das_nicht_besser = new GevilVCard(mMap);
					serverHref einLinkVomServer = geht_das_nicht_besser.new serverHref(responseHref, responseEtag);
					ret.add(einLinkVomServer);
				}
				
			}		
			
		return ret;
		
		}	catch (StringIndexOutOfBoundsException ie){
			System.out.println("+StringIndexOutOfBoundsException in getHref: " + ie + "\n");
			ie.printStackTrace();
			//return new String[]{"+Exc in getHREF: " + ie};					
		} catch (Exception e) {
			System.out.println("+Exc in getHref: " + BR);
			e.printStackTrace();
			//return new String[]{"+Exc in getHREF: " + e};
		}
		
		return null;
	}

	/*  UNGENUTZT°!
	public void r2eadxml2() {

		SAXBuilder builder = new SAXBuilder();
		// File xmlFile = new File("xml.xml");
		StringReader xmlFile = new StringReader(BSP);

		try {

			Document document = (Document) builder.build(xmlFile);
			Element rootNode = document.getRootElement();
			List list = rootNode.getChildren("staff");

			
			for (int i = 0; i < list.size(); i++) {

				Element node = (Element) list.get(i);

				System.out.println("+First Name : "
						+ node.getChildText("firstname"));
				System.out.println("+Last Name : "
						+ node.getChildText("lastname"));
				System.out.println("+Nick Name : "
						+ node.getChildText("nickname"));
				System.out.println("+Salary : " + node.getChildText("salary"));

			}

		} catch (Exception e) {
			System.out.println("+Exc: " + e);
		}

	}*/

}
