package WorkarroundExternalBugs;

import java.util.HashMap;

public class Workarounds {
	
	/**
	 * Problem: Citadel liefert auf ein PROPFIND eine XML Datei
	 * Jdom stürtzt beim Parsen dieser XML Datei mit einem 
	 * StringIndexOutOfBoundsException ab. Dieses Workarround löst das
	 * Problem vorübergehend, sodass Gevil-Sync weiter entwickelt werden kann.
	 * 
	 * @param in String der xml enthält.
	 * @return gefixter xml String.
	 */
	public static String fixXMLforJdom(String in){
		/*
		 * Dank jdom-1.1.1-android-fork.jar
		 * ist dieser Workarround überflüssig geworden.
		 *  
		 */		
		return in;		
	}
	
	
	// TODO es fehlt noch der ical4j-vcard Bugfix. Dieser sollte von der Klasse (?) auchnoch hierher verschoben werden.
	// einer von zweieen ist schon hier unten:
	// einmal: auslesen aus handy
	// einmal: auslesen vom Server ?!
	
	
	/**
	 * Problem: ical4j-vcard hängt sich auf, wenn eine Zeile mit einem
	 * Semikolon(;) ende. Es geht davon aus dass diese Felder beschrieben sind
	 * 
	 * @param vcin VCard String der nach der Vorbereitung von 
	 * ical4j-vcard eingelesen werden soll.
	 */
	public static String fixVCardForical4j_vcard(String vcin){
			String vcRet = "";
			String[] lines = vcin.split("\n");
		
			
			HashMap<String, Boolean> zeilenMap = new HashMap<String, Boolean>(lines.length);
			
			for(int i = 0; i<lines.length;i++){
				//
				// keine Zeile darf mit ; enden.
				//
				if(lines[i].trim().endsWith(";")){
					lines[i] += "TODO ical4j-vcard BUG @Workarounds";
				}				

				
				//
				// mehrfach vorkommende Zeilen entfernen.
				//
				String line = lines[i].trim();
				
				if(zeilenMap.get(line)!=null){
					// Diese Zeile ist bereits vorhanden.
					//System.out.println("@Bug Workaround, Zeile in Card von Citadel mehrfach vorhanden: " + line);
					lines[i] = "";
				}else{				
					zeilenMap.put(line, true);
				}
				
				// 
				// Ergebnis bauen.
				//
				if(line != ""){
					vcRet +=lines[i].trim() + "\n";
				}
			}
			
			/*
			if(doppelteZeile){
				System.out.println("Workarrounds hat VCard folgendermasen verändert: \n" + vcin + "\n.\n" + vcRet);
			}
			*/
		
		return vcRet;
		
	}


	public static String fixVCalendarForical4j_vcard(String vcin) {
		String vcRet = "";
		String[] lines = vcin.split("\n");
	
		
		HashMap<String, Boolean> zeilenMap = new HashMap<String, Boolean>(lines.length);
		
		for(int i = 0; i<lines.length;i++){
			//
			// keine Zeile darf mit ; enden.
			//
			if(lines[i].trim().endsWith(";")){
				lines[i] += "TODO ical4j-vcard BUG @Workarounds";
			}

			
			//
			// mehrfach vorkommende Zeilen entfernen.
			//
			String line = lines[i].trim();
			
			if(zeilenMap.get(line)!=null){				
				lines[i] = "";
			}else{				
				zeilenMap.put(line, true);
			}
			
			/* kam z.B. so von Citadel:
			DTSTART;TZID=3DUTC:20120206T140000
			DTEND;TZID=3DUTC:20120206T150000
			RRULE:FREQ=3DDAILY;COUNT=3D4
			*/
			if(lines[i].contains("TZID=3D")){
				lines[i] = lines[i].replace("3D", "");
			}
			if(lines[i].contains("RRULE")){
				lines[i] = lines[i].replace("3D", "");
			}			
			
			
			// 
			// Ergebnis bauen.
			//
			if(line != ""){
				vcRet +=lines[i].trim() + "\n";
			}
		}		

	
	return vcRet;
	}

}
