package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.CoreDataTypePrimitiveRepository;
import org.oagi.srt.repository.entity.CoreDataTypePrimitive;
import org.oagi.srt.repository.mapper.CoreDataTypePrimitiveMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
@CacheConfig(cacheNames = "CDTPris", keyGenerator = "simpleCacheKeyGenerator")
public class BaseCoreDataTypePrimitiveRepository extends NamedParameterJdbcDaoSupport
        implements CoreDataTypePrimitiveRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ONE_BY_CDT_PRI_ID_STATEMENT = "SELECT " +
            "cdt_pri_id, name " +
            "FROM cdt_pri " +
            "WHERE cdt_pri_id = :cdt_pri_id";

    @Override
    @Cacheable("CDTPris")
    public CoreDataTypePrimitive findOneByCdtPriId(int cdtPriId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("cdt_pri_id", cdtPriId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_CDT_PRI_ID_STATEMENT,
                namedParameters, CoreDataTypePrimitiveMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_NAME_STATEMENT = "SELECT " +
            "cdt_pri_id, name " +
            "FROM cdt_pri " +
            "WHERE name = :name";

    @Override
    @Cacheable("CDTPris")
    public CoreDataTypePrimitive findOneByName(String name) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("name", name);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_NAME_STATEMENT,
                namedParameters, CoreDataTypePrimitiveMapper.INSTANCE);
    }
}
