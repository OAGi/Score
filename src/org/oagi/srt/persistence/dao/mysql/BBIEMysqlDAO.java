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
import org.oagi.srt.persistence.dto.BBIEVO;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/

public class BBIEMysqlDAO extends SRTDAO{
	private final String _tableName = "bbie";

	private final String _FIND_ALL_BBIE_STATEMENT = 
			"SELECT BBIE_ID, Based_BCC_ID, Cardinality_Min, Cardinality_Max, isNillable, Fixed_Value, Assoc_From_ABIE_ID, Assoc_To_BBIEP_ID, "
					+ "Definition, bbie_guid, bdt_Primitive_Restriction_Id, code_list_id, bbie.default, remark, created_by_user_id, last_updated_by_user_id, creation_timestamp, last_update_timestamp, sequencing_key"
					+ " FROM " + _tableName;

	private final String _FIND_BBIE_STATEMENT = 
			"SELECT BBIE_ID, Based_BCC_ID, Cardinality_Min, Cardinality_Max, isNillable, Fixed_Value, Assoc_From_ABIE_ID, Assoc_To_BBIEP_ID, "
					+ "Definition, bbie_guid, bdt_Primitive_Restriction_Id, code_list_id, bbie.default, remark, created_by_user_id, last_updated_by_user_id, creation_timestamp, last_update_timestamp, sequencing_key"
					+ " FROM " + _tableName;
	
	private final String _INSERT_BBIE_STATEMENT = 
			"INSERT INTO " + _tableName + " (Based_BCC_ID, Cardinality_Min, Cardinality_Max, isNillable, Fixed_Value, Assoc_From_ABIE_ID, Assoc_To_BBIEP_ID, "
					+ "Definition, bbie_guid, bdt_Primitive_Restriction_Id, code_list_id, bbie.default, remark, created_by_user_id, last_updated_by_user_id, creation_timestamp, last_update_timestamp, sequencing_key)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)";

	private final String _UPDATE_BBIE_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET Based_BCC_ID = ?, Cardinality_Min = ?, Cardinality_Max = ?, isNillable = ?, Fixed_Value = ?, Assoc_From_ABIE_ID = ?, "
			+ "Assoc_To_BBIEP_ID = ?, Definition = ?, bbie_guid = ?, bdt_Primitive_Restriction_Id = ?, code_list_id = ?, bbie.default = ?, remark = ?, "
			+ "last_updated_by_user_id = ?, last_update_timestamp = CURRENT_TIMESTAMP, sequencing_key = ? where bbie_id = ?";

	private final String _DELETE_BBIE_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE BBIE_ID = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		BBIEVO bbieVO = (BBIEVO)obj;
		Connection conn = null;
		PreparedStatement ps = null;
		int key = -1;
		try {
			
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_BBIE_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, bbieVO.getBasedBCCID());
			ps.setInt(2, bbieVO.getCardinalityMin());
			ps.setInt(3, bbieVO.getCardinalityMax());
			ps.setInt(4, bbieVO.getNillable());
			ps.setString(5, bbieVO.getFixedValue());
			ps.setInt(6, bbieVO.getAssocFromABIEID());
			ps.setInt(7, bbieVO.getAssocToBBIEPID());
			ps.setString(8, bbieVO.getDefinition());
			ps.setString(9, bbieVO.getBbieGuid());
			
			
			if(bbieVO.getBdtPrimitiveRestrictionId() == 0)
				ps.setNull(10, java.sql.Types.INTEGER);
			else
				ps.setInt(10, bbieVO.getBdtPrimitiveRestrictionId());
			
			if(bbieVO.getCodeListId() == 0)
				ps.setNull(11, java.sql.Types.INTEGER);
			else
				ps.setInt(11, bbieVO.getCodeListId());
			
			ps.setString(12, bbieVO.getDefaultText());
			ps.setString(13, bbieVO.getRemark());
			ps.setInt(14, bbieVO.getCreatedByUserId());
			ps.setInt(15, bbieVO.getLastUpdatedByUserId());
			
