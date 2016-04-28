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
import org.oagi.srt.persistence.dto.ASCCVO;


/**
*
* @author Nasif Sikder
* @version 1.0
*
*/
public class ASCCOracleDAO extends SRTDAO {
	
	private final String _tableName = "ascc";
	
	private final String _FIND_ALL_ASCC_STATEMENT = 
			"SELECT ASCC_ID, GUID, Cardinality_Min, Cardinality_Max, Seq_Key, "
			+ "From_ACC_ID, To_ASCCP_ID, DEN, Definition, Created_By, owner_user_id, Last_Updated_By, "
			+ "Creation_Timestamp, Last_Update_Timestamp, State, revision_num, revision_tracking_num, revision_action, release_id, current_ascc_id, is_deprecated FROM " + _tableName;
	
	private final String _FIND_ASCC_STATEMENT = 
			"SELECT ASCC_ID, GUID, Cardinality_Min, Cardinality_Max, Seq_Key, "
					+ "From_ACC_ID, To_ASCCP_ID, DEN, Definition, Created_By, owner_user_id, Last_Updated_By, "
					+ "Creation_Timestamp, Last_Update_Timestamp, State, revision_num, revision_tracking_num, revision_action, release_id, current_ascc_id, is_deprecated FROM " + _tableName;
	
	private final String _INSERT_ASCC_STATEMENT = 
			"INSERT INTO " + _tableName + " (GUID, Cardinality_Min, Cardinality_Max, Seq_Key, "
					+ "From_ACC_ID, To_ASCCP_ID, DEN, Definition, Created_By, owner_user_id, Last_Updated_By, "
					+ "Creation_Timestamp, Last_Update_Timestamp, State, revision_num, revision_tracking_num, revision_action, release_id, current_ascc_id, is_deprecated) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?)";
	
	private final String _UPDATE_ASCC_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET Last_Update_Timestamp = CURRENT_TIMESTAMP, GUID = ?, Cardinality_Min = ?, Cardinality_Max = ?, Seq_Key = ?, "
			+ "From_ACC_ID = ?, To_ASCCP_ID = ?, DEN = ?, Definition = ?, Created_By = ?, owner_user_id = ?, Last_Updated_By = ?, "
			+ "State =?,  revision_num = ?, revision_tracking_num = ?, revision_action = ?, release_id = ?, current_ascc_id = ?, is_deprecated = ? "
			+ "WHERE ASCC_ID = ?";
	
