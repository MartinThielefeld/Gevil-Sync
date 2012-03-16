package com.gevil.calSyncZustand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang.NotImplementedException;

import com.gevil.AndroidCalendar.Konsolenausgabe;
import com.gevil.syncadapter.Constants;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Debug;
import android.provider.Settings.Secure;
import android.text.format.DateFormat;
import android.widget.TextView;

import syncZustand.GevilVCard.serverHref;
import syncZustand.AktuellServerGevilVCard;
import syncZustand.TypeBiMap;
import syncZustand.androidToVCardPropertyConversion;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Trigger;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;
import net.fortuna.ical4j.vcard.Property.Id;

public class GevilVCalendar {

	private Calendar mCalendar;
	private Integer etag;
	private String serverPath;
	//private int andoidId=-1;
	TypeBiMap mMap;
	public boolean FlagSyncTested;
	
	boolean dbg = false;
	
	private Konsolenausgabe konsolenersatz = new Konsolenausgabe();
	
	Integer androidId = -1;
	private int alarmInMinutes;
	
	
	private String BRtoString(BufferedReader in){		
        try {
			String line;
			String ret = "";
			while ((line = in.readLine()) != null) {
				//if(!line.contains("PRODID")){
					ret +=  line.trim() + "\r\n";
				//}
				//System.out.println( );
			}
			return ret;
        } catch (IOException e) {
        	e.printStackTrace();
			System.out.println("+Exec in printBufferedReader " + e);
		}
		return null;
}
	
	
	// TODO mMap in Konstruktor aufnehmen
	public GevilVCalendar(String myCalendarString, TypeBiMap mmMap){
		mCalendar = new Calendar();
		mMap = mmMap;
		
		//StringReader sin = new StringReader(myCalendarString);		
		//BufferedReader justForDebugging = new BufferedReader(sin);
		
		
		
		
		CalendarBuilder builder = new CalendarBuilder();

		try {		
			
			//mCalendar = builder.build(sin);
			mCalendar = builder.build(new StringReader(myCalendarString));
			
		} catch (IOException e) {		
			e.printStackTrace();
		} catch (ParserException e) {			

			System.out.println("\n.\nException Card: *******************\n" + myCalendarString);
			e.printStackTrace();			
			//throw new ArrayIndexOutOfBoundsException();
		}
		
		
	}


	public GevilVCalendar(Calendar basiccard, TypeBiMap mmMap) {
		mCalendar = basiccard;
		mMap = mmMap;
	}
	

	public GevilVCalendar(TypeBiMap mMap2) {
		
		mCalendar = new Calendar();
		mCalendar.getProperties().add(new ProdId("-//Gevil Sync//iCal4j 1.0//EN"));
		mCalendar.getProperties().add(Version.VERSION_2_0);
		mCalendar.getProperties().add(CalScale.GREGORIAN);
		
		mMap = mMap2;
	}
	
	public GevilVCalendar(serverHref serverHref) {
		if(!(this instanceof AktuellServerGevilVCalendar)){
			throw new ArrayIndexOutOfBoundsException();
		}
	}


	public void setKonsolenersatz(Konsolenausgabe a){
		this.konsolenersatz = a;
	}


	public URI generateUniqueId(Context ctx){
		String device_uid = Secure.getString(ctx.getContentResolver(),
                Secure.ANDROID_ID);
		

		
		double rand = Math.random();		
		Date d = new Date();			
		
		String eindeutig = d.toGMTString() + device_uid;
		int hashCode = eindeutig.hashCode();
		
		String ergebnis = String.valueOf(hashCode) + rand;
		
		System.out.println("@genUID, eindeutiger String: " + eindeutig + " .hash: " + hashCode + " dazu _id: " +  " => " + ergebnis);
		
		
		return URI.create(ergebnis);
               
	}


