package com.gevil.calSyncZustand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.lang.NotImplementedException;

import myHttp.GroupDAV;
import myHttp.GroupDAV.CalendarFromServerReader;
import myHttp.GroupDAV.ContactsFromServerReader;
import syncZustand.ContactOperations;
import syncZustand.GevilVCard;
import syncZustand.TypeBiMap;
import syncZustand.storeLoadSyncZustand;
import android.content.Context;
import android.database.sqlite.SQLiteException;

import com.gevil.notifications;
import com.gevil.AndroidCalendar.Konsolenausgabe;
import com.gevil.AndroidCalendar.ReadAndroidCalendar;
import com.gevil.AndroidCalendar.ReadAndroidCalendar.CalendarFromPhoneReader;
import com.gevil.AndroidContacts.readAndroidContacts;
import com.gevil.AndroidContacts.readAndroidContacts.ContactsFromPhoneReader;

public class calSyncZustand {

	ArrayList<GevilVCalendar> zustandGVCards = new ArrayList<GevilVCalendar>(0);
	
	/*
	 * gibt für eine UID die dazugehörige GevilVCard im zustand zurück
	 * 
	 */	
	Hashtable<String, GevilVCalendar> uidHash = new Hashtable<String, GevilVCalendar>();
	
	/*
	 * gibt zum Dateiname auf dem Server die GevilVCard im Sync-Zustand zurück.
	 * 
	 */
	Hashtable<String, GevilVCalendar> serverFileNameHash = new Hashtable<String, GevilVCalendar>();
	
	private Context mContext;
	
	private String username;
	private String password;	
	TypeBiMap mMap;
	Konsolenausgabe kDebug = new Konsolenausgabe();
	private notifications mNotification;
	
	/** 
	 * Datenbankobjekt das im Speicher gehalten oder ordentlich geschlossen
	 * werden muss.
	 */
	storeLoadCalSyncZustand mStoreLoadSyncZustand = null;
	String mSQLtableName;

	
	
	
	
	public ArrayList<GevilVCalendar> getZustand(){
		return zustandGVCards;
	}
	public void setZustand(ArrayList<GevilVCalendar> cards){
		zustandGVCards = cards;
		
	}

	
	public calSyncZustand(String user, String pass, Context context, TypeBiMap map, String Tabellenname, notifications inNotification){
		if(map==null){
			throw new NullPointerException();
		}
		
		this.mNotification = inNotification;
		username = user;
		password = pass;
		mContext = context;
		mMap = map;
		mSQLtableName = Tabellenname;
	}
	
	
	public void setKonsolenausgabe(Konsolenausgabe ausgabe2) {
		kDebug = ausgabe2;
		
	}

	
	
	/**
	 * Schließen der Datenbank um Speicher zu sparen.
	 */
	public void closeDatabases(){
		if(mStoreLoadSyncZustand != null){
			mStoreLoadSyncZustand.closeAll();
			mStoreLoadSyncZustand = null;
		}
	}
	
	
	private void maybeCreateDatabase(String Tabellenname){
		if(mStoreLoadSyncZustand==null){
			mStoreLoadSyncZustand = new storeLoadCalSyncZustand(mContext, Tabellenname, mMap);
		}
	}
	
	/**
	 * Speichert Synczustand in einer SQL lite Datenbank.
	 * @param Tabelenname
	 
	public void saveAsSQL(String Tabelenname){ 		
		maybeCreateDatabase(Tabelenname);
		mSlz.zustandKomplettNeuSpeichern(this, mContext, Tabelenname);		
	}	
	*/
	/**
	 * Liest einen Synczustand aus einer SQL lite Datenbank.
	 * @param Tabelenname
	 */	
	public void readFromSQL()  throws SQLiteException {
		maybeCreateDatabase(mSQLtableName);
		mStoreLoadSyncZustand.zustandLaden(this, mContext, mSQLtableName);
	}
	
	
	public void SQLinsert(GevilVCalendar iCard){ 		
		maybeCreateDatabase(mSQLtableName);
		mStoreLoadSyncZustand.insert(iCard, mContext, mSQLtableName);
	}	
	public void SQLupdate(GevilVCalendar iCard){ 		
		maybeCreateDatabase(mSQLtableName);
		mStoreLoadSyncZustand.update(iCard, mContext, mSQLtableName);
	}	
	public void SQLdelete(GevilVCalendar iCard){ 		
		maybeCreateDatabase(mSQLtableName);
		mStoreLoadSyncZustand.delete(iCard, mContext, mSQLtableName);
	}
	public boolean SQLexists(GevilVCalendar iCard){ 		
		maybeCreateDatabase(mSQLtableName);
		return mStoreLoadSyncZustand.exists(iCard, mContext, mSQLtableName);
	}
	 
	
	
