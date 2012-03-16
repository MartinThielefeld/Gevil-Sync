package com.gevil.AndroidCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import org.apache.commons.lang.NotImplementedException;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Date;
import syncZustand.TypeBiMap;

import com.gevil.AndroidContacts.readAndroidContacts.ContactsFromPhoneReader;
import com.gevil.calSyncZustand.GevilVCalendar;
import com.gevil.calSyncZustand.calSyncZustand;
import com.gevil.syncadapter.Constants;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class ReadAndroidCalendar {	

	Context mContext;
	TypeBiMap mMap;
	Konsolenausgabe debug = new Konsolenausgabe();
	private calSyncZustand mSyncZustand;
	
	private String mUsername;
	
	
	public ReadAndroidCalendar(TypeBiMap mMap, Context ctx, String accountUsername, calSyncZustand syncZustand) {		
		this.mMap = mMap;
		this.mContext = ctx;
		this.mUsername = accountUsername;
		this.mSyncZustand = syncZustand;
		
	}
	public void setKonsolenausgabe(Konsolenausgabe sdf){		
		debug = sdf;
	}


	

	

    // Select title", "dtstart","dtend", "rrule", "_sync_account", "delted
    /*
	public ArrayList<GevilVCalendar> readCalendar(boolean onlyGevilEvents){
    	
    	ArrayList<GevilVCalendar> ret = new ArrayList<GevilVCalendar>();
    	    	    	
    	GevilVCalendar first = new GevilVCalendar(mMap);
    	boolean firstUsed = false;
    	
        try {
        	        											
        	//final Cursor cursor = tryUriQuery(null, "_sync_account_type", new String[]{Constants.ACCOUNT_TYPE_TEST, Constants.ACCOUNT_TYPE, Constants.ACCOUNT_DISPLAY_NAME, });
        	final Cursor cursor = tryUriQuery(null, null, null);
        	debug.println(debug.ka);
        	
        	while (cursor.moveToNext()) {
                // Die folgenden drei Zeilen lahmen!
                
            	GevilVCalendar cal;
            	if(!firstUsed){
            		cal = first;
            		firstUsed = true;
            	}else{
            		cal = new GevilVCalendar(mMap);
            	}
            	
            	boolean eventBelongsToAccount = false;
            	debug.println(".");	
            	
            	for (int i = 0; i < cursor.getColumnCount(); i++) {
                	
	                	String Spaltenname = cursor.getColumnName(i);
	                	String Spalteninhalt = cursor.getString(i);
	                	
	                	
	                	if(Spaltenname != null && Spalteninhalt != null){ 
	                		if(Spaltenname.length()>0 && Spalteninhalt.length()>0){
	                			if(Spaltenname!="null" && Spalteninhalt!="null"){	                				
	                				if(Spaltenname.equals("title")){
	                					cal.setDescription(Spalteninhalt);
	                					debug.println("t> " + Spaltenname + ": " +Spalteninhalt);	
	                				}else if(Spaltenname.equals("dtstart")){	                					
	                					cal.setDtStart(Long.valueOf(Spalteninhalt));
	                				}else if(Spaltenname.equals("dtend")){ 
	                					cal.setDtEnd(Long.valueOf(Spalteninhalt));
	                				}else if(Spaltenname.equals("rrule")){
	                					cal.setRrule(Spalteninhalt);
	                				}else if(Spaltenname.equals("_id")){
	                					//debug.println("i> " + Spaltenname + ": " +Spalteninhalt);	
	                					cal.setAndroiId(Integer.valueOf(Spalteninhalt));
	                				}else if(Spaltenname.equals("_sync_account_type")){
	                					if(Spalteninhalt.equals(Constants.ACCOUNT_TYPE_TEST)){
	                						eventBelongsToAccount = true;
	                					}else{
	                						debug.println("a> " + Spaltenname + ": " +Spalteninhalt);
	                					}
	            					}else if(Spaltenname.equals("_sync_id")){
	            						cal.setUid(Spalteninhalt);
	            					}
	                				
	                				/*else if(Spaltenname.equals("delted")){
	                					// TODO
	                					cal.println("> " + Spaltenname + ": " + Spalteninhalt);	
	            					}else if(Spaltenname.equals("allDay")){
	                					// TODO
	                					cal.println("> " + Spaltenname + ": " + Spalteninhalt);	
	            					}else if(Spaltenname.equals("organizer")){
	                					// TODO
	                					cal.println("> " + Spaltenname + ": " + Spalteninhalt);	
	            					 *//*
	            					else{
	                					// TODO
	            						//debug.println("> " + Spaltenname + ": " + Spalteninhalt);	
	            					}
	                				
	                				
	                				/*
	                				if(Spaltenname.equals("title") ||Spaltenname.equals("dtstart") ||Spaltenname.equals("dtend") ||Spaltenname.equals("rrule") ||Spaltenname.equals("_sync_account") ||Spaltenname.equals("delted")){//, ,, , , )
	                				
		                				if(Spaltenname != "title"){
		                					Einzelevent += "\n" + Spaltenname + " .=. "+ Spalteninhalt;
		                				}else{
		                					// Der titel soll als erstes angezeigt werden.
		                					Einzelevent = "\n" +  Spaltenname + " .=. "+ Spalteninhalt + Einzelevent;
		                				}
	                				}
	                				*//*
	                			}
	                		}
	                	} 
	                	
                }                
                if(eventBelongsToAccount || !onlyGevilEvents){
                	// Es werden nur Events des GevilAccounts ausgelesen.
                	ret.add(cal);
                	debug.println(cal.getSummary() +  " added:)");	
                }else{
                	debug.println(cal.getSummary() +  " dif.Acc.");	
                }
               
            } // while cursor.movetonext()
        } catch (Exception e) {
            // Im Normalfall weil Cursor == null <=> Es gibt keinen Kalender <=> Simulator, nicht auf echtem Gerät.
        	debug.printStackTrace("Exc.readCalendar: ", e);
        }
        
        if(ret.size()==0){
        	// so ist weiterhin die "Konsolenausgabe" möglich.
        	ret.add(first);
        }
        
        return ret;
    }
    */
    
    private Uri getCalenderEventsUri(){
    	return Uri.parse("content://com.android.calendar/events");
    }
    
    private Uri getDifCalendersUri(){
    	return Uri.parse("content://com.android.calendar/calendars");
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
    */
	private Cursor tryUriQuery(String[] Projektion, String Selection, String[] SelArgs){    	
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

    			Cursor c = cr.query(Uri.parse(possURIs[i]), Projektion, Selection, SelArgs, null);
    			
	    		if(c!=null){
	    			debug.ka += "\nfolgende Kalender URI wurde gefunden: " + possURIs[i];
	    			return c;	
	    			// return possURIs[i];
	    			// mögliche Lösung: nur aktive Kalender akzeptieren.	    			
	    		}
	    	}catch(Exception e){
	    		// Falsche URIs führen zu einer Exception. Da durch Probieren herausgefunden werden soll, welche URIs richtig sind,
	    		// werden falsche schlicht ignoriert.
	    		//URIs += "Exception: " + e + ": " + possURIs[i] + "\n";
	    	}
    	}     
    	
    	debug.println("try hat kein URI gefunden.");
        return null;
    }
	
	
	
	/*
	 public String ffindAllCalendars(){//, final String title, final long dtstart, final long dtend) {
	    	final ContentResolver cr = mContext.getContentResolver();
	      
	        String Ergebnis = "";
	        String[] PROJEKTION = new String[]{ "_id", "displayname" };
	        //Cursor cursor = cr.query(Uri.parse("content://com.android.calendar/calendars"), null, null, null, null);
	        Cursor cursor = cr.query(this.getDifCalendersUri(), null, null, null, null);
	        	
	        if(cursor == null){
	        	// Es existiert kein Kalender. z.B. im Simulator, oder auch durch eine neue Android Version
	        	return "Cursor is null.";        		
	        }        								   	
	        
	        
	        //if (cursor.moveToFirst()) {
	        while(cursor.moveToNext()){
	        	final String[] calNames = new String[cursor.getCount()];
	        	final int[] calIds = new int[cursor.getCount()];
	            	
	            	
	        	/*            
	        	for (int i = 0; i < calNames.length; i++) {
	                //calIds[i] = cursor.getInt(0);
	        		Ergebnis += "id: " + cursor.getInt(cursor.getColumnIndex("_id")) + "\n";
	                //calNames[i] = cursor.getString(1);
	        		Ergebnis += "Name: " + cursor.getColumnName(cursor.getColumnIndex("displayname)) + "\n";
	                cursor.moveToNext();
	            }        	
	        	return Ergebnis;
	        	*//*
	        	
	        	for (int i = 0; i < cursor.getColumnCount(); i++) {
	            	
	            	String Spaltenname = cursor.getColumnName(i);
	            	String Spalteninhalt = cursor.getString(i);
	            	
	            	
	            	if(Spaltenname != null && Spalteninhalt != null){ 
	            		if(Spaltenname.length()>0 && Spalteninhalt.length()>0){
	            			if(Spaltenname!="null" && Spalteninhalt!="null"){    
	                    		Ergebnis +=  Spaltenname + ": " + Spalteninhalt + "\n";     
	            			}
	            		}
	            	}
	        	}
	        	Ergebnis += "\n.\n";
	        	
	        	
	        }
	        cursor.close();        
	        return Ergebnis;
	    }
	 */
	 
	 /**
	  * Gibt eine Insantz von CalendarFromPhoneReader zurück.
	  * @param context
	  * @param map
	  * @param accountName
	  * @return
	  */
	 public CalendarFromPhoneReader getCalendarFromPhoneReader(){
	   	return new CalendarFromPhoneReader();
	 }
	    
	 public class CalendarFromPhoneReader implements Iterator<GevilVCalendar>{
		
		final Cursor cursor;
		 GevilVCalendar nextCalendar = null;
		
	
		
		 
		 CalendarFromPhoneReader(){
			//cursor = tryUriQuery(null, "_sync_account_type", new String[]{Constants.ACCOUNT_TYPE_TEST });
	        cursor = tryUriQuery(null, null, null);
		 }

		@Override
		public boolean hasNext() {

			boolean dbg = true;
			
			if (cursor.getCount() > 0) {
				if (cursor.moveToNext()) {
							
					try {
						nextCalendar = readNextCalendarFromPhone();
					} catch (Exception e) {						
						//debug.printStackTrace("hasNext: ", e);
						e.printStackTrace();
						return false;
					}
					
					if(nextCalendar==null || nextCalendar.toString()==null){
						// rekursiver Aufruf!
						if(dbg)debug.println("rekursiver Aufruf!");						
						return hasNext();
					}else{
						// Aulesen war erfolgreich!
						if(dbg)debug.println("Aulesen war erfolgreich!");
						return true;
					}					
				}else{
					// !cur.moveToNext()
					if(dbg)debug.println("!cur.moveToNext()");				
					return false;
				}
			}else{
				//  !cur.getCount() > 0
				if(dbg)debug.println("!cur.getCount() > 0");
				return false;
			}
		}
		
		String addLine(String t, String newText){
			t += "\n" + newText;
			return t;
		}

		private GevilVCalendar readNextCalendarFromPhone() throws NumberFormatException, java.text.ParseException {           
        	GevilVCalendar cal = new GevilVCalendar(mMap);
        	cal.setKonsolenersatz(debug);
        	
        	boolean dbg = false;
        	
        	// wird ggf. auf true gesetzt d.h. wenn true, weiß ich sicher dass
        	// dass event zum Account gehört. Wenn false weiß ich nix.
        	boolean flagEventBelongsToAccount = false;
        	
        	// wird im Durchlauf auf false gesetzt. Wenn true weiß ich nix, wenn false weiß ich sicher
        	// dass das Event NICHT zum Account gehört.
        	boolean flagEventDoesNotBelongToAccount = false;
        	
        	boolean hasAlarm = false;
        	boolean eventDeleted = false;
        	String ausg = "";
        	ausg = addLine(ausg, ".\n");
        	
        	
        	String dtstartInMillis = null;
        	String duration = null;
        	
        	// TODO SELECTION zum Beschleunigen.
        	// while Schleife sollte effizienter als for Schleife sein.
        	int i=-1;
        	
        	
        	
        	String deleted = cursor.getString(cursor.getColumnIndex("deleted"));
        	if(deleted.trim().equals("1")){ eventDeleted=true; return null;}
        	
        	String ac_type = cursor.getString(cursor.getColumnIndex("_sync_account_type"));
			if(ac_type.equals(Constants.ACCOUNT_TYPE)){
				flagEventBelongsToAccount = true;				
			}else{
				flagEventDoesNotBelongToAccount = true;
				ausg = addLine(ausg,   "ac_type" + ": " +ac_type);
				return null;
				//debug.println("a> " + Spaltenname + ": " +Spalteninhalt);
			}
			
			
        	
        	while(i < cursor.getColumnCount()-1 && (!flagEventDoesNotBelongToAccount && !eventDeleted)){
        	i++; // anstatt das i++ ans ende der Schleife zu setzten, habe ich im Rumpf jeweils 1 abgezogen!
        	//for (int i = 0; i < cursor.getColumnCount(); i++)  {
            	
                	//System.out.println("i: " + i + " count: "+ cursor.getColumnCount());
        			String spaltenname = cursor.getColumnName(i);
                	String spalteninhalt = cursor.getString(i);           
                	                	
                	
                	//if(!flagEventDoesNotBelongToAccount && !eventDeleted)
                	if(spaltenname != null && spalteninhalt != null){ 
                		if(spaltenname.length()>0 && spalteninhalt.length()>0){
                			if(spaltenname!="null" && spalteninhalt!="null"){
                				
                				if(dbg)System.out.println(">" + spaltenname + ": " + spalteninhalt);
                				
                				if(spaltenname.equals("title")){
                					//cal.setDescription(Spalteninhalt);
                					cal.setSummary(spalteninhalt);
                					//debug.println();	
                					ausg = addLine(ausg, "t> " + spaltenname + ": " +spalteninhalt);
                				}else if(spaltenname.equals("description")){
                					cal.setDescription(spalteninhalt);
                				}else if(spaltenname.equals("eventLocation")){
                					cal.setLocation(spalteninhalt); 
                				}else if(spaltenname.equals("dtstart")){
                					dtstartInMillis = spalteninhalt;
                					
                					cal.setDtStart(getVCalendarTime(spalteninhalt));
                					//cal.setDtStart(Long.valueOf(Spalteninhalt));
                				}else if(spaltenname.equals("dtend")){
                					cal.setDtEnd(getVCalendarTime(spalteninhalt));                				            				
                				}else if(spaltenname.equals("_id")){
                					//debug.println("i> " + Spaltenname + ": " +Spalteninhalt);	
                					cal.setAndroiId(Integer.valueOf(spalteninhalt));
                					ausg = addLine(ausg,spaltenname + ": " +spalteninhalt);
                				}else if(spaltenname.equals("_sync_account_type")){
                					if(spalteninhalt.equals(Constants.ACCOUNT_TYPE)){
                						flagEventBelongsToAccount = true;
                					}else{
                						flagEventDoesNotBelongToAccount = true;
                						ausg = addLine(ausg,   spaltenname + ": " +spalteninhalt);
                						//debug.println("a> " + Spaltenname + ": " +Spalteninhalt);
                					}
            					}else if(spaltenname.equals("_sync_id")){
            						cal.setUid(spalteninhalt);
            					}else if(spaltenname.equals("deleted")){
            						//debug.println("d> " + Spaltenname + ": " +Spalteninhalt);
            						ausg = addLine(ausg,  spaltenname + ": " +spalteninhalt); 
            						if(spalteninhalt.equals("1")){
                						eventDeleted = true;
                					}
                					//cal.println("> " + Spaltenname + ": " + Spalteninhalt);	
            					}else if(spaltenname.equals("visibility")){
            						//debug.println("v> " + Spaltenname + ": " +Spalteninhalt);
            						ausg = addLine(ausg,spaltenname + ": " +spalteninhalt);
            						
            					}else if(spaltenname.equals("rrule")){
            						
            						String rec = spalteninhalt;/*.replace("RRULE:", "").replace("WKST=MO", "").trim();
            						while(rec.contains(";;")){rec = rec.replace(";;", ";");}
            						rec = rec.replace("\n", "");
            						//if(rec.endsWith(";")) rec = rec.substring(0, rec.length()-1 );
            						rec=rec.trim();
            						*/
            						
            						try{
            							cal.setRrule(rec);
            						}catch(Exception e){
            							flagEventDoesNotBelongToAccount = true;
            							System.out.println("excWert: " + rec + " t.: " + cursor.getString(cursor.getColumnIndex("title")));
            							e.printStackTrace();
            							
            						}
            						
            						ausg = addLine(ausg,spaltenname + ": " +spalteninhalt);
            					}else if(spaltenname.equals("duration")){            						
            						ausg = addLine(ausg,spaltenname + ": " +spalteninhalt);
            						duration = spalteninhalt;
            						//cal.setDuration(spalteninhalt);
            					}else if(spaltenname.equals("hasAlarm")){            						
            						ausg = addLine(ausg,spaltenname + ": " +spalteninhalt);            						
            						hasAlarm = true;            						
            						
            					}else{                					
            						ausg = addLine(ausg,spaltenname + ": " +spalteninhalt);
            					}
                				
                				
                				/*
                				if(Spaltenname.equals("title") ||Spaltenname.equals("dtstart") ||Spaltenname.equals("dtend") ||Spaltenname.equals("rrule") ||Spaltenname.equals("_sync_account") ||Spaltenname.equals("delted")){//, ,, , , )
                				
                				}
                				*/
                			}
                		}
                	} 
           	
            }                
            
        	if(dbg)System.out.println("\n.\n");
        	/*
        	if(hasrRule){
        		ausg = addLine(ausg, "hasRRule " + cal.getSummary());
        		debug.println(ausg);
        	}
        	*/
        	if(flagEventBelongsToAccount && !eventDeleted){            
            	// Es werden nur Events des GevilAccounts ausgelesen.            	
            	
        		if(duration != null){
        			// TODO duration einlesen und den korrekten wert anstatt 36000 addieren.
        			//cal.setDtEnd(cal.getPropValue("DTEND"));
        			
        			cal.setDuration(duration);
        		}
        	
        		if(hasAlarm){        			
        			if(dtstartInMillis==null)throw new ArrayIndexOutOfBoundsException();        			
        			
        			int androidAlarm = readAlarm(cal, dtstartInMillis);
     			        
        			/*
        			long alarmOffsetInMillis = androidAlarm * 60 * 1000; // Von Minuten nach Millisekunden umrechnen
        			long eventDtStart =  Long.valueOf(dtstartInMillis);        			
        			Calendar x = Calendar.getInstance();
        			x.setTimeInMillis(eventDtStart - alarmOffsetInMillis);
        			cal.setAlarm(eventDtStart - alarmOffsetInMillis);     
        			System.out.println("alarm: " + dtstartInMillis + " alarmInMinutes: " + androidAlarm + " alarmzeitpunkt: " + x.getTime().toLocaleString());
        			 */
        		
        		}
        		
        		
        		if(dbg)System.out.println("Dieses Event gehört zum Account und ist nicht gelöscht.");
        		debug.println(ausg);
            	debug.println(cal.getSummary() +  " added:");	            	
            	
            	
            	/*
            	GevilVCalendar zCal = mSyncZustand.getVCalendarByAndroidId(cal.getAndroidId());
            	if(zCal!=null){if(zCal.getUid() != cal.getUid()){
            			// Der Kontakt hat eine UID, die Gevil-Sync aber noch nicht kennt. Im Zustand haben wir aber einen Eintrag gefunden
            			// der die gleiche Android Id hat.  ==> der Android Kalender editor hat die UID geändert.
            			// zusätzlich verwenden wir noch eine Heuristik, ob die Karten wirklich gleich sind.
            			
            			
            			boolean heurIdentisch = false;     
            			
            			if(cal.propValuesEqual("DESCRIPTION", zCal))heurIdentisch = true;
            			
            			if(cal.propValuesEqual("LOCATION", zCal))heurIdentisch = true;
            			if(cal.propValuesEqual("SUMMARY", zCal))heurIdentisch = true;
            			if(cal.propValuesEqual("DTSTART", zCal)
            			&& cal.propValuesEqual("DTEND", zCal))heurIdentisch = true;
   
            			
            			if(heurIdentisch){
                			// Es wird angenommen dass die UID verloren gegangen ist. Deshalb wird die UID
            				// des im Sync-Zustand gefundenen VCalendars übernommen.
    	            		cal.setUid(zCal.getUid());            	
    		            	InsertUpdateDeleteAndroidCalendar iudCal = new InsertUpdateDeleteAndroidCalendar(mContext, mMap);
    		            	iudCal.update(cal, mUsername);
            				
    		            	System.out.println("heur: " + cal.getSummary() + " wurde nicht gelöscht, sonder verändert.");
    		            	
            			}else{
                			// Es wird angenommen dass es sich um einen völlig neuen Eintrag handelt.
    	            		zCal.setAndroiId(-1);
            				cal.setUid(cal.generateUniqueId(mContext).toString());            	
    		            	InsertUpdateDeleteAndroidCalendar iudCal = new InsertUpdateDeleteAndroidCalendar(mContext, mMap);
    		            	iudCal.update(cal, mUsername);
    		            	
    		            	System.out.println("heur: " + cal.getSummary() + " wurde gelöscht und zufällig ein neuer Eintrag mit gleicher _id erstellt.");
            			}
            		}else{
            			System.out.println("heur: " + cal.getSummary() + " uids passen schon..");
            		}}else{            		
            			System.out.println("heur: " + cal.getSummary() + " zCal == null. D.h. im Zustand ist keine Karte mit gleich AndroidId. AndroidId: " + cal.getAndroidId());
            		}
            	    */		
            	if(cal.getUid()==null){	
            			//cal.getUid()==null => wir generieren eine UID.
	            		cal.setUid(cal.generateUniqueId(mContext).toString());            	
		            	InsertUpdateDeleteAndroidCalendar iudCal = new InsertUpdateDeleteAndroidCalendar(mContext, mMap);
		            	iudCal.update(cal, mUsername);			            	
            	}
            	
            	if(cal.getUid()==null)throw new ArrayIndexOutOfBoundsException();	
            	
            	return cal;
            }else{     // !eventBelongsToAccount       	
            	if(dbg)if(!flagEventBelongsToAccount)System.out.println("flagEventBelongsToAccount = false");
            	if(dbg)if(eventDeleted)System.out.println("eventDeleted = true");
            	
            	return null;
            }
        	
		}
		
		
		private int readAlarm(GevilVCalendar cal, String dtstartInMillis) {
        	int androidAlarmTimeInMinutes = -1;
			boolean moreThanOneAlarm = false;
        	
        	
			ContentResolver cr = mContext.getContentResolver();
			Cursor c = cr.query(Uri.parse("content://com.android.calendar/reminders"), null, "event_id=?", new String[]{String.valueOf(cal.getAndroidId())}, null);
        	            	            	
        	while(c.moveToNext()){
        		//tv.setText(tv.getText() + "\n");
        		        	
        		androidAlarmTimeInMinutes = c.getInt(c.getColumnIndex("minutes"));        		
        		
        		long alarmOffsetInMillis = androidAlarmTimeInMinutes * 60 * 1000; // Von Minuten nach Millisekunden umrechnen
    			long eventDtStart =  Long.valueOf(dtstartInMillis);
    			Calendar x = Calendar.getInstance();
    			x.setTimeInMillis(eventDtStart - alarmOffsetInMillis);
    			cal.setAlarm(eventDtStart - alarmOffsetInMillis);     
    			
    			String d = "alarm";
    			if(moreThanOneAlarm) d+="[+]";
    			System.out.println(d + " " + dtstartInMillis + " alarmInMinutes: " + androidAlarmTimeInMinutes + " alarmzeitpunkt: " + x.getTime().toLocaleString());
    			
    			
    			moreThanOneAlarm=true;
        		
        		//System.out.println("alert: " + androidId + " = " + ret);
        		
        		/*
        		int j=0;
        		try{
            		for(int i=0;i<c.getCount()-1;i++){
            			j=i;
	            		String name = c.getColumnName(i);
	            		String inhalt = c.getString(i);
	            		//tv.setText(tv.getText() + name + ": " + inhalt + "\n");
	            		System.out.println(name + ": " + inhalt);
            		}
        		}catch(ArrayIndexOutOfBoundsException e){
        			System.out.println("i: " + j + " count: " + c.getCount());
        			e.printStackTrace();
        		}
        		*/
        	}
        	
        	return androidAlarmTimeInMinutes;
			
		}

		private String zweistellig(int monat){			
			//String m = String.valueOf(monat).replace("-", "");
			String m = String.valueOf(monat);
			while(m.length()<2){
				m = "0" + m;
			}			
			return m;
		}
		private String getVCalendarTime(String spalteninhalt) {
			
			
			Long timeInMillis = Long.valueOf(spalteninhalt);
			//Date d = new Date(timeInMillis);
			// es gibt verschiedene Zeitdarstellungsformen für den VCalendar. 
			// wir wollen die Zeit als UTC Zeit darstellen.
			
			
			Calendar ca = Calendar.getInstance();
			ca.setTimeInMillis(timeInMillis);
			String jahr = String.valueOf(ca.get(Calendar.YEAR));
			
			int mon = ca.get(Calendar.MONTH);
			//System.out.println(">monat vorher: " + mon);
			if(mon<12)mon++;					
			//System.out.println(">monat nachher: " + mon);
			String monat = zweistellig(mon);
			
			
			String tag = zweistellig(ca.get(Calendar.DAY_OF_MONTH));	
			
			
			
			int st = ca.get(Calendar.HOUR);
			
			int amPm = ca.get(Calendar.AM_PM);
			
			if(amPm==1){				
				st+=12;
			}
			String stunde = zweistellig(st);
			
			
			String minute = zweistellig(ca.get(Calendar.MINUTE));
			//System.out.println("<gelesen: "+ minute);
			
			String sekunde = zweistellig(ca.get(Calendar.SECOND));
			
			//String jahr = String.valueOf(d.getYear()+1900);
			//String monat = zweistellig(d.getMonth() + 1);
			//String tag = zweistellig(d.getDate());	
			//String stunde = zweistellig(d.getHours());
			//String minute = zweistellig(d.getMinutes());
			//String sekunde = zweistellig(d.getSeconds());
			
			
			/*
		
			int l = spalteninhalt.length();
			int uhrzeit = Integer.valueOf(
					spalteninhalt.substring(l-7)); /* Das ist jetzt die Uhrzeit in Sekunden.
					 									 * die letzten drei stellen verwerfen wir
					 									 * weil es Millisekunden sind.
					 									 */
			/*
			uhrzeit = uhrzeit / 1000;
			
			debug.println("Sekunden des Tages " + uhrzeit);
			
			//String sekunde = String.valueOf(uhrzeit % 60);
			uhrzeit = uhrzeit / 60;
			//String minute = String.valueOf(uhrzeit % 60);
			//String minute = String.valueOf(uhrzeit);
			uhrzeit = uhrzeit / 60;
			
			String stunde = zweistellig(String.valueOf(uhrzeit));
			*/
			
			String ret = jahr + monat + tag + "T" + stunde + minute + sekunde;
			/*
			debug.println("And. gelesen: " + spalteninhalt);
			debug.println("toLocale String: ");
			debug.println(d.toLocaleString());
			debug.println("daraus geparst: ");
			debug.println(ret);
			*/
			
			return ret;
		}

		@Override
		public GevilVCalendar next() {
			if(nextCalendar == null)throw new ArrayIndexOutOfBoundsException();
			
			return nextCalendar;
		}

		@Override
		public void remove() {
			// TODO was macht das?
			throw new NotImplementedException();			
		}
		 
	 }

}
