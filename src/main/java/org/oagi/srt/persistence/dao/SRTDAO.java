package org.oagi.srt.persistence.dao;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.PersistenceUtils;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Yunsu Lee
 * @version 1.0
 *
 */
@Repository
public class SRTDAO {

	public int insertObject(SRTObject obj) throws SRTDAOException {
		return 0;
	}

	public int insertObject(SRTObject obj, Connection conn) throws SRTDAOException {
		return 0;
	}

	public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
		return null;
	}

	public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
		return null;
	}

	public ArrayList<SRTObject> findObjects(QueryCondition qc) throws SRTDAOException {
		return new ArrayList();
	}

	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn) throws SRTDAOException {
		return new ArrayList();
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		return new ArrayList();
	}

	public ArrayList<SRTObject> findObjects(Connection conn) throws SRTDAOException {
		return new ArrayList();
	}

	public boolean updateObject(SRTObject obj) throws SRTDAOException {
		return false;
	}

	public boolean deleteObject(SRTObject obj) throws SRTDAOException {
		return false;
	}

	public int findMaxId() throws SRTDAOException {
		return 0;
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
		return count;
	}

}