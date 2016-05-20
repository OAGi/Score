package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository;
import org.oagi.srt.repository.entity.CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap;
import org.oagi.srt.repository.mapper.CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
@CacheConfig(cacheNames = "CDTSCAwdPriXpsTypeMaps", keyGenerator = "simpleCacheKeyGenerator")
public class BaseCoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository extends NamedParameterJdbcDaoSupport
        implements CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ALL_STATEMENT = "SELECT " +
            "cdt_sc_awd_pri_xps_type_map_id, cdt_sc_awd_pri, xbt_id " +
            "FROM cdt_sc_awd_pri_xps_type_map";

    @Override
    @Cacheable("CDTSCAwdPriXpsTypeMaps")
    public List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> findAll() {
        return getJdbcTemplate().query(FIND_ALL_STATEMENT, CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapMapper.INSTANCE);
    }

    private final String FIND_BY_CDT_SC_AWD_PRI_STATEMENT = "SELECT " +
            "cdt_sc_awd_pri_xps_type_map_id, cdt_sc_awd_pri, xbt_id " +
            "FROM cdt_sc_awd_pri_xps_type_map " +
            "WHERE cdt_sc_awd_pri = :cdt_sc_awd_pri";

    @Override
    @Cacheable("CDTSCAwdPriXpsTypeMaps")
    public List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> findByCdtScAwdPri(int cdtScAwdPri) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("cdt_sc_awd_pri", cdtScAwdPri);

        return getNamedParameterJdbcTemplate().query(FIND_BY_CDT_SC_AWD_PRI_STATEMENT,
                namedParameters, CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID_STATEMENT = "SELECT " +
            "cdt_sc_awd_pri_xps_type_map_id, cdt_sc_awd_pri, xbt_id " +
            "FROM cdt_sc_awd_pri_xps_type_map " +
            "WHERE cdt_sc_awd_pri_xps_type_map_id = :cdt_sc_awd_pri_xps_type_map_id";

    @Override
    @Cacheable("CDTSCAwdPriXpsTypeMaps")
    public CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap findOneByCdtScAwdPriXpsTypeMapId(int cdtScAwdPriXpsTypeMapId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("cdt_sc_awd_pri_xps_type_map_id", cdtScAwdPriXpsTypeMapId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID_STATEMENT,
                namedParameters, CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_CDT_SC_AWD_PRI_AND_XBT_ID_STATEMENT = "SELECT " +
            "cdt_sc_awd_pri_xps_type_map_id, cdt_sc_awd_pri, xbt_id " +
            "FROM cdt_sc_awd_pri_xps_type_map " +
            "WHERE cdt_sc_awd_pri = :cdt_sc_awd_pri AND xbt_id = :xbt_id";

    @Override
    @Cacheable("CDTSCAwdPriXpsTypeMaps")
    public CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap findOneByCdtScAwdPriAndXbtId(int cdtScAwdPri, int xbtId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("cdt_sc_awd_pri", cdtScAwdPri)
                .addValue("xbt_id", xbtId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_CDT_SC_AWD_PRI_AND_XBT_ID_STATEMENT,
                namedParameters, CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO cdt_sc_awd_pri_xps_type_map (" +
            "cdt_sc_awd_pri, xbt_id) VALUES (" +
            ":cdt_sc_awd_pri, :xbt_id)";

    @Override
    @CacheEvict("CDTSCAwdPriXpsTypeMaps")
    public void save(CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("cdt_sc_awd_pri", cdtScAwdPriXpsTypeMap.getCdtScAwdPri())
                .addValue("xbt_id", cdtScAwdPriXpsTypeMap.getXbtId());

        int cdtScAwdPriXpsTypeMapId = doSave(namedParameters, cdtScAwdPriXpsTypeMap);
        cdtScAwdPriXpsTypeMap.setCdtScAwdPriXpsTypeMapId(cdtScAwdPriXpsTypeMapId);
    }

    protected int doSave(MapSqlParameterSource namedParameters,
                         CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"cdt_sc_awd_pri_xps_type_map_id"});
        return keyHolder.getKey().intValue();
    }
}
