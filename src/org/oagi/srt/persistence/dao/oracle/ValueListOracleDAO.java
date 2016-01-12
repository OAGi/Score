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
import org.oagi.srt.persistence.dto.ValueListVO;

/**
 *
 * @author Nasif Sikder
 * @version 1.0
 *
 */

public class ValueListOracleDAO extends SRTDAO {
	
	private final String _tableName = "value_list";
	
	private final String _FIND_ALL_VALUE_LIST_STATEMENT = "SELECT Value_List_ID, Type, " 
	+ "Value_List_GUID, Name, List_ID, Agency_ID, Version_ID, Definition, "
	+ "Based_Code_List_ID, Extensible_Indicator, Created_By_User_ID, Creation_Timestamp, "
	+ "Last_Update_Timestamp, Definition_Source"
	+ " FROM " + _tableName;
	
	private final String _FIND_VALUE_LIST_STATEMENT = "SELECT Value_List_ID, Type, " 
	+ "Value_List_GUID, Name, List_ID, Agency_ID, Version_ID, Definition, "
	+ "Based_Code_List_ID, Extensible_Indicator, Created_By_User_ID, Creation_Timestamp, "
	+ "Last_Update_Timestamp, Definition_Source"
	+ " FROM " + _tableName;
	
	private final String _INSERT_VALUE_LIST_STATEMENT = 
			"INSERT INTO " + _tableName + " (Type, Value_List_GUID, Name, List_ID, Agency_ID,"
					+ " Version_ID, Definition, Based_Code_List_ID, Extensible_Indicator, "
					+ "Created_By_User_ID, Creation_Timestamp, Last_Update_Timestamp, "
					+ "Definition_Source) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)";
	
	private final String _UPDATE_VALUE_LIST_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET Last_Update_Timestamp = CURRENT_TIMESTAMP, Type = ?, Value_List_GUID = ?,"
			+ " List_ID = ?, Agency_ID = ?, Version_ID = ?, Definition = ?, Based_Code_List_ID = ?,"
			+ " Extensible_Indicator = ?, Created_By_User_ID = ?, "
			+ " Definition_Source = ? WHERE Value_List_ID = ?";

