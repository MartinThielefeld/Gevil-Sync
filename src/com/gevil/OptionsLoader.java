/**
 * http://de.wikibooks.org/wiki/Java_Standard:_Muster_Singleton
 */

package com.gevil;

import android.content.Context;
import android.content.SharedPreferences;

public class OptionsLoader {
	
	
	private static final String CONTACT_PHONEWINS = "vvvcontactsPhoneWinsad";
	private static final String CALENDAR_PHONEWINS = "vvvContactServerURLb";		
	private static final String urlCalendar = "vvvCalendarServerURLcd";
	private static final String urlContact =  "vvvContactServerURLd";
	
	
	notifications mNotification;

	private options_activity mOptions_activity;
	
	  // Eine (versteckte) Klassenvariable vom Typ der eigenen Klasse
	  private static OptionsLoader instance;
	  
	  // Verhindere die Erzeugung des Objektes über andere Methoden
	  private OptionsLoader () {}
	  
	  
	  public static OptionsLoader getInstance () {
	    if (OptionsLoader.instance == null) {
	      OptionsLoader.instance = new OptionsLoader ();
	    }
	    return OptionsLoader.instance;
	  }
	  
	  public notifications getNotificationActivity(Context ctx, String userName){
		  if(mNotification==null){
			  mNotification=new notifications();			  
			  String Tabellenname = "tblConflicts" + userName;
			  mNotification.setData(ctx, Tabellenname);
		  }
		  return mNotification;
	  }
	  
	  
	  public options_activity getOptionsnActivity(Context ctx, String userName){
		 if(mOptions_activity==null){
			 mOptions_activity = new options_activity();
			 mOptions_activity.setData(ctx, userName);
		 }		 
		 return mOptions_activity;
		 
		
	  }
	  
	  
	  
	  private boolean contactNotifiyChange = true;
	  boolean CalendarNotifiyChange;
	  String cachedContactsServerURL;
	  String cachedCalendarServerURL;
	  boolean chachedContactPhoneWins;
	  boolean contactFirstStart = true;
	  boolean calendarFirstStart = true;;
	  boolean chachedCalendarPhoneWins;
	  
	  public String getContactsServerUrl(Context ctx, String userName){
		  if(contactNotifiyChange || cachedContactsServerURL==null){
			  try{
				  // aus SQL neu laden
				  //ptions_activity o = getOptionsnActivity(ctx, userName);
				  contactNotifiyChange = false;
				  //cachedContactsServerURL =  o.getContactsServerURL();
				  cachedContactsServerURL =  readContactsServerURL(userName, ctx);
				  return cachedContactsServerURL;
			  }catch(RuntimeException e){
				  e.printStackTrace();
				  return "http://192.168.2.103/groupdav/Contacts";
			  }
		  }else{
			  // aus Tabelle nehmen
			  return cachedContactsServerURL;
		  }
	  }
	  
	  
	  
	  public String getCalendarServerUrl(Context ctx, String userName){		 
			if(contactNotifiyChange || cachedCalendarServerURL==null){
				try{  
					// aus SQL neu laden
				  //options_activity o = getOptionsnActivity(ctx, userName);
				  contactNotifiyChange = false;
				  //cachedCalendarServerURL =  o.getCalendarServerURL();
				  cachedCalendarServerURL =  readCalendarServerURL(userName, ctx);
				  return cachedCalendarServerURL;
				} catch(RuntimeException e){
					  e.printStackTrace();
					  return "http://192.168.2.103/groupdav/Calendar";
				}
			  }else{
				  // aus Tabelle nehmen
				  return cachedCalendarServerURL;
			  }
	  }
	  
	  
	  public boolean getContactPhoneWins(Context ctx, String userName){		 
			if(contactNotifiyChange || contactFirstStart){
				  // aus SQL neu laden
				  //options_activity o = getOptionsnActivity(ctx, userName);
				  contactNotifiyChange = false;
				  contactFirstStart = false;
				  //chachedContactPhoneWins = o.getContactPhoneWins();
				  chachedContactPhoneWins = readContactPhoneWins(userName, ctx);
				  return chachedContactPhoneWins;
			  }else{
				  // aus Tabelle nehmen
				  return chachedContactPhoneWins;
			  }
	  }
	   
