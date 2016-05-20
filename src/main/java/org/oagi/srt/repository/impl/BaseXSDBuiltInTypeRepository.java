package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.XSDBuiltInTypeRepository;
import org.oagi.srt.repository.entity.XSDBuiltInType;
import org.oagi.srt.repository.mapper.XSDBuiltInTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
@CacheConfig(cacheNames = "XBTs", keyGenerator = "simpleCacheKeyGenerator")
public class BaseXSDBuiltInTypeRepository extends NamedParameterJdbcDaoSupport implements XSDBuiltInTypeRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ALL_STATEMENT = "SELECT " +
            "xbt_id, name, builtIn_type, subtype_of_xbt_id " +
            "FROM xbt";

    @Override
    @Cacheable("XBTs")
    public List<XSDBuiltInType> findAll() {
        return getJdbcTemplate().query(FIND_ALL_STATEMENT, XSDBuiltInTypeMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_NAME_STATEMENT = "SELECT " +
            "xbt_id, name, builtIn_type, subtype_of_xbt_id " +
            "FROM xbt " +
            "WHERE name = :name";

    @Override
    @Cacheable("XBTs")
    public XSDBuiltInType findOneByName(String name) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("name", name);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_NAME_STATEMENT,
                namedParameters, XSDBuiltInTypeMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_BUILT_IN_NAME_STATEMENT = "SELECT " +
            "xbt_id, name, builtIn_type, subtype_of_xbt_id " +
            "FROM xbt " +
            "WHERE builtIn_type = :builtIn_type";

    @Override
    @Cacheable("XBTs")
    public XSDBuiltInType findOneByBuiltInType(String builtInType) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("builtIn_type", builtInType);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_BUILT_IN_NAME_STATEMENT,
                namedParameters, XSDBuiltInTypeMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_XBT_ID_STATEMENT = "SELECT " +
            "xbt_id, name, builtIn_type, subtype_of_xbt_id " +
            "FROM xbt " +
            "WHERE xbt_id = :xbt_id";

    @Override
    @Cacheable("XBTs")
    public XSDBuiltInType findOneByXbtId(int xbtId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("xbt_id", xbtId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_XBT_ID_STATEMENT,
                namedParameters, XSDBuiltInTypeMapper.INSTANCE);
    }
}
