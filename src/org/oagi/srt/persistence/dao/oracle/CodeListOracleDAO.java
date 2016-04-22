package org.oagi.srt.persistence.dao.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.DTVO;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/

public class CodeListOracleDAO extends SRTDAO {

	private final String _tableName = "code_list";
	private final String _FIND_ALL_Code_List_STATEMENT =
			"SELECT code_list_id, guid, enum_type_guid, Name, List_ID, "
			+ "Agency_ID, Version_ID, Definition, remark, Definition_Source, Based_Code_List_ID, Extensible_Indicator, Created_By, Last_Updated_By, Creation_Timestamp, "
			+ "Last_Update_Timestamp, State FROM " + _tableName + " order by Creation_Timestamp desc";
	
	private final String _FIND_Code_List_STATEMENT =
			"SELECT code_list_id, guid, enum_type_guid, Name, List_ID, "
			+ "Agency_ID, Version_ID, Definition, remark, Definition_Source, Based_Code_List_ID, Extensible_Indicator, Created_By, Last_Updated_By, Creation_Timestamp, "
			+ "Last_Update_Timestamp, State FROM " + _tableName ;
	
	private final String _INSERT_Code_List_STATEMENT = 
			"INSERT INTO " + _tableName + " (guid, enum_type_guid, Name, List_ID, "
			+ "Agency_ID, Version_ID, Definition, remark, Definition_Source, Based_Code_List_ID, Extensible_Indicator, Created_By, Last_Updated_By, Creation_Timestamp, "
			+ "Last_Update_Timestamp, State) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)";
	
	private final String _UPDATE_Code_List_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET Last_Update_Timestamp = CURRENT_TIMESTAMP, guid = ?,"
			+ " enum_type_guid = ?, Name = ?, List_ID = ?, Agency_ID = ?, Version_ID = ?, Definition = ?, Definition_Source = ?, Based_Code_List_ID = ?, Extensible_Indicator = ?, Created_By = ?,"
			+ " Last_Updated_By = ?, State = ?, remark = ? WHERE Code_List_ID = ?";
	
