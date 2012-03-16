package com.gevil.AndroidContacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import net.fortuna.ical4j.vcard.Property;


import com.gevil.AndroidContacts.readAndroidContacts.ContactsFromPhoneReader;
import com.gevil.syncadapter.Constants;

import syncZustand.GevilVCard;
import syncZustand.TypeBiMap;
import syncZustand.GevilVCardHash.PropertyByValueSearcher;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.provider.ContactsContract.Data;

public class InsertUpdateDeleteAndroidContacts {
		
	 
	 private TypeBiMap mMap;
	 private Context mContext;	 
	 private String AccountName;
	 private final ContentValues mValues;
	 
	 // ops.size vor Einfügen eines neuen Kontaktes.
	 int mBackReference;
	 
	 
	 
	 private ArrayList<ContentProviderOperation> ops;
	
	 
	 public InsertUpdateDeleteAndroidContacts(TypeBiMap mMap, Context context, String accountName) {	
		if(mMap == null){
			throw new NullPointerException();
		}
		 
		 this.mMap = mMap;
		this.mContext = context;
		AccountName = accountName;
			
		mValues = new ContentValues();
		ops = new ArrayList<ContentProviderOperation>();
	}

	
	 /**
	  * Einfügen eines bisher nicht vorhandenen Kontakt ins Android Adressbuch.
	  * @param context
	  * @param tCard  GevilVCard die den einzufügenden Account enthält
	  */
	 public void insert(GevilVCard tCard){
		 	boolean dbg = true;
		 	tCard.initMmap(mMap);
		 	
		 	mBackReference = ops.size();
		 	
		 	if(TextUtils.isEmpty(tCard.getN()[0]) &&   TextUtils.isEmpty(tCard.getN()[1])){
		 		System.out.println(tCard.getN()[0] + "  " + tCard.getN()[1]);
		 		
		 		throw new ArrayIndexOutOfBoundsException();
		 	}
		 	
		 	//
		 	// Es handelt sich um einen Gevil-Sync Account.
		 	//
		 	this.addAccount();		
		 	
		 	
		   	//
		 	// Vor- und Nachname einfügen.
		 	//
		 	this.addName(tCard);
		 	
		 	
		 	
		 	//
	        // Emailadresse(n) einfügen
	        //			 	
		 	PropertyByValueSearcher emailByValue = tCard.getHash().propertiesByValue(net.fortuna.ical4j.vcard.Property.Id.EMAIL);
		    Property emailProperty = emailByValue.getUninsertedPropertyOnce();
		    
		    while(emailProperty!=null){
			    String emailAdresse = emailProperty.getValue();
			    if(emailAdresse.trim() != ""){
				    try{				   
					   	
				    	if(tCard == null)System.out.println("Exc.Reason(1)");
				    	if(emailProperty == null)System.out.println("Exc.Reason(2)");
				    	if(mMap == null)System.out.println("Exc.Reason(3)");
				    	
				    	
				    	int androidEmailType=tCard.propertyToAndroidType(emailProperty, mMap.mailType(), true);					    
					    if(dbg)System.out.println("Einfügen ins Android AB: " + emailAdresse + " typ: " + androidEmailType + "\n.\n");						 
					    this.addEmail(emailAdresse,  androidEmailType, 0, true);
				    }catch(ArrayIndexOutOfBoundsException e){
				    	System.out.println(e + " bei " + emailAdresse);
				    	e.printStackTrace();
				    }
			    }		        		
			    // nächster Schleifendurchlauf
			    emailProperty = emailByValue.getUninsertedPropertyOnce();		    
		    }
		    emailByValue = null;
		    emailProperty = null;
		    
		 	 
		 	

		    
		    //
		    // Telefonnummern einfügen
		    //
		    
		    PropertyByValueSearcher phoneByValue = tCard.getHash().propertiesByValue(net.fortuna.ical4j.vcard.Property.Id.TEL);
		    Property phoneProperty = phoneByValue.getUninsertedPropertyOnce();
		    
		    while(phoneProperty!=null){
			    String phoneNumber = phoneProperty.getValue();
			    if(phoneNumber.trim() != ""){
				    try{				    
					    net.fortuna.ical4j.vcard.parameter.Type vCardPhoneType;
				    	if(phoneProperty.getParameters().size() > 0){
				    		 vCardPhoneType = new net.fortuna.ical4j.vcard.parameter.Type(phoneProperty.getParameters().get(0).getValue());
					    }else{
					    	vCardPhoneType = new net.fortuna.ical4j.vcard.parameter.Type("HOME");
					    }				    	
					    
				    	int androidPhoneType = mMap.toAndroidType(mMap.phoneType(), vCardPhoneType);
					    if(dbg)System.out.println("Einfügen ins Android AB: " + phoneNumber + " typ: " + androidPhoneType + "\n.\n");	
					    
					
					    
					    // TODO addPhone aktualisieren.
					    this.addPhone(phoneNumber, androidPhoneType, 0, true);
					    
				    }catch(ArrayIndexOutOfBoundsException e){
				    	System.out.println(e + " bei " + phoneNumber);
				    	e.printStackTrace();
				    }
			    }
		        		
			    // nächster Schleifendurchlauf
			    phoneProperty = phoneByValue.getUninsertedPropertyOnce();		    
		    }
		    
		    ////////
		    		 	
		    this.addUniqueId(tCard.getUid(), AccountName, 0, true);
		 	

		    if(ops.size()>50){
		    	apply();
		    }
	 }
	
	 
	 
