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
import org.oagi.srt.persistence.dto.BBIE_SCVO;

/**
 *
 * @author Nasif Sikder
 * @version 1.0
 *
 */
public class BBIE_SCMysqlDAO extends SRTDAO {
	
	private final String _tableName = "bbie_sc";

	private final String _FIND_ALL_BBIE_SC_STATEMENT = "SELECT BBIE_SC_ID, BBIE_ID, DT_SC_ID, "
			+ "Min_Cardinality, Max_Cardinality, DT_SC_Primitive_Restriction_ID FROM " + _tableName;
	
	private final String _FIND_BBIE_SC_STATEMENT = "SELECT BBIE_SC_ID, BBIE_ID, DT_SC_ID, "
			+ "Min_Cardinality, Max_Cardinality, DT_SC_Primitive_Restriction_ID FROM " + _tableName;
	
	private final String _INSERT_BBIE_SC_STATEMENT = "INSERT INTO " + _tableName + " (BBIE_ID, "
			+ "DT_SC_ID, Min_Cardinality, Max_Cardinality, DT_SC_Primitive_Restriction_ID) VALUES (?, ?, ?, ?, ?)";
	
	private final String _INSERT_BBIE_SC_WITH_ID_STATEMENT = "INSERT INTO " + _tableName + " (BBIE_ID, "
			+ "DT_SC_ID, Min_Cardinality, Max_Cardinality, DT_SC_Primitive_Restriction_ID, BBIE_SC_ID) VALUES (?, ?, ?, ?, ?, ?)";
	
