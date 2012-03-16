package com.gevil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.property.Trigger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import myHttp.GroupDAV;
import myHttp.GroupDAV.CalendarFromServerReader;
import myHttp.WebDAV.Authorization;
import myHttp.WebDAV.HttpPropfind;
import myHttp.WebDAV;

import syncZustand.ContactOperations;
import syncZustand.GevilVCard;
import syncZustand.TypeBiMap;
import syncZustand.syncZustand;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gevil.AndroidCalendar.Konsolenausgabe;
import com.gevil.AndroidCalendar.ReadAndroidCalendar;
import com.gevil.AndroidCalendar.ReadAndroidCalendar.CalendarFromPhoneReader;
import com.gevil.AndroidContacts.readAndroidContacts;
import com.gevil.AndroidContacts.readAndroidContacts.ContactsFromPhoneReader;
import com.gevil.calSyncZustand.CalendarOperations;
import com.gevil.calSyncZustand.GevilVCalendar;
import com.gevil.calSyncZustand.calSyncZustand;
import com.gevil.syncadapter.Constants;



public class Gevil extends Activity {
		
	private final Context gevil = this;
	TypeBiMap mmMap = new TypeBiMap();
	
	// zum Testen der notificationactivity
	
	
	private notifications getNotification(String username) {				
		notifications activityNotification = new notifications();
		activityNotification.setData(gevil, "tblConflicts" + username);
		activityNotification.notificationsAdded= false;
		
		return activityNotification;
	}
	
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);   
        
        
    
        /*
        Intent settingsActivity = new Intent(getBaseContext(), options_activity.class);
        settingsActivity.putExtra("username", "kaffe");
        startActivity(settingsActivity);
        */
        
        /*
        notificationactivity x = new notificationactivity();
        x.setData(gevil, "tblConflicts" + "kaffe");
        x.addConflict("testADDED0", "testId", 0);
        //x.showUserNotification(gevil);
        */
        

        
    
        //http://developer.android.com/resources/samples/ContactManager/src/com/example/android/contactmanager/ContactAdder.html
 
        /*
        int i = getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, Contacts._ID +"=?",new String[]{"1"});
        int j = getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, ContactsContract.Contacts._ID +"=?",new String[]{"1"});
        
        */
        
        
       
         // Zeile NICHT löschen, sonst gibt es einen unerwarteten Absturz!!
        setContentView(R.layout.main);
        
       final TextView tv = (TextView)findViewById(R.id.editText1); 
        
       Button btOptions = (Button)findViewById(R.id.btOptions);
       
        Button btExport = (Button)findViewById(R.id.button1);
        btExport.setText("ReadCFromCitadel(kaffe)");
        final Konsolenausgabe ausgabe = new Konsolenausgabe();
        
        btExport.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {  
            	tv.setText("");
            	
            	GroupDAV gd = new GroupDAV("kaffe", "tasse", mmMap, gevil);
            	CalendarFromServerReader it = gd.getCalendarIterator();
            	
            	while(it.hasNext()){
            		GevilVCalendar cal = it.next();
            		tv.setText(tv.getText() + cal.toString() + "\n.\n");
            		
            	}
            	/*PUT Alarm
            	try {
					GevilVCalendar alert = new GevilVCalendar(mmMap);
	            	Calendar cal = Calendar.getInstance();
	            	cal.set(2012, 1, 1);            		
            		
            		alert.setDtStart(cal.getTimeInMillis());
					alert.setDtEnd(cal.getTimeInMillis()+1000);					
					alert.setDescription("AlarmtestDesc.");					
					alert.setSummary("AlarmtestSummary");
					
					
					net.fortuna.ical4j.model.Calendar rc = alert.getCalendar();						
					Component valarm = new VAlarm();
					valarm.getProperties().add(new Trigger());
					rc.getComponents().add(valarm);					
					System.out.println(rc.toString());
					
					
					/**
					 * Jetzt hochladen des Kalenders incl. Alarm
					 */
					
            		/*
					GroupDAV gd = new GroupDAV("kaffe", "tasse", mmMap, gevil);
					
					alert = new GevilVCalendar(rc, mmMap);
					alert.setUid(alert.generateUniqueId(gevil).toString());
					
					WebDAV wd = new WebDAV("kaffe", "tasse", mmMap);
					wd.PutVCalendar(alert, "http://192.168.2.107/groupdav/Calendar/" + alert.getUid());
					
					
					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	*/
            	
            	
            	
            	
        
            /* PROPFIND	            	
            	WebDAV wd = new WebDAV("kaffe", "tasse", mmMap);              	
		        WebDAV.Authorization auth = wd.new Authorization("kaffe", "tasse");
		            	
		        HttpPropfind aPropFind = new HttpPropfind();
				aPropFind.setURI(URI.create("http://192.168.2.107/groupdav/Tasks"));
						
				aPropFind.addHeader(auth.getAuthKey(), auth.getAuthValue());
				HttpResponse response;
				
				try {
					DefaultHttpClient mHttpClient = new DefaultHttpClient();
					response = mHttpClient.execute(aPropFind);
		
					// Codierung der Daten stehen in einem Header
					// vielleicht ContentEncoding.
		
					BufferedReader in = null;	
					String encoding = "" + response.getEntity().getContentEncoding();
					in = new BufferedReader(new InputStreamReader(response.getEntity()
							.getContent()));
		
					String s = "";
					String line;
					while ((line = in.readLine()) != null) {	
						s += line += "\n";					
					}
					
					
					System.out.println(s);
					tv.setText(s);
					
				}catch(Exception e){
					
				}
            	*/
            }
            });
         
        Button btImport = (Button)findViewById(R.id.button2);
        btImport.setText("readReminder");
        btImport.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {             	
            	String account = "kaffe";
            	
            	tv.setText("");            	
            	
            	ContentResolver cr = gevil.getContentResolver();
            	Cursor c = cr.query(Uri.parse("content://com.android.calendar/reminders"), null, null, null, null);
            	            	            	
            	while(c.moveToNext()){            		
            		tv.setText(tv.getText() + "\n");
            		int j=0;
            		try{
	            		for(int i=0;i<c.getCount()-1;i++){
	            			j=i;
		            		String name = c.getColumnName(i);
		            		String inhalt = c.getString(i);
		            		tv.setText(tv.getText() + name + ": " + inhalt + "\n");
		            		System.out.println(name + ": " + inhalt); 
	            		}
            		}catch(ArrayIndexOutOfBoundsException e){
            			System.out.println("i: " + j + " count: " + c.getCount());
            			e.printStackTrace();
            		}
            		
            	}
            	
            	
            }});
        
        
        btOptions.setText("opt.");
        btOptions.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	tv.setText("");
            	AccountManager a = android.accounts.AccountManager.get(gevil);
            	Account[] acs = a.getAccounts();
            	
            	
                SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
                
            	tv.setText("");
            	
            	boolean setupOpened = false;
            	
            	for(Account x:acs){
            		if(x.type.equals("com.Gevil")){
            			Intent settingsActivity = new Intent(getBaseContext(), options_activity.class);
	            		settingsActivity.putExtra("username", x.name);
	                    startActivity(settingsActivity);
	                    setupOpened = true;
            		}
            		
            		tv.setText(tv.getText() + "\n" + x.name + " " + x.type);
            		
            	}
            	
            	if(!setupOpened){
            		//
            		// Durch diesen Hack wird der Options Dialog auch angezeigt, wenn kein Syncadapter
            		// installiert ist.
            		//
        			Intent settingsActivity = new Intent(getBaseContext(), options_activity.class);
            		settingsActivity.putExtra("username", "kaffe");
                    startActivity(settingsActivity);
                    tv.setText("**\n" + tv.getText() + "\n.\n(!)kein Syncadapter gefunden. Anzeige für kaffe.");
                    setupOpened = true;
            	}
            }
            
        });
        
        Button btShowical = (Button)findViewById(R.id.button3);
        btShowical.setText("syncCalendar_kaffe:tasse");
        btShowical.setOnClickListener(new View.OnClickListener() {
          

			public void onClick(View v) {     
        		tv.setText("");
            	String username = "kaffe";
        		
            	
            	
            	//// ************************************************
        		String Tabellenname = "GCal_" + username;        		
				calSyncZustand sz = new calSyncZustand("kaffe", "tasse", gevil, mmMap, Tabellenname, getNotification(username));
        		
        		Konsolenausgabe debug = new Konsolenausgabe();
        		
				sz.setKonsolenausgabe(debug );
        		//
        		// zu Beginn der Synchronisation wird der letzte Sync Zustand geladen.
        		//
        		try{
        			sz.readFromSQL();
        		}catch(SQLiteException e){
        			// Vermutlich existiert die Tabelle nicht.
        			sz.setZustand(new ArrayList<GevilVCalendar>(0));
        		}
        		sz.closeDatabases();
        		
        		
        		// Es ist sinnvoll zuerst den Vergleich mit Android durchzuführen,
        		// weil hierbei die _id's im SyncZustand aktualisiert werden.
        		//
        		
        		
        		try{
        		CalendarOperations androidOps = sz.syncVglAndroid();
        		androidOps.setKonsolenausgabe(debug);
        		
        		CalendarOperations serverOps = sz.syncVglServer();
        		serverOps.setKonsolenausgabe(debug);
        		
        		androidOps.findConflictingOperations(serverOps);
        		
        		/*
        		Iterator<CalendarOp> opIt = androidOps.getOpList().iterator();
        		while(opIt.hasNext()){
        			CalendarOp el = opIt.next();
        			GevilVCalendar cal = el.getGevilCalendar();
        			
        			tv.setText(tv.getText() + "Op.Typ: " + el.operationType() + "\n");
        			tv.setText(tv.getText() + cal.toString() +   "\n.");        			
        		}
        		*/
        		
        		serverOps.runAll(true);
        		androidOps.runAll(false);
        		}catch(Exception e){
        			//debug.printStackTrace("sVGL", e);
        			e.printStackTrace();
        		}
        		
        		sz.closeDatabases();        		
        		
        		tv.setText(tv.getText() + debug.ka);
            }


            });  
        
        
        Button btGroupDAV = (Button)findViewById(R.id.button4);
        btGroupDAV.setText("readCalendarFromPhone_kaffe:tasse");
        btGroupDAV.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                 
               	tv.setText("");
               	
               	ReadAndroidCalendar rcal = new ReadAndroidCalendar(mmMap, gevil, "kaffe", null);                
            	CalendarFromPhoneReader ir = rcal.getCalendarFromPhoneReader();
            	
            	while(ir.hasNext()){
            		GevilVCalendar cal = ir.next();
            		tv.setText(tv.getText() + cal.toString() + "\n");
            		
            	}
            	
            	
            	/*
            	System.out.println(".===========================================================");
            	System.out.println(".==  führe Synchronisation durch, authority: " );
            	System.out.println(".===========================================================");
                
               		String pwd = "passwort not found.";            	
               		pwd = "tasse" ;  
   					String Tabellenname = "Gevil_" + "kaffe";
               		syncZustand sz = new syncZustand("kaffe", "tasse", gevil, mmMap, Tabellenname);
               		
               		//
               		// zu Beginn der Synchronisation wird der letzte Sync Zustand geladen.
               		//
               		System.out.println("starte ReadFromSQL");
               		try{
               			sz.readFromSQL();
               		}catch(SQLiteException e){
               			// Vermutlich existiert die Tabelle nicht.
               			sz.setZustand(new ArrayList<GevilVCard>(0));
               		}
               		sz.closeDatabases();
               		System.out.println("starte syncVGL ANdroid");
               		ContactOperations runOnServer = sz.syncVglAndroid();               		
               		System.out.println(".\n==============================   syncVglAndroid jetzt: syncVglServer  ==========================");               		
               		ContactOperations runOnAndroid = sz.syncVglServer();
               		if(runOnAndroid != null && runOnServer != null){
               			runOnAndroid.findConflictingOperations(runOnServer);
               		}               		
               		// Unter Android ausführen
               		if(runOnAndroid!=null)runOnAndroid.runAll(true);
               		// Auf dem Server ausführen.
               		if(runOnServer!=null)runOnServer.runAll(false);
               		sz.closeDatabases();
               		System.out.println("================  ende onPerformSync. ==================" );
              */  
            }
        });  
        
    }
    
    public Context getContext(){
    	return gevil;
    }
    
    
    
    
	/**
	* erweitert die http Methoden um PROPFIND
	*/
	public class HttpPropfind extends HttpRequestBase {
		@Override
		public String getMethod() {
			return "PROPFIND";
		}

	}

}