	public int compareTo(GevilVCalendar that) {
		/*
		    BEGIN:VEVENT
			DTSTAMP:20111109T094315
			SUMMARY:TaeBo Special
			LOCATION:Ravensburg
			DESCRIPTION:TaeBo In Ravensburg
			DTSTART;TZID=Europe/London:20111109T000000
			DTEND;TZID=Europe/London:20111109T090000
			TRANSP:OPAQUE
			UID:4eba4b33-a34-0
			SEQUENCE:1
			ORGANIZER:MAILTO:kaffe@localhost.localdomain
			END:VEVENT
		 */
		
		//if(!comparePropValue("DTSTAMP", that))return -1;
		
		if(that instanceof AktuellServerGevilVCalendar){
			return 0;
		}
		
		if(!propValuesEqual("LOCATION", that)){
			konsolenersatz.println("diff. in LOCATION: " + getPropValue("LOCATION") + " != " + that.getPropValue("LOCATION"));
			return -1;
		}
		if(!propValuesEqual("DESCRIPTION", that)){
			konsolenersatz.println("diff. in DESCRIPTION");
			return -1;			
		}
		if(!propValuesEqual("SUMMARY", that)){
			konsolenersatz.println("diff. in SUMMARY");
			return -1;
		}
		if(!propValuesEqual("DTSTART", that)){
			konsolenersatz.println("diff. in DTSTART " + getPropValue("DTSTART") + " != " + that.getPropValue("DTSTART") );
			return -1;
		}
		
		String thisRrule = this.getRrule();
		String thatRrule = that.getRrule();
		
		if(thisRrule==null && thatRrule==null){
			// keine RRule. In diesem Fall gibt es ein DTEND
			if(!propValuesEqual("DTEND", that)){  // 4: nur das Jahr prüfen.
				konsolenersatz.println("compareTo.diff. in DTEND: " + getPropValue("DTEND") + " != " + that.getPropValue("DTEND") );
				return -1;
			}
		}
		
		if(!rRulesEqual(this, that)){
			return -1;
		}		
		

		if(!remindersEqual(that)){
			return -1;
		}
		
		//if(!comparePropValue("TRANSP", that))return -1;
		//if(!comparePropValue("ORGANIZER", that))return -1;
		return 0; // die beiden sind gleich.
		
	}
	
	
	public boolean remindersEqual(GevilVCalendar that){
		ComponentList thisC = mCalendar.getComponents();
		ComponentList thatC = that.getCalendar().getComponents();
		
		
		HashMap<String, Boolean> hash = new HashMap<String, Boolean>();
		
		int i = 0;
		while(i<thisC.size()){
			Component a = (Component)thisC.get(i);
			if(a.getProperties("TRIGGER").size()>0){
				// es handelt sich anscheinend um einen Alarm-Eintrag mit Trigger.
				String alarmZeitpunkt = a.getProperties("TRIGGER").get(0).toString();
				hash.put(alarmZeitpunkt, true);
			}
			
			i++;
		}
		
		int anz = i;
		
		
		i = 0;
		while(i<thatC.size()){
			Component a = (Component)thatC.get(i);
			if(a.getProperties("TRIGGER").size()>0){
				// es handelt sich anscheinend um einen Alarm-Eintrag mit Trigger.
				
				String alarmZeitpunkt = a.getProperties("TRIGGER").get(0).toString();
				
				if(hash.get(alarmZeitpunkt)!=null){
					hash.remove(alarmZeitpunkt);
				}else{
				// kein Eintrag gefunden.
					System.out.println("alarm_ ungleich weil " + alarmZeitpunkt + " nicht in this vorhanden");
					return false;
				}
			}			
			i++;
		}
		
		if(hash.keySet().size()>0){
			// Falls jedes Element gefunden wurde, muss die Hashmap leer sein.
			// Falls ein oder mehr Elemente nicht gefunden wurden, sind sie noch in der Map vorhanden.
			System.out.println("alarm_ ungleich weil keySet.size != 0  size="+hash.keySet().size());
			return false;
		}else{
			
			System.out.println("alarm_ gleich. Es wurden " + anz + " Alarmeinträge verglichen.");
			
			return true;
		}
		
		
		
		

		
	}
	
	
	public boolean rRulesEqual(GevilVCalendar thiss, GevilVCalendar that) {
		
		String stdsdring = "RRULE";
		boolean dbg=true;
		
		ComponentList clThis = thiss.getCalendar().getComponents();	
		ComponentList clThat = that.getCalendar().getComponents();	
		
		if(clThis!=null && clThat!=null){if(clThis.size()>0 && clThat.size()>0){						
			
			Property thisP = ((Component)clThis.get(0)).getProperty(stdsdring);
			Property thatP = ((Component)clThat.get(0)).getProperty(stdsdring);
			
			if(thisP!=null && thatP!=null){
				println("efg: " + thisP.toString());		
			
				HashMap<String, String> x = new HashMap<String, String>();
				
				/*
				 * Es kann passieren dass thiss mehr Einträge in der RRule hat,
				 * als that. In diesem Fall beinhaltet auch die HashMap diese Elemente. Dabei könnte
				 * es passieren dass trotzdem zu jedem Element aus that ein Element in der HashMap(this) 
				 * gefunden wird.
				 * 
				 * Wenn AnzahlElemente am Ende der beiden Schleifen 0 ist, ist sichergestellt,
				 * dass thiss und that die gleiche Anzahl von RRules haben. Ist die Anzahl verschieden,
				 * werden die rrules als unterschiedlich angesehen.
				 */
				int AnzahlElemente = 0;
				
				if(dbg)System.out.println("rRulesEqual " + thiss.getRrule() + " <> " + that.getRrule());
				
				
				//
				// HashMap über this aufbauen
				//
				@SuppressWarnings("unchecked")
				Iterator<Parameter> thisIt = thisP.getParameters().iterator();
				while(thisIt.hasNext()){
					Parameter th = thisIt.next();					
					x.put(th.getName(), th.getValue());
					if(th.getName()!="WKST")AnzahlElemente ++;;
				}
				
				//
				// alle Elemente von that in der Hashmap suchen.
				//
				@SuppressWarnings("unchecked")
				Iterator<Parameter> thatIt = thatP.getParameters().iterator();
				while(thatIt.hasNext()){
					Parameter th = thatIt.next();					
					String counter = x.get(th.getName());
					if(counter == null){
						// Parameter existiert in that aber nicht in this.
						if(th.getName() != "WKST"){
							System.out.println("rRulesEqual:FALSE kein Gegenpart zu " + th.getName() + "=" + th.getValue() + "gefunden");
							return false;
						}						
					}else{
						if(th.getName()!="WKST")AnzahlElemente--;;
					}
				}
				
				if(AnzahlElemente!=0){
					if(dbg)System.out.println("rRulesEqual:FALSE(a)");
					return false;	// Anzahl unterscheidet sich in thiss und that.
				}
				
				if(dbg)System.out.println("rRulesEqual:TRUE(a)");
				return true;				
				
			}else{
				if(thisP!=null || thatP!=null){
					if(dbg)System.out.println("rRulesEqual:FALSE(b)");
					return false; // eines ist null, das andere nicht.
				}
				if(thisP==null && thatP==null){
					if(dbg)System.out.println("rRulesEqual:TRUE(b)");
					return true; // Beide sind null, d.h. beide sind gleich.
				}
			}
		}else{
			if(dbg)System.out.println("rRulesEqual: " + (clThis.size() == clThat.size()) + "(c)");
			return clThis.size() == clThat.size();
		}
		}else{
			if(clThis!=null || clThat!=null){
				if(dbg)System.out.println("rRulesEqual:FALSE(d)");
				return false; // eins null, eins != null
			}
			if(dbg)System.out.println("rRulesEqual:TRUE(d)");
			return true; // beide null
		}
		
		
		return false;
	}


