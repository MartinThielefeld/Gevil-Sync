package syncZustand;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import myHttp.GroupDAV;
import myHttp.GroupDAV.ContactsFromServerReader;
import android.content.Context;
import android.database.sqlite.SQLiteException;

import com.gevil.notifications;
import com.gevil.AndroidCalendar.Konsolenausgabe;
import com.gevil.AndroidContacts.readAndroidContacts;
import com.gevil.AndroidContacts.readAndroidContacts.ContactsFromPhoneReader;

public class syncZustand {
	ArrayList<GevilVCard> zustandGVCards = new ArrayList<GevilVCard>(0);
	
	/*
	 * gibt für eine UID die dazugehörige GevilVCard im zustand zurück
	 * 
	 */	
	Hashtable<String, GevilVCard> uidHash = new Hashtable<String, GevilVCard>();
	
	/*
	 * gibt zum Dateiname auf dem Server die GevilVCard im Sync-Zustand zurück.
	 * 
	 */
	Hashtable<String, GevilVCard> serverFileNameHash = new Hashtable<String, GevilVCard>();
	
	private Context mContext;
	
	private String username;
	private String password;	
	TypeBiMap mMap;
	
	
	/** 
	 * Datenbankobjekt das im Speicher gehalten oder ordentlich geschlossen
	 * werden muss.
	 */
	storeLoadSyncZustand mStoreLoadSyncZustand = null;
	String mSQLtableName;

	private Konsolenausgabe debug;

	private notifications mNotification;
	
	
	
