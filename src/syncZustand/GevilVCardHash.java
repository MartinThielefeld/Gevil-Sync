package syncZustand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.text.TextUtils;

import syncZustand.GevilVCardHash.PropertyWithFlag;

import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.Property.Id;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.parameter.Type;


public class GevilVCardHash {

	GevilVCard mGevilVCard;
	VCard mVCard;
	
	GevilVCardHash(GevilVCard card){
		mGevilVCard = card;
		mVCard = card.getVCard();
		/*
		rehashSearchByname();
		rehashSearchByType();
		*/
	}
	

	
	public Property getUnmarkedProperty(PropertyByValueSearcher byValue, PropertyByTypeSearcher byType){
		// schnelle Hashing-Version Probieren
		int typeKey = byValue.getTypeKEYForUninsertedProperty();
		// 
		PropertyWithFlag wertInByType = byType.searchByType.get(typeKey);
		
		boolean dbg = false;
		
		if(wertInByType!=null)
		if(!wertInByType.flag){
			// wegen byValue.getTypeKEYForUninsertedProperty() ist sichergestellt, dass dieses Property in byValue nicht markiert ist.
			// wegen !wertInByType.flag ist sichergestellt, dass das Property auch in byType nicht markiert ist.
			
			// jetzt noch markieren dass es mittlerweise gefunden wurde.
			wertInByType.setFlag();
			byValue.getPropertyByValueOnce(byValue.getValueKEYForUninsertedProperty());
			
			if(dbg)System.out.println("UNGLEICH ERFOLG(1) bei Schnellem Hashing: " + wertInByType.getProp().getValue());
			
			return wertInByType.getProp();			
		}else{
			//
			// Jetzt könnte man, mit wertInByType.setFlag() dies markieren, und dann per Hashing weiter suchen.
			// Leider ist dann nicht bewiesen, dass wertInByType jemals zurück gegebn wurde.
			//
		}
		
		
		//
		// Sicherstellen, dass wirklich IMMER ALLE Properties gefunden werden.
		// 		
		Iterator<PropertyWithFlag> byTypeIt = byType.searchByType.values().iterator();
		Iterator<PropertyWithFlag> byValueIt = byValue.searchByname.values().iterator();
		
		int schleifendurchlaufe = 0;
		
		while(byTypeIt.hasNext()){
			//PropertyWithFlag a = byTypeIt.next();
			
			PropertyWithFlag propertyWithFlagDasMehrerePropertiesHabenKann = byTypeIt.next();
			
			// ACHTUNG! zu jedemType können mehrere Properties gespeichert sein.
			PropertyWithFlag a;				
			if(propertyWithFlagDasMehrerePropertiesHabenKann.flag){
				a=propertyWithFlagDasMehrerePropertiesHabenKann;
			}else{
				a=propertyWithFlagDasMehrerePropertiesHabenKann.getUnmarkedPropertyWithFlag();
			}			
			 
			while(a != null){
				if(dbg)System.out.println("UNGLEICH folgende key value paar kommt im Iterator vor: " + a.getProp().getValue());			
				//if(!a.flag){
					
					// schauen, ob diese im byTypeSearcher auch vorhanden ist, und nicht markiert.
					// einfach mal mit hash probieren
					
					// erstmal mit hash probieren. NOCH nicht markieren
					Property f = byValue.getPropertyByValue(a.getProp().getValue());
					if(a.getProp() == f){
						// JETZT im ByTypeSearcher markieren dass das schon gefunden wurde.
						byValue.getPropertyByValueOnce(a.getProp().getValue());
						a.setFlag();
						
						if(schleifendurchlaufe>5)System.out.println("Schleifendruchlaufe in getUnmarkedProperty: " + schleifendurchlaufe );
						if(dbg)System.out.println("UNGLEICH ERFOLG(2) bei langsamem Hashing: " + a.getProp().getValue());
						return a.getProp();
					}else{
						if(a!=null && f!=null){
							//System.out.println("UNGLEICH (a,hashErgebnis): " + a.getProp().toString() + " UNGLEICH (!=) " + f.toString());
						} else if(f==null){
							//System.out.println("UNGLEICH (a,hashErgebnis): " + a.getProp().toString() + "UNGLEICH (!=) null");
						}
					}
						
					// wir müssen 'beweisen', dass diese Property nicht irgenwdo anderst in der Liste verborgen ist.
					while(byValueIt.hasNext()){
						schleifendurchlaufe++;
						PropertyWithFlag b = byValueIt.next();
						if(!b.flag){
							if(a.getProp()==b.getProp()){
								if(schleifendurchlaufe>5)System.out.println("Schleifendruchlaufe in getUnmarkedProperty: " + schleifendurchlaufe );
								if(dbg)System.out.println("UNGLEICH ERFOLG(3)leider kein Hashing: " + a.getProp().getValue());
								return a.getProp();
							}	else{
								if(a!=null && b!=null){
									//System.out.println("UNGLEICH@2 (a,hashErgebnis): " + a.getProp().toString() + "\nUNGLEICH@2 (!=) " + b.getProp().toString());
								} else if(b==null){
									//System.out.println("UNGLEICH@2 (a,hashErgebnis): " + a.getProp().toString() + "\nUNGLEICH@2 (!=) null");
								}
							}					
						}
					}
					
					if(dbg)System.out.println("UNGLEICH die Property: " + a.getProp().getValue() + " wurde schon von byValue gefunden.");
					a.setFlag();

				a = propertyWithFlagDasMehrerePropertiesHabenKann.getUnmarkedPropertyWithFlag();
			}
		}
		
		return null;
	}
	
	
	public PropertyByValueSearcher propertiesByValue(Id eMailOderPhone){
		return new PropertyByValueSearcher(eMailOderPhone);
	}
	
