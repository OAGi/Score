package org.oagi.srt.persistence.dao.mysql;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.UserVO;
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
public class UserMysqlDAO extends SRTDAO {

	private final String _tableName = "app_user";

	private final String _FIND_ALL_USER_STATEMENT =
			"SELECT app_user_id, login_id, Password, Name, Organization, oagis_developer_indicator FROM " + _tableName;

	private final String _FIND_USER_STATEMENT =
			"SELECT app_user_id, login_id, Password, Name, Organization, oagis_developer_indicator FROM " + _tableName;

	private final String _INSERT_USER_STATEMENT = "INSERT INTO " + _tableName +
			" (login_id, Password, Name, Organization, oagis_developer_indicator) VALUES (?, ?, ?, ?, ?)";

	private final String _UPDATE_USER_STATEMENT = "UPDATE " + _tableName +
			" SET login_id = ?, Password = ?, Name = ?, Organization = ?, oagis_developer_indicator = ? WHERE app_user_id = ?";

	private final String _DELETE_USER_STATEMENT = "DELETE FROM " + _tableName + " WHERE app_user_id = ?";

	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;

		UserVO userVO = (UserVO) obj;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_USER_STATEMENT);
			ps.setString(1, userVO.getUserName());
			ps.setString(2, userVO.getPassword());
			ps.setString(3, userVO.getName());
			ps.setString(4, userVO.getOrganization());
			ps.setBoolean(5, userVO.getOagis_developer_indicator());

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

		UserVO userVO = new UserVO();
		try {
			conn = tx.open();
			String sql = _FIND_USER_STATEMENT;

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
				userVO.setUserID(rs.getInt("app_user_id"));
				userVO.setUserName(rs.getString("login_id"));
				userVO.setPassword(rs.getString("Password"));
				userVO.setName(rs.getString("Name"));
				userVO.setOrganization(rs.getString("Organization"));
				userVO.setOagis_developer_indicator(rs.getBoolean("oagis_developer_indicator"));

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
		return userVO;
	}

	public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		UserVO userVO = new UserVO();
		try {
			String sql = _FIND_USER_STATEMENT;

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
				userVO.setUserID(rs.getInt("app_user_id"));
				userVO.setUserName(rs.getString("login_id"));
				userVO.setPassword(rs.getString("Password"));
				userVO.setName(rs.getString("Name"));
				userVO.setOrganization(rs.getString("Organization"));
				userVO.setOagis_developer_indicator(rs.getBoolean("oagis_developer_indicator"));

			}
		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(rs);
			closeQuietly(ps);
		}
		return userVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			conn = tx.open();
			String sql = _FIND_ALL_USER_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				UserVO userVO = new UserVO();
				userVO.setUserID(rs.getInt("app_user_id"));
				userVO.setUserName(rs.getString("login_id"));
				userVO.setPassword(rs.getString("Password"));
				userVO.setName(rs.getString("Name"));
				userVO.setOrganization(rs.getString("Organization"));
				userVO.setOagis_developer_indicator(rs.getBoolean("oagis_developer_indicator"));
				list.add(userVO);
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
			String sql = _FIND_ALL_USER_STATEMENT;
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
				UserVO userVO = new UserVO();
				userVO.setUserID(rs.getInt("app_user_id"));
				userVO.setUserName(rs.getString("login_id"));
				userVO.setPassword(rs.getString("Password"));
				userVO.setName(rs.getString("Name"));
				userVO.setOrganization(rs.getString("Organization"));
				userVO.setOagis_developer_indicator(rs.getBoolean("oagis_developer_indicator"));
				list.add(userVO);
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
			String sql = _FIND_ALL_USER_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				UserVO userVO = new UserVO();
				userVO.setUserID(rs.getInt("app_user_id"));
				userVO.setUserName(rs.getString("login_id"));
				userVO.setPassword(rs.getString("Password"));
				userVO.setName(rs.getString("Name"));
				userVO.setOrganization(rs.getString("Organization"));
				userVO.setOagis_developer_indicator(rs.getBoolean("oagis_developer_indicator"));
				list.add(userVO);
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
			String sql = _FIND_ALL_USER_STATEMENT;
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
				UserVO userVO = new UserVO();
				userVO.setUserID(rs.getInt("app_user_id"));
				userVO.setUserName(rs.getString("login_id"));
				userVO.setPassword(rs.getString("Password"));
				userVO.setName(rs.getString("Name"));
				userVO.setOrganization(rs.getString("Organization"));
				userVO.setOagis_developer_indicator(rs.getBoolean("oagis_developer_indicator"));
				list.add(userVO);
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

		UserVO userVO = (UserVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_USER_STATEMENT);

			ps.setString(1, userVO.getUserName());
			ps.setString(2, userVO.getPassword());
			ps.setString(3, userVO.getName());
			ps.setString(4, userVO.getOrganization());
			ps.setBoolean(5, userVO.getOagis_developer_indicator());
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

		UserVO userVO = (UserVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_DELETE_USER_STATEMENT);
			ps.setInt(1, userVO.getUserID());
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
