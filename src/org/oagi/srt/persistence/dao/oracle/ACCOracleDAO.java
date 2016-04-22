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
import org.oagi.srt.persistence.dto.ACCVO;

/**
 *
 * @author Jaehun Lee
 * @version 1.0
 *
 */
public class ACCOracleDAO extends SRTDAO {

	private final String _tableName = "acc";

	private final String _FIND_ALL_ACC_STATEMENT = 
			"SELECT ACC_ID, GUID, Object_Class_Term, "
					+ "Den, Definition, Based_ACC_ID, Object_Class_Qualifier, OAGIS_Component_Type, Module, namespace_id, Created_By, owner_user_id, Last_Updated_By, "
					+ "Creation_Timestamp, Last_Update_Timestamp, State, revision_num, revision_tracking_num, revision_action, release_id, current_acc_id, is_deprecated FROM " + _tableName;

	private final String _FIND_ACC_STATEMENT = 
			"SELECT ACC_ID, GUID, Object_Class_Term, "
					+ "Den, Definition, Based_ACC_ID, Object_Class_Qualifier, OAGIS_Component_Type, Module, namespace_id, Created_By, owner_user_id, Last_Updated_By, "
					+ "Creation_Timestamp, Last_Update_Timestamp, State, revision_num, revision_tracking_num, revision_action, release_id, current_acc_id, is_deprecated FROM " + _tableName;
	
	private final String _INSERT_ACC_STATEMENT = 
			"INSERT INTO " + _tableName + " (GUID, Object_Class_Term, "
					+ "Den, Definition, Based_ACC_ID, Object_Class_Qualifier, OAGIS_Component_Type, Module, namespace_id, Created_By, owner_user_id, Last_Updated_By, "
					+ "Creation_Timestamp, Last_Update_Timestamp, State, revision_num, revision_tracking_num, revision_action, release_id, current_acc_id, is_deprecated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?)";

	private final String _UPDATE_ACC_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET Last_Update_Timestamp = CURRENT_TIMESTAMP, GUID = ?, Object_Class_Term = ?, "
			+ "Den = ?, Definition = ?, Based_ACC_ID = ?, Object_Class_Qualifier = ?, OAGIS_Component_Type = ?, Module = ?, namespace_id = ?, Created_By = ?, Last_Updated_By = ?, "
			+ "State =?,  revision_num = ?, revision_tracking_num = ?, revision_action = ?, release_id = ?, current_acc_id = ?, is_deprecated = ? WHERE ACC_ID = ?";

	private final String _DELETE_ACC_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE ACC_ID = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		ACCVO accVO = (ACCVO)obj;
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_ACC_STATEMENT);
			if( accVO.getACCGUID()==null ||  accVO.getACCGUID().length()==0 ||  accVO.getACCGUID().isEmpty() ||  accVO.getACCGUID().equals(""))				
				ps.setString(1,"\u00A0");
			else 	
				ps.setString(1, accVO.getACCGUID());

			if(accVO.getObjectClassTerm()==null || accVO.getObjectClassTerm().length()==0 || accVO.getObjectClassTerm().isEmpty() || accVO.getObjectClassTerm().equals("")){
				ps.setString(2, "\u00A0");
			}
			else 
				ps.setString(2, accVO.getObjectClassTerm());
			if(accVO.getDEN()==null || accVO.getDEN().length()==0 || accVO.getDEN().isEmpty() || accVO.getDEN().equals("")){
				ps.setString(3, "\u00A0");
			}
			else 
				ps.setString(3, accVO.getDEN());
//			if(accVO.getDefinition()==null || accVO.getDefinition().length()==0 || accVO.getDefinition().isEmpty() || accVO.getDefinition().equals("")){
//				ps.setString(4, "\u00A0");
//			}
//			else {
				String s = StringUtils.abbreviate(accVO.getDefinition(), 4000);
				ps.setString(4, s);
//			}

			if(accVO.getBasedACCID() < 1)
				ps.setNull(5, java.sql.Types.INTEGER);
			else
				ps.setInt(5, accVO.getBasedACCID());
//			if( accVO.getObjectClassQualifier()==null ||  accVO.getObjectClassQualifier().length()==0 ||  accVO.getObjectClassQualifier().isEmpty() ||  accVO.getObjectClassQualifier().equals(""))				
//				ps.setString(6,"\u00A0");
//			else 	
				ps.setString(6, accVO.getObjectClassQualifier());

			ps.setInt(7, accVO.getOAGISComponentType());
