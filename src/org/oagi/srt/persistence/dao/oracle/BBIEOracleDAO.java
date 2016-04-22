package org.oagi.srt.persistence.dao.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BBIEVO;
import org.oagi.srt.persistence.dto.DTVO;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/

public class BBIEOracleDAO extends SRTDAO{
	private final String _tableName = "bbie";

	private final String _FIND_ALL_BBIE_STATEMENT = 
			"SELECT BBIE_ID, GUID, Based_BCC_ID, From_ABIE_ID, To_BBIEP_ID, bdt_pri_restri_Id, code_list_id, Cardinality_Min, Cardinality_Max, default_value, is_Nillable, Fixed_Value,  "
					+ "is_Null, Definition, Remark, Created_by, Last_updated_by, Creation_timestamp, Last_update_timestamp, Seq_Key"
					+ " FROM " + _tableName;

	private final String _FIND_BBIE_STATEMENT = 
			"SELECT BBIE_ID, GUID, Based_BCC_ID, From_ABIE_ID, To_BBIEP_ID, bdt_pri_restri_Id, code_list_id, Cardinality_Min, Cardinality_Max, default_value, is_Nillable, Fixed_Value,  "
					+ "is_Null, Definition, Remark, Created_by, Last_updated_by, Creation_timestamp, Last_update_timestamp, Seq_Key"
					+ " FROM " + _tableName;
	
	private final String _INSERT_BBIE_STATEMENT = 
			"INSERT INTO " + _tableName + " (GUID, Based_BCC_ID, From_ABIE_ID, To_BBIEP_ID, bdt_pri_restri_Id, code_list_id, Cardinality_Min, Cardinality_Max, default_value, is_Nillable, Fixed_Value,  "
					+ "is_Null, Definition, Remark, Created_by, Last_updated_by, Creation_timestamp, Last_update_timestamp, Seq_Key)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)";

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
			String keys[] = {"BBIE_ID"};
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_BBIE_STATEMENT, keys);
			if( bbieVO.getBbieGuid()==null ||  bbieVO.getBbieGuid().length()==0 ||  bbieVO.getBbieGuid().isEmpty() ||  bbieVO.getBbieGuid().equals(""))				
				ps.setString(1,"\u00A0");
			else 	
				ps.setString(1, bbieVO.getBbieGuid());

			ps.setInt(2, bbieVO.getBasedBCCID());
			ps.setInt(3, bbieVO.getAssocFromABIEID());
			ps.setInt(4, bbieVO.getAssocToBBIEPID());
			if(bbieVO.getBdtPrimitiveRestrictionId() == 0)
				ps.setNull(5, java.sql.Types.INTEGER);
			else
				ps.setInt(5, bbieVO.getBdtPrimitiveRestrictionId());
			if(bbieVO.getCodeListId() == 0)
				ps.setNull(6, java.sql.Types.INTEGER);
			else
				ps.setInt(6, bbieVO.getCodeListId());
			ps.setInt(7, bbieVO.getCardinalityMin());
			ps.setInt(8, bbieVO.getCardinalityMax());
//			if( bbieVO.getDefaultText()==null ||  bbieVO.getDefaultText().length()==0 ||  bbieVO.getDefaultText().isEmpty() ||  bbieVO.getDefaultText().equals(""))				
//				ps.setString(9,"\u00A0");
//			else 	
				ps.setString(9, bbieVO.getDefaultText());

			ps.setInt(10, bbieVO.getNillable());
//			if( bbieVO.getFixedValue()==null ||  bbieVO.getFixedValue().length()==0 ||  bbieVO.getFixedValue().isEmpty() ||  bbieVO.getFixedValue().equals(""))				
//				ps.setString(11,"\u00A0");
//			else 	
				ps.setString(11, bbieVO.getFixedValue());

			ps.setInt(12, bbieVO.getIsNull());
//			if(bbieVO.getDefinition()==null || bbieVO.getDefinition().length()==0 || bbieVO.getDefinition().isEmpty() || bbieVO.getDefinition().equals("")){
//				ps.setString(13, "\u00A0");
//			}
//			else {
				String s = StringUtils.abbreviate(bbieVO.getDefinition(), 4000);
				ps.setString(13, s);
