package org.oagi.srt.persistence.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ABIEVO;
import org.oagi.srt.persistence.dto.DTVO;

/**
 *
 * @author Nasif Sikder
 * @author Jaehun Lee
 * @version 1.1
 *
 */

public class ABIEMysqlDAO extends SRTDAO {

	private final String _tableName = "abie";
	
	private final String _FIND_ALL_ABIE_STATEMENT =
			"SELECT ABIE_ID, GUID, Based_ACC_ID, isTop_Level, biz_ctx_id, Definition, "
			+ "Created_By, Last_Updated_By, Creation_Timestamp, "
			+ "Last_Update_Timestamp, State, Client_ID, Version, Status, Remark, biz_term FROM " + _tableName + " order by Last_Update_Timestamp desc";
	
	private final String _FIND_MAX_ID_STATEMENT =
			"SELECT max(abie_id) as max FROM " + _tableName;
	
	private final String _FIND_ABIE_STATEMENT =
			"SELECT ABIE_ID, GUID, Based_ACC_ID, isTop_Level, biz_ctx_id, Definition, "
					+ "Created_By, Last_Updated_By, Creation_Timestamp, "
					+ "Last_Update_Timestamp, State, Client_ID, Version, Status, Remark, biz_term FROM " + _tableName;
	
	private final String _INSERT_ABIE_STATEMENT = 
			"INSERT INTO " + _tableName + " (GUID, Based_ACC_ID, isTop_Level, biz_ctx_id, Definition, "
					+ "Created_By, Last_Updated_By, Creation_Timestamp, "
					+ "Last_Update_Timestamp, State, Client_ID, Version, Status, Remark, biz_term) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?, ?, ? ,?)";
	
	private final String _INSERT_ABIE_WITH_ID_STATEMENT = 
			"INSERT INTO " + _tableName + " (GUID, Based_ACC_ID, isTop_Level, biz_ctx_id, Definition, "
					+ "Created_By, Last_Updated_By, Creation_Timestamp, "
					+ "Last_Update_Timestamp, State, Client_ID, Version, Status, Remark, biz_term, ABIE_ID) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?, ?, ? ,?, ?)";
	
	private final String _DELETE_ABIE_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE ABIE_ID = ?";
	