	/**  
	 * Aufbauen der Hashtable uidHash
	 *  
	 */
	public void rehash(){
		uidHash = new Hashtable<String, GevilVCalendar>();
		
		if(zustandGVCards!=null){	
			Iterator<GevilVCalendar> i = zustandGVCards.iterator();
			
			while(i.hasNext()){
				GevilVCalendar card = i.next();
				
				String uniqueId = card.getUid();
				String serverPath = card.getServerPath();
				
				if(uniqueId != null){
					if(uidHash.get(uniqueId) != null){
						System.out.println("=========================================");
						System.out.println("Rehash: versuch doppelten Hashwert einzutragen: ");
						System.out.println(card.toString());
						System.out.println(".");
						System.out.println(uidHash.get(uniqueId).toString());
						System.out.println(".");
						System.out.println(".");						
						throw new IndexOutOfBoundsException();
					}
					uidHash.put(uniqueId, card);
					if(serverPath != null){
						serverFileNameHash.put(serverPath, card);
					}
				}
			}
		}
	}
	

	/**
	 * Läd den Zustand im Server in syncZustand. Anderst ausgedrückt: alle VCards.
	 * @param username
	 * @param password
	 */
	public void getVCardsFromServer(){
		myHttp.GroupDAV gd = new myHttp.GroupDAV(username, password, mMap, mContext);
		zustandGVCards = null;
		zustandGVCards = gd.getCalendar();	
		this.rehash();
	}
	
	
	/**
	 * Läd den Zustand von Handy in syncZustand. Anderst ausgedrückt: alle VCards.
	 * @param username
	 * @param password
	 */
	public void getVCardsFromPhone(){
		throw new NotImplementedException();
		/*
		readAndroidContacts rac = null;
		try{
			rac = new readAndroidContacts(mMap, username);
		}catch(Exception e){
			kDebug.println("." + e + " in  getVCardsFromPhone(1).");
		}
		try{
			zustandGVCards = rac.readContactsFromPhone(mContext, username);
		}catch(Exception e){
			kDebug.println("." + e + " in  getVCardsFromPhone(2).");
		}
		try{
			rehash();
		}catch(Exception e){
			kDebug.println("." + e + " in  getVCardsFromPhone(3).");
			e.printStackTrace();
		}
		*/
		
	}

	
	
	/**
	 * Vergleicht jede VCard des Servers mit syncZustand 
	 * @param username
	 * @param password
	 */
	public CalendarOperations syncVglServer(){

		GroupDAV gd = new GroupDAV(username, password, mMap, mContext);   
		CalendarFromServerReader calendarReader = gd.getCalendarIterator();
		
		try{
			CalendarOperations ret = syncVgl(calendarReader);
			return ret;
			
		}catch(Exception e){
			kDebug.printStackTrace(".vogel " + e + " in syncVglServer(0)", e);
		}			

		return null;
	}
	

	
	
	/**
	 * Vergleicht jede VCard von Android mit syncZustand 
	 * 
	 */
	public CalendarOperations syncVglAndroid(){			
		ReadAndroidCalendar rcal  = new  ReadAndroidCalendar(mMap, mContext, username, this);		
		//ContactsFromPhoneReader ContactReader = rac.getContactsPhoneReader(mContext, mMap, username);
		CalendarFromPhoneReader calendarReader = rcal.getCalendarFromPhoneReader();
		CalendarOperations ret = syncVgl(calendarReader);
		return ret;		
	}
	
