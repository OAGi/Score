package org.oagi.srt.persistence.dao.oracle;

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
import org.oagi.srt.persistence.dto.BusinessContextVO;
import org.oagi.srt.persistence.dto.DTVO;

/**
 *
 * @author Nasif Sikder
 * @version 1.0
 *
 */

public class BusinessContextOracleDAO extends SRTDAO {
	
	private final String _tableName = "biz_ctx";
	
	private final String _FIND_ALL_BUSINESS_CONTEXT_STATEMENT = 
			"SELECT biz_ctx_id, guid, name, created_by, last_updated_by, creation_timestamp, last_update_timestamp FROM " + _tableName;
	
	private final String _FIND_BUSINESS_CONTEXT_STATEMENT = 
			"SELECT biz_ctx_id, guid, name, created_by, last_updated_by, creation_timestamp, last_update_timestamp FROM " + _tableName;

	private final String _INSERT_BUSINESS_CONTEXT_STATEMENT = 
			"INSERT INTO " + _tableName + " (guid, name, created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
	
	private final String _UPDATE_BUSINESS_CONTEXT_STATEMENT =
			"UPDATE " + _tableName + " SET Last_Update_Timestamp = CURRENT_TIMESTAMP, guid = ?, Name = ?, created_by_user_id = ?, last_updated_by_user_id = ? WHERE biz_ctx_id = ?";
	
	private final String _DELETE_BUSINESS_CONTEXT_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE biz_ctx_id = ?";
	

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		BusinessContextVO business_contextVO = (BusinessContextVO)obj;
		try {
			Connection conn = tx.open();
			PreparedStatement ps = null;
			ps = conn.prepareStatement(_INSERT_BUSINESS_CONTEXT_STATEMENT);
			ps.setString(1, business_contextVO.getBusinessContextGUID());
			ps.setString(2, business_contextVO.getName());
			ps.setInt(3,  business_contextVO.getCreatedByUserId());
			ps.setInt(4, business_contextVO.getLastUpdatedByUserId());

			ps.executeUpdate();
			ps.close();
			tx.commit();
			conn.close();
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
		return 1;
	}

	public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		BusinessContextVO business_contextVO =  new BusinessContextVO();
		try {
			Connection conn = tx.open();
			String sql = _FIND_BUSINESS_CONTEXT_STATEMENT;

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
				business_contextVO.setBusinessContextID(rs.getInt("biz_ctx_id"));
				business_contextVO.setBusinessContextGUID(rs.getString("guid"));
				business_contextVO.setName(rs.getString("name"));
				business_contextVO.setCreatedByUserId(rs.getInt("created_by"));
				business_contextVO.setLastUpdatedByUserId(rs.getInt("last_updated_by"));
				business_contextVO.setCreationTimestamp(rs.getTimestamp("creation_timestamp"));
				business_contextVO.setLastUpdateTimestamp(rs.getTimestamp("last_update_timestamp"));
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
		return business_contextVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_BUSINESS_CONTEXT_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()){
				BusinessContextVO business_contextVO =  new BusinessContextVO();
				business_contextVO.setBusinessContextID(rs.getInt("biz_ctx_id"));
				business_contextVO.setBusinessContextGUID(rs.getString("guid"));
				business_contextVO.setName(rs.getString("name"));
				business_contextVO.setCreatedByUserId(rs.getInt("created_by"));
				business_contextVO.setLastUpdatedByUserId(rs.getInt("last_updated_by"));
				business_contextVO.setCreationTimestamp(rs.getTimestamp("creation_timestamp"));
				business_contextVO.setLastUpdateTimestamp(rs.getTimestamp("last_update_timestamp"));
				list.add(business_contextVO);
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
		BusinessContextVO business_contextVO = (BusinessContextVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_BUSINESS_CONTEXT_STATEMENT);
			
			ps.setString(1, business_contextVO.getBusinessContextGUID());
			ps.setString(2, business_contextVO.getName());
			ps.setInt(3,  business_contextVO.getCreatedByUserId());
			ps.setInt(4, business_contextVO.getLastUpdatedByUserId());

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
		BusinessContextVO business_contextVO = (BusinessContextVO)obj;
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_BUSINESS_CONTEXT_STATEMENT);
			ps.setInt(1, business_contextVO.getBusinessContextID());
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

	@Override
	public SRTObject findObject(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		BusinessContextVO business_contextVO = null;
		
		try {
			String sql = _FIND_BUSINESS_CONTEXT_STATEMENT;

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
				business_contextVO = new BusinessContextVO();
				business_contextVO.setBusinessContextID(rs.getInt("biz_ctx_id"));
				business_contextVO.setBusinessContextGUID(rs.getString("guid"));
				business_contextVO.setName(rs.getString("name"));
				business_contextVO.setCreatedByUserId(rs.getInt("created_by"));
				business_contextVO.setLastUpdatedByUserId(rs.getInt("last_updated_by"));
				business_contextVO.setCreationTimestamp(rs.getTimestamp("creation_timestamp"));
				business_contextVO.setLastUpdateTimestamp(rs.getTimestamp("last_update_timestamp"));
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
		return business_contextVO;
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

	@Override
	public int insertObject(SRTObject obj, Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}
}
