package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.BusinessContextRepository;
import org.oagi.srt.repository.entity.BusinessContext;
import org.oagi.srt.repository.mapper.BusinessContextMapper;
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
@CacheConfig(cacheNames = "BusinessContexts", keyGenerator = "simpleCacheKeyGenerator")
public class BaseBusinessContextRepository extends NamedParameterJdbcDaoSupport implements BusinessContextRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ALL_STATEMENT = "SELECT " +
            "biz_ctx_id, guid, name, created_by, last_updated_by, creation_timestamp, last_update_timestamp " +
            "FROM biz_ctx";

    @Override
    @Cacheable("BusinessContexts")
    public List<BusinessContext> findAll() {
        return getJdbcTemplate().query(FIND_ALL_STATEMENT, BusinessContextMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_BUSINESS_CONTEXT_ID_STATEMENT = "SELECT " +
            "biz_ctx_id, guid, name, created_by, last_updated_by, creation_timestamp, last_update_timestamp " +
            "FROM biz_ctx " +
            "WHERE biz_ctx_id = :biz_ctx_id";

    @Override
    @Cacheable("BusinessContexts")
    public BusinessContext findOneByBusinessContextId(int businessContextId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("biz_ctx_id", businessContextId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_BUSINESS_CONTEXT_ID_STATEMENT,
                namedParameters, BusinessContextMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO biz_ctx (" +
            "guid, name, created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES (" +
            ":guid, :name, :created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

    @Override
    @CacheEvict("BusinessContexts")
    public void save(BusinessContext businessContext) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", businessContext.getGuid())
                .addValue("name", businessContext.getName())
                .addValue("created_by", businessContext.getCreatedBy())
                .addValue("last_updated_by", businessContext.getLastUpdatedBy());

        int businessContextId = doSave(namedParameters, businessContext);
        businessContext.setBizCtxId(businessContextId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, BusinessContext businessContext) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"biz_ctx_id"});
        return keyHolder.getKey().intValue();
    }
}
