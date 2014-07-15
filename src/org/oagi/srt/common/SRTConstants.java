package org.oagi.srt.common;

import org.chanchan.common.util.ServerProperties;

/**
 * @version 1.0
 * @author Yunsu Lee
 */

public class SRTConstants {

	public static final int DT_TYPE = 0;
	
	public static final int DB_TYPE_UNKNOWN = -1;
	public static final int DB_TYPE_CLIENT = 0;
	public static final int DB_TYPE_ORACLE = 1;
	public static final int DB_TYPE_DB2 = 2;
	public static final int DB_TYPE_MSSQL = 3;
	public static final int DB_TYPE_POSTGRES = 4;
	public static final int DB_TYPE_ALTIBASE = 5;
	public static final int DB_TYPE_IFX = 6;
	public static final int DB_TYPE_MYSQL = 7;
	public static final int DB_TYPE_DERBY = 8;
	public static final int DB_TYPE_TIBERO = 9;
	public static final int DB_TYPE_SQLITE = 10;
	public static final int DB_TYPE_CUBRID = 11;
	
	public static final String SRT_PROPERTIES_FILE_NAME = "srt.properties";
	public static final String PRODUCT_NAME = "OAGi Semantic Refinement Tool";
	
	public static final String NS_CCTS_PREFIX = "ccts";
	public static final String NS_XSD_PREFIX = "xsd";
	public static final String NS_CCTS = "urn:un:unece:uncefact:documentation:1.1";
	public static final String NS_XSD = "http://www.w3.org/2001/XMLSchema";
	
	public static int getDBType() {
		ServerProperties props = ServerProperties.getInstance();
		String dbTypeVal = props.getProperty("srt.db.type");

		if (props.isEmpty() || dbTypeVal == null || dbTypeVal.length() == 0) {
			return DB_TYPE_UNKNOWN;
		}

		return Integer.parseInt(dbTypeVal);
	}


}
