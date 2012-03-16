package myHttp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import syncZustand.GevilVCard;
import syncZustand.TypeBiMap;


import com.gevil.OptionsLoader;
import com.gevil.calSyncZustand.GevilVCalendar;
import WorkarroundExternalBugs.Workarounds;
import android.content.Context;
import android.text.TextUtils;

//import org.apache.commons.httpclient.HttpClient;

public class WebDAV {	


	public static final int REGISTRATION_TIMEOUT = 20 * 1000; // ms
	private Authorization credentials;
	TypeBiMap mMap;

	public WebDAV(String Username, String Password, TypeBiMap map) {
		credentials = new Authorization(Username, Password);
		
		mMap = map;		
	}

	public Authorization getCredentials() {
		return credentials;
	}
	
	private static HttpClient mHttpClient;
	/**
	 * Erstellt ein neues HttpClient Objekt, falls noch keins vorhanden ist.
	 */
	public void maybeCreateHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = new DefaultHttpClient();
            
            final HttpParams params = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params,
                REGISTRATION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT);
            ConnManagerParams.setTimeout(params, REGISTRATION_TIMEOUT);
            
        }
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
    public boolean authenticate(String username, String password, Context ctx){
        //Handler handler, final Context context) {        
    	
    	final HttpResponse response;
    	
		//DefaultHttpClient mHttpClient = new DefaultHttpClient();
    	maybeCreateHttpClient();
    	
		HttpPropfind aPropFind = new HttpPropfind();
		String calendarURL = OptionsLoader.getInstance().getCalendarServerUrl(ctx, username);
		aPropFind.setURI(URI.create(calendarURL));

		Authorization auth = this.getCredentials();
		aPropFind.addHeader(auth.getAuthKey(), auth.getAuthValue());
		
		try {
			response = mHttpClient.execute(aPropFind);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MULTI_STATUS) {				
				return true;
	         } else {
	        	 System.out.println("++ authenticate failed(b): " + username + ":" + password + " Statuscode: " + response.getStatusLine().getStatusCode());
	             return false;
	         }
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exc. in WebDAV.authenticate: " + e);
			return false;
		}		
    }
    
    
    

    

	
	//
	// Hält Authentifizierungsinformationen, also Name und Passwort,
	// und gibt sie Base64-Codiert zurück.
	//
	public class Authorization {
		String Bcoded = "";

		public Authorization(String Username, String Password) {
			String foo = Username + ":" + Password;
			Bcoded = Base64.encodeBytes(foo.getBytes());
		}

		public String getAuthValue() {
			return Bcoded;
		}

		public String getAuthKey() {
			return "Authorization: Basic";
		}
	}

	/**
	* Hilfsfunktion,
	* Bricht einen String in einzelne Zeilen um, wobei jede Zeile
	* maxZeilenlaenge lang ist.
	*/
	public String inZeilenUmbrechen(String Text, int maxZeilenlaenge) {

		int l = Text.length();

		if (l < maxZeilenlaenge) {
			return Text;
		} else {
			// int maxZeilenlaenge = 150;
			int t = l / maxZeilenlaenge; // t: Anzahl Zeilen. Nach Umstellen
											// ergibt sich: Nenner = Anzahl
											// Zeichen pro Zeile

			String Ausgabe = "";
			for (int i = 1; i <= t; i++) {
				Ausgabe += Text.substring((i - 1) * l / t, i * l / t);
			}
			return Ausgabe;
		}
	}
	
	
	/**
	* PROPFIND gibt einen BufferedReader zurück, der die XML Antwort 
	* Des Servers enthält und von einem XML Parser weiter verarbeitet werden kann.
	*/
	public BufferedReader PROPFIND(String URL, Authorization auth)
			throws Exception {

		//DefaultHttpClient mHttpClient = new DefaultHttpClient();
		this.maybeCreateHttpClient();

		HttpPropfind aPropFind = new HttpPropfind();
		aPropFind.setURI(URI.create(URL));

		aPropFind.addHeader(auth.getAuthKey(), auth.getAuthValue());
		HttpResponse response;

		try {
			response = mHttpClient.execute(aPropFind);

			// Codierung der Daten stehen in einem Header
			// vielleicht ContentEncoding.

			BufferedReader in = null;

			String encoding = "" + response.getEntity().getContentEncoding();
			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));

			return in;
			

		}catch(java.net.SocketException e){
			System.out.println("ungültige Server URL: " + URL);
			e.printStackTrace();
		}catch (Exception e) {		
			e.printStackTrace();
			System.out.println("+canton err2 " + e);// ";//" + "  " + e);
		}
		return null;
	}
	
	
	/**
	 * eine Vcard auf Server hochladen. TODO achtung dass keine Dateien
	 * überschrieben werden.
	 * WICHTIG: Die ServerURL muss auch einen Dateiname enthalten.
	 * Für Vcard: *.vcf	 * 
	 * @param Vcard
	 * @param sURL
	 * @throws Exception
	 */
	public void PutVCard(GevilVCard Vcard, String sURL) throws Exception {
		boolean dbg = false;
		maybeCreateHttpClient();
		
		if(dbg)System.out.println("Hochladen der VCard nach: " + sURL);
		HttpPut httpput = new HttpPut(sURL);

		Authorization auth = this.getCredentials();
		httpput.addHeader(auth.getAuthKey(), auth.getAuthValue());		

		HttpResponse response;
		
		///////////	
		
		StringEntity vCardEntity = new StringEntity(Vcard.toString()); 
		vCardEntity.setContentType("text/x-vcard");
		
		httpput.setEntity(vCardEntity);			
		
		///////////			
		
		response = mHttpClient.execute(httpput);
		
		
		//if(response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED)
		
		if(dbg)System.out.println("2, etag: " + response.getHeaders("etag")[0].getValue());
		if(dbg)System.out.println("2, etag: " + response.getHeaders("Location")[0].getValue());		
		
		
		String newServerEtag = response.getHeaders("etag")[0].getValue().replace("\"", "");
		String newServerPath = response.getHeaders("Location")[0].getValue();
		
		
		if(newServerEtag==null)throw new ArrayIndexOutOfBoundsException();
		if(TextUtils.isEmpty(newServerEtag))throw new ArrayIndexOutOfBoundsException();
		if(newServerPath==null)throw new ArrayIndexOutOfBoundsException();
		if(TextUtils.isEmpty(newServerPath))throw new ArrayIndexOutOfBoundsException();
		
		Vcard.setEtag(Integer.valueOf(newServerEtag));
		Vcard.setServerPath(newServerPath);

	}
	
	
	
	/**
	 * eine Vcard auf Server hochladen. TODO achtung dass keine Dateien
	 * überschrieben werden.
	 * WICHTIG: Die ServerURL muss auch einen Dateiname enthalten.
	 * Für Vcard: *.vcf	 * 
	 * @param calendar
	 * @param sURL
	 * @throws Exception
	 */
	public void PutVCalendar(GevilVCalendar calendar, String sURL) throws Exception {
		boolean dbg = true;
		maybeCreateHttpClient();
		
		if(dbg)System.out.println("***** Hochladen vom VCalendar nach: " + sURL);
		if(dbg)System.out.println(calendar.toString() + "\n.\n.");
		
		if(calendar.getUid()==null){
			throw new ArrayIndexOutOfBoundsException();			
		}else if(calendar.getUid().length()<1){
			throw new ArrayIndexOutOfBoundsException();		
		}
		
		
		HttpPut httpput = new HttpPut(sURL);

		Authorization auth = this.getCredentials();
		httpput.addHeader(auth.getAuthKey(), auth.getAuthValue());		

		HttpResponse response;
		
		///////////	
		
		StringEntity vCardEntity = new StringEntity(calendar.toString()); 
		//vCardEntity.setContentType("text/x-vcalendar");
		vCardEntity.setContentType("text/calendar");
		
		httpput.setEntity(vCardEntity);			
		
		///////////			
		
		response = mHttpClient.execute(httpput);
		
		
		//if(response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED)
		
		if(dbg)System.out.println("2, etag: " + response.getHeaders("etag")[0].getValue());
		if(dbg)System.out.println("2, Location: " + response.getHeaders("Location")[0].getValue());		
		
		
		String newServerEtag = response.getHeaders("etag")[0].getValue().replace("\"", "");
		String newServerPath = response.getHeaders("Location")[0].getValue();
		
		if(newServerEtag==null) throw new ArrayIndexOutOfBoundsException();
		if(newServerPath==null) throw new ArrayIndexOutOfBoundsException();
		
		calendar.setEtag(Integer.valueOf(newServerEtag));
		calendar.setServerPath(newServerPath);

	}
	
	/**
	 * HttpDelete
	 * @param uid
	 * @param sURL
	 * @throws Exception
	 */
	public void delteVCard(String sURL) throws Exception {
		boolean dbg = false;
		
		
		maybeCreateHttpClient();
		HttpDelete httpdelete = new HttpDelete(sURL);

		Authorization auth = this.getCredentials();
		httpdelete.addHeader(auth.getAuthKey(), auth.getAuthValue());		

		/*
		HttpResponse response;
		response = 
		*/
		
		mHttpClient.execute(httpdelete);
		
	
		/*
		BufferedReader in = null;
		
		//String encoding = "" + response.getEntity().getContentEncoding();
		in = new BufferedReader(new InputStreamReader(response.getEntity()
				.getContent()));

		StringBuffer sb = new StringBuffer("");
		String line = "";
		String NL = System.getProperty("line.separator");
		while ((line = in.readLine()) != null) {
			sb.append(line + NL);
		}
		in.close();
		String page = sb.toString();
		
		if(dbg)System.out.println("================ Antwort des Servers auf Delete: ==========================");
		if(dbg)System.out.println(page);
		if(dbg)System.out.println("===========================================================================");
		*/
	}
	
	/**
	* Gibt das Ergebnis des GET als String zurück
	* => z.B. nachdem mit PROPFIND der Pfad einer vCard gefunden wurde
	* kann diese heruntergeladen werden.
	*/
	public GevilVCard httpGetVCard(String URL, Authorization auth) throws Exception {

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(URL);

		httpget.addHeader(auth.getAuthKey(), auth.getAuthValue());

		
			
		HttpResponse response = httpclient.execute(httpget);		
		BufferedReader in = null;
		in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		
		StringBuffer sb = new StringBuffer("");
		String line = "";
		String NL = System.getProperty("line.separator");
		while ((line = in.readLine()) != null) {
			sb.append(line + NL);
		}
		in.close();
		
		String vcardString = sb.toString();
		vcardString = Workarounds.fixVCardForical4j_vcard(vcardString);
        
			       
		GevilVCard gVCard = new GevilVCard(vcardString, mMap);
		//gVCard.setEtag(href.getEtag());
		
		String newServerEtag = response.getHeaders("etag")[0].getValue().replace("\"", "");
		gVCard.setEtag(Integer.valueOf(newServerEtag));
		
		
		return gVCard;
	}
	
	/**
	* Gibt das Ergebnis des GET als String zurück
	* => z.B. nachdem mit PROPFIND der Pfad einer vCard gefunden wurde
	* kann diese heruntergeladen werden.
	*/
	public GevilVCalendar httpGetCalendar(String URL, Authorization auth) throws Exception {

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(URL);

		httpget.addHeader(auth.getAuthKey(), auth.getAuthValue());

		
			
		HttpResponse response = httpclient.execute(httpget);		
		BufferedReader in = null;
		in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		
		StringBuffer sb = new StringBuffer("");
		String line = "";
		String NL = System.getProperty("line.separator");
		while ((line = in.readLine()) != null) {
			sb.append(line + NL);
		}
		in.close();
		
		String vcardString = sb.toString();
		vcardString = Workarounds.fixVCalendarForical4j_vcard(vcardString);        
		

		
		GevilVCalendar gVCalendar = new GevilVCalendar(vcardString, mMap);	
		
		String newServerEtag = response.getHeaders("etag")[0].getValue().replace("\"", "");
		gVCalendar.setEtag(Integer.valueOf(newServerEtag));		
		
		return gVCalendar;
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