//			if( accVO.getModule()==null ||  accVO.getModule().length()==0 ||  accVO.getModule().isEmpty() ||  accVO.getModule().equals(""))				
//				ps.setString(8,"\u00A0");
//			else 	
				ps.setString(8, accVO.getModule());

			ps.setInt(9, accVO.getNamespaceId());
			ps.setInt(10, accVO.getCreatedByUserId());
			ps.setInt(11, accVO.getOwnerUserId());
			ps.setInt(12, accVO.getLastUpdatedByUserId());
			//ps.setTimestamp(13, accVO.getLastUpdateTimestamp());
			ps.setInt(13, accVO.getState());
			ps.setInt(14, accVO.getRevisionNum());
			ps.setInt(15, accVO.getRevisionTrackingNum());
			ps.setInt(16, accVO.getRevisionAction());
			ps.setInt(17, accVO.getReleaseId());
			ps.setInt(18, accVO.getCurrentAccId());
			if(accVO.getIs_deprecated())
				ps.setInt(19,1);
			else
				ps.setInt(19,0);

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
		ACCVO accVO = null;
		Connection conn = null;
		try {
			conn = tx.open();
			String sql = _FIND_ACC_STATEMENT;

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
				accVO = new ACCVO();
				accVO.setACCID(rs.getInt("ACC_ID"));
				accVO.setACCGUID(rs.getString("GUID"));
				accVO.setObjectClassTerm(rs.getString("Object_Class_Term"));
				accVO.setDEN(rs.getString("DEN"));
				accVO.setDefinition(rs.getString("Definition"));
				accVO.setBasedACCID(rs.getInt("Based_ACC_ID"));
				accVO.setObjectClassQualifier(rs.getString("Object_Class_Qualifier"));
				accVO.setOAGISComponentType(rs.getInt("OAGIS_Component_Type"));
				accVO.setModule(rs.getString("Module"));
				accVO.setNamespaceId(rs.getInt("namespace_id"));
				accVO.setCreatedByUserId(rs.getInt("Created_By"));
				accVO.setOwnerUserId(rs.getInt("owner_user_id"));
				accVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
				accVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				accVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				accVO.setState(rs.getInt("State"));
				accVO.setRevisionNum(rs.getInt("revision_num"));
				accVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
				accVO.setRevisionAction(rs.getInt("revision_action"));
				accVO.setReleaseId(rs.getInt("release_id"));
				accVO.setCurrentAccId(rs.getInt("current_acc_id"));
				accVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
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
		return accVO;
	}
	
	public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
		//DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ACCVO accVO = null;
		//Connection conn = null;
		try {
			//conn = tx.open();
			String sql = _FIND_ACC_STATEMENT;

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
				accVO = new ACCVO();
				accVO.setACCID(rs.getInt("ACC_ID"));
				accVO.setACCGUID(rs.getString("GUID"));
				accVO.setObjectClassTerm(rs.getString("Object_Class_Term"));
				accVO.setDEN(rs.getString("DEN"));
				accVO.setDefinition(rs.getString("Definition"));
				accVO.setBasedACCID(rs.getInt("Based_ACC_ID"));
				accVO.setObjectClassQualifier(rs.getString("Object_Class_Qualifier"));
				accVO.setOAGISComponentType(rs.getInt("OAGIS_Component_Type"));
				accVO.setModule(rs.getString("Module"));
				accVO.setNamespaceId(rs.getInt("namespace_id"));
				accVO.setCreatedByUserId(rs.getInt("Created_By"));
				accVO.setOwnerUserId(rs.getInt("owner_user_id"));
				accVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
				accVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				accVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				accVO.setState(rs.getInt("State"));
				accVO.setRevisionNum(rs.getInt("revision_num"));
				accVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
				accVO.setRevisionAction(rs.getInt("revision_action"));
				accVO.setReleaseId(rs.getInt("release_id"));
				accVO.setCurrentAccId(rs.getInt("current_acc_id"));
				accVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
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
			//tx.close();
		}
		return accVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_ACC_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ACCVO accVO = new ACCVO();
				accVO.setACCID(rs.getInt("ACC_ID"));
				accVO.setACCGUID(rs.getString("GUID"));
				accVO.setObjectClassTerm(rs.getString("Object_Class_Term"));
				accVO.setDEN(rs.getString("DEN"));
				accVO.setDefinition(rs.getString("Definition"));
				accVO.setBasedACCID(rs.getInt("Based_ACC_ID"));
				accVO.setObjectClassQualifier(rs.getString("Object_Class_Qualifier"));
				accVO.setOAGISComponentType(rs.getInt("OAGIS_Component_Type"));
				accVO.setModule(rs.getString("Module"));
				accVO.setNamespaceId(rs.getInt("namespace_id"));
				accVO.setCreatedByUserId(rs.getInt("Created_By"));
				accVO.setOwnerUserId(rs.getInt("owner_user_id"));
				accVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
				accVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				accVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				accVO.setState(rs.getInt("State"));
				accVO.setRevisionNum(rs.getInt("revision_num"));
				accVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
				accVO.setRevisionAction(rs.getInt("revision_action"));
				accVO.setReleaseId(rs.getInt("release_id"));
				accVO.setCurrentAccId(rs.getInt("current_acc_id"));
				accVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
				list.add(accVO);
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
			String sql = _FIND_ALL_ACC_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ACCVO accVO = new ACCVO();
				accVO.setACCID(rs.getInt("ACC_ID"));
				accVO.setACCGUID(rs.getString("GUID"));
				accVO.setObjectClassTerm(rs.getString("Object_Class_Term"));
				accVO.setDEN(rs.getString("DEN"));
				accVO.setDefinition(rs.getString("Definition"));
				accVO.setBasedACCID(rs.getInt("Based_ACC_ID"));
				accVO.setObjectClassQualifier(rs.getString("Object_Class_Qualifier"));
				accVO.setOAGISComponentType(rs.getInt("OAGIS_Component_Type"));
				accVO.setModule(rs.getString("Module"));
				accVO.setNamespaceId(rs.getInt("namespace_id"));
				accVO.setCreatedByUserId(rs.getInt("Created_By"));
				accVO.setOwnerUserId(rs.getInt("owner_user_id"));
				accVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
				accVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				accVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				accVO.setState(rs.getInt("State"));
				accVO.setRevisionNum(rs.getInt("revision_num"));
				accVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
				accVO.setRevisionAction(rs.getInt("revision_action"));
				accVO.setReleaseId(rs.getInt("release_id"));
				accVO.setCurrentAccId(rs.getInt("current_acc_id"));
				accVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
				list.add(accVO);
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
		ACCVO accVO = (ACCVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_ACC_STATEMENT);

			if( accVO.getACCGUID()==null ||  accVO.getACCGUID().length()==0 ||  accVO.getACCGUID().isEmpty() ||  accVO.getACCGUID().equals(""))				
				ps.setString(1,"\u00A0");
			else 	
				ps.setString(1, accVO.getACCGUID());

			if( accVO.getObjectClassTerm()==null ||  accVO.getObjectClassTerm().length()==0 ||  accVO.getObjectClassTerm().isEmpty() ||  accVO.getObjectClassTerm().equals(""))				
				ps.setString(2,"\u00A0");
			else 	
				ps.setString(2, accVO.getObjectClassTerm());

			if( accVO.getDEN()==null ||  accVO.getDEN().length()==0 ||  accVO.getDEN().isEmpty() ||  accVO.getDEN().equals(""))				
				ps.setString(3,"\u00A0");
			else 	
				ps.setString(3, accVO.getDEN());