	private final String _DELETE_VALUE_LIST_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE Value_List_ID = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public ArrayList<SRTObject> findObjects(QueryCondition qc)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		ValueListVO value_listVO = (ValueListVO)obj;
		try {
			Connection conn = tx.open();
			PreparedStatement ps = null;
			ps = conn.prepareStatement(_INSERT_VALUE_LIST_STATEMENT);
			ps.setInt(1, value_listVO.getType());
			if( value_listVO.getValueListGUID()==null ||  value_listVO.getValueListGUID().length()==0 ||  value_listVO.getValueListGUID().isEmpty() ||  value_listVO.getValueListGUID().equals(""))				
				ps.setString(2,"\u00A0");
			else 	
				ps.setString(2, value_listVO.getValueListGUID());

			if( value_listVO.getName()==null ||  value_listVO.getName().length()==0 ||  value_listVO.getName().isEmpty() ||  value_listVO.getName().equals(""))				
				ps.setString(3,"\u00A0");
			else 	
				ps.setString(3, value_listVO.getName());

			if( value_listVO.getListID()==null ||  value_listVO.getListID().length()==0 ||  value_listVO.getListID().isEmpty() ||  value_listVO.getListID().equals(""))				
				ps.setString(4,"\u00A0");
			else 	
				ps.setString(4, value_listVO.getListID());

			if( value_listVO.getAgencyID()==null ||  value_listVO.getAgencyID().length()==0 ||  value_listVO.getAgencyID().isEmpty() ||  value_listVO.getAgencyID().equals(""))				
				ps.setString(5,"\u00A0");
			else 	
				ps.setString(5, value_listVO.getAgencyID());

			if( value_listVO.getVersionID()==null ||  value_listVO.getVersionID().length()==0 ||  value_listVO.getVersionID().isEmpty() ||  value_listVO.getVersionID().equals(""))				
				ps.setString(6,"\u00A0");
			else 	
				ps.setString(6, value_listVO.getVersionID());

			if(value_listVO.getDefinition()==null || value_listVO.getDefinition().length()==0 || value_listVO.getDefinition().isEmpty() || value_listVO.getDefinition().equals("")){
				ps.setString(7, "\u00A0");
			}
			else {
				String s = StringUtils.abbreviate(value_listVO.getDefinition(), 4000);
				ps.setString(7, s);
			}
			ps.setInt(8, value_listVO.getBasedCodeListID());
			ps.setInt(9, value_listVO.getExtensibleIndicator());
			ps.setInt(10, value_listVO.getCreatedByUserID());
			ps.setInt(11, value_listVO.getLastUpdatedByUserID());
			//ps.setTimestamp(12, value_listVO.getCreationTimestamp());
			//ps.setTimestamp(13, value_listVO.getLastUpdateTimestamp());
			if( value_listVO.getDefinitionSource()==null ||  value_listVO.getDefinitionSource().length()==0 ||  value_listVO.getDefinitionSource().isEmpty() ||  value_listVO.getDefinitionSource().equals(""))				
				ps.setString(12,"\u00A0");
			else 	
				ps.setString(12, value_listVO.getDefinitionSource());

			
			ps.executeUpdate();

			ResultSet tableKeys = ps.getGeneratedKeys();
			tableKeys.next();
			int autoGeneratedID = tableKeys.getInt(1);

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
		ValueListVO value_listVO = new ValueListVO();
		try {
			Connection conn = tx.open();
			String sql = _FIND_VALUE_LIST_STATEMENT;

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
				value_listVO.setValueListID(rs.getInt("Value_List_ID"));
				value_listVO.setType(rs.getInt("Type"));
				value_listVO.setValueListGUID(rs.getString("Value_List_GUID"));
				value_listVO.setName(rs.getString("Name"));
				value_listVO.setListID(rs.getString("List_ID"));
				value_listVO.setAgencyID(rs.getString("Agency_ID"));
				value_listVO.setVersionID(rs.getString("Version_ID"));
				value_listVO.setDefinition(rs.getString("Definition"));
				value_listVO.setBasedCodeListID(rs.getInt("Based_Code_List_ID"));
				value_listVO.setExtensibleIndicator(rs.getInt("Extensible_Indicator"));
				value_listVO.setCreatedByUserID(rs.getInt("Created_By_User_ID"));
				value_listVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By_User_ID"));
				value_listVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				value_listVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				value_listVO.setDefinitionSource(rs.getString("Definition_Source"));
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
		return value_listVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_VALUE_LIST_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ValueListVO value_listVO = new ValueListVO();
				value_listVO.setValueListID(rs.getInt("Value_List_ID"));
				value_listVO.setType(rs.getInt("Type"));
				value_listVO.setValueListGUID(rs.getString("Value_List_GUID"));
				value_listVO.setName(rs.getString("Name"));
				value_listVO.setListID(rs.getString("List_ID"));
				value_listVO.setAgencyID(rs.getString("Agency_ID"));
				value_listVO.setVersionID(rs.getString("Version_ID"));
				value_listVO.setDefinition(rs.getString("Definition"));
				value_listVO.setBasedCodeListID(rs.getInt("Based_Code_List_ID"));
				value_listVO.setExtensibleIndicator(rs.getInt("Extensible_Indicator"));
				value_listVO.setCreatedByUserID(rs.getInt("Created_By_User_ID"));
				value_listVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By_User_ID"));
				value_listVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				value_listVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				value_listVO.setDefinitionSource(rs.getString("Definition_Source"));
				list.add(value_listVO);
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
		ValueListVO value_listVO = (ValueListVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_VALUE_LIST_STATEMENT);

			ps.setInt(1, value_listVO.getType());
			if( value_listVO.getValueListGUID()==null ||  value_listVO.getValueListGUID().length()==0 ||  value_listVO.getValueListGUID().isEmpty() ||  value_listVO.getValueListGUID().equals(""))				
				ps.setString(2,"\u00A0");
			else 	
				ps.setString(2, value_listVO.getValueListGUID());

			if( value_listVO.getName()==null ||  value_listVO.getName().length()==0 ||  value_listVO.getName().isEmpty() ||  value_listVO.getName().equals(""))				
				ps.setString(3,"\u00A0");
			else 	
				ps.setString(3, value_listVO.getName());

			if( value_listVO.getListID()==null ||  value_listVO.getListID().length()==0 ||  value_listVO.getListID().isEmpty() ||  value_listVO.getListID().equals(""))				
				ps.setString(4,"\u00A0");
			else 	
				ps.setString(4, value_listVO.getListID());

			if( value_listVO.getAgencyID()==null ||  value_listVO.getAgencyID().length()==0 ||  value_listVO.getAgencyID().isEmpty() ||  value_listVO.getAgencyID().equals(""))				
				ps.setString(5,"\u00A0");
			else 	
				ps.setString(5, value_listVO.getAgencyID());

			if( value_listVO.getVersionID()==null ||  value_listVO.getVersionID().length()==0 ||  value_listVO.getVersionID().isEmpty() ||  value_listVO.getVersionID().equals(""))				
				ps.setString(6,"\u00A0");
			else 	
				ps.setString(6, value_listVO.getVersionID());

			if( value_listVO.getDefinition()==null ||  value_listVO.getDefinition().length()==0 ||  value_listVO.getDefinition().isEmpty() ||  value_listVO.getDefinition().equals(""))				
				ps.setString(7,"\u00A0");
			else 	{
				String s = StringUtils.abbreviate(value_listVO.getDefinition(), 4000);
				ps.setString(7, s);
			}
			ps.setInt(8, value_listVO.getBasedCodeListID());
			ps.setInt(9, value_listVO.getExtensibleIndicator());
			ps.setInt(10, value_listVO.getCreatedByUserID());
			ps.setInt(11, value_listVO.getLastUpdatedByUserID());
			//ps.setTimestamp(12, value_listVO.getCreationTimestamp());
			//ps.setTimestamp(13, value_listVO.getLastUpdateTimestamp());
			if( value_listVO.getDefinitionSource()==null ||  value_listVO.getDefinitionSource().length()==0 ||  value_listVO.getDefinitionSource().isEmpty() ||  value_listVO.getDefinitionSource().equals(""))				
				ps.setString(12,"\u00A0");
			else 	
				ps.setString(12, value_listVO.getDefinitionSource());

			ps.setInt(13, value_listVO.getValueListID());
			
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
		ValueListVO value_listVO = (ValueListVO)obj;
		
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_VALUE_LIST_STATEMENT);
			ps.setInt(1, value_listVO.getValueListID());
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
