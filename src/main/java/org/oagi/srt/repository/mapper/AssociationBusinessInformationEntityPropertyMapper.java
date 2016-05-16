package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class AssociationBusinessInformationEntityPropertyMapper implements RowMapper<AssociationBusinessInformationEntityProperty> {

    public static AssociationBusinessInformationEntityPropertyMapper INSTANCE = new AssociationBusinessInformationEntityPropertyMapper();

    @Override
    public AssociationBusinessInformationEntityProperty mapRow(ResultSet rs, int rowNum) throws SQLException {
        AssociationBusinessInformationEntityProperty associationBusinessInformationEntityProperty = new AssociationBusinessInformationEntityProperty();
        associationBusinessInformationEntityProperty.setAsbiepId(rs.getInt("asbiep_id"));
        associationBusinessInformationEntityProperty.setGuid(rs.getString("guid"));
        associationBusinessInformationEntityProperty.setBasedAsccpId(rs.getInt("based_asccp_id"));
        associationBusinessInformationEntityProperty.setRoleOfAbieId(rs.getInt("role_of_abie_id"));
        associationBusinessInformationEntityProperty.setDefinition(rs.getString("definition"));
        associationBusinessInformationEntityProperty.setRemark(rs.getString("remark"));
        associationBusinessInformationEntityProperty.setBizTerm(rs.getString("biz_term"));
        associationBusinessInformationEntityProperty.setCreatedBy(rs.getInt("created_by"));
        associationBusinessInformationEntityProperty.setLastUpdatedBy(rs.getInt("last_updated_by"));
        associationBusinessInformationEntityProperty.setCreationTimestamp(new Date(rs.getTimestamp("creation_timestamp").getTime()));
        associationBusinessInformationEntityProperty.setLastUpdateTimestamp(new Date(rs.getTimestamp("last_update_timestamp").getTime()));
        return associationBusinessInformationEntityProperty;
    }
}