	/**
	 * Vergleicht den syncZustand mit einer Menge von GevilVCards, die vom Server oder
	 * vom Client stammen können.
	 * 
	 * TODO return Liste von Operationen o.ä.
	 * @param reader
	 */
	private CalendarOperations syncVgl(Iterator<GevilVCalendar> reader){
				
		
		CalendarOperations ret = new CalendarOperations(username, password, mContext, mMap, this, mNotification);
		ret.setKonsolenausgabe(kDebug);
		
		// Konsolenausgaben an/aus schalten
		boolean dbg = true;
		
		if(dbg)kDebug.println(".");
		
		while(reader.hasNext()){
    			try{
    				GevilVCalendar card;
    				
    				if(reader instanceof CalendarFromServerReader){    	
    					// Falls der Etag sich nicht geändert hat, wird die VCard nicht vom Server geladen.
    					// statdessen wird ein Dummy geladen, der beim Vergleich immer als gleich betrachtet wird.
    					card = ((CalendarFromServerReader)reader).nextWatchEtag(this.serverFileNameHash, this.uidHash);    					
    				}else{	
    					// reader iteriert über VCards aus dem Android Adressbuch.
    					card = reader.next();
    				}
    				
    				if(kDebug!=null)card.setKonsolenersatz(kDebug);
    				
    				String cUID = card.getUid(); 
        			//if(dbg)kDebug.println(".\n Iterator gibt card mi uid: " + cUID + " versuche match.");
        			
        			if(cUID != null){
        				GevilVCalendar cardFromHash = this.uidHash.get(cUID);
        				
        				
        				if(cardFromHash!=null){
        					//
        					// Die VCard existiert auf beiden Seiten. Testen, ob Einträge sich geändert haben und ggf.
        					// entscheiden welche Änderung beibehalten wird.
        					//
        					
        					// ggf. Android Id aktualisieren
        					cardFromHash.setKonsolenersatz(kDebug);
        					if(cardFromHash.getAndroidId() == -1){
	        					if(card.getAndroidId()!=-1){
	        						cardFromHash.setAndroiId((card.getAndroidId()));
	        					}
        					}
        					
        					
        					//
        					// ZstVerglCard wurde gefunden. Sie muss also nichtmehr synchronisiert werden.
        					//
        					cardFromHash.FlagSyncTested = true;       
        					
        					
        					//
        					// Wenn der Iterator über Citadel Kontakte iteriert, sind in Card keine 
        					// Android _ids vorhanden. Die Android _id ist aber klar, weil sie im Zustand 
        					// per UID eindeutig identifiziert wurde.
        					// !! nicht bei Calndar
        					/*
        					if(card.getAndroidId()==-1){
        						card.setAndroidId(cardFromHash.getAndroidId());
        					}
        					*/
        					
        					if(cardFromHash.compareTo(card) != 0){
        						// ein Detail hat sich auf einer Seite geändert. Ein Update ist notwendig.
        						if(dbg)kDebug.println("********* MATCH, update notwendig _UID: " + cUID  );
        						
        						ret.addUpdate(card);
        						
        				
        					}else{
        						//if(dbg)kDebug.println("********* MATCH, Person unverändert, _UID: " + card.getN()[0] + card.getN()[1] + " *****************\n.");
        						//kDebug.print("   ===> Kontakt auf beiden Seiten unverändert.\n");
        					}
        					
        				}else{
        					//
        					// card hat eine UID, jedoch keine VCard unter SyncZustand hat die gleiche UID => 
        					// Die VCard muss kopiert werden.
        					//
        					if(dbg)kDebug.println("********* _UID: " + cUID + " VCard muss nach Android (Citadel) kopiert << werden:");
        					//if(dbg)kDebug.println(card.getN()[0] + " ");
        					if(dbg)kDebug.println("cUID: " + cUID + " Vergleichskarte wurde nicht gefunden.");
        					if(dbg)kDebug.println(".");        					
        					
        					ret.addInsert(card);
        					
        					///ContactOperation insert = new ContactOperation(card, 1, mContext, username);
        					///ret.add(insert);
        				}
        			}else{
        				//
        				// card hat keine UID. Sie muss also von Android kommen, weil alle Citadel VCards eine UID haben
        				// UND sie wurde noch nicht synchronisiert. => Diese VCard wurde unter Android neu erstellt, und muss jetzt
        				// auf Citadel geladen werden.
        				//
        				if(dbg)kDebug.println("********* _UID: " + cUID + " VCARD hat kein UID!! Fehler!! VCard muss nach Android (Citadel) kopiert werden:");
        				if(dbg)kDebug.println("Im Hinblick darauf, dass jede VCard eine UID hat darf das nicht vorkommen...************************************");
        				//if(dbg)kDebug.println(card.getN()[0] + card.getN()[1]);
        				if(dbg)kDebug.println(".");
        				if(dbg)kDebug.println(".");
        				if(dbg)kDebug.println(".");
    					
        				ret.addInsert(card);
        			}
        			
    			}catch(Exception e){
        			kDebug.printStackTrace("syncVgl() Exc. beim VCard Vergleich: ", e);
    			
    			}
		}
		//
		// Wir haben jetzt jeden Kontakt des Readers abgearbeitet. Anname: der Reader liest Kontakte vom Handy.
		// Falls auf dem handy jedoch ein Kontakt gelöscht wurde, ist dieser immernoch im Sync Zustand vorhanden und		
		// wurde nicht betrachtet. Dieser Kontakt muss dann gelöscht werden, ausgenommen, es gab einen Fehler beim matching
		// Deshalb: - Kontakte die schon abgearbeitet wurden markieren
		//			- in den folgenden zeilen: Nicht markierte Kontakte der Gegenseite, d.H. vom Zustand, anschauen.
		//
		
		Iterator<GevilVCalendar> altZustand= zustandGVCards.iterator();
		while(altZustand.hasNext()){		
			GevilVCalendar card = altZustand.next();
			if(!card.FlagSyncTested){				
				if(card.getUid()!=null){					
					// card ist im SyncZustand vorhanden. Nicht jedoch in Android (Citadel)
					// entweder sie wurde in Android (Citadel) gelöscht.
					if(dbg)kDebug.println("_UID: " + card.getUid() + "********* VCard muss gelöscht werden:");
					//if(dbg)kDebug.println(card.getN()[0] + card.getN()[1]+"\n.");
					if(dbg)kDebug.println(".");
					
					ret.addDelete(card);
				}else{
					// card ist im SyncZustand vorhanden. möglicherweise ist sie auch in Android (Citadel)
					// vorhanden. Dies kann nicht durch die Hastabelle herausgefunden werden, weil es keine UID gibt.
					// VORSCHLAG: Karten ohne UID generell nach Android (Citadel) kopieren.
					
					// Fall a) gegenkarte unter Android (Citadel) gefunden:
						// Karten auf Änderungen prüfen, und ggf. durchführen
					
					// Fall b) gegenkarte unter Android (Citadel) nicht gefunden:
						// Karte muss gelöscht werden.  
						//
						//		=> Sollte ausgeschlossen werden können, weil
						// nach der Synchronisation jedem Kontakt seine UID zugewiesen gewesen sein muss.
						// d.h. jeder kontakt im Zustand sollte eine UID haben.
						//
						// Löschen unmöglich, weil die Karte unter Andriod (Citadel) nicht existiert.
					
					if(dbg)kDebug.println("Zustand enthält eine VCard ohne Uid. Dies darf nicht vorkommen, weil nach abgeschlossener\n" +
							"Synchronisation jede VCard eine Uid zugewiesen haben muss.*****************************************************");
					//if(dbg)kDebug.println(card.getN()[0] + card.getN()[1]+"\n.");
					if(dbg)kDebug.println(".");
					
				}
			}
			
			// die nächste Synchronisation kann beginnen.
			card.FlagSyncTested = false;
		}
		
		return ret;
		
	}
	
	
	HashMap <Integer, GevilVCalendar> findByAndroidId;// = new HashMap <Integer, GevilVCalendar>();
	/**
	 * Suche ob im Sync-Zustand ein Kalenderevent mit der androidId enthalten ist.
	 * @param androidId
	 * @return
	 */
	