	private final String _DELETE_ASCC_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE ASCC_ID = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		ASCCVO asccVO = (ASCCVO)obj;
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_ASCC_STATEMENT);
			
			if( asccVO.getASCCGUID()==null ||  asccVO.getASCCGUID().length()==0 ||  asccVO.getASCCGUID().isEmpty() ||  asccVO.getASCCGUID().equals(""))				
				ps.setString(1,"**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			else 	
				ps.setString(1, asccVO.getASCCGUID());

			ps.setInt(2, asccVO.getCardinalityMin());
			ps.setInt(3, asccVO.getCardinalityMax());
			ps.setInt(4, asccVO.getSequencingKey());
			ps.setInt(5, asccVO.getAssocFromACCID());
			ps.setInt(6, asccVO.getAssocToASCCPID());
			if(asccVO.getDEN()==null || asccVO.getDEN().length()==0 || asccVO.getDEN().isEmpty() || asccVO.getDEN().equals("")){
				ps.setString(7, "**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			}
			else 
				ps.setString(7, asccVO.getDEN());
			
//			if(asccVO.getDefinition()==null || asccVO.getDefinition().length()==0 || asccVO.getDefinition().isEmpty() || asccVO.getDefinition().equals("")){
//				ps.setString(8, "\u00A0");
//			}
//			else {
				String s = StringUtils.abbreviate(asccVO.getDefinition(), 4000);
				ps.setString(8, s);
//			}
			ps.setInt(9, asccVO.getCreatedByUserId());
			ps.setInt(10, asccVO.getOwnerUserId());
			ps.setInt(11, asccVO.getLastUpdatedByUserId());
			//ps.setTimestamp(12, asccVO.getLastUpdateTimestamp());
			ps.setInt(12, asccVO.getState());
			
			if(asccVO.getRevisionNum() < 0){
				ps.setNull(13, java.sql.Types.INTEGER);
			}
			else {
				ps.setInt(13, asccVO.getRevisionNum());
			}			
			if(asccVO.getRevisionTrackingNum() < 0){
				ps.setNull(14, java.sql.Types.INTEGER);
			}
			else {
				ps.setInt(14, asccVO.getRevisionTrackingNum());
			}			
			if(asccVO.getRevisionAction() < 1){
				ps.setNull(15, java.sql.Types.INTEGER);
			}
			else {
				ps.setInt(15, asccVO.getRevisionAction());
			}			
			if(asccVO.getReleaseId() < 1){
				ps.setNull(16, java.sql.Types.INTEGER);
			}
			else {
				ps.setInt(16, asccVO.getReleaseId());
			}
			if(asccVO.getCurrentAsccId() < 1){
				ps.setNull(17, java.sql.Types.INTEGER);
			}
			else {
				ps.setInt(17, asccVO.getCurrentAsccId());
			}
			if( asccVO.getIs_deprecated())				
				ps.setInt(18,1);
			else 	
				ps.setInt(18,0);

			ps.executeUpdate();

//			ResultSet tableKeys = ps.getGeneratedKeys();
//			tableKeys.next();
//			int autoGeneratedID = tableKeys.getInt(1);

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

	@Override
	public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ASCCVO asccVO = null;
		
		try {
			Connection conn = tx.open();
			String sql = _FIND_ASCC_STATEMENT;

			String WHERE_OR_AND = " WHERE ";
			int nCond = qc.getSize();
			if (nCond > 0) {
				for (int n = 0; n < nCond; n++) {
					sql += WHERE_OR_AND + qc.getField(n) + " = ?";
					WHERE_OR_AND = " AND ";
				}
			}
			//System.out.println("### sql: " + sql);
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
				asccVO = new ASCCVO();
				asccVO.setASCCID(rs.getInt("ASCC_ID"));
				asccVO.setASCCGUID(rs.getString("GUID"));
				asccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				asccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				asccVO.setSequencingKey(rs.getInt("Seq_Key"));
				asccVO.setAssocFromACCID(rs.getInt("From_ACC_ID"));
				asccVO.setAssocToASCCPID(rs.getInt("To_ASCCP_ID"));
				asccVO.setDEN(rs.getString("DEN"));
				asccVO.setDefinition(rs.getString("Definition"));
				asccVO.setCreatedByUserId(rs.getInt("Created_By"));
				asccVO.setOwnerUserId(rs.getInt("owner_user_id"));
				asccVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
				asccVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asccVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				asccVO.setState(rs.getInt("State"));
				asccVO.setRevisionNum(rs.getInt("revision_num"));
				asccVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
				asccVO.setRevisionAction(rs.getInt("revision_action"));
				asccVO.setReleaseId(rs.getInt("release_id"));
				asccVO.setCurrentAsccId(rs.getInt("current_ascc_id"));
				asccVO.setIs_deprecated(rs.getBoolean("is_deprecated"));

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
		return asccVO;
		
	}
	
	public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ASCCVO asccVO = null;
		
		try {
			//Connection conn = tx.open();
			String sql = _FIND_ASCC_STATEMENT;

			String WHERE_OR_AND = " WHERE ";
			int nCond = qc.getSize();
			if (nCond > 0) {
				for (int n = 0; n < nCond; n++) {
					sql += WHERE_OR_AND + qc.getField(n) + " = ?";
					WHERE_OR_AND = " AND ";
				}
			}
			//System.out.println("### sql: " + sql);
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
				asccVO = new ASCCVO();
				asccVO.setASCCID(rs.getInt("ASCC_ID"));
				asccVO.setASCCGUID(rs.getString("GUID"));
				asccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				asccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				asccVO.setSequencingKey(rs.getInt("Seq_Key"));
				asccVO.setAssocFromACCID(rs.getInt("From_ACC_ID"));
				asccVO.setAssocToASCCPID(rs.getInt("To_ASCCP_ID"));
				asccVO.setDEN(rs.getString("DEN"));
				asccVO.setDefinition(rs.getString("Definition"));
				asccVO.setCreatedByUserId(rs.getInt("Created_By"));
				asccVO.setOwnerUserId(rs.getInt("owner_user_id"));
				asccVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
				asccVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asccVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				asccVO.setState(rs.getInt("State"));
				asccVO.setRevisionNum(rs.getInt("revision_num"));
				asccVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
				asccVO.setRevisionAction(rs.getInt("revision_action"));
				asccVO.setReleaseId(rs.getInt("release_id"));
				asccVO.setCurrentAsccId(rs.getInt("current_ascc_id"));
				asccVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
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
		return asccVO;
		
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			Connection conn = tx.open();
			String sql = _FIND_ALL_ASCC_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ASCCVO asccVO = new ASCCVO();
				
				asccVO.setASCCID(rs.getInt("ASCC_ID"));
				asccVO.setASCCGUID(rs.getString("GUID"));
				asccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				asccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				asccVO.setSequencingKey(rs.getInt("Seq_Key"));
				asccVO.setAssocFromACCID(rs.getInt("From_ACC_ID"));
				asccVO.setAssocToASCCPID(rs.getInt("To_ASCCP_ID"));
				asccVO.setDEN(rs.getString("DEN"));
				asccVO.setDefinition(rs.getString("Definition"));
				asccVO.setCreatedByUserId(rs.getInt("Created_By"));
				asccVO.setOwnerUserId(rs.getInt("owner_user_id"));
				asccVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
				asccVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asccVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				asccVO.setState(rs.getInt("State"));
				asccVO.setRevisionNum(rs.getInt("revision_num"));
				asccVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
				asccVO.setRevisionAction(rs.getInt("revision_action"));
				asccVO.setReleaseId(rs.getInt("release_id"));
				asccVO.setCurrentAsccId(rs.getInt("current_ascc_id"));
				asccVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
				list.add(asccVO);
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
			String sql = _FIND_ALL_ASCC_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ASCCVO asccVO = new ASCCVO();
				
				asccVO.setASCCID(rs.getInt("ASCC_ID"));
				asccVO.setASCCGUID(rs.getString("GUID"));
				asccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				asccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				asccVO.setSequencingKey(rs.getInt("Seq_Key"));
				asccVO.setAssocFromACCID(rs.getInt("From_ACC_ID"));
				asccVO.setAssocToASCCPID(rs.getInt("To_ASCCP_ID"));
				asccVO.setDEN(rs.getString("DEN"));
				asccVO.setDefinition(rs.getString("Definition"));
				asccVO.setCreatedByUserId(rs.getInt("Created_By"));
				asccVO.setOwnerUserId(rs.getInt("owner_user_id"));
				asccVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
				asccVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asccVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				asccVO.setState(rs.getInt("State"));
				asccVO.setRevisionNum(rs.getInt("revision_num"));
				asccVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
				asccVO.setRevisionAction(rs.getInt("revision_action"));
				asccVO.setReleaseId(rs.getInt("release_id"));
				asccVO.setCurrentAsccId(rs.getInt("current_ascc_id"));
				asccVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
				list.add(asccVO);
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
		ASCCVO asccVO = (ASCCVO)obj;
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_ASCC_STATEMENT);

			if( asccVO.getASCCGUID()==null ||  asccVO.getASCCGUID().length()==0 ||  asccVO.getASCCGUID().isEmpty() ||  asccVO.getASCCGUID().equals(""))				
				ps.setString(1,"**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			else 	
				ps.setString(1, asccVO.getASCCGUID());

			ps.setInt(2, asccVO.getCardinalityMin());
			ps.setInt(3, asccVO.getCardinalityMax());
			ps.setInt(4, asccVO.getSequencingKey());
			ps.setInt(5, asccVO.getAssocFromACCID());
			ps.setInt(6, asccVO.getAssocToASCCPID());
			if( asccVO.getDEN()==null ||  asccVO.getDEN().length()==0 ||  asccVO.getDEN().isEmpty() ||  asccVO.getDEN().equals(""))				
				ps.setString(7,"**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			else 	
				ps.setString(7, asccVO.getDEN());

//			if( asccVO.getDefinition()==null ||  asccVO.getDefinition().length()==0 ||  asccVO.getDefinition().isEmpty() ||  asccVO.getDefinition().equals(""))				
//				ps.setString(8,"\u00A0");
//			else 	{
				String s = StringUtils.abbreviate(asccVO.getDefinition(), 4000);
				ps.setString(8, s);
//			}

			ps.setInt(9, asccVO.getCreatedByUserId());
			ps.setInt(10, asccVO.getOwnerUserId());
			ps.setInt(11, asccVO.getLastUpdatedByUserId());
			//ps.setTimestamp(12, asccVO.getCreationTimestamp());
			ps.setInt(12, asccVO.getState());
			
			if(asccVO.getRevisionNum() < 0){
				ps.setNull(13, java.sql.Types.INTEGER);
			}
			else {
				ps.setInt(13, asccVO.getRevisionNum());
			}			
			if(asccVO.getRevisionTrackingNum() < 0){
				ps.setNull(14, java.sql.Types.INTEGER);
			}
			else {
				ps.setInt(14, asccVO.getRevisionTrackingNum());
			}			
			if(asccVO.getRevisionAction() < 1){
				ps.setNull(15, java.sql.Types.INTEGER);
			}
			else {
				ps.setInt(15, asccVO.getRevisionAction());
			}			
			if(asccVO.getReleaseId() < 1){
				ps.setNull(16, java.sql.Types.INTEGER);
			}
			else {
				ps.setInt(16, asccVO.getReleaseId());
			}
			if(asccVO.getCurrentAsccId() < 1){
				ps.setNull(17, java.sql.Types.INTEGER);
			}
			else {
				ps.setInt(17, asccVO.getCurrentAsccId());
			}
			if( asccVO.getIs_deprecated())				
				ps.setInt(18,1);
			else 	
				ps.setInt(18,0);
			

			ps.setInt(19, asccVO.getASCCID());
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
		ASCCVO asccVO = (ASCCVO)obj;

		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		try {
			Connection conn = tx.open();

			ps = conn.prepareStatement(_DELETE_ASCC_STATEMENT);
			ps.setInt(1, asccVO.getASCCID());
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
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			Connection conn = tx.open();
			String sql = _FIND_ASCC_STATEMENT;

			String WHERE_OR_AND = " WHERE ";
			int nCond = qc.getSize();
			if (nCond > 0) {
				for (int n = 0; n < nCond; n++) {
					sql += WHERE_OR_AND + qc.getField(n) + " = ?";
					WHERE_OR_AND = " AND ";
				}
			}
			//System.out.println("### sql: " + sql);
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
				ASCCVO asccVO = new ASCCVO();
				
				asccVO.setASCCID(rs.getInt("ASCC_ID"));
				asccVO.setASCCGUID(rs.getString("GUID"));
				asccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				asccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				asccVO.setSequencingKey(rs.getInt("Seq_Key"));
				asccVO.setAssocFromACCID(rs.getInt("From_ACC_ID"));
				asccVO.setAssocToASCCPID(rs.getInt("To_ASCCP_ID"));
				asccVO.setDEN(rs.getString("DEN"));
				asccVO.setDefinition(rs.getString("Definition"));
				asccVO.setCreatedByUserId(rs.getInt("Created_By"));
				asccVO.setOwnerUserId(rs.getInt("owner_user_id"));
				asccVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
				asccVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asccVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				asccVO.setState(rs.getInt("State"));
				asccVO.setRevisionNum(rs.getInt("revision_num"));
				asccVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
				asccVO.setRevisionAction(rs.getInt("revision_action"));
				asccVO.setReleaseId(rs.getInt("release_id"));
				asccVO.setCurrentAsccId(rs.getInt("current_ascc_id"));
				asccVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
				list.add(asccVO);
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
	
	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn)
			throws SRTDAOException {
		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		DBAgent tx = new DBAgent();
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			//Connection conn = tx.open();
			String sql = _FIND_ASCC_STATEMENT;

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
				ASCCVO asccVO = new ASCCVO();
				
				asccVO.setASCCID(rs.getInt("ASCC_ID"));
				asccVO.setASCCGUID(rs.getString("GUID"));
				asccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
				asccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
				asccVO.setSequencingKey(rs.getInt("Seq_Key"));
				asccVO.setAssocFromACCID(rs.getInt("From_ACC_ID"));
				asccVO.setAssocToASCCPID(rs.getInt("To_ASCCP_ID"));
				asccVO.setDEN(rs.getString("DEN"));
				asccVO.setDefinition(rs.getString("Definition"));
				asccVO.setCreatedByUserId(rs.getInt("Created_By"));
				asccVO.setOwnerUserId(rs.getInt("owner_user_id"));
				asccVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
				asccVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				asccVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				asccVO.setState(rs.getInt("State"));
				asccVO.setRevisionNum(rs.getInt("revision_num"));
				asccVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
				asccVO.setRevisionAction(rs.getInt("revision_action"));
				asccVO.setReleaseId(rs.getInt("release_id"));
				asccVO.setCurrentAsccId(rs.getInt("current_ascc_id"));
				asccVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
				list.add(asccVO);
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

	@Override
	public int insertObject(SRTObject obj, Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
