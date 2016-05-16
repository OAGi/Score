package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.AssociationBusinessInformationEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class AssociationBusinessInformationEntityMapper implements RowMapper<AssociationBusinessInformationEntity> {

    public static AssociationBusinessInformationEntityMapper INSTANCE = new AssociationBusinessInformationEntityMapper();

    @Override
    public AssociationBusinessInformationEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AssociationBusinessInformationEntity associationBusinessInformationEntity = new AssociationBusinessInformationEntity();
        associationBusinessInformationEntity.setAsbieId(rs.getInt("asbie_id"));
        associationBusinessInformationEntity.setGuid(rs.getString("guid"));
        associationBusinessInformationEntity.setFromAbieId(rs.getInt("from_abie_id"));
        associationBusinessInformationEntity.setToAsbiepId(rs.getInt("to_asbiep_id"));
        associationBusinessInformationEntity.setBasedAscc(rs.getInt("based_ascc"));
        associationBusinessInformationEntity.setDefinition(rs.getString("definition"));
        associationBusinessInformationEntity.setCardinalityMin(rs.getInt("cardinality_min"));
        associationBusinessInformationEntity.setCardinalityMax(rs.getInt("cardinality_max"));
        associationBusinessInformationEntity.setNillable(rs.getInt("is_nillable") == 1 ? true : false);
        associationBusinessInformationEntity.setRemark(rs.getString("remark"));
        associationBusinessInformationEntity.setCreatedBy(rs.getInt("created_by"));
        associationBusinessInformationEntity.setLastUpdatedBy(rs.getInt("last_updated_by"));
        associationBusinessInformationEntity.setCreationTimestamp(new Date(rs.getTimestamp("creation_timestamp").getTime()));
        associationBusinessInformationEntity.setLastUpdateTimestamp(new Date(rs.getTimestamp("last_update_timestamp").getTime()));
        associationBusinessInformationEntity.setSeqKey(rs.getInt("seq_key"));
        associationBusinessInformationEntity.setUsed(rs.getInt("is_used") == 1 ? true : false);
        return associationBusinessInformationEntity;
    }
}
