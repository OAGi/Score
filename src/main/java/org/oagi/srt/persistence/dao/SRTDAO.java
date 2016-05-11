package org.oagi.srt.persistence.dao;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.PersistenceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yunsu Lee
 * @version 1.0
 *
 */
@Repository
public class SRTDAO extends NamedParameterJdbcDaoSupport {

	@Autowired
	public void init(JdbcTemplate jdbcTemplate) {
		setJdbcTemplate(jdbcTemplate);
	}

	public int insertObject(SRTObject obj) throws SRTDAOException {
		throw new UnsupportedOperationException();
	}

	public int insertObject(SRTObject obj, Connection conn) throws SRTDAOException {
		throw new UnsupportedOperationException();
	}

	public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
		throw new UnsupportedOperationException();
	}

	public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
		throw new UnsupportedOperationException();
	}

	public ArrayList<SRTObject> findObjects(QueryCondition qc) throws SRTDAOException {
		throw new UnsupportedOperationException();
	}

	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn) throws SRTDAOException {
		throw new UnsupportedOperationException();
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		throw new UnsupportedOperationException();
	}

	public ArrayList<SRTObject> findObjects(Connection conn) throws SRTDAOException {
		throw new UnsupportedOperationException();
	}

	public boolean updateObject(SRTObject obj) throws SRTDAOException {
		throw new UnsupportedOperationException();
	}

	public boolean deleteObject(SRTObject obj) throws SRTDAOException {
		throw new UnsupportedOperationException();
	}

	public int findMaxId() throws SRTDAOException {
		throw new UnsupportedOperationException();
	}

	public final void closeQuietly(DBAgent txAgent) {
		PersistenceUtils.closeQuietly(txAgent);
	}

	public final void closeQuietly(AutoCloseable closeable) {
		PersistenceUtils.closeQuietly(closeable);
	}

	public int getASCCCount(int accId) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		int count = 0;
		try {
			conn = tx.open();
			String sql = "select count(*) as num from ascc where assoc_from_acc_id = ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, accId);
			rs = ps.executeQuery();
			if (rs.next()) {
				count = rs.getInt("num");
			}
			tx.commit();
			conn.close();
		} catch (BfPersistenceException e) {
			throw new SRTDAOException(SRTDAOException.DAO_FIND_ERROR, e);
		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
			}
			tx.close();
		}
		return count;
	}

}