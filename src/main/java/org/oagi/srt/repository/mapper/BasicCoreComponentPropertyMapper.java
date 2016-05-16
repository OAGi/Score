package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class BasicCoreComponentPropertyMapper implements RowMapper<BasicCoreComponentProperty> {

    public static BasicCoreComponentPropertyMapper INSTANCE = new BasicCoreComponentPropertyMapper();

    @Override
    public BasicCoreComponentProperty mapRow(ResultSet rs, int rowNum) throws SQLException {
        BasicCoreComponentProperty basicCoreComponentProperty = new BasicCoreComponentProperty();
        basicCoreComponentProperty.setBccpId(rs.getInt("bccp_id"));
        basicCoreComponentProperty.setGuid(rs.getString("guid"));
        basicCoreComponentProperty.setPropertyTerm(rs.getString("property_term"));
        basicCoreComponentProperty.setRepresentationTerm(rs.getString("representation_term"));
        basicCoreComponentProperty.setBdtId(rs.getInt("bdt_id"));
        basicCoreComponentProperty.setDen(rs.getString("den"));
        basicCoreComponentProperty.setDefinition(rs.getString("definition"));
        basicCoreComponentProperty.setModule(rs.getString("module"));
        basicCoreComponentProperty.setNamespaceId(rs.getInt("namespace_id"));
        basicCoreComponentProperty.setDeprecated(rs.getInt("is_deprecated") == 1 ? true : false);
        basicCoreComponentProperty.setCreatedBy(rs.getInt("created_by"));
        basicCoreComponentProperty.setOwnerUserId(rs.getInt("owner_user_id"));
        basicCoreComponentProperty.setLastUpdatedBy(rs.getInt("last_updated_by"));
        basicCoreComponentProperty.setCreationTimestamp(new Date(rs.getTimestamp("creation_timestamp").getTime()));
        basicCoreComponentProperty.setLastUpdateTimestamp(new Date(rs.getTimestamp("last_update_timestamp").getTime()));
        basicCoreComponentProperty.setState(rs.getInt("state"));
        basicCoreComponentProperty.setRevisionNum(rs.getInt("revision_num"));
        basicCoreComponentProperty.setRevisionTrackingNum(rs.getInt("revision_tracking_num"));
        basicCoreComponentProperty.setRevisionAction(rs.getInt("revision_action"));
        basicCoreComponentProperty.setReleaseId(rs.getInt("release_id"));
        basicCoreComponentProperty.setCurrentBccpId(rs.getInt("current_bccp_id"));
        return basicCoreComponentProperty;
    }
}
