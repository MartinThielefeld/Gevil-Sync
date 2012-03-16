package syncZustand;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.NotImplementedException;

import com.gevil.AndroidCalendar.Konsolenausgabe;
import com.gevil.syncadapter.Constants;

import syncZustand.GevilVCardHash.PropertyByTypeSearcher;
import syncZustand.GevilVCardHash.PropertyByValueSearcher;
import syncZustand.GevilVCardHash.PropertyWithFlag;

import android.content.Context;
import android.provider.ContactsContract;
import android.provider.Settings.Secure;
import android.text.TextUtils;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.vcard.Group;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.Property.Id;
import net.fortuna.ical4j.vcard.parameter.Type;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardOutputter;

public class GevilVCard implements Comparable<GevilVCard> {	
	
	private VCard mCard;
	private int AndriodID = -1;
	
	private GevilVCardHash mHash;
	private TypeBiMap mMap;
	
	private String dbgS="";		
	
	private String fileNameOnServer = null;
	
	
	private int etag = -1;
	
	private PropertyByTypeSearcher searchByType;
	private PropertyByValueSearcher searchByValue;
	
	private int alsGleichBefundenCardsAusgegeben = 0;
	private int alsUngleichBefundeneCardsAusgegeben=0;
	
	
	/**
	 * Konsolenausgabe zum Debuggen von compareTo
	 * @param identisch
	 * @param dbgS
	 * @param dbg
	 */
	private void compareToDebugPrint(boolean identisch, String dbgS, boolean dbg, GevilVCard c){
		if(dbg){
			if(!identisch && alsUngleichBefundeneCardsAusgegeben < 999999999){
				alsUngleichBefundeneCardsAusgegeben++;
				System.out.println("<<< compareTo ergab: UNGLEICH für " + this.getN()[0] + " mit " + c.getN()[0]);
				System.out.println(dbgS + "\n");
			}else if(identisch && alsGleichBefundenCardsAusgegeben < 0){
				alsGleichBefundenCardsAusgegeben++;				
				System.out.println("<<< compareTo ergab: IDENTISCH für " + this.getN()[0] + " mit " + c.getN()[0]);
				System.out.println(dbgS + "\n");
			}else{
				System.out.println("<<< compareTo ergab: " + identisch + " für " + this.getN()[0] + " mit " + c.getN()[0] + "\n");
			}
		}else{
			// keine Konsolenausgabe erwünscht.
		}
	}
	
	
	public URI generateUniqueId(Context ctx){
		String device_uid = Secure.getString(ctx.getContentResolver(),
                Secure.ANDROID_ID);
		

		
		double rand = Math.random();		
		Date d = new Date();		
		
		int andId = this.getAndroidId();
		if(andId==-1)throw new ArrayIndexOutOfBoundsException();
		
		String eindeutig = rand + d.toGMTString() + device_uid;
		int hashCode = eindeutig.hashCode();
		
		String ergebnis = String.valueOf(hashCode) + andId;
		
		System.out.println("@genUID, eindeutiger String: " + eindeutig + " .hash: " + hashCode + " dazu _id: " + andId + " => " + ergebnis);
		
		
		return URI.create(ergebnis);
               
	}
	
	
	
