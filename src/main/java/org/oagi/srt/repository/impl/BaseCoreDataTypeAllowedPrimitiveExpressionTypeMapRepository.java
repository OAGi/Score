package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository;
import org.oagi.srt.repository.entity.CoreDataTypeAllowedPrimitiveExpressionTypeMap;
import org.oagi.srt.repository.mapper.CoreDataTypeAllowedPrimitiveExpressionTypeMapMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
public class BaseCoreDataTypeAllowedPrimitiveExpressionTypeMapRepository extends NamedParameterJdbcDaoSupport
        implements CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }
    
    private final String FIND_BY_CDT_AWD_PRI_ID_STATEMENT = "SELECT " + 
            "cdt_awd_pri_xps_type_map_id, cdt_awd_pri_id, xbt_id " +
            "FROM cdt_awd_pri_xps_type_map " +
            "WHERE cdt_awd_pri_id = :cdt_awd_pri_id";

    @Override
    public List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> findByCdtAwdPriId(int cdtAwdPriId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("cdt_awd_pri_id", cdtAwdPriId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_CDT_AWD_PRI_ID_STATEMENT,
                namedParameters, CoreDataTypeAllowedPrimitiveExpressionTypeMapMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_CDT_AWD_PRI_XPS_TYPE_MAP_ID_STATEMENT = "SELECT " +
            "cdt_awd_pri_xps_type_map_id, cdt_awd_pri_id, xbt_id " +
            "FROM cdt_awd_pri_xps_type_map " +
            "WHERE cdt_awd_pri_xps_type_map_id = :cdt_awd_pri_xps_type_map_id";

    @Override
    public CoreDataTypeAllowedPrimitiveExpressionTypeMap findOneByCdtAwdPriXpsTypeMapId(int cdtAwdPriXpsTypeMapId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("cdt_awd_pri_xps_type_map_id", cdtAwdPriXpsTypeMapId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_CDT_AWD_PRI_XPS_TYPE_MAP_ID_STATEMENT,
                namedParameters, CoreDataTypeAllowedPrimitiveExpressionTypeMapMapper.INSTANCE);
    }
}
