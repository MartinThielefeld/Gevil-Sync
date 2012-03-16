package com.gevil;

import java.net.URI;

import myHttp.WebDAV;
import myHttp.WebDAV.HttpPropfind;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.gevil.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class options_activity extends PreferenceActivity {


	private static final String CONTACT_PHONEWINS = "vvvcontactsPhoneWinsad";
	private static final String CALENDAR_PHONEWINS = "vvvContactServerURLb";		
	private static final String urlCalendar = "vvvCalendarServerURLcd";
	private static final String urlContact =  "vvvContactServerURLd";
	/**
	 * Quell: http://www.kaloer.com/android-preferences
	 */
	
	// Muss im Intent der die Activity startet übergeben werden, und wird dann von onCreate() ausgelesen.
	String mUsername;
	Context mContext;
	
	private void storePreferences(){		
		if(mUsername==null)throw new ArrayIndexOutOfBoundsException();
		if(mContext==null)throw new ArrayIndexOutOfBoundsException();
		
		
		// schreiben für jeden Nutzer
		SharedPreferences prefs = getSharedPreferences(mUsername, 0);
		
		// auslesen des aktuellen Zustandes
		SharedPreferences aktuell = PreferenceManager.getDefaultSharedPreferences(mContext);		
		Editor edit = prefs.edit();		
		
		
		System.out.println("Wert für Calendar.phoneWins: " + aktuell.getBoolean("cbCalPhoneWins", false));
		
		if(aktuell.getBoolean("cbCalPhoneWins", false)){
			System.out.println("@store: PhoneWins = true");
			edit.putString(CALENDAR_PHONEWINS, "true");		
		}else{
			System.out.println("@store: PhoneWins = false");
			edit.putString(CALENDAR_PHONEWINS, "false");		
		}
		
		if(aktuell.getBoolean("cbContactsPhoneWins", false)){
			edit.putString(CONTACT_PHONEWINS, "true");
		}else{
			edit.putString(CONTACT_PHONEWINS, "false");
		}
		
		edit.putString(urlCalendar, aktuell.getString("editCalendarURL", ""));
		edit.putString(urlContact, aktuell.getString("editContactsURL", ""));		

		edit.commit();
		
		OptionsLoader.getInstance().notifiyChange();
	}
	
	
	private void loadPreferences(CheckBoxPreference checkBoxContactsPhoneWins, CheckBoxPreference checkBoxCalPhoneWins,
	EditTextPreference contactURL, EditTextPreference calendarURL, Preference btShowOpenConflicts) {
		
		if(mUsername==null)throw new ArrayIndexOutOfBoundsException();
		if(mContext==null)throw new ArrayIndexOutOfBoundsException();
		
		SharedPreferences prefs = getSharedPreferences(mUsername, 0);
		
		if(prefs.getString(CALENDAR_PHONEWINS, "false").trim().equals("true")){	
			checkBoxCalPhoneWins.setSummary("Phone Wins");			
			checkBoxCalPhoneWins.setChecked(true);
		}else{			
			checkBoxCalPhoneWins.setSummary("Server Wins");
			checkBoxCalPhoneWins.setChecked(false);		
		}
		
		if(prefs.getString(CONTACT_PHONEWINS, "false").trim().equals("true")){		
			checkBoxCalPhoneWins.setSummary("Phone Wins");			
			checkBoxContactsPhoneWins.setChecked(true);
		}else{
			checkBoxCalPhoneWins.setSummary("Server Wins");
			checkBoxContactsPhoneWins.setChecked(false);
		}
		
		btShowOpenConflicts.setSummary("Klicken um offene Konflikete von " + mUsername + " anzuzeigen");
		
		//calendarURL.setText(prefs.getString(urlCalendar, ""));
		calendarURL.setText(prefs.getString(urlCalendar, "http://192.168.2.103/groupdav/Calendar"));
		
		contactURL.setText(prefs.getString(urlContact, "http://192.168.2.103/groupdav/Contacts"));
		
	}
	
	
    /**
     * Herausfinden ob der gegebene Username + Passwort vom Server akzeptiert werden.
     * 
     * @param username The user's username
     * @param password The user's password
     * @param handler The hander instance from the calling UI thread.
     * @param context The context of the calling Activity.
     * @return boolean The boolean result indicating whether the user was
     *         successfully authenticated.
     */
    public boolean checkServerURL(String serverURL){
        //Handler handler, final Context context) {        
    	
    	final HttpResponse response;
	
    
    	HttpClient mHttpClient = new DefaultHttpClient();    		
    	int REGISTRATION_TIMEOUT = 5 * 1000; // ms
        final HttpParams params = mHttpClient.getParams();            
        HttpConnectionParams.setConnectionTimeout(params,
            REGISTRATION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT);
        ConnManagerParams.setTimeout(params, REGISTRATION_TIMEOUT);
    	
    		
    	HttpGet httpget = new HttpGet(serverURL);
		httpget.setURI(URI.create(serverURL));

		//Authorization auth = this.getCredentials();
		//aPropFind.addHeader(auth.getAuthKey(), auth.getAuthValue());
		
		try {
			response = mHttpClient.execute(httpget);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return true;
	         } else if(response.getStatusLine().getStatusCode() == 401) {
	        	 return true;
	         }else{
	            System.out.println("erhaltener http Status: " + response.getStatusLine().getStatusCode());
	        	 return false;
	         }
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exc. in WebDAV.authenticate: " + e);
			return false;
		}		
    }
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		
		
		final CheckBoxPreference checkBoxContactsPhoneWins = (CheckBoxPreference) findPreference("cbContactsPhoneWins");
		final CheckBoxPreference checkBoxCalPhoneWins = (CheckBoxPreference) findPreference("cbCalPhoneWins");			
		final Preference btShowOpenConflicts = (Preference) findPreference("showConflicts");
		
		final EditTextPreference contactURL = (EditTextPreference)findPreference("editContactsURL");
		final EditTextPreference calendarURL = (EditTextPreference)findPreference("editCalendarURL");
		
		
		
		mUsername = getIntent().getStringExtra("username");
		mContext = this;		
		loadPreferences(checkBoxContactsPhoneWins, checkBoxCalPhoneWins, contactURL, calendarURL, btShowOpenConflicts);
		
		
		

		
		
		
		
		
		contactURL.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				
				boolean validValue = checkServerURL((String)newValue);
				//System.out.println("geprüft wird die URL: " + (String)newValue + " Ergebnis: " + validValue);
				
				if(validValue){
					contactURL.setSummary("GroupDAV Kontakte URL, der eingegebene Server wurde erfolgreich kontaktiert. Ihr Passwort wurde nicht überprüft.");
					
				}else{
					contactURL.setSummary("GroupDAV Kontakte URL, leider konnte unter der eingegebnen Adresse kein Server gefunden werden.");
					
				}
				
				return true;
			}
			
		});
		
		calendarURL.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				
				boolean validValue = checkServerURL((String)newValue);
				//System.out.println("geprüft wird die URL: " + (String)newValue + " Ergebnis: " + validValue);
				
				if(validValue){
					calendarURL.setSummary("GroupDAV Kalender URL, der eingegebene Server wurde erfolgreich kontaktiert. Ihr Passwort wurde nicht überprüft.");
					
				}else{
					calendarURL.setSummary("GroupDAV Kalender URL, leider konnte unter der eingegebnen Adresse kein Server gefunden werden.");
					
				}
				
				return true;
			}
			
		});
	
		
        	btShowOpenConflicts.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                  public boolean onPreferenceClick(Preference preference) {
                        String Tabellenname = "tblConflicts" + mUsername; 
                                		
                        Intent notificationsWindow = new Intent(getBaseContext(), 
                               notifications.class);                                		
                        notificationsWindow.putExtra("tabellenname", Tabellenname);
                        startActivity(notificationsWindow);
                                	        
                                		
                        return true;
                    }

        	});
		
        	
		checkBoxCalPhoneWins.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				
				//boolean forCalendarPhoneWins = false;
				if(newValue.toString().equals("true")){
					//forCalendarPhoneWins=true;
					checkBoxCalPhoneWins.setSummary("Phone Wins");
					
				}else if(newValue.toString().equals("false")){
					//forCalendarPhoneWins=false;
					checkBoxCalPhoneWins.setSummary("Server Wins");
					
				}else{
					throw new ArrayIndexOutOfBoundsException();
				}
				
				
				return true;
			}
			
		});
		
		checkBoxContactsPhoneWins.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				
				//boolean forCalendarPhoneWins = false;
				if(newValue.toString().equals("true")){
					//forCalendarPhoneWins=true;
					checkBoxContactsPhoneWins.setSummary("Phone Wins");
					
				}else if(newValue.toString().equals("false")){
					//forCalendarPhoneWins=false;
					checkBoxContactsPhoneWins.setSummary("Server Wins");
					
				}else{
					throw new ArrayIndexOutOfBoundsException();
				}
				
				
				return true;
			}
			
		});

	}
	
	/**
	 * Sobald die Activity nichtmehr im Vordergrund ist, werden alle getätigten Optionen für
	 * den Benutzer mUsername gespeichert.
	 */
	@Override protected void onPause() {
		super.onPause();
		storePreferences();
	}


	public void setData(Context ctx, String userName) {
		mContext = ctx;
		mUsername=userName;
		
	}


	
	public String getContactsServerURL() {
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


	public String getCalendarServerURL() {
		if(mUsername==null)throw new ArrayIndexOutOfBoundsException();
		if(mContext==null)throw new ArrayIndexOutOfBoundsException();
				
		String pfad;
	
			SharedPreferences prefs = mContext.getSharedPreferences(mUsername, 0);
			pfad =  prefs.getString(urlCalendar, "http://192.168.2.103/groupdav/Calendar");

		System.out.println("gelesen wurde für Calendar: " + pfad);
		return pfad;
	}


	public boolean getContactPhoneWins() {
		if(mUsername==null)throw new ArrayIndexOutOfBoundsException();
		if(mContext==null)throw new ArrayIndexOutOfBoundsException();		
		
		Boolean contactPhoneWins;
		SharedPreferences prefs = mContext.getSharedPreferences(mUsername, 0);
		contactPhoneWins =  prefs.getBoolean(CONTACT_PHONEWINS, false);
		
		return contactPhoneWins;
	}


	public boolean getCalendarPhoneWins() {
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