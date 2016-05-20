package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.ContextSchemeRepository;
import org.oagi.srt.repository.entity.ContextScheme;
import org.oagi.srt.repository.mapper.ContextSchemeMapper;
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
@CacheConfig(cacheNames = "ContextSchemes", keyGenerator = "simpleCacheKeyGenerator")
public class BaseContextSchemeRepository extends NamedParameterJdbcDaoSupport implements ContextSchemeRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ALL_STATEMENT = "SELECT " +
            "classification_ctx_scheme_id, guid, scheme_id, scheme_name, description, " +
            "scheme_agency_id, scheme_version_id, ctx_category_id, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp " +
            "FROM classification_ctx_scheme";

    @Override
    @Cacheable("ContextSchemes")
    public List<ContextScheme> findAll() {
        return getJdbcTemplate().query(FIND_ALL_STATEMENT, ContextSchemeMapper.INSTANCE);
    }

    private final String FIND_BY_CONTEXT_CATEGORY_ID_STATEMENT = "SELECT " +
            "classification_ctx_scheme_id, guid, scheme_id, scheme_name, description, " +
            "scheme_agency_id, scheme_version_id, ctx_category_id, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp " +
            "FROM classification_ctx_scheme " +
            "WHERE ctx_category_id = :ctx_category_id";

    @Override
    @Cacheable("ContextSchemes")
    public List<ContextScheme> findByContextCategoryId(int contextCategoryId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("ctx_category_id", contextCategoryId);

        return getNamedParameterJdbcTemplate().query(
                FIND_BY_CONTEXT_CATEGORY_ID_STATEMENT, namedParameters, ContextSchemeMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_CONTEXT_SCHEME_ID_STATEMENT = "SELECT " +
            "classification_ctx_scheme_id, guid, scheme_id, scheme_name, description, " +
            "scheme_agency_id, scheme_version_id, ctx_category_id, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp " +
            "FROM classification_ctx_scheme " +
            "WHERE classification_ctx_scheme_id = :classification_ctx_scheme_id";

    @Override
    @Cacheable("ContextSchemes")
    public ContextScheme findOneByContextSchemeId(int contextSchemeId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("classification_ctx_scheme_id", contextSchemeId);

        return getNamedParameterJdbcTemplate().queryForObject(
                FIND_ONE_BY_CONTEXT_SCHEME_ID_STATEMENT, namedParameters, ContextSchemeMapper.INSTANCE);
    }

    private final String UPDATE_STATEMENT = "UPDATE classification_ctx_scheme SET " +
            "guid = :guid, scheme_id = :scheme_id, scheme_name = :scheme_name, " +
            "description = :description, scheme_agency_id = :scheme_agency_id, " +
            "scheme_version_id = :scheme_version_id, ctx_category_id = :ctx_category_id, " +
            "last_updated_by = :last_updated_by, last_update_timestamp = CURRENT_TIMESTAMP " +
            "WHERE classification_ctx_scheme_id = :classification_ctx_scheme_id";

    @Override
    @CacheEvict("ContextSchemes")
    public void update(ContextScheme contextScheme) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", contextScheme.getGuid())
                .addValue("scheme_id", contextScheme.getSchemeId())
                .addValue("scheme_name", contextScheme.getSchemeName())
                .addValue("description", contextScheme.getDescription())
                .addValue("scheme_agency_id", contextScheme.getSchemeAgencyId())
                .addValue("scheme_version_id", contextScheme.getSchemeVersionId())
                .addValue("ctx_category_id", contextScheme.getCtxCategoryId())
                .addValue("last_updated_by", contextScheme.getLastUpdatedBy())
                .addValue("classification_ctx_scheme_id", contextScheme.getClassificationCtxSchemeId());

        getNamedParameterJdbcTemplate().update(UPDATE_STATEMENT, namedParameters);
    }

    private final String SAVE_STATEMENT = "INSERT INTO classification_ctx_scheme (" +
            "guid, scheme_id, scheme_name, description, " +
            "scheme_agency_id, scheme_version_id, ctx_category_id, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES (" +
            ":guid, :scheme_id, :scheme_name, :description, " +
            ":scheme_agency_id, :scheme_version_id, :ctx_category_id, " +
            ":created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

    @Override
    @CacheEvict("ContextSchemes")
    public void save(ContextScheme contextScheme) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", contextScheme.getGuid())
                .addValue("scheme_id", contextScheme.getSchemeId())
                .addValue("scheme_name", contextScheme.getSchemeName())
                .addValue("description", contextScheme.getDescription())
                .addValue("scheme_agency_id", contextScheme.getSchemeAgencyId())
                .addValue("scheme_version_id", contextScheme.getSchemeVersionId())
                .addValue("ctx_category_id", contextScheme.getCtxCategoryId())
                .addValue("created_by", contextScheme.getCreatedBy())
                .addValue("last_updated_by", contextScheme.getLastUpdatedBy());

        int contextSchemeId = doSave(namedParameters, contextScheme);
        contextScheme.setClassificationCtxSchemeId(contextSchemeId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, ContextScheme contextScheme) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"classification_ctx_scheme_id"});
        return keyHolder.getKey().intValue();
    }

    private final String DELETE_BY_CONTEXT_SCHEME_ID_STATEMENT = "DELETE FROM classification_ctx_scheme " +
            "WHERE classification_ctx_scheme_id = :classification_ctx_scheme_id";

    @Override
    @CacheEvict("ContextSchemes")
    public void deleteByContextSchemeId(int contextSchemeId) {
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("classification_ctx_scheme_id", contextSchemeId);

        getNamedParameterJdbcTemplate().update(DELETE_BY_CONTEXT_SCHEME_ID_STATEMENT, sqlParameterSource);
    }
}
