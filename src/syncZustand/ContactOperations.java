package syncZustand;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.Property.Id;



import syncZustand.GevilVCardHash.PropertyByValueSearcher;
import myHttp.WebDAV;

import com.gevil.OptionsLoader;
import com.gevil.notifications;
import com.gevil.AndroidContacts.InsertUpdateDeleteAndroidContacts;
import com.gevil.calSyncZustand.AktuellServerGevilVCalendar;
import com.gevil.calSyncZustand.GevilVCalendar;
import com.gevil.calSyncZustand.CalendarOperations.CalendarOp;
import com.gevil.calSyncZustand.CalendarOperations.SQLupdate_Insert;
import com.gevil.calSyncZustand.CalendarOperations.Update;
import com.gevil.syncadapter.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;



/**
 * Liste von Operationen die ausgeführt werden können
 * @author Martin
 *
 */
public class ContactOperations {
	
	private ArrayList<ContactOp> opList = new ArrayList<ContactOp>(0);
	private String mUsername;  	// username auf Server
	private String mPassword;	// passwort auf Server
	private Context mContext;	// (Gevil)Context
	private TypeBiMap mMap;
	private InsertUpdateDeleteAndroidContacts iudAndContacts;//= new InsertUpdateDeleteAndroidContacts(mMap, mContext, mUsername);
	private syncZustand mSyncZustand;
	
	notifications mNotification;
	
	HashMap<String, ContactOp> conflictHash;
	
	ContactOperations(String username, String password, Context ctx, TypeBiMap map, syncZustand sz, notifications notification){
		if(ctx==null){
			throw new NullPointerException();
		}
		
		mUsername = username;
		mPassword = password;
		mContext = ctx;
		mMap = map;
		mSyncZustand = sz;
		
		mNotification = notification;
		/*
		mNotification = new notificationactivity();
		mNotification.setData(mContext, "tblConflicts" + mUsername);
		mNotification.notificationsAdded= false;
		*/
		
		iudAndContacts = new InsertUpdateDeleteAndroidContacts(mMap, mContext, mUsername);
	}
	
	public ArrayList<ContactOp> getOpList(){
		return opList;		
	}
	
