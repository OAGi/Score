package org.oagi.srt.persistence.dao.oracle;

import org.apache.commons.lang.StringUtils;
import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BBIEPVO;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Nasif Sikder
 * @version 1.0
 *
 */
@Repository
public class BBIEPOracleDAO extends SRTDAO {
	private final String _tableName = "bbiep";

	private final String _FIND_ALL_BBIEP_STATEMENT = "SELECT BBIEP_ID, GUID, Based_BCCP_ID, Definition, "
			+ "remark, biz_term, Created_By, Last_Updated_by, Creation_Timestamp, Last_Update_Timestamp FROM " 
			+ _tableName;
	
	private final String _FIND_MAX_ID_STATEMENT =
			"SELECT max(BBIEP_ID) as max FROM " + _tableName;
	
	private final String _FIND_BBIEP_STATEMENT = "SELECT BBIEP_ID, GUID, Based_BCCP_ID, Definition, "
			+ "remark, biz_term, Created_By, Last_Updated_by, Creation_Timestamp, Last_Update_Timestamp FROM " 
			+ _tableName;
	
	private final String _INSERT_BBIEP_STATEMENT = "INSERT INTO " + _tableName
			+ " (GUID, Based_BCCP_ID, Definition, remark, biz_term, Created_By, Last_Updated_by, "
			+ "Creation_Timestamp, Last_Update_Timestamp) VALUES (?, ?, ?, ?, ?,?,?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
	
	private final String _INSERT_BBIEP_WITH_ID_STATEMENT = "INSERT INTO " + _tableName
			+ " (GUID, Based_BCCP_ID, Definition, remark, biz_term, Created_By, Last_Updated_by, "
			+ "Creation_Timestamp, Last_Update_Timestamp, BBIEP_ID) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)";
	
	private final String _DELETE_BBIEP_STATEMENT = "DELETE FROM " + _tableName + " WHERE BBIEP_ID = ?";

    public int findMaxId() throws SRTDAOException {
        DBAgent tx = new DBAgent();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        int max = 1;
        try {
            conn = tx.open();
            String sql = _FIND_MAX_ID_STATEMENT;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next())
                max = rs.getInt("max");
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
        return max;
    }

