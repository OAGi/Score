package org.oagi.srt.persistence.dao.mysql;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BDTPrimitiveRestrictionVO;

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
public class BDTPrimitiveRestrictionMysqlDAO extends SRTDAO {
	private final String _tableName = "bdt_pri_restri";

	private final String _FIND_ALL_BDT_Primitive_Restriction_STATEMENT =
			"SELECT bdt_pri_restri_id, bdt_id, cdt_awd_pri_xps_type_map_id, code_list_id, is_default FROM " + _tableName;

	private final String _FIND_BDT_Primitive_Restriction_STATEMENT =
			"SELECT bdt_pri_restri_id, bdt_id, cdt_awd_pri_xps_type_map_id, code_list_id, is_default FROM " + _tableName;

	private final String _INSERT_BDT_Primitive_Restriction_STATEMENT =
			"INSERT INTO " + _tableName + " (bdt_id, cdt_awd_pri_xps_type_map_id, code_list_id, is_default) VALUES (?, ?, ?, ?)";

	private final String _UPDATE_BDT_Primitive_Restriction_STATEMENT =
			"UPDATE " + _tableName
			+ " SET bdt_pri_restri_id = ?, bdt_id = ?, cdt_awd_pri_xps_type_map_id = ?, code_list_id = ?, is_default = ? WHERE bdt_pri_restri_id = ?";

	private final String _DELETE_BDT_Primitive_Restriction_STATEMENT =
			"DELETE FROM " + _tableName + " WHERE bdt_pri_restri_id = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;

		BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = (BDTPrimitiveRestrictionVO) obj;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_BDT_Primitive_Restriction_STATEMENT);
			ps.setInt(1, bdtprimitiverestrictionVO.getBDTID());

			if (bdtprimitiverestrictionVO.getCDTPrimitiveExpressionTypeMapID() == 0)
				ps.setNull(2, java.sql.Types.INTEGER);
			else
				ps.setInt(2, bdtprimitiverestrictionVO.getCDTPrimitiveExpressionTypeMapID());

			if (bdtprimitiverestrictionVO.getCodeListID() == 0)
				ps.setNull(3, java.sql.Types.INTEGER);
			else
				ps.setInt(3, bdtprimitiverestrictionVO.getCodeListID());

			ps.setBoolean(4, bdtprimitiverestrictionVO.getisDefault());

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

		BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = new BDTPrimitiveRestrictionVO();
		try {
			conn = tx.open();
			String sql = _FIND_BDT_Primitive_Restriction_STATEMENT;

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
				bdtprimitiverestrictionVO.setBDTPrimitiveRestrictionID(rs.getInt("bdt_pri_restri_id"));
				bdtprimitiverestrictionVO.setBDTID(rs.getInt("bdt_id"));
				bdtprimitiverestrictionVO.setCDTPrimitiveExpressionTypeMapID(rs.getInt("cdt_awd_pri_xps_type_map_id"));
				bdtprimitiverestrictionVO.setCodeListID(rs.getInt("code_list_id"));
				bdtprimitiverestrictionVO.setisDefault(rs.getBoolean("is_default"));
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
		return bdtprimitiverestrictionVO;
	}

	public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = new BDTPrimitiveRestrictionVO();
		try {
			String sql = _FIND_BDT_Primitive_Restriction_STATEMENT;

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
				bdtprimitiverestrictionVO.setBDTPrimitiveRestrictionID(rs.getInt("bdt_pri_restri_id"));
				bdtprimitiverestrictionVO.setBDTID(rs.getInt("bdt_id"));
				bdtprimitiverestrictionVO.setCDTPrimitiveExpressionTypeMapID(rs.getInt("cdt_awd_pri_xps_type_map_id"));
				bdtprimitiverestrictionVO.setCodeListID(rs.getInt("code_list_id"));
				bdtprimitiverestrictionVO.setisDefault(rs.getBoolean("is_default"));
			}
		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(rs);
			closeQuietly(ps);
		}
		return bdtprimitiverestrictionVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			conn = tx.open();
			String sql = _FIND_ALL_BDT_Primitive_Restriction_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = new BDTPrimitiveRestrictionVO();
				bdtprimitiverestrictionVO.setBDTPrimitiveRestrictionID(rs.getInt("bdt_pri_restri_id"));
				bdtprimitiverestrictionVO.setBDTID(rs.getInt("bdt_id"));
				bdtprimitiverestrictionVO.setCDTPrimitiveExpressionTypeMapID(rs.getInt("cdt_awd_pri_xps_type_map_id"));
				bdtprimitiverestrictionVO.setCodeListID(rs.getInt("code_list_id"));
				bdtprimitiverestrictionVO.setisDefault(rs.getBoolean("is_default"));
				list.add(bdtprimitiverestrictionVO);
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

	public ArrayList<SRTObject> findObjects(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			conn = tx.open();
			String sql = _FIND_ALL_BDT_Primitive_Restriction_STATEMENT;
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
				BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = new BDTPrimitiveRestrictionVO();
				bdtprimitiverestrictionVO.setBDTPrimitiveRestrictionID(rs.getInt("bdt_pri_restri_id"));
				bdtprimitiverestrictionVO.setBDTID(rs.getInt("bdt_id"));
				bdtprimitiverestrictionVO.setCDTPrimitiveExpressionTypeMapID(rs.getInt("cdt_awd_pri_xps_type_map_id"));
				bdtprimitiverestrictionVO.setCodeListID(rs.getInt("code_list_id"));
				bdtprimitiverestrictionVO.setisDefault(rs.getBoolean("is_default"));
				list.add(bdtprimitiverestrictionVO);
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
			String sql = _FIND_ALL_BDT_Primitive_Restriction_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = new BDTPrimitiveRestrictionVO();
				bdtprimitiverestrictionVO.setBDTPrimitiveRestrictionID(rs.getInt("bdt_pri_restri_id"));
				bdtprimitiverestrictionVO.setBDTID(rs.getInt("bdt_id"));
				bdtprimitiverestrictionVO.setCDTPrimitiveExpressionTypeMapID(rs.getInt("cdt_awd_pri_xps_type_map_id"));
				bdtprimitiverestrictionVO.setCodeListID(rs.getInt("code_list_id"));
				bdtprimitiverestrictionVO.setisDefault(rs.getBoolean("is_default"));
				list.add(bdtprimitiverestrictionVO);
			}
		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(rs);
			closeQuietly(ps);
		}

		return list;
	}

	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn) throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			String sql = _FIND_ALL_BDT_Primitive_Restriction_STATEMENT;
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
				BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = new BDTPrimitiveRestrictionVO();
				bdtprimitiverestrictionVO.setBDTPrimitiveRestrictionID(rs.getInt("bdt_pri_restri_id"));
				bdtprimitiverestrictionVO.setBDTID(rs.getInt("bdt_id"));
				bdtprimitiverestrictionVO.setCDTPrimitiveExpressionTypeMapID(rs.getInt("cdt_awd_pri_xps_type_map_id"));
				bdtprimitiverestrictionVO.setCodeListID(rs.getInt("code_list_id"));
				bdtprimitiverestrictionVO.setisDefault(rs.getBoolean("is_default"));
				list.add(bdtprimitiverestrictionVO);
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

		BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = (BDTPrimitiveRestrictionVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_BDT_Primitive_Restriction_STATEMENT);

			ps.setInt(1, bdtprimitiverestrictionVO.getBDTID());
			ps.setInt(2, bdtprimitiverestrictionVO.getCDTPrimitiveExpressionTypeMapID());
			ps.setInt(3, bdtprimitiverestrictionVO.getCodeListID());
			ps.setBoolean(4, bdtprimitiverestrictionVO.getisDefault());
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

		BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = (BDTPrimitiveRestrictionVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_DELETE_BDT_Primitive_Restriction_STATEMENT);
			ps.setInt(1, bdtprimitiverestrictionVO.getBDTPrimitiveRestrictionID());
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
	public int insertObject(SRTObject obj, Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