	public ArrayList<GevilVCard> getZustand(){
		return zustandGVCards;
	}
	public void setZustand(ArrayList<GevilVCard> cards){
		zustandGVCards = cards;
		
	}

	
	public syncZustand(String user, String pass, Context context, TypeBiMap map, String Tabellenname, notifications inNotification){
		if(map==null){
			throw new NullPointerException();
		}		
		
		mNotification = inNotification;
		username = user;
		password = pass;
		mContext = context;
		mMap = map;
		mSQLtableName = Tabellenname;
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
			mStoreLoadSyncZustand = new storeLoadSyncZustand(mContext, Tabellenname, mMap);
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
	
	public void SQLinsert(GevilVCard iCard){ 		
		maybeCreateDatabase(mSQLtableName);
		mStoreLoadSyncZustand.insert(iCard, mContext, mSQLtableName);
	}	
	public void SQLupdate(GevilVCard iCard){ 		
		maybeCreateDatabase(mSQLtableName);
		mStoreLoadSyncZustand.update(iCard, mContext, mSQLtableName);
	}	
	public void SQLdelete(GevilVCard iCard){ 		
		maybeCreateDatabase(mSQLtableName);
		mStoreLoadSyncZustand.delete(iCard, mContext, mSQLtableName);
	}	

	
	
	/**  
	 * Aufbauen der Hashtable uidHash
	 *  
	 */
	public void rehash(){
		uidHash = new Hashtable<String, GevilVCard>();
		
		if(zustandGVCards!=null){	
			Iterator<GevilVCard> i = zustandGVCards.iterator();
			
			while(i.hasNext()){
				GevilVCard card = i.next();
				
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
		zustandGVCards = gd.getContacts();		
		this.rehash();
	}
	
	
	/**
	 * Läd den Zustand von Handy in syncZustand. Anderst ausgedrückt: alle VCards.
	 * @param username
	 * @param password
	 */
	public void getVCardsFromPhone(){
		readAndroidContacts rac = null;
		try{
			rac = new readAndroidContacts(mMap, username);
		}catch(Exception e){
			System.out.println("." + e + " in  getVCardsFromPhone(1).");
		}
		try{
			zustandGVCards = rac.readContactsToArrayList(mContext, username);
		}catch(Exception e){
			System.out.println("." + e + " in  getVCardsFromPhone(2).");
		}
		try{
			rehash();
		}catch(Exception e){
			System.out.println("." + e + " in  getVCardsFromPhone(3).");
			e.printStackTrace();
		}
		
	}

	
	
	/**
	 * Vergleicht jede VCard des Servers mit syncZustand 
	 * @param username
	 * @param password
	 */
	public ContactOperations syncVglServer(){
		
		myHttp.GroupDAV.ContactsFromServerReader ContactReader=null;
		GroupDAV gd = new GroupDAV(username, password, mMap, mContext);   
		ContactReader = gd.getContactsIterator();
		
		try{
			ContactOperations ret =  syncVgl(ContactReader);
			return ret;
		}catch(Exception e){			
			System.out.println(".vogel " + e + " in syncVglServer(0)");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Nach dem Put auf einen VCard-Server ist es möglich, dass dieser den Dateinamen
	 * so ändert, dass er nichtmehr der uid entspricht. In diesem Fall wird die Uid der
	 * betreffenden VCard aktualisiert und diese in Android und in Citadel upgedatet.
	 *//*
	public void funktioniertNICHTactualizeUids(){
		GroupDAV gd = new GroupDAV(username, password, mMap);
		ArrayList<serverHref> pfresponse = gd.getParsedPROPFIND();
		Iterator<serverHref> pIt = pfresponse.iterator();
		while(pIt.hasNext()){
			serverHref sHref = pIt.next();
			
			
			if(uidHash.get(sHref.getUid()) == null){
				// In der Hasmap ist die VCard noch unter der alten uid bekannt.
				// ODER: sie wurde gerade erst hochgeladen...
				
				//
				// etag Vergleich kann nicht durchgeführt werden wenn die VCard im Zustand nicht gefunden wird.
				// sollte unter sHref.getUid() im Zustand etwas vorhanden sein, wurde dis nicht gefunden, weil
				// die VCard gerade erst hochgeladen wurde (die Uid im Zustand veraltet ist)
				// Genau in diesem Fall ist ein Uid Update notwendig.
				//				
				
				GevilVCard serverCard = gd.getVCardByHref(sHref);
				
				// Falls eine VCard gerade erst hochgeladen wurde, kann dies der Grund sein, weshalb sie im Zustand noch nicht
				// vorhanden ist.
				//System.out.println("DownloadURL: " + sHref.getHref());
				if(serverCard!=null && serverCard.getOldUid()!=null)
				if(!sHref.getUid().equals(serverCard.getOldUid())){
					
					GevilVCard staleZustandCard = uidHash.get(serverCard.getOldUid());
					int androidId = staleZustandCard.getAndroidId();
					
					// Wir gehen an dieser Stelle davon aus, dass ein eventuelles Update unter 
					// Citadel bereits ausgeführt wurde und card deshalb up to date ist.
					String mcString = serverCard.toString();
					// aktualisierte UID eintragen
					mcString = mcString.replace(serverCard.getOldUid(), sHref.getUid());
					
					
					
					try {					
						// Jetzt: aktualisierte VCard in Citadel und in Android aktualisieren.
						//
						GevilVCard newCard = new GevilVCard(mcString, mMap);
						newCard.setAndroidId(androidId);
						
						System.out.println("$$ VCard bekommt ein uid update: " + serverCard.getN()[0] + " " + serverCard.getOldUid() + " neu: " + sHref.getUid());
						System.out.println("$$ neue uid in card: " + newCard.getUid());
						
						
						// update @Android
						//						
						if(androidId == -1){
							// Zum Update wird die dazugehörige _id in Android zwingend benötigt.
							throw new IndexOutOfBoundsException();
						}															
						insertUpdateContacts iuc = new insertUpdateContacts(androidId, mMap);				
						iuc.updateContact(mContext, newCard, androidId);
						
						
											
						// update @Server
						//
						/*
						String insertURL = Constants.CONTACTS_SERVER_URL + "/" + serverCard.getUid();// + ".vcf";
						String delURL = Constants.CONTACTS_SERVER_URL + "/" + serverCard.getUid();// + ".vcf";					
												
						WebDAV wd = new WebDAV(username, password);						
						wd.delteVCard(delURL);														
						wd.PutVCard(newCard.toString(), insertURL);
						*//*
						
					} catch (IOException e) {
						System.out.println("mcString: " + mcString);
						e.printStackTrace();
					} catch (ParserException e) {
						System.out.println("mcString: " + mcString);
						e.printStackTrace();
					} catch (Exception e) {
						System.out.println("Möglicherweise Problem beim VCard Upload?! serverURL: ");// + serverURL);
						e.printStackTrace();
					}	
					
				}
			}
		}
	}
	*/
	
	
	/**
	 * Vergleicht jede VCard von Android mit syncZustand 
	 * 
	 */
	public ContactOperations syncVglAndroid(){
		readAndroidContacts rac = new readAndroidContacts(mMap, username);
		if(debug!=null)rac.setKonsolenausgabe(debug);
		ContactsFromPhoneReader ContactReader = rac.getContactsPhoneReader(mContext, mMap, username);
		ContactReader.setKonsolenausgabe(debug);
		try{
			
			ContactOperations ret = syncVgl(ContactReader);
			return ret;
		}catch(Exception e){
			e.printStackTrace();
			if(debug!=null)debug.printStackTrace(e);
			return null;
		}
	}
	
	/**
	 * Vergleicht den syncZustand mit einer Menge von GevilVCards, die vom Server oder
	 * vom Client stammen können.
	 * 
	 * TODO return Liste von Operationen o.ä.
	 * @param reader
	 *//*
	private ContactOperations llöschensyncVgl(Iterator<GevilVCard> reader){
		try{
		
		ContactOperations ret = new ContactOperations(username, password, mContext, mMap, this);
		
		// Konsolenausgaben an/aus schalten
		boolean dbg = true;
		
		if(dbg)System.out.println(".");
		
		try{
		
		
			
			while(reader.hasNext()){
    			
    				GevilVCard card;
    				

    				if(reader instanceof ContactsFromServerReader){    	
    					// Fall der Etag sich nicht geändert hat, wird die VCard nicht vom Server geladen.
    					// statdessen wird ein Dummy geladen, der beim Vergleich immer als gleich betrachtet wird.
    					card = ((ContactsFromServerReader)reader).nextWatchEtag(this.serverFileNameHash, this.uidHash);    					
    				}else{	
    					// reader iteriert über VCards aus dem Android Adressbuch.
    					card = reader.next();
    				}
    				
    				
    				String cUID = card.getUid(); 
        			//if(dbg)System.out.println(".\n Iterator gibt card mi uid: " + cUID + " versuche match.");
        			
    				/*
        			if(cUID != null){
        				GevilVCard cardFromHash = this.uidHash.get(cUID);
        				if(cardFromHash!=null){
        					//
        					// Die VCard existiert auf beiden Seiten. Testen, ob Einträge sich geändert haben und ggf.
        					// entscheiden welche Änderung beibehalten wird.
        					//
        					
        					
        					//
        					// ZstVerglCard wurde gefunden. Sie muss also nichtmehr synchronisiert werden.
        					//
        					cardFromHash.FlagSyncTested = true;       
        					
        					
        					//
        					// Wenn der Iterator über Citadel Kontakte iteriert, sind in Card keine 
        					// Android _ids vorhanden. Die Android _id ist aber klar, weil sie im Zustand 
        					// per UID eindeutig identifiziert wurde.
        					//
        					if(card.getAndroidId()==-1){
        						card.setAndroidId(cardFromHash.getAndroidId());
        					}
        					
        					if(cardFromHash.compareTo(card) != 0){
        						// ein Detail hat sich auf einer Seite geändert. Ein Update ist notwendig.
        						if(dbg)System.out.println("********* MATCH, update notwendig _UID: " + cUID + " " + card.getN()[0] + 
        								card.getN()[1]);
        						
        						ret.addUpdate(card);
        						
        				
        					}else{
        						//if(dbg)System.out.println("********* MATCH, Person unverändert, _UID: " + card.getN()[0] + card.getN()[1] + " *****************\n.");
        						//System.out.print("   ===> Kontakt auf beiden Seiten unverändert.\n");
        					}
        					
        				}else{
        					//
        					// card hat eine UID, jedoch keine VCard unter SyncZustand hat die gleiche UID => 
        					// Die VCard muss kopiert werden.
        					//
        					if(dbg)System.out.println("********* _UID: " + cUID + " VCard muss nach Android (Citadel) kopiert << werden:");
        					if(dbg)System.out.println(card.getN()[0] + " " + card.getN()[1]);
        					if(dbg)System.out.println("cUID: " + cUID + " Vergleichskarte wurde nicht gefunden.");
        					if(dbg)System.out.println(".");        					
        					
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
        				if(dbg)System.out.println("********* _UID: " + cUID + " VCARD hat kein UID!! Fehler!! VCard muss nach Android (Citadel) kopiert werden:");
        				if(dbg)System.out.println("Im Hinblick darauf, dass jede VCard eine UID hat darf das nicht vorkommen...************************************");
        				if(dbg)System.out.println(card.getN()[0] + card.getN()[1]);
        				if(dbg)System.out.println(".");
        				if(dbg)System.out.println(".");
        				if(dbg)System.out.println(".");
    					
        				ret.addInsert(card);
        			}
        			*//*
    			
		}
		}catch(Exception e){
        			System.out.println("syncVgl() Exc. beim VCard Vergleich: " + e);
        			e.printStackTrace();
        			System.out.println(".");
        			debug.printStackTrace("sVGL(1)", e);
    			}
		
		//
		// Wir haben jetzt jeden Kontakt des Readers abgearbeitet. Anname: der Reader liest Kontakte vom Handy.
		// Falls auf dem handy jedoch ein Kontakt gelöscht wurde, ist dieser immernoch im Sync Zustand vorhanden und		
		// wurde nicht betrachtet. Dieser Kontakt muss dann gelöscht werden, ausgenommen, es gab einen Fehler beim matching
		// Deshalb: - Kontakte die schon abgearbeitet wurden markieren
		//			- in den folgenden zeilen: Nicht markierte Kontakte der Gegenseite, d.H. vom Zustand, anschauen.
		//
		/*
		Iterator<GevilVCard> altZustand= zustandGVCards.iterator();
		while(altZustand.hasNext()){		
			GevilVCard card = altZustand.next();
			if(!card.FlagSyncTested){				
				if(card.getUid()!=null){					
					// card ist im SyncZustand vorhanden. Nicht jedoch in Android (Citadel)
					// entweder sie wurde in Android (Citadel) gelöscht.
					if(dbg)System.out.println("_UID: " + card.getUid() + "********* VCard muss gelöscht werden:");
					if(dbg)System.out.println(card.getN()[0] + card.getN()[1]+"\n.");
					if(dbg)System.out.println(".");
					
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
					
					if(dbg)System.out.println("Zustand enthält eine VCard ohne Uid. Dies darf nicht vorkommen, weil nach abgeschlossener\n" +
							"Synchronisation jede VCard eine Uid zugewiesen haben muss.*****************************************************");
					if(dbg)System.out.println(card.getN()[0] + card.getN()[1]+"\n.");
					if(dbg)System.out.println(".");
					
				}
			}
			
			// die nächste Synchronisation kann beginnen.
			card.FlagSyncTested = false;
		}
		*//*
		return ret;
		}catch(Exception e){
			debug.printStackTrace(e);
			return null;
		}
		
	}*/
	
	
	
	
	private ContactOperations syncVgl(Iterator<GevilVCard> reader){
		try{
		
		ContactOperations ret = new ContactOperations(username, password, mContext, mMap, this, mNotification);
		
		// Konsolenausgaben an/aus schalten
		boolean dbg = true;
		
		if(dbg)System.out.println(".");
		
		while(reader.hasNext()){
    			try{
    				GevilVCard card;
    				if(reader instanceof ContactsFromServerReader){    	
    					// Fall der Etag sich nicht geändert hat, wird die VCard nicht vom Server geladen.
    					// statdessen wird ein Dummy geladen, der beim Vergleich immer als gleich betrachtet wird.
    					card = ((ContactsFromServerReader)reader).nextWatchEtag(this.serverFileNameHash, this.uidHash);    					
    				}else{	
    					// reader iteriert über VCards aus dem Android Adressbuch.
    					card = reader.next();
    				}
    				
    				String cUID = card.getUid(); 
        			//if(dbg)System.out.println(".\n Iterator gibt card mi uid: " + cUID + " versuche match.");
        			
        			if(cUID != null){
        				GevilVCard cardFromHash = this.uidHash.get(cUID);
        				if(cardFromHash!=null){
        					//
        					// Die VCard existiert auf beiden Seiten. Testen, ob Einträge sich geändert haben und ggf.
        					// entscheiden welche Änderung beibehalten wird.
        					//
        					
        					
        					//
        					// ZstVerglCard wurde gefunden. Sie muss also nichtmehr synchronisiert werden.
        					//
        					cardFromHash.FlagSyncTested = true;       
        					
        					
        					//
        					// Wenn der Iterator über Citadel Kontakte iteriert, sind in Card keine 
        					// Android _ids vorhanden. Die Android _id ist aber klar, weil sie im Zustand 
        					// per UID eindeutig identifiziert wurde.
        					//
        					if(card.getAndroidId()==-1){
        						card.setAndroidId(cardFromHash.getAndroidId());
        					}
        					
        					if(cardFromHash.compareTo(card) != 0){
        						// ein Detail hat sich auf einer Seite geändert. Ein Update ist notwendig.
        						if(dbg)System.out.println("********* MATCH, update notwendig _UID: " + cUID + " " + card.getN()[0] + 
        								card.getN()[1]);
        						
        						ret.addUpdate(card);
        						
        				
        					}else{
        						//if(dbg)System.out.println("********* MATCH, Person unverändert, _UID: " + card.getN()[0] + card.getN()[1] + " *****************\n.");
        						//System.out.print("   ===> Kontakt auf beiden Seiten unverändert.\n");
        					}
        					
        				}else{
        					//
        					// card hat eine UID, jedoch keine VCard unter SyncZustand hat die gleiche UID => 
        					// Die VCard muss kopiert werden.
        					//
        					if(dbg)System.out.println("********* _UID: " + cUID + " VCard muss nach Android (Citadel) kopiert << werden:");
        					if(dbg)System.out.println(card.getN()[0] + " " + card.getN()[1]);
        					if(dbg)System.out.println("cUID: " + cUID + " Vergleichskarte wurde nicht gefunden.");
        					if(dbg)System.out.println(".");        					
        					
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
        				if(dbg)System.out.println("********* _UID: " + cUID + " VCARD hat kein UID!! Fehler!! VCard muss nach Android (Citadel) kopiert werden:");
        				if(dbg)System.out.println("Im Hinblick darauf, dass jede VCard eine UID hat darf das nicht vorkommen...************************************");
        				if(dbg)System.out.println(card.getN()[0] + card.getN()[1]);
        				if(dbg)System.out.println(".");
        				if(dbg)System.out.println(".");
        				if(dbg)System.out.println(".");
    					
        				ret.addInsert(card);
        			}
        			
    			}catch(Exception e){
        			System.out.println("syncVgl() Exc. beim VCard Vergleich: " + e);
        			e.printStackTrace();
        			System.out.println(".");
        			debug.printStackTrace("sVGL(1)", e);
    			}
		}
		//
		// Wir haben jetzt jeden Kontakt des Readers abgearbeitet. Anname: der Reader liest Kontakte vom Handy.
		// Falls auf dem handy jedoch ein Kontakt gelöscht wurde, ist dieser immernoch im Sync Zustand vorhanden und		
		// wurde nicht betrachtet. Dieser Kontakt muss dann gelöscht werden, ausgenommen, es gab einen Fehler beim matching
		// Deshalb: - Kontakte die schon abgearbeitet wurden markieren
		//			- in den folgenden zeilen: Nicht markierte Kontakte der Gegenseite, d.H. vom Zustand, anschauen.
		//
		
		Iterator<GevilVCard> altZustand= zustandGVCards.iterator();
		while(altZustand.hasNext()){		
			GevilVCard card = altZustand.next();
			if(!card.FlagSyncTested){				
				if(card.getUid()!=null){					
					// card ist im SyncZustand vorhanden. Nicht jedoch in Android (Citadel)
					// entweder sie wurde in Android (Citadel) gelöscht.
					if(dbg)System.out.println("_UID: " + card.getUid() + "********* VCard muss gelöscht werden:");
					if(dbg)System.out.println(card.getN()[0] + card.getN()[1]+"\n.");
					if(dbg)System.out.println(".");
					
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
					
					if(dbg)System.out.println("Zustand enthält eine VCard ohne Uid. Dies darf nicht vorkommen, weil nach abgeschlossener\n" +
							"Synchronisation jede VCard eine Uid zugewiesen haben muss.*****************************************************");
					if(dbg)System.out.println(card.getN()[0] + card.getN()[1]+"\n.");
					if(dbg)System.out.println(".");
					
				}
			}
			
			// die nächste Synchronisation kann beginnen.
			card.FlagSyncTested = false;
		}
		
		return ret;
		}catch(Exception e){
			if(debug!=null)debug.printStackTrace(e);
			return null;
		}
		
	}
	
	
	
	public void setKonsolenausgabe(Konsolenausgabe debug) {
		this.debug = debug;
		
	}
	
}
