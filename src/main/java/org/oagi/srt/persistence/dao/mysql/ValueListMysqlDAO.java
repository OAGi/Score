package org.oagi.srt.persistence.dao.mysql;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ValueListVO;
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
public class ValueListMysqlDAO extends SRTDAO {

	private final String _tableName = "value_list";

	private final String _FIND_ALL_VALUE_LIST_STATEMENT = "SELECT Value_List_ID, Type, "
	+ "Value_List_GUID, Name, List_ID, Agency_ID, Version_ID, Definition, "
	+ "Based_Code_List_ID, Extensible_Indicator, Created_By_User_ID, Creation_Timestamp, "
	+ "Last_Update_Timestamp, Definition_Source"
	+ " FROM " + _tableName;

	private final String _FIND_VALUE_LIST_STATEMENT = "SELECT Value_List_ID, Type, "
	+ "Value_List_GUID, Name, List_ID, Agency_ID, Version_ID, Definition, "
	+ "Based_Code_List_ID, Extensible_Indicator, Created_By_User_ID, Creation_Timestamp, "
	+ "Last_Update_Timestamp, Definition_Source"
	+ " FROM " + _tableName;

	private final String _INSERT_VALUE_LIST_STATEMENT =
			"INSERT INTO " + _tableName + " (Type, Value_List_GUID, Name, List_ID, Agency_ID,"
					+ " Version_ID, Definition, Based_Code_List_ID, Extensible_Indicator, "
					+ "Created_By_User_ID, Creation_Timestamp, Last_Update_Timestamp, "
					+ "Definition_Source) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)";

	private final String _UPDATE_VALUE_LIST_STATEMENT =
			"UPDATE " + _tableName
			+ " SET Last_Update_Timestamp = CURRENT_TIMESTAMP, Type = ?, Value_List_GUID = ?,"
			+ " List_ID = ?, Agency_ID = ?, Version_ID = ?, Definition = ?, Based_Code_List_ID = ?,"
			+ " Extensible_Indicator = ?, Created_By_User_ID = ?, "
			+ " Definition_Source = ? WHERE Value_List_ID = ?";

	private final String _DELETE_VALUE_LIST_STATEMENT =
			"DELETE FROM " + _tableName + " WHERE Value_List_ID = ?";

	public int insertObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ValueListVO value_listVO = (ValueListVO) obj;
		try {
			conn = tx.open();
			ps = conn.prepareStatement(_INSERT_VALUE_LIST_STATEMENT);
			ps.setInt(1, value_listVO.getType());
			ps.setString(2, value_listVO.getValueListGUID());
			ps.setString(3, value_listVO.getName());
			ps.setString(4, value_listVO.getListID());
			ps.setString(5, value_listVO.getAgencyID());
			ps.setString(6, value_listVO.getVersionID());
			ps.setString(7, value_listVO.getDefinition());
			ps.setInt(8, value_listVO.getBasedCodeListID());
			ps.setInt(9, value_listVO.getExtensibleIndicator());
			ps.setInt(10, value_listVO.getCreatedByUserID());
			ps.setInt(11, value_listVO.getLastUpdatedByUserID());
			//ps.setTimestamp(12, value_listVO.getCreationTimestamp());
			//ps.setTimestamp(13, value_listVO.getLastUpdateTimestamp());
			ps.setString(12, value_listVO.getDefinitionSource());

			ps.executeUpdate();

			rs = ps.getGeneratedKeys();
			rs.next();
			int autoGeneratedID = rs.getInt(1);

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
		return 1;

	}

