package com.gevil.AndroidContacts;


import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.vcard.Parameter.Id;
import net.fortuna.ical4j.vcard.Property;

import syncZustand.AktuellClientGevilVCard;
import syncZustand.AktuellServerGevilVCard;
import syncZustand.GevilVCard;
import syncZustand.TypeBiMap;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;

import com.gevil.AndroidCalendar.Konsolenausgabe;
import com.gevil.syncadapter.Constants;


public class readAndroidContacts {
	
	public readAndroidContacts(TypeBiMap map, String account){
		mMap = map;
		mAccountName = account;
	}
	
	private final TypeBiMap mMap;
	private final String mAccountName;
	private Konsolenausgabe debug;
	
	/**
	 * Gibt eine ArrayListe zurück die alle Kontakte die im Handy gespeichert sind enthält.
	 * 
	 * @param context
	 * @param tf
	 * @return
	 */
	public ArrayList<GevilVCard> readContactsToArrayList(Context context, String accountName) {
		ArrayList<GevilVCard> ret = new ArrayList<GevilVCard>(0);
		
		Iterator<GevilVCard> it = this.getContactsPhoneReader(context, mMap, accountName);
		while(it.hasNext()){
			GevilVCard card = it.next();
			ret.add(card);
			
		}
		
		return ret;
	}
	
	
	/**
     * Lesen aller _id's von Kontakten im Android Adressbuch
     * und weiterverarbeitung.
     * 
     * @param context Activity Context
     *//* VERALTET; wird jetzt per ITERATOR erledigt.
	public ArrayList<GevilVCard> readContactsFromPhone(Gevil context, TextView tf) {
        //long rawContactId = -1;		
		
		ArrayList<GevilVCard> ret = new ArrayList<GevilVCard>(0);
        ContentResolver resolver = context.getContentResolver();        
       
        Cursor cur = resolver.query(ContactsContract.Contacts.CONTENT_URI, null,  //new String[]{"_id"}
				null, null, null);

        
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {

		 
	    	/* 	Intension: alle spaltennamen auslesen.
	    	 * 
	    	 *  for (int i = 0; i < cur.getColumnCount(); i++) {	        	
	        	String Spaltenname = cur.getColumnName(i);
	        	String Spalteninhalt = cur.getString(i);
	        	System.out.println("einzeln ausgelesene Spalten des Kontaks: ");
	        	if(Spaltenname != null && Spalteninhalt != null){   
	        		System.out.println(i + "" + Spaltenname + ": " + Spalteninhalt);
	        	}
	    	}  *//*      
	    	
				
				String id = cur.getString(cur
						.getColumnIndex(ContactsContract.Contacts._ID));
				
				GevilVCard card;
				card = this.readContactFromPhone(context, Long.valueOf(id));				
				
									
					try {
						if(card!=null){
							if(card.toString() != null)
							{
								ret.add(card);
								System.out.println(card.toString());
								//out.output(card, System.out);
							}
						}else{
							System.out.println("eine ausgelesene VCard ist null, _id: " + id);
						}
					} catch (Exception e1) {
						System.out.println("Exception NICHT von readContactFromPhone gefangen: " + e1);
					}
					
					System.out.println("vCard: \n\n" + card.toString() + "\n=========================");
				
				
				/*} catch (NullPointerException e1) {
					System.out.println("Exception von readContactFromPhone gefangen: " + e1);

				} catch (Exception e){
					System.out.println("Exception von readContactFromPhone gefangen: " + e);
				}*//*
			}
		}
		return ret;
	}
	*/
	
		
	/**
	 * zusammenfügen mehrerer VCards die in einer Hashmap gespeichert sind
	 * zu einer VCard. GevilVCards bei denen card.belongsToAccount==false gilt,
	 * werden nicht hinzugefügt.
	 * @return 
	 */
	/*
	private GevilVCard joinVCards(HashMap<Integer, GevilVCard> gCards){
		Iterator<GevilVCard> it = gCards.values().iterator();
		GevilVCard ret = null;
		String sUID = null;
		
		int joined = 0;
		
		while(it.hasNext()){
			GevilVCard card = it.next();
			if(card.belongsToAccount){
				joined++;
				// einfügen.				
				//System.out.println("joinHashVcards: " + card.getUid());
				if(ret!=null){
					
					//
					// Alle Properties, vor Allem Email und Telefon übernehmen
					//
					//						
					List<Property> x = card.getVCard().getProperties(net.fortuna.ical4j.vcard.Property.Id.EMAIL);
					Iterator<Property> pIt = x.iterator();
					while(pIt.hasNext()){
						Property prop = pIt.next();						
						ret.getVCard().getProperties().add(prop);
					}
					
					x = card.getVCard().getProperties(net.fortuna.ical4j.vcard.Property.Id.TEL);
					pIt = x.iterator();
					while(pIt.hasNext()){
						Property prop = pIt.next();	
						ret.getVCard().getProperties().add(prop);
					}
					
					//
					// Namen ggf. Zusammenfassen
					// 
					String[] vorNachName = ret.joinAndSetNames(ret.getN(), card.getN());
					card.setN(vorNachName[0], vorNachName[1]);
					

					
				}else{
					// für den Normalfall mit nur einer VCard sollte das schneller sein.
					ret = card;
				}
				if(ret.getUid()==null){
					if(card.getUid()!=null){
						sUID = card.getUid();
					}
				}				
			}		
		}
		
		if(ret!=null){
			//System.out.println("joinVCards UID: " + ret.getUid());
			if(ret.getUid()==null){			
				
				if(sUID !=null)
				try {
					ret.setUid(sUID);
				} catch (URISyntaxException e) {					
					e.printStackTrace();
				}else if(gCards.size() > 0){
					throw new ArrayIndexOutOfBoundsException();
				}
		}}
		
		//System.out.println("es wurden " + joined + " VCards zusammengefasst.");
		return ret;			
	}
	*/
	