//			}
			
//			if( bbieVO.getRemark()==null ||  bbieVO.getRemark().length()==0 ||  bbieVO.getRemark().isEmpty() ||  bbieVO.getRemark().equals(""))				
//				ps.setString(14,"\u00A0");
//			else 	
				ps.setString(14, bbieVO.getRemark());

			ps.setInt(15, bbieVO.getCreatedByUserId());
			ps.setInt(16, bbieVO.getLastUpdatedByUserId());
			ps.setDouble(17, bbieVO.getSequencing_key());
			ps.executeUpdate();
			
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()){
			    key = (int) rs.getLong(1);
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
			String[] keys = {"BBIE_ID"};
			ps = conn.prepareStatement(_INSERT_BBIE_STATEMENT, keys);
			if( bbieVO.getBbieGuid()==null ||  bbieVO.getBbieGuid().length()==0 ||  bbieVO.getBbieGuid().isEmpty() ||  bbieVO.getBbieGuid().equals(""))				
				ps.setString(1,"\u00A0");
			else 	
				ps.setString(1, bbieVO.getBbieGuid());

			ps.setInt(2, bbieVO.getBasedBCCID());
			ps.setInt(3, bbieVO.getAssocFromABIEID());
			ps.setInt(4, bbieVO.getAssocToBBIEPID());
			if(bbieVO.getBdtPrimitiveRestrictionId() == 0)
				ps.setNull(5, java.sql.Types.INTEGER);
			else
				ps.setInt(5, bbieVO.getBdtPrimitiveRestrictionId());
			if(bbieVO.getCodeListId() == 0)
				ps.setNull(6, java.sql.Types.INTEGER);
			else
				ps.setInt(6, bbieVO.getCodeListId());
			ps.setInt(7, bbieVO.getCardinalityMin());
			ps.setInt(8, bbieVO.getCardinalityMax());
//			if( bbieVO.getDefaultText()==null ||  bbieVO.getDefaultText().length()==0 ||  bbieVO.getDefaultText().isEmpty() ||  bbieVO.getDefaultText().equals(""))				
//				ps.setString(9,"\u00A0");
//			else 	
				ps.setString(9, bbieVO.getDefaultText());

			ps.setInt(10, bbieVO.getNillable());
//			if( bbieVO.getFixedValue()==null ||  bbieVO.getFixedValue().length()==0 ||  bbieVO.getFixedValue().isEmpty() ||  bbieVO.getFixedValue().equals(""))				
//				ps.setString(11,"\u00A0");
//			else 	
				ps.setString(11, bbieVO.getFixedValue());

			ps.setInt(12, bbieVO.getIsNull());
//			if(bbieVO.getDefinition()==null || bbieVO.getDefinition().length()==0 || bbieVO.getDefinition().isEmpty() || bbieVO.getDefinition().equals("")){
//				ps.setString(13, "\u00A0");
//			}
//			else {
				String s = StringUtils.abbreviate(bbieVO.getDefinition(), 4000);
				ps.setString(13, s);
//			}
			
//			if( bbieVO.getRemark()==null ||  bbieVO.getRemark().length()==0 ||  bbieVO.getRemark().isEmpty() ||  bbieVO.getRemark().equals(""))				
//				ps.setString(14,"\u00A0");
//			else 	
				ps.setString(14, bbieVO.getRemark());

			ps.setInt(15, bbieVO.getCreatedByUserId());
			ps.setInt(16, bbieVO.getLastUpdatedByUserId());
			ps.setDouble(17, bbieVO.getSequencing_key());

			ps.executeUpdate();
			
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()){
			    key = (int) rs.getLong(1);
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
				bbieVO.setBbieGuid(rs.getString("GUID"));
				bbieVO.setBasedBCCID(rs.getInt("Based_BCC_ID"));
				bbieVO.setAssocFromABIEID(rs.getInt("From_ABIE_ID"));
				bbieVO.setAssocToBBIEPID(rs.getInt("To_BBIEP_ID"));
				bbieVO.setBdtPrimitiveRestrictionId(rs.getInt("bdt_pri_restri_id"));
				bbieVO.setCodeListId(rs.getInt("Code_List_ID"));
				bbieVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				bbieVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				bbieVO.setDefaultText(rs.getString("Default_Value"));
				bbieVO.setNillable(rs.getInt("is_Nillable"));
				bbieVO.setFixedValue(rs.getString("Fixed_Value"));
				bbieVO.setIsNull(rs.getInt("is_Null"));
				bbieVO.setDefinition(rs.getString("Definition"));
				bbieVO.setRemark(rs.getString("Remark"));
				bbieVO.setCreatedByUserId(rs.getInt("Created_by"));
				bbieVO.setLastUpdatedByUserId(rs.getInt("Last_updated_by"));
				bbieVO.setSequencing_key(rs.getDouble("Seq_Key"));
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
				bbieVO.setBbieGuid(rs.getString("GUID"));
				bbieVO.setBasedBCCID(rs.getInt("Based_BCC_ID"));
				bbieVO.setAssocFromABIEID(rs.getInt("From_ABIE_ID"));
				bbieVO.setAssocToBBIEPID(rs.getInt("To_BBIEP_ID"));
				bbieVO.setBdtPrimitiveRestrictionId(rs.getInt("bdt_pri_restri_id"));
				bbieVO.setCodeListId(rs.getInt("Code_List_ID"));
				bbieVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				bbieVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				bbieVO.setDefaultText(rs.getString("Default_Value"));
				bbieVO.setNillable(rs.getInt("is_Nillable"));
				bbieVO.setFixedValue(rs.getString("Fixed_Value"));
				bbieVO.setIsNull(rs.getInt("is_Null"));
				bbieVO.setDefinition(rs.getString("Definition"));
				bbieVO.setRemark(rs.getString("Remark"));
				bbieVO.setCreatedByUserId(rs.getInt("Created_by"));
				bbieVO.setLastUpdatedByUserId(rs.getInt("Last_updated_by"));
				bbieVO.setSequencing_key(rs.getDouble("Seq_Key"));
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

	private final String _UPDATE_BBIE_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET GUID = ?, Based_BCC_ID = ?,  From_ABIE_ID = ?, To_BBIEP_ID = ?, bdt_pri_restri_id = ?, code_list_id = ?, "
			+ "Cardinality_Min = ?, Cardinality_Max = ?, default_value = ?, is_Nillable = ?, Fixed_Value = ?,"
			+ "is_null = ?, Definition = ?, remark = ?, "
			+ "last_updated_by = ?, last_update_timestamp = CURRENT_TIMESTAMP, seq_key = ? where bbie_id = ?";

	
	public boolean updateObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		BBIEVO bbieVO = (BBIEVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_BBIE_STATEMENT);
			
			if( bbieVO.getBbieGuid()==null ||  bbieVO.getBbieGuid().length()==0 ||  bbieVO.getBbieGuid().isEmpty() ||  bbieVO.getBbieGuid().equals(""))				
				ps.setString(1,"\u00A0");
			else 	
				ps.setString(1, bbieVO.getBbieGuid());

			ps.setInt(2, bbieVO.getBasedBCCID());
			ps.setInt(3, bbieVO.getAssocFromABIEID());
			ps.setInt(4, bbieVO.getAssocToBBIEPID());
			if(bbieVO.getBdtPrimitiveRestrictionId() == 0)
				ps.setNull(5, java.sql.Types.INTEGER);
			else
				ps.setInt(5, bbieVO.getBdtPrimitiveRestrictionId());
			if(bbieVO.getCodeListId() == 0)
				ps.setNull(6, java.sql.Types.INTEGER);
			else
				ps.setInt(6, bbieVO.getCodeListId());
			ps.setInt(7, bbieVO.getCardinalityMin());
			ps.setInt(8, bbieVO.getCardinalityMax());
//			if( bbieVO.getDefaultText()==null ||  bbieVO.getDefaultText().length()==0 ||  bbieVO.getDefaultText().isEmpty() ||  bbieVO.getDefaultText().equals(""))				
//				ps.setString(9,"\u00A0");
//			else 	
				ps.setString(9, bbieVO.getDefaultText());

			ps.setInt(10, bbieVO.getNillable());
//			if( bbieVO.getFixedValue()==null ||  bbieVO.getFixedValue().length()==0 ||  bbieVO.getFixedValue().isEmpty() ||  bbieVO.getFixedValue().equals(""))				
//				ps.setString(11,"\u00A0");
//			else 	
				ps.setString(11, bbieVO.getFixedValue());

			ps.setInt(12, bbieVO.getIsNull());
//			if( bbieVO.getDefinition()==null ||  bbieVO.getDefinition().length()==0 ||  bbieVO.getDefinition().isEmpty() ||  bbieVO.getDefinition().equals(""))				
//				ps.setString(13,"\u00A0");
//			else 	{
				String s = StringUtils.abbreviate(bbieVO.getDefinition(), 4000);
				ps.setString(13, s);
//			}

//			if( bbieVO.getRemark()==null ||  bbieVO.getRemark().length()==0 ||  bbieVO.getRemark().isEmpty() ||  bbieVO.getRemark().equals(""))				
//				ps.setString(14,"\u00A0");
//			else 	
				ps.setString(14, bbieVO.getRemark());

			ps.setInt(15, bbieVO.getLastUpdatedByUserId());
			ps.setDouble(16, bbieVO.getSequencing_key());
			ps.setInt(17, bbieVO.getBBIEID());
			
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
				bbieVO.setBbieGuid(rs.getString("GUID"));
				bbieVO.setBasedBCCID(rs.getInt("Based_BCC_ID"));
				bbieVO.setAssocFromABIEID(rs.getInt("From_ABIE_ID"));
				bbieVO.setAssocToBBIEPID(rs.getInt("To_BBIEP_ID"));
				bbieVO.setBdtPrimitiveRestrictionId(rs.getInt("bdt_pri_restri_id"));
				bbieVO.setCodeListId(rs.getInt("Code_List_ID"));
				bbieVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				bbieVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				bbieVO.setDefaultText(rs.getString("Default_Value"));
				bbieVO.setNillable(rs.getInt("is_Nillable"));
				bbieVO.setFixedValue(rs.getString("Fixed_Value"));
				bbieVO.setIsNull(rs.getInt("is_Null"));
				bbieVO.setDefinition(rs.getString("Definition"));
				bbieVO.setRemark(rs.getString("Remark"));
				bbieVO.setCreatedByUserId(rs.getInt("Created_by"));
				bbieVO.setLastUpdatedByUserId(rs.getInt("Last_updated_by"));
				bbieVO.setSequencing_key(rs.getDouble("Seq_Key"));
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

		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			String sql = _FIND_BBIE_STATEMENT;

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
				BBIEVO bbieVO = new BBIEVO();
				bbieVO.setBBIEID(rs.getInt("BBIE_ID"));
				bbieVO.setBbieGuid(rs.getString("GUID"));
				bbieVO.setBasedBCCID(rs.getInt("Based_BCC_ID"));
				bbieVO.setAssocFromABIEID(rs.getInt("From_ABIE_ID"));
				bbieVO.setAssocToBBIEPID(rs.getInt("To_BBIEP_ID"));
				bbieVO.setBdtPrimitiveRestrictionId(rs.getInt("bdt_pri_restri_id"));
				bbieVO.setCodeListId(rs.getInt("Code_List_ID"));
				bbieVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				bbieVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				bbieVO.setDefaultText(rs.getString("Default_Value"));
				bbieVO.setNillable(rs.getInt("is_Nillable"));
				bbieVO.setFixedValue(rs.getString("Fixed_Value"));
				bbieVO.setIsNull(rs.getInt("is_Null"));
				bbieVO.setDefinition(rs.getString("Definition"));
				bbieVO.setRemark(rs.getString("Remark"));
				bbieVO.setCreatedByUserId(rs.getInt("Created_by"));
				bbieVO.setLastUpdatedByUserId(rs.getInt("Last_updated_by"));
				bbieVO.setSequencing_key(rs.getDouble("Seq_Key"));
				list.add(bbieVO);
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