	/**
	 * Vergleich den gewählten Type der VCard this mit c. Types sind z.B. Telefon, Emailadresse, .... 
	 * @param c Vergleichsvcard
	 * @param dbg sind Debug Ausgaben auf der Konsole erwünscht?
	 * @param EmailOderPhone Type
	 * @return 0, falls die Types gleich sind, sonst ein andrere Wert.
	 */
	private int compareToType(GevilVCard c, boolean dbg, net.fortuna.ical4j.vcard.Property.Id EmailOderPhone){
		
		// Vergleich mit c		
		PropertyByValueSearcher search_c_ByAdress = c.getHash().propertiesByValue(EmailOderPhone);
		PropertyByValueSearcher search_this_ByAdress = this.getHash().propertiesByValue(EmailOderPhone);
		//PropertyByTypeSearcher search_c_ByType = hash.propertiesByType(Id.EMAIL);
		
		
		
		Property thisEmail = search_this_ByAdress.getUninsertedPropertyOnce();
		
		// this als ArrayList Iterator durchgehen.
		/*List<Property> thisMailAdr = mCard.getProperties(Id.EMAIL);	
		Iterator<Property> emailThisIt = thisMailAdr.iterator();
		*/
		while(thisEmail != null){			
			String thisMail = thisEmail.getValue();
			if(!TextUtils.isEmpty(thisMail)){				
				//searchByname.put(p.getValue(), new PropertyWithFlag(p));
					
				Property korr = search_c_ByAdress.getPropertyByValueOnce(thisMail);
					
				// falls in c keine korrespondierende Adresse gefunden wurde
				// unterscheiden sich die Beiden. 
				if(korr==null){
					if(dbg)dbgS+="\n<< keine korrespondierende Email in c zu thisMail:" + thisMail + " gefunden. die beiden sind ungleich." + "\n";
					compareToDebugPrint(false, dbgS, dbg, c);
					return -1;
				}
				
				// jetzt noch die Typen prüfen.				
				Object[] thisParams = korr.getParameters().toArray();
				Object[] cParams = thisEmail.getParameters().toArray();
				for(Object a:thisParams){
					for(Object b:cParams){
						if(!a.equals(b)){
							// Typen ungleich. Diese Objekte sollten Strings sein?!
							if(dbg)dbgS+="\n<<Emailtyp hat sich geändert: " + a + " zu " + b + " VCards: \n" + this.toString() + c.toString() + "\n.";
							compareToDebugPrint(false, dbgS, dbg, c);
							return -1;							
						}
					}
				}				
				if(dbg)dbgS+="\n<< Mails werden als identisch angesehen: " + thisMail + " und " + korr.getValue() + "\n.";
			}
			
			
			// nächste Emailadresse von this auslesen, welche dann mit c verglichen wird.
			thisEmail = search_this_ByAdress.getUninsertedPropertyOnce();
		}
		
		Property mail = search_c_ByAdress.getUninsertedPropertyOnce();
		if(mail!=null){
			if(dbg)dbgS+="\n<< c hat mindestens eine Emailadresse mehr, nämlich: " + mail.getValue();	
			compareToDebugPrint(false, dbgS, dbg, c);
			// mindestens eine Email in c gefunden, für die this kein Gegenstück hat.
			return -1;
		}
		
		return 0;
	}
	
	
	/**
	 * Gleichheit im Sinne der Synchronisation. D.h. ungleiche Kontakte 
	 * müssen abgeglichen werden.
	 * @return -1 kleiner/gleich ??
		0 beide gleich
		1 kleiner/gleich ??
	 */
	@Override
	  public int compareTo(GevilVCard c) {
		
		boolean dbg = false;
		
		/*
		return -1; // kleiner/gleich ??
		return 0; // beide gleich
		return 1; // kleiner/gleich ??
		*/
		
		if(c instanceof AktuellServerGevilVCard){
			// AktuellGevilVCard ist eine VCard mit identischem etag und daher immer identisch.
			return 0;
		}
		
		if(this.getEtag()!=-1 && c.getEtag() != -1){
			if(this.getEtag() != c.getEtag()){
				// Unterscheided sich der Etag im Zustand und auf dem Server, ist ein Update fällig,
				// um zu vermeiden dass die VCard beim nächsten Mal erneut heruntergeladen wird.
				if(dbg)System.out.println("GevilVCard.compareTo(): " + " VCard muss upgedated werden, um den etag zu aktualisieren");
				return -1;
			}
		}
		
		
		dbgS="";		
		//
		// Vor- und Nachname vergleichen
		//
		for(int j=0;j<=1;j++){
			
			String dbga = c.getN()[j];
			String dbgb = this.getN()[j];
			
			if(!c.getN()[j].equals(this.getN()[j])){
				if(dbg)dbgS+="\n<< Unterschied in getN: " + c.getN()[j] + " != " + this.getN()[j];
				compareToDebugPrint(false, dbgS, dbg, c);
				return -1;
			}else{
				if(dbg)dbgS+="\n<< "+ c.getN()[j] +  " == " + this.getN()[j] + " getN sind identisch!";				
			}
		}
		
		
		
		//
		// Emailadressen vergleichen.
		//		
		int mailId = compareToType(c, dbg, Id.EMAIL);
		if(mailId != 0){
			// Unterschied gefunden.
			return mailId;
		}
		
		
		//
		// Telefonnummern vergleichen.
		//
		int phoneId = compareToType(c, dbg, Id.TEL);
		if(phoneId != 0){
			// Unterschied gefunden.
			return phoneId;
		}

			
		//
		// TODO weiteres vergleichen
		//
		
		dbgS+="\n<< Ende der Funktion. Es wurden keine Ungleichheiten gefunden.";		
		if(dbg)compareToDebugPrint(true, dbgS, dbg, c);
		return 0;
		
	}
	
	
	public boolean belongsToAccount;
	