	public Calendar getCalendar() {		
		return mCalendar;
	}


	public boolean propValuesEqual(String value, GevilVCalendar that){
		String a = getPropValue(value);
		String b = that.getPropValue(value);
		if(a==null && b==null) return true;
		if(a==null) return false;
		if(b==null) return false;
		
		if(!a.equals(b)){
			println(value +": "+ a + " != " + b);
		}		
		return a.equals(b);
	}
	
	public void setEtag(Integer valueOf) {
		this.etag = valueOf;
		
	}
	
	
	private void loopOver(){
		ComponentList p = mCalendar.getComponents();
		if(p.size()>0){			

			Iterator<Component> pIt = p.iterator();
			while(pIt.hasNext()){				
				Component component = pIt.next();
				
				//System.out.println("getUid: " + component.getName());
				
				PropertyList p2 = component.getProperties();
				Iterator<Property> it2 = p2.iterator();
				while(it2.hasNext()){
					Property pp = it2.next();
					
					System.out.println("Calendar.loopOver: " + pp.getName() + pp.getValue());
				}
			}
		}
	}


	public String getUid() {
		ComponentList p = mCalendar.getComponents();		
		if(p.size()>0){		
			Property uid = ((Component)p.get(0)).getProperty("UID");
			if(uid==null)return null;
			return uid.getValue().replace("UID:", "").trim();
		}else{
			return null;
		}		
	}
	
	
	
	
	/**
	 * sucht die property namens pName im Kalendereintrag
	 * und gibt deren Value zurück.
	 * @param pName Namen der Property die gesucht werden soll
	 * @return Value dieser Property
	 */
	public String getPropValue(String pName){
		ComponentList cL = mCalendar.getComponents();		
		if(cL.size()>0){		
			Property pp = ((Component)cL.get(0)).getProperty(pName);
			
			if(pp!=null){
				try{
					String ret = pp.getValue();
					if(ret==null)return null;
					if(ret.trim().equals(""))return null;
					return ret.trim();
				}catch(Exception e){
					System.out.println("Exception: "  + pName + " nicht gefunden.");
					e.printStackTrace();
					return null;
				}
			}else{
				return null;
			}
		}else{
			return null;
		}		
	}
	
	
	
	
	public Property getProp(String pName){
		ComponentList cL = mCalendar.getComponents();		
		if(cL.size()>0){		
			Property pp = ((Component)cL.get(0)).getProperty(pName);			
			if(pp!=null){
				return pp;
			}else{
				return null;
			}
		}else{
			return null;
		}		
	}
	
	
	/**
	 * liest dtstart(dtend) aus dem Kalendereintrag aus und konvertiert es in
	 * ein Date Objekt.
	 * @param string dtstart oder dtend
	 * @return Date Objekt das den Inhalt von dtstart(end) beinhaltet
	 */
	public Date getTimeProp(String string){		
		
		//String timeProp = getPropValue(string);
		String timeProp = null;
		Parameter tzid = null;
		
		ComponentList cL = mCalendar.getComponents();		
		if(cL.size()>0){
			Property pp = ((Component)cL.get(0)).getProperty(string);
			if(pp==null)return null;
			ParameterList params = pp.getParameters();
			

			// falls vorhanden, TZID auslesen.
			try{
				tzid = params.getParameter("TZID");
			}catch(Exception e){
				
			}			
			// auslesen der Property, also dem String in dem Zeit und Datum gespeichert sind.						
			timeProp = pp.getValue();			
		}

		
		Integer jahr = Integer.valueOf(timeProp.substring(0,4));
		Integer monat = Integer.valueOf(timeProp.substring(4,6));
		Integer tag = Integer.valueOf(timeProp.substring(6,8));
		
		/*if(timeProp.charAt(8) != 'T'){
			throw new ArrayIndexOutOfBoundsException();
		}*/
		
		Integer stunde = Integer.valueOf(timeProp.substring(9,11));
		Integer minute = Integer.valueOf(timeProp.substring(11,13));
		Integer sekunde = Integer.valueOf(timeProp.substring(13,15));				
		
		int zeitZonenOffsetInMinuten = 0;
		if(tzid!=null){
			String tz = ((Parameter)tzid).getValue();
			if(tz.toLowerCase().contains("europe")){
				zeitZonenOffsetInMinuten = + 60;
			}
			if(tz.toLowerCase().contains("london")){
				zeitZonenOffsetInMinuten = 0;
			}	
			// TODO ggf. weitere Zeitzonen beachten.
		}
		
		
		//year  the year, 0 is 1900 
		//the month, 0 - 11.          nicht wie oben 1-12; das gleiche gilt für Stunde.
		Date d = null;
		
		if(timeProp.toLowerCase().endsWith("z")){
		// Zeitangabe ist in UTC. Daher ist kein Zeitzonenoffset erlaubt.
			d = new Date(jahr-1900, monat-1, tag, stunde, minute , sekunde);
		}else{
			d = new Date(jahr-1900, monat-1, tag, stunde, minute +zeitZonenOffsetInMinuten , sekunde);
		}
		
		//println(d.toLocaleString());
		
		return d;
	}
	