	/**
	 * einfügen einer Operation zur Operationsliste
	 * @param op
	 */
	public void addOp(ContactOp op){
		opList.add(op);
	}
	
	
	/**
	 * wird eingetragen wenn eine Operation entfernt wird.
	 * Dadurch kann die Liste weiter iteriert werden.
	 * @param serverOp
	 * @param list
	 */
	public void removeOp(ArrayList<ContactOp> list, ContactOp serverOp){
		//int index = opList.indexOf(u);
		
		int ind = list.indexOf(serverOp);
		list.set(ind, new removedOp(serverOp.getGevilVCard()));
		
	}
	
	
	
	
	/**
	 * Alle Operationen ausführen.
	 * @param android
	 */
	public void runAll(boolean android){
		
		
		Iterator<ContactOp> it = opList.iterator();
		
		while(it.hasNext()){
			ContactOp op = it.next();
			op.execute(android);
		}
		this.iudAndContacts.apply();		
			
	}
	
	
	
	
	/**
	 * Hashtabelle befüllen
	 */
	private void rehashConflictHash(){
		Iterator<ContactOp> it = opList.iterator();
		conflictHash = new HashMap<String, ContactOp>();
		
		while(it.hasNext()){
			ContactOp op = it.next();
			conflictHash.put(op.getGevilVCard().getUid(), op);
		}
	}
	
	
	/**
	 * Suche von Operationen die wiedersprüchlich sind (z.B. löschen in Android u. einfügen auf dem Server)
	 * Vergleicht this mit serverOps. Konvention: this sind die Operationen die auf Android ausgeführt
	 * werden. serverOps auf dem Server.
	 *
	 * @param serverOps Operationen die auf dem Server ausgeführt werden.
	 */
	public void findConflictingOperations(ContactOperations serverOps){
		// WICHTIG! rehash
		rehashConflictHash();	
		
		boolean dbg = true;		
		
		Iterator<ContactOp> itServerOps = serverOps.getOpList().iterator();
		
		while(itServerOps.hasNext()){
			ContactOp serverOp = itServerOps.next();
			
			String copsUID = serverOp.getGevilVCard().getUid();			
			
			ContactOp androidOp = conflictHash.get(copsUID);				
			
			if(androidOp != null){
				// Beim Benutzer nachfragen, welche Operation ausgeführt, und welche verwerfen werden soll.
				if(dbg)System.out.println("In Konflikt stehende Operationen gefunden:");
				if(dbg)System.out.println(serverOp.operationType() + " von " + serverOp.getGevilVCard() + "\n.");
				if(dbg)System.out.println(androidOp.operationType() + " von " + androidOp.getGevilVCard() + "\n.\n.");
				
				
				boolean conflictResolved = false;
				
				// 3 Fälle der Diagonale der (insert,update,delet)x(insert,update,delte) Matrix.
				if(androidOp instanceof insert && serverOp instanceof insert){
					/**
					 *  insert x insert
					 */
					
					/*
					 * tritt normalerweise auf, wenn der Sync-Zustand verloren gegangen ist.
					 */
					//System.out.println("Exception reason: In konflikt stehende Operationen: insert x insert");
					//throw new ArrayIndexOutOfBoundsException();
					
					if(androidOp.getGevilVCard().compareTo(serverOp.getGevilVCard())==0){
						// beide VCards sind gleich. D.h. sie müssen lediglich in den Sync-Zustand eingespeist werden.
						mSyncZustand.SQLinsert(serverOp.getGevilVCard());
						//serverOps.opList.remove(serverOp);
						removeOp(serverOps.opList, serverOp);						
						//this.opList.remove(androidOp);
						removeOp(opList, androidOp);						
						
						if(dbg)System.out.println("Insert Insert Konflikt, beide VCards sind identisch. Offenbar ist der Sync-Zustand verloren gegangen. VCard wird in den SZ eingefügt. " + serverOp.getGevilVCard().getN()[0]);
						
						conflictResolved = true;
					}else{
						//
						// Sync-Zustand fehlt, und beide Kontakte sind unterschiedlich.
						// Lösung: Nutzer muss Kontakte aneinander angleichen.
						//
						if(androidOp.getGevilVCard().getAndroidId()==-1)androidOp.getGevilVCard().setAndroidId(serverOp.getGevilVCard().getAndroidId());
						mNotification.addConflict("Die Unterschiede zwischen '" + androidOp.getGevilVCard().getReadableName() + "' und '" + serverOp.getGevilVCard().getReadableName() + "' konnten nicht aufgelöst werden. Bitte gleichen sie die Kontakte manuell aneinander an.", androidOp.getGevilVCard());
						mNotification.notificationsAdded = true;
						removeOp(serverOps.opList, serverOp);			
						removeOp(opList, androidOp);
						conflictResolved = true;
						
						
					}
					

					
					
				}else if(androidOp instanceof update && serverOp instanceof update){
					/**
					 *  update x update
					 */
					/*
					 * wirklich spannender Fall
					 */					
					GevilVCard joinedCard = joindUpdateVCards(androidOp.getGevilVCard(), serverOp.getGevilVCard(), dbg);
										
					if(dbg)System.out.println("Konfliktlösung: " + "Join ergab:\n" + joinedCard.toString() + "\n.");
					((update)androidOp).changeUpdateCard(joinedCard);
					((update)serverOp).changeUpdateCard(joinedCard);
					
					
					
					/*
					 * 
					 * 
					serverOps.opList.remove(serverOp);
					opList.remove(androidOp);
					*/
					
					conflictResolved = true;	
				}else if(androidOp instanceof delete && serverOp instanceof delete){
					// delete x delete
					/**
					 * Wenn links etwas gelöscht wurde, dann wurde es auch rechts gelöscht, und kann daher
					 * nichtmehr gelöscht werden.
					 */
					
					mNotification.addConflict("'" + androidOp.getGevilVCard().getReadableName() + "' wurde vom Benutzer auf beiden Seiten gelöscht. Es wird keine weitere Operation ausgeführt.", androidOp.getGevilVCard());
					mNotification.notificationsAdded = true;
					
					
					if(dbg)System.out.println("Konfliktlösung: " + "beide deletes werden verworfen. Kontakt wird aus dem Zustand entfernt.");
					//opList.remove(androidOp);
					removeOp(opList, androidOp);
					
					//serverOps.opList.remove(serverOp);
					removeOp(serverOps.opList, serverOp);
					
					mSyncZustand.SQLdelete(androidOp.getGevilVCard());	
					conflictResolved = true;
					
					
				}
				
				
				ContactOp swapServerOp = serverOp;
				ContactOp swapAndroidOp = androidOp;
				
				// 2*3 Fälle liegen nich auf der Diagonale der (insert,update,delet)*(insert,update,delte) Matrix.
				for(int i=0;i<=1;i++){
					if(swapAndroidOp instanceof insert){
						if(swapServerOp instanceof update){
							// insert x update
							/**
							 * Dieser Fall darf eigentlich nicht auftreten...
							 */
							mNotification.addConflict("'" + androidOp.getGevilVCard().getReadableName() + "' Insert * Update Konflikt. Wegen Inkonsitenz wurde die Synchronisation beendet.", androidOp.getGevilVCard());
							if(dbg)System.out.println("Exception reason: In konflikt stehende Operationen: update x insert");
							throw new ArrayIndexOutOfBoundsException();
						}else if(swapServerOp instanceof delete){
							// insert x delete: delete wird nicht ausgeführt.
							/**
							 * Darf streng genommen auch nicht auftreten.
							 */
							if(dbg)System.out.println("Konfliktlösung: " + "delete wird verworfen.");
							mNotification.addConflict("'" + androidOp.getGevilVCard().getReadableName() + "' Insert * Delete Konflikt. Sicherheitshalber wurde das Delete verworfen.", androidOp.getGevilVCard());
							//this.chooseServerOps(i, serverOps, this).remove(swapServerOp);
							removeOp(this.chooseServerOps(i, serverOps, this), swapServerOp);
							
							conflictResolved = true;	
							
							mNotification.addConflict(serverOp.getGevilVCard().getN()[0] + " wurde auf einer Seite verändert und auf der anderen gelöscht. Zur Sicherheit wird der Löschvorgagn verworfen.", serverOp.getGevilVCard());
							mNotification.notificationsAdded = true;
						}
					}else if(swapAndroidOp instanceof update){
						if(swapServerOp instanceof delete){
							// update x delete
							if(dbg)System.out.println("Konfliktlösung: " + "delete verwerfen, Update wird zu Insert, SQL Update");
							mNotification.addConflict("'" + androidOp.getGevilVCard().getReadableName() + "' Wurde auf einer Seite gelöscht und auf der anderen verändert. Sicherheitshalber wurde der Löschvorgang verworfen.", androidOp.getGevilVCard());
							mNotification.notificationsAdded = true;
							
							// das Update in ein Insert verwandeln							
							ArrayList<ContactOp>  androidOpList = chooseAndroidOps(i, serverOps, this);
							int indAndOp = androidOpList.indexOf(swapAndroidOp);						
							this.changeUpdateSQLupdate_Insert(indAndOp, androidOpList);		
							
							
							
							// Delete verwerfen:
							removeOp(this.chooseServerOps(i, serverOps, this), swapServerOp);
							conflictResolved = true;
							
							
														
							
						}									
					}
					

					// androidOp mit serverOp vertauschen, um Symetrische Fälle zu prüfen.					
					ContactOp temp = swapAndroidOp;
					swapAndroidOp=swapServerOp;
					swapServerOp=temp;
				}
				
				if(!conflictResolved){
					mNotification.addConflict("'" + androidOp.getGevilVCard().getReadableName() + "' Fehler: ungelöster Konflikt.", androidOp.getGevilVCard());
					if(androidOp instanceof delete){
						//opList.remove(androidOp);
						removeOp(opList, androidOp);
					}else{
						//serverOps.opList.remove(serverOp);						
						removeOp(serverOps.opList, serverOp);
					}	
				}
				
				if(mNotification.notificationsAdded){
					if(dbg)System.out.println(">showUserNotification wurde aufgerufen.");
					mNotification.showUserNotification(mContext);
					mNotification.notificationsAdded = false;
				}else{
					if(dbg)System.out.println(">showUserNotification NICHT aufgerufen.");
				}
					
				
				
			}	
		}		
		conflictHash = null;
	}
	
