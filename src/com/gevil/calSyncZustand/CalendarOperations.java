package com.gevil.calSyncZustand;


import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.Property.Id;

import org.apache.commons.lang.NotImplementedException;
import myHttp.WebDAV;
import syncZustand.GevilVCard;
import syncZustand.TypeBiMap;
import android.content.Context;
import android.os.Debug;

import com.gevil.OptionsLoader;
import com.gevil.notifications;
import com.gevil.AndroidCalendar.InsertUpdateDeleteAndroidCalendar;
import com.gevil.AndroidCalendar.Konsolenausgabe;
import com.gevil.calSyncZustand.CalendarOperations.CalendarOp;
import com.gevil.syncadapter.Constants;

public class CalendarOperations {

	
	private ArrayList<CalendarOp> opList = new ArrayList<CalendarOp>(0);
	private String mUsername;  	// username auf Server
	private String mPassword;	// passwort auf Server
	private Context mContext;	// (Gevil)Context
	private TypeBiMap mMap;	
	private calSyncZustand mSyncZustand;
	private InsertUpdateDeleteAndroidCalendar iudc;
	Konsolenausgabe kDebug = new Konsolenausgabe();
	
	HashMap<String, CalendarOp> conflictHash;
	private notifications mNotification;
	
	CalendarOperations(String username, String password, Context ctx, TypeBiMap map, calSyncZustand sz, notifications notification){
		if(ctx==null){
			throw new NullPointerException();
		}else if(notification == null) throw new NullPointerException();
		
		mUsername = username;
		mPassword = password;
		mContext = ctx;
		mMap = map;
		mSyncZustand = sz;
		iudc = new InsertUpdateDeleteAndroidCalendar(mContext, mMap);
		
		mNotification = notification;		
		
		//iudAndContacts = new InsertUpdateDeleteAndroidContacts(mMap, mContext, mUsername);
	}
	
	public void setKonsolenausgabe(Konsolenausgabe Konsolenausgabe) {
		kDebug = Konsolenausgabe;
		iudc.setKonsolenersatz(Konsolenausgabe);
		
	}
	
	public ArrayList<CalendarOp> getOpList(){
		return opList;		
	}
	
	/**
	 * einfügen einer Operation zur Operationsliste
	 * @param op
	 */
	public void addOp(CalendarOp op){
		opList.add(op);
	}
	
	/**
	 * macht aus einem Update ein Insert.
	 * Wird bei Delete Update Konflikten gemacht.
	 * @param u
	 */
	public void changeUpdateToInsert(int index, ArrayList<CalendarOp> list){
		//int index = opList.indexOf(u);
		
		CalendarOp u = list.get(index);
		if(!(u instanceof Update)){
			throw new ArrayIndexOutOfBoundsException();
		}
		
		Insert ins = new Insert(u.getGevilCalendar());
		ins.noExecSQLInsert=true;
		list.set(index, ins);
		
	}
	
	
	/**
	 * macht aus einem Update ein Insert.
	 * Wird bei Delete Update Konflikten gemacht.
	 * @param u
	 */
	public void changeUpdateSQLupdate_Insert(int index, ArrayList<CalendarOp> list){
		//int index = opList.indexOf(u);
		
		CalendarOp u = list.get(index);
		if(!(u instanceof Update)){
			throw new ArrayIndexOutOfBoundsException();
		}
		
		SQLupdate_Insert ins = new SQLupdate_Insert(u.getGevilCalendar());		
		list.set(index, ins);
		
	}
	
	/**
	 * wird eingetragen wenn eine Operation entfernt wird.
	 * Dadurch kann die Liste weiter iteriert werden.
	 * @param serverOp
	 * @param list
	 */
	public void removeOp(ArrayList<CalendarOp> list, CalendarOp serverOp){
		//int index = opList.indexOf(u);
		
		int ind = list.indexOf(serverOp);
		list.set(ind, new RemovedOp(serverOp.getGevilCalendar()));
		
	}
	
	
	
