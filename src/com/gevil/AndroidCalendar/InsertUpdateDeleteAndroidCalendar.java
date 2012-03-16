package com.gevil.AndroidCalendar;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;

import syncZustand.TypeBiMap;


import com.gevil.calSyncZustand.GevilVCalendar;
import com.gevil.syncadapter.Constants;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class InsertUpdateDeleteAndroidCalendar {
	Context mContext;
	TypeBiMap mMap;
	ContentResolver cr;
	public com.gevil.AndroidCalendar.Konsolenausgabe debug;
	
	
    public InsertUpdateDeleteAndroidCalendar(Context Context, TypeBiMap mMap) {		
		this.mContext = Context;
		this.mMap = mMap;
		debug = new Konsolenausgabe();		
		cr = mContext.getContentResolver();
	}


	public void setKonsolenersatz(Konsolenausgabe ausgabe) {
		debug = ausgabe;		
	} 

    
    

	/** 
	* Probiert alle angegebenen URIs durch. Sobald die Querry unter einer URI Erfolgreich ist
    * (Cursor != null), wird der Cursor zurück gegeben.
    * Normalerweise sollte es mit der ersten URI klappen.
    * 
    * 4Events deshalb, weil bei der SELECTION nicht "_id" und "displayname" selektiert werden, diese Spalten
    * kommen bei der Tabelle mit den verschiedenen Kalendern, nicht aber bei den Einträgen in den Kalendern vor.
    *
    * @param debug in diesen String kann etwas zum Debuggen unter einem realen Smartphone geschrieben werden.
	 * @return 
    */
    public void insert(GevilVCalendar cal, String accountUserName){	
    	// sollte sich, auf einem anderen Gerät, die URI zum Kalenderzugriff ändern, wird die unten
		// stehende Liste aller URIs durchprobiert.	
  	
    	String[] possURIs = {//"content://com.android.calendar/calendars",    			
    			"content://com.android.calendar/events",  // ging auf dem HTC Desire HD
    			"content://com.android.calendar",
    			"content://com.android.calendar",
    			"content://calendar/events",
    			"content://calendar/calendars",
    			"content://com.android.calendar",
    			"content://com.android.calendar/calendar",
    			"content://com.android.calender/calenders",
    			"content://com.android.calender/content",
    			"content://com.android.calender/content",
    			"content://calender/events/content","content://calender/calenders/content",
    			"content://com.android.calender/content",
    			"content://com.android.calender/content",
    			"content://com.android.calender",
    			"content://com.android.calender",
    			"content://calender/events", "content://calender/calenders",
    			"content://com.android.calender", 
    			"content://com.android.calender",
    			"com.htc.calendar",
    			"com.htc.calender",
    			"content://com.htc.calendar",
    			"content://com.htc.calender",
    			"content://com.htc.calenders"};    	
    	
    	
    	//for(int i = 0; i < possURIs.length;i++){
    		try{
    			
    			int calId = findOrCreateAccountCalendar(accountUserName);
    			
    			if(calId <0) throw new ArrayIndexOutOfBoundsException();
    			if(cal==null) debug.println("(!)cal ist null.");
    			if(cal.getContentValues(accountUserName, calId)==null) debug.println("(!)calGetCV. ist null.");
    			if(cal.getUid()==null)throw new ArrayIndexOutOfBoundsException();
    			//if(Uri.parse(possURIs[i])==null) debug.println("ur is null.");    
    		
    			debug.println("gefundene calId: " + calId);
    			//debug.println("verwendete URI: content://com.android.calendar/events");
    			
    			//cr.insert(Uri.parse(possURIs[i]), cal.getContentValues(accountUserName, calId));
    			Uri newUri = cr.insert(Uri.parse("content://com.android.calendar/events"), cal.getContentValues(accountUserName, calId));
    			    			
    			String y = newUri.toString();    			
    			Integer androidIdOfInsertedCalevent = Integer.valueOf(y.substring(y.lastIndexOf("/")+1));
    			
    			if(cal.getAndroidId()==-1){
    				System.out.println("heur ExcecuteInsert neue Id: " + androidIdOfInsertedCalevent);
    				cal.setAndroiId(androidIdOfInsertedCalevent);
    			}else if(cal.getAndroidId() != androidIdOfInsertedCalevent){
    				throw new ArrayIndexOutOfBoundsException();
    			}
    			
    			
    			//    			
    			// ggf jetzt noch den (die) Reminder einfügen.
    			//
    			this.inserReminders(cal);    			
    			
    			
    			return;
    			/*
	    		if(url!=null){
	    			debug.println("folgende Kalender URI wurde gefunden: " + possURIs[i]);
	    			return;
	    			//return c;	
	    			// return possURIs[i];
	    			// mögliche Lösung: nur aktive Kalender akzeptieren.	    			
	    		}
	    		*/
	    		
    		}catch(Exception e){
    			debug.printStackTrace("insert(1) ", e);
    		}
    	//} 
    	
    	
    	
    	/*
    	Date dt = new Date();
    	int id = findOrCreateAccountCalendar(accountUserName);    	
    	this.origAddToCalendar(id, "erster Eintrag in meinem kalender!", dt.getTime(), dt.getTime() + 1000);
    	*/
    	
    	return;
    }    
    
    private void inserReminders(GevilVCalendar cal) {
		ComponentList components = cal.getCalendar().getComponents();
		
		int i = 0;
		while(i<components.size()){
			Component a = (Component)components.get(i);
			if(a.getProperties("TRIGGER").size() > 0){
				// es handelt sich anscheinend um einen Alarm-Eintrag mit Trigger.
				
				// 01-10 18:10:43.864: I/System.out(3581): TRIGGER;VALUE=DATE-TIME:20120106T172000Z
				String triggerStartZeitpunkt = a.getProperties("TRIGGER").get(0).toString();
				int s = triggerStartZeitpunkt.lastIndexOf(":");
				triggerStartZeitpunkt=triggerStartZeitpunkt.substring(s+1);
				
				System.out.println(triggerStartZeitpunkt);
				
				Integer jahr = Integer.valueOf(triggerStartZeitpunkt.substring(0,4));
				Integer monat = Integer.valueOf(triggerStartZeitpunkt.substring(4,6));
				Integer tag = Integer.valueOf(triggerStartZeitpunkt.substring(6,8));
				Integer stunde = Integer.valueOf(triggerStartZeitpunkt.substring(9,11));
				Integer minute = Integer.valueOf(triggerStartZeitpunkt.substring(11,13));
				Integer sekunde = Integer.valueOf(triggerStartZeitpunkt.substring(13,15));				
				
				Date d = new Date(jahr-1900, monat-1, tag, stunde, minute , sekunde);
				long triggerTimeInMillis = d.getTime();
				long dtstartInMillis = cal.getTimeProp("DTSTART").getTime();
				long zeit = dtstartInMillis - triggerTimeInMillis;
				long zeitInMinuten = zeit / 60 / 1000;
				
				
				//if(zeitInMinuten > 10800){
				//	System.out.println("Ecx: " + tag + " " + monat + " " + jahr  + " : " + stunde  + " " +  minute  + " " + sekunde);
				//	System.out.println("dtstartInMillis " + dtstartInMillis +   " triggerTimeInMillis " + triggerTimeInMillis + " diff: " + zeit + "="+ zeitInMinuten + " Minuten");
					
				//	throw new ArrayIndexOutOfBoundsException();
				//}
				
		
				
				
				ContentValues cv = new ContentValues();
				cv.put("event_id", String.valueOf(cal.getAndroidId()));		
				cv.put("minutes", zeitInMinuten);
				cv.put("method", "1");
				
				cr.insert(Uri.parse("content://com.android.calendar/reminders"), cv);
			}
			
			i++;
		}
		
	}


	public void delete(GevilVCalendar cal, String accountUserName){
    	//cr.delete(Uri.parse("content://com.android.calendar/events"), "_sync_account_type=?", new String[]{Constants.ACCOUNT_TYPE_TEST});
    	//int deletedRows = cr.delete(Uri.parse("content://com.android.calendar/events"), "_sync_id=?", new String[]{cal.getUid() });
    	
    	int acId = this.findOrCreateAccountCalendar(accountUserName);
    	
    	if(cal.getAndroidId()==-1)throw new ArrayIndexOutOfBoundsException();
    	
    	int deletedRows = cr.delete(Uri.parse("content://com.android.calendar/events"), 
        		"_id=?",
        		new String[]{String.valueOf(cal.getAndroidId())});
    	
    	
    	if(deletedRows<1)throw new ArrayIndexOutOfBoundsException();
    	
    	debug.println("gelöschteZeilen: " + deletedRows);
    }
    
    
    /**
     * aktualisieren eines Kalendereintrages
     * @param cas aktueller Kalendereintrag als GevilVCalendar
     * @param accountUserName Username des Kalenders
     */
    public void update(GevilVCalendar cas, String accountUserName){
    
    	
    	if(cas.getUid()==null)throw new ArrayIndexOutOfBoundsException();
    	
		int accountCalId= findOrCreateAccountCalendar(accountUserName);									
		Uri x = Uri.parse("content://com.android.calendar/events");
		
		// TODO _id auslesen, bzw. in VCalendar speichern.
		
		int androidId = cas.getAndroidId();
		
		if(androidId == -1)throw new ArrayIndexOutOfBoundsException();  // TODO AndroidId aus SyncZustand via UID erhalten.
		
		x = ContentUris.withAppendedId(x, androidId);


		
		
		//int updatedRows = cr.update(x, cas.getContentValues(accountUserName, accountId), "_sync_id=?", new String[]{cas.getUid()});
		//int updatedRows = cr.update(x,cas.getContentValues(accountUserName, accountCalId), "calendar_id=? and _sync_id=?", new String[]{String.valueOf(accountCalId), cas.getUid()});
		int updatedRows = cr.update(x, cas.getContentValues(accountUserName, accountCalId), null, null);
		
		
		debug.println("vomUpdateAktualisiertZeilen: " + updatedRows);
    }
    
    
    /* 10.11.2011
    private void OLDinsert(konsolenausgabe debug, ContentValues cv, GevilVCalendar cal){    	
    	// sollte sich, auf einem anderen Gerät, die URI zum Kalenderzugriff ändern, wird die unten
		// stehende Liste aller URIs durchprobiert.	
  	
    	final ContentResolver cr = mContext.getContentResolver();
    	
    	String[] possURIs = {//"content://com.android.calendar/calendars",    			
    			"content://com.android.calendar/events",  // ging auf dem HTC Desire HD
    			"content://com.android.calendar",
    			"content://com.android.calendar",
    			"content://calendar/events",
    			"content://calendar/calendars",
    			"content://com.android.calendar",
    			"content://com.android.calendar/calendar",
    			"content://com.android.calender/calenders",
    			"content://com.android.calender/content",
    			"content://com.android.calender/content",
    			"content://calender/events/content","content://calender/calenders/content",
    			"content://com.android.calender/content",
    			"content://com.android.calender/content",
    			"content://com.android.calender",
    			"content://com.android.calender",
    			"content://calender/events", "content://calender/calenders",
    			"content://com.android.calender", 
    			"content://com.android.calender",
    			"com.htc.calendar",
    			"com.htc.calender",
    			"content://com.htc.calendar",
    			"content://com.htc.calender",
    			"content://com.htc.calenders"};    	
    	
    	for(int i = 0; i < possURIs.length;i++){
    		try{
    			// 4Events deshalb, weil bei der SELECTION nicht "_id" und "displayname" selektiert wird.
    			// Cursor c = cr.query(Uri.parse(possURIs[i]), null, null, null, null);   

    			//Cursor c = cr.query(Uri.parse(possURIs[i]), Projektion, Selection, SelArgs, null);
    			Uri url = cr.insert(Uri.parse(possURIs[i]), cal.getContentValues());    			
    			
	    		if(url!=null){
	    			debug.ka += "\nfolgende Kalender URI wurde gefunden: " + possURIs[i];
	    			return;
	    			//return c;	
	    			// return possURIs[i];
	    			// mögliche Lösung: nur aktive Kalender akzeptieren.	    			
	    		}
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    	}     
    	
    	throw new ArrayIndexOutOfBoundsException();
       // return null;
    }    
	*/
	
	
	
	
    private HashMap<String, Integer> calIdHash = new HashMap<String, Integer>();
	/**
	 * Sucht in der Tabelle in denen alle vorhandenen Kalender aufgelistet sind nach einem Kalender
	 * für den accountName und gibt dessen _id zurück. Falls der Kalender nicht existiert, wird er
	 * erstellt.
	 */
	 public int findOrCreateAccountCalendar(String accountName){//, final String title, final long dtstart, final long dtend) {
	    
		 if((calIdHash.get(accountName))!=null){
			 return calIdHash.get(accountName);
		 }
		 
		 
		 try{		
	        Uri allCalendarsUri = getDifCalendarUri();	               
	        //String[] PROJEKTION = new String[]{ "_id", "_sync_account", "_sync_account_type", "displayName"};
	        String[] PROJEKTION = new String[]{ "_id", "_sync_account", "displayName", "access_level"};
	        //String[] PROJEKTION = null;
	        Cursor cursor = cr.query(allCalendarsUri, PROJEKTION, "_sync_account=?", new String[]{accountName}, null);   
	       
	        
	        
	       	if(cursor.moveToFirst()){
		       	
	       		/*
		       	for (int i = 0; i < cursor.getColumnCount(); i++)  {
					String spaltenname = cursor.getColumnName(i);
		        	String spalteninhalt = cursor.getString(i);		        	
		        	System.out.println(">" + spaltenname + ": " + spalteninhalt);
				}
	       		*/
	       		
	       		// TODO nur bei erstem Aufruf wirklich prüfen. Danach Ergenis in Variable speichern.
	       		int id = cursor.getInt(cursor.getColumnIndex("_id"));
	       		String displayname = cursor.getString(cursor.getColumnIndex("displayName"));
	       		debug.println("findOrCreateCalendar gefunden: \n" + id + ": " + displayname);	       
	       		cursor.close();
	       		calIdHash.put(accountName, id);
	       		return id;
	       		
	       	}else{
	       		cursor.close();
	       		ContentValues cv = new ContentValues();
		       	//cv.put("_id",    er zählt automatisch hoch.
		       	cv.put("_sync_account", accountName);
		       	cv.put("_sync_account_type", Constants.ACCOUNT_TYPE);		    
		       	cv.put("displayName", Constants.ACCOUNT_DISPLAY_NAME);
		       	cv.put("hidden", 0);
		       	cv.put("selected", 1);
		       	
		       	cv.put("access_level", 700);
		       	cv.put("sync_events", 1);
		       	cv.put("timezone", "Europe/Amsterdam");
		       	cv.put("ownerAccount", accountName);
		       	
		       	//Vorsicht!   "displayOrder",  0




		       	
		       	
		       	
				cr.insert(allCalendarsUri, cv);
				debug.println("findOrCreateCalendar Kalender wurde einegfügt..");
				
				

				// rekursiver Aufruf würde genügen. Dies würde im Fehlerfall aber zu einer Endlosschleife führen.				
				cursor = cr.query(allCalendarsUri, PROJEKTION, "_sync_account=?", new String[]{accountName}, null);   
				if(cursor.moveToFirst()){
					int id = cursor.getInt(cursor.getColumnIndex("_id"));
					calIdHash.put(accountName, id);
					return id;
				}else{
					debug.println("auch beim zweiten Versuch wurde keine id gefunden.");					
					throw new ArrayIndexOutOfBoundsException();
				}
				
				
	       	}
		 }catch(Exception e){
			 debug.printStackTrace("findOrCreateAccount(1)", e);
			 return -1;
		 }
	       
	    }


	private Uri getDifCalendarUri() {		
		return Uri.parse("content://com.android.calendar/calendars");
	}
	 
	 
	 
	 /*
	 public void origAddToCalendar(int CalendarID, String title, long dtStart, long dtEnd) {	    	
	    	android.content.ContentValues cv = new android.content.ContentValues();
	 
	    	
	    	cv.put("calendar_id", CalendarID);
	    	cv.put("title", title);
	    	cv.put("dtstart", dtStart);
	    	cv.put("dtend", dtEnd);
	    	
	    	// Test: 20 Sekunden Alarm
	    	cv.put("hasAlarm", 0);
	    	
	    	/*cv.put("reminder_duration" , 20);    	
	    	cv.put("alerts_rintone", "conent://media/internal/audio/media/28");    	
	    	cv.put("reminder_type",0);
	    	cv.put("alerts_vibrate",1);
	    	cv.put("selected",1);
	    	* vll muss man einen Reminder in einer anderen Tabelle hinzufügen.
	    	*//*
	    		
	    	// mal sehen ob eine Wiederholung möglich ist.
	    	//cv.put("rrule", "FREQ=YEARLY;WKST=SUBYMONTHDAY=6,BYMONTH=12");
	    	
	    	
	    	cv.put("allDay", 0); 		// vermutlich geht es darum ob es ein ganztägiges Event ist.
	    	cv.put("eventStatus", 1);
	    	cv.put("transparency", 0);
	    	cv.put("description", title);
	    	cv.put("eventLocation", "im Internet");
	    	cv.put("visibility", 1);
	    	
	    	
	    	
	    	//Uri eventsUri = Uri.parse("content://com.android.calendar/events");
	    	Uri eventsUri = Uri.parse(this.findDifCalendarsURI());
	    
	    	try{
	    		Uri url = cr.insert(eventsUri, cv);
	    		debug.println("einfügen des Kalenderevents ist abgeschlossen.");
	    	}catch(Exception e){
	    		debug.printStackTrace("Exc. bei orig. Insert:", e);	    		
	    		
	    	}
	    	
	    
	    		
	    		
	 	}	    
	 */
	 
	 
	 
	 /**
	  *  Probiert alle angegebenen URIs durch. Sobald die Querry unter einer URI Erfolgreich ist
	  *  (Cursor != null), wird diese URI als String zurück gegeben.
	  *  Gibt die eine (erste) gefundene URI als STring zurück.
	  *  4Events deshalb, weil bei der SELECTION nicht "_id" und "displayname" selektiert wird, diese Spalten
	  *  kommen bei der Tabelle mit den verschiedenen Kalendern, nicht aber bei den Einträgen in den Kalendern vor.
	  *  @return
	  *//*
	  String findDifCalendarsURI(){
	    	// irgendeine dieser URLs ist (hoffentlich) die richtige, die zum Android Calender führt...
	    	// geht nur auf dem Gerät, nicht im Simulator. Im Simulator gibt es keinen(!) Kalender.    	
	    	
	    
	    	final ContentResolver cr = mContext.getContentResolver();
	    	
	    	
	    	
	    	String[] possURIs = {//"content://com.android.calendar/calendars",    			
	    			"content://com.android.calendar/events",
	    			"content://com.android.calendar",
	    			"content://com.android.calendar",
	    			"content://calendar/events",
	    			"content://calendar/calendars",
	    			"content://com.android.calendar",
	    			"content://com.android.calendar/calendar",
	    			"content://com.android.calender/calenders",
	    			"content://com.android.calender/content",
	    			"content://com.android.calender/content",
	    			"content://calender/events/content","content://calender/calenders/content",
	    			"content://com.android.calender/content",
	    			"content://com.android.calender/content",
	    			"content://com.android.calender",
	    			"content://com.android.calender",
	    			"content://calender/events", "content://calender/calenders",
	    			"content://com.android.calender", 
	    			"content://com.android.calender",
	    			"com.htc.calendar",
	    			"com.htc.calender",
	    			"content://com.htc.calendar",
	    			"content://com.htc.calender",
	    			"content://com.htc.calenders"};
	    	
	    	//String URI = "";
	    	for(int i = 0; i < possURIs.length;i++){
	    		try{
	    			
	    			// 4Events deshalb, weil bei der SELECTION nicht "_id" und "displayname" selektiert wird.
	    			// Final bitte nicht so implementieren!
	    			Cursor c = cr.query(Uri.parse(possURIs[i]), null, null, null, null);      	
		    		if(c!=null){
		    			debug.println("Ergebnis von findDifC.s URIs: " + possURIs[i]);
		    			c.close();
		    			return possURIs[i];
		    			//URIs+="+: " + possURIs[i] + " \n";
		    			//return possURIs[i];
		    			// immerhin wird die letzte URI zurück gegeben.
		    			// mögliche Lösung: nur aktive Kalender akzeptieren.
		    			//cursor = c;
		    		}
		    	}catch(Exception e){
		    		debug.println("normaleSearchException " + possURIs[i]);
		    		// Falsche URIs führen zu einer Exception. Da durch Probieren herausgefunden werden soll, welche URIs richtig sind,
		    		// werden falsche schlicht ignoriert.
		    		//URIs += "Exception: " + e + ": " + possURIs[i] + "\n";
		    	}
	    	}
	    	
	    	debug.println("findDifC.s kein Treffer! ");	 
	        return "";    	
	    }
	   */
   
    
    

}
