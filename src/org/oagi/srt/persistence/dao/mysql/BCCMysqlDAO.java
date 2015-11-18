package org.oagi.srt.persistence.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BCCVO;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/

public class BCCMysqlDAO extends SRTDAO{
	private final String _tableName = "bcc";

	private final String _FIND_ALL_BCC_STATEMENT = 
			"SELECT BCC_ID, BCC_GUID, Cardinality_Min, Cardinality_Max, Assoc_To_BCCP_ID, Assoc_From_ACC_ID, "
					+ "Sequencing_key, Entity_Type, DEN FROM " + _tableName;

	private final String _FIND_BCC_STATEMENT = 
			"SELECT BCC_ID, BCC_GUID, Cardinality_Min, Cardinality_Max, Assoc_To_BCCP_ID, Assoc_From_ACC_ID, "
					+ "Sequencing_key, Entity_Type, DEN FROM " + _tableName;
	
	private final String _INSERT_BCC_STATEMENT = 
			"INSERT INTO " + _tableName + " (BCC_GUID, Cardinality_Min, Cardinality_Max, Assoc_To_BCCP_ID, Assoc_From_ACC_ID, "
					+ "Sequencing_key, Entity_Type, DEN) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	private final String _UPDATE_BCC_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET BCC_GUID = ?, Cardinality_Min = ?, Cardinality_Max = ?, Assoc_To_BCCP_ID = ?, "
			+ "Assoc_From_ACC_ID = ?, Sequencing_key = ?, Entity_Type = ?, DEN = ? WHERE BCC_ID = ?";

	private final String _DELETE_BCC_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE BCC_ID = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		BCCVO bccVO = (BCCVO)obj;
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_BCC_STATEMENT);
			ps.setString(1, bccVO.getBCCGUID());
			ps.setInt(2, bccVO.getCardinalityMin());
			
//			if(bccVO.getCardinalityMax() == -1)
//				ps.setNull(3, java.sql.Types.INTEGER);
//			else
//				ps.setInt(3, bccVO.getCardinalityMax());
			
			ps.setInt(3, bccVO.getCardinalityMax());
			
			ps.setInt(4, bccVO.getAssocToBCCPID());
			ps.setInt(5, bccVO.getAssocFromACCID());
			ps.setInt(6, bccVO.getSequencingKey());
			ps.setInt(7, bccVO.getEntityType());
			ps.setString(8, bccVO.getDEN());

			ps.executeUpdate();

