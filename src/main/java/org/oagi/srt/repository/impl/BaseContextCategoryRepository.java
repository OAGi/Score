package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.ContextCategoryRepository;
import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.repository.mapper.ContextCategoryMapper;
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
public class BaseContextCategoryRepository extends NamedParameterJdbcDaoSupport implements ContextCategoryRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ALL_STATEMENT = "SELECT " +
            "ctx_category_id, guid, name, description " +
            "FROM ctx_category";

    @Override
    public List<ContextCategory> findAll() {
        return getJdbcTemplate().query(FIND_ALL_STATEMENT, ContextCategoryMapper.INSTANCE);
    }

    private final String FIND_BY_NAME_CONTAINING_STATEMENT = "SELECT " +
            "ctx_category_id, guid, name, description " +
            "FROM ctx_category " +
            "WHERE name LIKE :name";

    @Override
    public List<ContextCategory> findByNameContaining(String name) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("name", "%" + name + "%");

        return getNamedParameterJdbcTemplate().query(
                FIND_BY_NAME_CONTAINING_STATEMENT, namedParameters, ContextCategoryMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_CONTEXT_CATEGORY_ID_STATEMENT = "SELECT " +
            "ctx_category_id, guid, name, description " +
            "FROM ctx_category " +
            "WHERE ctx_category_id = :ctx_category_id";

    @Override
    public ContextCategory findOneByContextCategoryId(int contextCategoryId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("ctx_category_id", contextCategoryId);

        return getNamedParameterJdbcTemplate().queryForObject(
                FIND_ONE_BY_CONTEXT_CATEGORY_ID_STATEMENT, namedParameters, ContextCategoryMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO ctx_category (" +
            "guid, name, description) VALUES (" +
            ":guid, :name, :description)";

    @Override
    public void save(ContextCategory contextCategory) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", contextCategory.getGuid())
                .addValue("name", contextCategory.getName())
                .addValue("description", contextCategory.getDescription());

        int contextCategoryId = doSave(namedParameters, contextCategory);
        contextCategory.setCtxCategoryId(contextCategoryId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, ContextCategory contextCategory) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"ctx_category_id"});
        return keyHolder.getKey().intValue();
    }

    private final String UPDATE_STATEMENT = "UPDATE ctx_category SET " +
            "guid = :guid, name = :name, description = :description WHERE ctx_category_id = :ctx_category_id";

    @Override
    public void update(ContextCategory contextCategory) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", contextCategory.getGuid())
                .addValue("name", contextCategory.getName())
                .addValue("description", contextCategory.getDescription())
                .addValue("ctx_category_id", contextCategory.getCtxCategoryId());

        getNamedParameterJdbcTemplate().update(UPDATE_STATEMENT, namedParameters);
    }

    private final String DELETE_BY_CONTEXT_CATEGORY_ID_STATEMENT = "DELETE FROM ctx_category " +
            "WHERE ctx_category_id = :ctx_category_id";

    @Override
    public void deleteByContextCategoryId(int contextCategoryId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("ctx_category_id", contextCategoryId);

        getNamedParameterJdbcTemplate().update(DELETE_BY_CONTEXT_CATEGORY_ID_STATEMENT, namedParameters);
    }
}
