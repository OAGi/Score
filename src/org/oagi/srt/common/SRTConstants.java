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
	
	public static int getDBType() {
		ServerProperties props = ServerProperties.getInstance();
		String dbTypeVal = props.getProperty("srt.db.type");

		if (props.isEmpty() || dbTypeVal == null || dbTypeVal.length() == 0) {
			return DB_TYPE_UNKNOWN;
		}

		return Integer.parseInt(dbTypeVal);
	}


}