	public void notifiyChange() {
		contactNotifiyChange = true;		
		CalendarNotifiyChange = true;
	}
	
	
	public boolean getCalendarPhoneWins(Context mContext, String mUsername) {		
		if(CalendarNotifiyChange || calendarFirstStart){
			  // aus SQL neu laden
			  //options_activity o = getOptionsnActivity(mContext, mUsername);
			  CalendarNotifiyChange = false;
			  calendarFirstStart = false;
			  //chachedCalendarPhoneWins = o.getCalendarPhoneWins();
			  chachedCalendarPhoneWins = readCalendarPhoneWins(mUsername, mContext);
			  return chachedCalendarPhoneWins;
		  }else{
			  // aus Tabelle nehmen
			  return chachedCalendarPhoneWins;
		  }
	}
	
	
	
	/*
	 * Ab hier: eingefügt.
	 * 
	 * 
	 */
	
	
	
	private String readContactsServerURL(String mUsername, Context mContext) {
		
		if(mUsername==null)throw new ArrayIndexOutOfBoundsException();
		if(mContext==null)throw new ArrayIndexOutOfBoundsException();
		
		
		
		
		String pfad;
		//try{
			SharedPreferences prefs = mContext.getSharedPreferences(mUsername, 0);
			pfad =  prefs.getString(urlContact, "http://192.168.2.103/groupdav/Contacts");
		//}catch(NullPointerException e){
		//	pfad = "http://192.168.2.103/groupdav/Contacts";
		//}
		System.out.println("gelesen wurde für Server: " + pfad);
		return pfad;
	}


	private String readCalendarServerURL(String mUsername, Context mContext) {

		if(mUsername==null)throw new ArrayIndexOutOfBoundsException();
		if(mContext==null)throw new ArrayIndexOutOfBoundsException();
		
		
		String pfad;
	
			SharedPreferences prefs = mContext.getSharedPreferences(mUsername, 0);
			pfad =  prefs.getString(urlCalendar, "http://192.168.2.103/groupdav/Calendar");

		System.out.println("gelesen wurde für Calendar: " + pfad);
		return pfad;
	}


	private boolean readContactPhoneWins(String mUsername, Context mContext) {
		if(mUsername==null)throw new ArrayIndexOutOfBoundsException();
		if(mContext==null)throw new ArrayIndexOutOfBoundsException();		
		
		Boolean contactPhoneWins;
		SharedPreferences prefs = mContext.getSharedPreferences(mUsername, 0);
		
		try{
			contactPhoneWins =  prefs.getBoolean(CONTACT_PHONEWINS, false);
		}catch(ClassCastException e){
			e.printStackTrace();
			String x = prefs.getString(CONTACT_PHONEWINS, "false");
			if(x.trim().equals("true")){			
				contactPhoneWins = true;
			}else{
				contactPhoneWins = false;
			}
		}
		return contactPhoneWins;
	}


	private boolean readCalendarPhoneWins(String mUsername, Context mContext) {
		if(mUsername==null)throw new ArrayIndexOutOfBoundsException();
		if(mContext==null)throw new ArrayIndexOutOfBoundsException();		
		
		Boolean calendarPhoneWins;
		SharedPreferences prefs = mContext.getSharedPreferences(mUsername, 0);
		
		
		//calendarPhoneWins =  prefs.getBoolean(CALENDAR_PHONEWINS, false);
		
		
		if(prefs.getString(CALENDAR_PHONEWINS, "false").trim().equals("true")){	
			calendarPhoneWins = true;			
		}else{						
			calendarPhoneWins = false;	
		}
		
		
		
		return calendarPhoneWins;
	}
	  
}