	private void changeUpdateSQLupdate_Insert(int index, ArrayList<ContactOp> list) {
		//int index = opList.indexOf(u);
		
		ContactOp u = list.get(index);
		if(!(u instanceof update)){
			throw new ArrayIndexOutOfBoundsException();
		}
		
		insert_SQLupdate ins = new insert_SQLupdate(u.getGevilVCard());		
		list.set(index, ins);
		
	}

	private ArrayList<ContactOp> chooseAndroidOps(int i, ContactOperations serverO, ContactOperations androidO) {
		if(i==1){
			return serverO.opList;
		}else if(i==0){
			return androidO.opList;
		}else{
			throw new IndexOutOfBoundsException();
		}
	}

	/**
	 * Gibt die ServerOps zurück, wobei für i=1 serverOps und androidOps
	 * als vertauscht angenommen werden.
	 * @param i
	 * @param serverO
	 * @param androidO
	 * @return
	 */
	private ArrayList<ContactOp> chooseServerOps(int i, ContactOperations serverO, ContactOperations androidO){
		if(i==0){
			return serverO.opList;
		}else if(i==1){
			return androidO.opList;
		}else{
			throw new IndexOutOfBoundsException();
		}
	}
	
	
	/**
	 * Im Falle eines Update Update Konflikts, findet die Methode alle feingranularen Operationen
	 * und erstellt dem entsprechend eine neue VCard.
	 * @param andriod
	 * @param server
	 * @return
	 */
	private GevilVCard joindUpdateVCards(GevilVCard andriod, GevilVCard server, boolean dbg){
		ArrayList<Property> Handy = new ArrayList<Property>();
		ArrayList<Property> Zustand = new ArrayList<Property>();
		ArrayList<Property> Server = new ArrayList<Property>();
		
		
		 
		
		
		//
		// wir haben die VCard vom Handy und vom Server. Jetzt holen wir uns zum Vergleich noch
		// die aus dem Sync-Zustand.
		//
		GevilVCard zustand = mSyncZustand.uidHash.get(andriod.getUid());		
		
		
		
		//
		// Vor- und Nachname der zusammengesetzten VCard, ret, zusammenführen.
		//
		GevilVCard ret = new GevilVCard(mMap);
		try {
			ret.setUid(andriod.getUid());
			ret.setEtag(andriod.getEtag());
			String serverPath = zustand.getServerPath();
			if(serverPath==null) serverPath= andriod.getServerPath();
			if(serverPath==null) serverPath= server.getServerPath();
			ret.setServerPath(serverPath);
			
			int androidId = zustand.getAndroidId();
			if(androidId==-1) androidId = andriod.getAndroidId();
			if(androidId==-1) androidId = server.getAndroidId();
			ret.setAndroidId(androidId);
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
			// hier: abstürzen.
			return null;			
		}
		
		
		//
		// vor- und Nachname joinen
		//
		/*
		boolean namesJoined = false;
		String vorName;
		String nachName;
		String[] aN = a.getN();
		String[] bN = b.getN();				
		if(aN[0].equals(bN[0])){
			vorName = bN[0];
		}else{
			vorName = aN[0] + "(" + bN[0] + ")";
			namesJoined = true;			
		}		
		if(aN[1].equals(bN[1])){
			nachName = bN[1];
		}else{
			nachName = aN[1] + "(" +  bN[1] + ")";
			namesJoined = true;
		}
		ret.setN(vorName, nachName);		
		
		if(namesJoined){
			mNotification.addConflict("Der Name von '" + z.getReadableName() + "' wurde zu  '" + ret.getReadableName() + "' geändert.", ret);
			mNotification.notificationsAdded = true;
		}
		*/
		
		if(OptionsLoader.getInstance().getContactPhoneWins(mContext, mUsername)){
			// Phone Wins
			ret.setN(andriod.getN()[0], andriod.getN()[1]);
			mNotification.addConflict("Der Name von '" + zustand.getReadableName() + "' wurde zu  '" + ret.getReadableName() + "' geändert.", ret);
		}else{
			// Server Wins
			ret.setN(server.getN()[0], server.getN()[1]);
			mNotification.addConflict("Der Name von '" + zustand.getReadableName() + "' wurde zu  '" + ret.getReadableName() + "' geändert.", ret);
		}
		
		//ret.joinAndSetNames(a.getN(), b.getN());
		
		//
		// Email und Telefon Properties nach Delete und Insert 
		// Durchsuchen und dementsprechend zusammenbauen.
		// 
		ArrayList<Property>[] mailPropertyChanges = 
		this.fineGranePropertyJoin(andriod, zustand, server, Id.EMAIL, ret, dbg);
		this.fineGranePropertyJoin(andriod, zustand, server, Id.TEL, ret, dbg);
		
		
		//handyListe, zustandsListe, serverListe};
		
		Server = mailPropertyChanges[0];
		Zustand = mailPropertyChanges[1];		
		Handy = mailPropertyChanges[2];	
		
		if(dbg){  // Ausgabe der Konfliktabellen
			int size = Math.max( Math.max(Handy.size(), Zustand.size()), Server.size());
			String zeile;		
			System.out.println("Konflikttabellen: " );
			System.out.println("Handy\t Zustand\t Server");		
			for(int i=0; i<=size;i++){
				zeile = "";			
				if(i<Handy.size()){
					if(Handy.get(i)!=null)zeile += Handy.get(i).getValue();
				}zeile += ".\t";
				if(i<Zustand.size()){
					if(Zustand.get(i)!=null)zeile += Zustand.get(i).getValue();
				}zeile += ".\t";
				if(i<Server.size()){
					if(Server.get(i)!=null)zeile += Server.get(i).getValue();
				}zeile += ".\t";			
				System.out.println(zeile);			
			}
			System.out.println("\n.");
		}
		
		
		return ret;
	}
	