	public class PropertyByValueSearcher{
		
		HashMap <String, PropertyWithFlag> searchByname = new HashMap <String, PropertyWithFlag> (0);
		
		
		/**
		 * Konstruktor
		 * @param eMailOderPhone
		 */
		PropertyByValueSearcher(Id eMailOderPhone){
			searchByname = new HashMap <String, PropertyWithFlag> (0);
			rehashSearchByValue(searchByname, eMailOderPhone);				
		}
		
		
		/**
		 * Sucht die Email Adresse in der VCard an Hand einer HashMap.
		 * Es wird davon ausgegangen dass diese Adresse ins Android AB eingefüt wurde,
		 * und deshalb nichtmehr gesucht werden soll. Deshalb wird das Flag gesetzt.
		 * @param email
		 * @return Property, welche die Email und deren Properties enthält.
		 */
		public Property getPropertyByValueOnce(String email){				
			PropertyWithFlag ret = searchByname.get(email);
			
			if(ret!=null && !ret.getFlag()){
				//ret.setFlag();
				searchByname.remove(email); // ACHTUNG remove key, nicht Value!
				return ret.getProp();
			}else{
				// diese Property wird nichtmehr zurück gegeben, weil sie schon markiert ist.
				// FLag wird nichtmehr verwendet.
				return null;
			}
		}
		
		
		
		/**
		 * Sucht die Email Adresse in der VCard an Hand einer HashMap.
		 * @param email
		 * @return Property, welche die Email und deren Properties enthält.
		 */
		public Property getPropertyByValue(String email){	
			
			PropertyWithFlag x = searchByname.get(email);		
			if(x!=null){		
				return x.getProp();
			}else{
				return null;
			}
		}
		
		
		/**
		 * Gibt der Reihe nach alle Properties zurück, die noch nicht
		 * durch getPropertyByValueOnce abgerufen wurden.
		 * @return Property
		 */
		public Property getUninsertedPropertyOnce(){
		    String PropertyKey = getValueKEYForUninsertedProperty();		    
		    Property emailProperty = getPropertyByValueOnce(PropertyKey);
		    return emailProperty;
		}
		
		
		
