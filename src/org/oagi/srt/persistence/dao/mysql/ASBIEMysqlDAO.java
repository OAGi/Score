package org.oagi.srt.persistence.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ASBIEVO;
import org.oagi.srt.persistence.dto.DTVO;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/

public class ASBIEMysqlDAO extends SRTDAO{
	private final String _tableName = "asbie";

	private final String _FIND_ALL_ASBIE_STATEMENT = 
			"SELECT ASBIE_ID, guid, From_ABIE_ID, To_ASBIEP_ID, Based_ASCC, definition, Cardinality_Min, Cardinality_Max, "
			+ "is_nillable, remark, created_by, last_updated_by, creation_timestamp, last_update_timestamp, seq_key FROM "
					+ _tableName;

	private final String _FIND_ASBIE_STATEMENT = 
			"SELECT ASBIE_ID, guid, From_ABIE_ID, To_ASBIEP_ID, Based_ASCC, definition, Cardinality_Min, Cardinality_Max, "
			+ "is_nillable, remark, created_by, last_updated_by, creation_timestamp, last_update_timestamp, seq_key FROM "
					+ _tableName;
	
	private final String _INSERT_ASBIE_STATEMENT = 
			"INSERT INTO " + _tableName + " (GUID, From_ABIE_ID, To_ASBIEP_ID, Based_ASCC, definition, Cardinality_Min, Cardinality_Max, "
					+ "is_nillable, remark, created_by, last_updated_by, creation_timestamp, last_update_timestamp, seq_key)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)";

	private final String _UPDATE_ASBIE_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET From_ABIE_ID = ?, To_ASBIEP_ID = ?, Based_ASCC = ?, definition = ?, Cardinality_Min = ?, "
			+ "Cardinality_Max = ?, guid = ?, is_nillable = ?, remark = ?, last_updated_by = ?, last_update_timestamp = CURRENT_TIMESTAMP, seq_key = ? where ASBIE_ID = ?";