	private String getRrule(){
		String stdsdring = "RRULE";
		
		ComponentList cL = mCalendar.getComponents();		
		if(cL!=null)if(cL.size()>0){
			
			if(cL.get(0)==null){
				return null;
			}
			
			Property pp = ((Component)cL.get(0)).getProperty(stdsdring);
			
			if(pp!=null){
				//println("efg: " + pp.toString());
				String recurrenceString = pp.toString().trim();
				//
				// Jetzt haben wir die rrule vom VCalendar gelesen.
				// Und können sie bearbeiten.
				//
				
								
				Parameter wkst = pp.getParameter("WKST");
				if(wkst==null){
					// in Android wird wkst benötigt.
					if(!recurrenceString.endsWith(";")){
						recurrenceString+=";";
					}
					recurrenceString+="WKST=MO";
					
				}else{
					System.out.println("WKST ist im Citadel Kalender bereits vorhanden\n und wird deshalb nichtmehr eingefügt.");
				}
				
				recurrenceString = recurrenceString.replace("RRULE:", ""); // wichtig!
				
				return recurrenceString;
			}else{
				return null;
			}
			/*
			ParameterList params = pp.getParameters();
			

			// falls vorhanden, TZID auslesen.
			try{
				Parameter freq = params.getParameter("FREQ");
				
				
				
				ret += freq.toString();
				
				if(freq.getValue().equals("YEARLY")){
					// Monat und Tag werden benötigt.
				}
				
			
			}catch(Exception e){
				
			}	
			
			// auslesen der Property, also dem String in dem Zeit und Datum gespeichert sind.						
			rrProp = pp.getValue();	
			
			println(pp.toString() + " > " + ret);
			
			*/
		}		
		
		
		
		return null;
		
	}
	
