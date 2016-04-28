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
import org.oagi.srt.persistence.dto.ASBIEPVO;
import org.oagi.srt.persistence.dto.DTVO;

/**
 *
 * @author Nasif Sikder
 * @version 1.0
 *
 */

public class ASBIEPOracleDAO extends SRTDAO {
	private final String _tableName = "asbiep";

	private final String _FIND_ALL_ASBIEP_STATEMENT = 
			"SELECT ASBIEP_ID, GUID, Based_ASCCP_ID, Role_Of_ABIE_ID, Definition, Remark, biz_term, Created_By,"
			+ " Last_Updated_By, Creation_Timestamp, Last_Update_Timestamp from " + _tableName;
	
	private final String _FIND_MAX_ID_STATEMENT =
			"SELECT max(ASBIEP_ID) as max FROM " + _tableName;
	
	private final String _FIND_ASBIEP_STATEMENT = 
			"SELECT ASBIEP_ID, GUID, Based_ASCCP_ID, Role_Of_ABIE_ID, Definition, Remark, biz_term, Created_By,"
			+ " Last_Updated_By, Creation_Timestamp, Last_Update_Timestamp from " + _tableName;
	
	private final String _INSERT_ASBIEP_STATEMENT = "INSERT INTO " + _tableName + " "
			+ "(GUID, Based_ASCCP_ID, Role_Of_ABIE_ID, Definition, Remark, biz_term, Created_By,"
			+ " Last_Updated_By, Creation_Timestamp, Last_Update_Timestamp) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
	
	private final String _INSERT_ASBIEP_WITH_ID_STATEMENT = "INSERT INTO " + _tableName + " "
			+ "(GUID, Based_ASCCP_ID, Role_Of_ABIE_ID, Definition, Remark, biz_term, Created_By,"
			+ " Last_Updated_By, Creation_Timestamp, Last_Update_Timestamp, ASBIEP_ID) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)";
	
	private final String _DELETE_ASBIEP_STATEMENT = "DELETE FROM " + _tableName + " WHERE ASBIEP_ID = ?";

