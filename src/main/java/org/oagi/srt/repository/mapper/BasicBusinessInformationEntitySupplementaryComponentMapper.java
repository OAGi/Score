package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BasicBusinessInformationEntitySupplementaryComponentMapper
        implements RowMapper<BasicBusinessInformationEntitySupplementaryComponent> {

    public static BasicBusinessInformationEntitySupplementaryComponentMapper INSTANCE = new BasicBusinessInformationEntitySupplementaryComponentMapper();

    @Override
    public BasicBusinessInformationEntitySupplementaryComponent mapRow(ResultSet rs, int rowNum) throws SQLException {
        BasicBusinessInformationEntitySupplementaryComponent basicBusinessInformationEntitySupplementaryComponent =
                new BasicBusinessInformationEntitySupplementaryComponent();
        basicBusinessInformationEntitySupplementaryComponent.setBbieScId(rs.getInt("bbie_sc_id"));
        basicBusinessInformationEntitySupplementaryComponent.setBbieId(rs.getInt("bbie_id"));
        basicBusinessInformationEntitySupplementaryComponent.setDtScId(rs.getInt("dt_sc_id"));
        basicBusinessInformationEntitySupplementaryComponent.setDtScPriRestriId(rs.getInt("dt_sc_pri_restri_id"));
        basicBusinessInformationEntitySupplementaryComponent.setCodeListId(rs.getInt("code_list_id"));
        basicBusinessInformationEntitySupplementaryComponent.setAgencyIdListId(rs.getInt("agency_id_list_id"));
        basicBusinessInformationEntitySupplementaryComponent.setMinCardinality(rs.getInt("min_cardinality"));
        basicBusinessInformationEntitySupplementaryComponent.setMaxCardinality(rs.getInt("max_cardinality"));
        basicBusinessInformationEntitySupplementaryComponent.setDefaultValue(rs.getString("default_value"));
        basicBusinessInformationEntitySupplementaryComponent.setFixedValue(rs.getString("fixed_value"));
        basicBusinessInformationEntitySupplementaryComponent.setDefinition(rs.getString("definition"));
        basicBusinessInformationEntitySupplementaryComponent.setRemark(rs.getString("remark"));
        basicBusinessInformationEntitySupplementaryComponent.setBizTerm(rs.getString("biz_term"));
        basicBusinessInformationEntitySupplementaryComponent.setUsed(rs.getInt("is_used") == 1 ? true : false);
        return basicBusinessInformationEntitySupplementaryComponent;
    }
}
