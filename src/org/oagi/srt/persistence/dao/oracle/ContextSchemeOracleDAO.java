package org.oagi.srt.persistence.dao.oracle;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ContextSchemeVO;

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

public class ContextSchemeOracleDAO extends SRTDAO {
	
	private final String _tableName = "classification_ctx_scheme";

	private final String _FIND_ALL_CONTEXT_SCHEME_STATEMENT = "SELECT classification_ctx_scheme_id, guid, scheme_id, scheme_name, "
	+ "description, scheme_agency_id, scheme_version_id, ctx_category_id, created_by, last_updated_by, creation_timestamp, last_update_timestamp FROM " + _tableName;
	
	private final String _FIND_CONTEXT_SCHEME_STATEMENT = "SELECT classification_ctx_scheme_id, guid, scheme_id, scheme_name, "
			+ "description, scheme_agency_id, scheme_version_id, ctx_category_id, created_by, last_updated_by, creation_timestamp, last_update_timestamp FROM " + _tableName;
	
	private final String _INSERT_CONTEXT_SCHEME_STATEMENT = 
			"INSERT INTO " + _tableName + " (guid, scheme_id, scheme_name, "
			+ "description, scheme_agency_id, scheme_version_id, ctx_category_id, created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
	
	private final String _UPDATE_CONTEXT_SCHEME_STATEMENT =
			"UPDATE " + _tableName + " SET Last_Update_Timestamp = CURRENT_TIMESTAMP, guid = ?, scheme_id = ?, scheme_name = ?, "
				+ "description = ?, scheme_agency_id = ?, scheme_version_id = ?, ctx_category_id = ?, "
				+ "created_by = ?, last_updated_by = ? WHERE classification_ctx_scheme_id = ?";
	
