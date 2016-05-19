package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.BusinessDataTypePrimitiveRestrictionRepository;
import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;
import org.oagi.srt.repository.mapper.BusinessDataTypePrimitiveRestrictionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

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

    private final String FIND_ONE_BY_BDT_ID_AND_CDT_AWD_PRI_XPS_TYPE_MAP_ID_STATEMENT = "SELECT " +
            "bdt_pri_restri_id, bdt_id, cdt_awd_pri_xps_type_map_id, code_list_id, " +
            "is_default, agency_id_list_id " +
            "FROM bdt_pri_restri " +
            "WHERE bdt_id = :bdt_id AND cdt_awd_pri_xps_type_map_id = :cdt_awd_pri_xps_type_map_id";

    @Override
    public BusinessDataTypePrimitiveRestriction findOneByBdtIdAndCdtAwdPriXpsTypeMapId(int bdtId, int cdtAwdPriXpsTypeMapId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bdt_id", bdtId)
                .addValue("cdt_awd_pri_xps_type_map_id", cdtAwdPriXpsTypeMapId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_BDT_ID_AND_CDT_AWD_PRI_XPS_TYPE_MAP_ID_STATEMENT,
                namedParameters, BusinessDataTypePrimitiveRestrictionMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_CODE_LIST_ID_AND_CDT_AWD_PRI_XPS_TYPE_MAP_ID_STATEMENT = "SELECT " +
            "bdt_pri_restri_id, bdt_id, cdt_awd_pri_xps_type_map_id, code_list_id, " +
            "is_default, agency_id_list_id " +
            "FROM bdt_pri_restri " +
            "WHERE code_list_id = :code_list_id AND cdt_awd_pri_xps_type_map_id = :cdt_awd_pri_xps_type_map_id";

    @Override
    public BusinessDataTypePrimitiveRestriction findOneByCodeListIdAndCdtAwdPriXpsTypeMapId(int codeListId, int cdtAwdPriXpsTypeMapId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("code_list_id", codeListId)
                .addValue("cdt_awd_pri_xps_type_map_id", cdtAwdPriXpsTypeMapId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_CODE_LIST_ID_AND_CDT_AWD_PRI_XPS_TYPE_MAP_ID_STATEMENT,
                namedParameters, BusinessDataTypePrimitiveRestrictionMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_CDT_AWD_PRI_XPS_TYPE_MAP_ID_STATEMENT = "SELECT " +
            "bdt_pri_restri_id, bdt_id, cdt_awd_pri_xps_type_map_id, code_list_id, " +
            "is_default, agency_id_list_id " +
            "FROM bdt_pri_restri " +
            "WHERE code_list_id = :code_list_id AND cdt_awd_pri_xps_type_map_id = :cdt_awd_pri_xps_type_map_id";

    @Override
    public BusinessDataTypePrimitiveRestriction findOneByCdtAwdPriXpsTypeMapId(int cdtAwdPriXpsTypeMapId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("cdt_awd_pri_xps_type_map_id", cdtAwdPriXpsTypeMapId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_CDT_AWD_PRI_XPS_TYPE_MAP_ID_STATEMENT,
                namedParameters, BusinessDataTypePrimitiveRestrictionMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_CODE_LIST_ID_STATEMENT = "SELECT " +
            "bdt_pri_restri_id, bdt_id, cdt_awd_pri_xps_type_map_id, code_list_id, " +
            "is_default, agency_id_list_id " +
            "FROM bdt_pri_restri " +
            "WHERE code_list_id = :code_list_id";

    @Override
    public BusinessDataTypePrimitiveRestriction findOneByCodeListId(int codeListId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("code_list_id", codeListId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_CODE_LIST_ID_STATEMENT,
                namedParameters, BusinessDataTypePrimitiveRestrictionMapper.INSTANCE);
    }

    private final String FIND_BY_BDT_ID_STATEMENT = "SELECT " +
            "bdt_pri_restri_id, bdt_id, cdt_awd_pri_xps_type_map_id, code_list_id, " +
            "is_default, agency_id_list_id " +
            "FROM bdt_pri_restri " +
            "WHERE bdt_id = :bdt_id";

    @Override
    public List<BusinessDataTypePrimitiveRestriction> findByBdtId(int bdtId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bdt_id", bdtId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_BDT_ID_STATEMENT,
                namedParameters, BusinessDataTypePrimitiveRestrictionMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO bdt_pri_restri (" +
            "bdt_id, cdt_awd_pri_xps_type_map_id, code_list_id, " +
            "is_default, agency_id_list_id) VALUES (" +
            ":bdt_id, :cdt_awd_pri_xps_type_map_id, :code_list_id, " +
            ":is_default, :agency_id_list_id)";

    @Override
    public void save(BusinessDataTypePrimitiveRestriction bdtPriRestri) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bdt_id", bdtPriRestri.getBdtId())
                .addValue("cdt_awd_pri_xps_type_map_id", bdtPriRestri.getCdtAwdPriXpsTypeMapId() == 0 ? null : bdtPriRestri.getCdtAwdPriXpsTypeMapId())
                .addValue("code_list_id", bdtPriRestri.getCodeListId() == 0 ? null : bdtPriRestri.getCodeListId())
                .addValue("is_default", bdtPriRestri.isDefault() ? 1 : 0)
                .addValue("agency_id_list_id", bdtPriRestri.getAgencyIdListId() == 0 ? null : bdtPriRestri.getAgencyIdListId());

        int bdtPriRestriId = doSave(namedParameters, bdtPriRestri);
        bdtPriRestri.setBdtPriRestriId(bdtPriRestriId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, BusinessDataTypePrimitiveRestriction bdtPriRestri) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"bdt_pri_restri_id"});
        return keyHolder.getKey().intValue();
    }
}
