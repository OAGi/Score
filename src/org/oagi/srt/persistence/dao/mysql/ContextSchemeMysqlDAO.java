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
import org.oagi.srt.persistence.dto.ContextCategoryVO;
import org.oagi.srt.persistence.dto.ContextSchemeVO;

/**
 *
 * @author Nasif Sikder
 * @version 1.0
 *
 */

public class ContextSchemeMysqlDAO extends SRTDAO {
	
	private final String _tableName = "classification_context_scheme";

	private final String _FIND_ALL_CONTEXT_SCHEME_STATEMENT = "SELECT classification_context_scheme_id, classification_Context_Scheme_GUID, Scheme_ID, Scheme_Name, "
	+ "Description, Scheme_Agency_ID, Scheme_Version_Id, Context_Category_ID, created_by_user_id, last_updated_by_user_id FROM " + _tableName;
	
	private final String _FIND_CONTEXT_SCHEME_STATEMENT = "SELECT classification_context_scheme_id, classification_Context_Scheme_GUID, Scheme_ID, Scheme_Name, "
	+ "Description, Scheme_Agency_ID, Scheme_Version_Id, Context_Category_ID, created_by_user_id, last_updated_by_user_id FROM " + _tableName;
	
	private final String _INSERT_CONTEXT_SCHEME_STATEMENT = 
			"INSERT INTO " + _tableName + " (Scheme_ID, Scheme_Name, Description, Scheme_Agency_ID, "
					+ "Scheme_Version_Id, Context_Category_ID, classification_Context_Scheme_GUID, created_by_user_id, last_updated_by_user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private final String _UPDATE_CONTEXT_SCHEME_STATEMENT =
			"UPDATE " + _tableName + " SET Scheme_Name = ?, Description = ?, Scheme_Agency_ID = ?, "
				+ "Scheme_Version_Id = ?, Context_Category_ID = ?, created_by_user_id = ?, last_updated_by_user_id = ? WHERE classification_context_scheme_id = ?";
	
