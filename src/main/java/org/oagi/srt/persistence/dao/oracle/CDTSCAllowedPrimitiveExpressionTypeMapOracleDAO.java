package org.oagi.srt.persistence.dao.oracle;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveExpressionTypeMapVO;
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
public class CDTSCAllowedPrimitiveExpressionTypeMapOracleDAO extends SRTDAO {
	private final String _tableName = "cdt_sc_awd_pri_xps_type_map";

	private final String _FIND_ALL_CDT_SC_ALLOWED_PRIMITIVE_EXPRESSION_TYPE_MAP_STATEMENT
	= "SELECT cdt_sc_awd_pri_xps_type_map_id, cdt_sc_awd_pri, xbt_id "
			+ "FROM " + _tableName;
	
	private final String _FIND_CDT_SC_ALLOWED_PRIMITIVE_EXPRESSION_TYPE_MAP_STATEMENT
	= "SELECT cdt_sc_awd_pri_xps_type_map_id, cdt_sc_awd_pri, xbt_id "
			+ "FROM " + _tableName;
	
	private final String _INSERT_CDT_SC_ALLOWED_PRIMITIVE_EXPRESSION_TYPE_MAP_STATEMENT
	= "INSERT INTO " + _tableName + " (cdt_sc_awd_pri, xbt_id) VALUES (?, ?)";
	
	private final String _UPDATE_CDT_SC_ALLOWED_PRIMITIVE_EXPRESSION_TYPE_MAP_STATEMENT
	= "UPDATE " + _tableName + " SET cdt_sc_awd_pri = ?, xbt_id = ? "
			+ "WHERE cdt_sc_awd_pri_xps_type_map_id = ?";
	
	private final String _DELETE_CDT_SC_ALLOWED_PRIMITIVE_EXPRESSION_TYPE_MAP_STATEMENT
	= "DELETE FROM " + _tableName + " WHERE cdt_sc_awd_pri_xps_type_map_id = ?";

	@Override
	public ArrayList<SRTObject> findObjects(QueryCondition qc)
			throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			conn = tx.open();
			String sql = _FIND_ALL_CDT_SC_ALLOWED_PRIMITIVE_EXPRESSION_TYPE_MAP_STATEMENT;

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
			while (rs.next()) {
				CDTSCAllowedPrimitiveExpressionTypeMapVO cdt_sc_allowed_primitive_expression_type_mapVO =
						new CDTSCAllowedPrimitiveExpressionTypeMapVO();
				cdt_sc_allowed_primitive_expression_type_mapVO.setCTSCAllowedPrimitiveExpressionTypeMapID(rs.getInt("cdt_sc_awd_pri_xps_type_map_id"));
				cdt_sc_allowed_primitive_expression_type_mapVO.setCDTSCAllowedPrimitive(rs.getInt("cdt_sc_awd_pri"));
				cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(rs.getInt("xbt_id"));
				list.add(cdt_sc_allowed_primitive_expression_type_mapVO);
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

	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			String sql = _FIND_ALL_CDT_SC_ALLOWED_PRIMITIVE_EXPRESSION_TYPE_MAP_STATEMENT;

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
			while (rs.next()) {
				CDTSCAllowedPrimitiveExpressionTypeMapVO cdt_sc_allowed_primitive_expression_type_mapVO =
						new CDTSCAllowedPrimitiveExpressionTypeMapVO();
				cdt_sc_allowed_primitive_expression_type_mapVO.setCTSCAllowedPrimitiveExpressionTypeMapID(rs.getInt("cdt_sc_awd_pri_xps_type_map_id"));
				cdt_sc_allowed_primitive_expression_type_mapVO.setCDTSCAllowedPrimitive(rs.getInt("cdt_sc_awd_pri"));
				cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(rs.getInt("xbt_id"));
				list.add(cdt_sc_allowed_primitive_expression_type_mapVO);
			}
		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(rs);
			closeQuietly(ps);
		}

