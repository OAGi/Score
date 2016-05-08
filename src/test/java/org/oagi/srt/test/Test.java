package org.oagi.srt.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Test {
	
	public static void main(String args[]) {
		
		List<String> li = new ArrayList<String>();
	    for(int i = 0; i < 5; i++){
	        li.add("str" + i);
	    }

	    
	    Iterator it = li.iterator();
	    while(it.hasNext()) {
	    	String s = (String) it.next();
	    	if(s.equals("str4"))
	    		it.remove();
	    }
	    System.out.println(li);
	}

}
