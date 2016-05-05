package org.oagi.srt.persistence.dao.oracle;

import org.apache.commons.lang.StringUtils;
import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.DTSCVO;

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
public class DTSCOracleDAO extends SRTDAO {
	private final String _tableName = "dt_sc";

	private final String _FIND_ALL_DT_SC_STATEMENT = 
			"SELECT DT_SC_ID, guid, Property_Term, Representation_Term, Definition, "
					+ "Owner_DT_ID, Min_Cardinality, Max_Cardinality, Based_DT_SC_ID FROM " + _tableName;

	private final String _FIND_DT_SC_STATEMENT = 
			"SELECT DT_SC_ID, guid, Property_Term, Representation_Term, Definition, "
					+ "Owner_DT_ID, Min_Cardinality, Max_Cardinality, Based_DT_SC_ID FROM " + _tableName;
	
	private final String _INSERT_DT_SC_STATEMENT = 
			"INSERT INTO " + _tableName + " (guid, Property_Term, Representation_Term, Definition, "
					+ "Owner_DT_ID, Min_Cardinality, Max_Cardinality, Based_DT_SC_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	private final String _UPDATE_DT_SC_STATEMENT = 
			"UPDATE " + _tableName
			+ " SET guid = ?, Property_Term = ?, Representation_Term = ?, Definition = ?, "
			+ "Owner_DT_ID = ?, Min_Cardinality = ?, Max_Cardinality = ?, Based_DT_SC_ID = ? WHERE DT_SC_ID = ?";

	private final String _DELETE_DT_SC_STATEMENT = 
			"DELETE FROM " + _tableName + " WHERE DT_SC_ID = ?";

