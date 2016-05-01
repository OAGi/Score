package org.oagi.srt.persistence.dao.mysql;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BIEUserExtensionRevisionVO;

import java.sql.*;
import java.util.ArrayList;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/

public class BIEUserExtensionRevisionMysqlDAO extends SRTDAO{
	private final String _tableName = "bie_user_ext_revision";

	private final String _FIND_ALL_BIEUserExtensionRevision_STATEMENT =
			"SELECT bie_user_ext_revision_id, top_level_abie_id, ext_abie_id, ext_acc_id, user_ext_acc_id, revised_indicator"
					+ " FROM " + _tableName;

	private final String _FIND_BIEUserExtensionRevision_STATEMENT =
			"SELECT bie_user_ext_revision_id, top_level_abie_id, ext_abie_id, ext_acc_id, user_ext_acc_id, revised_indicator"
					+ " FROM " + _tableName;

	private final String _INSERT_BIEUserExtensionRevision_STATEMENT =
			"INSERT INTO " + _tableName + " (top_level_abie_id, ext_abie_id, ext_acc_id, user_ext_acc_id, revised_indicator)"
					+ " VALUES (?, ?, ?, ?, ?)";

	private final String _UPDATE_BIEUserExtensionRevision_STATEMENT =
			"UPDATE " + _tableName
			+ " SET top_level_abie_id = ?, ext_abie_id = ?,  ext_acc_id = ?, user_ext_acc_id = ?, revised_indicator = ? "
			+ " where bie_user_ext_revision_id = ?";

