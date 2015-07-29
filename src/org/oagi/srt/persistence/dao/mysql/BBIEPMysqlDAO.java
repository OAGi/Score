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
import org.oagi.srt.persistence.dto.BBIEPVO;
import org.oagi.srt.persistence.dto.DTVO;

/**
 *
 * @author Nasif Sikder
 * @version 1.0
 *
 */
public class BBIEPMysqlDAO extends SRTDAO {
	private final String _tableName = "bbiep";

	private final String _FIND_ALL_BBIEP_STATEMENT = "SELECT BBIEP_ID, BBIEP_GUID, Based_BCCP_ID, Definition, "
			+ "Created_By_User_ID, Last_Updated_by_User_ID, Creation_Timestamp, Last_Update_Timestamp, remark, business_term FROM " 
			+ _tableName;
	
	private final String _FIND_MAX_ID_STATEMENT =
			"SELECT max(BBIEP_ID) as max FROM " + _tableName;
	
	private final String _FIND_BBIEP_STATEMENT = "SELECT BBIEP_ID, BBIEP_GUID, Based_BCCP_ID, Definition, "
			+ "Created_By_User_ID, Last_Updated_by_User_ID, Creation_Timestamp, Last_Update_Timestamp, remark, business_term FROM " 
			+ _tableName;
	
	private final String _INSERT_BBIEP_STATEMENT = "INSERT INTO " + _tableName
			+ " (BBIEP_GUID, Based_BCCP_ID, Definition, Created_By_User_ID, Last_Updated_by_User_ID, "
			+ "Creation_Timestamp, Last_Update_Timestamp, remark, business_term) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?)";
	
	private final String _INSERT_BBIEP_WITH_ID_STATEMENT = "INSERT INTO " + _tableName
			+ " (BBIEP_GUID, Based_BCCP_ID, Definition, Created_By_User_ID, Last_Updated_by_User_ID, "
			+ "Creation_Timestamp, Last_Update_Timestamp, remark, business_term, BBIEP_ID) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?)";
	
