package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class AssociationCoreComponentMapper implements RowMapper<AssociationCoreComponent> {

    public static AssociationCoreComponentMapper INSTANCE = new AssociationCoreComponentMapper();

    @Override
    public AssociationCoreComponent mapRow(ResultSet rs, int rowNum) throws SQLException {
        AssociationCoreComponent associationCoreComponent = new AssociationCoreComponent();
        associationCoreComponent.setAsccId(rs.getInt("ascc_id"));
        associationCoreComponent.setGuid(rs.getString("guid"));
        associationCoreComponent.setCardinalityMin(rs.getInt("cardinality_min"));
        associationCoreComponent.setCardinalityMax(rs.getInt("cardinality_max"));
        associationCoreComponent.setSeqKey(rs.getInt("seq_key"));
        associationCoreComponent.setFromAccId(rs.getInt("from_acc_id"));
        associationCoreComponent.setToAsccpId(rs.getInt("to_asccp_id"));
        associationCoreComponent.setDen(rs.getString("den"));
        associationCoreComponent.setDefinition(rs.getString("definition"));
        associationCoreComponent.setDeprecated(rs.getInt("is_deprecated") == 1 ? true : false);
        associationCoreComponent.setCreatedBy(rs.getInt("created_by"));
        associationCoreComponent.setOwnerUserId(rs.getInt("owner_user_id"));
        associationCoreComponent.setLastUpdatedBy(rs.getInt("last_updated_by"));
        associationCoreComponent.setCreationTimestamp(new Date(rs.getTimestamp("creation_timestamp").getTime()));
        associationCoreComponent.setLastUpdateTimestamp(new Date(rs.getTimestamp("last_update_timestamp").getTime()));
        associationCoreComponent.setState(rs.getInt("state"));
        associationCoreComponent.setRevisionNum(rs.getInt("revision_num"));
        associationCoreComponent.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
        associationCoreComponent.setRevisionAction(rs.getInt("revision_action"));
        associationCoreComponent.setReleaseId(rs.getInt("release_id"));
        associationCoreComponent.setCurrentAsccId(rs.getInt("current_ascc_id"));
        return associationCoreComponent;
    }
}
