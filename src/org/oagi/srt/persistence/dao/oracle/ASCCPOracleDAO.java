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
import org.oagi.srt.persistence.dto.ASCCPVO;

/**
 *
 * @author Jaehun Lee
 * @version 1.0
 *
 */
public class ASCCPOracleDAO extends SRTDAO {

	private final String _tableName = "asccp";

	private final String _FIND_ALL_ASCCP_STATEMENT = 
			"SELECT ASCCP_ID, GUID, Property_Term, "
					+ "Definition, Role_Of_ACC_ID, Den, Created_By, owner_user_id, Last_Updated_By, "
					+ "Creation_Timestamp, Last_Update_Timestamp, State, Module, namespace_id, Reusable_Indicator, "
					+ "revision_num, revision_tracking_num, revision_action, release_id, current_asccp_id, is_deprecated FROM " + _tableName + " order by Property_Term asc";

	private final String _FIND_ASCCP_STATEMENT = 
			"SELECT ASCCP_ID, GUID, Property_Term, "
					+ "Definition, Role_Of_ACC_ID, Den, Created_By, owner_user_id, Last_Updated_By, "
					+ "Creation_Timestamp, Last_Update_Timestamp, State, Module, namespace_id, Reusable_Indicator, "
					+ "revision_num, revision_tracking_num, revision_action, release_id, current_asccp_id, is_deprecated FROM " + _tableName;
	
	private final String _INSERT_ASCCP_STATEMENT = 
			"INSERT INTO " + _tableName + " (GUID, Property_Term, "
					+ "Definition, Role_Of_ACC_ID, Den, Created_By, owner_user_id, Last_Updated_By, "
					+ "Creation_Timestamp, Last_Update_Timestamp, State, Module, namespace_id, Reusable_Indicator, "
					+ "revision_num, revision_tracking_num, revision_action, release_id, current_asccp_id, is_deprecated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private final String _UPDATE_ASCCP_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET Last_Update_Timestamp = CURRENT_TIMESTAMP, GUID = ?, Property_Term = ?, "
			+ "Definition = ?, Role_Of_ACC_ID = ?, Den = ?, Created_By = ?, owner_user_id = ?, Last_Updated_By = ?, "
			+ " State = ?, Module = ?, namespace_id = ?, Reusable_Indicator = ?,"
			+ "revision_num = ?, revision_tracking_num = ?, revision_action = ?, release_id = ?, current_asccp_id = ?, is_deprecated = ? WHERE ASCCP_ID = ?";