	public void initMmap(TypeBiMap map){
		if(mMap == null){
			mMap = map;
		}
	}
	
	
	/**
	 * liest aus einer Property den Android Phone bzw. Mail Type aus.
	 * @param insertPhone
	 * @param mMapPhoneOrEmail mMap.phoneType()  oder   mMap.mailType()
	 * @return Android Type.
	 */
	public int propertyToAndroidType(Property insertPhone, int mMapPhoneOrEmail, boolean mapUnknownToTYPE_HOME){
		int insertType;
		if(insertPhone.getParameters().size() > 0){
        	Type newVCardPhoneType = new net.fortuna.ical4j.vcard.parameter.Type(
        			insertPhone.getParameters().get(0).getValue());
        	
        	if(mMap==null)System.out.println("ExReaseon:(1)");
        	if(newVCardPhoneType==null)System.out.println("ExReaseon:(2)");
        	if(insertPhone==null)System.out.println("ExReaseon:(2)");
        	
        	System.out.println(insertPhone.toString());
        	
        	insertType = mMap.toAndroidType(mMapPhoneOrEmail, newVCardPhoneType);
        }else{
        	// Kein Type Eintrag in VCard vorhanden.
        	if(!mapUnknownToTYPE_HOME){
        		return -1;
        	}else{
	        	if(mMapPhoneOrEmail == mMap.phoneType()){
	        		insertType = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
	        	}else if(mMapPhoneOrEmail == mMap.mailType()){
	        		insertType = ContactsContract.CommonDataKinds.Email.TYPE_HOME;
	        	}else{
	        		throw new IndexOutOfBoundsException();
	        	}
        	}
        }		
		return insertType;
	}
	
	
	/**
	 * liest aus einer Property den Android Phone bzw. Mail Type aus.
	 * @param insertPhone
	 * @return Android Type.
	 */
	public Type propertyToVCardType(Property insertPhone, boolean mapUnknownToTYPE_HOME){
		Type insertType;
		if(insertPhone.getParameters().size() > 0){
        	Type newVCardPhoneType = new net.fortuna.ical4j.vcard.parameter.Type(
        			insertPhone.getParameters().get(0).getValue());
        	
        	insertType = newVCardPhoneType;
        }else{
        	// Kein Type Eintrag in VCard vorhanden.
        	if(!mapUnknownToTYPE_HOME){
        		return null;
        	}else{
        		insertType = new Type("HOME");
        	}
        }		
		return insertType;
	}
	
	
	
	/**
	 * Hier geht es darum, ob der Kontakt von onPerformSync schon verglichen wurde. 
	 * Es kann sein dass karten im SyncZustand noch vorhanden sind, aber nichtmehr
	 * unter Andriod (dem Server). Durch dieses Flag kann erkannt werden ob eine 
	 * Karte nochmals betrachtet werden muss.
	 */
	public boolean FlagSyncTested = false;
	private Konsolenausgabe debug;
	
	
	
	
	/**
	 * Klasse die eine VCard enthält und zusätzliche set und get Methoden.
	 * außderdem könnten hier zusätzliche Informationen zu jeder VCard gespeichert werden.
	 */
	public GevilVCard(TypeBiMap map){		
		//System.out.println("GevilVCard Konstruktor wird ausgeführt.");
		
		mCard = new VCard();		
		Property ver = new net.fortuna.ical4j.vcard.property.Version("2.1");
		mCard.getProperties().add(ver);	
		
		mMap = map;
	}
	
	
	
