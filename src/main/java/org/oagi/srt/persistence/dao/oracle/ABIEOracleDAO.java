package org.oagi.srt.persistence.dao.oracle;

import org.apache.commons.lang.StringUtils;
import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ABIEVO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Nasif Sikder
 * @author Jaehun Lee
 * @version 1.1
 *
 */

public class ABIEOracleDAO extends SRTDAO {

	private final String _tableName = "abie";

	private final String _FIND_ALL_ABIE_STATEMENT =
			"SELECT ABIE_ID, GUID, Based_ACC_ID, is_top_level, biz_ctx_id, Definition, "
			+ "Created_By, Last_Updated_By, Creation_Timestamp, "
			+ "Last_Update_Timestamp, State, Client_ID, Version, Status, Remark, biz_term FROM " + _tableName + " order by Last_Update_Timestamp desc";

	private final String _FIND_MAX_ID_STATEMENT =
			"SELECT max(abie_id) as max FROM " + _tableName;

	private final String _FIND_ABIE_STATEMENT =
			"SELECT ABIE_ID, GUID, Based_ACC_ID, is_top_level, biz_ctx_id, Definition, "
					+ "Created_By, Last_Updated_By, Creation_Timestamp, "
					+ "Last_Update_Timestamp, State, Client_ID, Version, Status, Remark, biz_term FROM " + _tableName;

	private final String _INSERT_ABIE_STATEMENT =
			"INSERT INTO " + _tableName + " (GUID, Based_ACC_ID, is_top_level, biz_ctx_id, Definition, "
					+ "Created_By, Last_Updated_By, Creation_Timestamp, "
					+ "Last_Update_Timestamp, State, Client_ID, Version, Status, Remark, biz_term) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?, ?, ? ,?)";

	private final String _INSERT_ABIE_WITH_ID_STATEMENT =
			"INSERT INTO " + _tableName + " (GUID, Based_ACC_ID, is_top_level, biz_ctx_id, Definition, "
					+ "Created_By, Last_Updated_By, Creation_Timestamp, "
					+ "Last_Update_Timestamp, State, Client_ID, Version, Status, Remark, biz_term, ABIE_ID) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?, ?, ? ,?, ?)";

	private final String _DELETE_ABIE_STATEMENT =
			"DELETE FROM " + _tableName + " WHERE ABIE_ID = ?";

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