	@Override
	public int findMaxId() throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
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
			String sql = _FIND_DT_SC_STATEMENT;

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
				DTSCVO dtscVO = new DTSCVO();
				dtscVO.setDTSCID(rs.getInt("DT_SC_ID"));
				dtscVO.setDTSCGUID(rs.getString("guid"));
				dtscVO.setPropertyTerm(rs.getString("Property_Term"));
				dtscVO.setRepresentationTerm(rs.getString("Representation_Term"));
				dtscVO.setDefinition(rs.getString("Definition"));
				dtscVO.setOwnerDTID(rs.getInt("Owner_DT_ID"));
				dtscVO.setMinCardinality(rs.getInt("Min_Cardinality"));
				dtscVO.setMaxCardinality(rs.getInt("Max_Cardinality"));
				dtscVO.setBasedDTSCID(rs.getInt("Based_DT_SC_ID"));
				list.add(dtscVO);
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
			//conn = tx.open();
			String sql = _FIND_DT_SC_STATEMENT;

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
				DTSCVO dtscVO = new DTSCVO();
				dtscVO.setDTSCID(rs.getInt("DT_SC_ID"));
				dtscVO.setDTSCGUID(rs.getString("guid"));
				dtscVO.setPropertyTerm(rs.getString("Property_Term"));
				dtscVO.setRepresentationTerm(rs.getString("Representation_Term"));
				dtscVO.setDefinition(rs.getString("Definition"));
				dtscVO.setOwnerDTID(rs.getInt("Owner_DT_ID"));
				dtscVO.setMinCardinality(rs.getInt("Min_Cardinality"));
				dtscVO.setMaxCardinality(rs.getInt("Max_Cardinality"));
				dtscVO.setBasedDTSCID(rs.getInt("Based_DT_SC_ID"));
				list.add(dtscVO);
			}
		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(rs);
			closeQuietly(ps);
		}

		return list;
	}

	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;

		DTSCVO dtscVO = (DTSCVO) obj;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_DT_SC_STATEMENT);
			if (dtscVO.getDTSCGUID() == null || dtscVO.getDTSCGUID().length() == 0 || dtscVO.getDTSCGUID().isEmpty() || dtscVO.getDTSCGUID().equals(""))
				ps.setString(1, "**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			else
				ps.setString(1, dtscVO.getDTSCGUID());

//			if( dtscVO.getPropertyTerm()==null ||  dtscVO.getPropertyTerm().length()==0 ||  dtscVO.getPropertyTerm().isEmpty() ||  dtscVO.getPropertyTerm().equals(""))				
//				ps.setString(2,"\u00A0");
//			else 	
				ps.setString(2, dtscVO.getPropertyTerm());

//			if( dtscVO.getRepresentationTerm()==null ||  dtscVO.getRepresentationTerm().length()==0 ||  dtscVO.getRepresentationTerm().isEmpty() ||  dtscVO.getRepresentationTerm().equals(""))				
//				ps.setString(3,"\u00A0");
//			else 	
				ps.setString(3, dtscVO.getRepresentationTerm());

//			if(dtscVO.getDefinition()==null || dtscVO.getDefinition().length()==0 || dtscVO.getDefinition().isEmpty() || dtscVO.getDefinition().equals("")){
//				ps.setString(4, "\u00A0");
//			}
//			else {
				String s = StringUtils.abbreviate(dtscVO.getDefinition(), 4000);
				ps.setString(4, s);
//			}
			ps.setInt(5, dtscVO.getOwnerDTID());
			ps.setInt(6, dtscVO.getMinCardinality());
			ps.setInt(7, dtscVO.getMaxCardinality());

			if (dtscVO.getBasedDTSCID() > 0) {
				ps.setInt(8, dtscVO.getBasedDTSCID());
			} else {
				ps.setNull(8, java.sql.Types.INTEGER);
			}

			ps.executeUpdate();

			//ResultSet tableKeys = ps.getGeneratedKeys();
			//tableKeys.next();
			//int autoGeneratedID = tableKeys.getInt(1);
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

		DTSCVO dtscVO = new DTSCVO();
		try {
			conn = tx.open();
			String sql = _FIND_DT_SC_STATEMENT;

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
				dtscVO.setDTSCID(rs.getInt("DT_SC_ID"));
				dtscVO.setDTSCGUID(rs.getString("guid"));
				dtscVO.setPropertyTerm(rs.getString("Property_Term"));
				dtscVO.setRepresentationTerm(rs.getString("Representation_Term"));
				dtscVO.setDefinition(rs.getString("Definition"));
				dtscVO.setOwnerDTID(rs.getInt("Owner_DT_ID"));
				dtscVO.setMinCardinality(rs.getInt("Min_Cardinality"));
				dtscVO.setMaxCardinality(rs.getInt("Max_Cardinality"));
				dtscVO.setBasedDTSCID(rs.getInt("Based_DT_SC_ID"));
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
		return dtscVO;
	}

	public SRTObject findObject(QueryCondition qc, Connection conn) throws SRTDAOException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		DTSCVO dtscVO = null;
		try {
			String sql = _FIND_DT_SC_STATEMENT;

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
				dtscVO = new DTSCVO();
				dtscVO.setDTSCID(rs.getInt("DT_SC_ID"));
				dtscVO.setDTSCGUID(rs.getString("guid"));
				dtscVO.setPropertyTerm(rs.getString("Property_Term"));
				dtscVO.setRepresentationTerm(rs.getString("Representation_Term"));
				dtscVO.setDefinition(rs.getString("Definition"));
				dtscVO.setOwnerDTID(rs.getInt("Owner_DT_ID"));
				dtscVO.setMinCardinality(rs.getInt("Min_Cardinality"));
				dtscVO.setMaxCardinality(rs.getInt("Max_Cardinality"));
				dtscVO.setBasedDTSCID(rs.getInt("Based_DT_SC_ID"));
			}
		} catch (SQLException e) {
			throw new SRTDAOException(SRTDAOException.SQL_EXECUTION_FAILED, e);
		} finally {
			closeQuietly(rs);
			closeQuietly(ps);
		}
		return dtscVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			conn = tx.open();
			String sql = _FIND_ALL_DT_SC_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				DTSCVO dtscVO = new DTSCVO();
				dtscVO.setDTSCID(rs.getInt("DT_SC_ID"));
				dtscVO.setDTSCGUID(rs.getString("guid"));
				dtscVO.setPropertyTerm(rs.getString("Property_Term"));
				dtscVO.setRepresentationTerm(rs.getString("Representation_Term"));
				dtscVO.setDefinition(rs.getString("Definition"));
				dtscVO.setOwnerDTID(rs.getInt("Owner_DT_ID"));
				dtscVO.setMinCardinality(rs.getInt("Min_Cardinality"));
				dtscVO.setMaxCardinality(rs.getInt("Max_Cardinality"));
				dtscVO.setBasedDTSCID(rs.getInt("Based_DT_SC_ID"));
				list.add(dtscVO);
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
			String sql = _FIND_ALL_DT_SC_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				DTSCVO dtscVO = new DTSCVO();
				dtscVO.setDTSCID(rs.getInt("DT_SC_ID"));
				dtscVO.setDTSCGUID(rs.getString("guid"));
				dtscVO.setPropertyTerm(rs.getString("Property_Term"));
				dtscVO.setRepresentationTerm(rs.getString("Representation_Term"));
				dtscVO.setDefinition(rs.getString("Definition"));
				dtscVO.setOwnerDTID(rs.getInt("Owner_DT_ID"));
				dtscVO.setMinCardinality(rs.getInt("Min_Cardinality"));
				dtscVO.setMaxCardinality(rs.getInt("Max_Cardinality"));
				dtscVO.setBasedDTSCID(rs.getInt("Based_DT_SC_ID"));
				list.add(dtscVO);
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

		DTSCVO dtscVO = (DTSCVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_DT_SC_STATEMENT);

			if (dtscVO.getDTSCGUID() == null || dtscVO.getDTSCGUID().length() == 0 || dtscVO.getDTSCGUID().isEmpty() || dtscVO.getDTSCGUID().equals(""))
				ps.setString(1, "**SOMETHING WRONG THIS VALUE CANNOT BE NULL**");
			else
				ps.setString(1, dtscVO.getDTSCGUID());