	private final String _DELETE_ASBIE_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE ASBIE_ID = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		ASBIEVO asbievo = (ASBIEVO)obj;
		Connection conn = null;
		PreparedStatement ps = null;
		int key = -1;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_ASBIE_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, asbievo.getAsbieGuid());
			ps.setInt(2, asbievo.getAssocFromABIEID());
			ps.setInt(3, asbievo.getAssocToASBIEPID());
			ps.setInt(4, asbievo.getBasedASCC());
			ps.setString(5, asbievo.getDefinition());
			ps.setInt(6, asbievo.getCardinalityMin());
			ps.setInt(7, asbievo.getCardinalityMax());
			ps.setInt(8, asbievo.getNillable());
			ps.setString(9, asbievo.getRemark());
			ps.setInt(10, asbievo.getCreatedByUserId());
			ps.setInt(11, asbievo.getLastUpdatedByUserId());
			ps.setDouble(12, asbievo.getSequencingKey());
			ps.executeUpdate();

			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()){
			    key = rs.getInt(1);
			}
			rs.close();
			ps.close();
			tx.commit();
			conn.close();
		} catch (BfPersistenceException e) {
			tx.rollback();
			throw new SRTDAOException(SRTDAOException.DAO_INSERT_ERROR, e);
		} catch (SQLException e) {
			e.printStackTrace();
			tx.rollback();
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			if(ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {}
			}
			try {
				if(conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {}
			tx.close();
		}
		return key;
	}
	
	public int insertObject(SRTObject obj, Connection conn) throws SRTDAOException {
		ASBIEVO asbievo = (ASBIEVO)obj;
		PreparedStatement ps = null;
		int key = -1;
		try {
			ps = conn.prepareStatement(_INSERT_ASBIE_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, asbievo.getAsbieGuid());
			ps.setInt(2, asbievo.getAssocFromABIEID());
			ps.setInt(3, asbievo.getAssocToASBIEPID());
			ps.setInt(4, asbievo.getBasedASCC());
			ps.setString(5, asbievo.getDefinition());
			ps.setInt(6, asbievo.getCardinalityMin());
			ps.setInt(7, asbievo.getCardinalityMax());
			ps.setInt(8, asbievo.getNillable());
			ps.setString(9, asbievo.getRemark());
			ps.setInt(10, asbievo.getCreatedByUserId());
			ps.setInt(11, asbievo.getLastUpdatedByUserId());
			ps.setDouble(12, asbievo.getSequencingKey());
			ps.executeUpdate();

			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()){
			    key = rs.getInt(1);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			if(ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {}
			}
		}
		return key;
	}

	public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ASBIEVO asbievo = new ASBIEVO();
		try {
			Connection conn = tx.open();
			String sql = _FIND_ASBIE_STATEMENT;

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
						ps.setString(n+1, (String) value);
					} else if (value instanceof Integer) {
						ps.setInt(n+1, ((Integer) value).intValue());
					}
				}
			}

			rs = ps.executeQuery();
			if (rs.next()) {
				asbievo.setASBIEID(rs.getInt("ASBIE_ID"));
				asbievo.setAsbieGuid(rs.getString("GUID"));
				asbievo.setAssocFromABIEID(rs.getInt("From_ABIE_ID"));
				asbievo.setAssocToASBIEPID(rs.getInt("To_ASBIEP_ID"));
				asbievo.setBasedASCC(rs.getInt("Based_ASCC"));
				asbievo.setDefinition(rs.getString("Definition"));
				asbievo.setCardinalityMin(rs.getInt("Cardinality_Min"));
				asbievo.setCardinalityMax(rs.getInt("Cardinality_Max"));
				asbievo.setNillable(rs.getInt("is_Nillable"));
				asbievo.setRemark(rs.getString("Remark"));
				asbievo.setCreatedByUserId(rs.getInt("created_by"));
				asbievo.setLastUpdatedByUserId(rs.getInt("last_updated_by"));
				asbievo.setSequencingKey(rs.getDouble("seq_key"));
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
			tx.close();
		}
		return asbievo;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_ASBIE_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ASBIEVO asbievo = new ASBIEVO();
				asbievo.setASBIEID(rs.getInt("ASBIE_ID"));
				asbievo.setAsbieGuid(rs.getString("GUID"));
				asbievo.setAssocFromABIEID(rs.getInt("From_ABIE_ID"));
				asbievo.setAssocToASBIEPID(rs.getInt("To_ASBIEP_ID"));
				asbievo.setBasedASCC(rs.getInt("Based_ASCC"));
				asbievo.setDefinition(rs.getString("Definition"));
				asbievo.setCardinalityMin(rs.getInt("Cardinality_Min"));
				asbievo.setCardinalityMax(rs.getInt("Cardinality_Max"));
				asbievo.setNillable(rs.getInt("is_Nillable"));
				asbievo.setRemark(rs.getString("Remark"));
				asbievo.setCreatedByUserId(rs.getInt("created_by"));
				asbievo.setLastUpdatedByUserId(rs.getInt("last_updated_by"));
				asbievo.setSequencingKey(rs.getDouble("seq_key"));
				list.add(asbievo);
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
			tx.close();
		}

		return list;
	}

	public boolean updateObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		ASBIEVO asbievo = (ASBIEVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_ASBIE_STATEMENT);
			
			ps.setString(1, asbievo.getAsbieGuid());
			ps.setInt(2, asbievo.getAssocFromABIEID());
			ps.setInt(3, asbievo.getAssocToASBIEPID());
			ps.setInt(4, asbievo.getBasedASCC());
			ps.setString(5, asbievo.getDefinition());
			ps.setInt(6, asbievo.getCardinalityMin());
			ps.setInt(7, asbievo.getCardinalityMax());
			ps.setInt(8, asbievo.getNillable());
			ps.setString(9, asbievo.getRemark());
			ps.setInt(10, asbievo.getCreatedByUserId());
			ps.setInt(11, asbievo.getLastUpdatedByUserId());
			ps.setDouble(12, asbievo.getSequencingKey());
			
			ps.executeUpdate();

			tx.commit();
			conn.close();
		} catch (BfPersistenceException e) {
			tx.rollback(e);
			throw new SRTDAOException(SRTDAOException.DAO_UPDATE_ERROR, e);
		} catch (SQLException e) {
			tx.rollback(e);
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			if(ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {}
			}
			tx.close();
		}

		return true;
	}

	public boolean deleteObject(SRTObject obj) throws SRTDAOException {
		ASBIEVO asbievo = (ASBIEVO)obj;

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_ASBIE_STATEMENT);
			ps.setInt(1, asbievo.getASBIEID());
			ps.executeUpdate();

			tx.commit();
			conn.close();
		} catch (BfPersistenceException e) {
			tx.rollback(e);
			throw new SRTDAOException(SRTDAOException.DAO_DELETE_ERROR, e);
		} catch (SQLException e) {
			tx.rollback(e);
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			if(ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {}
			}
			tx.close();
		}

		return true;

	}

	@Override
	public ArrayList<SRTObject> findObjects(QueryCondition qc)
			throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ASBIE_STATEMENT;

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
						ps.setString(n+1, (String) value);
					} else if (value instanceof Integer) {
						ps.setInt(n+1, ((Integer) value).intValue());
					}
				}
			}

			rs = ps.executeQuery();
			while (rs.next()) {
				ASBIEVO asbievo = new ASBIEVO();
				asbievo.setASBIEID(rs.getInt("ASBIE_ID"));
				asbievo.setAsbieGuid(rs.getString("GUID"));
				asbievo.setAssocFromABIEID(rs.getInt("From_ABIE_ID"));
				asbievo.setAssocToASBIEPID(rs.getInt("To_ASBIEP_ID"));
				asbievo.setBasedASCC(rs.getInt("Based_ASCC"));
				asbievo.setDefinition(rs.getString("Definition"));
				asbievo.setCardinalityMin(rs.getInt("Cardinality_Min"));
				asbievo.setCardinalityMax(rs.getInt("Cardinality_Max"));
				asbievo.setNillable(rs.getInt("is_Nillable"));
				asbievo.setRemark(rs.getString("Remark"));
				asbievo.setCreatedByUserId(rs.getInt("created_by"));
				asbievo.setLastUpdatedByUserId(rs.getInt("last_updated_by"));
				asbievo.setSequencingKey(rs.getDouble("seq_key"));
				list.add(asbievo);
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
			tx.close();
		}

		return list;
	}

	@Override
	public SRTObject findObject(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ASBIEVO asbievo = null;
		
		try {
			String sql = _FIND_ASBIE_STATEMENT;

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
						ps.setString(n+1, (String) value);
					} else if (value instanceof Integer) {
						ps.setInt(n+1, ((Integer) value).intValue());
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
			if (rs.next()) {
				asbievo = new ASBIEVO();
				asbievo.setASBIEID(rs.getInt("ASBIE_ID"));
				asbievo.setAsbieGuid(rs.getString("GUID"));
				asbievo.setAssocFromABIEID(rs.getInt("From_ABIE_ID"));
				asbievo.setAssocToASBIEPID(rs.getInt("To_ASBIEP_ID"));
				asbievo.setBasedASCC(rs.getInt("Based_ASCC"));
				asbievo.setDefinition(rs.getString("Definition"));
				asbievo.setCardinalityMin(rs.getInt("Cardinality_Min"));
				asbievo.setCardinalityMax(rs.getInt("Cardinality_Max"));
				asbievo.setNillable(rs.getInt("is_Nillable"));
				asbievo.setRemark(rs.getString("Remark"));
				asbievo.setCreatedByUserId(rs.getInt("created_by"));
				asbievo.setLastUpdatedByUserId(rs.getInt("last_updated_by"));
				asbievo.setSequencingKey(rs.getDouble("seq_key"));
			}
			
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
		}
		return asbievo;

	}

	@Override
	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			String sql = _FIND_ASBIE_STATEMENT;

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
						ps.setString(n+1, (String) value);
					} else if (value instanceof Integer) {
						ps.setInt(n+1, ((Integer) value).intValue());
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
				ASBIEVO asbievo = new ASBIEVO();
				asbievo.setASBIEID(rs.getInt("ASBIE_ID"));
				asbievo.setAsbieGuid(rs.getString("GUID"));
				asbievo.setAssocFromABIEID(rs.getInt("From_ABIE_ID"));
				asbievo.setAssocToASBIEPID(rs.getInt("To_ASBIEP_ID"));
				asbievo.setBasedASCC(rs.getInt("Based_ASCC"));
				asbievo.setDefinition(rs.getString("Definition"));
				asbievo.setCardinalityMin(rs.getInt("Cardinality_Min"));
				asbievo.setCardinalityMax(rs.getInt("Cardinality_Max"));
				asbievo.setNillable(rs.getInt("is_Nillable"));
				asbievo.setRemark(rs.getString("Remark"));
				asbievo.setCreatedByUserId(rs.getInt("created_by"));
				asbievo.setLastUpdatedByUserId(rs.getInt("last_updated_by"));
				asbievo.setSequencingKey(rs.getDouble("seq_key"));
				list.add(asbievo);
			}
			
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