	/**
	 * Gibt drei Liste zurück, in denen jeweils nur die in 
	 * Konflikt stehenden Properties enthalten sind.
	 * 
	 * @param handy
	 * @param zustand
	 * @param server
	 * @param emailOrPhone
	 * @return ArrayList[]{handyListe, zustandsListe, serverListe}
	 */
	private ArrayList<Property>[] fineGranePropertyJoin(GevilVCard handy, GevilVCard zustand, GevilVCard server, Id emailOrPhone, GevilVCard ergebnis, boolean dbg){
		 PropertyByValueSearcher valueH = handy.getHash().propertiesByValue(emailOrPhone);
		 PropertyByValueSearcher valueZ = zustand.getHash().propertiesByValue(emailOrPhone);
		 PropertyByValueSearcher valueS = server.getHash().propertiesByValue(emailOrPhone);
		 
		 ArrayList<Property> handyListe = new ArrayList<net.fortuna.ical4j.vcard.Property>();
		 ArrayList<Property> zustandsListe = new ArrayList<net.fortuna.ical4j.vcard.Property>();
		 ArrayList<Property> serverListe = new ArrayList<net.fortuna.ical4j.vcard.Property>();
		 		
		 String[] name = handy.getN();
		 ergebnis.setN(name[0], name[1]);
		 
		 
		 /*
		  * Suche zu handyProperty jeweils ein identisches Property in Zustand und Server
		  * rufe dann three Property mit dem gefundenen, identischen, Property auf. 
		  * Falls kein identisches Property gefunden wird, ist der Wert null.
		  */
		 Property handyProperty = valueH.getUninsertedPropertyOnce();
		 Property zustandProperty;
		 Property serverProperty;
		 while(handyProperty!=null){
			 zustandProperty = valueZ.getPropertyByValueOnce(handyProperty.getValue());
			 serverProperty = valueS.getPropertyByValueOnce(handyProperty.getValue());
			 
			 threPropertyCompare( 
					    handyListe, zustandsListe,  serverListe,
						handyProperty,  zustandProperty,  serverProperty, ergebnis, dbg);
			 
			 handyProperty = valueH.getUninsertedPropertyOnce();
			 
		 }		 
		 
		 /*
		  * Suche den verbleibenden zustandProperties jeweils ein identisches Property in Handy und Server
		  * rufe dann three Property mit dem gefundenen, identischen, Property auf. 
		  * Falls kein identisches Property gefunden wird, ist der Wert null.
		  */
		 zustandProperty = valueZ.getUninsertedPropertyOnce();	
		 while(zustandProperty!=null){
			handyProperty = valueH.getPropertyByValueOnce(zustandProperty.getValue());		 
			serverProperty = valueS.getPropertyByValueOnce(zustandProperty.getValue()); 
			 
			threPropertyCompare( 
				    handyListe, zustandsListe,  serverListe,
					handyProperty,  zustandProperty,  serverProperty, ergebnis, dbg);
			 
			zustandProperty = valueZ.getUninsertedPropertyOnce();
		 }
		 
		 /*
		  * Suche den verbleibenden serverProperties jeweils ein identisches Property in Handy und Server
		  * rufe dann three Property mit dem gefundenen, identischen, Property auf. 
		  * Falls kein identisches Property gefunden wird, ist der Wert null.
		  */
		 serverProperty = valueS.getUninsertedPropertyOnce();	
		 while(serverProperty!=null){
			handyProperty = valueH.getPropertyByValueOnce(serverProperty.getValue());		 
			zustandProperty = valueZ.getPropertyByValue(serverProperty.getValue());
			 
			threPropertyCompare( 
				    handyListe, zustandsListe,  serverListe,
					handyProperty,  zustandProperty,  serverProperty, ergebnis, dbg);
			 
			serverProperty = valueS.getUninsertedPropertyOnce();	
		 }
		 
		 
		 if(dbg)System.out.println("Ergebnis der feingranularen Konfliktlösung: \n" + ergebnis.toString() + "\n.");

		 return new ArrayList[]{handyListe, zustandsListe, serverListe};
	}
	