	//TODO löschen
	public GevilVCalendar getVCalendarByAndroidId(Integer androidId) {
		//
		// Rehash
		//
		if(findByAndroidId == null){

			// Der SyncZustand muss zuerst geladen werden.
			if(uidHash == null) throw new ArrayIndexOutOfBoundsException();
			findByAndroidId = new HashMap <Integer, GevilVCalendar>();
			
			Iterator<GevilVCalendar> it = uidHash.values().iterator();
			while(it.hasNext()){
				GevilVCalendar c = it.next();
				if(c.getAndroidId()!=-1){
					findByAndroidId.put(c.getAndroidId(), c);
					System.out.println("heur addToFindByA_id: " + c.getAndroidId() +  " =>" + c.getSummary());
				}else{
					System.out.println("heur addToFindByA_id: " + c.getSummary() + " hat keine AndroidID und wird nicht eingefügt.");
					// Wir suchen hier nur nach einer Android_id im Sync Zustand. wenn sie hier nicht 
					// vorhanden ist, macht es keinen Sinn sie im Adressbuch zu suchen,
					// weil wir die AndroidId selbst ja aus dem Adressbuch haben.
				}
			}
			
		}else{			
			System.out.println("heur finByAndroidId!=null #:" + findByAndroidId.size());
		}
		
		
		//
		//
		//
		return findByAndroidId.get(androidId);
	}

}
