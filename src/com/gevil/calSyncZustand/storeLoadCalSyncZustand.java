package com.gevil.calSyncZustand;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;

import syncZustand.TypeBiMap;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;

public class storeLoadCalSyncZustand {

	
	SyncZustandTabelleOpenHelper helper;
	SQLiteDatabase readableDatenbank;
	SQLiteDatabase Writabledatenbank;
	TypeBiMap mMap;
	final Context mContext;
	private Integer etag;
	
	/**
	 * Konstruktor
	 * @param ctx
	 * @param Tabellenname
	 */	
	storeLoadCalSyncZustand(Context ctx, String Tabellenname, TypeBiMap map){
		helper = new SyncZustandTabelleOpenHelper(ctx, Tabellenname);
		readableDatenbank = helper.getReadableDatabase();
		Writabledatenbank = helper.getWritableDatabase();
		mMap = map;
		mContext = ctx;
	}
	
		

	
	/**
	 * ACHTUNG! Nach dem Aufruf ist das Objekt dieser Klasse nichtmehr nutzbar!
	 */
	public void closeAll(){		
		readableDatenbank.close();
		Writabledatenbank.close();
	}
	
	
	/** 
	 * Helper Klasse wird zum Datenbankzugriff benötigt!
	 * @author Martin
	 * 
	 */
	private class SyncZustandTabelleOpenHelper extends SQLiteOpenHelper {

	    private static final int DATABASE_VERSION = 2;
	    
	    public String createTable(String tableName){
	    	return 
	    	"CREATE TABLE " + tableName + " (" +
            "uid" + " TEXT, " +
            "VCalendar" + " TEXT, " +
            "android_id" + " TEXT, " + 
            "etag" + " TEXT, " +
            "serverFilename" + " TEXT" + ")";
	    }

