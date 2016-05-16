package org.oagi.srt.persistence.dao.oracle;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BusinessContextValueVO;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Nasif Sikder
 * @version 1.0
 *
 */
@Repository
public class BusinessContextValueOracleDAO extends SRTDAO {
	
	private final String _tableName = "biz_ctx_value";

	private final String _FIND_ALL_BUSINESS_CONTEXT_VALUE_STATEMENT =
			"SELECT biz_ctx_value_id, biz_ctx_id, ctx_scheme_value_id FROM " + _tableName;
	
	private final String _FIND_BUSINESS_CONTEXT_VALUE_STATEMENT = 
			"SELECT biz_ctx_value_id, biz_ctx_id, ctx_scheme_value_id FROM " + _tableName;
	
	private final String _INSERT_BUSINESS_CONTEXT_VALUE_STATEMENT =
			"INSERT INTO " + _tableName + " (biz_ctx_id, ctx_scheme_value_id) VALUES (?, ?)";
	
	private final String _UPDATE_BUSINESS_CONTEXT_VALUE_STATEMENT = "UPDATE " + _tableName + 
			" SET biz_ctx_id = ?, ctx_scheme_value_id = ? WHERE biz_ctx_value_id = ?";
	
	private final String _DELETE_BUSINESS_CONTEXT_VALUE_STATEMENT =
			"DELETE FROM " + _tableName + " WHERE biz_ctx_value_id = ?";

	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;

		BusinessContextValueVO business_context_valueVO = (BusinessContextValueVO) obj;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_BUSINESS_CONTEXT_VALUE_STATEMENT);
			ps.setInt(1, business_context_valueVO.getBusinessContextID());
			ps.setInt(2, business_context_valueVO.getContextSchemeValueID());

			ps.executeUpdate();

			tx.commit();
		} catch (BfPersistenceException e) {
			tx.rollback();
			throw new SRTDAOException(SRTDAOException.DAO_INSERT_ERROR, e);
		} catch (SQLException e) {
			tx.rollback();
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(ps);
			closeQuietly(conn);
			closeQuietly(tx);
		}
		return 1;
	}

	public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		BusinessContextValueVO business_context_valueVO = new BusinessContextValueVO();
		try {
			conn = tx.open();
			String sql = _FIND_BUSINESS_CONTEXT_VALUE_STATEMENT;

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
						ps.setString(n + 1, (String) value);
					} else if (value instanceof Integer) {
						ps.setInt(n + 1, ((Integer) value).intValue());
					}
				}
			}

			rs = ps.executeQuery();
			if (rs.next()) {
				business_context_valueVO.setBusinessContextValueID(rs.getInt("biz_ctx_value_id"));
				business_context_valueVO.setBusinessContextID(rs.getInt("biz_ctx_id"));
				business_context_valueVO.setContextSchemeValueID(rs.getInt("ctx_scheme_value_id"));
			}
			tx.commit();
		} catch (BfPersistenceException e) {
			throw new SRTDAOException(SRTDAOException.DAO_FIND_ERROR, e);
		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(rs);
			closeQuietly(ps);
			closeQuietly(conn);
			closeQuietly(tx);
		}
		return business_context_valueVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			conn = tx.open();
			String sql = _FIND_ALL_BUSINESS_CONTEXT_VALUE_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				BusinessContextValueVO business_context_valueVO = new BusinessContextValueVO();
				business_context_valueVO.setBusinessContextValueID(rs.getInt("biz_ctx_value_id"));
				business_context_valueVO.setBusinessContextID(rs.getInt("biz_ctx_id"));
				business_context_valueVO.setContextSchemeValueID(rs.getInt("ctx_scheme_value_id"));
				list.add(business_context_valueVO);
			}
			tx.commit();
		} catch (BfPersistenceException e) {
			throw new SRTDAOException(SRTDAOException.DAO_FIND_ERROR, e);
		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(rs);
			closeQuietly(ps);
			closeQuietly(conn);
			closeQuietly(tx);
		}

		return list;
	}

	public boolean updateObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;

		BusinessContextValueVO business_context_valueVO = (BusinessContextValueVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_BUSINESS_CONTEXT_VALUE_STATEMENT);

			ps.setInt(1, business_context_valueVO.getBusinessContextID());
			ps.setInt(2, business_context_valueVO.getContextSchemeValueID());
			ps.executeUpdate();

			tx.commit();
		} catch (BfPersistenceException e) {
			tx.rollback(e);
			throw new SRTDAOException(SRTDAOException.DAO_UPDATE_ERROR, e);
		} catch (SQLException e) {
			tx.rollback(e);
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(ps);
			closeQuietly(conn);
			closeQuietly(tx);
		}

		return true;
	}

	public boolean deleteObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;

		BusinessContextValueVO business_context_valueVO = (BusinessContextValueVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_DELETE_BUSINESS_CONTEXT_VALUE_STATEMENT);
			ps.setInt(1, business_context_valueVO.getBusinessContextValueID());
			ps.executeUpdate();

			tx.commit();
		} catch (BfPersistenceException e) {
			tx.rollback(e);
			throw new SRTDAOException(SRTDAOException.DAO_DELETE_ERROR, e);
		} catch (SQLException e) {
			tx.rollback(e);
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(ps);
			closeQuietly(conn);
			closeQuietly(tx);
		}

		return true;
	}

	@Override
	public ArrayList<SRTObject> findObjects(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			conn = tx.open();
			String sql = _FIND_BUSINESS_CONTEXT_VALUE_STATEMENT;

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
						ps.setString(n + 1, (String) value);
					} else if (value instanceof Integer) {
						ps.setInt(n + 1, ((Integer) value).intValue());
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
				BusinessContextValueVO business_context_valueVO = new BusinessContextValueVO();
				business_context_valueVO.setBusinessContextValueID(rs.getInt("biz_ctx_value_id"));
				business_context_valueVO.setBusinessContextID(rs.getInt("biz_ctx_id"));
				business_context_valueVO.setContextSchemeValueID(rs.getInt("ctx_scheme_value_id"));
				list.add(business_context_valueVO);
			}
			tx.commit();
		} catch (BfPersistenceException e) {
			throw new SRTDAOException(SRTDAOException.DAO_FIND_ERROR, e);
		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(rs);
			closeQuietly(ps);
			closeQuietly(conn);
			closeQuietly(tx);
		}
		return list;
	}
}
