package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class BasicBusinessInformationEntityMapper implements RowMapper<BasicBusinessInformationEntity> {

    public static BasicBusinessInformationEntityMapper INSTANCE = new BasicBusinessInformationEntityMapper();

    @Override
    public BasicBusinessInformationEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        BasicBusinessInformationEntity basicBusinessInformationEntity = new BasicBusinessInformationEntity();
        basicBusinessInformationEntity.setBbieId(rs.getInt("bbie_id"));
        basicBusinessInformationEntity.setGuid(rs.getString("guid"));
        basicBusinessInformationEntity.setBasedBccId(rs.getInt("based_bcc_id"));
        basicBusinessInformationEntity.setFromAbieId(rs.getInt("from_abie_id"));
        basicBusinessInformationEntity.setToBbiepId(rs.getInt("to_bbiep_id"));
        basicBusinessInformationEntity.setBdtPriRestriId(rs.getInt("bdt_pri_restri_id"));
        basicBusinessInformationEntity.setCodeListId(rs.getInt("code_list_id"));
        basicBusinessInformationEntity.setCardinalityMin(rs.getInt("cardinality_min"));
        basicBusinessInformationEntity.setCardinalityMax(rs.getInt("cardinality_max"));
        basicBusinessInformationEntity.setDefaultValue(rs.getString("default_value"));
        basicBusinessInformationEntity.setNillable(rs.getInt("is_nillable") == 1 ? true : false);
        basicBusinessInformationEntity.setFixedValue(rs.getString("fixed_value"));
        basicBusinessInformationEntity.setNill(rs.getInt("is_null") == 1 ? true : false);
        basicBusinessInformationEntity.setDefinition(rs.getString("definition"));
        basicBusinessInformationEntity.setRemark(rs.getString("remark"));
        basicBusinessInformationEntity.setCreatedBy(rs.getInt("created_by"));
        basicBusinessInformationEntity.setLastUpdatedBy(rs.getInt("last_updated_by"));
        basicBusinessInformationEntity.setCreationTimestamp(new Date(rs.getTimestamp("creation_timestamp").getTime()));
        basicBusinessInformationEntity.setLastUpdateTimestamp(new Date(rs.getTimestamp("last_update_timestamp").getTime()));
        basicBusinessInformationEntity.setSeqKey(rs.getInt("seq_key"));
        basicBusinessInformationEntity.setUsed(rs.getInt("is_used") == 1 ? true : false);
        return basicBusinessInformationEntity;
    }
}
