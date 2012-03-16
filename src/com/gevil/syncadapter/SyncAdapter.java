/*
 * Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.gevil.syncadapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.gevil.notifications;
import com.gevil.AndroidCalendar.Konsolenausgabe;
import com.gevil.calSyncZustand.CalendarOperations;
import com.gevil.calSyncZustand.GevilVCalendar;
import com.gevil.calSyncZustand.calSyncZustand;

import myHttp.WebDAV;

import syncZustand.ContactOperations;
import syncZustand.GevilVCard;
import syncZustand.TypeBiMap;
import syncZustand.syncZustand;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";

    private final AccountManager mAccountManager;
    private final Context mContext;

    private Date mLastUpdated;
    TypeBiMap mMap;
    
    Konsolenausgabe debug = new Konsolenausgabe();

	private notifications mNotification;

	private notifications getNotification(String username) {				
		/*
		if(mNotification==null){
			mNotification = new notificationactivity();
			mNotification.setData(mContext, "tblConflicts" + username);
			mNotification.notificationsAdded= false;
		}
		
		if(mNotification.getTabellenname().substring("tblConflicts".length()) != username){
			mNotification = new notificationactivity();
			mNotification.setData(mContext, "tblConflicts" + username);
			mNotification.notificationsAdded= false;
		}else{
			System.out.println("Es wird eine neue NotificationActivity erstellt: "  + mNotification.getTabellenname().substring("tblConflicts".length()) + " != " + username);
		}
		
		return mNotification;
		*/
		return null;
	}

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
        mMap = new TypeBiMap();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {
    	
    	if(authority.equals("com.android.contacts")){
    		syncContacts(authority);
    	}
    	
    	if(authority.equals("com.android.calendar")){
    		syncCalendar(authority);
    	}
    }
    
    private void syncCalendar(String authority) {	
    	
    	System.out.println(".===========================================================");    	
		System.out.println(".==  führe Synchronisation durch, authority: " + authority);
    	System.out.println(".===========================================================");
   	

             try {
            	 Account[] ac = mAccountManager.getAccountsByType(Constants.AUTHTOKEN_TYPE);
            	 
            	 for(int i = 0; i < ac.length;i++){
            	 
            		String pwd = "passwort not found.";            	
            		pwd = mAccountManager.blockingGetAuthToken(ac[i], Constants.AUTHTOKEN_TYPE, false);		       			  
            		
            		WebDAV wd = new WebDAV(ac[i].name, pwd, mMap);
            		
            		//
            		// Nur wenn die Serverauthentifizierung erfolgreich war, darf eine Synchronisation durchgeführt werden!
            		//
            		if(wd.authenticate(ac[i].name, pwd, mContext)){
            			String username = ac[i].name;
            			String Tabellenname = "GCal_" + username;        		
            			calSyncZustand sz = new calSyncZustand(username, pwd, mContext, mMap, Tabellenname, getNotification(username));
            			
            			//Konsolenausgabe debug = new Konsolenausgabe();            			
            			//sz.setKonsolenausgabe(debug );
            			
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
            			
            			
            			//try{
            				CalendarOperations androidOps = sz.syncVglAndroid();
            				androidOps.setKonsolenausgabe(debug);
            				
            				CalendarOperations serverOps = sz.syncVglServer();
            				serverOps.setKonsolenausgabe(debug);
            				
            				androidOps.findConflictingOperations(serverOps);
            				
            				serverOps.runAll(true);
            				androidOps.runAll(false);
            			/*}catch(Exception e){
            				//debug.printStackTrace("sVGL", e);
            				e.printStackTrace();
            			}
            			*/
            			sz.closeDatabases();  
            		
            		}else{
            			 System.out.println("Authentifizierung fehlgeschlagen. Synchronisation wird abgebrochen.");
            		}
            		
            		
            	 }
			} catch (IndexOutOfBoundsException e){				
				System.out.println("." + e + " in OnPerfomSync(x.4). " +e);
				e.printStackTrace();
			} catch (OperationCanceledException e) {
				System.out.println("." + e + " in OnPerfomSync(x.5). " +e);
				e.printStackTrace();
			} catch (AuthenticatorException e) {
				System.out.println("." + e + " in OnPerfomSync(x.6). " +e);
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("." + e + " in OnPerfomSync(x.7). " +e);
				e.printStackTrace();
			}
             
			System.out.println("================  ende onPerformSync. ==================");
             
	}
    
    


	/**
     * Führt eine Synchronisation aller Kontakte des Servers mit denen des Handys aus.
     * @param authority
     */
    private void syncContacts(String authority){    	
    	System.out.println(".===========================================================");    	
		System.out.println(".==  führe Synchronisation durch, authority: " + authority);
    	System.out.println(".===========================================================");
    	
    	
      
             // per account Manager Benutzername(n) und dazugehörige(s) Passwört(er) auslesen.
             try {
            	 Account[] ac = mAccountManager.getAccountsByType(Constants.AUTHTOKEN_TYPE);
            	 
            	 for(int i = 0; i<ac.length;i++){
            	 
            		String pwd = "passwort not found.";            	
            		pwd = mAccountManager.blockingGetAuthToken(ac[i], Constants.AUTHTOKEN_TYPE, false);		       			  
            		
					
            		WebDAV wd = new WebDAV(ac[i].name, pwd, mMap);
            		
            		//
            		// Nur wenn die Serverauthentifizierung erfolgreich war, darf eine Synchronisation durchgeführt werden!
            		//
            		if(wd.authenticate(ac[i].name, pwd, mContext)){
	            		
	            		//System.out.println(".Account gefunden: " + i + " " + ac[i].name + ":" + pwd);         			  
						String Tabellenname = "Gevil_" + ac[i].name;						
						
	            		syncZustand sz = new syncZustand(ac[i].name, pwd, mContext, mMap, Tabellenname, this.getNotification(ac[i].name));
	            		
						sz.setKonsolenausgabe(debug);
						
						
	            		
	            		//
	            		// zu Beginn der Synchronisation wird der letzte Sync Zustand geladen.
	            		//
	            		try{
	            			sz.readFromSQL();
	            		}catch(SQLiteException e){
	            			// Vermutlich existiert die Tabelle nicht.
	            			sz.setZustand(new ArrayList<GevilVCard>(0));
	            		}
	            		sz.closeDatabases();
	            		
	            		
	            		// Inhalt von Citadel in syncZustand auslesen     
	            		//sz.getVCardsFromServer();
	
	            		
	            		ContactOperations runOnServer = sz.syncVglAndroid();
	            		
	            		System.out.println(".\n==============================   syncVglAndroid jetzt: syncVglServer  ==========================");
	            		
	            		ContactOperations runOnAndroid = sz.syncVglServer();
	            		
	            		//
	            		// vor dem Ausführen aller Operationen auf Server UND Android, überprüfen ob Wiedersprüchliche Operationen ex.
	            		//
	            		if(runOnAndroid != null && runOnServer != null){
	            			runOnAndroid.findConflictingOperations(runOnServer);
	            		}
	            		
	            		// Unter Android ausführen
	            		if(runOnAndroid!=null)runOnAndroid.runAll(true);
	            		
	            		// Auf dem Server ausführen.
	            		if(runOnServer!=null)runOnServer.runAll(false);
	            		
	            		
	            		
	            		//
	            		// Zum Schluss wird der Zustand der im Handy vorherrst als Zustand gespeichert.
	            		// Dieser Zustand gleicht nach korrekt ausgeführter Sync dem im Server.
	            		// TODO Performance schlecht, weil zweimal alle Kontakte durchgegangen werden.
	            		//
	            		
	            		
	            		
	            		/*
	            		System.out.println(".\n==============================  syncVglAndroid jetzt:  aktualisierung des SyncZustandes  ==========================");
	            		//sz.getVCardsFromPhone();
	            		sz.getVCardsFromServer();
	            		sz.saveAsSQL(Tabellenname);
	            		*/
	            		
	            		sz.closeDatabases();
	     	
	            	 } // end for, die alle vorhandenen Accounts durchgeht. 
            		 else{
            			 System.out.println("Authentifizierung fehlgeschlagen. Synchronisation wird abgebrochen.");
            		 } 
            	 }
			} catch (IndexOutOfBoundsException e){				
				System.out.println("." + e + " in OnPerfomSync(4). " +e);
				e.printStackTrace();
			} catch (OperationCanceledException e) {
				System.out.println("." + e + " in OnPerfomSync(5). " +e);
				e.printStackTrace();
			} catch (AuthenticatorException e) {
				System.out.println("." + e + " in OnPerfomSync(6). " +e);
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("." + e + " in OnPerfomSync(7). " +e);
				e.printStackTrace();
			}
             
			System.out.println("================  ende onPerformSync. ==================");
             
             /*
             
             // fetch updates from the sample service over the cloud
             users =
                NetworkUtilities.fetchFriendUpdates(account, authtoken,
                    mLastUpdated);
            // update the last synced date.
            mLastUpdated = new Date();
            // update platform contacts.
            Log.d(TAG, "Calling contactManager's sync contacts");
            ContactManager.syncContacts(mContext, account.name, users);
            // fetch and update status messages for all the synced users.
            statuses = NetworkUtilities.fetchFriendStatuses(account, authtoken);
            ContactManager.insertStatuses(mContext, account.name, statuses);
        */
    }
}