			ps.setDouble(16, bbieVO.getSequencing_number());

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
		BBIEVO bbieVO = (BBIEVO)obj;
		PreparedStatement ps = null;
		int key = -1;
		try {
			ps = conn.prepareStatement(_INSERT_BBIE_STATEMENT, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, bbieVO.getBasedBCCID());
			ps.setInt(2, bbieVO.getCardinalityMin());
			ps.setInt(3, bbieVO.getCardinalityMax());
			ps.setInt(4, bbieVO.getNillable());
			ps.setString(5, bbieVO.getFixedValue());
			ps.setInt(6, bbieVO.getAssocFromABIEID());
			ps.setInt(7, bbieVO.getAssocToBBIEPID());
			ps.setString(8, bbieVO.getDefinition());
			ps.setString(9, bbieVO.getBbieGuid());
			
			
			if(bbieVO.getBdtPrimitiveRestrictionId() == 0)
				ps.setNull(10, java.sql.Types.INTEGER);
			else
				ps.setInt(10, bbieVO.getBdtPrimitiveRestrictionId());
			
			if(bbieVO.getCodeListId() == 0)
				ps.setNull(11, java.sql.Types.INTEGER);
			else
				ps.setInt(11, bbieVO.getCodeListId());
			
			ps.setString(12, bbieVO.getDefaultText());
			ps.setString(13, bbieVO.getRemark());
			ps.setInt(14, bbieVO.getCreatedByUserId());
			ps.setInt(15, bbieVO.getLastUpdatedByUserId());
			
			ps.setDouble(16, bbieVO.getSequencing_number());

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
		BBIEVO bbieVO = new BBIEVO();
		try {
			Connection conn = tx.open();
			String sql = _FIND_BBIE_STATEMENT;

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
				bbieVO.setBBIEID(rs.getInt("BBIE_ID"));
				bbieVO.setBasedBCCID(rs.getInt("Based_BCC_ID"));
				bbieVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				bbieVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				bbieVO.setNillable(rs.getInt("isNillable"));
				bbieVO.setFixedValue(rs.getString("Fixed_Value"));
				bbieVO.setAssocFromABIEID(rs.getInt("Assoc_From_ABIE_ID"));
				bbieVO.setAssocToBBIEPID(rs.getInt("Assoc_To_BBIEP_ID"));
				bbieVO.setDefinition(rs.getString("Definition"));
				bbieVO.setBbieGuid(rs.getString("bbie_guid"));
				bbieVO.setBdtPrimitiveRestrictionId(rs.getInt("Bdt_Primitive_Restriction_Id"));
				bbieVO.setCodeListId(rs.getInt("code_list_id"));
				bbieVO.setDefaultText(rs.getString("default"));
				bbieVO.setRemark(rs.getString("remark"));
				bbieVO.setCreatedByUserId(rs.getInt("created_by_user_id"));
				bbieVO.setLastUpdatedByUserId(rs.getInt("last_updated_by_user_id"));
				bbieVO.setSequencing_number(rs.getDouble("sequencing_number"));
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
		return bbieVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_BBIE_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				BBIEVO bbieVO = new BBIEVO();
				bbieVO.setBBIEID(rs.getInt("BBIE_ID"));
				bbieVO.setBasedBCCID(rs.getInt("Based_BCC_ID"));
				bbieVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				bbieVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				bbieVO.setNillable(rs.getInt("isNillable"));
				bbieVO.setFixedValue(rs.getString("Fixed_Value"));
				bbieVO.setAssocFromABIEID(rs.getInt("Assoc_From_ABIE_ID"));
				bbieVO.setAssocToBBIEPID(rs.getInt("Assoc_To_BBIEP_ID"));
				bbieVO.setDefinition(rs.getString("Definition"));
				bbieVO.setBbieGuid(rs.getString("bbie_guid"));
				bbieVO.setBdtPrimitiveRestrictionId(rs.getInt("Bdt_Primitive_Restriction_Id"));
				bbieVO.setCodeListId(rs.getInt("code_list_id"));
				bbieVO.setDefaultText(rs.getString("default"));
				bbieVO.setRemark(rs.getString("remark"));
				bbieVO.setCreatedByUserId(rs.getInt("created_by_user_id"));
				bbieVO.setLastUpdatedByUserId(rs.getInt("last_updated_by_user_id"));
				bbieVO.setSequencing_number(rs.getDouble("sequencing_number"));
				list.add(bbieVO);
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
		BBIEVO bbieVO = (BBIEVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_BBIE_STATEMENT);
			
			ps.setInt(1, bbieVO.getBasedBCCID());
			ps.setInt(2, bbieVO.getCardinalityMin());
			ps.setInt(3, bbieVO.getCardinalityMax());
			ps.setInt(4, bbieVO.getNillable());
			ps.setString(5, bbieVO.getFixedValue());
			ps.setInt(6, bbieVO.getAssocFromABIEID());
			ps.setInt(7, bbieVO.getAssocToBBIEPID());
			ps.setString(8, bbieVO.getDefinition());
			ps.setString(9, bbieVO.getBbieGuid());
			ps.setInt(10, bbieVO.getBdtPrimitiveRestrictionId());
			ps.setInt(11, bbieVO.getCodeListId());
			ps.setString(12, bbieVO.getDefaultText());
			ps.setString(13, bbieVO.getRemark());
			ps.setInt(14, bbieVO.getCreatedByUserId());
			ps.setInt(15, bbieVO.getLastUpdatedByUserId());
			ps.setDouble(16, bbieVO.getSequencing_number());
			ps.setInt(17,  bbieVO.getBBIEID());
			
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
		BBIEVO bbieVO = (BBIEVO)obj;

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_BBIE_STATEMENT);
			ps.setInt(1, bbieVO.getBBIEID());
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
			String sql = _FIND_BBIE_STATEMENT;

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
				BBIEVO bbieVO = new BBIEVO();
				bbieVO.setBBIEID(rs.getInt("BBIE_ID"));
				bbieVO.setBasedBCCID(rs.getInt("Based_BCC_ID"));
				bbieVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				bbieVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				bbieVO.setNillable(rs.getInt("isNillable"));
				bbieVO.setFixedValue(rs.getString("Fixed_Value"));
				bbieVO.setAssocFromABIEID(rs.getInt("Assoc_From_ABIE_ID"));
				bbieVO.setAssocToBBIEPID(rs.getInt("Assoc_To_BBIEP_ID"));
				bbieVO.setDefinition(rs.getString("Definition"));
				bbieVO.setBbieGuid(rs.getString("bbie_guid"));
				bbieVO.setBdtPrimitiveRestrictionId(rs.getInt("Bdt_Primitive_Restriction_Id"));
				bbieVO.setCodeListId(rs.getInt("code_list_id"));
				bbieVO.setDefaultText(rs.getString("default"));
				bbieVO.setRemark(rs.getString("remark"));
				bbieVO.setCreatedByUserId(rs.getInt("created_by_user_id"));
				bbieVO.setLastUpdatedByUserId(rs.getInt("last_updated_by_user_id"));
				bbieVO.setSequencing_number(rs.getDouble("sequencing_number"));
				list.add(bbieVO);
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