//			ResultSet tableKeys = ps.getGeneratedKeys();
//			tableKeys.next();
//			int autoGeneratedID = tableKeys.getInt(1);
			ps.close();
			tx.commit();
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
		return 1;
	}

	public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		BCCVO bccVO = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_BCC_STATEMENT;

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
				bccVO = new BCCVO();
				bccVO.setBCCID(rs.getInt("BCC_ID"));
				bccVO.setBCCGUID(rs.getString("BCC_GUID"));
				bccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				bccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				bccVO.setAssocToBCCPID(rs.getInt("Assoc_To_BCCP_ID"));
				bccVO.setAssocFromACCID(rs.getInt("Assoc_From_ACC_ID"));
				bccVO.setSequencingKey(rs.getInt("Sequencing_key"));
				bccVO.setEntityType(rs.getInt("Entity_Type"));
				bccVO.setDEN(rs.getString("DEN"));

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
		return bccVO;
	}
	
	public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		BCCVO bccVO = null;
		try {
			//Connection conn = tx.open();
			String sql = _FIND_BCC_STATEMENT;

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
				bccVO = new BCCVO();
				bccVO.setBCCID(rs.getInt("BCC_ID"));
				bccVO.setBCCGUID(rs.getString("BCC_GUID"));
				bccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				bccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				bccVO.setAssocToBCCPID(rs.getInt("Assoc_To_BCCP_ID"));
				bccVO.setAssocFromACCID(rs.getInt("Assoc_From_ACC_ID"));
				bccVO.setSequencingKey(rs.getInt("Sequencing_key"));
				bccVO.setEntityType(rs.getInt("Entity_Type"));
				bccVO.setDEN(rs.getString("DEN"));

			}
			//tx.commit();
			//conn.close();
		//} catch (BfPersistenceException e) {
		//	throw new SRTDAOException(SRTDAOException.DAO_FIND_ERROR, e);
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
		return bccVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_BCC_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				BCCVO bccVO = new BCCVO();
				bccVO.setBCCID(rs.getInt("BCC_ID"));
				bccVO.setBCCGUID(rs.getString("BCC_GUID"));
				bccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				bccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				bccVO.setAssocToBCCPID(rs.getInt("Assoc_To_BCCP_ID"));
				bccVO.setAssocFromACCID(rs.getInt("Assoc_From_ACC_ID"));
				bccVO.setSequencingKey(rs.getInt("Sequencing_key"));
				bccVO.setEntityType(rs.getInt("Entity_Type"));
				bccVO.setDEN(rs.getString("DEN"));
				list.add(bccVO);
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
	
	public ArrayList<SRTObject> findObjects(Connection conn) throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			//Connection conn = tx.open();
			String sql = _FIND_ALL_BCC_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				BCCVO bccVO = new BCCVO();
				bccVO.setBCCID(rs.getInt("BCC_ID"));
				bccVO.setBCCGUID(rs.getString("BCC_GUID"));
				bccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				bccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				bccVO.setAssocToBCCPID(rs.getInt("Assoc_To_BCCP_ID"));
				bccVO.setAssocFromACCID(rs.getInt("Assoc_From_ACC_ID"));
				bccVO.setSequencingKey(rs.getInt("Sequencing_key"));
				bccVO.setEntityType(rs.getInt("Entity_Type"));
				bccVO.setDEN(rs.getString("DEN"));
				list.add(bccVO);
			}
			//tx.commit();
			//conn.close();
		//} catch (BfPersistenceException e) {
		//	throw new SRTDAOException(SRTDAOException.DAO_FIND_ERROR, e);
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
		BCCVO bccVO = (BCCVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_BCC_STATEMENT);
			
			ps.setString(1, bccVO.getBCCGUID());
			ps.setInt(2, bccVO.getCardinalityMin());
			ps.setInt(3, bccVO.getCardinalityMax());
			ps.setInt(4, bccVO.getAssocToBCCPID());
			ps.setInt(5, bccVO.getAssocFromACCID());
			ps.setInt(6, bccVO.getSequencingKey());
			ps.setInt(7, bccVO.getEntityType());
			ps.setString(8, bccVO.getDEN());
			ps.setInt(9, bccVO.getBCCID());
			ps.executeUpdate();

			tx.commit();
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
		BCCVO bccVO = (BCCVO)obj;

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_BCC_STATEMENT);
			ps.setInt(1, bccVO.getBCCID());
			ps.executeUpdate();

			tx.commit();
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
			String sql = _FIND_BCC_STATEMENT;

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
				BCCVO bccVO = new BCCVO();
				bccVO.setBCCID(rs.getInt("BCC_ID"));
				bccVO.setBCCGUID(rs.getString("BCC_GUID"));
				bccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				bccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				bccVO.setAssocToBCCPID(rs.getInt("Assoc_To_BCCP_ID"));
				bccVO.setAssocFromACCID(rs.getInt("Assoc_From_ACC_ID"));
				bccVO.setSequencingKey(rs.getInt("Sequencing_key"));
				bccVO.setEntityType(rs.getInt("Entity_Type"));
				bccVO.setDEN(rs.getString("DEN"));
				list.add(bccVO);
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
	
	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			//Connection conn = tx.open();
			String sql = _FIND_BCC_STATEMENT;

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
				BCCVO bccVO = new BCCVO();
				bccVO.setBCCID(rs.getInt("BCC_ID"));
				bccVO.setBCCGUID(rs.getString("BCC_GUID"));
				bccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				bccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				bccVO.setAssocToBCCPID(rs.getInt("Assoc_To_BCCP_ID"));
				bccVO.setAssocFromACCID(rs.getInt("Assoc_From_ACC_ID"));
				bccVO.setSequencingKey(rs.getInt("Sequencing_key"));
				bccVO.setEntityType(rs.getInt("Entity_Type"));
				bccVO.setDEN(rs.getString("DEN"));
				list.add(bccVO);
			}
			//tx.commit();
			//conn.close();
		//} catch (BfPersistenceException e) {
		//	throw new SRTDAOException(SRTDAOException.DAO_FIND_ERROR, e);
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
	public int insertObject(SRTObject obj, Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}
}