	 /**
	  * Update eines bestehenden Kontaktes im Android Adressbuch
	  * @param context
	  * @param tCard		damit wird der Kontakt überschrieben.
	  * @param rawContactId	_id des Kontaktes der upgedatet werden soll.
	  */
	 public void updateContact(GevilVCard tCard, int rawContactId) {
	 		Uri uri = null;	     
	 		boolean dbg = true;
	        
	 		tCard.initMmap(mMap);

	        ContentResolver resolver = mContext.getContentResolver();
	        
	        final Cursor c =
	            resolver.query(Data.CONTENT_URI, DataQueryContactId.PROJECTION,
	            		DataQueryContactId.SELECTION,
	                new String[] {String.valueOf(rawContactId)}, null);
	        
	        if(c.getCount() < 1){
	        	throw new ArrayIndexOutOfBoundsException();
	        }
	        
	        //PropertyByTypeSearcher phoneByType = tCard.getHash().propertiesByType(net.fortuna.ical4j.vcard.Property.Id.TEL);
	        PropertyByValueSearcher phoneByValue = tCard.getHash().propertiesByValue(net.fortuna.ical4j.vcard.Property.Id.TEL);
	        PropertyByValueSearcher emailByValue = tCard.getHash().propertiesByValue(net.fortuna.ical4j.vcard.Property.Id.EMAIL);
         
	        boolean uidAktualisiert = false;          
         
	        try {
	        	while (c.moveToNext()) {
	        		final long id = c.getLong(DataQueryContactId.COLUMN_ID);	        		
	                final String mimeType = c.getString(DataQueryContactId.COLUMN_MIMETYPE);
	                uri = ContentUris.withAppendedId(Data.CONTENT_URI, id);
	                
	                

	                //
	                // Vor- und Nachname
	                //
	                if (mimeType.equals(StructuredName.CONTENT_ITEM_TYPE)) {
	                    /*
	                	final String lastName =
	                        c.getString(DataQuery.COLUMN_FAMILY_NAME);
	                    final String firstName =
	                        c.getString(DataQuery.COLUMN_GIVEN_NAME);
	                    
	                    String newName[] = tCard.getN();
	                    */
	                    
	                    if(dbg)System.out.println("> Namensupdate. rawContactId: " + rawContactId + " .. " + tCard.getReadableName());
	                    this.updateName(tCard, uri, rawContactId);		                    
	                }
	                
	                //
	                // Telefonnummern
	                //
	                else if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
	                    final int type = c.getInt(DataQuery.COLUMN_PHONE_TYPE);
	                    final String phone = c.getString(DataQuery.COLUMN_PHONE_NUMBER);
	                                   
	                    Property phoneProperty = phoneByValue.getPropertyByValueOnce(phone);	
	                    if(phoneProperty==null){			                    
	                    	//phoneProperty = tCard.getHash().getUnmarkedProperty(phoneByValue, phoneByType);
	                    	phoneProperty = phoneByValue.getUninsertedPropertyOnce();
	                    }		                   
	                    if(phoneProperty==null){
	                    	// keine Telefonnr. gefunden die eingetrage werden könnte.
	                    	// Deshalb: nummber löschen.
	                    	//this.updatePhone(null, uri, type, rawContactId);
	                    	this.deleteUri(uri);
	                    }else{
	                    	int insertType = tCard.propertyToAndroidType(phoneProperty, mMap.phoneType(), true);		                    	                    	
	                    	this.updatePhone(phoneProperty.getValue(), uri, insertType, rawContactId);
	                    }
	                }

	                //
	                // Emailadressen
	                //
	                else if (mimeType.equals(android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) { 
	                	final String email = c.getString(DataQuery.COLUMN_EMAIL_ADDRESS); 
	                    final int phoneABtype = c.getInt(DataQuery.COLUMN_EMAIL_TYPE);
	                    
	                    Property emailProperty = emailByValue.getPropertyByValueOnce(email);
	                    if(emailProperty==null){
	                    	emailProperty = emailByValue.getUninsertedPropertyOnce();
	                    }
	                    
						if(emailProperty!=null){
	                    	int androidEmailType=tCard.propertyToAndroidType(emailProperty, mMap.mailType(), true);
	                    	updateEmail(emailProperty.getValue(), uri, androidEmailType, rawContactId);	
	                    }else{
	                    	// Emailadresse löschen.
	                    	//updateEmail(null, uri, phoneABtype, rawContactId);
	                    	deleteUri(uri);
	                    }

	                }
	                //
	                // UID
	                //
	                else if (mimeType.equals(Constants.MIMETYPE_UID)) {
	                	String uid  = c.getString(DataQuery.COLUMN_UNIQUE_ID);
	                	
	                	if(!tCard.getUid().equals(uid)){
	                		System.out.println("versuche unterschiedliche UIDs ins Android AB einzufügen, bisher: " + uid + " versuche einzufügen: " + tCard.getUid());
	                		// Der Fall, dass ein Kontakt der noch keine UID besitzt aktualisiert wird
	                		// ist momentan nicht berücksichtigt.
	                		throw new ArrayIndexOutOfBoundsException();
	                	}else{
	                		uidAktualisiert = true;
	                	}
	                		
	                	
	                }
	            } // while
	        } finally {
	            c.close();
	        }
	        
	        
	        //
	        // Jede Emailadresse die nicht upgedatet wurde, wird jetzt eingefügt.
	        // Notwendig wenn eine neue Adresse hinzugefügt wird. Hier ist, auch wen der Kontakt schon
	        // existiert, ein insert anstatt eines updates notwendig.
	        //
		    Property insertEmail = emailByValue.getUninsertedPropertyOnce();
		    while(insertEmail!=null){			    		
		    	int insertType = tCard.propertyToAndroidType(insertEmail, mMap.mailType(), true);			    	
		    	this.addEmail(insertEmail.getValue(), insertType, rawContactId, false);			    	
		    	insertEmail = emailByValue.getUninsertedPropertyOnce();
		    }	
		    
		    
		    //
		    //  zusätzliche Telefonnummern eintragen
		    //
         //Property insertPhone = tCard.getHash().getUnmarkedProperty(phoneByValue, phoneByType);
		    Property insertPhone = phoneByValue.getUninsertedPropertyOnce();
		    while(insertPhone!=null){			    		
		    	int insertType = tCard.propertyToAndroidType(insertPhone, mMap.phoneType(), true);			    	
		    	this.addPhone(insertPhone.getValue(), insertType, rawContactId, false);			    	
		    	insertPhone = phoneByValue.getUninsertedPropertyOnce();
		    }
         
		    
		    
		    //
		    // ggf. neue UID einfügen.
		    //
		    if(!uidAktualisiert){
		    	this.addUniqueId(tCard.getUid(), AccountName, rawContactId, false);
		    }

	        
	        if(ops.size()<50){
	        	apply();
	        }

	    }
	 
	 
	 private void deleteUri(Uri d){
		 
		 ContentProviderOperation.Builder builder;				
		 
		 //System.out.println("gelöscht wird: " + rawContactId);
		 //Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, rawContactId);
	
		 
		d.buildUpon().appendQueryParameter(
		            ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
		 
		builder =  ContentProviderOperation.newDelete(d);
		ops.add(builder.build());
		  
		apply();
		 
	 }
	 
	 public void delete(int rawContactId){	
		

		 
		 if(rawContactId == -1)throw new ArrayIndexOutOfBoundsException();
		//newDelete(rawContactId);
		
		// DOCH NICHT??? ich habe RawContacts nach android.provider.ContactsContract.Contacts geändert.
		// Jetzt werden auch zusammengefasste Kontakte gelöscht.
		 
		 ContentProviderOperation.Builder builder;				
		 
		 //System.out.println("gelöscht wird: " + rawContactId);
		 //Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, rawContactId);
		 Uri uri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
		 
		 uri.buildUpon().appendQueryParameter(
		            ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
		 
		builder =  ContentProviderOperation.newDelete(uri);
		ops.add(builder.build());
		  
		apply();

	 }
	 
	 

	 
	 
	 
		private Hashtable<String, Integer> find_IdHash = new Hashtable<String, Integer>();
		/**
		 * Durchsucht das Android AB nach der, zu einer UID korrespondierenden
		 * Android_id.
		 * @param UID
		 * @return
		 */
		public int findAndroidId(String UID){
			if(find_IdHash == null) find_IdHash = new Hashtable<String, Integer>();
			if(UID==null)throw new NullPointerException();
			
			Integer x = find_IdHash.get(UID);
			if(x!=null){
				return x;
			}else{	
				readAndroidContacts rac = new readAndroidContacts(mMap, AccountName);
				ContactsFromPhoneReader cit = rac.getContactsPhoneReader(mContext, mMap, AccountName);
		
				while(cit.hasNext()){
					GevilVCard c = cit.next();				
					//if(find_IdHash.get(c.getUid()) == null){
						find_IdHash.put(c.getUid(), c.getAndroidId());
					//}
				}
				Integer ret = find_IdHash.get(UID);
				if(ret == null) throw new ArrayIndexOutOfBoundsException();
				if(ret <0){
					System.out.println("kein Eintrag im AB für " + UID + " gefunden.");
					throw new ArrayIndexOutOfBoundsException();
					
					}
				return ret;
			}
		}
	 
	 //////////////////////////////
	 
	 /*
	 private void updateUniqueId(String uid, String username, Uri inuri, int RawContactId){
		 Uri uURI = inuri.buildUpon().appendQueryParameter(
		          ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();	
		 
		 
		 ContentProviderOperation.Builder builder;
		 builder = ContentProviderOperation.newUpdate(uURI);
		 
		 
		 //Create a Data record of custom type "vnd.android.cursor.item/vnd.fm.last.android.profile" to display a link to the Last.fm profile
		 builder.withValue(Phone.RAW_CONTACT_ID, RawContactId);		 
		 //builder.withValue(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/vnd.fm.last.android.profile");
		 builder.withValue(ContactsContract.Data.MIMETYPE, Constants.MIMETYPE_UID);
		 
		 	
		 if(username!=null){
			 builder.withValue(ContactsContract.Data.DATA1, username);		 
			 builder.withValue(ContactsContract.Data.DATA2, "Gevil-Sync Profil");
		 }
		 
		
		 builder.withValue(ContactsContract.Data.DATA3, uid);
		 //builder.withValue(ContactsContract.Data.DATA4, uid);		 		 
		 ops.add(builder.build());
	 }	
	*/
	 
	 
	 /**
	  * 
	  * @param uid
	  * @param username
	  * @param RawContactId
	  * @param newContact
	  */
	 private void addUniqueId(String uid, String username, int RawContactId, boolean newContact){
		if(uid==null){
			return;
		}else if(TextUtils.isEmpty(uid)){
			return;
		}
		 

		 ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
		 if(newContact){
			 builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, mBackReference);		
		 }else{
			 // BackReference wird genommen, wenn es ein neuer Kontakt ist.
			 // Kontakt besteht schon => kein BackReference
			 //
			 // Samplesyncadapter benutzt auch die RawContactId.		 
			 builder.withValue(ContactsContract.Data.RAW_CONTACT_ID, RawContactId);
		 }
		 
		 builder.withValue(ContactsContract.Data.MIMETYPE, Constants.MIMETYPE_UID);
		 
		 if(username!=null){
			 // ... ungenutzt
			 builder.withValue(ContactsContract.Data.DATA1, username);		 
			 builder.withValue(ContactsContract.Data.DATA2, "Gevil-Sync Profil");
		 }
		 
		 // .. ungenutzt
		 builder.withValue(ContactsContract.Data.DATA3, uid);
		 //builder.withValue(ContactsContract.Data.DATA4, uid);		 		 
		 ops.add(builder.build());
	 }

	 
	 
	 /**
	  * wenn ein neuer Kontakt in Android eingefügt wird, kann hier der AccountTyp festgelegt werden.
	  * @param AccountName Name des Accounts
	  * @param AccountType Typ des Accounts
	  */
	 private void addAccount(){
		 ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)	
			   .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, AccountName)
			   .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE) 
			   .build());
	 }
	 
	 
	 /**
	  * Einfügen eines Namens zu einem neu eingeüften Kontakt.
	  * @param card die den Namen enthält.
	  */
	 private void addName(GevilVCard card){	
			 
		 ContentProviderOperation.Builder builder;
		 
		 builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);		 
		 
		 	
		 builder.withValue(ContactsContract.Data.MIMETYPE,  ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
		 
		 String[] vorNachName = card.getN();		 
		 
		 builder.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, vorNachName[0]);
		 builder.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, vorNachName[1]);
		 
		 builder.withValueBackReference(Phone.RAW_CONTACT_ID, mBackReference);	
		 
		 ops.add(builder.build());     
		 

	 }
	 
	 /**
	  * Fügt eine neue Emailadresse ins Android AB ein.
	  * 
	  * @param emailAdresse einzufügende EmailAdresse.
	  * @param androidType EMAIL_HOME, EMAIL_WORK, etc.
	  * @aram newContact gibt an, ob etwas in einen bestehenden Kontakt eingfügt wird, oder ob der Kontakt neu ist.
	  */
	 private void addEmail(String emailAdresse, int androidType, int RawContactId, boolean newContact){	
		 
		 Uri uri = Data.CONTENT_URI;
		 uri.buildUpon().appendQueryParameter(
		            ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
		 
		 ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
		 builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE); 
		 builder.withValue(ContactsContract.CommonDataKinds.Email.DATA, emailAdresse);
		 builder.withValue(ContactsContract.CommonDataKinds.Email.TYPE, androidType);
		 System.out.println("Als Emailtyp für " + emailAdresse + " wird " + androidType + " eingetragen.");
		 
		 
		 
		 if(newContact){
			 builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, mBackReference);		
		 }else{
			 // BackReference wird genommen, wenn es ein neuer Kontakt ist.
			 // Kontakt besteht schon => kein BackReference
			 //
			 // Samplesyncadapter benutzt auch die RawContactId.		 
			 builder.withValue(ContactsContract.Data.RAW_CONTACT_ID, RawContactId);	
		 }
		 
		 ops.add(builder.build()); 
	 }	
	 
	 
	 
	 /**
	  * fügt eine Telefonnummer in einen neu einzufügenden Kontakt ein
	  */
	 private void addPhone(String phoneNumber, int androidPhoneType, int RawContactId, boolean newContact){	
		 Uri uri = ContactsContract.Data.CONTENT_URI;
		 Uri uURI = uri.buildUpon().appendQueryParameter(
		          ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();	
		             
		 ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
		 
		 if(!newContact){
			 // BackReference wird genommen, wenn es ein neuer Kontakt ist.
			 // Kontakt besteht schon => kein BackReference
			 //
			 // Samplesyncadapter benutzt auch die RawContactId.		 
			 builder.withValue(ContactsContract.Data.RAW_CONTACT_ID, RawContactId);	
		 }
		 
	     builder.withValue(Phone.NUMBER, phoneNumber); 	    
	     builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, androidPhoneType);
		 /*
	     builder.withValue(ContactsContract.Data.MIMETYPE,
	             ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
		 
		 
		 */
	     builder.withValue(Phone.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
	     
		 if(newContact){
			 builder.withValueBackReference(Data.RAW_CONTACT_ID, mBackReference);		
		 }
	
		 

		 ops.add(builder.build());       
	 }
	 
	 
	 
	 
	 private void updateName(GevilVCard card, Uri inuri, int rawContactID){		 
		 // 
		 // das ist, warum auch immer, die URI fürs Update.
		 //
		 Uri uURI = inuri.buildUpon().appendQueryParameter(
		          ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();			 
			 
		 ContentProviderOperation.Builder builder;
		 builder = ContentProviderOperation.newUpdate(uURI);
		 
		 /*
		 builder.withValue(Phone.RAW_CONTACT_ID, rawContactID);		
		 builder.withValue(ContactsContract.Data.MIMETYPE,  ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
		 */
		 
		 String[] vorNachName = card.getN();
		 
		 builder.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, vorNachName[0]);
		 builder.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, vorNachName[1]);
		 ops.add(builder.build());       
	 }
	 
	 

	 
	 

	 
	 
	 private void updateEmail(String emailAdresse, Uri inuri, long androidEmailType, int contactID){		 
		 // 
		 // das ist, warum auch immer, die URI fürs Update.
		 //
		 Uri uURI = inuri.buildUpon().appendQueryParameter(
		          ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();	
			 
		 ContentProviderOperation.Builder builder;
		 
		 builder = ContentProviderOperation.newUpdate(uURI);
		 
		
		
		// builder.withValue(ContactsContract.Data.MIMETYPE,
         //        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);						   
                 
		 /*
		 builder.withValue(Email.RAW_CONTACT_ID, contactID);
		 
		 builder.withValue(ContactsContract.Data.MIMETYPE,
		 	ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
		 */
	
		 builder.withValue(ContactsContract.CommonDataKinds.Email.DATA, emailAdresse);  		 
		// INSTEAD OF   builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, emailAdresse);  
		                
		 builder.withValue(ContactsContract.CommonDataKinds.Email.TYPE, androidEmailType);                
		// INSTEAD OF builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,	androidEmailType);
                      //ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
						
		 ops.add(builder.build());       
	 }

	 
	 
	 
	 

	
	 
	 /*	 
	 private void BackupOfOldaddPhone(GevilVCard card, int androidPhoneType){	
		 
		 // Beim Einfügen eines neuen Kontaktes ist die ID immer 0.	 
		 int RawContactId = 0;
		 
		 // Das Mapping von androidPhoneType nach VCardPhoneType wird von GevilVCard übernommen.
		 int VCardPhoneType = androidPhoneType;
		 int index = 0;
		 
			 
		 ContentProviderOperation.Builder builder;
		 builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
		 builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, RawContactId) ;     
		
		 builder.withValue(ContactsContract.Data.MIMETYPE,
	             ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);						   
	            
	     builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, card.getPhone(index));                   
	     builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, VCardPhoneType);
		 ops.add(builder.build());       
		 }
	 */
	 /**
	  * ändert die Telefonnummer in einem vorhandenen Kontakt.
	  * @param card
	  * @param inuri
	  * @param androidPhoneType
	  */
	 private void updatePhone(String phone, Uri inuri, long androidPhoneType, int contactID){		 
		 // 
		 // das ist, warum auch immer, die URI fürs Update.
		 //
		 Uri uURI = inuri.buildUpon().appendQueryParameter(
		          ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();	
		  			

		// Das Mappint von androidPhoneType nach VCardPhoneType wird von GevilVCard übernommen.
		 long VCardPhoneType = androidPhoneType;
		 
		 //
		 // TODO wenn mehrere Nummern gespeichert sind, hat jede einen Index.
		 //
		 int index = 0;
		 
			 
		 ContentProviderOperation.Builder builder;
		 builder = ContentProviderOperation.newUpdate(uURI);
		 
		 /*
		 builder.withValue(Phone.RAW_CONTACT_ID, contactID);		
		 builder.withValue(ContactsContract.Data.MIMETYPE,
                 ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);						   
         */ 
                
         builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);                   
         builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, VCardPhoneType);
		 ops.add(builder.build());       
	 }
	 
	 

	 
	    public void apply() {
	        if (ops.size() == 0) {
	            return;
	        }
	       
	        try {
	           mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);	           
	        } catch (final OperationApplicationException e) {
	            e.printStackTrace();
	        } catch (final RemoteException e) {
	        	e.printStackTrace();
	        }
	        ops.clear();
	    }
	    
	    
	    
	    
	    
	    /**
	     * Constants for a query to get contact data for a given rawContactId
	     */
	    private interface DataQuery {
	        

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

	        public static final int COLUMN_UNIQUE_ID = COLUMN_DATA3;
	        
	        
	    }
	    
	    /**
	     * Constants for a query to get contact data for a given rawContactId
	     */
	    private interface DataQueryContactId {
	        public static final String[] PROJECTION =
	            new String[] {Data._ID, Data.MIMETYPE, Data.DATA1, Data.DATA2,
	                Data.DATA3, Data.RAW_CONTACT_ID, Data.CONTACT_ID, "dirty"};
	        
	        //public static final String SELECTION = Data.CONTACT_ID + "=?";
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
	        //public static final int RAW_CONTACT_ID = 4;
	        
	        public static final int COLUMN_UNIQUE_ID = COLUMN_DATA3;
	    }

	 
}