	/**
	 * Hilfsmethode von fineGraneJoin. Findet an Hand der drei Properties, ob
	 * es sich um ein Insert oder ein Delete handelt, und fügt das Property ggf. zu ergebnis
	 * hinzu.
	 * @param handyListe
	 * @param zustandsListe
	 * @param serverListe
	 * @param handyProperty 	entweder null oder identsich mit den beiden anderen Properties
	 * @param zustandProperty 	entweder null oder identsich mit den beiden anderen Properties
	 * @param serverProperty 	entweder null oder identsich mit den beiden anderen Properties
	 */
	private void threPropertyCompare(
	ArrayList<Property> handyListe, ArrayList<Property> zustandsListe, ArrayList<Property> serverListe,
	Property handyProperty, Property zustandProperty, Property serverProperty,
	GevilVCard ergebnis, boolean dbg){
		
			if(!propertiesIdentisch(handyProperty, zustandProperty) || 
				    !propertiesIdentisch(handyProperty, serverProperty)){
					 // nicht alle gleich
					 //if(handyProperty!=null)
						 handyListe.add(handyProperty);
					 //if(zustandProperty!=null)
						 zustandsListe.add(zustandProperty);
					 //if(serverProperty!=null)
						 serverListe.add(serverProperty);
						 
						 
					if(zustandProperty!=null){
						if(serverProperty==null && handyProperty==null){
							// Delete, sogar auf beiden Seiten!
							if(dbg)System.out.println("feingranulares Delete von: " + zustandProperty.getValue());
							
						}else{
							if(propertiesIdentisch(serverProperty,zustandProperty) && propertiesIdentisch(handyProperty,zustandProperty)){
								// alle drei gleich. Kann ausgechlossen werden
								throw new ArrayIndexOutOfBoundsException();
								
							}else if(serverProperty!=null && !propertiesIdentisch(serverProperty,zustandProperty)){
								// Änderung im Server. (Parameter)
								ergebnis.addProperti(serverProperty);
								if(dbg)System.out.println("feingranulare Parameteränderung im Server: " + serverProperty.getValue());
								
							}
							else if(handyProperty!=null && !propertiesIdentisch(handyProperty,zustandProperty)){
								// Änderung im Handy. (Parameter)
								ergebnis.addProperti(handyProperty);
								if(dbg)System.out.println("feingranulare Parameteränderung im Handy: " + handyProperty.getValue());
								
							}else if(handyProperty==null || serverProperty==null){
								// Delete: 
								if(dbg)System.out.println("feingranulares, Delete von: " + zustandProperty.getValue());
								
							}else{
								// es sollte kein Fall mehr übrig sein.
								throw new ArrayIndexOutOfBoundsException();
							}
						}
					}else{		// ZustandProperty == null
						// Insert   (eventuell Update, das kann aber nicht sein, weil nur gleiche Properties gesucht werden...)
						Property nP = handyProperty;
						if(nP==null)nP=serverProperty;
						if(nP==null)throw new ArrayIndexOutOfBoundsException();  // dazu müssten alle drei Properties null sein
																				 // D.h. aber dass alle gleich sind => Wiederspruch						
						ergebnis.addProperti(nP);
						if(dbg)System.out.println("feingranulares Insert von: " + nP.getValue());
					}
			}else{				
				// alle Properties sind identisch.
				if(zustandProperty!=null)
					ergebnis.addProperti(zustandProperty);
				
				if(dbg)System.out.println("feingranula keinerlei Änderung bei: " + zustandProperty.getValue());
			}
	}
	
	
	/**
	 * Vegleicht zwei Properties nach Name UND allen Parametern.
	 * @param kProp
	 * @param zProp
	 * @return ob die beiden Properties gleichen Namen UND gleiche Properties haben.
	 */
	private boolean propertiesIdentisch(Property kProp, Property zProp){
		if(kProp==null && zProp == null) return true;
		if(kProp==null) return false;
		if(zProp==null) return false;
		
			if(kProp.getValue().equals(zProp.getValue())){
				List<Parameter> psK = kProp.getParameters();
				List<Parameter> psZ = zProp.getParameters();
				
				boolean vergleichsParameterFound = false;				
				for(Parameter pK:psK){
					vergleichsParameterFound = false;
					for(Parameter pZ:psZ){
						if(pZ.getValue().equals(pK.getValue())){
							vergleichsParameterFound=true;
						}	
					}
					if(!vergleichsParameterFound){
						return false;
					}
				}	
				// getValue identisch und alle Parameter sind identsich (=immer vergleichsParameterFound)
				return true;
			}else{
				return false;
			}
	}
	


	
	/*
	 * Ab jetzt: insert update dele Klassen
	 *
	 */
	