	/*
	 * Sind unter eine contactId mehrere Kontakte mit unterschiedlicher rawContactId zusammengefasst,
	 * so wird in dieser Hasmap jeder Kontakt gespeichert.
	 
	HashMap<Integer, GevilVCard> rawContactIDtoVCard;
	Iterator<Integer> rawContactIdIt;
	int posOfIterator = 0;
	int lastContactId = -1;
	int lastRawContactId = -1;
	GevilVCard lastReadContact;
	*/
	
	/**
	 * Liest zuerst alle Kontakte einer ContactId in eine Hashmap ein.
	 * Als nächstes werden der Reihe nach bei jedem Aufruf alle Kontakte die zum Account gehören zurück gegeben, und
	 * sobald kein dazugehöriger Kontakt mehr vorhanden ist, wird der Kontakt unter der nächsten contactId
	 * ausgelesen.
	 * 
	 * @param mContext
	 * @param contactId
	 * @param map
	 * @param iud
	 * @return
	 */
	/*
	private GevilVCard nextContactWithContactId(Context mContext, int contactId, TypeBiMap map, InsertUpdateDeleteAndroidContacts iud) {
		boolean dbg=false;
		
		//if(rawContactIDtoVCard==null)rawContactIDtoVCard = new HashMap<Integer, GevilVCard>();

		
		if(lastContactId != contactId){
			//
			// Da sich die contactId geändert hat, wird ein neuer zusammengefasster Kontakt ausgelesen.
			//
			if(dbg)System.out.println(".\n=== Lese zusammengesetzten Kontakt, weil " + lastContactId + " != " + contactId + " ========");
			
			rawContactIDtoVCard = readContactFromPhone(mContext, contactId, map, iud);
			rawContactIdIt = rawContactIDtoVCard.keySet().iterator();
			posOfIterator = 0;
			if(dbg)System.out.println(".\n.\nzugehörige Personen entnehmen");
			
		}else if(rawContactIdIt == null){			
			throw new ArrayIndexOutOfBoundsException();
		}		
		
		//
		// ACHTUNG! nicht mit RawContactId verwechseln.
		// 
		lastContactId = contactId;
			
		while(rawContactIdIt.hasNext()){
			posOfIterator++;
			Integer kKey = rawContactIdIt.next();
			GevilVCard k = rawContactIDtoVCard.get(kKey);			
			
			
			int lastId=-1;
			if(lastReadContact!=null)
			lastId = lastReadContact.getAndroidId();
			
			
			if(k.belongsToAccount &&  lastRawContactId != kKey){
				//
				// Android_id setzten
				//
				k.setAndroidId(kKey);
				
				if(dbg)System.out.println("nCont.With.C.Id: " + k.getReadableName() + " " + k.belongsToAccount + " id: " + contactId + " rawC.Id: " + kKey + " pos: " + posOfIterator + " size: " + rawContactIDtoVCard.size() + "\n.\n");
				
				//
				// UID setzten.
				//				
				String genUID;					
				try{
					if(k.getUid() == null){
						genUID = k.generateUniqueId(mContext).toString();
						//System.out.println("getUid==null, generate UID: " + genUID + " für: " + k.getN()[0]);
						k.setUid(genUID);						
						iud.updateContact(k, k.getAndroidId());
						iud.apply();
					}else if(TextUtils.isEmpty(k.getUid())){
						genUID = k.generateUniqueId(mContext).toString();
						//System.out.println("getUid is empty, generate UID: " + genUID + " für: " + k.getN()[0]);
						k.setUid(genUID);						
						iud.updateContact(k, k.getAndroidId());
						iud.apply();
					}
				}catch(URISyntaxException e){
					e.printStackTrace();
				}
				
				if(lastReadContact!=null)
				if(lastReadContact.getAndroidId() == k.getAndroidId()){
					throw new ArrayIndexOutOfBoundsException();
				}
				
				lastReadContact = k;			
				lastRawContactId = k.getAndroidId();
				return k;		
				
			}else{
				if(k.getReadableName().toLowerCase().contains("kaf.")){
					System.out.println("> Treffer: \n " + k.toString());
				}
				
				if(dbg)System.out.println(k.getReadableName() + " gehört nicht zum SA oder wurde schon gelesen.");
			}
				
		}

					
		rawContactIDtoVCard = null;
			
			
		//
		// keiner der Kontakte gehört zum Account.
		//		
		return null;
	}
	*/
	  