//			if( accVO.getDefinition()==null ||  accVO.getDefinition().length()==0 ||  accVO.getDefinition().isEmpty() ||  accVO.getDefinition().equals(""))				
//				ps.setString(4,"\u00A0");
//			else 	{
				String s = StringUtils.abbreviate(accVO.getDefinition(), 4000);
				ps.setString(4, s);
//			}

			ps.setInt(5, accVO.getBasedACCID());
//			if( accVO.getObjectClassQualifier()==null ||  accVO.getObjectClassQualifier().length()==0 ||  accVO.getObjectClassQualifier().isEmpty() ||  accVO.getObjectClassQualifier().equals(""))				
//				ps.setString(6,"\u00A0");
//			else 	
				ps.setString(6, accVO.getObjectClassQualifier());

			ps.setInt(7, accVO.getOAGISComponentType());
//			if( accVO.getModule()==null ||  accVO.getModule().length()==0 ||  accVO.getModule().isEmpty() ||  accVO.getModule().equals(""))				
//				ps.setString(8,"\u00A0");
//			else 	
				ps.setString(8, accVO.getModule());

			ps.setInt(9, accVO.getNamespaceId());
			ps.setInt(10, accVO.getCreatedByUserId());
			ps.setInt(11, accVO.getOwnerUserId());
			ps.setInt(12, accVO.getLastUpdatedByUserId());
			//ps.setTimestamp(13, accVO.getLastUpdateTimestamp());
			ps.setInt(13, accVO.getState());
			ps.setInt(14, accVO.getRevisionNum());
			ps.setInt(15, accVO.getRevisionTrackingNum());
			ps.setInt(16, accVO.getRevisionAction());
			ps.setInt(17, accVO.getReleaseId());
			ps.setInt(18, accVO.getCurrentAccId());
			if( accVO.getIs_deprecated())				
				ps.setInt(19,1);
			else 	
				ps.setInt(19,0);

			ps.setInt(20, accVO.getACCID());
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
		ACCVO accVO = (ACCVO)obj;

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_ACC_STATEMENT);
			ps.setInt(1, accVO.getACCID());
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
	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn)
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
