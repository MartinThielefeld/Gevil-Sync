package syncZustand;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;


import android.provider.ContactsContract;

import net.fortuna.ical4j.vcard.Group;
import net.fortuna.ical4j.vcard.Group.Id;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.parameter.Type;

/*
 * macht aus den Properties die aus Android ausgelesen wurden
 * properties die von ical4j-vcard genutzt werden können.
 */
public class androidToVCardPropertyConversion {
	
	// TODO alles wieder auf private stellen und dann die add Methoden aus  conversion verwenden.
	
	
	
	protected static net.fortuna.ical4j.vcard.property.N parseN(String vorName, String nachName){		
		// präfixe
		String AndroidName = vorName + nachName;
		String[] Anrede = new String[]{""};
		
		// Achtung suffixBUG beachten! Ansonsten entsteht ein schwierig zu findender Fehler!
		String[] suffix = new String[]{"TODO:ical4j-vcard suffixBUG"}; // z.B. auser Dienst.
		
		//
		// experimentell: erkennen einiger bekannter prä- und suffixe.
		//
		if(AndroidName.toLowerCase().contains("herr")){
			Anrede = new String[] {"Herr"};
		}if(AndroidName.toLowerCase().contains("frau")){
			Anrede = new String[] {"Frau"};
		}if(AndroidName.toLowerCase().contains("prof.")){
			Anrede = new String[] {"Prof."};
		}if(AndroidName.toLowerCase().contains("dr.")){
			Anrede = new String[] {"Dr."};
		}// TODO ggf mehr Präfixe?
		 // TODO prä- oder suffixe aus nachName und vorName entfernen.		
		
		return new net.fortuna.ical4j.vcard.property.N(vorName, nachName, new String[] {""}, Anrede, suffix);
		//return new net.fortuna.ical4j.vcard.property.N("Thielefeld", "Peter", new String[] {"Peterle, Steuerle"}, new String[] {"Herr"}, new String[] {"a.D."})		
	}
	

	
	protected static net.fortuna.ical4j.vcard.property.Fn parseFn(String vorName){
		return new net.fortuna.ical4j.vcard.property.Fn(vorName);
		//return new new net.fortuna.ical4j.vcard.property.Fn("Peter"));
	}
	
	protected static net.fortuna.ical4j.vcard.property.Email parseEmail(String eMail, int androidType, TypeBiMap map){
		List<Parameter> emailTypeList = new java.util.ArrayList<Parameter>();	
		boolean dbg = false;
		
		
		
		// ACHTUNG! wenn als Typ Group.id.EXTENDET ausgewählt wird, steht in der erzeugten VCard.toString()
		// die Zeile .EMAIL:....
		// Wenn dies dann wieder von ical4j-VCard eingelesen werden soll, wird diese Adresse einfach übersprungen.
		/*
		if (androidType == android.provider.ContactsContract.CommonDataKinds.Email.TYPE_HOME){
			if(dbg)System.out.println("parseEmail TYPE_HOME");
			
			net.fortuna.ical4j.vcard.parameter.Type mailType = new net.fortuna.ical4j.vcard.parameter.Type("HOME");                			
			emailTypeList.add(mailType);
			
			//group = new Group(Group.Id.HOME);
		}else if (androidType == android.provider.ContactsContract.CommonDataKinds.Email.TYPE_WORK){
			if(dbg)System.out.println("parseEmail TYPE_WORK");
			group = new Group(Group.Id.WORK);
		}else if (androidType == android.provider.ContactsContract.CommonDataKinds.Email.TYPE_OTHER){
			if(dbg)System.out.println("parseEmail TYPE_OTHER");
			group = new Group(Group.Id.EXTENDED);
		}else if (androidType == android.provider.ContactsContract.CommonDataKinds.Email.TYPE_MOBILE){
			if(dbg)System.out.println("parseEmail TYPE_MOBILE");
			group = new Group(Group.Id.EXTENDED);
		}else{
			if(dbg)System.out.println("parseEmail else => TYPE_HOME");
			
			net.fortuna.ical4j.vcard.parameter.Type mailType = new net.fortuna.ical4j.vcard.parameter.Type("OTHER");                			
			emailTypeList.add(mailType);
			
		  // group = new Group(Group.Id.EXTENDED); NICHT verwenden siehe oben!!
		}*/		
		
		emailTypeList.add(map.toVCardType(map.mailType(), androidType));
		
		// falscher Konstruktor.  return new net.fortuna.ical4j.vcard.property.Email(group, eMail);
		return new net.fortuna.ical4j.vcard.property.Email(emailTypeList, eMail);
		
	
	}	
	
	
	protected static net.fortuna.ical4j.vcard.property.Telephone parseTelephone(String androidPhone, int type, TypeBiMap map){			
			List<Parameter> TelTypeList = new java.util.ArrayList<Parameter>();
			
			/*
			if (type == android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
            	net.fortuna.ical4j.vcard.parameter.Type TelType = new net.fortuna.ical4j.vcard.parameter.Type("mobile");                			
                TelTypeList.add(TelType);                
			} else if (type == android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_HOME) {
        		net.fortuna.ical4j.vcard.parameter.Type TelType = new net.fortuna.ical4j.vcard.parameter.Type("home");                			
            	TelTypeList.add(TelType);             
			}else if (type == android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_OTHER) {
        		net.fortuna.ical4j.vcard.parameter.Type TelType = new net.fortuna.ical4j.vcard.parameter.Type("other");                			
            	TelTypeList.add(TelType);
            }else{
        		net.fortuna.ical4j.vcard.parameter.Type TelType = new net.fortuna.ical4j.vcard.parameter.Type("unknown");                			
            	TelTypeList.add(TelType);
            } // TODO wenn das klappt, weitere types hier einfügen z.B. work fax etx.
			*/
			
			TelTypeList.add(map.toVCardType(map.phoneType(), type));
		try {
			return new net.fortuna.ical4j.vcard.property.Telephone(TelTypeList, androidPhone );
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			System.out.println(e + " beim Parsen einer Telefonnummer: " + androidPhone);
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e + " beim Parsen einer Telefonnummer: " + androidPhone);
			//e.printStackTrace();
		}		
		return null;
	}
	
	protected static net.fortuna.ical4j.vcard.property.Uid parseUID(String uid) throws URISyntaxException{
		URI u;
		//try {
			u = new URI(uid);
			return new net.fortuna.ical4j.vcard.property.Uid(u);
		/*} catch (URISyntaxException e) {
			System.out.println("Exception beim Parsen von UID: " + uid);
			return null;
		}*/
		
	}
	
	
	


	
}
