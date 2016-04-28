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
import org.oagi.srt.persistence.dto.BDTPrimitiveRestrictionVO;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/
public class BDTPrimitiveRestrictionOracleDAO extends SRTDAO {
	private final String _tableName = "bdt_pri_restri";

	private final String _FIND_ALL_BDT_Primitive_Restriction_STATEMENT = 
			"SELECT bdt_pri_restri_id, bdt_id, cdt_awd_pri_xps_type_map_id, code_list_id, is_default, agency_id_list_id FROM " + _tableName;

	private final String _FIND_BDT_Primitive_Restriction_STATEMENT = 
			"SELECT bdt_pri_restri_id, bdt_id, cdt_awd_pri_xps_type_map_id, code_list_id, is_default, agency_id_list_id FROM " + _tableName;

	private final String _INSERT_BDT_Primitive_Restriction_STATEMENT = 
			"INSERT INTO " + _tableName + " (bdt_id, cdt_awd_pri_xps_type_map_id, code_list_id, is_default, agency_id_list_id) VALUES (?, ?, ?, ?, ?)";

	private final String _UPDATE_BDT_Primitive_Restriction_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET bdt_pri_restri_id = ?, bdt_id = ?, cdt_awd_pri_xps_type_map_id = ?, code_list_id = ?, is_default = ?, agency_id_list_id = ? WHERE bdt_pri_restri_id = ?";