	private final String _DELETE_CONTEXT_SCHEME_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE classification_context_scheme_id = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public ArrayList<SRTObject> findObjects(QueryCondition qc) throws SRTDAOException {
		
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = tx.open();
			String sql = _FIND_CONTEXT_SCHEME_STATEMENT;

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
				ContextSchemeVO context_schemeVO = new ContextSchemeVO();
				context_schemeVO.setContextSchemeID(rs.getInt("classification_context_scheme_id"));
				context_schemeVO.setSchemeGUID(rs.getString("classification_Context_Scheme_GUID"));
				context_schemeVO.setSchemeID(rs.getString("Scheme_ID"));
				context_schemeVO.setSchemeName(rs.getString("Scheme_Name"));
				context_schemeVO.setDescription(rs.getString("Description"));
				context_schemeVO.setSchemeAgencyID(rs.getString("Scheme_Agency_ID"));
				//context_schemeVO.setSchemeAgencyName(rs.getString("Scheme_Agency_Name"));
				context_schemeVO.setSchemeVersion("Scheme_Version_Id");
				context_schemeVO.setContextCategoryID(rs.getInt("Context_Category_ID"));
				context_schemeVO.setCreatedByUserId(rs.getInt("created_by_user_id"));
				context_schemeVO.setLastUpdatedByUserId(rs.getInt("last_updated_by_user_id"));
				
				list.add(context_schemeVO);
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
		return list;
	}
	
	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		ContextSchemeVO context_schemeVO = (ContextSchemeVO)obj;
		try {
			Connection conn = tx.open();
			PreparedStatement ps = null;
			ps = conn.prepareStatement(_INSERT_CONTEXT_SCHEME_STATEMENT);
			ps.setString(1, context_schemeVO.getSchemeID());
			ps.setString(2, context_schemeVO.getSchemeName());
			ps.setString(3, context_schemeVO.getDescription());
			ps.setString(4, context_schemeVO.getSchemeAgencyID());
			//ps.setString(5, context_schemeVO.getSchemeAgencyName());
			ps.setString(5, context_schemeVO.getSchemeVersion());
			ps.setInt(6, context_schemeVO.getContextCategoryID());
			ps.setString(7, context_schemeVO.getSchemeGUID());
			ps.setInt(8,  context_schemeVO.getCreatedByUserId());
			ps.setInt(9, context_schemeVO.getLastUpdatedByUserId());

			ps.executeUpdate();

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
		ContextSchemeVO context_schemeVO = new ContextSchemeVO();
		try {
			Connection conn = tx.open();
			String sql = _FIND_CONTEXT_SCHEME_STATEMENT;

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
				context_schemeVO.setContextSchemeID(rs.getInt("classification_context_scheme_id"));
				context_schemeVO.setSchemeGUID(rs.getString("classification_Context_Scheme_GUID"));
				context_schemeVO.setSchemeID(rs.getString("Scheme_ID"));
				context_schemeVO.setSchemeName(rs.getString("Scheme_Name"));
				context_schemeVO.setDescription(rs.getString("Description"));
				context_schemeVO.setSchemeAgencyID(rs.getString("Scheme_Agency_ID"));
				//context_schemeVO.setSchemeAgencyName(rs.getString("Scheme_Agency_Name"));
				context_schemeVO.setSchemeVersion("Scheme_Version_Id");
				context_schemeVO.setContextCategoryID(rs.getInt("Context_Category_ID"));
				context_schemeVO.setCreatedByUserId(rs.getInt("created_by_user_id"));
				context_schemeVO.setLastUpdatedByUserId(rs.getInt("last_updated_by_user_id"));

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
		return context_schemeVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_CONTEXT_SCHEME_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ContextSchemeVO context_schemeVO = new ContextSchemeVO();
				context_schemeVO.setContextSchemeID(rs.getInt("classification_context_scheme_id"));
				context_schemeVO.setSchemeGUID(rs.getString("classification_Context_Scheme_GUID"));
				context_schemeVO.setSchemeID(rs.getString("Scheme_ID"));
				context_schemeVO.setSchemeName(rs.getString("Scheme_Name"));
				context_schemeVO.setDescription(rs.getString("Description"));
				context_schemeVO.setSchemeAgencyID(rs.getString("Scheme_Agency_ID"));
				//context_schemeVO.setSchemeAgencyName(rs.getString("Scheme_Agency_Name"));
				context_schemeVO.setSchemeVersion(rs.getString("Scheme_Version_Id"));
				context_schemeVO.setContextCategoryID(rs.getInt("Context_Category_ID"));
				context_schemeVO.setCreatedByUserId(rs.getInt("created_by_user_id"));
				context_schemeVO.setLastUpdatedByUserId(rs.getInt("last_updated_by_user_id"));
				list.add(context_schemeVO);
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
		ContextSchemeVO context_schemeVO = (ContextSchemeVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_CONTEXT_SCHEME_STATEMENT);

			ps.setString(1, context_schemeVO.getSchemeName());
			ps.setString(2, context_schemeVO.getDescription());
			ps.setString(3, context_schemeVO.getSchemeAgencyID());
			//ps.setString(4, context_schemeVO.getSchemeAgencyName());
			ps.setString(4, context_schemeVO.getSchemeVersion());
			ps.setInt(5, context_schemeVO.getContextCategoryID());
			ps.setInt(6,  context_schemeVO.getCreatedByUserId());
			ps.setInt(7, context_schemeVO.getLastUpdatedByUserId());
			ps.setInt(8, context_schemeVO.getContextSchemeID());
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
		ContextSchemeVO context_schemeVO = (ContextSchemeVO)obj;
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_CONTEXT_SCHEME_STATEMENT);
			ps.setInt(1, context_schemeVO.getContextSchemeID());
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
	public SRTObject findObject(QueryCondition qc, Connection conn)
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