	/**
	 * Alle Operationen ausführen.
	 * @param android
	 */
	public void runAll(boolean android){		
		Iterator<CalendarOp> it = opList.iterator();
		
		while(it.hasNext()){
			CalendarOp op = it.next();
			op.execute(android);
		}
		//if(android)this.iudAndContacts.apply();
	}
	
	
	
	
	/**
	 * Hashtabelle befüllen
	 */
	private void rehashConflictHash(){
		Iterator<CalendarOp> it = opList.iterator();
		conflictHash = new HashMap<String, CalendarOp>();
		
		while(it.hasNext()){
			CalendarOp op = it.next();
			conflictHash.put(op.getGevilCalendar().getUid(), op);
		}
	}
	
	
	
	
	/**
	 * Suche von Operationen die wiedersprüchlich sind (z.B. löschen in Android u. einfügen auf dem Server)
	 * Vergleicht this mit serverOps. Konvention: this sind die Operationen die auf Android ausgeführt
	 * werden. serverOps auf dem Server.
	 *
	 * @param serverOps Operationen die auf dem Server ausgeführt werden.
	 */
	public void findConflictingOperations(CalendarOperations serverOps){
		// WICHTIG! rehash
		rehashConflictHash();	
		
		boolean dbg = true;
		
		Iterator<CalendarOp> itServerOps = serverOps.getOpList().iterator();
		
		while(itServerOps.hasNext()){
			CalendarOp serverOp = itServerOps.next();
			
			String copsUID = serverOp.getGevilCalendar().getUid();			
			
			CalendarOp androidOp = conflictHash.get(copsUID);				
			
			if(androidOp != null){
				// Beim Benutzer nachfragen, welche Operation ausgeführt, und welche verwerfen werden soll.
				if(dbg)kDebug.println("In Konflikt stehende Operationen gefunden:");
				if(dbg)kDebug.println(serverOp.operationType() + " von " + serverOp.getGevilCalendar() + "\n.");
				if(dbg)kDebug.println(androidOp.operationType() + " von " + androidOp.getGevilCalendar() + "\n.\n.");
				
				
				boolean conflictResolved = false;
				
				// 3 Fälle der Diagonale der (insert,update,delet)x(insert,update,delte) Matrix.
				if(androidOp instanceof Insert && serverOp instanceof Insert){
					/**
					 *  insert x insert
					 */

					//kDebug.println("Exception reason: In konflikt stehende Operationen: insert x insert");
					//throw new ArrayIndexOutOfBoundsException();
					
					// == das if wird ausgeführt!!
					
					if(androidOp.getGevilCalendar().compareTo(serverOp.getGevilCalendar())==0){
						// beide VCards sind gleich. D.h. sie müssen lediglich in den Sync-Zustand eingespeist werden.
						/*
						mSyncZustand.SQLinsert(serverOp.getGevilCalendar());
						serverOps.opList.remove(serverOp);
						this.opList.remove(androidOp);
						*/
						
						// Beide Operationen verwerfen. Jedoch danach in Sync Zustand einfügen.					
						//serverOps.opList.remove(serverOp);
											
						removeOp(serverOps.opList, serverOp);
						
						// statt sie zu löschen wird die AndroidOp mit der onlyZustandInsert -Op überschrieben.
						int ind = opList.indexOf(androidOp);
						opList.set(ind, new OnlyZustandInsert(androidOp.OpVCard));
						
						if(dbg)kDebug.println("Insert Insert Konflikt, beide VCards sind identisch. Offenbar ist der Sync-Zustand verloren gegangen. VCard wird in den SZ eingefügt. ");						
						conflictResolved=true;
						
					}else{
						// Sync-Zustand ist verloren gegangen und Beide Seiten unterscheiden sich
						
						// Beide Operationen verwerfen. Jedoch danach in Sync Zustand einfügen.					
						//opList.remove(androidOp);
						removeOp(opList, androidOp);
						
						// statt sie zu löschen wird die AndroidOp mit der onlyZustandInsert -Op überschrieben.
						int ind = serverOps.getOpList().indexOf(serverOp);
						serverOps.getOpList().set(ind, new OnlyZustandInsert(serverOp.OpVCard));
						
						if(dbg)kDebug.println("Insert Insert Konflikt, beide VCards sind identisch. Offenbar ist der Sync-Zustand verloren gegangen. VCard wird in den SZ eingefügt. ");
						
						mNotification.addConflict("Die Unterschiede zwischen '" + androidOp.getGevilCalendar().getSummary() + "' und '" + serverOp.getGevilCalendar().getSummary() + "' konnten nicht aufgelöst werden. Bitte gleichen sie die Events manuell aneinander an.", androidOp.getGevilCalendar());
						conflictResolved=true;

					}
					
					
				}else if(androidOp instanceof Update && serverOp instanceof Update){
					/**
					 *  update x update
					 */
					//mNotification.addConflict("Update Update KOnflikt@Calendar: android Operation wird gelöscht.", androidOp.getGevilCalendar());
					//if(dbg)kDebug.println("Konfliklösung: lösche androidOp.");
					//removeOp(opList, androidOp);
					
					GevilVCalendar joinedCard = joindUpdateVCards(androidOp.getGevilCalendar(), serverOp.getGevilCalendar(), dbg);
										
					//if(dbg)
						System.out.println("Konfliktlösung: " + "Join ergab:\n" + joinedCard.toString() + "\n.");
					((Update)androidOp).changeUpdateCard(joinedCard);
					((Update)serverOp).changeUpdateCard(joinedCard);
					conflictResolved = true;	
										
					
					
				}else if(androidOp instanceof Delete && serverOp instanceof Delete){
					/** 
					 * delete x delete
					 */
					
					mNotification.addConflict("Der Kalendereintrag '" + androidOp.getGevilCalendar().getSummary() + "' wurde auf dem Server und dem Handy gelöscht. Er wird nun auch aus Gevil-Sync entfernt.", androidOp.getGevilCalendar());
					
					if(dbg)kDebug.println("Konfliktlösung: " + "beide deletes werden verworfen. Kontakt wird aus dem Zustand entfernt.");
					//opList.remove(androidOp);
					//serverOps.opList.remove(serverOp);
					removeOp(opList, androidOp);
					removeOp(serverOps.opList, serverOp);
					
					mSyncZustand.SQLdelete(androidOp.getGevilCalendar());	
					conflictResolved = true;	
				}
				
				
				CalendarOp swapServerOp = serverOp;
				CalendarOp swapAndroidOp = androidOp;
				
				// 2*3 Fälle liegen nich auf der Diagonale der (insert,update,delet)*(insert,update,delte) Matrix.
				for(int i=0;i<=1;i++){
					if(swapAndroidOp instanceof Insert){
						if(swapServerOp instanceof Update){
							/**
							 * insert x update
							 */
							if(dbg)kDebug.println("Exception reason: In konflikt stehende Operationen: update x insert");
							throw new ArrayIndexOutOfBoundsException();
						}else if(swapServerOp instanceof Delete){
							/**
							 *  insert x delete
							 */								
							removeOp(this.chooseServerOps(i, serverOps, this), swapServerOp);							
							conflictResolved = true;
							
							// Ein Delete wird erkannt, wenn der Eintrag zwar im Zustand, nicht jedoch im Server
							// ( Handy) existiert. Ein Insert wird erkannt, wenn der Eintrag im Zustand NICHT existiert.
							throw new ArrayIndexOutOfBoundsException();
						}
					}else if(swapAndroidOp instanceof Update){
						if(swapServerOp instanceof Delete){
							/**
							 *  update x delete
							 */
							if(dbg)kDebug.println("Konfliktlösung: " + "mache aus dem Update ein Inser und verwerfe das Delte.");
							mNotification.addConflict("Der Kalendereintrag '" + swapAndroidOp.getGevilCalendar().getSummary() + "' wurde gelöscht. Auf der anderen Seite wurde er jedoch zu '" + swapServerOp.getGevilCalendar().getSummary() + "' abgeändert. Der Löschvorgang wird verworfen.", swapServerOp.getGevilCalendar());
							
							// das Update in ein Insert verwandeln							
							ArrayList<CalendarOp> androidOpList = this.chooseAndroidOps(i, serverOps, this);
							int indAndOp = androidOpList.indexOf(swapAndroidOp);						
							this.changeUpdateSQLupdate_Insert(indAndOp, androidOpList);							
														
							// Delete verwerfen.
							removeOp(this.chooseServerOps(i, serverOps, this), swapServerOp);
							conflictResolved = true;
						}									
					}					

					// androidOp mit serverOp vertauschen, um Symetrische Fälle zu prüfen.					
					CalendarOp temp = swapAndroidOp;
					swapAndroidOp=swapServerOp;
					swapServerOp=temp;
				}
				
				if(!conflictResolved){					
					kDebug.println("==============================");
					kDebug.println("ConflictNOTresolved!!");					
					kDebug.println("==============================");
					
					if(androidOp instanceof Delete){
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
	

	
	
	/**
	 * Im Falle eines Update Update Konflikts, findet die Methode alle feingranularen Operationen
	 * und erstellt dem entsprechend eine neue VCard.
	 * @param andriod
	 * @param server
	 * @return
	 */
	private GevilVCalendar joindUpdateVCards(GevilVCalendar andriod, GevilVCalendar server, boolean dbg){
		//
		// wir haben die VCard vom Handy und vom Server. Jetzt holen wir uns zum Vergleich noch
		// die aus dem Sync-Zustand.
		//
		GevilVCalendar zustand = mSyncZustand.uidHash.get(andriod.getUid());		
		
		
		
		//
		// Vor- und Nachname der zusammengesetzten VCard, ret, zusammenführen.
		//
		GevilVCalendar ret = new GevilVCalendar(mMap);
		
		ret.setUid(andriod.getUid());
		ret.setEtag(andriod.getEtag());
		String serverPath = zustand.getServerPath();
		if(serverPath==null) serverPath= andriod.getServerPath();
		if(serverPath==null) serverPath= server.getServerPath();
		ret.setServerPath(serverPath);
		
		int androidId = zustand.getAndroidId();
		if(androidId==-1) androidId = andriod.getAndroidId();
		if(androidId==-1) androidId = server.getAndroidId();
		ret.setAndroiId(androidId);
			

		
		
		String aStart = andriod.getPropValue("DTSTART");
		String aEnd = andriod.getPropValue("DTEND");		
		String sStart = server.getPropValue("DTSTART");
		String sEnd = server.getPropValue("DTEND");
		String zStart = zustand.getPropValue("DTSTART");
		String zEnd = zustand.getPropValue("DTEND");
		
		DtStart aDTstart = (DtStart) andriod.getProp(("DTSTART"));
		DtEnd aDTend = (DtEnd) andriod.getProp(("DTEND"));
		DtStart sDTstart = (DtStart) server.getProp(("DTSTART"));
		DtEnd sDTend = (DtEnd) server.getProp(("DTEND"));
		
		boolean androidUpdate = !aStart.equals(zStart) || ! aEnd.equals(zEnd);
		boolean serverUpdate = !sStart.equals(zStart) || ! sEnd.equals(zEnd);		
		
		
		if(serverUpdate && androidUpdate){
			//
			// Konflikt: Unterschied auf beiden Seiten.
			//
			if(OptionsLoader.getInstance().getCalendarPhoneWins(mContext, mUsername)){
				// Phone Wins
				//ret.setStartEnd(aStart, aEnd);
				ret.setStartEnd(aDTstart, aDTend);				
				mNotification.addConflict("Die Zeitänderung von '" + zustand.getSummary() + "' konnte nicht Konfliktfrei ermittelt werden. PhoneWins wurde angewandt.", ret);
			}else{
				// Server Wins
				//ret.setStartEnd(sStart, sEnd);
				ret.setStartEnd(sDTstart, sDTend);				
				mNotification.addConflict("Die Zeitänderung von '" + zustand.getSummary() + "' konnte nicht Konfliktfrei ermittelt werden. ServerWins wurde angewandt.", ret);			}
		}else if(serverUpdate){
			// Eventzeit wurde auf dem Server verändert.
			//ret.setStartEnd(sStart, sEnd);
			ret.setStartEnd(sDTstart, sDTend);
			//mNotification.addConflict("Kein Konflikt, nur Server Update: '" + zustand.getSummary() + "' Server Update wurde übernommen.", ret);
		}else if(androidUpdate){
			// Eventzeit wurde auf dem Handy verändert.
			//ret.setStartEnd(aStart, aEnd);
			ret.setStartEnd(aDTstart, aDTend);
			//mNotification.addConflict("Kein Konflikt, nur Android Update: '" + zustand.getSummary() + "' Handy Update wurde übernommen.", ret);
		}else if(!serverUpdate && !androidUpdate){
			// beide gleich.
			//ret.setStartEnd(aStart, aEnd);
			//mNotification.addConflict("Zeiten sind unverändert: '" + zustand.getSummary() + "' Handy Update wurde übernommen.", ret);
			ret.setStartEnd(aDTstart, aDTend);
		}else{
			throw new ArrayIndexOutOfBoundsException();
		}	
	
		String rDescription = solveConflict(andriod, zustand, server,"DESCRIPTION");
		//if(rDescription==null)throw new ArrayIndexOutOfBoundsException();
		ret.setDescription(rDescription);
		
		String rSummary = solveConflict(andriod, zustand, server, "SUMMARY");
		//if(rSummary==null)throw new ArrayIndexOutOfBoundsException();
		ret.setSummary(rSummary);
		
		String rLocation = solveConflict(andriod, zustand, server, "LOCATION");
		//if(rLocation==null)throw new ArrayIndexOutOfBoundsException();
		ret.setLocation(rLocation);
		
		String rRrule = solveRRConflict(andriod, zustand, server, "RRULE");
		//if(rRrule==null)throw new ArrayIndexOutOfBoundsException();
		try {
			ret.setRrule(rRrule);
		} catch (ParseException e) {
			System.out.println("Exrrule: " + rRrule);
			e.printStackTrace();
		}catch (IllegalArgumentException e) {
			System.out.println("Exrrule: " + rRrule);
			e.printStackTrace();
		}
	
		
		if(ret.getPropValue("DTSTART")==null)throw new ArrayIndexOutOfBoundsException();
		if(ret.getPropValue("DTEND")==null)throw new ArrayIndexOutOfBoundsException();
		if(ret.getPropValue("DESCRIPTION")==null && ret.getPropValue("SUMMARY")==null)throw new ArrayIndexOutOfBoundsException();		
		return ret;
	}
	
	


	private boolean compareWithNull(String a, String b){
		if(a==null && b==null){
			return true;
		}else if(a==null || b==null){
			return false;
		}else{
			return a.trim().equals(b.trim());
		}
		
	}
	

	/**
	 * Konfliktlösung für z.B. DESCRIPTION, SUMMARY, ?RRULE
	 * @param androidCard
	 * @param zustandCard
	 * @param serverCard
	 * @param propType
	 * @return
	 */
	private String solveConflict(GevilVCalendar androidCard, GevilVCalendar zustandCard, GevilVCalendar serverCard, String propType) {
		String androidPropValue = androidCard.getPropValue(propType);
		String zustandPropValue = zustandCard.getPropValue(propType);
		String serverPropValue = serverCard.getPropValue(propType);		
		
		/*
		if(androidPropValue==null || zustandPropValue==null || serverPropValue==null){
			System.out.println("type: " + propType);
			if(androidPropValue==null)System.out.println("androidNULL");
			if(zustandPropValue==null)System.out.println("zustandNULL");
			if(serverPropValue==null)System.out.println("serverNULL");
			
			throw new ArrayIndexOutOfBoundsException();
		}
		*/
		
		Boolean androidUpdate = !compareWithNull(androidPropValue, zustandPropValue);
		Boolean serverUpdate = !compareWithNull(serverPropValue, zustandPropValue);
		
		if(androidUpdate && serverUpdate){
			// Konfliktfall
			if(OptionsLoader.getInstance().getCalendarPhoneWins(mContext, mUsername)){
				// Phone Wins
				mNotification.addConflict("Die " + propType + " von '" + zustandCard.getSummary() + "' konnte nicht Konfliktfrei ermittelt werden. Phone Wins wurde angewandt.", androidCard);			
				return androidPropValue;
			}else{
				// Server Wins
				mNotification.addConflict("Die " + propType + " von '" + zustandCard.getSummary() + "' konnte nicht Konfliktfrei ermittelt werden. Server Wins wurde angewandt.", androidCard);
				return serverPropValue;
			}
		}else if(androidUpdate){
			//mNotification.addConflict("Die " + propType + " von '" + zustandCard.getSummary() + "' nur Update. Der android Wert wurde übernommen.", androidCard);
			return androidPropValue;
			
		}else if(serverUpdate){
			//mNotification.addConflict("Die " + propType + " von '" + zustandCard.getSummary() + "' nur Update. Der Server Wert wurde übernommen.", androidCard);
			return serverPropValue;
			
		}else{
			// beide gleich (Transitivität), daher egal was zurück gegeben wird.
			//mNotification.addConflict("Die " + propType + " von '" + zustandCard.getSummary() + "' nur Update. Der Server Wert wurde übernommen.", androidCard);
			return androidPropValue;
		}		
		
	
	}
	
	
	
	private String solveRRConflict(GevilVCalendar androidCard, GevilVCalendar zustandCard, GevilVCalendar serverCard, String propType) {
		boolean dbg=true;
		String androidPropValue = androidCard.getPropValue(propType);
		//String zustandPropValue = zustandCard.getPropValue(propType);
		String serverPropValue = serverCard.getPropValue(propType);		
		

		Boolean androidUpdate = !androidCard.rRulesEqual(androidCard, zustandCard);
		Boolean serverUpdate = !androidCard.rRulesEqual(serverCard, zustandCard);
		
		//Boolean androidUpdate = !rRuleCompare(androidPropValue, zustandPropValue);
		//Boolean serverUpdate = !rRuleCompare(serverPropValue, zustandPropValue);
		
		if(androidUpdate && serverUpdate){
			// Konfliktfall
			if(OptionsLoader.getInstance().getCalendarPhoneWins(mContext, mUsername)){
				// Phone Wins
				if(dbg)System.out.println("solveRRconflict: true true; Phone Wins.");
				mNotification.addConflict("Die " + propType + " von '" + zustandCard.getSummary() + "' konnte nicht Konfliktfrei ermittelt werden. Phone Wins wurde angewandt.", androidCard);			
				return androidPropValue;
			}else{
				// Server Wins
				if(dbg)System.out.println("solveRRconflict: true true; Server Wins.");
				mNotification.addConflict("Die " + propType + " von '" + zustandCard.getSummary() + "' konnte nicht Konfliktfrei ermittelt werden. Server Wins wurde angewandt.", androidCard);
				return serverPropValue;
			}
		}else if(androidUpdate){
			if(dbg)System.out.println("solveRRconflict: true false.");
			//mNotification.addConflict("Die " + propType + " von '" + zustandCard.getSummary() + "' nur Update. Der android Wert wurde übernommen.", androidCard);
			return androidPropValue;
			
		}else if(serverUpdate){
			if(dbg)System.out.println("solveRRconflict: true false.");
			//mNotification.addConflict("Die " + propType + " von '" + zustandCard.getSummary() + "' nur Update. Der Server Wert wurde übernommen.", androidCard);
			return serverPropValue;
			
		}else{
			// beide gleich (Transitivität), daher egal was zurück gegeben wird.
			//mNotification.addConflict("Die " + propType + " von '" + zustandCard.getSummary() + "' Server und Handy RRule identisch. Der Android Wert wurde übernommen.", androidCard);
			System.out.println("solveRRconflict: keine Änderung der RRule.");
			return androidPropValue;
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
	private ArrayList<CalendarOp> chooseServerOps(int i, CalendarOperations serverO, CalendarOperations androidO){
		if(i==0){
			return serverO.getOpList();
		}else if(i==1){
			return androidO.getOpList();
		}else{
			throw new IndexOutOfBoundsException();
		}
	}
	private ArrayList<CalendarOp> chooseAndroidOps(int i, CalendarOperations serverO, CalendarOperations androidO){
		if(i==1){
			return serverO.getOpList();
		}else if(i==0){
			return androidO.getOpList();
		}else{
			throw new IndexOutOfBoundsException();
		}
	}
	

	
	
	
	
	
	/**
	 * Fügt alle in a und b enthaltenen (z.B. Emailadressen) in die return VCard ein
	 * @param addTo Zu dieser VCard werden die Werte hinzugefügt. Sie kann z.B. schon einen Namen enthalten.
	 * @param a
	 * @param b
	 * @param phoneOrEmailId mMap.mailType(), mMap.phoneType()
	 * @return
	 */
	/*
	private GevilVCard addToVCard(GevilCalendar addTo, GevilCalendar a, GevilVCard b, Id phoneOrEmailId, int phoneOrEmail){
		PropertyByValueSearcher aMailByValue = a.getHash().propertiesByValue(phoneOrEmailId);
		PropertyByValueSearcher bMailByValue = b.getHash().propertiesByValue(phoneOrEmailId);
		 
		a.initMmap(mMap); b.initMmap(mMap);
		
		Property eineMailVonA = aMailByValue.getUninsertedPropertyOnce();
		 
		while(eineMailVonA != null){		 
			// Es ist egal, ob b diese Adresse auch hat. Sie wird aber zur Duplikatvermeidung in b markiert.
			 bMailByValue.getPropertyByValueOnce(eineMailVonA.getValue());			 
			 int emailAdressTyp = a.propertyToAndroidType(eineMailVonA,phoneOrEmail , true);
			 addTo.addEmail(eineMailVonA.getValue(), emailAdressTyp);			 
			 
			 eineMailVonA = aMailByValue.getUninsertedPropertyOnce();
		}
		// A hat jetzt keine weiteren Adressen mehr.
		Property eineMailVonB = bMailByValue.getUninsertedPropertyOnce();
		while(eineMailVonB != null){
			 int emailAdressTyp = b.propertyToAndroidType(eineMailVonB, phoneOrEmail, true);
			 addTo.addEmail(eineMailVonB.getValue(), emailAdressTyp);
			 
			 eineMailVonB = bMailByValue.getUninsertedPropertyOnce();
		}
		
		return addTo;
	}
	*/
	

	
	/*
	 * Ab jetzt: insert update dele Klassen
	 *
	 */
	
	public abstract class CalendarOp{
		GevilVCalendar OpVCard;		
		
		CalendarOp(GevilVCalendar card){
			OpVCard = card;					
		}		
		
		/*
		public GevilCalendar getGevilVCard(){
			return OpVCard;
		}
		*/
		public GevilVCalendar getGevilCalendar(){
			return OpVCard;
		}
		
		abstract void execute(boolean android);
		
		public abstract String operationType();
	}
	
	
	public void addInsert(GevilVCalendar card){
		this.addOp(new Insert(card));
	}
	
	/** 
	 * Einfügen eines neuen Kontaktes
	 * @author Martin
	 * 
	 */
	public class Insert extends CalendarOp{

		Insert(GevilVCalendar card) {
			super(card);
			// TODO Auto-generated constructor stub
		}

		@Override
		void execute(boolean android) {
			try {
				if(mSyncZustand.SQLexists(OpVCard)){
					throw new ArrayIndexOutOfBoundsException();
				}
				
				
				
				if(android){
					kDebug.println("@execute: Android insert von " + OpVCard.getSummary() );
					iudc.insert(OpVCard, mUsername);
					
				}else{
					kDebug.println("@execute: insert to Server " + OpVCard.getSummary());				
					
					WebDAV wd = new WebDAV(mUsername, mPassword, mMap);
				
						String calendarURL = OptionsLoader.getInstance().getCalendarServerUrl(mContext, mUsername);
						wd.PutVCalendar(OpVCard, calendarURL + "/" + OpVCard.getUid() + ".vcf");
					
				}
				
				if(!noExecSQLInsert){
					
					// Android UND Server inserts werden auch im Sync Zustand ausgeführt.
					mSyncZustand.SQLinsert(OpVCard);
				}
				
			}catch (Exception e) {
					kDebug.printStackTrace("Exception beim upload von: "+ OpVCard.getPropValue("SUMMARY"), e);
			}
		}
		
		private boolean noExecSQLInsert = false;
		/**
		 * Bei einem Update Delete Konflikt wird das Delete verworfen und das Update in eine Insert verwandelt.
		 * In diesem Fall ist der Eintrag jedoch schon im Sync-Zustand vorhanden, und darf nicht noch Einmal eingefügt werden.
		 * Deshalb wird das Insert dort dann unterdrückt.
		 */
		public void dontExecSQL(){
			noExecSQLInsert=true;
		}

		@Override
		public String operationType() {
			return "Insert";
		}
		
	}
	
	public void addonlyZustandInsert(GevilVCalendar card){
		this.addOp(new OnlyZustandInsert(card));
	}
	
	public class OnlyZustandInsert extends CalendarOp{

		OnlyZustandInsert(GevilVCalendar card) {
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
			*/
			
			
				
			// Android UND Server inserts werden auch im Sync Zustand ausgeführt.
			if(mSyncZustand.SQLexists(OpVCard)){
				mSyncZustand.SQLupdate(OpVCard);
			}else{
				mSyncZustand.SQLinsert(OpVCard);
			}
			
			
		}

		@Override
		public String operationType() {
			return "onlyZustandInsert";
		}
		
	}
	
	
	public void addUpdate(GevilVCalendar card){
		this.addOp(new Update(card));
	}
	
	/**
	 * Aktualisieren eines bestehenden Kontaktes
	 * @author Martin
	 *
	 */
	public class Update extends CalendarOp{
		
		Update(GevilVCalendar card){
			super(card);
			/*
			if(card.getAndroidId()==-1){
				// Ein Update ist nur möglich, wenn die Android _id bekannt ist.
				kDebug.println(card.toString());
				throw new IndexOutOfBoundsException();
			}
			*/
		}
		
		public void changeUpdateCard(GevilVCalendar nCard){
			OpVCard = nCard;
			/*if(nCard.getAndroidId()==-1){
				// Ein Update ist nur möglich, wenn die Android _id bekannt ist.
				kDebug.println(nCard.toString());
				throw new IndexOutOfBoundsException();
			}*/
		}
		
		public void changeUpdateToInsert(){
			
		}

		@Override
		void execute(boolean android) {
			try {
				
				if(OpVCard instanceof AktuellServerGevilVCalendar){
					throw new ArrayIndexOutOfBoundsException();
				}
					
					
				if(android){
					kDebug.println("@execute: Android update von " + OpVCard.getPropValue("SUMMARY") );
					
					
					if(OpVCard.getAndroidId()==-1){
						GevilVCalendar zCal = mSyncZustand.uidHash.get(OpVCard.getUid());
						OpVCard.setAndroiId(zCal.getAndroidId());
						if(OpVCard.getAndroidId()==-1)throw new ArrayIndexOutOfBoundsException();
					}
					iudc.update(OpVCard, mUsername);
					
					
				}else{
					String calendarURL = OptionsLoader.getInstance().getCalendarServerUrl(mContext, mUsername);
					kDebug.println("@execute: Server update von " + OpVCard.getSummary() + ", verwendete URL: " +calendarURL + "/" + OpVCard.getUid());
					kDebug.println("@exec " + OpVCard.toString());
	
					WebDAV wd = new WebDAV(mUsername, mPassword, mMap);
						
					String sURL = calendarURL + "/" + OpVCard.getUid();				
					wd.PutVCalendar(OpVCard, sURL);// + ".vcf");
					
				}
				
				// Android UND Server Updates werden auch im Sync Zustand ausgeführt.
				mSyncZustand.SQLupdate(OpVCard);
				
			
			} catch (Exception e) {
					//kDebug.println("Exception beim upload von  ...  " + Constants.CONTACTS_SERVER_URL);
					
					kDebug.printStackTrace("wd.put: " + OpVCard.getPropValue("SUMMARY"), e);
					
			}
		}

		@Override
		public String operationType() {
			return "Update";
		}
	}
	
	
	public void addDelete(GevilVCalendar card){
		this.addOp(new Delete(card));
	}
	
	/**
	 * Löschen eines Kontaktes
	 * @author Martin
	 *
	 */
	public class Delete extends CalendarOp{

		Delete(GevilVCalendar card) {
			super(card);
			/*
			if(card.getAndroidId()==-1){
				// Ein Delete ist nur möglich, wenn die Android _id bekannt ist.
				kDebug.println("Exception Card uid: " + card.getUid() + " " + card.getN()[0]);
				//throw new IndexOutOfBoundsException();
			}
			*/
		}

		@Override
		void execute(boolean android) {
			if(android){		
				kDebug.println("@execute: Android delete von " + OpVCard.getPropValue("SUMMARY") );
				iudc.delete(OpVCard, mUsername);
				
			}else{
				String calendarURL = OptionsLoader.getInstance().getCalendarServerUrl(mContext, mUsername);
				String delURL = calendarURL + "/" + OpVCard.getUid();
				
				kDebug.println("@execute: Server delete von " + OpVCard.getSummary() + "  url: " +  delURL);
				
				WebDAV wd = new WebDAV(mUsername, mPassword, mMap);
				try {
					wd.delteVCard(delURL);
				} catch (Exception e) {
					kDebug.printStackTrace("Exception beim Delete von " + delURL, e);					
				}
				
			}
			
			// Android UND Server Deletes werden auch im Sync Zustand ausgeführt.
			mSyncZustand.SQLdelete(OpVCard);			
		}

		@Override
		public String operationType() {
			return "Delete";
		}
		
	}
	
	
	/** 
	 * Im Falle eines Delete Update Konfliktes wird der Kontakt im Sync-Zustand
	 * upgedatet, auf einer seite eingefügt, und das Delete verworfen.
	 * @author Martin
	 * 
	 */
	public class SQLupdate_Insert extends CalendarOp{

		SQLupdate_Insert(GevilVCalendar card) {
			super(card);
			// TODO Auto-generated constructor stub
		}

		@Override
		void execute(boolean android) {
			try {
				
				if(android){
					//
					// ein Insert unter Android
					//
					kDebug.println("@execute: Android insert von " + OpVCard.getSummary() );
					iudc.insert(OpVCard, mUsername);
					
				}else{
					//
					// Insert im Server
					//
					kDebug.println("@execute: insert to Server " + OpVCard.getSummary());				
					
					WebDAV wd = new WebDAV(mUsername, mPassword, mMap);
				
						String calendarURL = OptionsLoader.getInstance().getCalendarServerUrl(mContext, mUsername);
						wd.PutVCalendar(OpVCard, calendarURL + "/" + OpVCard.getUid() + ".vcf");					
				}
				
				
				if(mSyncZustand.SQLexists(OpVCard)){	
					mSyncZustand.SQLupdate(OpVCard);
				}else{
					mSyncZustand.SQLinsert(OpVCard);
				}
			
				
			}catch (Exception e) {
					kDebug.printStackTrace("Exception beim upload von: "+ OpVCard.getPropValue("SUMMARY"), e);
			}
		}

		@Override
		public String operationType() {
			return "SQLupdate_Insert";
		}
		
	}
	
	
	public class RemovedOp extends CalendarOp{

		RemovedOp(GevilVCalendar card) {
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
