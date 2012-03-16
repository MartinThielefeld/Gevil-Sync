package syncZustand;

import java.util.HashMap;

import net.fortuna.ical4j.vcard.parameter.Type;
import android.provider.ContactsContract;

public class TypeBiMap {
	
		
		
		HashMap<String, String> vCardToAndroid;
		HashMap<String, String> androidToVCard;
		
		int Email = 0;
		int Telefon = 1;
		
		public int mailType(){
			return Email;
		}
		public int phoneType(){
			return Telefon;
		}
		
		private String makeString(int typ, int andTyp){
			return String.valueOf(typ) + "," + String.valueOf(andTyp);
		}
		private int getType(String s){
			if(s!=null)
			{
				String ret = s.split(",")[1];
				return Integer.parseInt(ret);
			}else{
				return -1;
			}
		}
		
		private String makeString(int typ, String andTyp){
			return String.valueOf(typ) + "," + String.valueOf(andTyp);
		}
		private String getReversType(String s){
			String ret = s.split(",")[1];
			return ret;
		}
		
		public TypeBiMap(){
			
			add(makeString(Email, ContactsContract.CommonDataKinds.Email.TYPE_HOME), makeString(Email, "HOME"));
			add(makeString(Email,  ContactsContract.CommonDataKinds.Email.TYPE_MOBILE), makeString(Email, "MOBILE"));
			add(makeString(Email,  ContactsContract.CommonDataKinds.Email.TYPE_OTHER), makeString(Email, "OTHER"));	//3
			add(makeString(Email,  ContactsContract.CommonDataKinds.Email.TYPE_WORK), makeString(Email, "WORK"));
			//add(makeString(Email, 404), makeString(Email, "HOME"));// 404 habe ich festgelegt, d.h. kein Typ gefunden und wird auf OTHER gemappt.
			
			
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE), makeString(Telefon, "MOBILE"));  	//2	
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_HOME), makeString(Telefon, "HOME"));   		//1
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT), makeString(Telefon, "ASSISTANT"));
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK), makeString(Telefon, "CALLBACK"));
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_CAR), makeString(Telefon, "CAR"));
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN), makeString(Telefon, "COMPANY_MAIN"));
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME), makeString(Telefon, "FAX_HOME"));
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK), makeString(Telefon, "FAX_WORK"));	
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_ISDN), makeString(Telefon, "ISDN"));
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_MAIN), makeString(Telefon, "MAIN"));
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_MMS), makeString(Telefon, "MMS"));		
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER), makeString(Telefon, "OTHER"));		// 7
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX), makeString(Telefon, "OTHER_FAX"));
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_PAGER), makeString(Telefon, "PAGER"));
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_RADIO), makeString(Telefon, "RADIO"));
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_TELEX), makeString(Telefon, "TELEX"));
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD), makeString(Telefon, "TTY_TDD"));
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_WORK), makeString(Telefon, "WORK"));		// 3 wird fälschl nach OTHER gemappt...
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE), makeString(Telefon, "WORK_MOBILE"));
			add(makeString(Telefon, ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER), makeString(Telefon, "WORK_PAGER"));
			//add(makeString(Telefon, 404), makeString(Telefon, "HOME"));// 404 habe ich festgelegt, d.h. kein Typ gefunden und wird auf HOME gemappt.
			  
		}
		
		
		public void add(String androidType, String type){
			if(vCardToAndroid==null)vCardToAndroid=new HashMap<String, String>(0);
			if(androidToVCard==null)androidToVCard=new HashMap<String, String>(0);			
			
			vCardToAndroid.put(type, androidType);
			androidToVCard.put(androidType, type);
		}
		
		
		public Type toVCardType(int t, int andType){
			
			boolean dbg = false;
			String ret = androidToVCard.get(makeString(t, andType));
			if(ret!=null){
				
				if(t==mailType()){
					if(dbg)System.out.println("%% Email TypeMap android: " + andType + " Ergebnis: " + ret);
				}else if(t==phoneType()){
					if(dbg)System.out.println("%% Phone TypeMap android: " + andType + " Ergebnis: " + ret);
				}
				return new Type(getReversType(ret));
			}else{
				if(dbg)System.out.println("%% Android-Type nicht gefunden! email(0) " + t + andType + " = " + makeString(t,andType));
				return new Type("OTHER");
			}
		}
		
		public int toAndroidType(int HandyOrMail, Type VCardType){			
			String type = vCardToAndroid.get(makeString(HandyOrMail, VCardType.getValue()));
			if(type!=null){
				return getType(type);
			}else{
				//System.out.println("toAndroidType keinHashTreffer: " + VCardType);
				throw new NullPointerException();				
			}
				
		}
		 

}
