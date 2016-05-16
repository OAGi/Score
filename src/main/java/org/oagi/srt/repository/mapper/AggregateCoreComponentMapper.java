package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class AggregateCoreComponentMapper implements RowMapper<AggregateCoreComponent> {

    public static AggregateCoreComponentMapper INSTANCE = new AggregateCoreComponentMapper();

    @Override
    public AggregateCoreComponent mapRow(ResultSet rs, int rowNum) throws SQLException {
        AggregateCoreComponent aggregateCoreComponent = new AggregateCoreComponent();
        aggregateCoreComponent.setAccId(rs.getInt("acc_id"));
        aggregateCoreComponent.setGuid(rs.getString("guid"));
        aggregateCoreComponent.setObjectClassTerm(rs.getString("object_class_term"));
        aggregateCoreComponent.setDen(rs.getString("den"));
        aggregateCoreComponent.setDefinition(rs.getString("definition"));
        aggregateCoreComponent.setBasedAccId(rs.getInt("based_acc_id"));
        aggregateCoreComponent.setObjectClassQualifier(rs.getString("object_class_qualifier"));
        aggregateCoreComponent.setOagisComponentType(rs.getInt("oagis_component_type"));
        aggregateCoreComponent.setModule(rs.getString("module"));
        aggregateCoreComponent.setNamespaceId(rs.getInt("namespace_id"));
        aggregateCoreComponent.setCreatedBy(rs.getInt("created_by"));
        aggregateCoreComponent.setOwnerUserId(rs.getInt("owner_user_id"));
        aggregateCoreComponent.setLastUpdatedBy(rs.getInt("last_updated_by"));
        aggregateCoreComponent.setCreationTimestamp(new Date(rs.getTimestamp("creation_timestamp").getTime()));
        aggregateCoreComponent.setLastUpdateTimestamp(new Date(rs.getTimestamp("last_update_timestamp").getTime()));
        aggregateCoreComponent.setState(rs.getInt("state"));
        aggregateCoreComponent.setRevisionNum(rs.getInt("revision_num"));
        aggregateCoreComponent.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
        aggregateCoreComponent.setRevisionAction(rs.getInt("revision_action"));
        aggregateCoreComponent.setReleaseId(rs.getInt("release_id"));
        aggregateCoreComponent.setCurrentAccId(rs.getInt("current_acc_id"));
        aggregateCoreComponent.setDeprecated(rs.getInt("is_deprecated") == 1 ? true : false);
        return aggregateCoreComponent;
    }
}
