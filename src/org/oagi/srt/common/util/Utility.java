package org.oagi.srt.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.UUID;

import org.chanchan.common.persistence.db.ConnectionPoolManager;
import org.chanchan.common.util.ServerProperties;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.web.startup.SRTInitializer;
import org.oagi.srt.web.startup.SRTInitializerException;

public class Utility {
	public static String generateGUID(){
		return "oagis-id-" + UUID.randomUUID().toString().replaceAll("-", "");
	}

	public static String first(String den) {
		return den.substring(0, den.indexOf(".")).replace("_", " ");
	}
	
	public static String createDENFormat(String str) {
		String pre = str.substring(0, str.indexOf("Type"));
		return pre + ". Type";
	}
	
	public static String firstToUpperCase(String str) {
		String prefix = str.substring(0, 1);
		String suffix = str.substring(1);
		return prefix.toUpperCase() + suffix;
	}
	
	public static String spaceSeparator(String str) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < str.length(); i++) {
			if(Character.isUpperCase(str.charAt(i)) && i != 0) {
				if(i > 0 && i < str.length() && Character.isUpperCase(str.charAt(i - 1)))
					if (i < str.length() - 1 && Character.isLowerCase(str.charAt(i + 1)))
						sb.append(" " + str.charAt(i));
					else
						sb.append(str.charAt(i));
				else 
					sb.append(" " + str.charAt(i));
			} else {
				sb.append(str.charAt(i));
			}
		}
		return sb.toString();
	}
	
	public static void dbSetup() throws Exception {
		ServerProperties props = ServerProperties.getInstance();
		String _propFile = "/" + SRTConstants.SRT_PROPERTIES_FILE_NAME;
		try {
			InputStream is = SRTInitializer.class.getResourceAsStream(_propFile);
			if (is == null) {
				throw new SRTInitializerException(_propFile + " not found!");
			}
			try {
				props.load(is, true);
			} catch (IOException e) {
				throw new SRTInitializerException(_propFile + " cannot be read...");
			}
		} catch (Exception e) {
			System.out.println("[SRTInitializer] Fail to Getting "
					+ SRTConstants.SRT_PROPERTIES_FILE_NAME + " URL : "
					+ e.toString());
		}
		try {
			ConnectionPoolManager cpm = ConnectionPoolManager.getInstance();
			String poolName = cpm.getDefaultPoolName();
			System.out.println("DefaultPoolName:" + poolName);
			Connection dbConnection = cpm.getConnection(poolName);
			dbConnection.close();
			System.out.println("DB Connection Pool initialized...");
			cpm.release();
		} catch (Exception e) {
			System.out.println("[SRTInitializer] Fail to Creating Connection Pool : "
					+ e.toString());
			e.printStackTrace();
			throw new SRTInitializerException("[SRTInitializer] Fail to Creating Connection Pool : "
					+ e.toString());
		}
	
	}
}
