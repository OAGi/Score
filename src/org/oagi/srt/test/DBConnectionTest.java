package org.oagi.srt.test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

import org.chanchan.common.persistence.db.ConnectionPoolManager;
import org.chanchan.common.util.ServerProperties;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dao.mysql.DTMysqlDAO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.startup.SRTInitializer;
import org.oagi.srt.startup.SRTInitializerException;

public class DBConnectionTest {

	public static void main(String args[]) throws SRTDAOException, SRTInitializerException {
		setup();
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");

		QueryCondition qc = new QueryCondition();
		qc.add("dt_id", new Integer(12));
		DTVO dtVO = (DTVO)dao.findObject(qc);
		System.out.println("### " + dtVO.getDEN());
	}

	private static void setup() throws SRTInitializerException {
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
