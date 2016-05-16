package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class BasicCoreComponentMapper implements RowMapper<BasicCoreComponent> {

    public static BasicCoreComponentMapper INSTANCE = new BasicCoreComponentMapper();

    @Override
    public BasicCoreComponent mapRow(ResultSet rs, int rowNum) throws SQLException {
        BasicCoreComponent basicCoreComponent = new BasicCoreComponent();
        basicCoreComponent.setBccId(rs.getInt("bcc_id"));
        basicCoreComponent.setGuid(rs.getString("guid"));
        basicCoreComponent.setCardinalityMin(rs.getInt("cardinality_min"));
        basicCoreComponent.setCardinalityMax(rs.getInt("cardinality_max"));
        basicCoreComponent.setToBccpId(rs.getInt("to_bccp_id"));
        basicCoreComponent.setFromAccId(rs.getInt("from_acc_id"));
        basicCoreComponent.setSeqKey(rs.getInt("seq_key"));
        basicCoreComponent.setEntityType(rs.getInt("entity_type"));
        basicCoreComponent.setDen(rs.getString("den"));
        basicCoreComponent.setDefinition(rs.getString("definition"));
        basicCoreComponent.setCreatedBy(rs.getInt("created_by"));
        basicCoreComponent.setOwnerUserId(rs.getInt("owner_user_id"));
        basicCoreComponent.setLastUpdatedBy(rs.getInt("last_updated_by"));
        basicCoreComponent.setCreationTimestamp(new Date(rs.getTimestamp("creation_timestamp").getTime()));
        basicCoreComponent.setLastUpdateTimestamp(new Date(rs.getTimestamp("last_update_timestamp").getTime()));
        basicCoreComponent.setState(rs.getInt("state"));
        basicCoreComponent.setRevisionNum(rs.getInt("revision_num"));
        basicCoreComponent.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
        basicCoreComponent.setRevisionAction(rs.getInt("revision_action"));
        basicCoreComponent.setReleaseId(rs.getInt("release_id"));
        basicCoreComponent.setCurrentBccId(rs.getInt("current_bcc_id"));
        basicCoreComponent.setDeprecated(rs.getInt("is_deprecated") == 1 ? true : false);
        return basicCoreComponent;
    }
}
