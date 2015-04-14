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
import org.oagi.srt.persistence.dto.BDTPrimitiveRestrictionVO;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/
public class BDTPrimitiveRestrictionMysqlDAO extends SRTDAO {
	private final String _tableName = "bdt_primitive_restriction";

	private final String _FIND_ALL_BDT_Primitive_Restriction_STATEMENT = 
			"SELECT BDT_Primitive_Restriction_ID, BDT_ID, CDT_Primitive_Expression_Type_Map_ID, Code_List_ID, isDefault FROM " + _tableName;

	private final String _FIND_BDT_Primitive_Restriction_STATEMENT = 
			"SELECT BDT_Primitive_Restriction_ID, BDT_ID, CDT_Primitive_Expression_Type_Map_ID, Code_List_ID, isDefault FROM " + _tableName;

	private final String _INSERT_BDT_Primitive_Restriction_STATEMENT = 
			"INSERT INTO " + _tableName + " (BDT_ID, CDT_Primitive_Expression_Type_Map_ID, isDefault) VALUES (?, ?, ?)";

	private final String _UPDATE_BDT_Primitive_Restriction_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET BDT_Primitive_Restriction_ID = ?, BDT_ID = ?, CDT_Primitive_Expression_Type_Map_ID = ?, Code_List_ID = ?, isDefault = ? WHERE BDT_Primitive_Restriction_ID = ?";

	private final String _DELETE_BDT_Primitive_Restriction_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE BDT_Primitive_Restriction_ID = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public boolean insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = (BDTPrimitiveRestrictionVO) obj;
		try {
			Connection conn = tx.open();
			PreparedStatement ps = null;
			ps = conn.prepareStatement(_INSERT_BDT_Primitive_Restriction_STATEMENT);
			ps.setInt(1, bdtprimitiverestrictionVO.getBDTID());
			if(bdtprimitiverestrictionVO.getCDTPrimitiveExpressionTypeMapID() == 0)
				ps.setNull(2, java.sql.Types.INTEGER);
			else
				ps.setInt(2, bdtprimitiverestrictionVO.getCDTPrimitiveExpressionTypeMapID());
			
			if(bdtprimitiverestrictionVO.getCodeListID() == 0)
				ps.setNull(3, java.sql.Types.INTEGER);
			else
				ps.setInt(3, bdtprimitiverestrictionVO.getCodeListID());
			
			ps.setBoolean(3, bdtprimitiverestrictionVO.getisDefault());
			
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
		return true;
	}

	public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = new BDTPrimitiveRestrictionVO();
		
		try {
			Connection conn = tx.open();
			String sql = _FIND_BDT_Primitive_Restriction_STATEMENT;

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
				bdtprimitiverestrictionVO.setBDTPrimitiveRestrictionID(rs.getInt("BDT_Primitive_Restriction_ID"));
				bdtprimitiverestrictionVO.setBDTID(rs.getInt("BDT_ID"));
				bdtprimitiverestrictionVO.setCDTPrimitiveExpressionTypeMapID(rs.getInt("CDT_Primitive_Expression_Type_Map_ID"));
				bdtprimitiverestrictionVO.setCodeListID(rs.getInt("Code_List_ID"));
				bdtprimitiverestrictionVO.setisDefault(rs.getBoolean("isDefault"));
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
		return bdtprimitiverestrictionVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_BDT_Primitive_Restriction_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = new BDTPrimitiveRestrictionVO();
				bdtprimitiverestrictionVO.setBDTPrimitiveRestrictionID(rs.getInt("BDT_Primitive_Restriction_ID"));
				bdtprimitiverestrictionVO.setBDTID(rs.getInt("BDT_ID"));
				bdtprimitiverestrictionVO.setCDTPrimitiveExpressionTypeMapID(rs.getInt("CDT_Primitive_Expression_Type_Map_ID"));
				bdtprimitiverestrictionVO.setCodeListID(rs.getInt("Code_List_ID"));
				bdtprimitiverestrictionVO.setisDefault(rs.getBoolean("isDefault"));
				list.add(bdtprimitiverestrictionVO);
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
		BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = (BDTPrimitiveRestrictionVO) obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_BDT_Primitive_Restriction_STATEMENT);
			
			ps.setInt(1, bdtprimitiverestrictionVO.getBDTID());
			ps.setInt(2, bdtprimitiverestrictionVO.getCDTPrimitiveExpressionTypeMapID());
			ps.setInt(3, bdtprimitiverestrictionVO.getCodeListID());
			ps.setBoolean(4, bdtprimitiverestrictionVO.getisDefault());
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
		BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = (BDTPrimitiveRestrictionVO) obj;

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_BDT_Primitive_Restriction_STATEMENT);
			ps.setInt(1, bdtprimitiverestrictionVO.getBDTPrimitiveRestrictionID());
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
		// TODO Auto-generated method stub
		return null;
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

}