//			if( dtscVO.getPropertyTerm()==null ||  dtscVO.getPropertyTerm().length()==0 ||  dtscVO.getPropertyTerm().isEmpty() ||  dtscVO.getPropertyTerm().equals(""))				
//				ps.setString(2,"\u00A0");
//			else 	
				ps.setString(2, dtscVO.getPropertyTerm());

//			if( dtscVO.getRepresentationTerm()==null ||  dtscVO.getRepresentationTerm().length()==0 ||  dtscVO.getRepresentationTerm().isEmpty() ||  dtscVO.getRepresentationTerm().equals(""))				
//				ps.setString(3,"\u00A0");
//			else 	
				ps.setString(3, dtscVO.getRepresentationTerm());

//			if( dtscVO.getDefinition()==null ||  dtscVO.getDefinition().length()==0 ||  dtscVO.getDefinition().isEmpty() ||  dtscVO.getDefinition().equals(""))				
//				ps.setString(4,"\u00A0");
//			else 	{
				String s = StringUtils.abbreviate(dtscVO.getDefinition(), 4000);
				ps.setString(4, s);
//			}
			ps.setInt(5, dtscVO.getOwnerDTID());
			ps.setInt(6, dtscVO.getMinCardinality());
			ps.setInt(7, dtscVO.getMaxCardinality());
			if (dtscVO.getBasedDTSCID() > 0) {
				ps.setInt(8, dtscVO.getBasedDTSCID());
			} else {
				ps.setNull(8, java.sql.Types.INTEGER);
			}
			ps.setInt(9, dtscVO.getDTSCID());

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

		DTSCVO dtscVO = (DTSCVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_DELETE_DT_SC_STATEMENT);
			ps.setInt(1, dtscVO.getDTSCID());
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
	public int insertObject(SRTObject obj, Connection conn)
			throws SRTDAOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
