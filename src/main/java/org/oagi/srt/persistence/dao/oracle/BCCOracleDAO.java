package org.oagi.srt.persistence.dao.oracle;

import org.apache.commons.lang.StringUtils;
import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BCCVO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/

public class BCCOracleDAO extends SRTDAO{
	private final String _tableName = "bcc";

	private final String _FIND_ALL_BCC_STATEMENT = 
			"SELECT BCC_ID, GUID, Cardinality_Min, Cardinality_Max, To_BCCP_ID, From_ACC_ID, "
					+ "Seq_key, Entity_Type, DEN, Definition, Created_By, owner_user_id, Last_Updated_By, "
					+ "Creation_Timestamp, Last_Update_Timestamp, State, revision_num, revision_tracking_num, revision_action, release_id, current_bcc_id, is_deprecated FROM " + _tableName;

	private final String _FIND_BCC_STATEMENT = 
			"SELECT BCC_ID, GUID, Cardinality_Min, Cardinality_Max, To_BCCP_ID, From_ACC_ID, "
					+ "Seq_key, Entity_Type, DEN, Definition, Created_By, owner_user_id, Last_Updated_By, "
					+ "Creation_Timestamp, Last_Update_Timestamp, State, revision_num, revision_tracking_num, revision_action, release_id, current_bcc_id, is_deprecated FROM " + _tableName;
	
	private final String _INSERT_BCC_STATEMENT = 
			"INSERT INTO " + _tableName + " (GUID, Cardinality_Min, Cardinality_Max, To_BCCP_ID, From_ACC_ID, "
					+ "Seq_key, Entity_Type, DEN, Definition, Created_By, owner_user_id, Last_Updated_By, "
					+ "Creation_Timestamp, Last_Update_Timestamp, State, revision_num, revision_tracking_num, revision_action, release_id, current_bcc_id, is_deprecated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?)";

	private final String _UPDATE_BCC_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET Last_Update_Timestamp = CURRENT_TIMESTAMP, GUID = ?, Cardinality_Min = ?, Cardinality_Max = ?, "
			+ "To_BCCP_ID = ?, From_ACC_ID = ?, Seq_Key = ?, Entity_Type = ?, DEN = ?, Definition = ?, Created_By = ?, owner_user_id = ?, Last_Updated_By = ?, "
			+ "State =?,  revision_num = ?, revision_tracking_num = ?, revision_action = ?, release_id = ?, current_bcc_id = ?, is_deprecated = ? "
			+ "WHERE BCC_ID = ?";

