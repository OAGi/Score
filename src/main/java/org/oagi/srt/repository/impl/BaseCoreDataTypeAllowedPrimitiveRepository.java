package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.CoreDataTypeAllowedPrimitiveRepository;
import org.oagi.srt.repository.entity.CoreDataTypeAllowedPrimitive;
import org.oagi.srt.repository.mapper.CoreDataTypeAllowedPrimitiveMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
public class BaseCoreDataTypeAllowedPrimitiveRepository extends NamedParameterJdbcDaoSupport
        implements CoreDataTypeAllowedPrimitiveRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_BY_CDT_ID_STATEMENT = "SELECT " +
            "cdt_awd_pri_id, cdt_id, cdt_pri_id, is_default " +
            "FROM cdt_awd_pri " +
            "WHERE cdt_id = :cdt_id";

    @Override
    public List<CoreDataTypeAllowedPrimitive> findByCdtId(int cdtId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("cdt_id", cdtId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_CDT_ID_STATEMENT,
                namedParameters, CoreDataTypeAllowedPrimitiveMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_CDT_AWD_PRI_ID_STATEMENT = "SELECT " +
            "cdt_awd_pri_id, cdt_id, cdt_pri_id, is_default " +
            "FROM cdt_awd_pri " +
            "WHERE cdt_awd_pri_id = :cdt_awd_pri_id";

    @Override
    public CoreDataTypeAllowedPrimitive findOneByCdtAwdPriId(int cdtAwdPriId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("cdt_awd_pri_id", cdtAwdPriId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_CDT_AWD_PRI_ID_STATEMENT,
                namedParameters, CoreDataTypeAllowedPrimitiveMapper.INSTANCE);
    }
}
