package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.BusinessContextValueRepository;
import org.oagi.srt.repository.entity.BusinessContextValue;
import org.oagi.srt.repository.mapper.BusinessContextValueFindAllMapper;
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
public class BaseBusinessContextValueRepository extends NamedParameterJdbcDaoSupport implements BusinessContextValueRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_BY_CONTEXT_SCHEME_VALUE_ID_STATEMENT = "SELECT " +
            "biz_ctx_value_id, biz_ctx_id, ctx_scheme_value_id " +
            "FROM biz_ctx_value " +
            "WHERE ctx_scheme_value_id = :ctx_scheme_value_id";

    @Override
    public List<BusinessContextValue> findByContextSchemeValueId(int contextSchemeValueId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("ctx_scheme_value_id", contextSchemeValueId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_CONTEXT_SCHEME_VALUE_ID_STATEMENT,
                namedParameters, BusinessContextValueFindAllMapper.INSTANCE);
    }

    private final String FIND_BY_BUSINESS_CONTEXT_ID_STATEMENT = "SELECT " +
            "biz_ctx_value_id, biz_ctx_id, ctx_scheme_value_id " +
            "FROM biz_ctx_value " +
            "WHERE biz_ctx_id = :biz_ctx_id";

    @Override
    public List<BusinessContextValue> findByBusinessContextId(int businessContextId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("biz_ctx_id", businessContextId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_BUSINESS_CONTEXT_ID_STATEMENT,
                namedParameters, BusinessContextValueFindAllMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO biz_ctx_value (" +
            "biz_ctx_id, ctx_scheme_value_id) VALUES (" +
            ":biz_ctx_id, :ctx_scheme_value_id)";

    @Override
    public void save(BusinessContextValue businessContextValue) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("biz_ctx_id", businessContextValue.getBizCtxId())
                .addValue("ctx_scheme_value_id", businessContextValue.getCtxSchemeValueId());

        int businessContextValueId = doSave(namedParameters, businessContextValue);
        businessContextValue.setBizCtxValueId(businessContextValueId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, BusinessContextValue businessContextValue) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"biz_ctx_value_id"});
        return keyHolder.getKey().intValue();
    }
}