	  HashMap<Integer, GevilVCard> gCards;
	  
	  private GevilVCard readContactByRawContactId(Context mContext, int contact_id, int rawContactId, TypeBiMap map, InsertUpdateDeleteAndroidContacts iud) {	      
			 if(gCards==null)gCards = new HashMap<Integer, GevilVCard>();
			 boolean dbg = false;
			 
			 GevilVCard ret = gCards.get(rawContactId);
			 if(ret==null){
				// hinzufügen. Alte Einträge in gCards bleiben erhalten.  
				gCards=readContactBy_id(mContext, contact_id, map, gCards);
				ret= gCards.get(rawContactId);
			 }
			 
			 if(ret==null){
				 System.out.println("contactId: "+contact_id + " rawContactId: " + rawContactId);
				 throw new ArrayIndexOutOfBoundsException();
			 }
			 
			 
			 //
			 // ggf. UID generieren und ins Adresbuch eintragen
			 //
				String genUID;				
				try{
				if(ret.getUid() == null){
					genUID = ret.generateUniqueId(mContext).toString();
					if(dbg)System.out.println("getUid==null, generate UID: " + genUID + " für: " + ret.getN()[0]);
					ret.setUid(genUID);
					if(dbg)System.out.println(".+ readContactFromPhone UID == null, deshalb setUID: " + genUID + " " + ret.getN()[1]);
					iud.updateContact(ret, ret.getAndroidId());
					iud.apply();
				}else if(TextUtils.isEmpty(ret.getUid())){
					genUID = ret.generateUniqueId(mContext).toString();
					if(dbg)System.out.println("getUid is empty, generate UID: " + genUID + " für: " + ret.getN()[0]);
					ret.setUid(genUID);
					if(dbg)System.out.println(".+ readContactFromPhone UID is empty, deshalb setUID: " + genUID + " " + ret.getN()[1]);
					iud.updateContact(ret, ret.getAndroidId());
					iud.apply();
				}
				}catch(URISyntaxException e){
					System.out.println("=> Fehler beim Zuweisen der UID: " + e);
					//System.out.println("=> es sollte zugewiesen werden: " + genUID);
				}
				
				ret.setAndroidId(rawContactId);
			 
			 return ret;
	    }
	  
	  
	  
