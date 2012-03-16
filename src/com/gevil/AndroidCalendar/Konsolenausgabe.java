package com.gevil.AndroidCalendar;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class Konsolenausgabe{
	
	public String ka = "";
	
	/**
	 * Konsolenersatz. der String konsolenersatz kann, falls keine 
	 * Konsole vorhanden ist, in einem Textfeld ausgegeben werden.
	 * @param string
	 */
	public void println(String string){
		
		ka += "\n" + string;
		System.out.println("Konsolenersatz: " + string);
	}
	
	
	
	public void printStackTrace(Throwable t){
		printStackTrace("",t);
	}	
	
	public void printStackTrace(String x, Throwable t){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
	    t.printStackTrace(pw);
	    pw.flush();
	    sw.flush();
	    
	    boolean anzeigen= true;
	    String ret = "";
	    String[] a = sw.toString().split("\n");
	    for(String line:a){
	    	if(line.contains("at com.gevil"))anzeigen = true;
	    	if(line.contains("Exception"))anzeigen = true;
	    	if(line.contains("at android"))anzeigen = false;
	    	
	    	if(anzeigen)ret += "\n" + line;
	    	
	    }
	    println("\n" + x + ret);
	    //println("\n"+ x + sw.toString());
	    
	    // auch auf der Eclipse-Konsole ausgeben.
	    t.printStackTrace();
	}
	
}