	public String getSummary() {
		return getPropValue("SUMMARY");
	}
	
	public String getServerPath() {
		return this.serverPath;
	}
	

	

	
	public String toString(){
		return mCalendar.toString();
	}




	
	public Integer getAndroidId(){
		return androidId;
	}
	
/*
	public int getAndroidId() {
		return this.andoidId;
	}
*/

	public Integer getEtag() {
		return this.etag;
	}

	/*
	public void ssetAndroidId(int androidId) {
		this.andoidId = androidId;
	}
	 */
	
	/**
	 * Konsolenersatz. der String konsolenersatz kann, falls keine 
	 * Konsole vorhanden ist, in einem Textfeld ausgegeben werden.
	 * @param string
	 */
	public void println(String string){
		konsolenersatz.println(string);
	}	
	public String getKonsolenersatz(){
		return konsolenersatz.ka;
	}
	

	/**
	 * Gibt die Component mit index loc zurück, und, erstellt sie
	 * falls sie noch nicht existiert. normalerweise ist loc == 0.
	 * @param loc
	 * @return
	 */
	public Component getOrAddComponent(int loc){		
		ComponentList comp = mCalendar.getComponents();
		if(comp.size()>loc){
			Component ev = (Component)comp.get(loc);
			return ev;
		}else{
			VEvent ve = new VEvent();
			mCalendar.getComponents().add(ve);
			return getOrAddComponent(loc);
		}
			
	}
	