	abstract class ContactOp{
		GevilVCard OpVCard;		
		
		ContactOp(GevilVCard card){
			OpVCard = card;					
		}		
		
		public GevilVCard getGevilVCard(){
			return OpVCard;
		}
		
		abstract void execute(boolean android);
		
		abstract String operationType();
	}
	
	
	public void addInsert(GevilVCard card){
		this.addOp(new insert(card));
	}
	
	/** 
	 * Einfügen eines neuen Kontaktes
	 * @author Martin
	 * 
	 */
	public class insert extends ContactOp{

		insert(GevilVCard card) {
			super(card);
			// TODO Auto-generated constructor stub
		}

		@Override
		void execute(boolean android) {
			// durch den großen try-catch Block soll eine ganz oder garnicht Semantik 
			// erreicht werden.
			try {
				
				if(android){
					
					
					System.out.println("@execute: insert to Android von " + OpVCard.getN()[0]);				
					
					/*
					OldinsertUpdateContacts iuc = new OldinsertUpdateContacts(0, mMap);				
					iuc.addNewContact(mContext, OpVCard, mUsername, Constants.ACCOUNT_TYPE);
					*/				
					iudAndContacts.insert(OpVCard);
					
				}else{
					System.out.println("@execute: insert to Server " + OpVCard.getN()[0]);				
					
					WebDAV wd = new WebDAV(mUsername, mPassword, mMap);
					
						String serverURL = OptionsLoader.getInstance().getContactsServerUrl(mContext, mUsername);
						wd.PutVCard(OpVCard, serverURL + "/" + OpVCard.getUid() + ".vcf");
	
				}
				
				// Android UND Server inserts werden auch im Sync Zustand ausgeführt.
				mSyncZustand.SQLinsert(OpVCard);
			
			} catch (Exception e) {
					System.out.println("Exception beim upload von " + OpVCard.getN()[0]);
					e.printStackTrace();
					System.out.println(".");
			}
			
		}

