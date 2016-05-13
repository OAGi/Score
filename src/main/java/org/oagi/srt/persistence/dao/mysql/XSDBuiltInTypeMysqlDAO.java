package org.oagi.srt.persistence.dao.mysql;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/
@Repository
public class XSDBuiltInTypeMysqlDAO extends SRTDAO {
	private final String _tableName = "xbt";

	private final String _FIND_ALL_XSD_BuiltIn_Type_STATEMENT =
			"SELECT xbt_id, name, builtIn_type, subtype_of_xbt_id FROM " + _tableName;

	private final String _FIND_XSD_BuiltIn_Type_STATEMENT =
			"SELECT xbt_id, name, builtIn_type, subtype_of_xbt_id FROM " + _tableName;

	private final String _INSERT_XSD_BuiltIn_Type_STATEMENT =
			"INSERT INTO " + _tableName + " (name, builtIn_type, subtype_of_xbt_id) VALUES (?, ?, ?)";

	private final String _UPDATE_XSD_BuiltIn_Type_STATEMENT =
			"UPDATE " + _tableName
			+ " SET name = ?, builtIn_type = ?, subtype_of_xbt_id = ?, WHERE xbt_id = ?";

	private final String _DELETE_XSD_BuiltIn_Type_STATEMENT =
			"DELETE FROM " + _tableName + " WHERE xbt_id = ?";

	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		XSDBuiltInTypeVO xsdbuiltintypeVO = (XSDBuiltInTypeVO) obj;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_XSD_BuiltIn_Type_STATEMENT);
			ps.setString(1, xsdbuiltintypeVO.getName());
			ps.setString(2, xsdbuiltintypeVO.getBuiltInType());
			ps.setInt(3, xsdbuiltintypeVO.getSubtypeOfXSDBuiltinTypeId());

			ps.executeUpdate();

			rs = ps.getGeneratedKeys();
			rs.next();
			//int autoGeneratedID = tableKeys.getInt(1);

			tx.commit();
		} catch (BfPersistenceException e) {
			tx.rollback();
			throw new SRTDAOException(SRTDAOException.DAO_INSERT_ERROR, e);
		} catch (SQLException e) {
			tx.rollback();
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(rs);
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

		XSDBuiltInTypeVO xsdbuiltintypeVO = new XSDBuiltInTypeVO();
		try {
			conn = tx.open();
			String sql = _FIND_XSD_BuiltIn_Type_STATEMENT;

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
				xsdbuiltintypeVO.setXSDBuiltInTypeID(rs.getInt("xbt_id"));
				xsdbuiltintypeVO.setName(rs.getString("Name"));
				xsdbuiltintypeVO.setBuiltInType(rs.getString("BuiltIn_Type"));
				xsdbuiltintypeVO.setSubtypeOfXSDBuiltinTypeId(rs.getInt("subtype_of_xbt_id"));
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
		return xsdbuiltintypeVO;
	}

	public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		XSDBuiltInTypeVO xsdbuiltintypeVO = new XSDBuiltInTypeVO();
		try {
			String sql = _FIND_XSD_BuiltIn_Type_STATEMENT;

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
				xsdbuiltintypeVO.setXSDBuiltInTypeID(rs.getInt("xbt_id"));
				xsdbuiltintypeVO.setName(rs.getString("Name"));
				xsdbuiltintypeVO.setBuiltInType(rs.getString("BuiltIn_Type"));
				xsdbuiltintypeVO.setSubtypeOfXSDBuiltinTypeId(rs.getInt("subtype_of_xbt_id"));
			}
		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(rs);
			closeQuietly(ps);
		}
		return xsdbuiltintypeVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			conn = tx.open();
			String sql = _FIND_ALL_XSD_BuiltIn_Type_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				XSDBuiltInTypeVO xsdbuiltintypeVO = new XSDBuiltInTypeVO();
				xsdbuiltintypeVO.setXSDBuiltInTypeID(rs.getInt("xbt_id"));
				xsdbuiltintypeVO.setName(rs.getString("Name"));
				xsdbuiltintypeVO.setBuiltInType(rs.getString("BuiltIn_Type"));
				xsdbuiltintypeVO.setSubtypeOfXSDBuiltinTypeId(rs.getInt("subtype_of_xbt_id"));
				list.add(xsdbuiltintypeVO);
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

		XSDBuiltInTypeVO xsdbuiltintypeVO = (XSDBuiltInTypeVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_XSD_BuiltIn_Type_STATEMENT);

			ps.setString(1, xsdbuiltintypeVO.getName());
			ps.setString(2, xsdbuiltintypeVO.getBuiltInType());
			ps.setInt(3, xsdbuiltintypeVO.getSubtypeOfXSDBuiltinTypeId());
			ps.setInt(4, xsdbuiltintypeVO.getXSDBuiltInTypeID());
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

		XSDBuiltInTypeVO xsdbuiltintypeVO = (XSDBuiltInTypeVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_DELETE_XSD_BuiltIn_Type_STATEMENT);
			ps.setInt(1, xsdbuiltintypeVO.getXSDBuiltInTypeID());
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