	private final String _DELETE_BIEUserExtensionRevision_STATEMENT =
			"DELETE FROM " + _tableName + " WHERE bie_user_ext_revision_id = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		BIEUserExtensionRevisionVO bieUserExtensionRevisionVO = (BIEUserExtensionRevisionVO) obj;
		int key = -1;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_BIEUserExtensionRevision_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, bieUserExtensionRevisionVO.getTop_level_abie_id());
			ps.setInt(2, bieUserExtensionRevisionVO.getExt_abie_id());
			ps.setInt(3, bieUserExtensionRevisionVO.getExt_acc_id());
			ps.setInt(4, bieUserExtensionRevisionVO.getUser_ext_acc_id());
			ps.setBoolean(5, bieUserExtensionRevisionVO.getRevised_indicator());
			ps.executeUpdate();

			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			}
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
		return key;
	}

	public int insertObject(SRTObject obj, Connection conn) throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		BIEUserExtensionRevisionVO bieUserExtensionRevisionVO = (BIEUserExtensionRevisionVO) obj;
		int key = -1;
		try {
			ps = conn.prepareStatement(_INSERT_BIEUserExtensionRevision_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, bieUserExtensionRevisionVO.getTop_level_abie_id());
			ps.setInt(2, bieUserExtensionRevisionVO.getExt_abie_id());
			ps.setInt(3, bieUserExtensionRevisionVO.getExt_acc_id());
			ps.setInt(4, bieUserExtensionRevisionVO.getUser_ext_acc_id());
			ps.setBoolean(5, bieUserExtensionRevisionVO.getRevised_indicator());

			ps.executeUpdate();

			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(rs);
			closeQuietly(ps);
		}
		return key;
	}

	public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		BIEUserExtensionRevisionVO bieUserExtensionRevisionVO = new BIEUserExtensionRevisionVO();
		try {
			conn = tx.open();
			String sql = _FIND_BIEUserExtensionRevision_STATEMENT;

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
				bieUserExtensionRevisionVO.setBie_user_ext_revision_id(rs.getInt("bie_user_ext_revision_id"));
				bieUserExtensionRevisionVO.setTop_level_abie_id(rs.getInt("top_level_abie_id"));
				bieUserExtensionRevisionVO.setExt_abie_id(rs.getInt("ext_abie_id"));
				bieUserExtensionRevisionVO.setExt_acc_id(rs.getInt("ext_acc_id"));
				bieUserExtensionRevisionVO.setUser_ext_acc_id(rs.getInt("user_ext_acc_id"));
				bieUserExtensionRevisionVO.setRevised_indicator(rs.getBoolean("revised_indicator"));
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
		return bieUserExtensionRevisionVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			conn = tx.open();
			String sql = _FIND_ALL_BIEUserExtensionRevision_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				BIEUserExtensionRevisionVO bieUserExtensionRevisionVO = new BIEUserExtensionRevisionVO();
				bieUserExtensionRevisionVO.setBie_user_ext_revision_id(rs.getInt("bie_user_ext_revision_id"));
				bieUserExtensionRevisionVO.setTop_level_abie_id(rs.getInt("top_level_abie_id"));
				bieUserExtensionRevisionVO.setExt_abie_id(rs.getInt("ext_abie_id"));
				bieUserExtensionRevisionVO.setExt_acc_id(rs.getInt("ext_acc_id"));
				bieUserExtensionRevisionVO.setUser_ext_acc_id(rs.getInt("user_ext_acc_id"));
				bieUserExtensionRevisionVO.setRevised_indicator(rs.getBoolean("revised_indicator"));
				list.add(bieUserExtensionRevisionVO);
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

		BIEUserExtensionRevisionVO bieUserExtensionRevisionVO = (BIEUserExtensionRevisionVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_BIEUserExtensionRevision_STATEMENT);

			ps.setInt(1, bieUserExtensionRevisionVO.getTop_level_abie_id());
			ps.setInt(2, bieUserExtensionRevisionVO.getExt_abie_id());
			ps.setInt(3, bieUserExtensionRevisionVO.getExt_acc_id());
			ps.setInt(4, bieUserExtensionRevisionVO.getUser_ext_acc_id());
			ps.setBoolean(5, bieUserExtensionRevisionVO.getRevised_indicator());
			ps.setInt(6, bieUserExtensionRevisionVO.getBie_user_ext_revision_id());

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

		BIEUserExtensionRevisionVO bieUserExtensionRevisionVO = (BIEUserExtensionRevisionVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_DELETE_BIEUserExtensionRevision_STATEMENT);
			ps.setInt(1, bieUserExtensionRevisionVO.getBie_user_ext_revision_id());
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
	public ArrayList<SRTObject> findObjects(QueryCondition qc)
			throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			conn = tx.open();
			String sql = _FIND_BIEUserExtensionRevision_STATEMENT;

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
				BIEUserExtensionRevisionVO bieUserExtensionRevisionVO = new BIEUserExtensionRevisionVO();
				bieUserExtensionRevisionVO.setBie_user_ext_revision_id(rs.getInt("bie_user_ext_revision_id"));
				bieUserExtensionRevisionVO.setTop_level_abie_id(rs.getInt("top_level_abie_id"));
				bieUserExtensionRevisionVO.setExt_abie_id(rs.getInt("ext_abie_id"));
				bieUserExtensionRevisionVO.setExt_acc_id(rs.getInt("ext_acc_id"));
				bieUserExtensionRevisionVO.setUser_ext_acc_id(rs.getInt("user_ext_acc_id"));
				bieUserExtensionRevisionVO.setRevised_indicator(rs.getBoolean("revised_indicator"));
				list.add(bieUserExtensionRevisionVO);
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

	@Override
	public SRTObject findObject(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			String sql = _FIND_BIEUserExtensionRevision_STATEMENT;

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
				BIEUserExtensionRevisionVO bieUserExtensionRevisionVO = new BIEUserExtensionRevisionVO();
				bieUserExtensionRevisionVO.setBie_user_ext_revision_id(rs.getInt("bie_user_ext_revision_id"));
				bieUserExtensionRevisionVO.setTop_level_abie_id(rs.getInt("top_level_abie_id"));
				bieUserExtensionRevisionVO.setExt_abie_id(rs.getInt("ext_abie_id"));
				bieUserExtensionRevisionVO.setExt_acc_id(rs.getInt("ext_acc_id"));
				bieUserExtensionRevisionVO.setUser_ext_acc_id(rs.getInt("user_ext_acc_id"));
				bieUserExtensionRevisionVO.setRevised_indicator(rs.getBoolean("revised_indicator"));
				list.add(bieUserExtensionRevisionVO);
			}

		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(rs);
			closeQuietly(ps);
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