	/**
	 * Konstruktor
	 * @param card
	 * @param map
	 */
	public GevilVCard(VCard card, TypeBiMap map){
		mMap = map;
		mCard = card;
	}
	
	GevilVCard(int androidId, String uid) {
		if(!(this instanceof AktuellClientGevilVCard)){
			// darf ausschließlich von AktuellClientGevilVCard genutzt werden.
			throw new ArrayIndexOutOfBoundsException();
		}
	}
	
	public GevilVCard(String cardAsString, TypeBiMap map) throws IOException, ParserException{
		
		StringReader sr = new StringReader(cardAsString);
		VCardBuilder builder = new VCardBuilder(sr);
		VCard newCard = builder.build();
		mMap = map;
		mCard = newCard;
	
	}
	
	
	public VCard getVCard(){
		return mCard;
	}
	
	/**
	 * Vorname der VCard setzten
	 * @param vorName
	 */
	public void setFN(String vorName){		
		
		if(this.getFN()==null){
			mCard.getProperties().add(androidToVCardPropertyConversion.parseFn(vorName));
		}
	}
	
	
	public String[] joinAndSetNames(String[] vorNachName1, String[] vorNachName2){						
		
		vorNachName1[0]=vorNachName1[0].trim();
		vorNachName1[1]=vorNachName1[1].trim();
		vorNachName2[0]=vorNachName2[0].trim();
		vorNachName2[1]=vorNachName2[1].trim();
		
		if(vorNachName1[0].equals(vorNachName2[1])){
			String tmp = vorNachName1[0];
			vorNachName1[0] = vorNachName1[1];
			vorNachName1[1] = tmp;
		}
		
		if(TextUtils.isEmpty(vorNachName1[0])){
			vorNachName1[0] = vorNachName2[0];
		}if(TextUtils.isEmpty(vorNachName2[0])){
			vorNachName2[0] = vorNachName1[0];
		}if(TextUtils.isEmpty(vorNachName1[1])){
			vorNachName1[1] = vorNachName2[1];
		}if(TextUtils.isEmpty(vorNachName2[1])){
			vorNachName2[1] = vorNachName1[1];
		}		
		
		String retVorName;
		String retNachName;
		String[] aN = vorNachName1;
		String[] bN = vorNachName2;				
		if(aN[0].equals(bN[0])){
			retVorName = bN[0];
		}else{
			retVorName = aN[0] + "(" + bN[0] + ")";
		}		
		if(aN[1].equals(bN[1])){
			retNachName = bN[1];
		}else{
			retNachName = aN[1] + "(" +  bN[1] + ")";
		}
		
		return new String[]{retVorName, retNachName};
	}
	
	
	
	/**
	 * Fn, d.H. vor- und Nachname der VCard setzten.
	 * @param vorName
	 * @param nachName
	 */
	public void setN(String vorName, String nachName){
		if(vorName==null)vorName="";
		if(nachName==null)nachName="";
		
		if(vorName.trim().equals("") && nachName.trim().equals("")){
			//System.out.println("leere Namen werden nicht eingefüg.: "+  vorName + " " + nachName);			
			//throw new ArrayIndexOutOfBoundsException();
			return;
		}
		
		if(this.getN()[0].equals("") && this.getN()[1].equals("")){
			// wenn kein Name eingefügt wurde, gibt die VCard "" zurück.
			// => Es wurde noch kein Namen eingefügt. Deshalb wird das jetzt getan.
			mCard.getProperties().add(androidToVCardPropertyConversion.parseN(vorName, nachName));
		}else{
			
			if(mCard.getProperty(Id.N)!=null)mCard.getProperties().remove(mCard.getProperty(Id.N));
			mCard.getProperties().add(androidToVCardPropertyConversion.parseN(vorName, nachName));
			
			/*
			// Name ist schon vorhanden.
			if(!vorName.equals(getN()[0]) && !nachName.equals(getN()[1])){
				if(!vorName.equals(getN()[1]) && !nachName.equals(getN()[0])){					
					System.out.println(mCard.toString());
					System.out.println("\n.\n");
					System.out.println("Ungleich: "+  vorName + " " + nachName + "  ist anderst als " + getN()[0] + " " + getN()[1]);
					// es wird versucht zwei unterschliedliche Namen hinzuzufügen.
					throw new ArrayIndexOutOfBoundsException();
				}
			}
			*/
		}

	}
	
