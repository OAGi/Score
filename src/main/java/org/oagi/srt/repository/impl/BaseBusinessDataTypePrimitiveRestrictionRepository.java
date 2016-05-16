package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.BusinessDataTypePrimitiveRestrictionRepository;
import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;
import org.oagi.srt.repository.mapper.BusinessDataTypePrimitiveRestrictionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
public class BaseBusinessDataTypePrimitiveRestrictionRepository extends NamedParameterJdbcDaoSupport
        implements BusinessDataTypePrimitiveRestrictionRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ONE_BY_BDT_ID_AND_DEFAULT_STATEMENT = "SELECT " +
            "bdt_pri_restri_id, bdt_id, cdt_awd_pri_xps_type_map_id, code_list_id, " +
            "is_default, agency_id_list_id " +
            "FROM bdt_pri_restri " +
            "WHERE bdt_id = :bdt_id AND is_default = :is_default";

    @Override
    public BusinessDataTypePrimitiveRestriction findOneByBdtIdAndDefault(int bdtId, boolean isDefault) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bdt_id", bdtId)
                .addValue("is_default", isDefault ? 1 : 0);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_BDT_ID_AND_DEFAULT_STATEMENT,
                namedParameters, BusinessDataTypePrimitiveRestrictionMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_BDT_PRI_RESTRI_ID_STATEMENT = "SELECT " +
            "bdt_pri_restri_id, bdt_id, cdt_awd_pri_xps_type_map_id, code_list_id, " +
            "is_default, agency_id_list_id " +
            "FROM bdt_pri_restri " +
            "WHERE bdt_pri_restri_id = :bdt_pri_restri_id";

    @Override
    public BusinessDataTypePrimitiveRestriction findOneByBdtPriRestriId(int bdtPriRestriId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bdt_pri_restri_id", bdtPriRestriId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_BDT_PRI_RESTRI_ID_STATEMENT,
                namedParameters, BusinessDataTypePrimitiveRestrictionMapper.INSTANCE);
    }
}
