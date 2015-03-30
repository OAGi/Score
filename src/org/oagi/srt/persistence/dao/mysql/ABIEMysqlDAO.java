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
import org.oagi.srt.persistence.dto.ABIEVO;

/**
 *
 * @author Nasif Sikder
 * @version 1.0
 *
 */
public class ABIEMysqlDAO extends SRTDAO {

	private final String _tableName = "abie";
	
	private final String _FIND_ALL_ABIE_STATEMENT =
			"SELECT ABIE_ID, Based_ACC_ID, isTop_Level, Business_Context_ID, Definition, "
			+ "Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, "
			+ "Last_Update_Timestamp, State, ABIE_GUID, Client_ID, Version, Status, Remark, Business_Term FROM " + _tableName;
	
	private final String _FIND_ABIE_STATEMENT =
			"SELECT ABIE_ID, Based_ACC_ID, isTop_Level, Business_Context_ID, Definition, "
			+ "Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, "
			+ "Last_Update_Timestamp, State, ABIE_GUID, Client_ID, Version, Status, Remark, Business_Term FROM " + _tableName;
	
	private final String _INSERT_ABIE_STATEMENT = 
			"INSERT INTO " + _tableName + " (Based_ACC_ID, isTop_Level, Business_Context_ID,"
			+ " Definition, Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, "
			+ "Last_Update_Timestamp, State, ABIE_GUID, Client_ID, Version, Status, Remark, Business_Term) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?, ?, ? ,? , ?)";
	
	private final String _UPDATE_ABIE_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET Last_Update_Timestamp = CURRENT_TIMESTAMP, Based_ACC_ID = ?,"
			+ " isTop_Level = ?, Business_Context_ID = ?, Definition = ?, Created_By_User_ID = ?,"
			+ " Last_Updated_By_User_ID = ?, State = ?, ABIE_GUID = ?, Client_ID = ?, Version = ?, Status = ?, Remark = ?, Business_Term = ? WHERE ABIE_ID = ?";
	
	private final String _DELETE_ABIE_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE ABIE_ID = ?";
	
	
	public boolean insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		ABIEVO abieVO = (ABIEVO)obj;
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_ABIE_STATEMENT);
			ps.setInt(1, abieVO.getBasedACCID());
			ps.setInt(2, abieVO.getIsTopLevel());
			ps.setInt(3, abieVO.getBusinessContextID());
			ps.setString(4, abieVO.getDefinition());
			ps.setInt(5, abieVO.getCreatedByUserID());
			ps.setInt(6, abieVO.getLastUpdatedByUserID());
			ps.setInt(7, abieVO.getState());
			ps.setString(8, abieVO.getAbieGUID());
			ps.setString(9, abieVO.getClientID());
			ps.setString(10, abieVO.getVersion());
			ps.setString(11, abieVO.getStatus());
			ps.setString(12, abieVO.getRemark());
			ps.setString(13, abieVO.getBusinessTerm());

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
			if(ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {}
			}
			try {
				if(conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {}
			tx.close();
		}
		return true;

	}

	public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ABIEVO abieVO = new ABIEVO();
		try {
			Connection conn = tx.open();
			String sql = _FIND_ABIE_STATEMENT;

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
				abieVO.setABIEID(rs.getInt("ABIE_ID"));
				abieVO.setBasedACCID(rs.getInt("Based_ACC_ID"));				
				abieVO.setIsTopLevel(rs.getInt("isTop_Level"));
				abieVO.setBusinessContextID(rs.getInt("Business_Context_ID"));
				abieVO.setDefinition(rs.getString("Definition"));
				abieVO.setCreatedByUserID(rs.getInt("Created_By_User_ID"));
				abieVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By_User_ID"));
				abieVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				abieVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				abieVO.setState(rs.getInt("State"));	
				abieVO.setAbieGUID("ABIE_GUID");
				abieVO.setClientID("Client_ID");
				abieVO.setVersion("Version");
				abieVO.setStatus("Status");
				abieVO.setRemark("Remark");
				abieVO.setBusinessTerm("Business_Term");
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
		return abieVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_ABIE_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ABIEVO abieVO = new ABIEVO();
				abieVO.setABIEID(rs.getInt("ABIE_ID"));
				abieVO.setBasedACCID(rs.getInt("Based_ACC_ID"));				
				abieVO.setIsTopLevel(rs.getInt("isTop_Level"));
				abieVO.setBusinessContextID(rs.getInt("Business_Context_ID"));
				abieVO.setDefinition(rs.getString("Definition"));
				abieVO.setCreatedByUserID(rs.getInt("Created_By_User_ID"));
				abieVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By_User_ID"));
				abieVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				abieVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				abieVO.setState(rs.getInt("State"));
				abieVO.setAbieGUID("ABIE_GUID");
				abieVO.setClientID("Client_ID");
				abieVO.setVersion("Version");
				abieVO.setStatus("Status");
				abieVO.setRemark("Remark");
				abieVO.setBusinessTerm("Business_Term");
				list.add(abieVO);
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

	
	public boolean updateObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		ABIEVO abieVO = (ABIEVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_ABIE_STATEMENT);
			
			ps.setInt(1, abieVO.getBasedACCID());
			ps.setInt(2, abieVO.getIsTopLevel());
			ps.setInt(3, abieVO.getBusinessContextID());
			ps.setString(4, abieVO.getDefinition());
			ps.setInt(5, abieVO.getLastUpdatedByUserID());
			ps.setInt(6, abieVO.getState());
			ps.setString(7, abieVO.getAbieGUID());
			ps.setString(8, abieVO.getClientID());
			ps.setString(9, abieVO.getVersion());
			ps.setString(10, abieVO.getStatus());
			ps.setString(11, abieVO.getRemark());
			ps.setString(12, abieVO.getBusinessTerm());
			
			ps.executeUpdate();

			tx.commit();
			conn.close();
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

	
	public boolean deleteObject(SRTObject obj) throws SRTDAOException {
		ABIEVO abieVO = (ABIEVO)obj;
		
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_ABIE_STATEMENT);
			ps.setInt(1, abieVO.getABIEID());
			ps.executeUpdate();

			tx.commit();
			conn.close();
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
