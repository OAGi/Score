package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository;
import org.oagi.srt.repository.entity.BusinessDataTypeSupplementaryComponentPrimitiveRestriction;
import org.oagi.srt.repository.mapper.BusinessDataTypeSupplementaryComponentPrimitiveRestrictionMapper;
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
public class BaseBusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository extends NamedParameterJdbcDaoSupport
        implements BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_BY_BDT_SC_ID_STATEMENT = "SELECT " +
            "bdt_sc_pri_restri_id, bdt_sc_id, cdt_sc_awd_pri_xps_type_map_id, " +
            "code_list_id, is_default, agency_id_list_id " +
            "FROM bdt_sc_pri_restri " +
            "WHERE bdt_sc_id = :bdt_sc_id";

    @Override
    public List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> findByBdtScId(int bdtScId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bdt_sc_id", bdtScId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_BDT_SC_ID_STATEMENT,
                namedParameters, BusinessDataTypeSupplementaryComponentPrimitiveRestrictionMapper.INSTANCE);
    }

    private final String FIND_BY_BDT_SC_PRI_RESTRI_ID_STATEMENT = "SELECT " +
            "bdt_sc_pri_restri_id, bdt_sc_id, cdt_sc_awd_pri_xps_type_map_id, " +
            "code_list_id, is_default, agency_id_list_id " +
            "FROM bdt_sc_pri_restri " +
            "WHERE bdt_sc_pri_restri_id = :bdt_sc_pri_restri_id";

    @Override
    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScPriRestriId(int bdtScPriRestriId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bdt_sc_pri_restri_id", bdtScPriRestriId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_BY_BDT_SC_PRI_RESTRI_ID_STATEMENT,
                namedParameters, BusinessDataTypeSupplementaryComponentPrimitiveRestrictionMapper.INSTANCE);
    }

    private final String FIND_BY_BDT_SC_ID_AND_DEFAULT_STATEMENT = "SELECT " +
            "bdt_sc_pri_restri_id, bdt_sc_id, cdt_sc_awd_pri_xps_type_map_id, " +
            "code_list_id, is_default, agency_id_list_id " +
            "FROM bdt_sc_pri_restri " +
            "WHERE bdt_sc_id = :bdt_sc_id AND is_default = :is_default";

    @Override
    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScIdAndDefault(int bdtScId, boolean isDefault) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bdt_sc_id", bdtScId)
                .addValue("is_default", isDefault ? 1 : 0);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_BY_BDT_SC_ID_AND_DEFAULT_STATEMENT,
                namedParameters, BusinessDataTypeSupplementaryComponentPrimitiveRestrictionMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_BDT_SC_ID_AND_CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID_STATEMENT = "SELECT " +
            "bdt_sc_pri_restri_id, bdt_sc_id, cdt_sc_awd_pri_xps_type_map_id, " +
            "code_list_id, is_default, agency_id_list_id " +
            "FROM bdt_sc_pri_restri " +
            "WHERE bdt_sc_id = :bdt_sc_id AND cdt_sc_awd_pri_xps_type_map_id = :cdt_sc_awd_pri_xps_type_map_id";

    @Override
    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScIdAndCdtScAwdPriXpsTypeMapId(int bdtScId, int cdtScAwdPriXpsTypeMapId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bdt_sc_id", bdtScId)
                .addValue("cdt_sc_awd_pri_xps_type_map_id", cdtScAwdPriXpsTypeMapId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_BDT_SC_ID_AND_CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID_STATEMENT,
                namedParameters, BusinessDataTypeSupplementaryComponentPrimitiveRestrictionMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_BDT_SC_ID_AND_CODE_LIST_ID_STATEMENT = "SELECT " +
            "bdt_sc_pri_restri_id, bdt_sc_id, cdt_sc_awd_pri_xps_type_map_id, " +
            "code_list_id, is_default, agency_id_list_id " +
            "FROM bdt_sc_pri_restri " +
            "WHERE bdt_sc_id = :bdt_sc_id AND code_list_id = :code_list_id";

    @Override
    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScIdAndCodeListId(int bdtScId, int codeListId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bdt_sc_id", bdtScId)
                .addValue("code_list_id", codeListId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_BDT_SC_ID_AND_CODE_LIST_ID_STATEMENT,
                namedParameters, BusinessDataTypeSupplementaryComponentPrimitiveRestrictionMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_BDT_SC_ID_AND_AGENCY_ID_LIST_ID_STATEMENT = "SELECT " +
            "bdt_sc_pri_restri_id, bdt_sc_id, cdt_sc_awd_pri_xps_type_map_id, " +
            "code_list_id, is_default, agency_id_list_id " +
            "FROM bdt_sc_pri_restri " +
            "WHERE bdt_sc_id = :bdt_sc_id AND agency_id_list_id = :agency_id_list_id";

    @Override
    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScIdAndAgencyIdListId(int bdtScId, int agencyIdListId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bdt_sc_id", bdtScId)
                .addValue("agency_id_list_id", agencyIdListId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_BDT_SC_ID_AND_AGENCY_ID_LIST_ID_STATEMENT,
                namedParameters, BusinessDataTypeSupplementaryComponentPrimitiveRestrictionMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_BDT_SC_ID_AND_CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID_AND_CODE_LIST_ID_AND_AGENCY_ID_LIST_ID_STATEMENT = "SELECT " +
            "bdt_sc_pri_restri_id, bdt_sc_id, cdt_sc_awd_pri_xps_type_map_id, " +
            "code_list_id, is_default, agency_id_list_id " +
            "FROM bdt_sc_pri_restri " +
            "WHERE bdt_sc_id = :bdt_sc_id AND cdt_sc_awd_pri_xps_type_map_id = :cdt_sc_awd_pri_xps_type_map_id AND " +
            "code_list_id = :code_list_id AND agency_id_list_id = :agency_id_list_id";

    @Override
    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScIdAndCdtScAwdPriXpsTypeMapIdAndCodeListIdAndAgencyIdListId(
            int bdtScId, int cdtScAwdPriXpsTypeMapId, int codeListId, int agencyIdListId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bdt_sc_id", bdtScId)
                .addValue("cdt_sc_awd_pri_xps_type_map_id", cdtScAwdPriXpsTypeMapId)
                .addValue("code_list_id", codeListId)
                .addValue("agency_id_list_id", agencyIdListId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_BDT_SC_ID_AND_CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID_AND_CODE_LIST_ID_AND_AGENCY_ID_LIST_ID_STATEMENT,
                namedParameters, BusinessDataTypeSupplementaryComponentPrimitiveRestrictionMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO bdt_sc_pri_restri (" +
            "bdt_sc_id, cdt_sc_awd_pri_xps_type_map_id, " +
            "code_list_id, is_default, agency_id_list_id) VALUES (" +
            ":bdt_sc_id, :cdt_sc_awd_pri_xps_type_map_id, " +
            ":code_list_id, :is_default, :agency_id_list_id)";

    @Override
    public void save(BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bdt_sc_id", bdtScPriRestri.getBdtScId())
                .addValue("cdt_sc_awd_pri_xps_type_map_id", bdtScPriRestri.getCdtScAwdPriXpsTypeMapId())
                .addValue("code_list_id", bdtScPriRestri.getCodeListId())
                .addValue("is_default", bdtScPriRestri.isDefault() ? 1 : 0)
                .addValue("agency_id_list_id", bdtScPriRestri.getAgencyIdListId());

        int bdtScPriRestriId = doSave(namedParameters, bdtScPriRestri);
        bdtScPriRestri.setCodeListId(bdtScPriRestriId);
    }

    protected int doSave(MapSqlParameterSource namedParameters,
                         BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"bdt_sc_pri_restri_id"});
        return keyHolder.getKey().intValue();
    }
}
