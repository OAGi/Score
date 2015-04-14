package org.oagi.srt.persistence.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dto.ASCCPVO;

/**
 *
 * @author Yunsu Lee
 * @version 1.0
 *
 */
public abstract class SRTDAO {

	public abstract boolean insertObject(SRTObject obj) throws SRTDAOException;
	
	public abstract SRTObject findObject(QueryCondition qc)	throws SRTDAOException;
	
	public abstract SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException;
	
	public abstract ArrayList<SRTObject> findObjects(QueryCondition qc)	throws SRTDAOException;
	public abstract ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn) throws SRTDAOException;

	public abstract ArrayList<SRTObject> findObjects() throws SRTDAOException;
	public abstract ArrayList<SRTObject> findObjects(Connection conn) throws SRTDAOException;
	
	public abstract boolean updateObject(SRTObject obj) throws SRTDAOException;

	public abstract boolean deleteObject(SRTObject obj) throws SRTDAOException;
	
	public abstract int findMaxId() throws SRTDAOException;
	
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
		return count;
	}

}