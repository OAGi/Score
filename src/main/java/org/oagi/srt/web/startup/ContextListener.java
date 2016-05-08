package org.oagi.srt.web.startup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.oagi.srt.common.Log4jProperties;
import org.oagi.srt.common.SRTConstants;

/**
 * 
 * @author Yunsu Lee
 */
public final class ContextListener implements ServletContextListener {

    private Logger _logger = Logger.getLogger(ContextListener.class);

    /**
     * Constructor for ContextListener.
     */
    public ContextListener() {
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
    	
    	initLog();
    	System.out.println("");
        System.out.println("*********************************************");
        System.out.println("   Starting......");
        System.out.println("       "
        		+ SRTConstants.PRODUCT_NAME);
        System.out.println("*********************************************");
        System.out.println("");
        try {
            SRTInitializer initializer = SRTInitializer.getInstance();
            initializer.init(servletContextEvent.getServletContext());
            System.out.println(SRTConstants.PRODUCT_NAME + " " + " Engine started..");
        } catch (SRTInitializerException e) {
            System.err.println(e);
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("");
        System.out.println("*********************************************");
        System.out.println("   Stopping......");
        System.out.println("       "
        		+ SRTConstants.PRODUCT_NAME + " ");
        System.out.println("*********************************************");
        System.out.println("");

        try {
            SRTInitializer initializer = SRTInitializer.getInstance();
            initializer.release();
            System.out.println(SRTConstants.PRODUCT_NAME + " " + " Engine stopped..");
        } catch (SRTInitializerException e) {
            System.err.println(e);
        }
    }
    
    
    private void initLog() {
		try{
			Log4jProperties log4jp = Log4jProperties.getInstance();
			log4jp.loadProperties();
		}catch(Throwable t){
			t.printStackTrace();
		}
	}
}