		@Override
		String operationType() {
			return "Insert";
		}
		
	}
	
	
	public void addUpdate(GevilVCard card){
		this.addOp(new update(card));
	}
	
	/**
	 * Aktualisieren eines bestehenden Kontaktes
	 * @author Martin
	 *
	 */
	public class update extends ContactOp{
		
		update(GevilVCard card){
			super(card);
			/*
			if(card.getAndroidId()==-1){
				// Ein Update ist nur möglich, wenn die Android _id bekannt ist.
				System.out.println(card.toString());
				throw new IndexOutOfBoundsException();
			}
			*/
		}
		
		public void changeUpdateCard(GevilVCard nCard){
			OpVCard = nCard;
			/*if(nCard.getAndroidId()==-1){
				// Ein Update ist nur möglich, wenn die Android _id bekannt ist.
				System.out.println(nCard.toString());
				throw new IndexOutOfBoundsException();
			}*/
		}

		@Override
		void execute(boolean android) {
			// durch den großen try-catch Block soll eine ganz- oder garnicht Semantik erreicht werden.
			try {
				

				
				if(android){
					System.out.println("@execute: Android update von " + OpVCard.getN()[0]);
					
					/*
					OldinsertUpdateContacts iuc = new OldinsertUpdateContacts(and_id, mMap);				
					iuc.updateContact(mContext, OpVCard, and_id);
					*/
					
					int androidId = OpVCard.getAndroidId();
					if(androidId == -1){
						androidId=iudAndContacts.findAndroidId(OpVCard.getUid());
						OpVCard.setAndroidId(androidId);
						System.out.println("contactOPS.update, androidID musste durch suchen gefunden werden: " + OpVCard.getN()[0] + ", Ergebnis: " + androidId);					
					}
					
					iudAndContacts.updateContact(OpVCard, androidId);
					
				}else{
					System.out.println("@execute: Server update von " + OpVCard.getN()[0] + ", verwendete UID: " + OpVCard.getUid());
	
					WebDAV wd = new WebDAV(mUsername, mPassword, mMap);
					
						String serverURL = OptionsLoader.getInstance().getContactsServerUrl(mContext, mUsername);
						wd.PutVCard(OpVCard, serverURL + "/" + OpVCard.getUid());// + ".vcf");
					
				}
				
				// Android UND Server Updates werden auch im Sync Zustand ausgeführt.
				mSyncZustand.SQLupdate(OpVCard);
				
			} catch (Exception e) {
					System.out.println("Exception beim upload von " + OpVCard.getN()[0]);
					e.printStackTrace();
					System.out.println(".");
			}
			
			
		}

		@Override
		String operationType() {
			return "Update";
		}
	}
	
	
	public void addDelete(GevilVCard card){
		this.addOp(new delete(card));
	}
	
	/**
	 * Löschen eines Kontaktes
	 * @author Martin
	 *
	 */
	public class delete extends ContactOp{

