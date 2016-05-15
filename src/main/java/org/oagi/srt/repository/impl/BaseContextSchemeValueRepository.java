package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.ContextSchemeValueRepository;
import org.oagi.srt.repository.entity.ContextSchemeValue;
import org.oagi.srt.repository.mapper.ContextSchemeValueFindAllMapper;
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
public class BaseContextSchemeValueRepository extends NamedParameterJdbcDaoSupport implements ContextSchemeValueRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_BY_CONTEXT_SCHEME_ID_STATEMENT = "SELECT " +
            "ctx_scheme_value_id, guid, value, meaning, owner_ctx_scheme_id " +
            "FROM ctx_scheme_value " +
            "WHERE owner_ctx_scheme_id = :owner_ctx_scheme_id";

    @Override
    public List<ContextSchemeValue> findByContextSchemeId(int contextSchemeId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("owner_ctx_scheme_id", contextSchemeId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_CONTEXT_SCHEME_ID_STATEMENT,
                namedParameters, ContextSchemeValueFindAllMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_CONTEXT_SCHEME_VALUE_ID_STATEMENT = "SELECT " +
            "ctx_scheme_value_id, guid, value, meaning, owner_ctx_scheme_id " +
            "FROM ctx_scheme_value " +
            "WHERE ctx_scheme_value_id = :ctx_scheme_value_id";

    @Override
    public ContextSchemeValue findOneByContextSchemeValueId(int contextSchemeValueId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("ctx_scheme_value_id", contextSchemeValueId);

        return getNamedParameterJdbcTemplate().queryForObject(
                FIND_ONE_BY_CONTEXT_SCHEME_VALUE_ID_STATEMENT,
                namedParameters, ContextSchemeValueFindAllMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO ctx_scheme_value (" +
            "guid, value, meaning, owner_ctx_scheme_id) VALUES (" +
            ":guid, :value, :meaning, :owner_ctx_scheme_id)";

    @Override
    public void save(ContextSchemeValue contextSchemeValue) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", contextSchemeValue.getGuid())
                .addValue("value", contextSchemeValue.getValue())
                .addValue("meaning", contextSchemeValue.getMeaning())
                .addValue("owner_ctx_scheme_id", contextSchemeValue.getOwnerCtxSchemeId());

        int contextSchemeValueId = doSave(namedParameters, contextSchemeValue);
        contextSchemeValue.setCtxSchemeValueId(contextSchemeValueId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, ContextSchemeValue contextSchemeValue) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"ctx_scheme_value_id"});
        return keyHolder.getKey().intValue();
    }

    private final String DELETE_BY_CONTEXT_SCHEME_ID_STATEMENT =
            "DELETE FROM ctx_scheme_value WHERE owner_ctx_scheme_id = :owner_ctx_scheme_id";

    @Override
    public void deleteByContextSchemeId(int contextSchemeId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("owner_ctx_scheme_id", contextSchemeId);

        getNamedParameterJdbcTemplate().update(DELETE_BY_CONTEXT_SCHEME_ID_STATEMENT, namedParameters);
    }

    private final String DELETE_BY_CONTEXT_SCHEME_VALUE_ID_STATEMENT =
            "DELETE FROM ctx_scheme_value WHERE ctx_scheme_value_id = :ctx_scheme_value_id";

    @Override
    public void deleteByContextSchemeValueId(int contextSchemeValueId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("ctx_scheme_value_id", contextSchemeValueId);

        getNamedParameterJdbcTemplate().update(DELETE_BY_CONTEXT_SCHEME_VALUE_ID_STATEMENT, namedParameters);
    }
}