	/**
	 * UID des VCalendars eindeutig setzten
	 * @param inUid
	 * @throws URISyntaxException
	 */
	public void setUid(String inUid){	
		
		String vorhUID = getUid();
		if(vorhUID == null){		
			// In der VCard ist noch keine UID vorhanden. jetzt einfügen.
			Component ev = getOrAddComponent(0);
			Uid newUid = new Uid(inUid);
			ev.getProperties().add(newUid);
		}else{
			if(!vorhUID.trim().equals(inUid.replace("UID:", "").trim())){
				println("Es wird versucht zwei unterschiedliche UIDs einzufügen: " + inUid.replace("UID:", "").trim() + " " + vorhUID.trim() + " VCard: " + getSummary());
				//System.out.println("Android_id: " + this.getAndroidId());
				System.out.println("ServerPfad: " + this.getServerPath());
				// es wird versucht eine andere UID einzufügen. Darf garnicht sein.
				throw new ArrayIndexOutOfBoundsException();
			}
		}
	}
	
	


	public void setSummary(String summaryText){
		Component ev = getOrAddComponent(0);
		ev.getProperties().add(new Summary(summaryText));
	}


	public void setLocation(String newLoc) {
		Component ev = getOrAddComponent(0);		
		ev.getProperties().add(new Location(newLoc));
	}


	public void setDescription(String string) {
		Component ev = getOrAddComponent(0);
		ev.getProperties().add(new Description(string));			
	}
	
	/*
	public void setDtStart(Long long1) throws ParseException  {
		Component ev = getOrAddComponent(0);		
		ev.getProperties().add(new DtStart(new DateTime(long1)));			
	}
	 */
	
	
	public void setDtStart(String long1) throws ParseException  {
		DtStart einfuegen = new DtStart(new DateTime(long1));
		
		String that = getPropValue("DTSTART");
		if(that != null){
			if(!einfuegen.getValue().equals(that)){
				// es wird versucht zwei unterschiedliche DTSTART Werte einzutragen.
				println(einfuegen.getValue() + " != " + that);
				throw new ArrayIndexOutOfBoundsException();
			}			
		}else{
			Component ev = getOrAddComponent(0);		
			ev.getProperties().add(einfuegen);
		}
	}
	
	public void setDtStart(Long date) throws ParseException   {
		Component ev = getOrAddComponent(0);
		ev.getProperties().add(new DtStart(new DateTime(date)));				
	}	
	
	public void setDtEnd(Long date) throws ParseException   {
		Component ev = getOrAddComponent(0);
		ev.getProperties().add(new DtEnd(new DateTime(date)));				
	}
	
	public void setDtEnd(String date) throws ParseException   {
		Component ev = getOrAddComponent(0);
		ev.getProperties().add(new DtEnd(new DateTime(date)));				
	}
	

	public void setDuration(String duration) {		
		Duration d = new Duration();
		d.setValue(duration);		
		
		Component ev = getOrAddComponent(0);
		ev.getProperties().add(d);		
	}
	