	public SRTObject findObject(QueryCondition qc) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ValueListVO value_listVO = new ValueListVO();
		try {
			conn = tx.open();
			String sql = _FIND_VALUE_LIST_STATEMENT;

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
				value_listVO.setValueListID(rs.getInt("Value_List_ID"));
				value_listVO.setType(rs.getInt("Type"));
				value_listVO.setValueListGUID(rs.getString("Value_List_GUID"));
				value_listVO.setName(rs.getString("Name"));
				value_listVO.setListID(rs.getString("List_ID"));
				value_listVO.setAgencyID(rs.getString("Agency_ID"));
				value_listVO.setVersionID(rs.getString("Version_ID"));
				value_listVO.setDefinition(rs.getString("Definition"));
				value_listVO.setBasedCodeListID(rs.getInt("Based_Code_List_ID"));
				value_listVO.setExtensibleIndicator(rs.getInt("Extensible_Indicator"));
				value_listVO.setCreatedByUserID(rs.getInt("Created_By_User_ID"));
				value_listVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By_User_ID"));
				value_listVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				value_listVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				value_listVO.setDefinitionSource(rs.getString("Definition_Source"));
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
		return value_listVO;
	}

	public ArrayList<SRTObject> findObjects() throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ArrayList<SRTObject> list = new ArrayList<SRTObject>();
		try {
			conn = tx.open();
			String sql = _FIND_ALL_VALUE_LIST_STATEMENT;
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ValueListVO value_listVO = new ValueListVO();
				value_listVO.setValueListID(rs.getInt("Value_List_ID"));
				value_listVO.setType(rs.getInt("Type"));
				value_listVO.setValueListGUID(rs.getString("Value_List_GUID"));
				value_listVO.setName(rs.getString("Name"));
				value_listVO.setListID(rs.getString("List_ID"));
				value_listVO.setAgencyID(rs.getString("Agency_ID"));
				value_listVO.setVersionID(rs.getString("Version_ID"));
				value_listVO.setDefinition(rs.getString("Definition"));
				value_listVO.setBasedCodeListID(rs.getInt("Based_Code_List_ID"));
				value_listVO.setExtensibleIndicator(rs.getInt("Extensible_Indicator"));
				value_listVO.setCreatedByUserID(rs.getInt("Created_By_User_ID"));
				value_listVO.setLastUpdatedByUserID(rs.getInt("Last_Updated_By_User_ID"));
				value_listVO.setCreationTimestamp(rs.getTimestamp("Creation_Timestamp"));
				value_listVO.setLastUpdateTimestamp(rs.getTimestamp("Last_Update_Timestamp"));
				value_listVO.setDefinitionSource(rs.getString("Definition_Source"));
				list.add(value_listVO);
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

	public boolean updateObject(SRTObject obj) throws SRTDAOException {
		DBAgent tx = new DBAgent();
		Connection conn = null;
		PreparedStatement ps = null;

		ValueListVO value_listVO = (ValueListVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_UPDATE_VALUE_LIST_STATEMENT);

			ps.setInt(1, value_listVO.getType());
			ps.setString(2, value_listVO.getValueListGUID());
			ps.setString(3, value_listVO.getName());
			ps.setString(4, value_listVO.getListID());
			ps.setString(5, value_listVO.getAgencyID());
			ps.setString(6, value_listVO.getVersionID());
			ps.setString(7, value_listVO.getDefinition());
			ps.setInt(8, value_listVO.getBasedCodeListID());
			ps.setInt(9, value_listVO.getExtensibleIndicator());
			ps.setInt(10, value_listVO.getCreatedByUserID());
			ps.setInt(11, value_listVO.getLastUpdatedByUserID());
			//ps.setTimestamp(12, value_listVO.getCreationTimestamp());
			//ps.setTimestamp(13, value_listVO.getLastUpdateTimestamp());
			ps.setString(12, value_listVO.getDefinitionSource());
			ps.setInt(13, value_listVO.getValueListID());

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

		ValueListVO value_listVO = (ValueListVO) obj;
		try {
			conn = tx.open();

			ps = conn.prepareStatement(_DELETE_VALUE_LIST_STATEMENT);
			ps.setInt(1, value_listVO.getValueListID());
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
}