	private final String _DELETE_BBIEP_STATEMENT = "DELETE FROM " + _tableName + " WHERE BBIEP_ID = ?";

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
		BBIEPVO bbiepVO = (BBIEPVO)obj;
		Connection conn = null;
		PreparedStatement ps = null;
		int key = -1;
		try {
			conn = tx.open();
			
			if(bbiepVO.getBBIEPID() == -1)
				ps = conn.prepareStatement(_INSERT_BBIEP_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			else
				ps = conn.prepareStatement(_INSERT_BBIEP_WITH_ID_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			
			ps.setString(1, bbiepVO.getBBIEPGUID());
			ps.setInt(2, bbiepVO.getBasedBCCPID());
			ps.setString(3, bbiepVO.getDefinition());
			ps.setInt(4, bbiepVO.getCreatedByUserID());
			ps.setInt(5, bbiepVO.getLastUpdatedbyUserID());
			ps.setString(6, bbiepVO.getRemark());
			ps.setString(7, bbiepVO.getBusinessTerm());
			if(bbiepVO.getBBIEPID() != -1)
				ps.setInt(8, bbiepVO.getBBIEPID());
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
		BBIEPVO bbiepVO = (BBIEPVO)obj;
		PreparedStatement ps = null;
		int key = -1;
		try {
			if(bbiepVO.getBBIEPID() == -1)
				ps = conn.prepareStatement(_INSERT_BBIEP_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			else
				ps = conn.prepareStatement(_INSERT_BBIEP_WITH_ID_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			
			ps.setString(1, bbiepVO.getBBIEPGUID());
			ps.setInt(2, bbiepVO.getBasedBCCPID());
			ps.setString(3, bbiepVO.getDefinition());
			ps.setInt(4, bbiepVO.getCreatedByUserID());
			ps.setInt(5, bbiepVO.getLastUpdatedbyUserID());
			ps.setString(6, bbiepVO.getRemark());
			ps.setString(7, bbiepVO.getBusinessTerm());
			if(bbiepVO.getBBIEPID() != -1)
				ps.setInt(8, bbiepVO.getBBIEPID());
			
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
		BBIEPVO bbiepVO = new BBIEPVO();
		try {
			Connection conn = tx.open();
			String sql = _FIND_BBIEP_STATEMENT;

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
				bbiepVO.setBBIEPID(rs.getInt("BBIEP_ID"));
				bbiepVO.setBBIEPGUID(rs.getString("BBIEP_GUID"));
				bbiepVO.setBasedBCCPID(rs.getInt("Based_BCCP_ID"));
				bbiepVO.setDefinition(rs.getString("Definition"));
				bbiepVO.setCreatedByUserID(rs.getInt("Created_By_User_ID"));
				bbiepVO.setLastUpdatedbyUserID(rs.getInt("Last_Updated_by_User_ID"));
				bbiepVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				bbiepVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				bbiepVO.setRemark(rs.getString("remark"));
				bbiepVO.setBusinessTerm(rs.getString("business_term"));
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
		return bbiepVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_BBIEP_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				BBIEPVO bbiepVO = new BBIEPVO();
				bbiepVO.setBBIEPID(rs.getInt("BBIEP_ID"));
				bbiepVO.setBBIEPGUID(rs.getString("BBIEP_GUID"));
				bbiepVO.setBasedBCCPID(rs.getInt("Based_BCCP_ID"));
				bbiepVO.setDefinition(rs.getString("Definition"));
				bbiepVO.setCreatedByUserID(rs.getInt("Created_By_User_ID"));
				bbiepVO.setLastUpdatedbyUserID(rs.getInt("Last_Updated_by_User_ID"));
				bbiepVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				bbiepVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				bbiepVO.setRemark(rs.getString("remark"));
				bbiepVO.setBusinessTerm(rs.getString("business_term"));
				list.add(bbiepVO);
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
	
	private final String _UPDATE_BBIEP_STATEMENT = "UPDATE " + _tableName + " SET "
			+ "Last_Update_Timestamp = CURRENT_TIMESTAMP, BBIEP_GUID = ?, Based_BCCP_ID = ?, Definition = ?, "
			+ "Created_By_User_ID = ?, Last_Updated_by_User_ID = ?, Creation_Timestamp = ?, remark = ?, business_term = ? WHERE BBIEP_ID = ?";

	public boolean updateObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		BBIEPVO bbiepVO = (BBIEPVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_BBIEP_STATEMENT);
			
			ps.setString(1, bbiepVO.getBBIEPGUID());
			ps.setInt(2, bbiepVO.getBasedBCCPID());
			ps.setString(3, bbiepVO.getDefinition());
			ps.setInt(4, bbiepVO.getCreatedByUserID());
			ps.setInt(5, bbiepVO.getLastUpdatedbyUserID());
			ps.setTimestamp(6, bbiepVO.getCreationTimestamp());
			ps.setString(7, bbiepVO.getRemark());
			ps.setString(8, bbiepVO.getBusinessTerm());
			ps.setInt(9, bbiepVO.getBBIEPID());
			
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
		BBIEPVO bbiepVO = (BBIEPVO)obj;
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_BBIEP_STATEMENT);
			ps.setInt(1, bbiepVO.getBBIEPID());
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
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_BBIEP_STATEMENT;

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
			while (rs.next()) {
				BBIEPVO bbiepVO = new BBIEPVO();
				bbiepVO.setBBIEPID(rs.getInt("BBIEP_ID"));
				bbiepVO.setBBIEPGUID(rs.getString("BBIEP_GUID"));
				bbiepVO.setBasedBCCPID(rs.getInt("Based_BCCP_ID"));
				bbiepVO.setDefinition(rs.getString("Definition"));
				bbiepVO.setCreatedByUserID(rs.getInt("Created_By_User_ID"));
				bbiepVO.setLastUpdatedbyUserID(rs.getInt("Last_Updated_by_User_ID"));
				bbiepVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				bbiepVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				bbiepVO.setRemark(rs.getString("remark"));
				bbiepVO.setBusinessTerm(rs.getString("business_term"));
				list.add(bbiepVO);
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
	public SRTObject findObject(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		BBIEPVO bbiepVO = null;
		
		try {
			String sql = _FIND_BBIEP_STATEMENT;

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
				bbiepVO = new BBIEPVO();
				bbiepVO.setBBIEPID(rs.getInt("BBIEP_ID"));
				bbiepVO.setBBIEPGUID(rs.getString("BBIEP_GUID"));
				bbiepVO.setBasedBCCPID(rs.getInt("Based_BCCP_ID"));
				bbiepVO.setDefinition(rs.getString("Definition"));
				bbiepVO.setCreatedByUserID(rs.getInt("Created_By_User_ID"));
				bbiepVO.setLastUpdatedbyUserID(rs.getInt("Last_Updated_by_User_ID"));
				bbiepVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				bbiepVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				bbiepVO.setRemark(rs.getString("remark"));
				bbiepVO.setBusinessTerm(rs.getString("business_term"));
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
		return bbiepVO;
	}

	@Override
	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<SRTObject> findObjects(Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return null;
	}
}
