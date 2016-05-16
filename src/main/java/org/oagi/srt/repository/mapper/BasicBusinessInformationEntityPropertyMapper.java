package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntityProperty;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class BasicBusinessInformationEntityPropertyMapper implements RowMapper<BasicBusinessInformationEntityProperty> {

    public static BasicBusinessInformationEntityPropertyMapper INSTANCE = new BasicBusinessInformationEntityPropertyMapper();

    @Override
    public BasicBusinessInformationEntityProperty mapRow(ResultSet rs, int rowNum) throws SQLException {
        BasicBusinessInformationEntityProperty basicBusinessInformationEntityProperty = new BasicBusinessInformationEntityProperty();
        basicBusinessInformationEntityProperty.setBbiepId(rs.getInt("bbiep_id"));
        basicBusinessInformationEntityProperty.setGuid(rs.getString("guid"));
        basicBusinessInformationEntityProperty.setBasedBccpId(rs.getInt("based_bccp_id"));
        basicBusinessInformationEntityProperty.setDefinition(rs.getString("definition"));
        basicBusinessInformationEntityProperty.setRemark(rs.getString("remark"));
        basicBusinessInformationEntityProperty.setBizTerm(rs.getString("biz_term"));
        basicBusinessInformationEntityProperty.setCreatedBy(rs.getInt("created_by"));
        basicBusinessInformationEntityProperty.setLastUpdatedBy(rs.getInt("last_updated_by"));
        basicBusinessInformationEntityProperty.setCreationTimestamp(new Date(rs.getTimestamp("creation_timestamp").getTime()));
        basicBusinessInformationEntityProperty.setLastUpdateTimestamp(new Date(rs.getTimestamp("last_update_timestamp").getTime()));
        return basicBusinessInformationEntityProperty;
    }
}