	private final String _DELETE_Code_List_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE Code_List_ID = ?";
	
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
			String sql = _FIND_Code_List_STATEMENT;

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
				CodeListVO codelistVO = new CodeListVO();
				codelistVO.setCodeListID(rs.getInt("Code_List_ID"));
				codelistVO.setCodeListGUID(rs.getString("guid"));
				codelistVO.setEnumerationTypeGUID(rs.getString("enum_type_guid"));
				codelistVO.setName(rs.getString("Name"));
				codelistVO.setListID(rs.getString("List_ID"));
				codelistVO.setAgencyID(rs.getInt("Agency_ID"));
				codelistVO.setVersionID(rs.getString("Version_ID"));
				codelistVO.setDefinition(rs.getString("Definition"));
				codelistVO.setRemark(rs.getString("remark"));
				codelistVO.setDefinitionSource(rs.getString("Definition_Source"));
				codelistVO.setBasedCodeListID(rs.getInt("Based_Code_List_ID"));
				codelistVO.setExtensibleIndicator(rs.getBoolean("Extensible_Indicator"));
				codelistVO.setCreatedByUserID(rs.getInt("Created_By"));
				codelistVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
				codelistVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				codelistVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				codelistVO.setState(rs.getString("State"));
				list.add(codelistVO);
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
	
	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		CodeListVO codelistVO = (CodeListVO)obj;
		try {
			Connection conn = tx.open();
			PreparedStatement ps = null;
			ps = conn.prepareStatement(_INSERT_Code_List_STATEMENT);
			if( codelistVO.getCodeListGUID()==null ||  codelistVO.getCodeListGUID().length()==0 ||  codelistVO.getCodeListGUID().isEmpty() ||  codelistVO.getCodeListGUID().equals(""))				
				ps.setString(1,"\u00A0");
			else 	
				ps.setString(1, codelistVO.getCodeListGUID());

			if( codelistVO.getEnumerationTypeGUID()==null ||  codelistVO.getEnumerationTypeGUID().length()==0 ||  codelistVO.getEnumerationTypeGUID().isEmpty() ||  codelistVO.getEnumerationTypeGUID().equals(""))				
				ps.setString(2,"\u00A0");
			else 	
				ps.setString(2, codelistVO.getEnumerationTypeGUID());

//			if( codelistVO.getName()==null ||  codelistVO.getName().length()==0 ||  codelistVO.getName().isEmpty() ||  codelistVO.getName().equals(""))				
//				ps.setString(3,"\u00A0");
//			else 	
				ps.setString(3, codelistVO.getName());

			if( codelistVO.getListID()==null ||  codelistVO.getListID().length()==0 ||  codelistVO.getListID().isEmpty() ||  codelistVO.getListID().equals(""))				
				ps.setString(4,"\u00A0");
			else 	
				ps.setString(4, codelistVO.getListID());

			ps.setInt(5, codelistVO.getAgencyID());
			if( codelistVO.getVersionID()==null ||  codelistVO.getVersionID().length()==0 ||  codelistVO.getVersionID().isEmpty() ||  codelistVO.getVersionID().equals(""))				
				ps.setString(6,"\u00A0");
			else 	
				ps.setString(6, codelistVO.getVersionID());

//			if(codelistVO.getDefinition()==null || codelistVO.getDefinition().length()==0 || codelistVO.getDefinition().isEmpty() || codelistVO.getDefinition().equals("")){
//				ps.setString(7, "\u00A0");
//			}
//			else {
				String s = StringUtils.abbreviate(codelistVO.getDefinition(), 4000);
				ps.setString(7, s);
//			}
//			if( codelistVO.getRemark()==null ||  codelistVO.getRemark().length()==0 ||  codelistVO.getRemark().isEmpty() ||  codelistVO.getRemark().equals(""))				
//				ps.setString(8,"\u00A0");
//			else 	
				ps.setString(8, codelistVO.getRemark());

//			if( codelistVO.getDefinitionSource()==null ||  codelistVO.getDefinitionSource().length()==0 ||  codelistVO.getDefinitionSource().isEmpty() ||  codelistVO.getDefinitionSource().equals(""))				
//				ps.setString(9,"\u00A0");
//			else 	
				ps.setString(9, codelistVO.getDefinitionSource());

			if(codelistVO.getBasedCodeListID() > 0)
				ps.setInt(10, codelistVO.getBasedCodeListID());
			else
				ps.setNull(10, codelistVO.getBasedCodeListID());
			if( codelistVO.getExtensibleIndicator())				
				ps.setInt(11,1);
			else 	
				ps.setInt(11,0);

			ps.setInt(12, codelistVO.getCreatedByUserID());
			ps.setInt(13, codelistVO.getLastUpdatedByUserID());
			//ps.setTimestamp(14, codelistVO.getLastUpdateTimestamp());
			if( codelistVO.getState()==null ||  codelistVO.getState().length()==0 ||  codelistVO.getState().isEmpty() ||  codelistVO.getState().equals(""))				
				ps.setString(14,"\u00A0");
			else 	
				ps.setString(14, codelistVO.getState());


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
		CodeListVO codelistVO = new CodeListVO();
		try {
			Connection conn = tx.open();
			String sql = _FIND_Code_List_STATEMENT;

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
			
			//System.out.println("##### SQL: " + sql);

			rs = ps.executeQuery();
			if (rs.next()) {
				codelistVO.setCodeListID(rs.getInt("Code_List_ID"));
				codelistVO.setCodeListGUID(rs.getString("guid"));
				codelistVO.setEnumerationTypeGUID(rs.getString("enum_type_guid"));
				codelistVO.setName(rs.getString("Name"));
				codelistVO.setListID(rs.getString("List_ID"));
				codelistVO.setAgencyID(rs.getInt("Agency_ID"));
				codelistVO.setVersionID(rs.getString("Version_ID"));
				codelistVO.setDefinition(rs.getString("Definition"));
				codelistVO.setRemark(rs.getString("remark"));
				codelistVO.setDefinitionSource(rs.getString("Definition_Source"));
				codelistVO.setBasedCodeListID(rs.getInt("Based_Code_List_ID"));
				codelistVO.setExtensibleIndicator(rs.getBoolean("Extensible_Indicator"));
				codelistVO.setCreatedByUserID(rs.getInt("Created_By"));
				codelistVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
				codelistVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				codelistVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				codelistVO.setState(rs.getString("State"));
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
		return codelistVO;
	}
	
	public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		CodeListVO codelistVO = new CodeListVO();
		try {
			String sql = _FIND_Code_List_STATEMENT;

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
			
			//System.out.println("##### SQL: " + sql);

			rs = ps.executeQuery();
			if (rs.next()) {
				codelistVO.setCodeListID(rs.getInt("Code_List_ID"));
				codelistVO.setCodeListGUID(rs.getString("guid"));
				codelistVO.setEnumerationTypeGUID(rs.getString("enum_type_guid"));
				codelistVO.setName(rs.getString("Name"));
				codelistVO.setListID(rs.getString("List_ID"));
				codelistVO.setAgencyID(rs.getInt("Agency_ID"));
				codelistVO.setVersionID(rs.getString("Version_ID"));
				codelistVO.setDefinition(rs.getString("Definition"));
				codelistVO.setRemark(rs.getString("remark"));
				codelistVO.setDefinitionSource(rs.getString("Definition_Source"));
				codelistVO.setBasedCodeListID(rs.getInt("Based_Code_List_ID"));
				codelistVO.setExtensibleIndicator(rs.getBoolean("Extensible_Indicator"));
				codelistVO.setCreatedByUserID(rs.getInt("Created_By"));
				codelistVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
				codelistVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				codelistVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				codelistVO.setState(rs.getString("State"));
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
		return codelistVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_Code_List_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				CodeListVO codelistVO = new CodeListVO();
				codelistVO.setCodeListID(rs.getInt("Code_List_ID"));
				codelistVO.setCodeListGUID(rs.getString("guid"));
				codelistVO.setEnumerationTypeGUID(rs.getString("enum_type_guid"));
				codelistVO.setName(rs.getString("Name"));
				codelistVO.setListID(rs.getString("List_ID"));
				codelistVO.setAgencyID(rs.getInt("Agency_ID"));
				codelistVO.setVersionID(rs.getString("Version_ID"));
				codelistVO.setDefinition(rs.getString("Definition"));
				codelistVO.setRemark(rs.getString("remark"));
				codelistVO.setDefinitionSource(rs.getString("Definition_Source"));
				codelistVO.setBasedCodeListID(rs.getInt("Based_Code_List_ID"));
				codelistVO.setExtensibleIndicator(rs.getBoolean("Extensible_Indicator"));
				codelistVO.setCreatedByUserID(rs.getInt("Created_By"));
				codelistVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
				codelistVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				codelistVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				codelistVO.setState(rs.getString("State"));
				list.add(codelistVO);
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
			String sql = _FIND_ALL_Code_List_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				CodeListVO codelistVO = new CodeListVO();
				codelistVO.setCodeListID(rs.getInt("Code_List_ID"));
				codelistVO.setCodeListGUID(rs.getString("guid"));
				codelistVO.setEnumerationTypeGUID(rs.getString("enum_type_guid"));
				codelistVO.setName(rs.getString("Name"));
				codelistVO.setListID(rs.getString("List_ID"));
				codelistVO.setAgencyID(rs.getInt("Agency_ID"));
				codelistVO.setVersionID(rs.getString("Version_ID"));
				codelistVO.setDefinition(rs.getString("Definition"));
				codelistVO.setRemark(rs.getString("remark"));
				codelistVO.setDefinitionSource(rs.getString("Definition_Source"));
				codelistVO.setBasedCodeListID(rs.getInt("Based_Code_List_ID"));
				codelistVO.setExtensibleIndicator(rs.getBoolean("Extensible_Indicator"));
				codelistVO.setCreatedByUserID(rs.getInt("Created_By"));
				codelistVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
				codelistVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				codelistVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				codelistVO.setState(rs.getString("State"));
				list.add(codelistVO);
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
		CodeListVO codelistVO = (CodeListVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_Code_List_STATEMENT);
			
			if( codelistVO.getCodeListGUID()==null ||  codelistVO.getCodeListGUID().length()==0 ||  codelistVO.getCodeListGUID().isEmpty() ||  codelistVO.getCodeListGUID().equals(""))				
				ps.setString(1,"\u00A0");
			else 	
				ps.setString(1, codelistVO.getCodeListGUID());

			if( codelistVO.getEnumerationTypeGUID()==null ||  codelistVO.getEnumerationTypeGUID().length()==0 ||  codelistVO.getEnumerationTypeGUID().isEmpty() ||  codelistVO.getEnumerationTypeGUID().equals(""))				
				ps.setString(2,"\u00A0");
			else 	
				ps.setString(2, codelistVO.getEnumerationTypeGUID());

//			if( codelistVO.getName()==null ||  codelistVO.getName().length()==0 ||  codelistVO.getName().isEmpty() ||  codelistVO.getName().equals(""))				
//				ps.setString(3,"\u00A0");
//			else 	
				ps.setString(3, codelistVO.getName());

			if( codelistVO.getListID()==null ||  codelistVO.getListID().length()==0 ||  codelistVO.getListID().isEmpty() ||  codelistVO.getListID().equals(""))				
				ps.setString(4,"\u00A0");
			else 	
				ps.setString(4, codelistVO.getListID());

			ps.setInt(5, codelistVO.getAgencyID());
			if( codelistVO.getVersionID()==null ||  codelistVO.getVersionID().length()==0 ||  codelistVO.getVersionID().isEmpty() ||  codelistVO.getVersionID().equals(""))				
				ps.setString(6,"\u00A0");
			else 	
				ps.setString(6, codelistVO.getVersionID());

//			if( codelistVO.getDefinition()==null ||  codelistVO.getDefinition().length()==0 ||  codelistVO.getDefinition().isEmpty() ||  codelistVO.getDefinition().equals(""))				
//				ps.setString(7,"\u00A0");
//			else 	{
				String s = StringUtils.abbreviate(codelistVO.getDefinition(), 4000);
				ps.setString(7, s);
//			}
//			if( codelistVO.getDefinitionSource()==null ||  codelistVO.getDefinitionSource().length()==0 ||  codelistVO.getDefinitionSource().isEmpty() ||  codelistVO.getDefinitionSource().equals(""))				
//				ps.setString(8,"\u00A0");
//			else 	
				ps.setString(8, codelistVO.getDefinitionSource());
			if(codelistVO.getBasedCodeListID() < 1)
				ps.setNull(9, java.sql.Types.INTEGER);
			else
				ps.setInt(9, codelistVO.getBasedCodeListID());
			
			if( codelistVO.getExtensibleIndicator())				
				ps.setInt(10,1);
			else 	
				ps.setInt(10,0);

			ps.setInt(11, codelistVO.getCreatedByUserID());
			ps.setInt(12, codelistVO.getLastUpdatedByUserID());
			if( codelistVO.getState()==null ||  codelistVO.getState().length()==0 ||  codelistVO.getState().isEmpty() ||  codelistVO.getState().equals(""))				
				ps.setString(13,"\u00A0");
			else 	
				ps.setString(13, codelistVO.getState());
//			if( codelistVO.getRemark()==null ||  codelistVO.getRemark().length()==0 ||  codelistVO.getRemark().isEmpty() ||  codelistVO.getRemark().equals(""))				
//				ps.setString(14,"\u00A0");
//			else 	
				ps.setString(14, codelistVO.getRemark());
			ps.setInt(15, codelistVO.getCodeListID());
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
		CodeListVO codelistVO = (CodeListVO)obj;
		
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_Code_List_STATEMENT);
			ps.setInt(1, codelistVO.getCodeListID());
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
	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			String sql = _FIND_Code_List_STATEMENT;

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
				CodeListVO codelistVO = new CodeListVO();
				codelistVO.setCodeListID(rs.getInt("Code_List_ID"));
				codelistVO.setCodeListGUID(rs.getString("guid"));
				codelistVO.setEnumerationTypeGUID(rs.getString("enum_type_guid"));
				codelistVO.setName(rs.getString("Name"));
				codelistVO.setListID(rs.getString("List_ID"));
				codelistVO.setAgencyID(rs.getInt("Agency_ID"));
				codelistVO.setVersionID(rs.getString("Version_ID"));
				codelistVO.setDefinition(rs.getString("Definition"));
				codelistVO.setRemark(rs.getString("remark"));
				codelistVO.setDefinitionSource(rs.getString("Definition_Source"));
				codelistVO.setBasedCodeListID(rs.getInt("Based_Code_List_ID"));
				codelistVO.setExtensibleIndicator(rs.getBoolean("Extensible_Indicator"));
				codelistVO.setCreatedByUserID(rs.getInt("Created_By"));
				codelistVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
				codelistVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				codelistVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				codelistVO.setState(rs.getString("State"));
				list.add(codelistVO);
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
	public int insertObject(SRTObject obj, Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}
}