	private final String _DELETE_BDT_Primitive_Restriction_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE bdt_pri_restri_id = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = (BDTPrimitiveRestrictionVO) obj;
		try {
			Connection conn = tx.open();
			PreparedStatement ps = null;
			ps = conn.prepareStatement(_INSERT_BDT_Primitive_Restriction_STATEMENT);
			ps.setInt(1, bdtprimitiverestrictionVO.getBDTID());
			
			if(bdtprimitiverestrictionVO.getCDTPrimitiveExpressionTypeMapID() <1)
				ps.setNull(2, java.sql.Types.INTEGER);
			else
				ps.setInt(2, bdtprimitiverestrictionVO.getCDTPrimitiveExpressionTypeMapID());
			
			if(bdtprimitiverestrictionVO.getCodeListID() <1)
				ps.setNull(3, java.sql.Types.INTEGER);
			else
				ps.setInt(3, bdtprimitiverestrictionVO.getCodeListID());
			
			if( bdtprimitiverestrictionVO.getisDefault())				
				ps.setInt(4,1);
			else 	
				ps.setInt(4,0);
			if(bdtprimitiverestrictionVO.getAgencyIDListID() <1)
				ps.setNull(5, java.sql.Types.INTEGER);
			else 
				ps.setInt(5, bdtprimitiverestrictionVO.getAgencyIDListID());
			
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
				bdtprimitiverestrictionVO.setBDTPrimitiveRestrictionID(rs.getInt("bdt_pri_restri_id"));
				bdtprimitiverestrictionVO.setBDTID(rs.getInt("bdt_id"));
				bdtprimitiverestrictionVO.setCDTPrimitiveExpressionTypeMapID(rs.getInt("cdt_awd_pri_xps_type_map_id"));
				bdtprimitiverestrictionVO.setCodeListID(rs.getInt("code_list_id"));
				bdtprimitiverestrictionVO.setisDefault(rs.getBoolean("is_default"));
				bdtprimitiverestrictionVO.setAgencyIDListID(rs.getInt("agency_id_list_id"));
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
	
	public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = new BDTPrimitiveRestrictionVO();
		
		try {
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
				bdtprimitiverestrictionVO.setBDTPrimitiveRestrictionID(rs.getInt("bdt_pri_restri_id"));
				bdtprimitiverestrictionVO.setBDTID(rs.getInt("bdt_id"));
				bdtprimitiverestrictionVO.setCDTPrimitiveExpressionTypeMapID(rs.getInt("cdt_awd_pri_xps_type_map_id"));
				bdtprimitiverestrictionVO.setCodeListID(rs.getInt("code_list_id"));
				bdtprimitiverestrictionVO.setisDefault(rs.getBoolean("is_default"));
				bdtprimitiverestrictionVO.setAgencyIDListID(rs.getInt("agency_id_list_id"));
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
				bdtprimitiverestrictionVO.setBDTPrimitiveRestrictionID(rs.getInt("bdt_pri_restri_id"));
				bdtprimitiverestrictionVO.setBDTID(rs.getInt("bdt_id"));
				bdtprimitiverestrictionVO.setCDTPrimitiveExpressionTypeMapID(rs.getInt("cdt_awd_pri_xps_type_map_id"));
				bdtprimitiverestrictionVO.setCodeListID(rs.getInt("code_list_id"));
				bdtprimitiverestrictionVO.setisDefault(rs.getBoolean("is_default"));
				bdtprimitiverestrictionVO.setAgencyIDListID(rs.getInt("agency_id_list_id"));
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
	
	public ArrayList<SRTObject> findObjects(QueryCondition qc) throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_BDT_Primitive_Restriction_STATEMENT;
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
				BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = new BDTPrimitiveRestrictionVO();
				bdtprimitiverestrictionVO.setBDTPrimitiveRestrictionID(rs.getInt("bdt_pri_restri_id"));
				bdtprimitiverestrictionVO.setBDTID(rs.getInt("bdt_id"));
				bdtprimitiverestrictionVO.setCDTPrimitiveExpressionTypeMapID(rs.getInt("cdt_awd_pri_xps_type_map_id"));
				bdtprimitiverestrictionVO.setCodeListID(rs.getInt("code_list_id"));
				bdtprimitiverestrictionVO.setisDefault(rs.getBoolean("is_default"));
				bdtprimitiverestrictionVO.setAgencyIDListID(rs.getInt("agency_id_list_id"));
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
	
	public ArrayList<SRTObject> findObjects(Connection conn) throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = _FIND_ALL_BDT_Primitive_Restriction_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = new BDTPrimitiveRestrictionVO();
				bdtprimitiverestrictionVO.setBDTPrimitiveRestrictionID(rs.getInt("bdt_pri_restri_id"));
				bdtprimitiverestrictionVO.setBDTID(rs.getInt("bdt_id"));
				bdtprimitiverestrictionVO.setCDTPrimitiveExpressionTypeMapID(rs.getInt("cdt_awd_pri_xps_type_map_id"));
				bdtprimitiverestrictionVO.setCodeListID(rs.getInt("code_list_id"));
				bdtprimitiverestrictionVO.setisDefault(rs.getBoolean("is_default"));
				bdtprimitiverestrictionVO.setAgencyIDListID(rs.getInt("agency_id_list_id"));
				
				list.add(bdtprimitiverestrictionVO);
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
	
	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn) throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = _FIND_ALL_BDT_Primitive_Restriction_STATEMENT;
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
				BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = new BDTPrimitiveRestrictionVO();
				bdtprimitiverestrictionVO.setBDTPrimitiveRestrictionID(rs.getInt("bdt_pri_restri_id"));
				bdtprimitiverestrictionVO.setBDTID(rs.getInt("bdt_id"));
				bdtprimitiverestrictionVO.setCDTPrimitiveExpressionTypeMapID(rs.getInt("cdt_awd_pri_xps_type_map_id"));
				bdtprimitiverestrictionVO.setCodeListID(rs.getInt("code_list_id"));
				bdtprimitiverestrictionVO.setisDefault(rs.getBoolean("is_default"));
				bdtprimitiverestrictionVO.setAgencyIDListID(rs.getInt("agency_id_list_id"));
				list.add(bdtprimitiverestrictionVO);
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

	public boolean updateObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		BDTPrimitiveRestrictionVO bdtprimitiverestrictionVO = (BDTPrimitiveRestrictionVO) obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_BDT_Primitive_Restriction_STATEMENT);
			
			ps.setInt(1, bdtprimitiverestrictionVO.getBDTID());
			if(bdtprimitiverestrictionVO.getCDTPrimitiveExpressionTypeMapID()<1)
				ps.setNull(2, java.sql.Types.INTEGER);
			else
				ps.setInt(2, bdtprimitiverestrictionVO.getCDTPrimitiveExpressionTypeMapID());
			if(bdtprimitiverestrictionVO.getCodeListID()<1)
				ps.setNull(3, java.sql.Types.INTEGER);
			else
				ps.setInt(3, bdtprimitiverestrictionVO.getCodeListID());
			if( bdtprimitiverestrictionVO.getisDefault())				
				ps.setInt(4,1);
			else 	
				ps.setInt(4,0);
			if(bdtprimitiverestrictionVO.getAgencyIDListID()<1){
				ps.setNull(5, java.sql.Types.INTEGER);
			}
			else 
				ps.setInt(5, bdtprimitiverestrictionVO.getAgencyIDListID());

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
	public int insertObject(SRTObject obj, Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