	/**
	 * eine weitere Telefonnummer zur VCard hinzufügen.
	 * @param phoneNr
	 * @param type
	 * @param map
	 */
	public void addPhone(String phoneNr, int type){
		mCard.getProperties().add(androidToVCardPropertyConversion.parseTelephone(phoneNr, type, mMap));
	}
	/**
	 * eine weitere Emailadresse zuv VCard hinzufügen.
	 * @param email
	 * @param androidType
	 */
	public void addEmail(String email, int androidType){
		if(!TextUtils.isEmpty(email)){
			mCard.getProperties().add(androidToVCardPropertyConversion.parseEmail(email, androidType, mMap));
		}
	}
	
	
	
	
	/**
	 * Einfügen von Email-, Handy, u.a. Properties
	 * @param nP
	 */
	public void addProperti(Property nP){		
		mCard.getProperties().add(nP);
	}
	

	
	
	/**
	 * UID der VCard eindeutig setzten
	 * @param Uid
	 * @throws URISyntaxException
	 */
	public void setUid(String Uid) throws URISyntaxException{	
		
		
		//if("AndroidId=3A3".equals(Uid))throw new ArrayIndexOutOfBoundsException();
		
		String vorhUID = getUid();
		if(vorhUID == null){		
			// In der VCard ist noch keine UID vorhanden. jetzt einfügen.
			mCard.getProperties().add(androidToVCardPropertyConversion.parseUID(Uid));
		}else{
			if(!vorhUID.trim().equals(Uid.replace("UID:", "").trim())){
				System.out.println("Es wird versucht zwei unterschiedliche UIDs einzufügen: " + Uid.replace("UID:", "").trim() + " " + vorhUID.trim() + " VCard: " + getN()[0] + " " + getN()[1]);
				System.out.println("Android_id: " + this.getAndroidId());
				System.out.println("ServerPfad: " + this.getServerPath());
				// es wird versucht eine andere UID einzufügen. Darf garnicht sein.
				throw new ArrayIndexOutOfBoundsException();
			}
		}
	}
	
	
	public void setEtag(int nEtag){
		etag = nEtag;
	}
	
	public int getEtag(){
		return etag;
	}
	

	
	public String getUid(){
		if(mCard == null) return null;
		
		List<Property> uidProp = mCard.getProperties(Id.UID);
		if(uidProp.size()>0){
			return uidProp.get(0).toString().replace("UID:", "").trim();
		}else{
			return null;
		}
	}
	
	
	public String getFN(){			
		Property property = mCard.getProperty(Id.FN);
		if(property!=null){
			String ret =  property.getValue();
			return ret;		
		}else{
			return null;
		}
	}
	
	
	/**
	 * Vor- und Nachname der VCard auslesen.
	 * @return [0]: Vorname, [1]: Nachname
	 */
	public String[] getN(){
		
		boolean dbg=false;
		
		String[] ret = new String[]{"",""};		
		// N:Vorname;Nachname;;;;  wird geparst.
		
		Property prop = mCard.getProperty(Id.N);
		if(prop==null){
			//System.out.println("VCard hat keinen Namen:\n" + this.toString() + "\n.");
			return ret;
		}
		String thisVNname = prop.toString();
		if(thisVNname==null){
			return null;
		}
		
		String[] foo = thisVNname.split(";");
		
		try{
			ret[0] = foo[0].substring(2).trim();  // N: abschneiden
			ret[1] = foo[1].trim();
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("(!)(!)(!)(!)(!)(!) getN(): Fehler in VCard:" + this.toString() + " \n.");
			return new String[]{"Fehler in VCard",""};
		}
		return ret;
	}
	
	
/*
	public String gfetEmail(int index){	
		/*
		if(index == -1){
			return null;
		}
			
		// TODO Lösung mit try, catch ist unschön. Funktioniert aber.
		try{
			return mCard.getProperties(Id.EMAIL).get(index).getValue();
		}catch(Exception e){
			//System.out.println("(!) Exception in getEmail (letzte Mail gelesen?): " + e + " index: " + index);
			return null;
		}
		*//*
		
		List<Property> mailProp = mCard.getProperties(Id.EMAIL);
		if(mailProp.size()>0){
			return mailProp.get(0).getValue();
		}else{
			return null;
		}
		
	}
*/
	
	
	
	
	
	
	/**
	 * _id die für die Arbeit auf dem Android Adresbuch benötigt wird.
	 * @param id
	 */
	public void setAndroidId(int id){
		AndriodID = id;
	}
	
