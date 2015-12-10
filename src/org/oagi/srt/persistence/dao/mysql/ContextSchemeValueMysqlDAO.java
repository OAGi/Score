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
import org.oagi.srt.persistence.dto.ContextSchemeVO;
import org.oagi.srt.persistence.dto.ContextSchemeValueVO;

/**
 *
 * @author Nasif Sikder
 * @version 1.0
 *
 */

public class ContextSchemeValueMysqlDAO extends SRTDAO {

	private final String _tableName = "ctx_scheme_value";

	private final String _FIND_ALL_CONTEXT_SCHEME_VALUE_STATEMENT =
			"SELECT ctx_scheme_value_id, guid, Value, Meaning, owner_ctx_scheme_id FROM " + _tableName;
	
	private final String _FIND_CONTEXT_SCHEME_VALUE_STATEMENT = 
			"SELECT ctx_scheme_value_id, guid, Value, Meaning, owner_ctx_scheme_id FROM " + _tableName;
	
	private final String _INSERT_CONTEXT_SCHEME_VALUE_STATEMENT = 
			"INSERT INTO " + _tableName + " (guid, Value, Meaning, owner_ctx_scheme_id) VALUES (?, ?, ?, ?)";
	
	private final String _UPDATE_CONTEXT_SCHEME_VALUE_STATEMENT = "UPDATE " + _tableName + " SET guid = ?,"
			+ " Value = ?, Meaning = ?, owner_ctx_scheme_id = ? WHERE ctx_scheme_value_id = ?";
	
	private final String _DELETE_CONTEXT_SCHEME_VALUE_STATEMENT =
			"DELETE FROM " + _tableName + " WHERE ctx_scheme_value_id = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public ArrayList<SRTObject> findObjects(QueryCondition qc) throws SRTDAOException {
		
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = tx.open();
			String sql = _FIND_CONTEXT_SCHEME_VALUE_STATEMENT;

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
				ContextSchemeValueVO context_scheme_valueVO = new ContextSchemeValueVO();
				context_scheme_valueVO.setContextSchemeValueID(rs.getInt("ctx_scheme_value_id"));
				context_scheme_valueVO.setContextSchemeValueGUID(rs.getString("guid"));
				context_scheme_valueVO.setValue(rs.getString("value"));
				context_scheme_valueVO.setMeaning(rs.getString("meaning"));
				context_scheme_valueVO.setOwnerContextSchemeID(rs.getInt("owner_ctx_scheme_id"));
				list.add(context_scheme_valueVO);
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
			try {
				if(conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {}
			tx.close();
		}
		return list;
	}
	
	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		ContextSchemeValueVO context_scheme_valueVO = (ContextSchemeValueVO)obj;
		try {
			Connection conn = tx.open();
			PreparedStatement ps = null;
			ps = conn.prepareStatement(_INSERT_CONTEXT_SCHEME_VALUE_STATEMENT);
			ps.setString(1, context_scheme_valueVO.getContextSchemeValueGUID());
			ps.setString(2, context_scheme_valueVO.getValue());
			ps.setString(3, context_scheme_valueVO.getMeaning());
			ps.setInt(4, context_scheme_valueVO.getOwnerContextSchemeID());

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
		return 1;
	}

	public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ContextSchemeValueVO context_scheme_valueVO = new ContextSchemeValueVO();
		try {
			Connection conn = tx.open();
			String sql = _FIND_CONTEXT_SCHEME_VALUE_STATEMENT;

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
				context_scheme_valueVO.setContextSchemeValueID(rs.getInt("ctx_scheme_value_id"));
				context_scheme_valueVO.setContextSchemeValueGUID(rs.getString("guid"));
				context_scheme_valueVO.setValue(rs.getString("value"));
				context_scheme_valueVO.setMeaning(rs.getString("meaning"));
				context_scheme_valueVO.setOwnerContextSchemeID(rs.getInt("owner_ctx_scheme_id"));
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
		return context_scheme_valueVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_CONTEXT_SCHEME_VALUE_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ContextSchemeValueVO context_scheme_valueVO = new ContextSchemeValueVO();
				context_scheme_valueVO.setContextSchemeValueID(rs.getInt("ctx_scheme_value_id"));
				context_scheme_valueVO.setContextSchemeValueGUID(rs.getString("guid"));
				context_scheme_valueVO.setValue(rs.getString("value"));
				context_scheme_valueVO.setMeaning(rs.getString("meaning"));
				context_scheme_valueVO.setOwnerContextSchemeID(rs.getInt("owner_ctx_scheme_id"));
				list.add(context_scheme_valueVO);
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
		ContextSchemeValueVO context_scheme_valueVO = (ContextSchemeValueVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_CONTEXT_SCHEME_VALUE_STATEMENT);

			ps.setString(1, context_scheme_valueVO.getContextSchemeValueGUID());
			ps.setString(2, context_scheme_valueVO.getValue());
			ps.setString(3, context_scheme_valueVO.getMeaning());
			ps.setInt(4, context_scheme_valueVO.getOwnerContextSchemeID());
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

	public boolean deleteObject(SRTObject obj) throws SRTDAOException {
		ContextSchemeValueVO context_scheme_valueVO = (ContextSchemeValueVO)obj;
		
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_CONTEXT_SCHEME_VALUE_STATEMENT);
			ps.setInt(1, context_scheme_valueVO.getContextSchemeValueID());
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
	public SRTObject findObject(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return null;
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