	private final String _DELETE_BCC_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE BCC_ID = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}

    public int insertObject(SRTObject obj) throws SRTDAOException {
        DBAgent tx = new DBAgent();
        Connection conn = null;
        PreparedStatement ps = null;

        BCCVO bccVO = (BCCVO) obj;
        try {
            conn = tx.open();
            ps = conn.prepareStatement(_INSERT_BCC_STATEMENT);
            if (bccVO.getBCCGUID() == null || bccVO.getBCCGUID().length() == 0 || bccVO.getBCCGUID().isEmpty() || bccVO.getBCCGUID().equals(""))
                ps.setString(1, "**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
            else
                ps.setString(1, bccVO.getBCCGUID());

            ps.setInt(2, bccVO.getCardinalityMin());
            ps.setInt(3, bccVO.getCardinalityMax());
            ps.setInt(4, bccVO.getAssocToBCCPID());
            ps.setInt(5, bccVO.getAssocFromACCID());
            ps.setInt(6, bccVO.getSequencingKey());
            ps.setInt(7, bccVO.getEntityType());
            if (bccVO.getDEN() == null || bccVO.getDEN().length() == 0 || bccVO.getDEN().isEmpty() || bccVO.getDEN().equals("")) {
                ps.setString(8, "**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
            } else
                ps.setString(8, bccVO.getDEN());

//			if(bccVO.getDefinition()==null || bccVO.getDefinition().length()==0 || bccVO.getDefinition().isEmpty() || bccVO.getDefinition().equals("")){
//				ps.setString(9, "\u00A0");
//			}
//			else {
				String s = StringUtils.abbreviate(bccVO.getDefinition(), 4000);
				ps.setString(9, s);
//			}
            ps.setInt(10, bccVO.getCreatedByUserId());
            ps.setInt(11, bccVO.getOwnerUserId());
            ps.setInt(12, bccVO.getLastUpdatedByUserId());
            //ps.setTimestamp(13, bccVO.getLastUpdateTimestamp());
            ps.setInt(13, bccVO.getState());

            if (bccVO.getRevisionNum() < 0) {
                ps.setNull(14, java.sql.Types.INTEGER);
            } else {
                ps.setInt(14, bccVO.getRevisionNum());
            }
            if (bccVO.getRevisionTrackingNum() < 0) {
                ps.setNull(15, java.sql.Types.INTEGER);
            } else {
                ps.setInt(15, bccVO.getRevisionTrackingNum());
            }
            if (bccVO.getRevisionAction() < 1) {
                ps.setNull(16, java.sql.Types.INTEGER);
            } else {
                ps.setInt(16, bccVO.getRevisionAction());
            }
            if (bccVO.getReleaseId() < 1) {
                ps.setNull(17, java.sql.Types.INTEGER);
            } else {
                ps.setInt(17, bccVO.getReleaseId());
            }
            if (bccVO.getCurrentBccId() < 1) {
                ps.setNull(18, java.sql.Types.INTEGER);
            } else {
                ps.setInt(18, bccVO.getCurrentBccId());
            }
            if (bccVO.getIs_deprecated())
                ps.setInt(19, 1);
            else
                ps.setInt(19, 0);


            ps.executeUpdate();

//			ResultSet tableKeys = ps.getGeneratedKeys();
//			tableKeys.next();
//			int autoGeneratedID = tableKeys.getInt(1);
			tx.commit();
        } catch (BfPersistenceException e) {
            tx.rollback();
            throw new SRTDAOException(SRTDAOException.DAO_INSERT_ERROR, e);
        } catch (SQLException e) {
            tx.rollback();
            throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
        } finally {
            closeQuietly(ps);
            closeQuietly(conn);
            closeQuietly(tx);
        }
        return 1;
    }

    public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
        DBAgent tx = new DBAgent();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        BCCVO bccVO = null;
        try {
            conn = tx.open();
            String sql = _FIND_BCC_STATEMENT;

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
                        ps.setString(n + 1, (String) value);
                    } else if (value instanceof Integer) {
                        ps.setInt(n + 1, ((Integer) value).intValue());
                    }
                }
            }

            rs = ps.executeQuery();
            if (rs.next()) {
                bccVO = new BCCVO();
                bccVO.setBCCID(rs.getInt("BCC_ID"));
                bccVO.setBCCGUID(rs.getString("GUID"));
                bccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
                bccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
                bccVO.setAssocToBCCPID(rs.getInt("To_BCCP_ID"));
                bccVO.setAssocFromACCID(rs.getInt("From_ACC_ID"));
                bccVO.setSequencingKey(rs.getInt("Seq_key"));
                bccVO.setEntityType(rs.getInt("Entity_Type"));
                bccVO.setDEN(rs.getString("DEN"));
                bccVO.setDefinition(rs.getString("Definition"));
                bccVO.setCreatedByUserId(rs.getInt("Created_By"));
                bccVO.setOwnerUserId(rs.getInt("owner_user_id"));
                bccVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
                bccVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
                bccVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
                bccVO.setState(rs.getInt("State"));
                bccVO.setRevisionNum(rs.getInt("revision_num"));
                bccVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
                bccVO.setRevisionAction(rs.getInt("revision_action"));
                bccVO.setReleaseId(rs.getInt("release_id"));
                bccVO.setCurrentBccId(rs.getInt("current_bcc_id"));
                bccVO.setIs_deprecated(rs.getBoolean("is_deprecated"));

            }
            tx.commit();
        } catch (BfPersistenceException e) {
            throw new SRTDAOException(SRTDAOException.DAO_FIND_ERROR, e);
        } catch (SQLException e) {
            throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
            closeQuietly(tx);
        }
        return bccVO;
    }

    public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        BCCVO bccVO = null;
        try {
            String sql = _FIND_BCC_STATEMENT;

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
                        ps.setString(n + 1, (String) value);
                    } else if (value instanceof Integer) {
                        ps.setInt(n + 1, ((Integer) value).intValue());
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
                bccVO = new BCCVO();
                bccVO.setBCCID(rs.getInt("BCC_ID"));
                bccVO.setBCCGUID(rs.getString("GUID"));
                bccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
                bccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
                bccVO.setAssocToBCCPID(rs.getInt("To_BCCP_ID"));
                bccVO.setAssocFromACCID(rs.getInt("From_ACC_ID"));
                bccVO.setSequencingKey(rs.getInt("Seq_key"));
                bccVO.setEntityType(rs.getInt("Entity_Type"));
                bccVO.setDEN(rs.getString("DEN"));
                bccVO.setDefinition(rs.getString("Definition"));
                bccVO.setCreatedByUserId(rs.getInt("Created_By"));
                bccVO.setOwnerUserId(rs.getInt("owner_user_id"));
                bccVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
                bccVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
                bccVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
                bccVO.setState(rs.getInt("State"));
                bccVO.setRevisionNum(rs.getInt("revision_num"));
                bccVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
                bccVO.setRevisionAction(rs.getInt("revision_action"));
                bccVO.setReleaseId(rs.getInt("release_id"));
                bccVO.setCurrentBccId(rs.getInt("current_bcc_id"));
                bccVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
            }
        } catch (SQLException e) {
            throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
        return bccVO;
    }

    public ArrayList<SRTObject> findObjects() throws SRTDAOException {
        DBAgent tx = new DBAgent();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        ArrayList<SRTObject> list = new ArrayList<SRTObject>();
        try {
            conn = tx.open();
            String sql = _FIND_ALL_BCC_STATEMENT;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                BCCVO bccVO = new BCCVO();
                bccVO.setBCCID(rs.getInt("BCC_ID"));
                bccVO.setBCCGUID(rs.getString("GUID"));
                bccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
                bccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
                bccVO.setAssocToBCCPID(rs.getInt("To_BCCP_ID"));
                bccVO.setAssocFromACCID(rs.getInt("From_ACC_ID"));
                bccVO.setSequencingKey(rs.getInt("Seq_key"));
                bccVO.setEntityType(rs.getInt("Entity_Type"));
                bccVO.setDEN(rs.getString("DEN"));
                bccVO.setDefinition(rs.getString("Definition"));
                bccVO.setCreatedByUserId(rs.getInt("Created_By"));
                bccVO.setOwnerUserId(rs.getInt("owner_user_id"));
                bccVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
                bccVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
                bccVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
                bccVO.setState(rs.getInt("State"));
                bccVO.setRevisionNum(rs.getInt("revision_num"));
                bccVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
                bccVO.setRevisionAction(rs.getInt("revision_action"));
                bccVO.setReleaseId(rs.getInt("release_id"));
                bccVO.setCurrentBccId(rs.getInt("current_bcc_id"));
                bccVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
                list.add(bccVO);
            }
            tx.commit();
        } catch (BfPersistenceException e) {
            throw new SRTDAOException(SRTDAOException.DAO_FIND_ERROR, e);
        } catch (SQLException e) {
            throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
            closeQuietly(tx);
        }

        return list;
    }

    public ArrayList<SRTObject> findObjects(Connection conn) throws SRTDAOException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ArrayList<SRTObject> list = new ArrayList<SRTObject>();
        try {
            String sql = _FIND_ALL_BCC_STATEMENT;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                BCCVO bccVO = new BCCVO();
                bccVO.setBCCID(rs.getInt("BCC_ID"));
                bccVO.setBCCGUID(rs.getString("GUID"));
                bccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
                bccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
                bccVO.setAssocToBCCPID(rs.getInt("To_BCCP_ID"));
                bccVO.setAssocFromACCID(rs.getInt("From_ACC_ID"));
                bccVO.setSequencingKey(rs.getInt("Seq_key"));
                bccVO.setEntityType(rs.getInt("Entity_Type"));
                bccVO.setDEN(rs.getString("DEN"));
                bccVO.setDefinition(rs.getString("Definition"));
                bccVO.setCreatedByUserId(rs.getInt("Created_By"));
                bccVO.setOwnerUserId(rs.getInt("owner_user_id"));
                bccVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
                bccVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
                bccVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
                bccVO.setState(rs.getInt("State"));
                bccVO.setRevisionNum(rs.getInt("revision_num"));
                bccVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
                bccVO.setRevisionAction(rs.getInt("revision_action"));
                bccVO.setReleaseId(rs.getInt("release_id"));
                bccVO.setCurrentBccId(rs.getInt("current_bcc_id"));
                bccVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
                list.add(bccVO);
            }
        } catch (SQLException e) {
            throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }

        return list;
    }

    public boolean updateObject(SRTObject obj) throws SRTDAOException {
        DBAgent tx = new DBAgent();
        Connection conn = null;
        PreparedStatement ps = null;

        BCCVO bccVO = (BCCVO) obj;
        try {
            conn = tx.open();

            ps = conn.prepareStatement(_UPDATE_BCC_STATEMENT);

            if (bccVO.getBCCGUID() == null || bccVO.getBCCGUID().length() == 0 || bccVO.getBCCGUID().isEmpty() || bccVO.getBCCGUID().equals(""))
                ps.setString(1, "**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
            else
                ps.setString(1, bccVO.getBCCGUID());

            ps.setInt(2, bccVO.getCardinalityMin());
            ps.setInt(3, bccVO.getCardinalityMax());
            ps.setInt(4, bccVO.getAssocToBCCPID());
            ps.setInt(5, bccVO.getAssocFromACCID());
            ps.setInt(6, bccVO.getSequencingKey());
            ps.setInt(7, bccVO.getEntityType());
            if (bccVO.getDEN() == null || bccVO.getDEN().length() == 0 || bccVO.getDEN().isEmpty() || bccVO.getDEN().equals(""))
                ps.setString(8, "**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
            else
                ps.setString(8, bccVO.getDEN());

//			if( bccVO.getDefinition()==null ||  bccVO.getDefinition().length()==0 ||  bccVO.getDefinition().isEmpty() ||  bccVO.getDefinition().equals(""))				
//				ps.setString(9,"\u00A0");
//			else 	{
				String s = StringUtils.abbreviate(bccVO.getDefinition(), 4000);
				ps.setString(9, s);
//			}

            ps.setInt(10, bccVO.getCreatedByUserId());
            ps.setInt(11, bccVO.getOwnerUserId());
            ps.setInt(12, bccVO.getLastUpdatedByUserId());
            //ps.setTimestamp(13, bccVO.getLastUpdateTimestamp());
            ps.setInt(13, bccVO.getState());

            if (bccVO.getRevisionNum() < 0) {
                ps.setNull(14, java.sql.Types.INTEGER);
            } else {
                ps.setInt(14, bccVO.getRevisionNum());
            }
            if (bccVO.getRevisionTrackingNum() < 0) {
                ps.setNull(15, java.sql.Types.INTEGER);
            } else {
                ps.setInt(15, bccVO.getRevisionTrackingNum());
            }
            if (bccVO.getRevisionAction() < 1) {
                ps.setNull(16, java.sql.Types.INTEGER);
            } else {
                ps.setInt(16, bccVO.getRevisionAction());
            }
            if (bccVO.getReleaseId() < 1) {
                ps.setNull(17, java.sql.Types.INTEGER);
            } else {
                ps.setInt(17, bccVO.getReleaseId());
            }
            if (bccVO.getCurrentBccId() < 1) {
                ps.setNull(18, java.sql.Types.INTEGER);
            } else {
                ps.setInt(18, bccVO.getCurrentBccId());
            }
            if (bccVO.getIs_deprecated())
                ps.setInt(19, 1);
            else
                ps.setInt(19, 0);


            ps.setInt(20, bccVO.getBCCID());
            ps.executeUpdate();

            tx.commit();
        } catch (BfPersistenceException e) {
            tx.rollback(e);
            throw new SRTDAOException(SRTDAOException.DAO_UPDATE_ERROR, e);
        } catch (SQLException e) {
            tx.rollback(e);
            throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
        } finally {
            closeQuietly(ps);
            closeQuietly(conn);
            closeQuietly(tx);
        }

        return true;
    }

    public boolean deleteObject(SRTObject obj) throws SRTDAOException {
        DBAgent tx = new DBAgent();
        Connection conn = null;
        PreparedStatement ps = null;

        BCCVO bccVO = (BCCVO) obj;
        try {
            conn = tx.open();

            ps = conn.prepareStatement(_DELETE_BCC_STATEMENT);
            ps.setInt(1, bccVO.getBCCID());
            ps.executeUpdate();

            tx.commit();
        } catch (BfPersistenceException e) {
            tx.rollback(e);
            throw new SRTDAOException(SRTDAOException.DAO_DELETE_ERROR, e);
        } catch (SQLException e) {
            tx.rollback(e);
            throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
        } finally {
            closeQuietly(ps);
            closeQuietly(conn);
            closeQuietly(tx);
        }

        return true;

    }

    @Override
    public ArrayList<SRTObject> findObjects(QueryCondition qc)
            throws SRTDAOException {
        DBAgent tx = new DBAgent();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        ArrayList<SRTObject> list = new ArrayList<SRTObject>();
        try {
            conn = tx.open();
            String sql = _FIND_BCC_STATEMENT;

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
                        ps.setString(n + 1, (String) value);
                    } else if (value instanceof Integer) {
                        ps.setInt(n + 1, ((Integer) value).intValue());
                    }
                }
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                BCCVO bccVO = new BCCVO();
                bccVO.setBCCID(rs.getInt("BCC_ID"));
                bccVO.setBCCGUID(rs.getString("GUID"));
                bccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
                bccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
                bccVO.setAssocToBCCPID(rs.getInt("To_BCCP_ID"));
                bccVO.setAssocFromACCID(rs.getInt("From_ACC_ID"));
                bccVO.setSequencingKey(rs.getInt("Seq_key"));
                bccVO.setEntityType(rs.getInt("Entity_Type"));
                bccVO.setDEN(rs.getString("DEN"));
                bccVO.setDefinition(rs.getString("Definition"));
                bccVO.setCreatedByUserId(rs.getInt("Created_By"));
                bccVO.setOwnerUserId(rs.getInt("owner_user_id"));
                bccVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
                bccVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
                bccVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
                bccVO.setState(rs.getInt("State"));
                bccVO.setRevisionNum(rs.getInt("revision_num"));
                bccVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
                bccVO.setRevisionAction(rs.getInt("revision_action"));
                bccVO.setReleaseId(rs.getInt("release_id"));
                bccVO.setCurrentBccId(rs.getInt("current_bcc_id"));
                bccVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
                list.add(bccVO);
            }
            tx.commit();
        } catch (BfPersistenceException e) {
            throw new SRTDAOException(SRTDAOException.DAO_FIND_ERROR, e);
        } catch (SQLException e) {
            throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
            closeQuietly(tx);
        }

        return list;
    }

    public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn)
            throws SRTDAOException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ArrayList<SRTObject> list = new ArrayList<SRTObject>();
        try {
            String sql = _FIND_BCC_STATEMENT;

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
                        ps.setString(n + 1, (String) value);
                    } else if (value instanceof Integer) {
                        ps.setInt(n + 1, ((Integer) value).intValue());
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
                BCCVO bccVO = new BCCVO();
                bccVO.setBCCID(rs.getInt("BCC_ID"));
                bccVO.setBCCGUID(rs.getString("GUID"));
                bccVO.setCardinalityMin(rs.getInt("Cardinality_Min"));
                bccVO.setCardinalityMax(rs.getInt("Cardinality_Max"));
                bccVO.setAssocToBCCPID(rs.getInt("To_BCCP_ID"));
                bccVO.setAssocFromACCID(rs.getInt("From_ACC_ID"));
                bccVO.setSequencingKey(rs.getInt("Seq_key"));
                bccVO.setEntityType(rs.getInt("Entity_Type"));
                bccVO.setDEN(rs.getString("DEN"));
                bccVO.setDefinition(rs.getString("Definition"));
                bccVO.setCreatedByUserId(rs.getInt("Created_By"));
                bccVO.setOwnerUserId(rs.getInt("owner_user_id"));
                bccVO.setLastUpdatedByUserId(rs.getInt("Last_Updated_By"));
                bccVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
                bccVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
                bccVO.setState(rs.getInt("State"));
                bccVO.setRevisionNum(rs.getInt("revision_num"));
                bccVO.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
                bccVO.setRevisionAction(rs.getInt("revision_action"));
                bccVO.setReleaseId(rs.getInt("release_id"));
                bccVO.setCurrentBccId(rs.getInt("current_bcc_id"));
                bccVO.setIs_deprecated(rs.getBoolean("is_deprecated"));
                list.add(bccVO);
            }
        } catch (SQLException e) {
            throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
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
