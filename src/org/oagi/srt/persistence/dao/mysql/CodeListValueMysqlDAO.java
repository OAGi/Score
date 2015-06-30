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
import org.oagi.srt.persistence.dto.CodeListValueVO;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/
public class CodeListValueMysqlDAO extends SRTDAO {
	private final String _tableName = "code_list_value";

	private final String _FIND_ALL_Code_List_Value_STATEMENT = 
			"SELECT Code_List_Value_ID, Code_List_ID, Value, Name, Definition, Definition_Source, Used_Indicator, Locked_Indicator, Extension_Indicator FROM " + _tableName;

	private final String _FIND_Code_List_Value_STATEMENT = 
			"SELECT Code_List_Value_ID, Code_List_ID, Value, Name, Definition, Definition_Source, Used_Indicator, Locked_Indicator, Extension_Indicator FROM " + _tableName;

	private final String _INSERT_Code_List_Value_STATEMENT = 
			"INSERT INTO " + _tableName + " (Code_List_ID, Value, Name, Definition, Definition_Source, Used_Indicator, Locked_Indicator, Extension_Indicator) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	private final String _UPDATE_Code_List_Value_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET Code_List_ID = ?, Value = ?, Name = ?, Definition = ?, Definition_Source = ?, Used_Indicator = ?, Locked_Indicator = ?, Extension_Indicator = ? WHERE Code_List_Value_ID = ?";

	private final String _DELETE_Code_List_Value_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE Code_List_Value_ID = ?";


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
			String sql = _FIND_ALL_Code_List_Value_STATEMENT;
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
				CodeListValueVO codelistvalueVO = new CodeListValueVO();
				codelistvalueVO.setCodeListValueID(rs.getInt("Code_List_Value_ID"));
				codelistvalueVO.setOwnerCodeListID(rs.getInt("Code_List_ID"));
				codelistvalueVO.setValue(rs.getString("Value"));
				codelistvalueVO.setName(rs.getString("Name"));
				codelistvalueVO.setDefinition(rs.getString("Definition"));
				codelistvalueVO.setDefinitionSource(rs.getString("Definition_Source"));
				codelistvalueVO.setUsedIndicator(rs.getBoolean("Used_Indicator"));
				codelistvalueVO.setLockedIndicator(rs.getBoolean("Locked_Indicator"));
				codelistvalueVO.setExtensionIndicator(rs.getBoolean("extension_indicator"));
				if(codelistvalueVO.getUsedIndicator() && !codelistvalueVO.getLockedIndicator() && !codelistvalueVO.isExtensionIndicator())
					codelistvalueVO.setColor("blue");
				else if(!codelistvalueVO.getUsedIndicator() && !codelistvalueVO.getLockedIndicator() && !codelistvalueVO.isExtensionIndicator())
					codelistvalueVO.setColor("orange");
				else if((!codelistvalueVO.getUsedIndicator() && !codelistvalueVO.getLockedIndicator()) || (codelistvalueVO.getLockedIndicator()))
					codelistvalueVO.setColor("red");
				else if(codelistvalueVO.getUsedIndicator() && !codelistvalueVO.getLockedIndicator() && codelistvalueVO.isExtensionIndicator())
					codelistvalueVO.setColor("green");
				list.add(codelistvalueVO);
			}
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
	
	public boolean insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		CodeListValueVO codelistvalueVO = (CodeListValueVO) obj;
		try {
			Connection conn = tx.open();
			PreparedStatement ps = null;
			ps = conn.prepareStatement(_INSERT_Code_List_Value_STATEMENT);
			ps.setInt(1, codelistvalueVO.getOwnerCodeListID());
			ps.setString(2, codelistvalueVO.getValue());
			ps.setString(3, codelistvalueVO.getName());
			ps.setString(4, codelistvalueVO.getDefinition());
			ps.setString(5, codelistvalueVO.getDefinitionSource());
			ps.setBoolean(6, codelistvalueVO.getUsedIndicator());
			ps.setBoolean(7, codelistvalueVO.getLockedIndicator());
			ps.setBoolean(8, codelistvalueVO.isExtensionIndicator());
			

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
		return true;
	}

	public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		CodeListValueVO codelistvalueVO = new CodeListValueVO();
		
		try {
			Connection conn = tx.open();
			String sql = _FIND_Code_List_Value_STATEMENT;

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
				codelistvalueVO.setCodeListValueID(rs.getInt("Code_List_Value_ID"));
				codelistvalueVO.setOwnerCodeListID(rs.getInt("Code_List_ID"));
				codelistvalueVO.setValue(rs.getString("Value"));
				codelistvalueVO.setName(rs.getString("Name"));
				codelistvalueVO.setDefinition(rs.getString("Definition"));
				codelistvalueVO.setDefinitionSource(rs.getString("Definition_Source"));
				codelistvalueVO.setUsedIndicator(rs.getBoolean("Used_Indicator"));
				codelistvalueVO.setUsedIndicator(rs.getBoolean("Locked_Indicator"));
				codelistvalueVO.setExtensionIndicator(rs.getBoolean("extension_indicator"));
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
		return codelistvalueVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_Code_List_Value_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				CodeListValueVO codelistvalueVO = new CodeListValueVO();
				codelistvalueVO.setCodeListValueID(rs.getInt("Code_List_Value_ID"));
				codelistvalueVO.setOwnerCodeListID(rs.getInt("Code_List_ID"));
				codelistvalueVO.setValue(rs.getString("Value"));
				codelistvalueVO.setName(rs.getString("Name"));
				codelistvalueVO.setDefinition(rs.getString("Definition"));
				codelistvalueVO.setDefinitionSource(rs.getString("Definition_Source"));
				codelistvalueVO.setUsedIndicator(rs.getBoolean("Used_Indicator"));
				codelistvalueVO.setUsedIndicator(rs.getBoolean("Locked_Indicator"));
				codelistvalueVO.setExtensionIndicator(rs.getBoolean("extension_indicator"));
				list.add(codelistvalueVO);
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
		CodeListValueVO codelistvalueVO = (CodeListValueVO) obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_Code_List_Value_STATEMENT);

			ps.setInt(1, codelistvalueVO.getOwnerCodeListID());
			ps.setString(2, codelistvalueVO.getValue());
			ps.setString(3, codelistvalueVO.getName());
			ps.setString(4, codelistvalueVO.getDefinition());
			ps.setString(5, codelistvalueVO.getDefinitionSource());
			ps.setBoolean(6, codelistvalueVO.getUsedIndicator());
			ps.setBoolean(7, codelistvalueVO.getLockedIndicator());
			ps.setBoolean(8, codelistvalueVO.isExtensionIndicator());
			ps.setInt(9, codelistvalueVO.getCodeListValueID());

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
		CodeListValueVO codelistvalueVO = (CodeListValueVO) obj;

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_Code_List_Value_STATEMENT);
			ps.setInt(1, codelistvalueVO.getCodeListValueID());
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


}