	public void setRrule(String rrule) throws ParseException{
			if(rrule==null)return;
		
		
			rrule=rrule.replace("RRULE:", "").trim();
			//replace("WKST=MO", "")
			
			//
			// Entfernen von WKST=...; Regeln
			//
			int s = rrule.indexOf("WKST=");
			int e = rrule.substring(s+5).indexOf(";") + 5 + s; // WKST= sind 5 Zeichen.
			if(s==e)throw new ArrayIndexOutOfBoundsException();
			while(rrule.contains(";;")){rrule = rrule.replace(";;", ";");}
			while(rrule.contains("\n")){rrule = rrule.replace("\n", "").replace("\r", "");}			
			if(s!=-1){
				if(e-5>s){
					System.out.println("vorher: " + rrule + " s: " + s + " e: " + e);			
					
					if(rrule.length() >= e+1){
						rrule = rrule.substring(0,s) + rrule.substring(e+1);
					}else{
						rrule = rrule.substring(0,s);
					}
					System.out.println("nacher: " + rrule);
				}else{
					System.out.println("vorher: " + rrule);			
					rrule = rrule.substring(0,s);			
					System.out.println("nacher: " + rrule + " s: " + s + " e: " + e);
				}
			}

		
		
		
		Component ev = getOrAddComponent(0);
		//try{
			ev.getProperties().add(new RRule(rrule));
		/*}catch(IllegalArgumentException e){
			System.out.println("ExcRRule: " + rrule + " sum: " + this.getSummary());
			e.printStackTrace();
			
		}
		*/
	}
	
	public void setServerPath(String href) {
		this.serverPath=href;		
	}

	
	public void setAndroiId(Integer valueOf) {
		androidId = valueOf;		
	}

	


	
	