	private final String _DELETE_ASCCP_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE ASCCP_ID = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		ASCCPVO asccpVO = (ASCCPVO)obj;
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_ASCCP_STATEMENT);
			if( asccpVO.getASCCPGUID()==null ||  asccpVO.getASCCPGUID().length()==0 ||  asccpVO.getASCCPGUID().isEmpty() ||  asccpVO.getASCCPGUID().equals(""))				
				ps.setString(1,"\u00A0");
			else 	
				ps.setString(1, asccpVO.getASCCPGUID());

			if( asccpVO.getPropertyTerm()==null ||  asccpVO.getPropertyTerm().length()==0 ||  asccpVO.getPropertyTerm().isEmpty() ||  asccpVO.getPropertyTerm().equals(""))				
				ps.setString(2,"\u00A0");
			else 	
				ps.setString(2, asccpVO.getPropertyTerm());

			if( asccpVO.getDefinition()==null ||  asccpVO.getDefinition().length()==0 ||  asccpVO.getDefinition().isEmpty() ||  asccpVO.getDefinition().equals(""))				
				ps.setString(3,"\u00A0");
			else 	{
				String s = StringUtils.abbreviate(asccpVO.getDefinition(), 4000);
				ps.setString(3, s);
			}
			ps.setInt(4, asccpVO.getRoleOfACCID());
			if(asccpVO.getDEN()==null || asccpVO.getDEN().length()==0 || asccpVO.getDEN().isEmpty() || asccpVO.getDEN().equals("")){
				ps.setString(5, "\u00A0");
			}
			else 
				ps.setString(5, asccpVO.getDEN());
			
			ps.setInt(6, asccpVO.getCreatedByUserId());
			ps.setInt(7, asccpVO.getOwnerUserId());
			ps.setInt(8, asccpVO.getLastUpdatedByUserId());
			//ps.setTimestamp(9, asccpVO.getLastUpdateTimestamp());
			ps.setInt(9, asccpVO.getState());
			if( asccpVO.getModule()==null ||  asccpVO.getModule().length()==0 ||  asccpVO.getModule().isEmpty() ||  asccpVO.getModule().equals(""))				
				ps.setString(10,"\u00A0");
			else 	
				ps.setString(10, asccpVO.getModule());

			ps.setInt(11, asccpVO.getNamespaceId());
			if( asccpVO.getReusableIndicator())				
				ps.setInt(12,1);
			else 	
				ps.setInt(12,0);

			ps.setInt(13, asccpVO.getRevisionNum());
			ps.setInt(14, asccpVO.getRevisionTrackingNum());
			if( asccpVO.getRevisionAction())				
				ps.setInt(15,1);
			else 	
				ps.setInt(15,0);

			ps.setInt(16, asccpVO.getReleaseId());
			ps.setInt(17, asccpVO.getCurrentAsccpId());
			if( asccpVO.getIs_deprecated())				
				ps.setInt(18,1);
			else 	
				ps.setInt(18,0);

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
		return 1;
	}

	public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ASCCPVO asccpVO = null;
		Connection conn = null;
		try {
			conn = tx.open();
			String sql = _FIND_ASCCP_STATEMENT;

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
				asccpVO = new ASCCPVO();
				asccpVO.setASCCPID(rs.getInt("ASCCP_ID"));
				asccpVO.setASCCPGUID(rs.getString("GUID"));
				asccpVO.setPropertyTerm(rs.getString("Property_Term"));
				asccpVO.setDefinition(rs.getString("Definition"));
				asccpVO.setRoleOfACCID(rs.getInt("Role_Of_ACC_ID"));
				asccpVO.setDEN(rs.getString("DEN"));
				asccpVO.setCreatedByUserId(rs.getInt("Created_By"));
				asccpVO.setOwnerUserId(rs.getInt("owner_user_id"));
				asccpVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
				asccpVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asccpVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				asccpVO.setState(rs.getInt("State"));
				asccpVO.setModule(rs.getString("Module"));
				asccpVO.setNamespaceId(rs.getInt("namespace_id"));
				asccpVO.setReusableIndicator(rs.getBoolean("reusable_indicator"));
				asccpVO.setRevisionNum(rs.getInt("revision_num"));
				asccpVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
				asccpVO.setRevisionAction(rs.getBoolean("revision_action"));
				asccpVO.setReleaseId(rs.getInt("release_id"));
				asccpVO.setCurrentAsccpId(rs.getInt("current_asccp_id"));
				asccpVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
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
		return asccpVO;
	}
	
	public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ASCCPVO asccpVO = null;
		//Connection conn = null;
		try {
			//conn = tx.open();
			String sql = _FIND_ASCCP_STATEMENT;

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
				asccpVO = new ASCCPVO();
				asccpVO.setASCCPID(rs.getInt("ASCCP_ID"));
				asccpVO.setASCCPGUID(rs.getString("GUID"));
				asccpVO.setPropertyTerm(rs.getString("Property_Term"));
				asccpVO.setDefinition(rs.getString("Definition"));
				asccpVO.setRoleOfACCID(rs.getInt("Role_Of_ACC_ID"));
				asccpVO.setDEN(rs.getString("DEN"));
				asccpVO.setCreatedByUserId(rs.getInt("Created_By"));
				asccpVO.setOwnerUserId(rs.getInt("owner_user_id"));
				asccpVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
				asccpVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asccpVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				asccpVO.setState(rs.getInt("State"));
				asccpVO.setModule(rs.getString("Module"));
				asccpVO.setNamespaceId(rs.getInt("namespace_id"));
				asccpVO.setReusableIndicator(rs.getBoolean("reusable_indicator"));
				asccpVO.setRevisionNum(rs.getInt("revision_num"));
				asccpVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
				asccpVO.setRevisionAction(rs.getBoolean("revision_action"));
				asccpVO.setReleaseId(rs.getInt("release_id"));
				asccpVO.setCurrentAsccpId(rs.getInt("current_asccp_id"));
				asccpVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
			}
			//tx.commit();
			//conn.close();
		//} catch (BfPersistenceException e) {
		//	throw new SRTDAOException(SRTDAOException.DAO_FIND_ERROR, e);
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
			//try {
			//	if(conn != null && !conn.isClosed())
			//		conn.close();
			//} catch (SQLException e) {}
			tx.close();
		}
		return asccpVO;
	}
	
	public ArrayList<SRTObject> findObjects(QueryCondition qc) throws SRTDAOException {
		
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = tx.open();
			String sql = _FIND_ASCCP_STATEMENT;

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
			
			sql += " order by Property_Term asc";
			
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
				ASCCPVO asccpVO = new ASCCPVO();
				asccpVO.setASCCPID(rs.getInt("ASCCP_ID"));
				asccpVO.setASCCPGUID(rs.getString("GUID"));
				asccpVO.setPropertyTerm(rs.getString("Property_Term"));
				asccpVO.setDefinition(rs.getString("Definition"));
				asccpVO.setRoleOfACCID(rs.getInt("Role_Of_ACC_ID"));
				asccpVO.setDEN(rs.getString("DEN"));
				asccpVO.setCreatedByUserId(rs.getInt("Created_By"));
				asccpVO.setOwnerUserId(rs.getInt("owner_user_id"));
				asccpVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
				asccpVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asccpVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				asccpVO.setState(rs.getInt("State"));
				asccpVO.setModule(rs.getString("Module"));
				asccpVO.setNamespaceId(rs.getInt("namespace_id"));
				asccpVO.setReusableIndicator(rs.getBoolean("reusable_indicator"));
				asccpVO.setRevisionNum(rs.getInt("revision_num"));
				asccpVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
				asccpVO.setRevisionAction(rs.getBoolean("revision_action"));
				asccpVO.setReleaseId(rs.getInt("release_id"));
				asccpVO.setCurrentAsccpId(rs.getInt("current_asccp_id"));
				asccpVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
				list.add(asccpVO);
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
	
	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn) throws SRTDAOException {
		
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		//Connection conn = null;
		try {
			//conn = tx.open();
			String sql = _FIND_ASCCP_STATEMENT;

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
			
			sql += " order by Property_Term asc";
			
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
				ASCCPVO asccpVO = new ASCCPVO();
				asccpVO.setASCCPID(rs.getInt("ASCCP_ID"));
				asccpVO.setASCCPGUID(rs.getString("GUID"));
				asccpVO.setPropertyTerm(rs.getString("Property_Term"));
				asccpVO.setDefinition(rs.getString("Definition"));
				asccpVO.setRoleOfACCID(rs.getInt("Role_Of_ACC_ID"));
				asccpVO.setDEN(rs.getString("DEN"));
				asccpVO.setCreatedByUserId(rs.getInt("Created_By"));
				asccpVO.setOwnerUserId(rs.getInt("owner_user_id"));
				asccpVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
				asccpVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asccpVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				asccpVO.setState(rs.getInt("State"));
				asccpVO.setModule(rs.getString("Module"));
				asccpVO.setNamespaceId(rs.getInt("namespace_id"));
				asccpVO.setReusableIndicator(rs.getBoolean("reusable_indicator"));
				asccpVO.setRevisionNum(rs.getInt("revision_num"));
				asccpVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
				asccpVO.setRevisionAction(rs.getBoolean("revision_action"));
				asccpVO.setReleaseId(rs.getInt("release_id"));
				asccpVO.setCurrentAsccpId(rs.getInt("current_asccp_id"));
				asccpVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
				list.add(asccpVO);
			}
			//tx.commit();
			//conn.close();
		//} catch (BfPersistenceException e) {
		//	throw new SRTDAOException(SRTDAOException.DAO_FIND_ERROR, e);
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
			//try {
			//	if(conn != null && !conn.isClosed())
			//		conn.close();
			//} catch (SQLException e) {}
			tx.close();
		}
		return list;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_ASCCP_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ASCCPVO asccpVO = new ASCCPVO();
				asccpVO.setASCCPID(rs.getInt("ASCCP_ID"));
				asccpVO.setASCCPGUID(rs.getString("GUID"));
				asccpVO.setPropertyTerm(rs.getString("Property_Term"));
				asccpVO.setDefinition(rs.getString("Definition"));
				asccpVO.setRoleOfACCID(rs.getInt("Role_Of_ACC_ID"));
				asccpVO.setDEN(rs.getString("DEN"));
				asccpVO.setCreatedByUserId(rs.getInt("Created_By"));
				asccpVO.setOwnerUserId(rs.getInt("owner_user_id"));
				asccpVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
				asccpVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asccpVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				asccpVO.setState(rs.getInt("State"));
				asccpVO.setModule(rs.getString("Module"));
				asccpVO.setNamespaceId(rs.getInt("namespace_id"));
				asccpVO.setReusableIndicator(rs.getBoolean("reusable_indicator"));
				asccpVO.setRevisionNum(rs.getInt("revision_num"));
				asccpVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
				asccpVO.setRevisionAction(rs.getBoolean("revision_action"));
				asccpVO.setReleaseId(rs.getInt("release_id"));
				asccpVO.setCurrentAsccpId(rs.getInt("current_asccp_id"));
				asccpVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
				list.add(asccpVO);
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

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			//Connection conn = tx.open();
			String sql = _FIND_ALL_ASCCP_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ASCCPVO asccpVO = new ASCCPVO();
				asccpVO.setASCCPID(rs.getInt("ASCCP_ID"));
				asccpVO.setASCCPGUID(rs.getString("GUID"));
				asccpVO.setPropertyTerm(rs.getString("Property_Term"));
				asccpVO.setDefinition(rs.getString("Definition"));
				asccpVO.setRoleOfACCID(rs.getInt("Role_Of_ACC_ID"));
				asccpVO.setDEN(rs.getString("DEN"));
				asccpVO.setCreatedByUserId(rs.getInt("Created_By"));
				asccpVO.setOwnerUserId(rs.getInt("owner_user_id"));
				asccpVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
				asccpVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asccpVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				asccpVO.setState(rs.getInt("State"));
				asccpVO.setModule(rs.getString("Module"));
				asccpVO.setNamespaceId(rs.getInt("namespace_id"));
				asccpVO.setReusableIndicator(rs.getBoolean("reusable_indicator"));
				asccpVO.setRevisionNum(rs.getInt("revision_num"));
				asccpVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
				asccpVO.setRevisionAction(rs.getBoolean("revision_action"));
				asccpVO.setReleaseId(rs.getInt("release_id"));
				asccpVO.setCurrentAsccpId(rs.getInt("current_asccp_id"));
				asccpVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
				list.add(asccpVO);
			}
			//tx.commit();
			//conn.close();
		//} catch (BfPersistenceException e) {
		//	throw new SRTDAOException(SRTDAOException.DAO_FIND_ERROR, e);
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
		ASCCPVO asccpVO = (ASCCPVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_ASCCP_STATEMENT);

			if( asccpVO.getASCCPGUID()==null ||  asccpVO.getASCCPGUID().length()==0 ||  asccpVO.getASCCPGUID().isEmpty() ||  asccpVO.getASCCPGUID().equals(""))				
				ps.setString(1,"\u00A0");
			else 	
				ps.setString(1, asccpVO.getASCCPGUID());

			if( asccpVO.getPropertyTerm()==null ||  asccpVO.getPropertyTerm().length()==0 ||  asccpVO.getPropertyTerm().isEmpty() ||  asccpVO.getPropertyTerm().equals(""))				
				ps.setString(2,"\u00A0");
			else 	
				ps.setString(2, asccpVO.getPropertyTerm());

			if( asccpVO.getDefinition()==null ||  asccpVO.getDefinition().length()==0 ||  asccpVO.getDefinition().isEmpty() ||  asccpVO.getDefinition().equals(""))				
				ps.setString(3,"\u00A0");
			else 	{
				String s = StringUtils.abbreviate(asccpVO.getDefinition(), 4000);
				ps.setString(3, s);
			}

			ps.setInt(4, asccpVO.getRoleOfACCID());
			if( asccpVO.getDEN()==null ||  asccpVO.getDEN().length()==0 ||  asccpVO.getDEN().isEmpty() ||  asccpVO.getDEN().equals(""))				
				ps.setString(5,"\u00A0");
			else 	
				ps.setString(5, asccpVO.getDEN());

			ps.setInt(6, asccpVO.getCreatedByUserId());
			ps.setInt(7, asccpVO.getOwnerUserId());
			ps.setInt(8, asccpVO.getLastUpdatedByUserId());
			//ps.setTimestamp(9, asccpVO.getLastUpdateTimestamp());
			ps.setInt(9, asccpVO.getState());
			if( asccpVO.getModule()==null ||  asccpVO.getModule().length()==0 ||  asccpVO.getModule().isEmpty() ||  asccpVO.getModule().equals(""))				
				ps.setString(10,"\u00A0");
			else 	
				ps.setString(10, asccpVO.getModule());

			ps.setInt(11, asccpVO.getNamespaceId());
			if( asccpVO.getReusableIndicator())				
				ps.setInt(12,1);
			else 	
				ps.setInt(12,0);

			ps.setInt(13, asccpVO.getRevisionNum());
			ps.setInt(14, asccpVO.getRevisionTrackingNum());
			if( asccpVO.getRevisionAction())				
				ps.setInt(15,1);
			else 	
				ps.setInt(15,0);

			ps.setInt(16, asccpVO.getReleaseId());
			ps.setInt(17, asccpVO.getCurrentAsccpId());
			if( asccpVO.getIs_deprecated())				
				ps.setInt(18,1);
			else 	
				ps.setInt(18,0);

			ps.setInt(19, asccpVO.getASCCPID());
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
		ASCCPVO asccpVO = (ASCCPVO)obj;

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_ASCCP_STATEMENT);
			ps.setInt(1, asccpVO.getASCCPID());
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
	public int insertObject(SRTObject obj, Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
