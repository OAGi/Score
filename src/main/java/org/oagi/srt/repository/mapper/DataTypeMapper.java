package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.DataType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class DataTypeMapper implements RowMapper<DataType> {

    public static DataTypeMapper INSTANCE = new DataTypeMapper();

    @Override
    public DataType mapRow(ResultSet rs, int rowNum) throws SQLException {
        DataType dataType = new DataType();
        dataType.setDtId(rs.getInt("dt_id"));
        dataType.setGuid(rs.getString("guid"));
        dataType.setType(rs.getInt("type"));
        dataType.setVersionNum(rs.getString("version_num"));
        dataType.setPreviousVersionDtId(rs.getInt("previous_version_dt_id"));
        dataType.setDataTypeTerm(rs.getString("data_type_term"));
        dataType.setQualifier(rs.getString("qualifier"));
        dataType.setBasedDtId(rs.getInt("based_dt_id"));
        dataType.setDen(rs.getString("den"));
        dataType.setContentComponentDen(rs.getString("content_component_den"));
        dataType.setDefinition(rs.getString("definition"));
        dataType.setContentComponentDefinition(rs.getString("content_component_definition"));
        dataType.setRevisionDoc(rs.getString("revision_doc"));
        dataType.setModule(rs.getString("module"));
        dataType.setState(rs.getInt("state"));
        dataType.setCreatedBy(rs.getInt("created_by"));
        dataType.setOwnerUserId(rs.getInt("owner_user_id"));
        dataType.setLastUpdatedBy(rs.getInt("last_updated_by"));
        dataType.setCreationTimestamp(new Date(rs.getTimestamp("creation_timestamp").getTime()));
        dataType.setLastUpdateTimestamp(new Date(rs.getTimestamp("last_update_timestamp").getTime()));
        dataType.setRevisionNum(rs.getInt("revision_num"));
        dataType.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
        dataType.setRevisionAction(rs.getInt("revision_action"));
        dataType.setReleaseId(rs.getInt("release_id"));
        dataType.setCurrentBdtId(rs.getInt("current_bdt_id"));
        dataType.setDeprecated(rs.getInt("is_deprecated") == 1 ? true : false);
        return dataType;
    }
}