	public int findMaxId() throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		int max = 1;
		try {
			Connection conn = tx.open();
			String sql = _FIND_MAX_ID_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs.next())
				max = rs.getInt("max");
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
		return max;
	}
	
	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		ASBIEPVO asbiepVO = (ASBIEPVO)obj;
		Connection conn = null;
		PreparedStatement ps = null;
		int key = -1;
		try {
			conn = tx.open();
			String keys[] = {"ASBIEP_ID"};
			if(asbiepVO.getASBIEPID() < 1)
				ps = conn.prepareStatement(_INSERT_ASBIEP_STATEMENT, keys);
			else
				ps = conn.prepareStatement(_INSERT_ASBIEP_WITH_ID_STATEMENT, keys);
			
			if( asbiepVO.getASBIEPGUID()==null ||  asbiepVO.getASBIEPGUID().length()==0 ||  asbiepVO.getASBIEPGUID().isEmpty() ||  asbiepVO.getASBIEPGUID().equals(""))				
				ps.setString(1,"**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			else 	
				ps.setString(1, asbiepVO.getASBIEPGUID());

			ps.setInt(2, asbiepVO.getBasedASCCPID());
			ps.setInt(3, asbiepVO.getRoleOfABIEID());
//			if(asbiepVO.getDefinition()==null || asbiepVO.getDefinition().length()==0 || asbiepVO.getDefinition().isEmpty() || asbiepVO.getDefinition().equals("")){
//				ps.setString(4, "\u00A0");
//			}
//			else {
				String s = StringUtils.abbreviate(asbiepVO.getDefinition(), 4000);
				ps.setString(4, s);
//			}
//			if( asbiepVO.getRemark()==null ||  asbiepVO.getRemark().length()==0 ||  asbiepVO.getRemark().isEmpty() ||  asbiepVO.getRemark().equals(""))				
//				ps.setString(5,"\u00A0");
//			else 	
				ps.setString(5, asbiepVO.getRemark());

//			if( asbiepVO.getBusinessTerm()==null ||  asbiepVO.getBusinessTerm().length()==0 ||  asbiepVO.getBusinessTerm().isEmpty() ||  asbiepVO.getBusinessTerm().equals(""))				
//				ps.setString(6,"\u00A0");
//			else 	
				ps.setString(6, asbiepVO.getBusinessTerm());

			ps.setInt(7, asbiepVO.getCreatedByUserID());
			ps.setInt(8, asbiepVO.getLastUpdatedByUserID());
			if(asbiepVO.getASBIEPID() > 0)
				ps.setInt(9, asbiepVO.getASBIEPID());

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
		ASBIEPVO asbiepVO = (ASBIEPVO)obj;
		PreparedStatement ps = null;
		int key = -1;
		try {
			String keys[] = {"ASBIEP_ID"};
			if(asbiepVO.getASBIEPID() < 1)
				ps = conn.prepareStatement(_INSERT_ASBIEP_STATEMENT, keys);
			else
				ps = conn.prepareStatement(_INSERT_ASBIEP_WITH_ID_STATEMENT, keys);
			
			if( asbiepVO.getASBIEPGUID()==null ||  asbiepVO.getASBIEPGUID().length()==0 ||  asbiepVO.getASBIEPGUID().isEmpty() ||  asbiepVO.getASBIEPGUID().equals(""))				
				ps.setString(1,"**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			else 	
				ps.setString(1, asbiepVO.getASBIEPGUID());

			ps.setInt(2, asbiepVO.getBasedASCCPID());
			ps.setInt(3, asbiepVO.getRoleOfABIEID());
//			if(asbiepVO.getDefinition()==null || asbiepVO.getDefinition().length()==0 || asbiepVO.getDefinition().isEmpty() || asbiepVO.getDefinition().equals("")){
//				ps.setString(4, "\u00A0");
//			}
//			else {
				String s = StringUtils.abbreviate(asbiepVO.getDefinition(), 4000);
				ps.setString(4, s);
//			}
//			if( asbiepVO.getRemark()==null ||  asbiepVO.getRemark().length()==0 ||  asbiepVO.getRemark().isEmpty() ||  asbiepVO.getRemark().equals(""))				
//				ps.setString(5,"\u00A0");
//			else 	
				ps.setString(5, asbiepVO.getRemark());

//			if( asbiepVO.getBusinessTerm()==null ||  asbiepVO.getBusinessTerm().length()==0 ||  asbiepVO.getBusinessTerm().isEmpty() ||  asbiepVO.getBusinessTerm().equals(""))				
//				ps.setString(6,"\u00A0");
//			else 	
				ps.setString(6, asbiepVO.getBusinessTerm());

			ps.setInt(7, asbiepVO.getCreatedByUserID());
			ps.setInt(8, asbiepVO.getLastUpdatedByUserID());
			if(asbiepVO.getASBIEPID() > 0)
				ps.setInt(9, asbiepVO.getASBIEPID());

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
		ASBIEPVO asbiepVO = new ASBIEPVO();
		try {
			Connection conn = tx.open();
			String sql = _FIND_ASBIEP_STATEMENT;

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
				asbiepVO.setASBIEPID(rs.getInt("ASBIEP_ID"));
				asbiepVO.setASBIEPGUID(rs.getString("GUID"));
				asbiepVO.setBasedASCCPID(rs.getInt("Based_ASCCP_ID"));
				asbiepVO.setRoleOfABIEID(rs.getInt("Role_Of_ABIE_ID"));
				asbiepVO.setDefinition(rs.getString("Definition"));
				asbiepVO.setRemark(rs.getString("Remark"));
				asbiepVO.setBusinessTerm(rs.getString("biz_term"));
				asbiepVO.setCreatedByUserID(rs.getInt("Created_By"));
				asbiepVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
				asbiepVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asbiepVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
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
		return asbiepVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_ASBIEP_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ASBIEPVO asbiepVO = new ASBIEPVO();
				asbiepVO.setASBIEPID(rs.getInt("ASBIEP_ID"));
				asbiepVO.setASBIEPGUID(rs.getString("GUID"));
				asbiepVO.setBasedASCCPID(rs.getInt("Based_ASCCP_ID"));
				asbiepVO.setRoleOfABIEID(rs.getInt("Role_Of_ABIE_ID"));
				asbiepVO.setDefinition(rs.getString("Definition"));
				asbiepVO.setRemark(rs.getString("Remark"));
				asbiepVO.setBusinessTerm(rs.getString("biz_term"));
				asbiepVO.setCreatedByUserID(rs.getInt("Created_By"));
				asbiepVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
				asbiepVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asbiepVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				list.add(asbiepVO);
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

	private final String _UPDATE_ASBIEP_STATEMENT = "UPDATE " + _tableName + 
			" SET GUID = ?, Based_ASCCP_ID = ?, Role_Of_ABIE_ID = ?, Definition = ?, Remark = ?, biz_term = ?, Created_By = ?, Last_Updated_By = ?, Last_Update_Timestamp = CURRENT_TIMESTAMP "
			+ "WHERE ASBIEP_ID = ?";
	
	@Override
	public boolean updateObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		ASBIEPVO asbiepVO = (ASBIEPVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_ASBIEP_STATEMENT);

			if( asbiepVO.getASBIEPGUID()==null ||  asbiepVO.getASBIEPGUID().length()==0 ||  asbiepVO.getASBIEPGUID().isEmpty() ||  asbiepVO.getASBIEPGUID().equals(""))				
				ps.setString(1,"**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			else 	
				ps.setString(1, asbiepVO.getASBIEPGUID());

			ps.setInt(2, asbiepVO.getBasedASCCPID());
			ps.setInt(3, asbiepVO.getRoleOfABIEID());
//			if( asbiepVO.getDefinition()==null ||  asbiepVO.getDefinition().length()==0 ||  asbiepVO.getDefinition().isEmpty() ||  asbiepVO.getDefinition().equals(""))				
//				ps.setString(4,"\u00A0");
//			else 	{
				String s = StringUtils.abbreviate(asbiepVO.getDefinition(), 4000);
				ps.setString(4, s);
//			}
//			if( asbiepVO.getRemark()==null ||  asbiepVO.getRemark().length()==0 ||  asbiepVO.getRemark().isEmpty() ||  asbiepVO.getRemark().equals(""))				
//				ps.setString(5,"\u00A0");
//			else 	
				ps.setString(5, asbiepVO.getRemark());

//			if( asbiepVO.getBusinessTerm()==null ||  asbiepVO.getBusinessTerm().length()==0 ||  asbiepVO.getBusinessTerm().isEmpty() ||  asbiepVO.getBusinessTerm().equals(""))				
//				ps.setString(6,"\u00A0");
//			else 	
				ps.setString(6, asbiepVO.getBusinessTerm());

			ps.setInt(7, asbiepVO.getCreatedByUserID());
			ps.setInt(8, asbiepVO.getLastUpdatedByUserID());
			//ps.setTimestamp(9, asbiepVO.getCreationTimestamp());
			ps.setInt(9, asbiepVO.getASBIEPID());
			
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

	@Override
	public boolean deleteObject(SRTObject obj) throws SRTDAOException {
		ASBIEPVO asbiepVO = (ASBIEPVO)obj;
		
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_ASBIEP_STATEMENT);
			ps.setInt(1, asbiepVO.getASBIEPID());
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SRTObject findObject(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ASBIEPVO asbiepVO = null;
		
		try {
			String sql = _FIND_ASBIEP_STATEMENT;

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
			if (rs.next()) {
				asbiepVO = new ASBIEPVO();
				asbiepVO.setASBIEPID(rs.getInt("ASBIEP_ID"));
				asbiepVO.setASBIEPGUID(rs.getString("GUID"));
				asbiepVO.setBasedASCCPID(rs.getInt("Based_ASCCP_ID"));
				asbiepVO.setRoleOfABIEID(rs.getInt("Role_Of_ABIE_ID"));
				asbiepVO.setDefinition(rs.getString("Definition"));
				asbiepVO.setRemark(rs.getString("Remark"));
				asbiepVO.setBusinessTerm(rs.getString("biz_term"));
				asbiepVO.setCreatedByUserID(rs.getInt("Created_By"));
				asbiepVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
				asbiepVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asbiepVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
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
		return asbiepVO;

	}

	@Override
	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			String sql = _FIND_ASBIEP_STATEMENT;

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
				ASBIEPVO asbiepVO = new ASBIEPVO();
				asbiepVO.setASBIEPID(rs.getInt("ASBIEP_ID"));
				asbiepVO.setASBIEPGUID(rs.getString("GUID"));
				asbiepVO.setBasedASCCPID(rs.getInt("Based_ASCCP_ID"));
				asbiepVO.setRoleOfABIEID(rs.getInt("Role_Of_ABIE_ID"));
				asbiepVO.setDefinition(rs.getString("Definition"));
				asbiepVO.setRemark(rs.getString("Remark"));
				asbiepVO.setBusinessTerm(rs.getString("biz_term"));
				asbiepVO.setCreatedByUserID(rs.getInt("Created_By"));
				asbiepVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
				asbiepVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asbiepVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				list.add(asbiepVO);
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