	    SyncZustandTabelleOpenHelper(Context context, String Tabellenname) {	
	        super(context, Tabellenname, null, DATABASE_VERSION);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	        //db.execSQL(DICTIONARY_TABLE_CREATE);
	    }

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
		}
	}
	
	
	
	/**
	 *  löscht zuerst den gesammten Inhalt der Tabelle und schreibt dann den kompletten syncZustand
	 *  neu in die Tabelle ein.   TODO eventuell schneller machen, z.B. nur ausführung von Sync Befehlen.
	 
	public void zustandKomplettNeuSpeichern(syncZustand z, Context ctx, String Tabellenname){
		
		if(helper==null)helper = new SyncZustandTabelleOpenHelper(ctx, Tabellenname);
		
		// TODO Tabellename = Name des Kontos.
		
		// erst schauen ob die Tabelle schon existiert.
		try{
			readableDatenbank.execSQL(helper.createTable(Tabellenname));
		}catch(Exception e){
			// TODO bitte ohne try catch.
			System.out.println("Die Tabelle " + Tabellenname + " existiert bereits. TODO mit if prüfen ob Tabelle erstellt werden muss." + e);			
			// Tabelle leeren.
			this.emptyTable(Tabellenname, ctx);			
		}
		

		ContentValues kontakt;
		Iterator<GevilVCard> it = z.getZustand().iterator();
		while(it.hasNext()){
			GevilVCard card = it.next();	

			
			if(card.getAndroidId() == -1){
				//
				// suche korrespondierende Android _id im Handyadressbuch
				// TODO Sehr langsam!!
				//
				readAndroidContacts rac = new readAndroidContacts(mMap);
				ContactsFromPhoneReader cit = rac.getContactsPhoneReader(ctx, mMap);
				boolean done = false;
				while(cit.hasNext() && !done){
					GevilVCard c = cit.next();
					if(c.getUid().equals(card.getUid())){
						card.setAndroidId(c.getAndroidId());
						done = true;
						//System.out.println("ungleich GLEICHE! UIDs: " + c.getUid() + " und " + card.getUid());
					}else{
						//System.out.println("ungleiche UIDs: " + c.getUid() + " und " + card.getUid());
					}
				}
				if(!done){
					//System.out.println("Es wurde keine Androi _id zu " + card.getN()[0] + " gefunden.");
					card.setAndroidId(-1);
				}
				
			}
			
			kontakt = new ContentValues();			
			//datenbank.insert(table, nullColumnHack, values)
			kontakt.put("android_id" , card.getAndroidId());
			kontakt.put("uid" , card.getUid());
			kontakt.put("VCard" , card.toString());			
			kontakt.put("etag", card.getEtag());
			Writabledatenbank.insert(Tabellenname, null, kontakt );
		}
	}
	*/	
	
	
	
	

		
	/**
	 * Liest den Inhalt der SQL-Tabelle als ArrayList<GevilVCard> aus und schreibt das Ergebnis in den SyncZustand.
	 * @param z
	 * @param ctx
	 * @param Tabellenname
	 */
	public void zustandLaden(calSyncZustand z, Context ctx, String Tabellenname) throws SQLiteException{
		if(helper==null)helper = new SyncZustandTabelleOpenHelper(ctx, Tabellenname);
			
		boolean dbg = false;
		
		//
		// SQL SELECT *
		//
		final Cursor c = readableDatenbank.query(Tabellenname, 
			// Projektion (=Spalten) (=Columns)
			//new String[] {"uid", "VCard", "android_id", "etag"},
			DataQuery.FROM,
			
			// Selektion [WHERE]
			null, null,
			
			// groupBy, having, orderBy
			null, null, null);
		
		// Datenbank wird in VCard ArrayList ausgelesen.
		ArrayList<GevilVCalendar> ret = new ArrayList<GevilVCalendar>(c.getCount());
		
		
		// Einzelne Zeilen der Datenbank durchgehen.
		while (c.moveToNext()) {   
			String uid = c.getString(DataQuery.SPALTE_UID);
			String vCalendarString = c.getString(DataQuery.SPALTE_VCALENDAR);
			
			String idString = c.getString(DataQuery.SPALTE_ANDROID_ID);
			
			int android_id=-1;
			if(idString != null)
			if(TextUtils.isDigitsOnly(idString)){
				android_id = Integer.valueOf(idString);
			}
			
			int etag = -1;
			String x = c.getString(DataQuery.SPALTE_ETAG);
			if(x!=null)
			etag = Integer.valueOf(x);
			
			if(dbg)System.out.println("geladener SQLetag: " + etag);
			
			String pathOnServer = c.getString(DataQuery.SPALTE_SERVERPATH);
			
			//System.out.println("folgendes wurde aus der DB ausgelesen: \n uid: " + uid + "\n VCard: " + VCard + "\n android_id: " + android_id );
			//System.out.println(".");
			
			int index = 0;
			if(dbg)System.out.println("<>");
			
			try {
				
				//c = Workarounds.fixVCardForical4j_vcard(VCardString);	
		        StringReader sr = new StringReader(vCalendarString);
		        
		        CalendarBuilder builder = new CalendarBuilder();
		        //VCardBuilder builder = new VCardBuilder(sr);
				Calendar basiccard = builder.build(sr);				
				GevilVCalendar card = new GevilVCalendar(basiccard, mMap);
				
				// spezielle Attribute zur GEvilVCard hinzufügen
				
			//	card.setUid(uid);	// TODO uid wird eigentlich in VCard selbst gespeichert.			
				card.setAndroiId(android_id);
				card.setEtag(etag);
				card.setServerPath(pathOnServer);
				
				
				if(dbg)System.out.println("folgende VCard wurdge gelesen: \n" + card.toString());
				if(dbg)System.out.println(".\n VcardString-Spalte in DB: \n" + vCalendarString);
				if(dbg)System.out.println(".\n.");
				
				
				// ist das notwenidg?
				if(ret.size() > index){
					ret.add(index, card);
				}else{
					ret.add(card);
				}
				index++;
				
				
			
			// Im Falle einer Exception sofort Synchronisation Abbrechen!
			// Ansonsten ist der SyncZustand lerr, und es wird alles eingefügt,
			// obwohl Kontakte schon bestehen. 
			}catch (ParserException e) {
				System.out.println("(2)Exception in zustandLaden: " + e);
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("(1)Exception in zustandLaden: " + e);
				e.printStackTrace();
			} /* catch (Exception e) {
				System.out.println("(3)Exception in zustandLaden: " + e);
				e.printStackTrace();
			} */// ACHTUNG catch ist innerhalb der while Schleife!
			  // wenn c hier geschlossen wird, ist es beim nächsten Schleifendurchlauf 
			  // nicht verfügbar
			
		}
		
		c.close();
				
		z.setZustand(ret);
		z.rehash();		
	
	}
	
	
	/**
	 * leeren einer Tabelle
	 * @param Tabellenname
	 * @param ctx
	 */
	private void emptyTable(String Tabellenname, Context ctx){
		//SyncZustandTabelleOpenHelper helper = new SyncZustandTabelleOpenHelper(ctx, Tabellenname);
		//SQLiteDatabase datenbank = helper.getWritableDatabase();
		if(helper==null)helper = new SyncZustandTabelleOpenHelper(ctx, Tabellenname);
		
		readableDatenbank.delete(Tabellenname, null, null);
	}
	
	
	
	

	public void insert(GevilVCalendar iCard, Context ctx, String Tabellenname){
			
			if(helper==null)helper = new SyncZustandTabelleOpenHelper(ctx, Tabellenname);
			
			
			if(exists(iCard, ctx, Tabellenname)){
				// wo zum Teufel werden Dupletten ins SQL eingefügt?
				throw new ArrayIndexOutOfBoundsException();
			}
			
			
			// erst schauen ob die Tabelle schon existiert.
			try{
				readableDatenbank.execSQL(helper.createTable(Tabellenname));
			}catch(Exception e){
				// TODO bitte ohne try catch.
				System.out.println("Die Tabelle " + Tabellenname + " existiert bereits. TODO mit if prüfen ob Tabelle erstellt werden muss." + e);			
			}
			
			/*
			if(iCard.getAndroidId() == -1){
				throw new ArrayIndexOutOfBoundsException();
			}
			*/
				
			ContentValues kontakt = new ContentValues();			
			//datenbank.insert(table, nullColumnHack, values)
			kontakt.put("android_id" , iCard.getAndroidId());
			kontakt.put("uid" , iCard.getUid());
			kontakt.put("VCalendar" , iCard.toString());			
			kontakt.put("etag", iCard.getEtag());
			kontakt.put("serverFilename", iCard.getServerPath());
		
			
			Writabledatenbank.insert(Tabellenname, null, kontakt );
	}
	
	
	

	public void update(GevilVCalendar iCard, Context ctx, String Tabellenname){
			
			if(helper==null)helper = new SyncZustandTabelleOpenHelper(ctx, Tabellenname);			
			// erst schauen ob die Tabelle schon existiert.
			/*
			try{
				readableDatenbank.execSQL(helper.createTable(Tabellenname));
			}catch(Exception e){
				// TODO bitte ohne try catch.
				System.out.println("Die Tabelle " + Tabellenname + " existiert bereits. TODO mit if prüfen ob Tabelle erstellt werden muss." + e);			
			}*/
			
			/*
			if(iCard.getAndroidId() == -1){
				throw new ArrayIndexOutOfBoundsException();
			}
			*/
				
			ContentValues kontakt = new ContentValues();			
			//datenbank.insert(table, nullColumnHack, values)
			kontakt.put("android_id" , iCard.getAndroidId());
			kontakt.put("uid" , iCard.getUid());
			kontakt.put("VCalendar" , iCard.toString());			
			kontakt.put("etag", iCard.getEtag());
			kontakt.put("serverFilename", iCard.getServerPath());	
			
			int affectedRows = Writabledatenbank.update(Tabellenname, kontakt, "uid" + "=?", new String[]{iCard.getUid()});
			
						
			if(affectedRows < 1){
				System.out.println("update fehlgeschlagen, es wurden " + affectedRows + " Zeilen verändert.  uid_ " + iCard.getUid());
				throw new ArrayIndexOutOfBoundsException();
			}
			
	}
	
	
	public void delete(GevilVCalendar iCard, Context ctx, String Tabellenname){
		
		if(helper==null)helper = new SyncZustandTabelleOpenHelper(ctx, Tabellenname);			
		// erst schauen ob die Tabelle schon existiert.
		/*
		try{
			readableDatenbank.execSQL(helper.createTable(Tabellenname));
		}catch(Exception e){
			// TODO bitte ohne try catch.
			System.out.println("Die Tabelle " + Tabellenname + " existiert bereits. TODO mit if prüfen ob Tabelle erstellt werden muss." + e);			
		}*/	
		
		int n = Writabledatenbank.delete(Tabellenname, "uid" + "=?", new String[]{iCard.getUid()});
		
		if(n<1){
			System.out.println("Fehler bei Delete von UID: " + iCard.getUid());
			throw new ArrayIndexOutOfBoundsException();
		}
	}
	
	
	public boolean exists(GevilVCalendar iCard, Context mContext2, String mSQLtableName) {
		
		try{
			if(iCard.getUid()==null)throw new ArrayIndexOutOfBoundsException();
			
			final Cursor c = readableDatenbank.query(mSQLtableName, 
				// Projektion (=Spalten) (=Columns)
				//new String[] {"uid", "VCard", "android_id", "etag"},
				DataQuery.FROM,
				
				// Selektion [WHERE]
				"uid=?", new String[]{iCard.getUid()},
				
				// groupBy, having, orderBy
				null, null, null);

			// falls ein einziger Eintrag vorkommt, existiert die Karte mit dieser UID.
			while (c.moveToNext()) {
				String rUID = c.getString(DataQuery.SPALTE_UID);
				if(!rUID.equals(iCard.getUid())){
					System.out.println("*** EXCEPTION REASON: " + rUID + " != " + iCard.getUid());
					throw new ArrayIndexOutOfBoundsException();
				}
				return true;				
			}
			
		return false;
		}catch(SQLiteException e){
			// vermutlich: Tabelle existiert nicht.
			System.out.println("prüfen ob wirklich no such table?? > " + e.getMessage());
			return false;
		}
	}
	
	
	
	/**
     * Konstanten für die Datenbankabfrage.
     */
    private interface DataQuery {
        public static final String[] FROM =
        	new String[] {"uid", "VCalendar", "android_id", "etag", "serverFilename"};

        public static final int SPALTE_UID = 0;
        public static final int SPALTE_VCALENDAR = 1;
        public static final int SPALTE_ANDROID_ID = 2;
        public static final int SPALTE_ETAG = 3;
        public static final int SPALTE_SERVERPATH = 4;
  

        //public static final String SELECTION = Data.RAW_CONTACT_ID + "=?";
    }




}
