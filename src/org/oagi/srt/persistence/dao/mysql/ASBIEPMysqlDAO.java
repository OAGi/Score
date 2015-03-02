package org.oagi.srt.persistence.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ASBIEPVO;

/**
 *
 * @author Nasif Sikder
 * @version 1.0
 *
 */

public class ASBIEPMysqlDAO extends SRTDAO {
	private final String _tableName = "asbiep";

	private final String _FIND_ALL_ASBIEP_STATEMENT = 
			"SELECT * FROM ASBIEP_ID, ASBIEP_GUID, Based_ASCCP_ID, Role_Of_ABIE_ID, Definition, Created_By_User_ID,"
			+ " Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp" + _tableName;
	
	private final String _FIND_ASBIEP_STATEMENT = 
			"SELECT * FROM ASBIEP_ID, ASBIEP_GUID, Based_ASCCP_ID, Role_Of_ABIE_ID, Definition, Created_By_User_ID,"
			+ " Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp" + _tableName;
	
	private final String _INSERT_ASBIEP_STATEMENT = "INSERT INTO " + _tableName + " "
			+ "(ASBIEP_GUID, Based_ASCCP_ID, Role_Of_ABIE_ID, Definition, Created_By_User_ID, "
			+ "Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) "
			+ "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
	
	private final String _UPDATE_ASBIEP_STATEMENT = "UPDATE " + _tableName + 
			" SET Last_Update_Timestamp = CURRENT_TIMESTAMP, ASBIEP_GUID = ?, Based_ASCCP_ID = ?,"
			+ " Role_Of_ABIE_ID = ?, Definition = ?, Created_By_User_ID = ?, Last_Updated_By_User_ID = ?, "
			+ "Creation_Timestamp = ? WHERE ASBIEP_ID = ?";
	
	private final String _DELETE_ASBIEP_STATEMENT = "DELETE FROM " + _tableName + " WHERE ASBIEP_ID = ?";

	public boolean insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		ASBIEPVO asbiepVO = (ASBIEPVO)obj;
		try {
			Connection conn = tx.open();
			PreparedStatement ps = null;
			ps = conn.prepareStatement(_INSERT_ASBIEP_STATEMENT);
			ps.setString(1, asbiepVO.getASBIEPGUID());
			ps.setInt(2, asbiepVO.getBasedASCCPID());
			ps.setInt(3, asbiepVO.getRoleOfABIEID());
			ps.setString(4, asbiepVO.getDefinition());
			ps.setInt(5, asbiepVO.getCreatedByUserID());
			ps.setInt(6, asbiepVO.getLastUpdatedByUserID());

			ps.executeUpdate();

			ps.close();
			tx.commit();
		} catch (BfPersistenceException e) {
			tx.rollback();
			throw new SRTDAOException(SRTDAOException.DAO_INSERT_ERROR, e);
		} catch (SQLException e) {
			e.printStackTrace();
			tx.rollback();
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			tx.close();
		}
		return true;
	}

	public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ASBIEPVO asbiepVO = new ASBIEPVO();
		try {
			Connection conn = tx.open();
			String sql = _FIND_ASBIEP_STATEMENT;

			String WHERE_OR_AND = " WHERE ";
			int nCond = qc.getSize();
			if (nCond > 0) {
				for (int n = 0; n < nCond; n++) {
					sql += WHERE_OR_AND + qc.getField(n) + " = ?";
					WHERE_OR_AND = " AND ";
				}
			}
			ps = conn.prepareStatement(sql);
			if (nCond > 0) {
				for (int n = 0; n < nCond; n++) {
					Object value = qc.getValue(n);
					if (value instanceof String) {
						ps.setString(n+1, (String) value);
					} else if (value instanceof Integer) {
						ps.setInt(n+1, ((Integer) value).intValue());
					}
				}
			}

			rs = ps.executeQuery();
			if (rs.next()) {
				asbiepVO.setASBIEPID(rs.getInt("ASBIEP_ID"));
				asbiepVO.setASBIEPGUID(rs.getString("ASBIEP_GUID"));
				asbiepVO.setBasedASCCPID(rs.getInt("Based_ASCCP_ID"));
				asbiepVO.setRoleOfABIEID(rs.getInt("Role_Of_ABIE_ID"));
				asbiepVO.setDefinition(rs.getString("Definition"));
				asbiepVO.setCreatedByUserID(rs.getInt("Created_By_User_ID"));
				asbiepVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By_User_ID"));
				asbiepVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asbiepVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
			}
			tx.commit();
			conn.close();
		} catch (BfPersistenceException e) {
			throw new SRTDAOException(SRTDAOException.DAO_FIND_ERROR, e);
		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			if(ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {}
			}
			if(rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {}
			}
			tx.close();
		}
		return asbiepVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_ASBIEP_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ASBIEPVO asbiepVO = new ASBIEPVO();
				asbiepVO.setASBIEPID(rs.getInt("ASBIEP_ID"));
				asbiepVO.setASBIEPGUID(rs.getString("ASBIEP_GUID"));
				asbiepVO.setBasedASCCPID(rs.getInt("Based_ASCCP_ID"));
				asbiepVO.setRoleOfABIEID(rs.getInt("Role_Of_ABIE_ID"));
				asbiepVO.setDefinition(rs.getString("Definition"));
				asbiepVO.setCreatedByUserID(rs.getInt("Created_By_User_ID"));
				asbiepVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By_User_ID"));
				asbiepVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asbiepVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				list.add(asbiepVO);
			}
			tx.commit();
			conn.close();
		} catch (BfPersistenceException e) {
			throw new SRTDAOException(SRTDAOException.DAO_FIND_ERROR, e);
		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			if(ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {}
			}
			if(rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {}
			}
			tx.close();
		}

		return list;		
	}

	@Override
	public boolean updateObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		ASBIEPVO asbiepVO = (ASBIEPVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_ASBIEP_STATEMENT);

			ps.setString(1, asbiepVO.getASBIEPGUID());
			ps.setInt(2, asbiepVO.getBasedASCCPID());
			ps.setInt(3, asbiepVO.getRoleOfABIEID());
			ps.setString(4, asbiepVO.getDefinition());
			ps.setInt(5, asbiepVO.getCreatedByUserID());
			ps.setInt(6, asbiepVO.getLastUpdatedByUserID());
			ps.setTimestamp(7, asbiepVO.getCreationTimestamp());
			ps.setTimestamp(8, asbiepVO.getLastUpdateTimestamp());
			ps.executeUpdate();

			tx.commit();
		} catch (BfPersistenceException e) {
			tx.rollback(e);
			throw new SRTDAOException(SRTDAOException.DAO_UPDATE_ERROR, e);
		} catch (SQLException e) {
			tx.rollback(e);
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			if(ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {}
			}
			tx.close();
		}

		return true;
	}

	@Override
	public boolean deleteObject(SRTObject obj) throws SRTDAOException {
		ASBIEPVO asbiepVO = (ASBIEPVO)obj;
		
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_ASBIEP_STATEMENT);
			ps.setInt(1, asbiepVO.getASBIEPID());
			ps.executeUpdate();

			tx.commit();
		} catch (BfPersistenceException e) {
			tx.rollback(e);
			throw new SRTDAOException(SRTDAOException.DAO_DELETE_ERROR, e);
		} catch (SQLException e) {
			tx.rollback(e);
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			if(ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {}
			}
			tx.close();
		}

		return true;


	}

	@Override
	public ArrayList<SRTObject> findObjects(QueryCondition qc)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return null;
	}

}