	/**
	 * mCard.getProperties(Id.TEL).get(index).getValue();
	 * @return
	 */
	public int getAndroidId(){
		return AndriodID;
	}

	/**
	 * Gibt eine TypeBiMap zurück, in android Types zu VCard types
	 * und anderst herum umgewandelt werden können.
	 * @return
	 */
	public TypeBiMap getTypeMap(){
		return mMap;
	}
	
	/**
	 * Gibt ein Objekt der Klasse GevilVCard Hash zurück, mit der die VCard 
	 * durchsucht werden kann.
	 * @return
	 */
	public GevilVCardHash getHash(){
		if(mHash!=null){
				return mHash;
		}else{
			mHash = new GevilVCardHash(this);
			return mHash;
		}
	}
	
	
	/**
	 * gibt die VCard als String zurück.
	 */
	public String toString(){
		
		
		// verhindert manachmal eine parserException.
		if(getFN() == null){
			setFN(getN()[0]);
		}
		
		
		StringWriter s = new StringWriter();
		
		
		VCardOutputter out = new VCardOutputter();
		String ret;
		/*
		ret = mCard.toString();
		ret = ret.replace("HOME.EMAIL", "EMAIL");
		*/
		
		try{
			out.output(mCard, s);
			ret = s.toString();			
			return ret;
		}catch(Exception e){				
			e.printStackTrace();
			System.out.println("Exception card: \n" + mCard.toString());
		}
		
		return null;
		/*
		if(mCard!=null){
			return mCard.toString();
		}else{
			return null;
		}
		*/
	}
	
	
	/**
	 * Speichert einen HREF und dazugeörigen Etag, wie er vom Server geladen wurde.
	 * @author Martin
	 *
	 */
	public class serverHref{
		public serverHref(String nHref, int nEtag){
			setEtag(nEtag);
			setHref(nHref);
		}
		
		public String getHref(){
			// ggf / am Ende entfernen.
			if(href.endsWith("/")){
				return href.substring(0, href.length()-1);
			}else{
				return href;
			}
		}
		public void setHref(String nh){
			href = nh;
		}
		
		public int getEtag(){
			return etag;
		}
		public void setEtag(int netag){
			etag = netag;
		}
		
		
		 public String getUid(){				
				String href = getHref();
				
				// Der Dateiname ist die UID.
				int ind=href.lastIndexOf("/");		
				String ret = href.substring(ind+1).replace("UID:", "").trim();
				
				//System.out.println("**************************************************************************");
				//System.out.print("extrahiere UID aus: " + href + " Extraktionsergebnis: " + ret + "\n");
				return ret;
				
			}
		
		
		private String href;
		private int etag;
	}
	
	
	
	
	/**
	 * Konstruktor wird in AktuelleGevilVCard benötigt. Hier darf er nicht genutzt werden.
	 * @param h
	 */
	public GevilVCard(serverHref h){
		if(!(this instanceof AktuellServerGevilVCard)){
			throw new ArrayIndexOutOfBoundsException();
		}
	}


	public void setServerPath(String newServerPath) {
		fileNameOnServer = newServerPath;
	}
	
	public String getServerPath(){
		return fileNameOnServer;
	}


	public void setKonsolenausgabe(Konsolenausgabe debug) {
		this.debug = debug;
		
	}


	public String getReadableName() {
		return this.getN()[0] + " " + this.getN()[1];
	
	}

}