	private final String _DELETE_BBIE_SC_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE BBIE_SC_ID = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		BBIE_SCVO bbie_scVO = (BBIE_SCVO)obj;
		Connection conn = null;
		PreparedStatement ps = null;
		int key = -1;
		try {
			conn = tx.open();
			if(bbie_scVO.getBBIESCID() == -1)
				ps = conn.prepareStatement(_INSERT_BBIE_SC_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			else
				ps = conn.prepareStatement(_INSERT_BBIE_SC_WITH_ID_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			
			
			ps.setInt(1, bbie_scVO.getBBIEID());
			ps.setInt(2, bbie_scVO.getDTSCID());
			ps.setInt(3, bbie_scVO.getMinCardinality());
			ps.setInt(4, bbie_scVO.getMaxCardinality());
			
			if(bbie_scVO.getDTSCPrimitiveRestrictionID() == 0)
				ps.setNull(5, java.sql.Types.INTEGER);
			else
				ps.setInt(5, bbie_scVO.getDTSCPrimitiveRestrictionID());
			
			if(bbie_scVO.getBBIESCID() != -1)
				ps.setInt(6, bbie_scVO.getBBIESCID());
			
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()){
			    key = rs.getInt(1);
			}
			rs.close();
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
		return key;
	}
	
	public int insertObject(SRTObject obj, Connection conn) throws SRTDAOException {
		BBIE_SCVO bbie_scVO = (BBIE_SCVO)obj;
		PreparedStatement ps = null;
		int key = -1;
		try {
			if(bbie_scVO.getBBIESCID() == -1)
				ps = conn.prepareStatement(_INSERT_BBIE_SC_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			else
				ps = conn.prepareStatement(_INSERT_BBIE_SC_WITH_ID_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			
			
			ps.setInt(1, bbie_scVO.getBBIEID());
			ps.setInt(2, bbie_scVO.getDTSCID());
			ps.setInt(3, bbie_scVO.getMinCardinality());
			ps.setInt(4, bbie_scVO.getMaxCardinality());
			
			if(bbie_scVO.getDTSCPrimitiveRestrictionID() == 0)
				ps.setNull(5, java.sql.Types.INTEGER);
			else
				ps.setInt(5, bbie_scVO.getDTSCPrimitiveRestrictionID());
			
			if(bbie_scVO.getBBIESCID() != -1)
				ps.setInt(6, bbie_scVO.getBBIESCID());
			
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
		BBIE_SCVO bbie_scVO = new BBIE_SCVO();
		try {
			Connection conn = tx.open();
			String sql = _FIND_BBIE_SC_STATEMENT;

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
				bbie_scVO.setBBIESCID(rs.getInt("BBIE_SC_ID"));
				bbie_scVO.setBBIEID(rs.getInt("BBIE_ID"));
				bbie_scVO.setDTSCID(rs.getInt("DT_SC_ID"));
				bbie_scVO.setMinCardinality(rs.getInt("Min_Cardinality"));
				bbie_scVO.setMaxCardinality(rs.getInt("Max_Cardinality"));
				bbie_scVO.setDTSCPrimitiveRestrictionID(rs.getInt("DT_SC_Primitive_Restriction_ID"));
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
		return bbie_scVO;
	}
	
	public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		BBIE_SCVO bbie_scVO = new BBIE_SCVO();
		try {
			//Connection conn = tx.open();
			String sql = _FIND_BBIE_SC_STATEMENT;

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
				bbie_scVO.setBBIESCID(rs.getInt("BBIE_SC_ID"));
				bbie_scVO.setBBIEID(rs.getInt("BBIE_ID"));
				bbie_scVO.setDTSCID(rs.getInt("DT_SC_ID"));
				bbie_scVO.setMinCardinality(rs.getInt("Min_Cardinality"));
				bbie_scVO.setMaxCardinality(rs.getInt("Max_Cardinality"));
				bbie_scVO.setDTSCPrimitiveRestrictionID(rs.getInt("DT_SC_Primitive_Restriction_ID"));
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
		return bbie_scVO;
	}

	@Override
	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_BBIE_SC_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				BBIE_SCVO bbie_scVO = new BBIE_SCVO();
				bbie_scVO.setBBIESCID(rs.getInt("BBIE_SC_ID"));
				bbie_scVO.setBBIEID(rs.getInt("BBIE_ID"));
				bbie_scVO.setDTSCID(rs.getInt("DT_SC_ID"));
				bbie_scVO.setMinCardinality(rs.getInt("Min_Cardinality"));
				bbie_scVO.setMaxCardinality(rs.getInt("Max_Cardinality"));
				bbie_scVO.setDTSCPrimitiveRestrictionID(rs.getInt("DT_SC_Primitive_Restriction_ID"));
				list.add(bbie_scVO);
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
			String sql = _FIND_ALL_BBIE_SC_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				BBIE_SCVO bbie_scVO = new BBIE_SCVO();
				bbie_scVO.setBBIESCID(rs.getInt("BBIE_SC_ID"));
				bbie_scVO.setBBIEID(rs.getInt("BBIE_ID"));
				bbie_scVO.setDTSCID(rs.getInt("DT_SC_ID"));
				bbie_scVO.setMinCardinality(rs.getInt("Min_Cardinality"));
				bbie_scVO.setMaxCardinality(rs.getInt("Max_Cardinality"));
				bbie_scVO.setDTSCPrimitiveRestrictionID(rs.getInt("DT_SC_Primitive_Restriction_ID"));
				list.add(bbie_scVO);
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
	
	private final String _UPDATE_BBIE_SC_STATEMENT = "UPDATE " + _tableName + " SET BBIE_ID = ?, "
			+ "DT_SC_ID = ?, Min_Cardinality = ?, Max_Cardinality = ?, DT_SC_Primitive_Restriction_ID = ? "
			+ "WHERE BBIE_SC_ID = ?";

	public boolean updateObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		BBIE_SCVO bbie_scVO = (BBIE_SCVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_BBIE_SC_STATEMENT);

			ps.setInt(1, bbie_scVO.getBBIEID());
			ps.setInt(2, bbie_scVO.getDTSCID());
			ps.setInt(3, bbie_scVO.getMinCardinality());
			ps.setInt(4, bbie_scVO.getMaxCardinality());
			ps.setInt(5, bbie_scVO.getDTSCPrimitiveRestrictionID());
			ps.setInt(6, bbie_scVO.getBBIESCID());
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
		BBIE_SCVO bbie_scVO = (BBIE_SCVO)obj;
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_BBIE_SC_STATEMENT);
			ps.setInt(1, bbie_scVO.getBBIESCID());
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return null;
	}

}
