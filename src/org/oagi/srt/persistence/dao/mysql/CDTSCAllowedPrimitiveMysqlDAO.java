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
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveVO;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/
public class CDTSCAllowedPrimitiveMysqlDAO extends SRTDAO {
	private final String _tableName = "cdt_sc_allowed_primitive";

	private final String _FIND_ALL_CDT_SC_Allowed_Primitive_STATEMENT = 
			"SELECT CDT_SC_Allowed_Primitive_ID, CDT_SC_ID, CDT_Primitive_ID, isDefault FROM " + _tableName;

	private final String _FIND_CDT_SC_Allowed_Primitive_STATEMENT = 
			"SELECT CDT_SC_Allowed_Primitive_ID, CDT_SC_ID, CDT_Primitive_ID, isDefault FROM " + _tableName;

	private final String _INSERT_CDT_SC_Allowed_Primitive_STATEMENT = 
			"INSERT INTO " + _tableName + " (CDT_SC_ID, CDT_Primitive_ID, isDefault) VALUES (?, ?, ?)";

	private final String _UPDATE_CDT_SC_Allowed_Primitive_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET CDT_SC_Allowed_Primitive_ID = ?, CDT_SC_ID = ?, CDT_Primitive_ID = ?, isDefault = ? WHERE CDT_SC_Allowed_Primitive_ID = ?";

	private final String _DELETE_CDT_SC_Allowed_Primitive_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE CDT_SC_Allowed_Primitive_ID = ?";


	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
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
			String sql = _FIND_ALL_CDT_SC_Allowed_Primitive_STATEMENT;
			
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
				CDTSCAllowedPrimitiveVO cdtscallowedprimitiveVO = new CDTSCAllowedPrimitiveVO();
				cdtscallowedprimitiveVO.setCDTSCAllowedPrimitiveID(rs.getInt("CDT_SC_Allowed_Primitive_ID"));
				cdtscallowedprimitiveVO.setCDTSCID(rs.getInt("CDT_SC_ID"));
				cdtscallowedprimitiveVO.setCDTPrimitiveID(rs.getInt("CDT_Primitive_ID"));
				cdtscallowedprimitiveVO.setisDefault(rs.getBoolean("isDefault"));
				list.add(cdtscallowedprimitiveVO);
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

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = _FIND_ALL_CDT_SC_Allowed_Primitive_STATEMENT;
			
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
				CDTSCAllowedPrimitiveVO cdtscallowedprimitiveVO = new CDTSCAllowedPrimitiveVO();
				cdtscallowedprimitiveVO.setCDTSCAllowedPrimitiveID(rs.getInt("CDT_SC_Allowed_Primitive_ID"));
				cdtscallowedprimitiveVO.setCDTSCID(rs.getInt("CDT_SC_ID"));
				cdtscallowedprimitiveVO.setCDTPrimitiveID(rs.getInt("CDT_Primitive_ID"));
				cdtscallowedprimitiveVO.setisDefault(rs.getBoolean("isDefault"));
				list.add(cdtscallowedprimitiveVO);
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
	
	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		CDTSCAllowedPrimitiveVO cdtscallowedprimitiveVO = (CDTSCAllowedPrimitiveVO) obj;
		try {
			Connection conn = tx.open();
			PreparedStatement ps = null;
			ps = conn.prepareStatement(_INSERT_CDT_SC_Allowed_Primitive_STATEMENT);
			ps.setInt(1, cdtscallowedprimitiveVO.getCDTSCID());
			ps.setInt(2, cdtscallowedprimitiveVO.getCDTPrimitiveID());
			ps.setBoolean(3, cdtscallowedprimitiveVO.getisDefault());

			ps.executeUpdate();

			//ResultSet tableKeys = ps.getGeneratedKeys();
			//tableKeys.next();
			//int autoGeneratedID = tableKeys.getInt(1);

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
			tx.close();
		}
		return 1;
	}

	public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		CDTSCAllowedPrimitiveVO cdtscallowedprimitiveVO = new CDTSCAllowedPrimitiveVO();
		
		try {
			Connection conn = tx.open();
			String sql = _FIND_CDT_SC_Allowed_Primitive_STATEMENT;

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
				cdtscallowedprimitiveVO.setCDTSCAllowedPrimitiveID(rs.getInt("CDT_SC_Allowed_Primitive_ID"));
				cdtscallowedprimitiveVO.setCDTSCID(rs.getInt("CDT_SC_ID"));
				cdtscallowedprimitiveVO.setCDTPrimitiveID(rs.getInt("CDT_Primitive_ID"));
				cdtscallowedprimitiveVO.setisDefault(rs.getBoolean("isDefault"));
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
		return cdtscallowedprimitiveVO;
	}
	
	public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		CDTSCAllowedPrimitiveVO cdtscallowedprimitiveVO = new CDTSCAllowedPrimitiveVO();
		
		try {
			String sql = _FIND_CDT_SC_Allowed_Primitive_STATEMENT;

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
				cdtscallowedprimitiveVO.setCDTSCAllowedPrimitiveID(rs.getInt("CDT_SC_Allowed_Primitive_ID"));
				cdtscallowedprimitiveVO.setCDTSCID(rs.getInt("CDT_SC_ID"));
				cdtscallowedprimitiveVO.setCDTPrimitiveID(rs.getInt("CDT_Primitive_ID"));
				cdtscallowedprimitiveVO.setisDefault(rs.getBoolean("isDefault"));
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
		return cdtscallowedprimitiveVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_CDT_SC_Allowed_Primitive_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				CDTSCAllowedPrimitiveVO cdtscallowedprimitiveVO = new CDTSCAllowedPrimitiveVO();
				cdtscallowedprimitiveVO.setCDTSCAllowedPrimitiveID(rs.getInt("CDT_SC_Allowed_Primitive_ID"));
				cdtscallowedprimitiveVO.setCDTSCID(rs.getInt("CDT_SC_ID"));
				cdtscallowedprimitiveVO.setCDTPrimitiveID(rs.getInt("CDT_Primitive_ID"));
				cdtscallowedprimitiveVO.setisDefault(rs.getBoolean("isDefault"));
				list.add(cdtscallowedprimitiveVO);
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
		CDTSCAllowedPrimitiveVO cdtscallowedprimitiveVO = (CDTSCAllowedPrimitiveVO) obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_CDT_SC_Allowed_Primitive_STATEMENT);

			ps.setInt(1, cdtscallowedprimitiveVO.getCDTSCID());
			ps.setInt(2, cdtscallowedprimitiveVO.getCDTPrimitiveID());
			ps.setBoolean(3, cdtscallowedprimitiveVO.getisDefault());
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
		CDTSCAllowedPrimitiveVO cdtscallowedprimitiveVO = (CDTSCAllowedPrimitiveVO) obj;

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_CDT_SC_Allowed_Primitive_STATEMENT);
			ps.setInt(1, cdtscallowedprimitiveVO.getCDTSCAllowedPrimitiveID());
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
	public ArrayList<SRTObject> findObjects(Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int insertObject(SRTObject obj, Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}


}