	  private HashMap<Integer, GevilVCard> readContactBy_id(Context mContext, int contact_id, TypeBiMap map, HashMap<Integer, GevilVCard> gCards) {
	        if(mContext == null)return null;    	
	        boolean dbg = false;
	        
	    	ContentResolver resolver = mContext.getContentResolver();
		    
		   
			//ArrayList<GevilVCard> gCards = new ArrayList<GevilVCard>();
			
			//gCards.put(contact_id, gCard);
			
	    	final String[] PROJECTION2 =
	        	new String[] {Data._ID, Data.MIMETYPE, Data.DATA1, Data.DATA2,
	                Data.DATA3, Data.RAW_CONTACT_ID, Data.CONTACT_ID, };
	    	
	    	Cursor c = resolver.query(Data.CONTENT_URI, PROJECTION2,
	    			Data.CONTACT_ID + "=?", new String[] {String.valueOf(contact_id)}, null);
	    	
	    	/*
			final Cursor c = resolver.query(Data.CONTENT_URI, DataQueryContactId.PROJECTION,
					DataQueryContactId.SELECTION, new String[] {String.valueOf(contact_id)}, null);	
			*/
			
			if(dbg)System.out.println("Beginne Auslesen des Kontaktes mit der _id: " + contact_id + " cursor.count: " + c.getCount());
			String ausgabe = "";
			
	        try {
	        	//if(c!=null)
	        	while (c.moveToNext()) {	   
	        		
	        	
	        		if(dbg){
		        			if(dbg)System.out.println(".\n.\n");
		        		for (int i = 0; i < c.getColumnCount(); i++) {	        	
		    	        	String Spaltenname = c.getColumnName(i);
		    	        	String Spalteninhalt = c.getString(i);
		    	        	if(Spalteninhalt!=null){
		    	        		System.out.println("* "+ Spaltenname + ": " + Spalteninhalt);
		    	        		ausgabe += Spaltenname + ": " + Spalteninhalt + "\n";
		    	        	}
		    	    	}
		        		if(dbg)System.out.println(".\n");
	        		}
	        		
	        		
	        		//final long id = c.getLong(DataQuery.COLUMN_ID);	        
	        	    //final String mimeType = c.getString(DataQuery.COLUMN_MIMETYPE);
	            	String mimeType = c.getString(DataQueryRawContactId.COLUMN_MIMETYPE);
	            	int thisRawContactId = c.getInt(c.getColumnIndex(Data.RAW_CONTACT_ID)); 
	            	
	            	
	            	if(gCards.get(thisRawContactId)==null){
	            		GevilVCard gCard = new GevilVCard(mMap);
	            		gCards.put(thisRawContactId, gCard);
	            		if(dbg)System.out.println("gelesene RawContactId: " + thisRawContactId + " diese wird als key der Hashmap verwendet!");
	            		gCards.get(thisRawContactId).belongsToAccount=false;
	            		gCards.get(thisRawContactId).setAndroidId(thisRawContactId);
	            	}
	            	

	                if (mimeType.equals(android.provider.ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {                	
	                	final String nachName = c.getString(DataQueryRawContactId.COLUMN_FAMILY_NAME);
	                    final String vorName = c.getString(DataQueryRawContactId.COLUMN_GIVEN_NAME);
	                    
	                    gCards.get(thisRawContactId).setN(vorName, nachName);
	                    gCards.get(thisRawContactId).setFN(vorName);	   
	                    
	                    if(dbg)System.out.println("gelesene name: " + gCards.get(thisRawContactId).getReadableName());
	                    
	                    
	                    //
	                    // wird der Kontakt von ContactEditor erstellt, fügt dieser die Kontaktinformationen
	                    // nicht MIMETYPE_UID ein, sondern direkt uneterm Name...
	                    //
	                    /*
	                    String accountName = c.getString( c.getColumnIndex("account_name")).trim();
		            	if(accountName.trim().equals(mAccountName.trim())){
		            		// da der Kontakt nicht zu diesem Account gehört: verwerfen.
		            		gCards.get(thisRawContactId).belongsToAccount = true;
		            		//if(dbg)
		            			System.out.println("gelesener AccountName:  " + accountName + " gehört SCHON zum SA.");
		            	}else{
		            		gCards.get(thisRawContactId).belongsToAccount = false;
		            		if(dbg)System.out.println("gelesener AccountName:   _" + accountName + "_ gehört nicht zum SA, weil mAccN: _" + mAccountName + "_");
		            	}
		            	*/
	                    
	                }else if (mimeType.equals(android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
	                	final int type = c.getInt(DataQueryRawContactId.COLUMN_PHONE_TYPE);
	                	String phoneNr = c.getString(DataQueryRawContactId.COLUMN_PHONE_NUMBER); 
	                	
	                	if(phoneNr!=null)
	                	if(phoneNr.trim()!=""){
	                		gCards.get(thisRawContactId).addPhone(phoneNr, type);       
	                	}
	                	
	                	
	                }else if (mimeType.equals(android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {  
	                	final int type = c.getInt(DataQueryRawContactId.COLUMN_EMAIL_TYPE);
	                	String email = c.getString(DataQueryRawContactId.COLUMN_EMAIL_ADDRESS);
	                	
	                	if(email!=null)
	                	if(email.trim()!=""){
		                	gCards.get(thisRawContactId).addEmail(email, type);	                	
	                	}

	                }else if (mimeType.equals(Constants.MIMETYPE_UID)) {
	                	
		            	String uid  = c.getString(DataQueryRawContactId.COLUMN_UNIQUE_ID).trim();
		            	String accountName = c.getString( DataQueryRawContactId.ACCOUNT_NAME).trim();	            	
		            	
		            	if(uid!=null)if(uid!=""){
		            		if(thisRawContactId == contact_id || gCards.get(thisRawContactId).getUid()==null){
		            			gCards.get(thisRawContactId).setUid(uid);
		            		}
			            }
		            	if(accountName.trim().equals(mAccountName.trim())){
		            		// da der Kontakt nicht zu diesem Account gehört: verwerfen.
		            		gCards.get(thisRawContactId).belongsToAccount = true;
		            		if(dbg)System.out.println("gelesener AccountName:  " + accountName + " gehört SCHON zum SA.");
		            	}else{
		            		gCards.get(thisRawContactId).belongsToAccount = false;
		            		if(dbg)System.out.println("gelesener AccountName:   _" + accountName + "_ gehört nicht zum SA, weil mAccN: _" + mAccountName + "_");
		            	}
	                }
	                
	            } // while

	        } catch (Exception e) {	        
	        	e.printStackTrace();
	        	if(debug!=null)debug.printStackTrace(e);
			} finally {	          
				try{ if(c!=null) c.close(); }catch(Exception e){
	        	   e.printStackTrace();
	           }
	        }
	        return gCards;
	    }
	  
	  
	  
	  
	  /*
      public GevilVCard OldreadContactFromPhone(Context mContext, int rawContactId, TypeBiMap map, InsertUpdateDeleteAndroidContacts iud) throws Exception{
	        if(mContext == null)return null;    	
	        boolean dbg = false;
	        
	        
	    	ContentResolver resolver = mContext.getContentResolver();
	        
		    String ausgabeText="";
		    
		    
		    HashMap<Integer, GevilVCard> gCards = new HashMap<Integer, GevilVCard>();
			//ArrayList<GevilVCard> gCards = new ArrayList<GevilVCard>();
			GevilVCard gCard = new GevilVCard(mMap);
			gCards.put(rawContactId, gCard);
			
			
			
	        // Durch ContactId werden (?) mehrere Kontakte direkt gejoint.
			final Cursor c = resolver.query(Data.CONTENT_URI, DataQueryContactId.PROJECTION,
					DataQueryContactId.SELECTION, new String[] {String.valueOf(rawContactId)}, null);	
			

			
			
			if(dbg)System.out.println("Beginne Auslesen des Kontaktes mit der _id: " + rawContactId);
				

			boolean FNadded = false;
			
	        try {	
	        	if(c!=null) while (c.moveToNext()) {
	        		
	        		//Intention: alle Spalten auslesen.
	        		
	        		if(dbg){
		        			if(dbg)System.out.println(".\n.\n");
		        		for (int i = 0; i < c.getColumnCount(); i++) {	        	
		    	        	String Spaltenname = c.getColumnName(i);
		    	        	String Spalteninhalt = c.getString(i);
		    	        	if(Spalteninhalt!=null)System.out.println("* "+ Spaltenname + ": " + Spalteninhalt);
		    	    	}
		        		if(dbg)System.out.println(".\n");
	        		}
	        		
	        		
	        		//final long id = c.getLong(DataQuery.COLUMN_ID);
	        		String mimeType = null;
	        		    //final String mimeType = c.getString(DataQuery.COLUMN_MIMETYPE);
	            	mimeType = c.getString(DataQueryContactId.COLUMN_MIMETYPE);
	            
	                //uri = android.content.ContentUris.withAppendedId(Data.CONTENT_URI, id);

	            	
	            	int thisRawContactId = c.getInt(c.getColumnIndex(Data.RAW_CONTACT_ID)); 
	            	
	            	if(rawContactId != thisRawContactId){
	            		System.out.println("rawContactId: " + rawContactId + " thisRCID " + thisRawContactId);
	            	}
	            	
	            	if(gCards.get(thisRawContactId)==null){
	            		// neuer Kontakt mit neuer RawContactId.
	            		gCard = new GevilVCard(mMap);
	            		gCards.put(thisRawContactId, gCard);
	            		if(dbg)System.out.println("gelesene RawContactId: " + thisRawContactId);
	            		gCards.get(thisRawContactId).belongsToAccount=false;
	            	}
	            	

	                if (mimeType.equals(android.provider.ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {                	
	                	final String nachName =
	                        c.getString(DataQueryRawContactId.COLUMN_FAMILY_NAME);
	                    final String vorName =
	                        c.getString(DataQueryRawContactId.COLUMN_GIVEN_NAME);
	                 
	                    //System.out.println("gelesen: " + vorName + ", " + nachName);                    
	                    
	                   // if(thisRawContactId == rawContactId){                    
	                    	gCards.get(thisRawContactId).setN(vorName, nachName);
	                    	gCards.get(thisRawContactId).setFN(vorName);                   
		                    
	                   // }else{
	                   // 	gCard.joinAndSetNames(new String[]{vorName, nachName}, gCard.getN());
	                   // }
	                    FNadded = true;	
	                    
	                }                     
	                else if (mimeType.equals(android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {     
	                	final int type = c.getInt(DataQueryRawContactId.COLUMN_PHONE_TYPE);
	                	String phoneNr = c.getString(DataQueryRawContactId.COLUMN_PHONE_NUMBER); 
	                	
	                	if(phoneNr!=null)
	                	if(phoneNr.trim()!=""){
	                		gCards.get(thisRawContactId).addPhone(phoneNr, type);       
	                	}
	                	//System.out.println("ReadContactsFromPhone. readPhone: " + phoneNr);                	
	                	//card.getProperties().add(androidToVCardPropertyConversion.parseTelephone(phoneNr, type));
	                    /*if (type == android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
	                        String cellPhone = c.getString(DataQuery.COLUMN_PHONE_NUMBER);                        
	                        ausgabeText += "\ncellPhone: " + cellPhone;                        
	                    }*//*
	                }                
	                else if (mimeType.equals(android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {  
	                	final int type = c.getInt(DataQueryRawContactId.COLUMN_EMAIL_TYPE);
	                	String email = c.getString(DataQueryRawContactId.COLUMN_EMAIL_ADDRESS);
	                	
	                	if(email!=null)
	                	if(email.trim()!=""){
		                	if(dbg)ausgabeText += "\nemail: " + email;
		                	if(dbg)System.out.println("ReadContactsFromPhone. email: " + email + " Type: " + type);
		                	
		                	gCards.get(thisRawContactId).addEmail(email, type);	                	
	                	}
	                    
	                    
	                    
	                    //card.getProperties().add(androidToVCardPropertyConversion.parseEmail(email, type));

	                }  //else if (mimeType.equals("vnd.android.cursor.item/vnd.fm.last.android.profile")) { 
	            	
	                else if (mimeType.equals(Constants.MIMETYPE_UID)) {
		            	
		            	String uid  = c.getString(DataQueryRawContactId.COLUMN_UNIQUE_ID).trim();
		            	String accountName = c.getString( DataQueryRawContactId.ACCOUNT_NAME).trim();	            	
		            	
		            	if(uid!=null)if(uid!=""){
		            		if(thisRawContactId == rawContactId || gCards.get(thisRawContactId).getUid()==null){
		            			gCards.get(thisRawContactId).setUid(uid);
		            		}
			            }
		            	
		            	if(accountName.trim().equals(mAccountName.trim())){
		            		// da der Kontakt nicht zu diesem Account gehört: verwerfen.
		            		gCards.get(thisRawContactId).belongsToAccount = true;
		            		if(dbg)System.out.println("gelesener AccountName:  " + accountName + " gehört SCHON zum SA.");
		            	}else{
		            		gCards.get(thisRawContactId).belongsToAccount = false;
		            		if(dbg)System.out.println("gelesener AccountName:   _" + accountName + "_ gehört nicht zum SA, weil mAccN: _" + mAccountName + "_");
		            	}
		            	
		                
	               
	                }
	                
	            } // while

	        } catch (Exception e) {
	        	System.out.println("*******************************\n"+ e + " Exception in reacContactFromPhone(0). 0:normaler try-catch block.");
	        	e.printStackTrace();
	        	debug.printStackTrace("contactsFromPhone1", e);
			} finally {
	           // TODO jetzt wird jedenfalls eine Nullpointer Exception vermieden.
			   // eigentlich kann man das so lassen ?!
				try{ if(c!=null) c.close(); }catch(Exception e){
	        	   System.out.println(e + " (3)Exception in reacContactFromPhone. 3:in finally");
	           }
	        }      
			//System.out.println("@@@ AccountName: " + ausgabeText + "\n \n========================");		
			
			
			GevilVCard retCard = this.joinVCards(gCards);
			if(retCard==null)return retCard;
			
			retCard.setAndroidId(rawContactId);
			
			
			// TODO retCard aus allen VCards zusammensetzten.
			
			if(!FNadded){
				if(dbg)System.out.println("kein Namen für VCard gefunden, _uid: " + retCard.getUid());
				return null;
			}
			

			
			//String genUID = "AndroidId:" + rawContactId;
			
			String genUID;
			
			try{
			if(retCard.getUid() == null){
				genUID = retCard.generateUniqueId(mContext).toString();
				System.out.println("getUid==null, generate UID: " + genUID + " für: " + retCard.getN()[0]);
				retCard.setUid(genUID);
				if(dbg)System.out.println(".+ readContactFromPhone UID == null, deshalb setUID: " + genUID + " " + retCard.getN()[1]);
				iud.updateContact(retCard, retCard.getAndroidId());
				iud.apply();
			}else if(TextUtils.isEmpty(retCard.getUid())){
				genUID = retCard.generateUniqueId(mContext).toString();
				System.out.println("getUid is empty, generate UID: " + genUID + " für: " + retCard.getN()[0]);
				retCard.setUid(genUID);
				if(dbg)System.out.println(".+ readContactFromPhone UID is empty, deshalb setUID: " + genUID + " " + retCard.getN()[1]);
				iud.updateContact(retCard, retCard.getAndroidId());
				iud.apply();
			}
			}catch(URISyntaxException e){
				System.out.println("=> Fehler beim Zuweisen der UID: " + e);
				//System.out.println("=> es sollte zugewiesen werden: " + genUID);
			}
			
			retCard.setAndroidId(rawContactId);
			
	        return retCard;
	    }
    
    */
    
    /**
     * Gibt einen Iterator für Handykontakte zurück
     * @param context
     * @return
     */
    public ContactsFromPhoneReader getContactsPhoneReader(Context context, TypeBiMap map, String accountName){
    	return new ContactsFromPhoneReader(context, map);
    }  
	
	public class ContactsFromPhoneReader implements java.util.Iterator<GevilVCard>{
		Cursor cur;
		
		private Context mContext;
		private TypeBiMap mMap;
		GevilVCard nextGevilVCard = new GevilVCard(mMap);

		private InsertUpdateDeleteAndroidContacts iud;

		public ContactsFromPhoneReader(Context context, TypeBiMap map){
			this.mContext = context;
			this.mMap = map;
			this.iud = new InsertUpdateDeleteAndroidContacts(mMap, context, mAccountName);
			
			String[] projection = new String[]{"account_type", "account_name", "contact_id", "_id", "display_name", "deleted"};
        	
        	cur = mContext.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,  projection,
        		   ContactsContract.RawContacts.ACCOUNT_TYPE + "=?", new String[]{Constants.ACCOUNT_TYPE}, null);        
        	
		}
		
		
		@Override
		public boolean hasNext() {
			boolean dbg = false;
			Boolean endLoop = false;
			
        	while(cur.moveToNext() && !endLoop){			
        		int deleted = cur.getInt(cur.getColumnIndex("deleted"));   
        		String contactAccountName = cur.getString(cur.getColumnIndex("account_name"));
        		/*
        		String vorName = cur.getString(cur.getColumnIndex("display_name"));
	        	String nachName = cur.getString(cur.getColumnIndex("display_name"));
        		*/
	        		if(deleted==0 && mAccountName.equals(contactAccountName)){        			
		        	//
		        	// TODO auslesen und zurückgeben.
		        	//
	        		
	        		int rawContactId = cur.getInt(cur.getColumnIndex(ContactsContract.RawContacts._ID));
	        		int android_id = cur.getInt(cur.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
	        			
	        		
	        		//if(dbg)System.out.println("lese den Kontakt mit der rawContactId: " + rawContactId + " android_id: " + android_id + " " + vorName + " " + nachName);
	        		nextGevilVCard=readContactByRawContactId(mContext, android_id, rawContactId, mMap, iud);
	        			
	        		endLoop=true;
	        		return true;
	        		}else{
	        			if(!mAccountName.equals(contactAccountName)){
	        				//System.out.println(vorName + " " + nachName + " wird nicht gelesen weil " + mAccountName + " != " + contactAccountName);
	        			}
	        		}
	        	
        	}
        	
        	// alle Kontakte durchsucht, keiner Gefunden.
			return false;
		}

		@Override
		public GevilVCard next() {        	
        	return nextGevilVCard;
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}


		public void setKonsolenausgabe(Konsolenausgabe debug2) {
			debug = debug2;
			
		}
		
	}	

	
	/**
     * Constants for a query to get contact data for a given rawContactId
     */
    private interface DataQueryRawContactId {
        public static final String[] PROJECTION =
            new String[] {Data._ID, Data.MIMETYPE, Data.DATA1, Data.DATA2,
                Data.DATA3,};
        
        public static final String SELECTION = Data.RAW_CONTACT_ID + "=?";
        
        
        public static final int COLUMN_ID = 0;
        public static final int COLUMN_MIMETYPE = 1;
        public static final int COLUMN_DATA1 = 2;
        public static final int COLUMN_DATA2 = 3;
        public static final int COLUMN_DATA3 = 4;
        public static final int COLUMN_PHONE_NUMBER = COLUMN_DATA1;
        public static final int COLUMN_PHONE_TYPE = COLUMN_DATA2;
        public static final int COLUMN_EMAIL_ADDRESS = COLUMN_DATA1;
        public static final int COLUMN_EMAIL_TYPE = COLUMN_DATA2;
        public static final int COLUMN_GIVEN_NAME = COLUMN_DATA2;
        public static final int COLUMN_FAMILY_NAME = COLUMN_DATA3;
        public static final int ACCOUNT_NAME = COLUMN_DATA1;
        public static final int COLUMN_UNIQUE_ID = COLUMN_DATA3;
    }
    
	
    

	public void setKonsolenausgabe(Konsolenausgabe debug) {
		this.debug = debug;
		
	}
    
    
}
