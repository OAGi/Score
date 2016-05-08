package org.oagi.srt.common;


import java.net.URL;

import org.apache.log4j.PropertyConfigurator;

/*
 * @author Yunsu Lee
 */

public class Log4jProperties {

	private static final String propFile = "/log4j.properties";

	private static Log4jProperties instance = new Log4jProperties();

	public static Log4jProperties getInstance() {
		return instance;
	}

	public synchronized void loadProperties() {
		
		System.out.println("start log4j loadProperties");
		URL url = this.getClass().getResource(propFile);
		String fp = null;
		if( url != null ) {
			fp = url.getFile();
			System.out.println("Properties file [" + fp + "] is found..");
			PropertyConfigurator.configure(fp);
		}else{
			System.out.println("Properties file [" + propFile + "] not found..");
		}
	
	}
	
}
