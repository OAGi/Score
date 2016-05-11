package org.oagi.srt.persistence.dao.oracle;

import org.apache.commons.lang.StringUtils;
import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BBIE_SCVO;
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
public class BBIE_SCOracleDAO extends SRTDAO {
	
	private final String _tableName = "bbie_sc";

	private final String _FIND_ALL_BBIE_SC_STATEMENT = "SELECT BBIE_SC_ID, BBIE_ID, DT_SC_ID, "
			+ "DT_SC_Pri_Restri_ID, Code_List_ID, Agency_ID_List_ID, Min_Cardinality, Max_Cardinality, Default_value, Fixed_Value, Definition, Remark, biz_Term, is_used FROM " + _tableName;
	
	private final String _FIND_BBIE_SC_STATEMENT = "SELECT BBIE_SC_ID, BBIE_ID, DT_SC_ID, "
			+ "DT_SC_Pri_Restri_ID, Code_List_ID, Agency_ID_List_ID, Min_Cardinality, Max_Cardinality, Default_value, Fixed_Value, Definition, Remark, biz_Term, is_used FROM " + _tableName;
	
	private final String _INSERT_BBIE_SC_STATEMENT = "INSERT INTO " + _tableName + " (BBIE_ID, "
			+ "DT_SC_ID, DT_SC_Pri_Restri_ID, Code_List_ID, Agency_ID_List_ID, Min_Cardinality, Max_Cardinality, Default_value, Fixed_Value, Definition, Remark, biz_Term) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private final String _INSERT_BBIE_SC_WITH_ID_STATEMENT = "INSERT INTO " + _tableName + " (BBIE_ID, "
			+ "DT_SC_ID, DT_SC_Pri_Restri_ID, Code_List_ID, Agency_ID_List_ID, Min_Cardinality, Max_Cardinality, Default_value, Fixed_Value, Definition, Remark, biz_Term, BBIE_SC_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private final String _DELETE_BBIE_SC_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE BBIE_SC_ID = ?";

    public int insertObject(SRTObject obj) throws SRTDAOException {
        DBAgent tx = new DBAgent();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        BBIE_SCVO bbie_scVO = (BBIE_SCVO) obj;
        int key = -1;
        try {
            String keys[] = {"BBIE_SC_ID"};
            conn = tx.open();
            if (bbie_scVO.getBBIESCID() < 1)
                ps = conn.prepareStatement(_INSERT_BBIE_SC_STATEMENT, keys);
            else
                ps = conn.prepareStatement(_INSERT_BBIE_SC_WITH_ID_STATEMENT, keys);


            ps.setInt(1, bbie_scVO.getBBIEID());
            ps.setInt(2, bbie_scVO.getDTSCID());

            if (bbie_scVO.getDTSCPrimitiveRestrictionID() < 1)
                ps.setNull(3, java.sql.Types.INTEGER);
            else
                ps.setInt(3, bbie_scVO.getDTSCPrimitiveRestrictionID());

            if (bbie_scVO.getCodeListId() < 1)
                ps.setNull(4, java.sql.Types.INTEGER);
            else
                ps.setInt(4, bbie_scVO.getCodeListId());

            if (bbie_scVO.getAgencyIdListId() < 1)
                ps.setNull(5, java.sql.Types.INTEGER);
            else
                ps.setInt(5, bbie_scVO.getAgencyIdListId());

            ps.setInt(6, bbie_scVO.getMinCardinality());
            ps.setInt(7, bbie_scVO.getMaxCardinality());
//			if( bbie_scVO.getDefaultText()==null ||  bbie_scVO.getDefaultText().length()==0 ||  bbie_scVO.getDefaultText().isEmpty() ||  bbie_scVO.getDefaultText().equals(""))				
//				ps.setString(8,"\u00A0");
//			else 	
				ps.setString(8, bbie_scVO.getDefaultText());

//			if( bbie_scVO.getFixedValue()==null ||  bbie_scVO.getFixedValue().length()==0 ||  bbie_scVO.getFixedValue().isEmpty() ||  bbie_scVO.getFixedValue().equals(""))				
//				ps.setString(9,"\u00A0");
//			else 	
				ps.setString(9, bbie_scVO.getFixedValue());

//			if(bbie_scVO.getDefinition()==null || bbie_scVO.getDefinition().length()==0 || bbie_scVO.getDefinition().isEmpty() || bbie_scVO.getDefinition().equals("")){
//				ps.setString(10, "\u00A0");
//			}
//			else {
				String s = StringUtils.abbreviate(bbie_scVO.getDefinition(), 4000);
				ps.setString(10, s);
//			}
//			if( bbie_scVO.getRemark()==null ||  bbie_scVO.getRemark().length()==0 ||  bbie_scVO.getRemark().isEmpty() ||  bbie_scVO.getRemark().equals(""))				
//				ps.setString(11,"\u00A0");
//			else 	
				ps.setString(11, bbie_scVO.getRemark());

//			if( bbie_scVO.getBusinessTerm()==null ||  bbie_scVO.getBusinessTerm().length()==0 ||  bbie_scVO.getBusinessTerm().isEmpty() ||  bbie_scVO.getBusinessTerm().equals(""))				
//				ps.setString(12,"\u00A0");
//			else 	
				ps.setString(12, bbie_scVO.getBusinessTerm());

            if (bbie_scVO.getBBIESCID() > 0)
                ps.setInt(13, bbie_scVO.getBBIESCID());
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

        BBIE_SCVO bbie_scVO = (BBIE_SCVO) obj;
        int key = -1;
        try {
            String keys[] = {"BBIE_SC_ID"};
            if (bbie_scVO.getBBIESCID() < 1)
                ps = conn.prepareStatement(_INSERT_BBIE_SC_STATEMENT, keys);
            else
                ps = conn.prepareStatement(_INSERT_BBIE_SC_WITH_ID_STATEMENT, keys);


            ps.setInt(1, bbie_scVO.getBBIEID());
            ps.setInt(2, bbie_scVO.getDTSCID());

            if (bbie_scVO.getDTSCPrimitiveRestrictionID() < 1)
                ps.setNull(3, java.sql.Types.INTEGER);
            else
                ps.setInt(3, bbie_scVO.getDTSCPrimitiveRestrictionID());

            if (bbie_scVO.getCodeListId() < 1)
                ps.setNull(4, java.sql.Types.INTEGER);
            else
                ps.setInt(4, bbie_scVO.getCodeListId());

            if (bbie_scVO.getAgencyIdListId() < 1)
                ps.setNull(5, java.sql.Types.INTEGER);
            else
                ps.setInt(5, bbie_scVO.getAgencyIdListId());

            ps.setInt(6, bbie_scVO.getMinCardinality());
            ps.setInt(7, bbie_scVO.getMaxCardinality());
//			if( bbie_scVO.getDefaultText()==null ||  bbie_scVO.getDefaultText().length()==0 ||  bbie_scVO.getDefaultText().isEmpty() ||  bbie_scVO.getDefaultText().equals(""))				
//				ps.setString(8,"\u00A0");
//			else 	
				ps.setString(8, bbie_scVO.getDefaultText());

//			if( bbie_scVO.getFixedValue()==null ||  bbie_scVO.getFixedValue().length()==0 ||  bbie_scVO.getFixedValue().isEmpty() ||  bbie_scVO.getFixedValue().equals(""))				
//				ps.setString(9,"\u00A0");
//			else 	
				ps.setString(9, bbie_scVO.getFixedValue());

//			if(bbie_scVO.getDefinition()==null || bbie_scVO.getDefinition().length()==0 || bbie_scVO.getDefinition().isEmpty() || bbie_scVO.getDefinition().equals("")){
//				ps.setString(10, "\u00A0");
//			}
//			else {
				String s = StringUtils.abbreviate(bbie_scVO.getDefinition(), 4000);
				ps.setString(10, s);
//			}
//			if( bbie_scVO.getRemark()==null ||  bbie_scVO.getRemark().length()==0 ||  bbie_scVO.getRemark().isEmpty() ||  bbie_scVO.getRemark().equals(""))				
//				ps.setString(11,"\u00A0");
//			else 	
				ps.setString(11, bbie_scVO.getRemark());

//			if( bbie_scVO.getBusinessTerm()==null ||  bbie_scVO.getBusinessTerm().length()==0 ||  bbie_scVO.getBusinessTerm().isEmpty() ||  bbie_scVO.getBusinessTerm().equals(""))				
//				ps.setString(12,"\u00A0");
//			else 	
				ps.setString(12, bbie_scVO.getBusinessTerm());


            if (bbie_scVO.getBBIESCID() > 0)
                ps.setInt(13, bbie_scVO.getBBIESCID());

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

        BBIE_SCVO bbie_scVO = new BBIE_SCVO();
        try {
            conn = tx.open();
            String sql = _FIND_BBIE_SC_STATEMENT;

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
                bbie_scVO.setBBIESCID(rs.getInt("BBIE_SC_ID"));
                bbie_scVO.setBBIEID(rs.getInt("BBIE_ID"));
                bbie_scVO.setDTSCID(rs.getInt("DT_SC_ID"));
                bbie_scVO.setDTSCPrimitiveRestrictionID(rs.getInt("DT_SC_Pri_Restri_ID"));
                bbie_scVO.setCodeListId(rs.getInt("Code_List_ID"));
                bbie_scVO.setAgencyIdListId(rs.getInt("Agency_ID_List_ID"));
                bbie_scVO.setMinCardinality(rs.getInt("Min_Cardinality"));
                bbie_scVO.setMaxCardinality(rs.getInt("Max_Cardinality"));
                bbie_scVO.setDefaultText(rs.getString("Default_value"));
                bbie_scVO.setFixedValue(rs.getString("Fixed_Value"));
                bbie_scVO.setDefinition(rs.getString("Definition"));
                bbie_scVO.setRemark(rs.getString("Remark"));
                bbie_scVO.setBusinessTerm(rs.getString("biz_term"));
                bbie_scVO.setUsed(rs.getInt("is_used") == 1 ? true : false);
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
        return bbie_scVO;
    }

    public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        BBIE_SCVO bbie_scVO = new BBIE_SCVO();
        try {
            String sql = _FIND_BBIE_SC_STATEMENT;

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
                bbie_scVO.setBBIESCID(rs.getInt("BBIE_SC_ID"));
                bbie_scVO.setBBIEID(rs.getInt("BBIE_ID"));
                bbie_scVO.setDTSCID(rs.getInt("DT_SC_ID"));
                bbie_scVO.setDTSCPrimitiveRestrictionID(rs.getInt("DT_SC_Pri_Restri_ID"));
                bbie_scVO.setCodeListId(rs.getInt("Code_List_ID"));
                bbie_scVO.setAgencyIdListId(rs.getInt("Agency_ID_List_ID"));
                bbie_scVO.setMinCardinality(rs.getInt("Min_Cardinality"));
                bbie_scVO.setMaxCardinality(rs.getInt("Max_Cardinality"));
                bbie_scVO.setDefaultText(rs.getString("Default_value"));
                bbie_scVO.setFixedValue(rs.getString("Fixed_Value"));
                bbie_scVO.setDefinition(rs.getString("Definition"));
                bbie_scVO.setRemark(rs.getString("Remark"));
                bbie_scVO.setBusinessTerm(rs.getString("biz_term"));
                bbie_scVO.setUsed(rs.getInt("is_used") == 1 ? true : false);
            }
        } catch (SQLException e) {
            throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
        return bbie_scVO;
    }

    @Override
    public ArrayList<SRTObject> findObjects() throws SRTDAOException {
        DBAgent tx = new DBAgent();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        ArrayList<SRTObject> list = new ArrayList<SRTObject>();
        try {
            conn = tx.open();
            String sql = _FIND_BBIE_SC_STATEMENT;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                BBIE_SCVO bbie_scVO = new BBIE_SCVO();
                bbie_scVO.setBBIESCID(rs.getInt("BBIE_SC_ID"));
                bbie_scVO.setBBIEID(rs.getInt("BBIE_ID"));
                bbie_scVO.setDTSCID(rs.getInt("DT_SC_ID"));
                bbie_scVO.setDTSCPrimitiveRestrictionID(rs.getInt("DT_SC_Pri_Restri_ID"));
                bbie_scVO.setCodeListId(rs.getInt("Code_List_ID"));
                bbie_scVO.setAgencyIdListId(rs.getInt("Agency_ID_List_ID"));
                bbie_scVO.setMinCardinality(rs.getInt("Min_Cardinality"));
                bbie_scVO.setMaxCardinality(rs.getInt("Max_Cardinality"));
                bbie_scVO.setDefaultText(rs.getString("Default_value"));
                bbie_scVO.setFixedValue(rs.getString("Fixed_Value"));
                bbie_scVO.setDefinition(rs.getString("Definition"));
                bbie_scVO.setRemark(rs.getString("Remark"));
                bbie_scVO.setBusinessTerm(rs.getString("biz_term"));
                bbie_scVO.setUsed(rs.getInt("is_used") == 1 ? true : false);
                list.add(bbie_scVO);
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
            String sql = _FIND_BBIE_SC_STATEMENT;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                BBIE_SCVO bbie_scVO = new BBIE_SCVO();
                bbie_scVO.setBBIESCID(rs.getInt("BBIE_SC_ID"));
                bbie_scVO.setBBIEID(rs.getInt("BBIE_ID"));
                bbie_scVO.setDTSCID(rs.getInt("DT_SC_ID"));
                bbie_scVO.setDTSCPrimitiveRestrictionID(rs.getInt("DT_SC_Pri_Restri_ID"));
                bbie_scVO.setCodeListId(rs.getInt("Code_List_ID"));
                bbie_scVO.setAgencyIdListId(rs.getInt("Agency_ID_List_ID"));
                bbie_scVO.setMinCardinality(rs.getInt("Min_Cardinality"));
                bbie_scVO.setMaxCardinality(rs.getInt("Max_Cardinality"));
                bbie_scVO.setDefaultText(rs.getString("Default_value"));
                bbie_scVO.setFixedValue(rs.getString("Fixed_Value"));
                bbie_scVO.setDefinition(rs.getString("Definition"));
                bbie_scVO.setRemark(rs.getString("Remark"));
                bbie_scVO.setBusinessTerm(rs.getString("biz_term"));
                bbie_scVO.setUsed(rs.getInt("is_used") == 1 ? true : false);
                list.add(bbie_scVO);
            }
        } catch (SQLException e) {
            throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }

        return list;
    }

    private final String _UPDATE_BBIE_SC_STATEMENT = "UPDATE " + _tableName + " SET BBIE_ID = ?, "
            + "DT_SC_ID = ?, DT_SC_Pri_Restri_ID = ?, Code_List_ID = ?, Agency_ID_List_ID = ?, Min_Cardinality = ?, Max_Cardinality = ?,  "
            + "Default_Value = ?, Fixed_Value = ?, Definition = ?, Remark = ?, biz_term = ?, is_used = ? WHERE BBIE_SC_ID = ?";

    public boolean updateObject(SRTObject obj) throws SRTDAOException {
        DBAgent tx = new DBAgent();
        Connection conn = null;
        PreparedStatement ps = null;

        BBIE_SCVO bbie_scVO = (BBIE_SCVO) obj;
        try {
            conn = tx.open();

            ps = conn.prepareStatement(_UPDATE_BBIE_SC_STATEMENT);

            ps.setInt(1, bbie_scVO.getBBIEID());
            ps.setInt(2, bbie_scVO.getDTSCID());

            if (bbie_scVO.getDTSCPrimitiveRestrictionID() < 1)
                ps.setNull(3, java.sql.Types.INTEGER);
            else
                ps.setInt(3, bbie_scVO.getDTSCPrimitiveRestrictionID());

            if (bbie_scVO.getCodeListId() < 1)
                ps.setNull(4, java.sql.Types.INTEGER);
            else
                ps.setInt(4, bbie_scVO.getCodeListId());

            if (bbie_scVO.getAgencyIdListId() < 1)
                ps.setNull(5, java.sql.Types.INTEGER);
            else
                ps.setInt(5, bbie_scVO.getAgencyIdListId());

            ps.setInt(6, bbie_scVO.getMinCardinality());
            ps.setInt(7, bbie_scVO.getMaxCardinality());
//			if( bbie_scVO.getDefaultText()==null ||  bbie_scVO.getDefaultText().length()==0 ||  bbie_scVO.getDefaultText().isEmpty() ||  bbie_scVO.getDefaultText().equals(""))				
//				ps.setString(8,"\u00A0");
//			else 	
				ps.setString(8, bbie_scVO.getDefaultText());

//			if( bbie_scVO.getFixedValue()==null ||  bbie_scVO.getFixedValue().length()==0 ||  bbie_scVO.getFixedValue().isEmpty() ||  bbie_scVO.getFixedValue().equals(""))				
//				ps.setString(9,"\u00A0");
//			else 	
				ps.setString(9, bbie_scVO.getFixedValue());

//			if( bbie_scVO.getDefinition()==null ||  bbie_scVO.getDefinition().length()==0 ||  bbie_scVO.getDefinition().isEmpty() ||  bbie_scVO.getDefinition().equals(""))				
//				ps.setString(10,"\u00A0");
//			else 	{
				String s = StringUtils.abbreviate(bbie_scVO.getDefinition(), 4000);
				ps.setString(10, s);
//			}

//			if( bbie_scVO.getRemark()==null ||  bbie_scVO.getRemark().length()==0 ||  bbie_scVO.getRemark().isEmpty() ||  bbie_scVO.getRemark().equals(""))				
//				ps.setString(11,"\u00A0");
//			else 	
				ps.setString(11, bbie_scVO.getRemark());

//			if( bbie_scVO.getBusinessTerm()==null ||  bbie_scVO.getBusinessTerm().length()==0 ||  bbie_scVO.getBusinessTerm().isEmpty() ||  bbie_scVO.getBusinessTerm().equals(""))				
//				ps.setString(12,"\u00A0");
//			else 	
				ps.setString(12, bbie_scVO.getBusinessTerm());
				
			ps.setInt(13, bbie_scVO.isUsed() ? 1 : 0);

            if (bbie_scVO.getBBIESCID() > 0)
                ps.setInt(14, bbie_scVO.getBBIESCID());
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

        BBIE_SCVO bbie_scVO = (BBIE_SCVO) obj;
        try {
            conn = tx.open();

            ps = conn.prepareStatement(_DELETE_BBIE_SC_STATEMENT);
            ps.setInt(1, bbie_scVO.getBBIESCID());
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
            String sql = _FIND_BBIE_SC_STATEMENT;

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
                BBIE_SCVO bbie_scVO = new BBIE_SCVO();
                bbie_scVO.setBBIESCID(rs.getInt("BBIE_SC_ID"));
                bbie_scVO.setBBIEID(rs.getInt("BBIE_ID"));
                bbie_scVO.setDTSCID(rs.getInt("DT_SC_ID"));
                bbie_scVO.setDTSCPrimitiveRestrictionID(rs.getInt("DT_SC_Pri_Restri_ID"));
                bbie_scVO.setCodeListId(rs.getInt("Code_List_ID"));
                bbie_scVO.setAgencyIdListId(rs.getInt("Agency_ID_List_ID"));
                bbie_scVO.setMinCardinality(rs.getInt("Min_Cardinality"));
                bbie_scVO.setMaxCardinality(rs.getInt("Max_Cardinality"));
                bbie_scVO.setDefaultText(rs.getString("Default_value"));
                bbie_scVO.setFixedValue(rs.getString("Fixed_Value"));
                bbie_scVO.setDefinition(rs.getString("Definition"));
                bbie_scVO.setRemark(rs.getString("Remark"));
                bbie_scVO.setBusinessTerm(rs.getString("biz_term"));
                bbie_scVO.setUsed(rs.getInt("is_used") == 1 ? true : false);
                list.add(bbie_scVO);
            }
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
    public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn)
            throws SRTDAOException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ArrayList<SRTObject> list = new ArrayList<SRTObject>();
        try {
            String sql = _FIND_BBIE_SC_STATEMENT;

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
                BBIE_SCVO bbie_scVO = new BBIE_SCVO();
                bbie_scVO.setBBIESCID(rs.getInt("BBIE_SC_ID"));
                bbie_scVO.setBBIEID(rs.getInt("BBIE_ID"));
                bbie_scVO.setDTSCID(rs.getInt("DT_SC_ID"));
                bbie_scVO.setDTSCPrimitiveRestrictionID(rs.getInt("DT_SC_Pri_Restri_ID"));
                bbie_scVO.setCodeListId(rs.getInt("Code_List_ID"));
                bbie_scVO.setAgencyIdListId(rs.getInt("Agency_ID_List_ID"));
                bbie_scVO.setMinCardinality(rs.getInt("Min_Cardinality"));
                bbie_scVO.setMaxCardinality(rs.getInt("Max_Cardinality"));
                bbie_scVO.setDefaultText(rs.getString("Default_value"));
                bbie_scVO.setFixedValue(rs.getString("Fixed_Value"));
                bbie_scVO.setDefinition(rs.getString("Definition"));
                bbie_scVO.setRemark(rs.getString("Remark"));
                bbie_scVO.setBusinessTerm(rs.getString("biz_term"));
                bbie_scVO.setUsed(rs.getInt("is_used") == 1 ? true : false);
                list.add(bbie_scVO);
            }

        } catch (SQLException e) {
            throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
        return list;
    }

}