        ABIEVO abieVO = (ABIEVO) obj;
        int key = -1;
        try {
			String keys[] = {"ABIE_ID"};
			conn = tx.open();
			if(abieVO.getABIEID() < 1)
				ps = conn.prepareStatement(_INSERT_ABIE_STATEMENT, keys);
			else
				ps = conn.prepareStatement(_INSERT_ABIE_WITH_ID_STATEMENT, keys);
			if(abieVO.getAbieGUID()==null || abieVO.getAbieGUID().length()==0 || abieVO.getAbieGUID().isEmpty() ||abieVO.getAbieGUID().equals(""))
				ps.setString(1, "\u00A0");
			else
				ps.setString(1, abieVO.getAbieGUID());
			ps.setInt(2, abieVO.getBasedACCID());
			ps.setInt(3, abieVO.getIsTopLevel());
			ps.setInt(4, abieVO.getBusinessContextID());
//			if(abieVO.getDefinition()==null || abieVO.getDefinition().length()==0 || abieVO.getDefinition().isEmpty() ||abieVO.getDefinition().equals(""))
//				ps.setString(5, "");
//			else {
				String s = StringUtils.abbreviate(abieVO.getDefinition(), 4000);
				ps.setString(5, s);
//			}
			ps.setInt(6, abieVO.getCreatedByUserID());
			ps.setInt(7, abieVO.getLastUpdatedByUserID());
			if(abieVO.getState() < 1)
				ps.setNull(8, java.sql.Types.INTEGER);
			else
				ps.setInt(8, abieVO.getState());
			if(abieVO.getClientID() < 1)
				ps.setNull(9, java.sql.Types.INTEGER);
			else
				ps.setInt(9, abieVO.getClientID());

//			if( abieVO.getVersion()==null ||  abieVO.getVersion().length()==0 ||  abieVO.getVersion().isEmpty() ||  abieVO.getVersion().equals(""))				
//				ps.setString(10,"\u00A0");
//			else 	
				ps.setString(10, abieVO.getVersion());


//			if( abieVO.getStatus()==null ||  abieVO.getStatus().length()==0 ||  abieVO.getStatus().isEmpty() ||  abieVO.getStatus().equals(""))					
//				ps.setString(11,"\u00A0");	
//			else 		
				ps.setString(11, abieVO.getStatus());

//			if( abieVO.getRemark()==null ||  abieVO.getRemark().length()==0 ||  abieVO.getRemark().isEmpty() ||  abieVO.getRemark().equals(""))				
//				ps.setString(12,"\u00A0");
//			else 	
				ps.setString(12, abieVO.getRemark());

//			if( abieVO.getBusinessTerm()==null ||  abieVO.getBusinessTerm().length()==0 ||  abieVO.getBusinessTerm().isEmpty() ||  abieVO.getBusinessTerm().equals(""))					
//				ps.setString(13,"\u00A0");	
//			else 		
				ps.setString(13, abieVO.getBusinessTerm());

			if(abieVO.getABIEID() > 0)
				ps.setInt(14, abieVO.getABIEID());

			ps.executeUpdate();

			rs = ps.getGeneratedKeys();
			if (rs.next()){
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

        ABIEVO abieVO = (ABIEVO) obj;
        int key = -1;
        try {
            String[] keys = {"ABIE_ID"};
            if (abieVO.getABIEID() == 0)
                ps = conn.prepareStatement(_INSERT_ABIE_STATEMENT, keys);
            else
                ps = conn.prepareStatement(_INSERT_ABIE_WITH_ID_STATEMENT, keys);

            if (abieVO.getAbieGUID() == null || abieVO.getAbieGUID().length() == 0 || abieVO.getAbieGUID().isEmpty() || abieVO.getAbieGUID().equals(""))
                ps.setString(1, "\u00A0");
            else
                ps.setString(1, abieVO.getAbieGUID());

            ps.setInt(2, abieVO.getBasedACCID());
            ps.setInt(3, abieVO.getIsTopLevel());
            ps.setInt(4, abieVO.getBusinessContextID());
//			if(abieVO.getDefinition()==null || abieVO.getDefinition().length()==0 || abieVO.getDefinition().isEmpty() ||abieVO.getDefinition().equals(""))
//				ps.setString(5, "\u00A0");
//			else {
				String s = StringUtils.abbreviate(abieVO.getDefinition(), 4000);
				ps.setString(5, s);
//			}
            ps.setInt(6, abieVO.getCreatedByUserID());
            ps.setInt(7, abieVO.getLastUpdatedByUserID());
            if (abieVO.getState() < 1)
                ps.setNull(8, java.sql.Types.INTEGER);
            else
                ps.setInt(8, abieVO.getState());
            if (abieVO.getClientID() < 1)
                ps.setNull(9, java.sql.Types.INTEGER);
            else
                ps.setInt(9, abieVO.getClientID());


//			if( abieVO.getVersion()==null ||  abieVO.getVersion().length()==0 ||  abieVO.getVersion().isEmpty() ||  abieVO.getVersion().equals(""))				
//				ps.setString(10,"\u00A0");
//			else 	
				ps.setString(10, abieVO.getVersion());

//			if( abieVO.getStatus()==null ||  abieVO.getStatus().length()==0 ||  abieVO.getStatus().isEmpty() ||  abieVO.getStatus().equals(""))				
//				ps.setString(11,"\u00A0");
//			else 	
				ps.setString(11, abieVO.getStatus());

//			if( abieVO.getRemark()==null ||  abieVO.getRemark().length()==0 ||  abieVO.getRemark().isEmpty() ||  abieVO.getRemark().equals(""))				
//				ps.setString(12,"\u00A0");
//			else 	
				ps.setString(12, abieVO.getRemark());

//			if( abieVO.getBusinessTerm()==null ||  abieVO.getBusinessTerm().length()==0 ||  abieVO.getBusinessTerm().isEmpty() ||  abieVO.getBusinessTerm().equals(""))				
//				ps.setString(13,"\u00A0");
//			else 	
				ps.setString(13, abieVO.getBusinessTerm());

			if(abieVO.getABIEID() > 0)
				ps.setInt(14, abieVO.getABIEID());

            ps.executeUpdate();

            rs = ps.getGeneratedKeys();

            if (rs.next()) {
                key = (int) rs.getLong(1);
            }
        } catch (SQLException e) {
			e.printStackTrace();
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

        ABIEVO abieVO = new ABIEVO();
        try {
            conn = tx.open();
            String sql = _FIND_ABIE_STATEMENT;

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
                abieVO.setABIEID(rs.getInt("ABIE_ID"));
                abieVO.setBasedACCID(rs.getInt("Based_ACC_ID"));
                abieVO.setIsTopLevel(rs.getInt("is_top_level"));
                abieVO.setBusinessContextID(rs.getInt("biz_ctx_id"));
                abieVO.setDefinition(rs.getString("Definition"));
                abieVO.setCreatedByUserID(rs.getInt("Created_By"));
                abieVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
                abieVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
                abieVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
                abieVO.setState(rs.getInt("State"));
                abieVO.setAbieGUID(rs.getString("GUID"));
                abieVO.setClientID(rs.getInt("Client_ID"));
                abieVO.setVersion(rs.getString("Version"));
                abieVO.setStatus(rs.getString("Status"));
                abieVO.setRemark(rs.getString("Remark"));
                abieVO.setBusinessTerm(rs.getString("biz_term"));
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
        return abieVO;
    }

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
        DBAgent tx = new DBAgent();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			conn = tx.open();
			String sql = _FIND_ALL_ABIE_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ABIEVO abieVO = new ABIEVO();
				abieVO.setABIEID(rs.getInt("ABIE_ID"));
				abieVO.setBasedACCID(rs.getInt("Based_ACC_ID"));
				abieVO.setIsTopLevel(rs.getInt("is_top_level"));
				abieVO.setBusinessContextID(rs.getInt("biz_ctx_id"));
				abieVO.setDefinition(rs.getString("Definition"));
				abieVO.setCreatedByUserID(rs.getInt("Created_By"));
				abieVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
				abieVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				abieVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				abieVO.setState(rs.getInt("State"));
				abieVO.setAbieGUID(rs.getString("GUID"));
				abieVO.setClientID(rs.getInt("Client_ID"));
				abieVO.setVersion(rs.getString("Version"));
				abieVO.setStatus(rs.getString("Status"));
				abieVO.setRemark(rs.getString("Remark"));
				abieVO.setBusinessTerm(rs.getString("biz_term"));
				list.add(abieVO);
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

	private final String _UPDATE_ABIE_STATEMENT =
			"UPDATE " + _tableName
			+ " SET Based_ACC_ID = ?, is_top_level = ?,  "
			+ " biz_ctx_ID = ?, Definition = ?, Last_Update_Timestamp = CURRENT_TIMESTAMP, "
			+ " Last_Updated_By = ?, State = ?, GUID = ?, Client_ID = ?, Version = ?, Status = ?, Remark = ?, biz_term = ? WHERE ABIE_ID = ?";

	public boolean updateObject(SRTObject obj) throws SRTDAOException {
        DBAgent tx = new DBAgent();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        ABIEVO abieVO = (ABIEVO) obj;
        try {
			conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_ABIE_STATEMENT);

			ps.setInt(1, abieVO.getBasedACCID());
			ps.setInt(2, abieVO.getIsTopLevel());
			ps.setInt(3, abieVO.getBusinessContextID());
//			if(abieVO.getDefinition()==null || abieVO.getDefinition().length()==0 || abieVO.getDefinition().isEmpty() ||abieVO.getDefinition().equals(""))
//				ps.setString(4, "\u00A0");
//			else {
				String s = StringUtils.abbreviate(abieVO.getDefinition(), 4000);
				ps.setString(4, s);
//			}
            ps.setInt(5, abieVO.getLastUpdatedByUserID());
            ps.setInt(6, abieVO.getState());
            if (abieVO.getAbieGUID() == null || abieVO.getAbieGUID().length() == 0 || abieVO.getAbieGUID().isEmpty() || abieVO.getAbieGUID().equals(""))
                ps.setString(7, "\u00A0");
            else
                ps.setString(7, abieVO.getAbieGUID());

            if (abieVO.getClientID() < 1)
                ps.setInt(8, abieVO.getClientID());
            else
                ps.setNull(8, java.sql.Types.INTEGER);

//			if( abieVO.getVersion()==null ||  abieVO.getVersion().length()==0 ||  abieVO.getVersion().isEmpty() ||  abieVO.getVersion().equals(""))				
//				ps.setString(9,"\u00A0");
//			else 	
				ps.setString(9, abieVO.getVersion());

//			if( abieVO.getStatus()==null ||  abieVO.getStatus().length()==0 ||  abieVO.getStatus().isEmpty() ||  abieVO.getStatus().equals(""))				
//				ps.setString(10,"\u00A0");
//			else 	
				ps.setString(10, abieVO.getStatus());

//			if( abieVO.getRemark()==null ||  abieVO.getRemark().length()==0 ||  abieVO.getRemark().isEmpty() ||  abieVO.getRemark().equals(""))				
//				ps.setString(11,"\u00A0");
//			else 	
				ps.setString(11, abieVO.getRemark());

//			if( abieVO.getBusinessTerm()==null ||  abieVO.getBusinessTerm().length()==0 ||  abieVO.getBusinessTerm().isEmpty() ||  abieVO.getBusinessTerm().equals(""))				
//				ps.setString(12,"\u00A0");
//			else 	
				ps.setString(12, abieVO.getBusinessTerm());

            ps.setInt(13, abieVO.getABIEID());

            ps.executeUpdate();

            tx.commit();
        } catch (BfPersistenceException e) {
			tx.rollback(e);
			throw new SRTDAOException(SRTDAOException.DAO_UPDATE_ERROR, e);
		} catch (SQLException e) {
			tx.rollback(e);
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
            closeQuietly(rs);
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

		ABIEVO abieVO = (ABIEVO)obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_DELETE_ABIE_STATEMENT);
			ps.setInt(1, abieVO.getABIEID());
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
            String sql = _FIND_ABIE_STATEMENT;

            String WHERE_OR_AND = " WHERE ";
            int nCond = qc.getSize();
            if (nCond > 0) {
                for (int n = 0; n < nCond; n++) {
                    sql += WHERE_OR_AND + qc.getField(n) + " = ?";
                    WHERE_OR_AND = " AND ";
                }
            }

            sql += " order by Last_Update_Timestamp desc";

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
                ABIEVO abieVO = new ABIEVO();
                abieVO.setABIEID(rs.getInt("ABIE_ID"));
                abieVO.setBasedACCID(rs.getInt("Based_ACC_ID"));
                abieVO.setIsTopLevel(rs.getInt("is_top_level"));
                abieVO.setBusinessContextID(rs.getInt("biz_ctx_id"));
                abieVO.setDefinition(rs.getString("Definition"));
                abieVO.setCreatedByUserID(rs.getInt("Created_By"));
                abieVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
                abieVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
                abieVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
                abieVO.setState(rs.getInt("State"));
                abieVO.setAbieGUID(rs.getString("GUID"));
                abieVO.setClientID(rs.getInt("Client_ID"));
                abieVO.setVersion(rs.getString("Version"));
                abieVO.setStatus(rs.getString("Status"));
                abieVO.setRemark(rs.getString("Remark"));
                abieVO.setBusinessTerm(rs.getString("biz_term"));
                list.add(abieVO);
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
	public SRTObject findObject(QueryCondition qc, Connection conn)
			throws SRTDAOException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ABIEVO abieVO = null;
        try {
            String sql = _FIND_ABIE_STATEMENT;

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
                abieVO = new ABIEVO();
                abieVO.setABIEID(rs.getInt("ABIE_ID"));
                abieVO.setBasedACCID(rs.getInt("Based_ACC_ID"));
                abieVO.setIsTopLevel(rs.getInt("is_top_level"));
                abieVO.setBusinessContextID(rs.getInt("biz_ctx_id"));
                abieVO.setDefinition(rs.getString("Definition"));
                abieVO.setCreatedByUserID(rs.getInt("Created_By"));
                abieVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
                abieVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
                abieVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
                abieVO.setState(rs.getInt("State"));
                abieVO.setAbieGUID(rs.getString("GUID"));
                abieVO.setClientID(rs.getInt("Client_ID"));
                abieVO.setVersion(rs.getString("Version"));
                abieVO.setStatus(rs.getString("Status"));
                abieVO.setRemark(rs.getString("Remark"));
                abieVO.setBusinessTerm(rs.getString("biz_term"));
            }

        } catch (SQLException e) {
            throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
        return abieVO;
	}

	@Override
	public ArrayList<SRTObject> findObjects(QueryCondition qc, Connection conn)
			throws SRTDAOException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ArrayList<SRTObject> list = new ArrayList<SRTObject>();
        try {
            String sql = _FIND_ABIE_STATEMENT;

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
                ABIEVO abieVO = new ABIEVO();
                abieVO.setABIEID(rs.getInt("ABIE_ID"));
                abieVO.setBasedACCID(rs.getInt("Based_ACC_ID"));
                abieVO.setIsTopLevel(rs.getInt("is_top_level"));
                abieVO.setBusinessContextID(rs.getInt("biz_ctx_id"));
                abieVO.setDefinition(rs.getString("Definition"));
                abieVO.setCreatedByUserID(rs.getInt("Created_By"));
                abieVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By"));
                abieVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
                abieVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
                abieVO.setState(rs.getInt("State"));
                abieVO.setAbieGUID(rs.getString("GUID"));
                abieVO.setClientID(rs.getInt("Client_ID"));
                abieVO.setVersion(rs.getString("Version"));
                abieVO.setStatus(rs.getString("Status"));
                abieVO.setRemark(rs.getString("Remark"));
                abieVO.setBusinessTerm(rs.getString("biz_term"));
                list.add(abieVO);
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
	public ArrayList<SRTObject> findObjects(Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return null;
	}

}