		return list;
	}

	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;

		CDTSCAllowedPrimitiveExpressionTypeMapVO cdt_sc_allowed_primitive_expression_type_mapVO =
				(CDTSCAllowedPrimitiveExpressionTypeMapVO) obj;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_CDT_SC_ALLOWED_PRIMITIVE_EXPRESSION_TYPE_MAP_STATEMENT);
			ps.setInt(1, cdt_sc_allowed_primitive_expression_type_mapVO.getCDTSCAllowedPrimitive());
			ps.setInt(2, cdt_sc_allowed_primitive_expression_type_mapVO.getXSDBuiltInTypeID());

			ps.executeUpdate();

			//ResultSet tableKeys = ps.getGeneratedKeys();
			//tableKeys.next();
			//int autoGeneratedID = tableKeys.getInt(1);

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

		CDTSCAllowedPrimitiveExpressionTypeMapVO cdt_sc_allowed_primitive_expression_type_mapVO =
				new CDTSCAllowedPrimitiveExpressionTypeMapVO();
		try {
			conn = tx.open();
			String sql = _FIND_CDT_SC_ALLOWED_PRIMITIVE_EXPRESSION_TYPE_MAP_STATEMENT;

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
				cdt_sc_allowed_primitive_expression_type_mapVO.setCTSCAllowedPrimitiveExpressionTypeMapID(rs.getInt("cdt_sc_awd_pri_xps_type_map_id"));
				cdt_sc_allowed_primitive_expression_type_mapVO.setCDTSCAllowedPrimitive(rs.getInt("cdt_sc_awd_pri"));
				cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(rs.getInt("xbt_id"));
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
		return cdt_sc_allowed_primitive_expression_type_mapVO;
	}

	public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		CDTSCAllowedPrimitiveExpressionTypeMapVO cdt_sc_allowed_primitive_expression_type_mapVO =
				new CDTSCAllowedPrimitiveExpressionTypeMapVO();
		try {
			String sql = _FIND_CDT_SC_ALLOWED_PRIMITIVE_EXPRESSION_TYPE_MAP_STATEMENT;

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
				cdt_sc_allowed_primitive_expression_type_mapVO.setCTSCAllowedPrimitiveExpressionTypeMapID(rs.getInt("cdt_sc_awd_pri_xps_type_map_id"));
				cdt_sc_allowed_primitive_expression_type_mapVO.setCDTSCAllowedPrimitive(rs.getInt("cdt_sc_awd_pri"));
				cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(rs.getInt("xbt_id"));
			}
		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(rs);
			closeQuietly(ps);
		}
		return cdt_sc_allowed_primitive_expression_type_mapVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			conn = tx.open();
			String sql = _FIND_ALL_CDT_SC_ALLOWED_PRIMITIVE_EXPRESSION_TYPE_MAP_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				CDTSCAllowedPrimitiveExpressionTypeMapVO cdt_sc_allowed_primitive_expression_type_mapVO =
						new CDTSCAllowedPrimitiveExpressionTypeMapVO();
				cdt_sc_allowed_primitive_expression_type_mapVO.setCTSCAllowedPrimitiveExpressionTypeMapID(rs.getInt("cdt_sc_awd_pri_xps_type_map_id"));
				cdt_sc_allowed_primitive_expression_type_mapVO.setCDTSCAllowedPrimitive(rs.getInt("cdt_sc_awd_pri"));
				cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(rs.getInt("xbt_id"));
				list.add(cdt_sc_allowed_primitive_expression_type_mapVO);
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

	public ArrayList<SRTObject> findObjects(Connection conn) throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			String sql = _FIND_ALL_CDT_SC_ALLOWED_PRIMITIVE_EXPRESSION_TYPE_MAP_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				CDTSCAllowedPrimitiveExpressionTypeMapVO cdt_sc_allowed_primitive_expression_type_mapVO =
						new CDTSCAllowedPrimitiveExpressionTypeMapVO();
				cdt_sc_allowed_primitive_expression_type_mapVO.setCTSCAllowedPrimitiveExpressionTypeMapID(rs.getInt("cdt_sc_awd_pri_xps_type_map_id"));
				cdt_sc_allowed_primitive_expression_type_mapVO.setCDTSCAllowedPrimitive(rs.getInt("cdt_sc_awd_pri"));
				cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(rs.getInt("xbt_id"));
				list.add(cdt_sc_allowed_primitive_expression_type_mapVO);
			}
		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(rs);
			closeQuietly(ps);
		}

		return list;
	}

	public boolean updateObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;

		CDTSCAllowedPrimitiveExpressionTypeMapVO cdt_sc_allowed_primitive_expression_type_mapVO =
				(CDTSCAllowedPrimitiveExpressionTypeMapVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_CDT_SC_ALLOWED_PRIMITIVE_EXPRESSION_TYPE_MAP_STATEMENT);

			ps.setInt(1, cdt_sc_allowed_primitive_expression_type_mapVO.getCDTSCAllowedPrimitive());
			ps.setInt(2, cdt_sc_allowed_primitive_expression_type_mapVO.getXSDBuiltInTypeID());
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

		CDTSCAllowedPrimitiveExpressionTypeMapVO cdt_sc_allowed_primitive_expression_type_mapVO =
				(CDTSCAllowedPrimitiveExpressionTypeMapVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_DELETE_CDT_SC_ALLOWED_PRIMITIVE_EXPRESSION_TYPE_MAP_STATEMENT);
			ps.setInt(1, cdt_sc_allowed_primitive_expression_type_mapVO.getCTSCAllowedPrimitiveExpressionTypeMapID());
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
}