	private final String _DELETE_CONTEXT_SCHEME_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE classification_ctx_scheme_id = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ArrayList<SRTObject> findObjects(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
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
						ps.setString(n + 1, (String) value);
					} else if (value instanceof Integer) {
						ps.setInt(n + 1, ((Integer) value).intValue());
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
				context_schemeVO.setContextSchemeID(rs.getInt("classification_ctx_scheme_id"));
				context_schemeVO.setSchemeGUID(rs.getString("guid"));
				context_schemeVO.setSchemeID(rs.getString("scheme_id"));
				context_schemeVO.setSchemeName(rs.getString("scheme_name"));
				context_schemeVO.setDescription(rs.getString("description"));
				context_schemeVO.setSchemeAgencyID(rs.getString("scheme_agency_id"));
				context_schemeVO.setSchemeVersion("scheme_version_id");
				context_schemeVO.setContextCategoryID(rs.getInt("ctx_category_id"));
				context_schemeVO.setCreatedByUserId(rs.getInt("created_by"));
				context_schemeVO.setLastUpdatedByUserId(rs.getInt("last_updated_by"));
				context_schemeVO.setCreationTimestamp(rs.getTimestamp("creation_timestamp"));
				context_schemeVO.setLastUpdateTimestamp(rs.getTimestamp("last_update_timestamp"));
				list.add(context_schemeVO);
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

	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;

		ContextSchemeVO context_schemeVO = (ContextSchemeVO) obj;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_CONTEXT_SCHEME_STATEMENT);
			if (context_schemeVO.getSchemeGUID() == null || context_schemeVO.getSchemeGUID().length() == 0 || context_schemeVO.getSchemeGUID().isEmpty() || context_schemeVO.getSchemeGUID().equals(""))
				ps.setString(1, "**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			else
				ps.setString(1, context_schemeVO.getSchemeGUID());

			if (context_schemeVO.getSchemeID() == null || context_schemeVO.getSchemeID().length() == 0 || context_schemeVO.getSchemeID().isEmpty() || context_schemeVO.getSchemeID().equals(""))
				ps.setString(2, "**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			else
				ps.setString(2, context_schemeVO.getSchemeID());

//			if( context_schemeVO.getSchemeName()==null ||  context_schemeVO.getSchemeName().length()==0 ||  context_schemeVO.getSchemeName().isEmpty() ||  context_schemeVO.getSchemeName().equals(""))				
//				ps.setString(3,"\u00A0");
//			else 	
				ps.setString(3, context_schemeVO.getSchemeName());

//			if( context_schemeVO.getDescription()==null ||  context_schemeVO.getDescription().length()==0 ||  context_schemeVO.getDescription().isEmpty() ||  context_schemeVO.getDescription().equals(""))				
//				ps.setString(4,"\u00A0");
//			else 	
				ps.setString(4, context_schemeVO.getDescription());

			if (context_schemeVO.getSchemeAgencyID() == null || context_schemeVO.getSchemeAgencyID().length() == 0 || context_schemeVO.getSchemeAgencyID().isEmpty() || context_schemeVO.getSchemeAgencyID().equals(""))
				ps.setString(5, "**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			else
				ps.setString(5, context_schemeVO.getSchemeAgencyID());

			if (context_schemeVO.getSchemeVersion() == null || context_schemeVO.getSchemeVersion().length() == 0 || context_schemeVO.getSchemeVersion().isEmpty() || context_schemeVO.getSchemeVersion().equals(""))
				ps.setString(6, "**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			else
				ps.setString(6, context_schemeVO.getSchemeVersion());

			ps.setInt(7, context_schemeVO.getContextCategoryID());
			ps.setInt(8, context_schemeVO.getCreatedByUserId());
			ps.setInt(9, context_schemeVO.getLastUpdatedByUserId());

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

		ContextSchemeVO context_schemeVO = new ContextSchemeVO();
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
				context_schemeVO.setContextSchemeID(rs.getInt("classification_ctx_scheme_id"));
				context_schemeVO.setSchemeGUID(rs.getString("guid"));
				context_schemeVO.setSchemeID(rs.getString("scheme_id"));
				context_schemeVO.setSchemeName(rs.getString("scheme_name"));
				context_schemeVO.setDescription(rs.getString("description"));
				context_schemeVO.setSchemeAgencyID(rs.getString("scheme_agency_id"));
				context_schemeVO.setSchemeVersion("scheme_version_id");
				context_schemeVO.setContextCategoryID(rs.getInt("ctx_category_id"));
				context_schemeVO.setCreatedByUserId(rs.getInt("created_by"));
				context_schemeVO.setLastUpdatedByUserId(rs.getInt("last_updated_by"));
				context_schemeVO.setCreationTimestamp(rs.getTimestamp("creation_timestamp"));
				context_schemeVO.setLastUpdateTimestamp(rs.getTimestamp("last_update_timestamp"));

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
		return context_schemeVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			conn = tx.open();
			String sql = _FIND_ALL_CONTEXT_SCHEME_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ContextSchemeVO context_schemeVO = new ContextSchemeVO();
				context_schemeVO.setContextSchemeID(rs.getInt("classification_ctx_scheme_id"));
				context_schemeVO.setSchemeGUID(rs.getString("guid"));
				context_schemeVO.setSchemeID(rs.getString("scheme_id"));
				context_schemeVO.setSchemeName(rs.getString("scheme_name"));
				context_schemeVO.setDescription(rs.getString("description"));
				context_schemeVO.setSchemeAgencyID(rs.getString("scheme_agency_id"));
				context_schemeVO.setSchemeVersion("scheme_version_id");
				context_schemeVO.setContextCategoryID(rs.getInt("ctx_category_id"));
				context_schemeVO.setCreatedByUserId(rs.getInt("created_by"));
				context_schemeVO.setLastUpdatedByUserId(rs.getInt("last_updated_by"));
				context_schemeVO.setCreationTimestamp(rs.getTimestamp("creation_timestamp"));
				context_schemeVO.setLastUpdateTimestamp(rs.getTimestamp("last_update_timestamp"));
				list.add(context_schemeVO);
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

		ContextSchemeVO context_schemeVO = (ContextSchemeVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_CONTEXT_SCHEME_STATEMENT);

			if (context_schemeVO.getSchemeGUID() == null || context_schemeVO.getSchemeGUID().length() == 0 || context_schemeVO.getSchemeGUID().isEmpty() || context_schemeVO.getSchemeGUID().equals(""))
				ps.setString(1, "**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			else
				ps.setString(1, context_schemeVO.getSchemeGUID());

			if (context_schemeVO.getSchemeID() == null || context_schemeVO.getSchemeID().length() == 0 || context_schemeVO.getSchemeID().isEmpty() || context_schemeVO.getSchemeID().equals(""))
				ps.setString(2, "**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			else
				ps.setString(2, context_schemeVO.getSchemeID());

//			if( context_schemeVO.getSchemeName()==null ||  context_schemeVO.getSchemeName().length()==0 ||  context_schemeVO.getSchemeName().isEmpty() ||  context_schemeVO.getSchemeName().equals(""))				
//				ps.setString(3,"\u00A0");
//			else 	
				ps.setString(3, context_schemeVO.getSchemeName());

//			if( context_schemeVO.getDescription()==null ||  context_schemeVO.getDescription().length()==0 ||  context_schemeVO.getDescription().isEmpty() ||  context_schemeVO.getDescription().equals(""))				
//				ps.setString(4,"\u00A0");
//			else 	
				ps.setString(4, context_schemeVO.getDescription());

			if (context_schemeVO.getSchemeAgencyID() == null || context_schemeVO.getSchemeAgencyID().length() == 0 || context_schemeVO.getSchemeAgencyID().isEmpty() || context_schemeVO.getSchemeAgencyID().equals(""))
				ps.setString(5, "**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			else
				ps.setString(5, context_schemeVO.getSchemeAgencyID());

			if (context_schemeVO.getSchemeVersion() == null || context_schemeVO.getSchemeVersion().length() == 0 || context_schemeVO.getSchemeVersion().isEmpty() || context_schemeVO.getSchemeVersion().equals(""))
				ps.setString(6, "**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			else
				ps.setString(6, context_schemeVO.getSchemeVersion());

			ps.setInt(7, context_schemeVO.getContextCategoryID());
			ps.setInt(8, context_schemeVO.getCreatedByUserId());
			ps.setInt(9, context_schemeVO.getLastUpdatedByUserId());
			ps.setInt(10, context_schemeVO.getContextCategoryID());
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

		ContextSchemeVO context_schemeVO = (ContextSchemeVO) obj;
		try {
			conn = tx.open();

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
			closeQuietly(ps);
			closeQuietly(conn);
			closeQuietly(tx);
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
