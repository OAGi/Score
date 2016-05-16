package org.oagi.srt.web.startup;

import org.apache.log4j.Logger;
import org.chanchan.common.persistence.db.ConnectionPoolManager;
import org.chanchan.common.util.ServerProperties;
import org.oagi.srt.common.SRTConstants;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

/**
 * 
 * @author Yunsu Lee
 */
public class SRTInitializer {

    private static Logger _logger = Logger.getLogger(SRTInitializer.class);
    private static SRTInitializer _instance = null;
    private static Object _lock = new Object();
    private static boolean _started = false;
    private String _propFile = SRTConstants.SRT_PROPERTIES_FILE_NAME;

    public static SRTInitializer getInstance() {
        if (_instance == null) {
            synchronized (_lock) {
                _instance = new SRTInitializer();
            }
        }
        return _instance;
    }

    private SRTInitializer() {
        System.out.println("SRTInitializer...");
    }

    public void init(ServletContext context) throws SRTInitializerException {
        if (_started) {
            System.out.println("SRTInitializer called TWICE! Return without doing anything...");
            return;
        }
        System.out.println("");
        System.out.println("########################################");
        System.out.println("# Initialize System Resource Start     ");

        System.out.println("----------------------------------------");
        System.out.println("# Initializing ServerProperties");
        ServerProperties props = ServerProperties.getInstance();
        try {
    		InputStream is = SRTInitializer.class.getResourceAsStream(_propFile);
    		if (is == null) {
    			_logger.debug("Properties file [" + _propFile + "] not found");
    			throw new SRTInitializerException(_propFile + " not found!");
    		}
    		try {
                String properties_path = context.getRealPath("/WEB-INF/classes" + _propFile);
                props.setServerPropertiesURL(properties_path);

    			props.load(is, true);
    		} catch (IOException e) {
    			throw new SRTInitializerException(_propFile + " cannot be read...");
    		}

        } catch (Exception e) {
            System.out.println("[SRTInitializer] Fail to Getting "
            		+ SRTConstants.SRT_PROPERTIES_FILE_NAME + " URL : "
                        + e.toString());
        }
        // Initializing Step-2 : DBConnectionPool ����...
        System.out.println("----------------------------------------");
        System.out.println("# Initializing DBConnectionPool");
        try {
			ConnectionPoolManager cpm = ConnectionPoolManager.getInstance();
			String poolName = cpm.getDefaultPoolName();
			System.out.println("DefaultPoolName:" + poolName);
			Connection dbConnection = cpm.getConnection(poolName);
			dbConnection.close();
			_logger.debug("DB Connection Pool initialized...");
			cpm.release();
		} catch (Exception e) {
			_logger.error("[SRTInitializer] Fail to Creating Connection Pool : "
					+ e.toString(), e);
			throw new SRTInitializerException("[SRTInitializer] Fail to Creating Connection Pool : "
					+ e.toString());
		}

        System.out.println("----------------------------------------");
        System.out.println("# Initialize System Resource End     ");
        System.out.println("########################################");
        System.out.println("");
        _started = true;
    }

    public void release() throws SRTInitializerException {
    	_started = false;

        System.out.println("----------------------------------------");
        System.out.println("destroy Connection Pool");
        try {
            ConnectionPoolManager cpm = ConnectionPoolManager.getInstance();
            cpm.forceRelease();
            // DBConnectionManager.release();
        } catch (Exception e) {
            System.out.println("[SRTInitializer] Fail to Destroyting Connection Pool : "
                        + e.getMessage());
        }
    }
    
}