	public int findMaxId() throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		int max = 1;
		try {
			Connection conn = tx.open();
			String sql = _FIND_MAX_ID_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs.next())
				max = rs.getInt("max");
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
		return max;
	}
	
	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		ABIEVO abieVO = (ABIEVO)obj;
		Connection conn = null;
		PreparedStatement ps = null;
		int key = -1;
		try {
			conn = tx.open();
			if(abieVO.getABIEID() == -1)
				ps = conn.prepareStatement(_INSERT_ABIE_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			else
				ps = conn.prepareStatement(_INSERT_ABIE_WITH_ID_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			
			ps.setString(1, abieVO.getAbieGUID());
			ps.setInt(2, abieVO.getBasedACCID());
			ps.setInt(3, abieVO.getIsTopLevel());
			ps.setInt(4, abieVO.getBusinessContextID());
			ps.setString(5, abieVO.getDefinition());
			ps.setInt(6, abieVO.getCreatedByUserID());
			ps.setInt(7, abieVO.getLastUpdatedByUserID());
			if(abieVO.getState() == 0)
				ps.setNull(8, java.sql.Types.INTEGER);
			else
				ps.setInt(8, abieVO.getState());
			ps.setString(9, abieVO.getClientID());
			ps.setString(10, abieVO.getVersion());
			ps.setString(11, abieVO.getStatus());
			ps.setString(12, abieVO.getRemark());
			ps.setString(13, abieVO.getBusinessTerm());
			if(abieVO.getABIEID() != -1)
				ps.setInt(14, abieVO.getABIEID());

			ps.executeUpdate();
			
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()){
			    key = rs.getInt(1);
			}
			rs.close();
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
		return key;

	}
	
	public int insertObject(SRTObject obj, Connection conn) throws SRTDAOException {
		ABIEVO abieVO = (ABIEVO)obj;
		PreparedStatement ps = null;
		int key = -1;
		try {
			if(abieVO.getABIEID() == -1)
				ps = conn.prepareStatement(_INSERT_ABIE_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			else
				ps = conn.prepareStatement(_INSERT_ABIE_WITH_ID_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			
			ps.setString(1, abieVO.getAbieGUID());
			ps.setInt(2, abieVO.getBasedACCID());
			ps.setInt(3, abieVO.getIsTopLevel());
			ps.setInt(4, abieVO.getBusinessContextID());
			ps.setString(5, abieVO.getDefinition());
			ps.setInt(6, abieVO.getCreatedByUserID());
			ps.setInt(7, abieVO.getLastUpdatedByUserID());
			ps.setInt(8, abieVO.getState());
			ps.setString(9, abieVO.getClientID());
			ps.setString(10, abieVO.getVersion());
			ps.setString(11, abieVO.getStatus());
			ps.setString(12, abieVO.getRemark());
			ps.setString(13, abieVO.getBusinessTerm());
			if(abieVO.getABIEID() != -1)
				ps.setInt(14, abieVO.getABIEID());


			ps.executeUpdate();
			
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()){
			    key = rs.getInt(1);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			if(ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {}
			}
		}
		return key;

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
				abieVO.setBusinessContextID(rs.getInt("biz_ctx_id"));
				abieVO.setDefinition(rs.getString("Definition"));
				abieVO.setCreatedByUserID(rs.getInt("Created_By"));
				abieVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
				abieVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				abieVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				abieVO.setState(rs.getInt("State"));	
				abieVO.setAbieGUID(rs.getString("GUID"));
				abieVO.setClientID(rs.getString("Client_ID"));
				abieVO.setVersion(rs.getString("Version"));
				abieVO.setStatus(rs.getString("Status"));
				abieVO.setRemark(rs.getString("Remark"));
				abieVO.setBusinessTerm(rs.getString("biz_term"));
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
				abieVO.setBusinessContextID(rs.getInt("biz_ctx_id"));
				abieVO.setDefinition(rs.getString("Definition"));
				abieVO.setCreatedByUserID(rs.getInt("Created_By"));
				abieVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
				abieVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				abieVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				abieVO.setState(rs.getInt("State"));	
				abieVO.setAbieGUID(rs.getString("GUID"));
				abieVO.setClientID(rs.getString("Client_ID"));
				abieVO.setVersion(rs.getString("Version"));
				abieVO.setStatus(rs.getString("Status"));
				abieVO.setRemark(rs.getString("Remark"));
				abieVO.setBusinessTerm(rs.getString("biz_term"));
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

	private final String _UPDATE_ABIE_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET Based_ACC_ID = ?, isTop_Level = ?,  "
			+ " biz_ctx_ID = ?, Definition = ?, Last_Update_Timestamp = CURRENT_TIMESTAMP, "
			+ " Last_Updated_By = ?, State = ?, GUID = ?, Client_ID = ?, Version = ?, Status = ?, Remark = ?, biz_term = ? WHERE ABIE_ID = ?";
	
	public boolean updateObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		ABIEVO abieVO = (ABIEVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_ABIE_STATEMENT);
			
			ps.setString(1, abieVO.getAbieGUID());
			ps.setInt(2, abieVO.getBasedACCID());
			ps.setInt(3, abieVO.getIsTopLevel());
			ps.setInt(4, abieVO.getBusinessContextID());
			ps.setString(5, abieVO.getDefinition());
			ps.setInt(6, abieVO.getLastUpdatedByUserID());
			ps.setInt(7, abieVO.getState());
			ps.setString(8, abieVO.getClientID());
			ps.setString(9, abieVO.getVersion());
			ps.setString(10, abieVO.getStatus());
			ps.setString(11, abieVO.getRemark());
			ps.setString(12, abieVO.getBusinessTerm());
			ps.setInt(13, abieVO.getABIEID());
			
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
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
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
			
			sql += " order by Last_Update_Timestamp desc";
			
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
			while (rs.next()) {
				ABIEVO abieVO = new ABIEVO();
				abieVO.setABIEID(rs.getInt("ABIE_ID"));
				abieVO.setBasedACCID(rs.getInt("Based_ACC_ID"));				
				abieVO.setIsTopLevel(rs.getInt("isTop_Level"));
				abieVO.setBusinessContextID(rs.getInt("biz_ctx_id"));
				abieVO.setDefinition(rs.getString("Definition"));
				abieVO.setCreatedByUserID(rs.getInt("Created_By"));
				abieVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
				abieVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				abieVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				abieVO.setState(rs.getInt("State"));	
				abieVO.setAbieGUID(rs.getString("GUID"));
				abieVO.setClientID(rs.getString("Client_ID"));
				abieVO.setVersion(rs.getString("Version"));
				abieVO.setStatus(rs.getString("Status"));
				abieVO.setRemark(rs.getString("Remark"));
				abieVO.setBusinessTerm(rs.getString("biz_term"));
				list.add(abieVO);
			}
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
	public SRTObject findObject(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ABIEVO abieVO = null;
		
		try {
			String sql = _FIND_ABIE_STATEMENT;

			String WHERE_OR_AND = " WHERE ";
			int nCond = qc.getSize();
			if (nCond > 0) {
				for (int n = 0; n < nCond; n++) {
					sql += WHERE_OR_AND + qc.getField(n) + " = ?";
					WHERE_OR_AND = " AND ";
				}
			}
			
			int nCond2 = qc.getLikeSize();
			if (nCond2 > 0) {
				for (int n = 0; n < nCond2; n++) {
					sql += WHERE_OR_AND + qc.getLikeField(n) + " like ?";
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
			
			if (nCond2 > 0) {
				for (int n = 0; n < nCond2; n++) {
					Object value = qc.getLikeValue(n);
					if (value instanceof String) {
						ps.setString(nCond + n + 1, (String) value);
					} else if (value instanceof Integer) {
						ps.setInt(nCond + n + 1, ((Integer) value).intValue());
					}
				}
			}

			rs = ps.executeQuery();
			if (rs.next()) {
				abieVO = new ABIEVO();
				abieVO.setABIEID(rs.getInt("ABIE_ID"));
				abieVO.setBasedACCID(rs.getInt("Based_ACC_ID"));				
				abieVO.setIsTopLevel(rs.getInt("isTop_Level"));
				abieVO.setBusinessContextID(rs.getInt("biz_ctx_id"));
				abieVO.setDefinition(rs.getString("Definition"));
				abieVO.setCreatedByUserID(rs.getInt("Created_By"));
				abieVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
				abieVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				abieVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				abieVO.setState(rs.getInt("State"));	
				abieVO.setAbieGUID(rs.getString("GUID"));
				abieVO.setClientID(rs.getString("Client_ID"));
				abieVO.setVersion(rs.getString("Version"));
				abieVO.setStatus(rs.getString("Status"));
				abieVO.setRemark(rs.getString("Remark"));
				abieVO.setBusinessTerm(rs.getString("biz_term"));
			}
			
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
		}
		return abieVO;

	}

	@Override
	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			String sql = _FIND_ABIE_STATEMENT;

			String WHERE_OR_AND = " WHERE ";
			int nCond = qc.getSize();
			if (nCond > 0) {
				for (int n = 0; n < nCond; n++) {
					sql += WHERE_OR_AND + qc.getField(n) + " = ?";
					WHERE_OR_AND = " AND ";
				}
			}
			
			int nCond2 = qc.getLikeSize();
			if (nCond2 > 0) {
				for (int n = 0; n < nCond2; n++) {
					sql += WHERE_OR_AND + qc.getLikeField(n) + " like ?";
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
			
			if (nCond2 > 0) {
				for (int n = 0; n < nCond2; n++) {
					Object value = qc.getLikeValue(n);
					if (value instanceof String) {
						ps.setString(nCond + n + 1, (String) value);
					} else if (value instanceof Integer) {
						ps.setInt(nCond + n + 1, ((Integer) value).intValue());
					}
				}
			}

			rs = ps.executeQuery();
			while (rs.next()) {
				ABIEVO abieVO = new ABIEVO();
				abieVO.setABIEID(rs.getInt("ABIE_ID"));
				abieVO.setBasedACCID(rs.getInt("Based_ACC_ID"));				
				abieVO.setIsTopLevel(rs.getInt("isTop_Level"));
				abieVO.setBusinessContextID(rs.getInt("biz_ctx_id"));
				abieVO.setDefinition(rs.getString("Definition"));
				abieVO.setCreatedByUserID(rs.getInt("Created_By"));
				abieVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
				abieVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				abieVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				abieVO.setState(rs.getInt("State"));	
				abieVO.setAbieGUID(rs.getString("GUID"));
				abieVO.setClientID(rs.getString("Client_ID"));
				abieVO.setVersion(rs.getString("Version"));
				abieVO.setStatus(rs.getString("Status"));
				abieVO.setRemark(rs.getString("Remark"));
				abieVO.setBusinessTerm(rs.getString("biz_term"));
				list.add(abieVO);
			}
			
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
		}
		return list;
	}

	@Override
	public ArrayList<SRTObject> findObjects(Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return null;
	}

}
