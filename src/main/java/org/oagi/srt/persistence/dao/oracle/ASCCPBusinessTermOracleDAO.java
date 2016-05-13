package org.oagi.srt.persistence.dao.oracle;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ASCCPBusinessTermVO;
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
public class ASCCPBusinessTermOracleDAO extends SRTDAO {
	private final String _tableName = "asccp_business_term";

	private final String _FIND_ALL_ASCCP_BUSINESS_TERM_STATEMENT = 
			"SELECT idASCCP_Business_Term FROM " + _tableName;
	
	private final String _FIND_ASCCP_BUSINESS_TERM_STATEMENT = 
			"SELECT idASCCP_Business_Term FROM " + _tableName;
	
	private final String _INSERT_ASCCP_BUSINESS_TERM_STATEMENT = 
			"INSERT INTO " + _tableName + " (idASCCP_Business_Term) VALUES (?)";
	
	private final String _UPDATE_ASCCP_BUSINESS_TERM_STATEMENT = 
			"UPDATE " + _tableName + " SET idASCCP_Business_Term = ? WHERE idASCCP_Business_Term = ?";
	
	private final String _DELETE_ASCCP_BUSINESS_TERM_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE idASCCP_Business_Term = ?";

	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ASCCPBusinessTermVO asccp_business_termVO = (ASCCPBusinessTermVO) obj;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_ASCCP_BUSINESS_TERM_STATEMENT);
			ps.setInt(1, asccp_business_termVO.getidASCCPBusinessTerm());

			ps.executeUpdate();

			rs = ps.getGeneratedKeys();
			rs.next();
			int autoGeneratedID = rs.getInt(1);

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

		ASCCPBusinessTermVO asccp_business_termVO = new ASCCPBusinessTermVO();
		try {
			conn = tx.open();
			String sql = _FIND_ASCCP_BUSINESS_TERM_STATEMENT;

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
				asccp_business_termVO.setidASCCPBusinessTerm(rs.getInt("idASCCP_Business_Term"));
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
		return asccp_business_termVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			conn = tx.open();
			String sql = _FIND_ALL_ASCCP_BUSINESS_TERM_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ASCCPBusinessTermVO asccp_business_termVO = new ASCCPBusinessTermVO();
				asccp_business_termVO.setidASCCPBusinessTerm(rs.getInt("idASCCP_Business_Term"));
				list.add(asccp_business_termVO);
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

		ASCCPBusinessTermVO asccp_business_termVO = (ASCCPBusinessTermVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_ASCCP_BUSINESS_TERM_STATEMENT);
			ps.setInt(1, asccp_business_termVO.getidASCCPBusinessTerm());

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

		ASCCPBusinessTermVO asccp_business_termVO = (ASCCPBusinessTermVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_DELETE_ASCCP_BUSINESS_TERM_STATEMENT);
			ps.setInt(1, asccp_business_termVO.getidASCCPBusinessTerm());
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