		/**
		 * Sucht ein Property das noch nicht markiert ist und gibt den key dazu zurück.
		 * Mit diesem Key kann dann in getPropertyByValue oder getPropertyByValueOnce das 
		 * dazugehörige Property abgerufen werden. Außerdem kann geschaut werden, ob diese 
		 * Propery in einer PropertyByType schon markiert ist.
		 * 
		 * Achtung! Property Flag wird hier nicht gesetzt. Setzen durch suche in getPropertyByValueOnce
		 * @return key, dazugehöriger Value kann mit getPropertyByValue erlangt werden.
		 */
		private String getValueKEYForUninsertedProperty(){			
			Iterator<PropertyWithFlag> nextUnmarkedProperty = searchByname.values().iterator();
			 
			// zur Komplexität: Der Iterator gibt immer das erste Element zurück, WEIL, Elemente
			// die schon markiert sind, aus der Liste entfernt sind.  O(1)
			
			while(nextUnmarkedProperty.hasNext()){
				PropertyWithFlag prop = nextUnmarkedProperty.next();
				if(!prop.flag){  // Flag wird nichtmehr verwendet. Stattdessen werden die Elemente gelöscht.
					return prop.getProp().getValue();
				}else{
					throw new IndexOutOfBoundsException();  // würde mich interessieren ob das vorkommt.
				}
			}
			return null;
			
		}
		
		/**
		 * Das gleiche wie getValueKeyForUninsertedProperty, jedoch kann dieser Key mit einem 
		 * PropertyByTypeSearcher Objekt verwendet werden.
		 * @return
		 */
		private Integer getTypeKEYForUninsertedProperty(){			
			Iterator<PropertyWithFlag> nextUnmarkedProperty = searchByname.values().iterator();
			
			while(nextUnmarkedProperty.hasNext()){
				PropertyWithFlag prop = nextUnmarkedProperty.next();
				if(!prop.flag){
					if(prop.getProp().getParameters().size() > 0){
						return Integer.valueOf(prop.getProp().getParameters().get(0).getValue());
					}else{
						// TODO ist schon vorgekommen, wenn eine CitadelVCard eine Email ohne Typ hat.
					}
				}
			}
			return -1;
			
		}

		
		/**
		 * Hashmap aufbauen
		 * @param searchByname
		 * @param eMailOderPhone
		 */
		void rehashSearchByValue(HashMap <String, PropertyWithFlag> searchByname, Id eMailOderPhone){
			
			List<Property> emailAdressen = mVCard.getProperties(eMailOderPhone);	
			
			//List<Property> emailAdressen = mVCard.getProperties(Id.EMAIL);		
			Iterator<Property> emailAdressIterator = emailAdressen.iterator();
			while(emailAdressIterator.hasNext()){
				Property p = emailAdressIterator.next();
				try{
					if(!TextUtils.isEmpty(p.getValue())){				
						searchByname.put(p.getValue(), new PropertyWithFlag(p));
						
						/*
						HashList<Parameter> params = (HashList<Parameter>) p.getParameters();
						Iterator<Parameter> pIterator = params.iterator();
							while(pIterator.hasNext()){
								Parameter param = pIterator.next();					
								ret += "\n param.value: " + param.getValue();
							}
						*/
					}else{
						//System.out.println("leere Werte werden nicht eingefügt.");
					}
				}catch(Exception e){
					System.out.println("Exception in rehashSeachByName: " + mGevilVCard.getFN() + " " + e);
					e.printStackTrace();
				}
				
			}
		}
	}
	
	
	
	public PropertyByTypeSearcher propertiesByType(Id eMailOderPhone){
		return new PropertyByTypeSearcher(eMailOderPhone);
	}
	
	/**
	 * Sucht ValuesByType. Beispiel: Suchen einer Emailadresse in der VCard, die vom Typ HOME ist.
	 * @author Martin
	 *
	 */
	public class PropertyByTypeSearcher{

	HashMap <Type, PropertyWithFlag> searchByType;
		
		
	PropertyByTypeSearcher(Id eMailOderPhone){
		searchByType = new HashMap <Type, PropertyWithFlag> (0);
		rehashSearchByType(searchByType, eMailOderPhone);
	}
	
	
	
	
	/**
	 * Sucht die Email Adresse in der VCard an Hand einer HashMap.
	 * Es wird davon ausgegangen dass diese Adresse ins Android AB eingefüt wurde,
	 * und deshalb nichtmehr gesucht werden soll. Deshalb wird das Flag gesetzt.
	 * @param Android Phone type
	 * @return Property, welche die Email und deren Properties enthält.
	 */
	public Property getPropertyByTypeOnce(int type){
		
		PropertyWithFlag ret= searchByType.get(type);
		
		if(ret!=null){
			System.out.println("ret!=null");
			
			if(!ret.getFlag()){
				System.out.println("unmarkierte Property gefunden.");
				ret.setFlag();
				return ret.getProp();
			}else{
				// ggf. null
				System.out.println("suche weitere Properties");
				return ret.getUnmarkedPropertyOnce();
			}
		}else{
			System.out.println("in Hashmap wurde nix gefunden.");
			return null;
		}		
	}
	
	
	

	
	

