package org.oagi.srt.persistence.dao;

import org.oagi.srt.common.SRTConstants;

/**
 * @author YunSu Lee
 * @version 1.0
 */
public abstract class DAOFactory {
	
	public static DAOFactory getDAOFactory() throws SRTDAOException {
		return getDAOFactory(SRTConstants.getDBType());
	}

	public static DAOFactory getDAOFactory(int dbType)
			throws SRTDAOException {

		String cName = "org.oagi.srt.persistence.dao.";

		switch (dbType) {
			case SRTConstants.DB_TYPE_ORACLE:
				cName += "OracleDAOFactory";
				break;

			case SRTConstants.DB_TYPE_ALTIBASE:
				cName += "AltibaseDAOFactory";
				break;
			case SRTConstants.DB_TYPE_DB2:
				cName += "DB2DAOFactory";
				break;
			case SRTConstants.DB_TYPE_CLIENT:
				cName += "ClientDAOFactory";
				break;
			case SRTConstants.DB_TYPE_POSTGRES:
				cName += "PgsqlDAOFactory";
				break;
			case SRTConstants.DB_TYPE_MSSQL:
				cName += "MssqlDAOFactory";
				break;
			case SRTConstants.DB_TYPE_IFX:
				cName += "IfxDAOFactory";
				break;
			case SRTConstants.DB_TYPE_MYSQL:
				cName += "MysqlDAOFactory";
				break;
			case SRTConstants.DB_TYPE_DERBY:
				cName += "DerbyDAOFactory";
				break;
			case SRTConstants.DB_TYPE_TIBERO:
				cName += "TiberoDAOFactory";
				break;
				
			case SRTConstants.DB_TYPE_SQLITE:
				cName += "SqliteDAOFactory";
				break;	
				
			case SRTConstants.DB_TYPE_CUBRID:
				cName += "CubridDAOFactory";
				break;	
				
			default:
				throw new SRTDAOException(SRTDAOException.DAOFACTORY_NOT_FOUND);
		}
		return getSrtDAOFactory(cName);
	}

	private static DAOFactory getSrtDAOFactory(String cName)
			throws SRTDAOException {
		DAOFactory dao = null;
		try {
			dao = (DAOFactory)Class.forName(cName).newInstance();
		} catch (ClassNotFoundException e) {
			throw new SRTDAOException(SRTDAOException.DAO_CLASS_NOT_FOUND);
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new SRTDAOException(SRTDAOException.DAO_CLASS_INSTANTIATION_FAILED);
		} catch (IllegalAccessException e) {
			throw new SRTDAOException(SRTDAOException.DAO_CLASS_ILLEGAL_ACCESS);
		}
		return dao;
	}

	public abstract SRTDAO getDAO(String name) throws SRTDAOException;

}
