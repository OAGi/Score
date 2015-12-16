package org.oagi.srt.persistence.dao.oracle;

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
import org.oagi.srt.persistence.dto.ASCCPVO;
import org.oagi.srt.persistence.dto.ContextCategoryVO;

/**
 *
 * @author Nasif Sikder
 * @version 1.0
 *
 */

public class ContextCategoryOracleDAO extends SRTDAO {

	private final String _tableName = "ctx_category";
	
	private final String _FIND_ALL_CONTEXT_CATEGORY_STATEMENT = 
			"SELECT ctx_category_id, guid, Name, Description FROM " + _tableName + " order by ctx_category_id desc";
	
	private final String _FIND_CONTEXT_CATEGORY_STATEMENT = 
			"SELECT ctx_category_id, guid, Name, Description FROM " + _tableName;
	
	private final String _INSERT_CONTEXT_CATEGORY_STATEMENT = 
			"INSERT INTO " + _tableName + " (guid, Name, Description) VALUES (?, ?, ?)";
	
	private final String _UPDATE_CONTEXT_CATEGORY_STATEMENT =
			"UPDATE " + _tableName + " SET guid = ?, Name = ?, Description = ? WHERE ctx_category_id = ?";
	
	private final String _DELETE_CONTEXT_CATEGORY_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE ctx_category_id = ?";

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
			String sql = _FIND_CONTEXT_CATEGORY_STATEMENT;

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
			
			sql += " order by name asc";
			
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
				ContextCategoryVO context_categoryVO = new ContextCategoryVO();
				context_categoryVO.setContextCategoryID(rs.getInt("ctx_category_id"));
				context_categoryVO.setContextCategoryGUID(rs.getString("guid"));
				context_categoryVO.setName(rs.getString("Name"));
				context_categoryVO.setDescription(rs.getString("Description"));	
				list.add(context_categoryVO);
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
		ContextCategoryVO context_categoryVO = (ContextCategoryVO)obj;
		try {
			Connection conn = tx.open();
			PreparedStatement ps = null;
			ps = conn.prepareStatement(_INSERT_CONTEXT_CATEGORY_STATEMENT);
			ps.setString(1, context_categoryVO.getContextCategoryGUID());
			ps.setString(2, context_categoryVO.getName());
			ps.setString(3, context_categoryVO.getDescription());
			
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
		ContextCategoryVO context_categoryVO = new ContextCategoryVO();
		try {
			Connection conn = tx.open();
			String sql = _FIND_CONTEXT_CATEGORY_STATEMENT;

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
				context_categoryVO.setContextCategoryID(rs.getInt("ctx_category_id"));
				context_categoryVO.setContextCategoryGUID("guid");
				context_categoryVO.setName(rs.getString("Name"));
				context_categoryVO.setDescription(rs.getString("Description"));				
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
		return context_categoryVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_CONTEXT_CATEGORY_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ContextCategoryVO context_categoryVO = new ContextCategoryVO();
				context_categoryVO.setContextCategoryID(rs.getInt("ctx_category_id"));
				context_categoryVO.setContextCategoryGUID(rs.getString("guid"));
				context_categoryVO.setName(rs.getString("Name"));
				context_categoryVO.setDescription(rs.getString("Description"));	
				list.add(context_categoryVO);
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
		ContextCategoryVO context_categoryVO = (ContextCategoryVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_CONTEXT_CATEGORY_STATEMENT);

			ps.setString(1, context_categoryVO.getContextCategoryGUID());
			ps.setString(2, context_categoryVO.getName());
			ps.setString(3, context_categoryVO.getDescription());
			ps.setInt(4, context_categoryVO.getContextCategoryID());
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
		ContextCategoryVO context_categoryVO = (ContextCategoryVO)obj;
		
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_CONTEXT_CATEGORY_STATEMENT);
			ps.setInt(1, context_categoryVO.getContextCategoryID());
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
