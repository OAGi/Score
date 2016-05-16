package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class AssociationCoreComponentPropertyMapper implements RowMapper<AssociationCoreComponentProperty> {

    public static AssociationCoreComponentPropertyMapper INSTANCE = new AssociationCoreComponentPropertyMapper();

    @Override
    public AssociationCoreComponentProperty mapRow(ResultSet rs, int rowNum) throws SQLException {
        AssociationCoreComponentProperty associationCoreComponentProperty = new AssociationCoreComponentProperty();
        associationCoreComponentProperty.setAsccpId(rs.getInt("asccp_id"));
        associationCoreComponentProperty.setGuid(rs.getString("guid"));
        associationCoreComponentProperty.setPropertyTerm(rs.getString("property_term"));
        associationCoreComponentProperty.setDefinition(rs.getString("definition"));
        associationCoreComponentProperty.setRoleOfAccId(rs.getInt("role_of_acc_id"));
        associationCoreComponentProperty.setDen(rs.getString("den"));
        associationCoreComponentProperty.setCreatedBy(rs.getInt("created_by"));
        associationCoreComponentProperty.setOwnerUserId(rs.getInt("owner_user_id"));
        associationCoreComponentProperty.setLastUpdatedBy(rs.getInt("last_updated_by"));
        associationCoreComponentProperty.setCreationTimestamp(new Date(rs.getTimestamp("creation_timestamp").getTime()));
        associationCoreComponentProperty.setLastUpdateTimestamp(new Date(rs.getTimestamp("last_update_timestamp").getTime()));
        associationCoreComponentProperty.setState(rs.getInt("state"));
        associationCoreComponentProperty.setModule(rs.getString("module"));
        associationCoreComponentProperty.setNamespaceId(rs.getInt("namespace_id"));
        associationCoreComponentProperty.setReusableIndicator(rs.getInt("reusable_indicator") == 1 ? true : false);
        associationCoreComponentProperty.setDeprecated(rs.getInt("is_deprecated") == 1 ? true : false);
        associationCoreComponentProperty.setRevisionNum(rs.getInt("revision_num"));
        associationCoreComponentProperty.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
        associationCoreComponentProperty.setRevisionAction(rs.getInt("revision_action"));
        associationCoreComponentProperty.setReleaseId(rs.getInt("release_id"));
        associationCoreComponentProperty.setCurrentAsccpId(rs.getInt("current_asccp_id"));
        return associationCoreComponentProperty;
    }
}
