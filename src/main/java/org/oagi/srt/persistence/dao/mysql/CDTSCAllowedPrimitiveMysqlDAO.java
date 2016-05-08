package org.oagi.srt.persistence.dao.mysql;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveVO;

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
public class CDTSCAllowedPrimitiveMysqlDAO extends SRTDAO {
	private final String _tableName = "cdt_sc_awd_pri";

	private final String _FIND_ALL_CDT_SC_Allowed_Primitive_STATEMENT =
			"SELECT cdt_sc_awd_pri_id, cdt_sc_id, cdt_pri_id, is_default FROM " + _tableName;

	private final String _FIND_CDT_SC_Allowed_Primitive_STATEMENT =
			"SELECT cdt_sc_awd_pri_id, cdt_sc_id, cdt_pri_id, is_default FROM " + _tableName;

	private final String _INSERT_CDT_SC_Allowed_Primitive_STATEMENT =
			"INSERT INTO " + _tableName + " (cdt_sc_id, cdt_pri_id, is_default) VALUES (?, ?, ?)";

	private final String _UPDATE_CDT_SC_Allowed_Primitive_STATEMENT =
			"UPDATE " + _tableName
			+ " SET cdt_sc_awd_pri_id = ?, cdt_sc_id = ?, cdt_pri_id = ?, is_default = ? WHERE cdt_sc_awd_pri_id = ?";

	private final String _DELETE_CDT_SC_Allowed_Primitive_STATEMENT =
			"DELETE FROM " + _tableName + " WHERE cdt_sc_awd_pri_id = ?";


	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}

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
			String sql = _FIND_ALL_CDT_SC_Allowed_Primitive_STATEMENT;

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
				CDTSCAllowedPrimitiveVO cdtscallowedprimitiveVO = new CDTSCAllowedPrimitiveVO();
				cdtscallowedprimitiveVO.setCDTSCAllowedPrimitiveID(rs.getInt("cdt_sc_awd_pri_id"));
				cdtscallowedprimitiveVO.setCDTSCID(rs.getInt("cdt_sc_id"));
				cdtscallowedprimitiveVO.setCDTPrimitiveID(rs.getInt("cdt_pri_id"));
				cdtscallowedprimitiveVO.setisDefault(rs.getBoolean("is_default"));
				list.add(cdtscallowedprimitiveVO);
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
			String sql = _FIND_ALL_CDT_SC_Allowed_Primitive_STATEMENT;

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
				CDTSCAllowedPrimitiveVO cdtscallowedprimitiveVO = new CDTSCAllowedPrimitiveVO();
				cdtscallowedprimitiveVO.setCDTSCAllowedPrimitiveID(rs.getInt("cdt_sc_awd_pri_id"));
				cdtscallowedprimitiveVO.setCDTSCID(rs.getInt("cdt_sc_id"));
				cdtscallowedprimitiveVO.setCDTPrimitiveID(rs.getInt("cdt_pri_id"));
				cdtscallowedprimitiveVO.setisDefault(rs.getBoolean("is_default"));
				list.add(cdtscallowedprimitiveVO);
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

		CDTSCAllowedPrimitiveVO cdtscallowedprimitiveVO = (CDTSCAllowedPrimitiveVO) obj;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_CDT_SC_Allowed_Primitive_STATEMENT);
			ps.setInt(1, cdtscallowedprimitiveVO.getCDTSCID());
			ps.setInt(2, cdtscallowedprimitiveVO.getCDTPrimitiveID());
			ps.setBoolean(3, cdtscallowedprimitiveVO.getisDefault());

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

		CDTSCAllowedPrimitiveVO cdtscallowedprimitiveVO = new CDTSCAllowedPrimitiveVO();
		try {
			conn = tx.open();
			String sql = _FIND_CDT_SC_Allowed_Primitive_STATEMENT;

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
				cdtscallowedprimitiveVO.setCDTSCAllowedPrimitiveID(rs.getInt("cdt_sc_awd_pri_id"));
				cdtscallowedprimitiveVO.setCDTSCID(rs.getInt("cdt_sc_id"));
				cdtscallowedprimitiveVO.setCDTPrimitiveID(rs.getInt("cdt_pri_id"));
				cdtscallowedprimitiveVO.setisDefault(rs.getBoolean("is_default"));
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
		return cdtscallowedprimitiveVO;
	}

	public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		CDTSCAllowedPrimitiveVO cdtscallowedprimitiveVO = new CDTSCAllowedPrimitiveVO();
		try {
			String sql = _FIND_CDT_SC_Allowed_Primitive_STATEMENT;

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
				cdtscallowedprimitiveVO.setCDTSCAllowedPrimitiveID(rs.getInt("cdt_sc_awd_pri_id"));
				cdtscallowedprimitiveVO.setCDTSCID(rs.getInt("cdt_sc_id"));
				cdtscallowedprimitiveVO.setCDTPrimitiveID(rs.getInt("cdt_pri_id"));
				cdtscallowedprimitiveVO.setisDefault(rs.getBoolean("is_default"));
			}
		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(rs);
			closeQuietly(ps);
		}
		return cdtscallowedprimitiveVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			conn = tx.open();
			String sql = _FIND_ALL_CDT_SC_Allowed_Primitive_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				CDTSCAllowedPrimitiveVO cdtscallowedprimitiveVO = new CDTSCAllowedPrimitiveVO();
				cdtscallowedprimitiveVO.setCDTSCAllowedPrimitiveID(rs.getInt("cdt_sc_awd_pri_id"));
				cdtscallowedprimitiveVO.setCDTSCID(rs.getInt("cdt_sc_id"));
				cdtscallowedprimitiveVO.setCDTPrimitiveID(rs.getInt("cdt_pri_id"));
				cdtscallowedprimitiveVO.setisDefault(rs.getBoolean("is_default"));
				list.add(cdtscallowedprimitiveVO);
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

		CDTSCAllowedPrimitiveVO cdtscallowedprimitiveVO = (CDTSCAllowedPrimitiveVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_CDT_SC_Allowed_Primitive_STATEMENT);

			ps.setInt(1, cdtscallowedprimitiveVO.getCDTSCID());
			ps.setInt(2, cdtscallowedprimitiveVO.getCDTPrimitiveID());
			ps.setBoolean(3, cdtscallowedprimitiveVO.getisDefault());
			ps.setInt(4, cdtscallowedprimitiveVO.getCDTSCAllowedPrimitiveID());
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

		CDTSCAllowedPrimitiveVO cdtscallowedprimitiveVO = (CDTSCAllowedPrimitiveVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_DELETE_CDT_SC_Allowed_Primitive_STATEMENT);
			ps.setInt(1, cdtscallowedprimitiveVO.getCDTSCAllowedPrimitiveID());
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