	public ContentValues getContentValues(String accountUserName, int accountId) {
		android.content.ContentValues cv = new android.content.ContentValues();
	
		cv.put("calendar_id", accountId);		
		
		cv.put("_sync_id", getUid());
		
		cv.put("_sync_account_type", Constants.ACCOUNT_TYPE);		
		//EXCEPTION!!  cv.put("displayName", Constants.ACCOUNT_DISPLAY_NAME);
		cv.put("_sync_account", accountUserName);			    
		//cv.put("selected", 1);
		//cv.put("visibility", 1);
		
		
		cv.put("guestsCanModify", 1);

		/*
    	cv.put("eventStatus", 1);
    	cv.put("transparency", 0);    	  	
    	cv.put("visibility", 0);				       	
       	//cv.put("hidden", 0);
       	cv.put("selected", 1);	  
		*/
		
		String bescheibung = getPropValue("SUMMARY");			
    	if(bescheibung!=null){
    		cv.put("title", bescheibung);
    		if(dbg)println("getContentValues, DESCRIPTION eingefügt: " + bescheibung);
    	}
    	else{
    		if(dbg)println("getContentValues, DESCRIPTION wurde in VCalendar nicht gefunden!");
    	}    	
    	
    	String zusammenfasung = getPropValue("DESCRIPTION");
    	if(zusammenfasung!=null){
    		cv.put("description", zusammenfasung);
    		if(dbg)println("getContentValues, SUMMARY eingefügt: " + zusammenfasung);
    	}
    	else{
    		if(dbg)println("getContentValues, SUMMARY wurde in VCalendar nicht gefunden!");
    	}
    		
	    	Date dtstart = getTimeProp("DTSTART");	    	 
	    	if(dtstart!=null){
	    		cv.put("dtstart", dtstart.getTime());
	    		if(dbg)println("getContentValues, DTSTART eingefügt: " + dtstart.toGMTString());
	    	}
	    	else{
	    		if(dbg)println("getContentValues, DTSTART wurde in VCalendar nicht gefunden!");
	    		throw new ArrayIndexOutOfBoundsException();
	    	}
	    	
	    	
		   	String rrule = this.getRrule();
	    	if(rrule!=null){
	    		
	    		rrule = rrule.replace("RRULE:", ""); // wichtig!
	    		
	    		// TODO Mapping zwischen Recurrences einbauen.
	    		//rrule = "FREQ=WEEKLY;WKST=MO;BYDAY=TU";

	    		
	    		cv.put("visibility", 0); // komisch, aber ohne das geht es nicht.
	    	//	cv.put("eventTimezone", "UTC");
	    		
	    		
	    		cv.put("rrule", rrule);
	    		cv.put("duration", "P3600S");	
	    				
	    			    		
	    		println("getContentValues, rrule eingefügt: " + rrule);	    		
	    	}else{
	    		// DTEND wird nur eingefügt, falls keine rrule vorhanden ist.
	    		// http://developer.android.com/reference/android/provider/CalendarContract.Events.html
	    			    		
	    		System.out.println("****************** kein RRULE vorhande. DTEND wird eingefügt.");
	    		Date dtend = getTimeProp("DTEND");	    	
	    	
			   	if(dtend!=null){
			   		cv.put("dtend", dtend.getTime());			   		
			   	}else{
			   		//
			   		// TODO ggf. durch duration ersetzten, falls eine Recurrence eingefügt wird.
			   		//
			    	if(dbg)println("getContentValues, DTEND wurde in VCalendar nicht gefunden!");
			    	
			    	if(dtstart!=null){ // wurde eigentlich oben schon geprüft.		    		
			    		cv.put("dtend", dtstart.getTime() +1000);
			    	}
			    }
			   	
			   	
	    	}

    	
	    	// geklaut von Jahrestag Martina:
	    	/*
	    	cv.put("allDay", "1");
	    	cv.put("rrule", "FREQ=YEARLY;WKST=MO;BYMONTHDAY=12;BYMONTH=1");
	    	cv.put("dtstart", "1294819800000");
	    	//cv.put("timezone", "Europe/Berlin");
	    	cv.put("_sync_time", "2011-01-08T04:12:50.000Z");
	    	cv.put("duration", "P3600S");
	    	//cv.put("dtend", "0");
		   	*/
	    	
	    	/*
	    	cv.put("rrule", "FREQ=WEEKLY;WKST=SU;BYDAY=WE");
	    	cv.put("allDay", 1);   // 0 for false, 1 for true
	    	cv.put("eventStatus", 1);
	    	cv.put("hasAlarm", 1); // 0 for false, 1 for true
	    	cv.put("duration","P3600S");
			*/
		   	
    	
    	if(hasAlarm()){
    		cv.put("hasAlarm", 1);
    	}
    	
    	String ort = getPropValue("LOCATION");
    	if(ort!=null)cv.put("eventLocation", ort);
    	else{
    		if(dbg)println("getContentValues, LOCATION wurde in VCalendar nicht gefunden!");
    	}
    	

    	
    	
    	/*
    	cv.put("hasAlarm", 0);
    	cv.put("reminder_duration" , 20);
    	cv.put("alerts_rintone", "conent://media/internal/audio/media/28");    	
    	cv.put("reminder_type",0);
    	cv.put("alerts_vibrate",1);
    	cv.put("selected",1);    		
    	// mal sehen ob eine Wiederholung möglich ist.
    	//cv.put("rrule", "FREQ=YEARLY;WKST=SUBYMONTHDAY=6,BYMONTH=12");
    	
    	
    	cv.put("allDay", 0); 	
    	*/
    	

		return cv;
	}

	
	

	
	

	public void setStartEnd(DtStart aStart, DtEnd aEnd) {
		if(aEnd==null || aStart ==null){
			throw new ArrayIndexOutOfBoundsException();
		}
		
		Component ev = getOrAddComponent(0);
	
		ev.getProperties().add(aStart);
		ev.getProperties().add(aEnd);
		
		/*} catch (ParseException e) {
			for(int i=0;i<15;i++){
				e.printStackTrace();
			}
		}*/
		
	}


	private boolean hasAlarm(){
		ComponentList components = mCalendar.getComponents();
	
		
		int i = 0;
		while(i<components.size()){
			Component a = (Component)components.get(i);
			if(a.getProperties("TRIGGER").size()>0){
				// es handelt sich anscheinend um einen Alarm-Eintrag mit Trigger.
				return true;
			}			
			i++;
		}
		return false;
		
	}
	
	public void setAlarm(long readAlarm) {
			
		
		DateTime alarm = new DateTime(readAlarm);
		Component valarm = new VAlarm();	
		valarm.getProperties().add(new Trigger(alarm));
		mCalendar.getComponents().add(valarm);	
		System.out.println("Alarm Added:: " + mCalendar.toString());
		
		
	}







	
	
	
	
}