    public int insertObject(SRTObject obj) throws SRTDAOException {
        DBAgent tx = new DBAgent();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        BBIEPVO bbiepVO = (BBIEPVO) obj;
        int key = -1;
        try {
            conn = tx.open();
            String keys[] = {"BBIEP_ID"};
            if (bbiepVO.getBBIEPID() < 1)
                ps = conn.prepareStatement(_INSERT_BBIEP_STATEMENT, keys);
            else
                ps = conn.prepareStatement(_INSERT_BBIEP_WITH_ID_STATEMENT, keys);

            if (bbiepVO.getBBIEPGUID() == null || bbiepVO.getBBIEPGUID().length() == 0 || bbiepVO.getBBIEPGUID().isEmpty() || bbiepVO.getBBIEPGUID().equals(""))
                ps.setString(1, "\u00A0");
            else
                ps.setString(1, bbiepVO.getBBIEPGUID());

            ps.setInt(2, bbiepVO.getBasedBCCPID());
//			if(bbiepVO.getDefinition()==null || bbiepVO.getDefinition().length()==0 || bbiepVO.getDefinition().isEmpty() || bbiepVO.getDefinition().equals("")){
//				ps.setString(3, "\u00A0");
//			}
//			else {
				String s = StringUtils.abbreviate(bbiepVO.getDefinition(), 4000);
				ps.setString(3, s);	
//			}
//			if( bbiepVO.getRemark()==null ||  bbiepVO.getRemark().length()==0 ||  bbiepVO.getRemark().isEmpty() ||  bbiepVO.getRemark().equals(""))				
//				ps.setString(4,"\u00A0");
//			else 	
				ps.setString(4, bbiepVO.getRemark());

//			if( bbiepVO.getBusinessTerm()==null ||  bbiepVO.getBusinessTerm().length()==0 ||  bbiepVO.getBusinessTerm().isEmpty() ||  bbiepVO.getBusinessTerm().equals(""))				
//				ps.setString(5,"\u00A0");
//			else 	
				ps.setString(5, bbiepVO.getBusinessTerm());

            ps.setInt(6, bbiepVO.getCreatedByUserID());
            ps.setInt(7, bbiepVO.getLastUpdatedbyUserID());

            if (bbiepVO.getBBIEPID() > 1)
                ps.setInt(8, bbiepVO.getBBIEPID());
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                key = (int) rs.getLong(1);
            }

            tx.commit();
        } catch (BfPersistenceException e) {
            tx.rollback();
            throw new SRTDAOException(SRTDAOException.DAO_INSERT_ERROR, e);
        } catch (SQLException e) {
            tx.rollback();
            throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
            closeQuietly(tx);
        }
        return key;
    }

    public int insertObject(SRTObject obj, Connection conn) throws SRTDAOException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        BBIEPVO bbiepVO = (BBIEPVO) obj;
        int key = -1;
        try {
            String keys[] = {"BBIEP_ID"};
            if (bbiepVO.getBBIEPID() < 1)
                ps = conn.prepareStatement(_INSERT_BBIEP_STATEMENT, keys);
            else
                ps = conn.prepareStatement(_INSERT_BBIEP_WITH_ID_STATEMENT, keys);

            if (bbiepVO.getBBIEPGUID() == null || bbiepVO.getBBIEPGUID().length() == 0 || bbiepVO.getBBIEPGUID().isEmpty() || bbiepVO.getBBIEPGUID().equals(""))
                ps.setString(1, "\u00A0");
            else
                ps.setString(1, bbiepVO.getBBIEPGUID());

            ps.setInt(2, bbiepVO.getBasedBCCPID());
//			if(bbiepVO.getDefinition()==null || bbiepVO.getDefinition().length()==0 || bbiepVO.getDefinition().isEmpty() || bbiepVO.getDefinition().equals("")){
//				ps.setString(3, "\u00A0");
//			}
//			else {
				String s = StringUtils.abbreviate(bbiepVO.getDefinition(), 4000);
				ps.setString(3, s);
//			}
//			if( bbiepVO.getRemark()==null ||  bbiepVO.getRemark().length()==0 ||  bbiepVO.getRemark().isEmpty() ||  bbiepVO.getRemark().equals(""))				
//				ps.setString(4,"\u00A0");
//			else 	
				ps.setString(4, bbiepVO.getRemark());

//			if( bbiepVO.getBusinessTerm()==null ||  bbiepVO.getBusinessTerm().length()==0 ||  bbiepVO.getBusinessTerm().isEmpty() ||  bbiepVO.getBusinessTerm().equals(""))				
//				ps.setString(5,"\u00A0");
//			else 	
				ps.setString(5, bbiepVO.getBusinessTerm());

            ps.setInt(6, bbiepVO.getCreatedByUserID());
            ps.setInt(7, bbiepVO.getLastUpdatedbyUserID());
            if (bbiepVO.getBBIEPID() > 1)
                ps.setInt(8, bbiepVO.getBBIEPID());

            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                key = (int) rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
        return key;
    }

    public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
        DBAgent tx = new DBAgent();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        BBIEPVO bbiepVO = new BBIEPVO();
        try {
            conn = tx.open();
            String sql = _FIND_BBIEP_STATEMENT;

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
                bbiepVO.setBBIEPID(rs.getInt("BBIEP_ID"));
                bbiepVO.setBBIEPGUID(rs.getString("GUID"));
                bbiepVO.setBasedBCCPID(rs.getInt("Based_BCCP_ID"));
                bbiepVO.setDefinition(rs.getString("Definition"));
                bbiepVO.setRemark(rs.getString("remark"));
                bbiepVO.setBusinessTerm(rs.getString("biz_term"));
                bbiepVO.setCreatedByUserID(rs.getInt("Created_By"));
                bbiepVO.setLastUpdatedbyUserID(rs.getInt("Last_Updated_by"));
                bbiepVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
                bbiepVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
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
        return bbiepVO;
    }

    public ArrayList<SRTObject> findObjects() throws SRTDAOException {
        DBAgent tx = new DBAgent();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        ArrayList<SRTObject> list = new ArrayList<SRTObject>();
        try {
            conn = tx.open();
            String sql = _FIND_ALL_BBIEP_STATEMENT;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                BBIEPVO bbiepVO = new BBIEPVO();
                bbiepVO.setBBIEPID(rs.getInt("BBIEP_ID"));
                bbiepVO.setBBIEPGUID(rs.getString("GUID"));
                bbiepVO.setBasedBCCPID(rs.getInt("Based_BCCP_ID"));
                bbiepVO.setDefinition(rs.getString("Definition"));
                bbiepVO.setRemark(rs.getString("remark"));
                bbiepVO.setBusinessTerm(rs.getString("biz_term"));
                bbiepVO.setCreatedByUserID(rs.getInt("Created_By"));
                bbiepVO.setLastUpdatedbyUserID(rs.getInt("Last_Updated_by"));
                bbiepVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
                bbiepVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
                list.add(bbiepVO);
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

    private final String _UPDATE_BBIEP_STATEMENT = "UPDATE " + _tableName + " SET "
			+ "Last_Update_Timestamp = CURRENT_TIMESTAMP, GUID = ?, Based_BCCP_ID = ?, Definition = ?, "
			+ "remark = ?, biz_term = ?, Created_By = ?, Last_Updated_by = ? WHERE BBIEP_ID = ?";

    public boolean updateObject(SRTObject obj) throws SRTDAOException {
        DBAgent tx = new DBAgent();
        Connection conn = null;
        PreparedStatement ps = null;

        BBIEPVO bbiepVO = (BBIEPVO) obj;
        try {
            conn = tx.open();

            ps = conn.prepareStatement(_UPDATE_BBIEP_STATEMENT);

            if (bbiepVO.getBBIEPGUID() == null || bbiepVO.getBBIEPGUID().length() == 0 || bbiepVO.getBBIEPGUID().isEmpty() || bbiepVO.getBBIEPGUID().equals(""))
                ps.setString(1, "\u00A0");
            else
                ps.setString(1, bbiepVO.getBBIEPGUID());

            ps.setInt(2, bbiepVO.getBasedBCCPID());
//			if( bbiepVO.getDefinition()==null ||  bbiepVO.getDefinition().length()==0 ||  bbiepVO.getDefinition().isEmpty() ||  bbiepVO.getDefinition().equals(""))				
//				ps.setString(3,"\u00A0");
//			else 	{
				String s = StringUtils.abbreviate(bbiepVO.getDefinition(), 4000);
				ps.setString(3,s);
//			}

//			if( bbiepVO.getRemark()==null ||  bbiepVO.getRemark().length()==0 ||  bbiepVO.getRemark().isEmpty() ||  bbiepVO.getRemark().equals(""))				
//				ps.setString(4,"\u00A0");
//			else 	
				ps.setString(4, bbiepVO.getRemark());

//			if( bbiepVO.getBusinessTerm()==null ||  bbiepVO.getBusinessTerm().length()==0 ||  bbiepVO.getBusinessTerm().isEmpty() ||  bbiepVO.getBusinessTerm().equals(""))				
//				ps.setString(5,"\u00A0");
//			else 	
				ps.setString(5, bbiepVO.getBusinessTerm());

            ps.setInt(6, bbiepVO.getCreatedByUserID());
            ps.setInt(7, bbiepVO.getLastUpdatedbyUserID());
            //ps.setTimestamp(8, bbiepVO.getCreationTimestamp());
            ps.setInt(8, bbiepVO.getBBIEPID());
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

        BBIEPVO bbiepVO = (BBIEPVO) obj;
        try {
            conn = tx.open();

            ps = conn.prepareStatement(_DELETE_BBIEP_STATEMENT);
            ps.setInt(1, bbiepVO.getBBIEPID());
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
            String sql = _FIND_BBIEP_STATEMENT;

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
                BBIEPVO bbiepVO = new BBIEPVO();
                bbiepVO.setBBIEPID(rs.getInt("BBIEP_ID"));
                bbiepVO.setBBIEPGUID(rs.getString("GUID"));
                bbiepVO.setBasedBCCPID(rs.getInt("Based_BCCP_ID"));
                bbiepVO.setDefinition(rs.getString("Definition"));
                bbiepVO.setRemark(rs.getString("remark"));
                bbiepVO.setBusinessTerm(rs.getString("biz_term"));
                bbiepVO.setCreatedByUserID(rs.getInt("Created_By"));
                bbiepVO.setLastUpdatedbyUserID(rs.getInt("Last_Updated_by"));
                bbiepVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
                bbiepVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
                list.add(bbiepVO);
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

    @Override
    public SRTObject findObject(QueryCondition qc, Connection conn)
            throws SRTDAOException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        BBIEPVO bbiepVO = null;

        try {
            String sql = _FIND_BBIEP_STATEMENT;

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
                bbiepVO = new BBIEPVO();
                bbiepVO.setBBIEPID(rs.getInt("BBIEP_ID"));
                bbiepVO.setBBIEPGUID(rs.getString("GUID"));
                bbiepVO.setBasedBCCPID(rs.getInt("Based_BCCP_ID"));
                bbiepVO.setDefinition(rs.getString("Definition"));
                bbiepVO.setRemark(rs.getString("remark"));
                bbiepVO.setBusinessTerm(rs.getString("biz_term"));
                bbiepVO.setCreatedByUserID(rs.getInt("Created_By"));
                bbiepVO.setLastUpdatedbyUserID(rs.getInt("Last_Updated_by"));
                bbiepVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
                bbiepVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
            }

        } catch (SQLException e) {
            throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
        return bbiepVO;
    }
}