	/**
	 * Hashmap aufbauen
	 * @param searchByType aufgebaute Hashmap
	 * @param eMailOderPhone z.B. Phone oder Email
	 */
	void rehashSearchByType(HashMap<Type, PropertyWithFlag> searchByType, Id eMailOderPhone){			
			boolean dbg = true;
		
			List<Property> emailAdressen = mVCard.getProperties(eMailOderPhone);	
			Iterator<Property> emailAdressIterator = emailAdressen.iterator();
			
			while(emailAdressIterator.hasNext()){
				Property p = emailAdressIterator.next();
				try{					
					// keine Null Werte einfügen.
					if(!TextUtils.isEmpty(p.getValue())){
						
						Type typ = mGevilVCard.propertyToVCardType(p, true);
						/*
						if(p.getParameters().size() > 0){
							typ = new Type(p.getParameters().get(0).getValue());
						}else{
							// wenn kein Type gefunden wird, wird dies immer auf OTHER gemappt.
							typ = new Type("OTHER");
						}
						*/
						
						
						if(dbg)System.out.println("Property zum einfügen in Hash wurde gefunden: " + p.getValue()+ ". Ihr typ ist: " + typ);
						
						PropertyWithFlag mP = searchByType.get(typ);
						if(mP==null){
							// noch nicht vorhanden, einfügen.
							searchByType.put(typ, new PropertyWithFlag(p));
						}else{
							// zur Liste hinzufügen
							mP.setFurhterProperty(p);
						}
					}else{
						if(dbg)System.out.println("leere Werte werden ignoriert: " + p.getValue());
					}

				}catch(Exception e){
					System.out.println("Exception in rehashSeachByName: " + mGevilVCard.getFN() + " " + e);
					e.printStackTrace();
				}
				
			}
		}
	
	
	

	
	}

	
	
	class PropertyWithFlag{
		Property prop;
		Iterator<PropertyWithFlag> futherPropertiesIterator;
		
		ArrayList<PropertyWithFlag> furtherProps;
		
		boolean flag = false;
		
		public PropertyWithFlag(Property prop) {			
			this.prop = prop;			
		}
		public Property getProp(){
			return prop;
		}
		public boolean getFlag(){
			return flag;
		}
		public void setFlag(){
			this.flag=true;
		}

		
		/**
		 * Falls es, in der Hashmap, zu einem Typ, mehr als eine Property gibt
		 * werden diese hier gespeichert.
		 * @param p
		 */		
		public void setFurhterProperty(Property p){
			if(furtherProps==null){
				furtherProps = new ArrayList<PropertyWithFlag>(0);
			}
			furtherProps.add(new PropertyWithFlag(p));
		}
		
		/**
		 * Gibt die nächste Property, die nicht markiert ist, zurück
		 * @return unmarked Property
		 */
		public Property getUnmarkedPropertyOnce(){
			if(futherPropertiesIterator == null){
				if(furtherProps==null) return null;
				futherPropertiesIterator = furtherProps.iterator();
			}
			while(futherPropertiesIterator.hasNext()){
				PropertyWithFlag pp = futherPropertiesIterator.next();
				if(!pp.getFlag()){
					pp.setFlag();
					return pp.getProp();
				}
				
			}
			return null;
		}
		
		/**
		 * Gibt die nächste Property, die nicht markiert ist, zurück
		 * @return unmarked Property
		 */
		public PropertyWithFlag getUnmarkedPropertyWithFlag(){
			if(futherPropertiesIterator == null){
				if(furtherProps==null) return null;
				futherPropertiesIterator = furtherProps.iterator();
			}
			while(futherPropertiesIterator.hasNext()){
				PropertyWithFlag pp = futherPropertiesIterator.next();
				if(!pp.getFlag()){
					return pp;
				}
				
			}
			return null;
		}
		
		public void resetIterator(){
			futherPropertiesIterator = null;
		}
		
		
	}
}