		delete(GevilVCard card) {
			super(card);
			/*
			if(card.getAndroidId()==-1){
				// Ein Delete ist nur möglich, wenn die Android _id bekannt ist.
				System.out.println("Exception Card uid: " + card.getUid() + " " + card.getN()[0]);
				//throw new IndexOutOfBoundsException();
			}
			*/
		}

		@Override
		void execute(boolean android) {
			if(android){
				System.out.println("@execute: Android delete von " + OpVCard.getN()[0]);
				
				/*
				OldinsertUpdateContacts iuc = new OldinsertUpdateContacts(0, mMap);				
				iuc.delelteContact(OpVCard.getAndroidId(), mContext);
			    */
				
				int androidId = OpVCard.getAndroidId();
				if(androidId == -1){
					androidId=iudAndContacts.findAndroidId(OpVCard.getUid());
					OpVCard.setAndroidId(androidId);
				}				
				iudAndContacts.delete(androidId);
				
				
			}else{
				String serverURL = OptionsLoader.getInstance().getContactsServerUrl(mContext, mUsername);
				String delURL = serverURL + "/" + OpVCard.getUid();
				
				System.out.println("@execute: Server delete von " + OpVCard.getN()[0] + ", " + delURL);
				
				WebDAV wd = new WebDAV(mUsername, mPassword, mMap);
				try {
					wd.delteVCard(delURL);
				} catch (Exception e) {
					System.out.println("Exception beim Delete von " + delURL);
					e.printStackTrace();					
				}
				
			}
			
			// Android UND Server Deletes werden auch im Sync Zustand ausgeführt.
			mSyncZustand.SQLdelete(OpVCard);			
		}

		@Override
		String operationType() {
			return "Delete";
		}
		
	}
	
	
	/** 
	 * Einfügen eines neuen Kontaktes
	 * @author Martin
	 * 
	 */
	public class insert_SQLupdate extends ContactOp{

		insert_SQLupdate(GevilVCard card) {
			super(card);
			// TODO Auto-generated constructor stub
		}

		@Override
		void execute(boolean android) {
			// durch den großen try-catch Block soll eine ganz oder garnicht Semantik 
			// erreicht werden.
			try {
				
				if(android){
					
					
					System.out.println("@execute: insert to Android von " + OpVCard.getN()[0]);				
					
					/*
					OldinsertUpdateContacts iuc = new OldinsertUpdateContacts(0, mMap);				
					iuc.addNewContact(mContext, OpVCard, mUsername, Constants.ACCOUNT_TYPE);
					*/				
					iudAndContacts.insert(OpVCard);
					
				}else{
					System.out.println("@execute: insert to Server " + OpVCard.getN()[0]);				
					
					WebDAV wd = new WebDAV(mUsername, mPassword, mMap);
					
						String serverURL = OptionsLoader.getInstance().getContactsServerUrl(mContext, mUsername);
						wd.PutVCard(OpVCard, serverURL + "/" + OpVCard.getUid() + ".vcf");
	
				}
				
				// Android UND Server inserts werden auch im Sync Zustand ausgeführt.
				mSyncZustand.SQLupdate(OpVCard);
			
			} catch (Exception e) {
					System.out.println("Exception beim upload von " + OpVCard.getN()[0]);
					e.printStackTrace();
					System.out.println(".");
			}
			
		}

		@Override
		String operationType() {
			return "Insert";
		}
		
	}
	
	public class removedOp extends ContactOp{

		removedOp(GevilVCard card) {
			super(card);
			// TODO Auto-generated constructor stub
		}

		@Override
		void execute(boolean android) {
			/* onlyZustandInsert wird nicht in Android oder dem Server ausgeführt.
			if(android){
				kDebug.println("@execute: Android insert von " + OpVCard.getSummary() );
				iudc.insert(OpVCard, mUsername);
				
			}else{
				kDebug.println("@execute: insert to Server " + OpVCard.getSummary());				
				
				WebDAV wd = new WebDAV(mUsername, mPassword, mMap);
				try {
					wd.PutVCalendar(OpVCard, Constants.CALENDAR_SERVER_URL + "/" + OpVCard.getUid() + ".vcf");
				} catch (Exception e) {
					kDebug.printStackTrace("Exception beim upload von: "+ OpVCard.getPropValue("SUMMARY"), e);
				}
			}
				
			// Android UND Server inserts werden auch im Sync Zustand ausgeführt.
			mSyncZustand.SQLinsert(OpVCard);
			*/
		}

		@Override
		public String operationType() {
			return "removedOp";
		}
		
	}
	

}
