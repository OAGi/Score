package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.BusinessContextValueRepository;
import org.oagi.srt.repository.entity.BusinessContextValue;
import org.oagi.srt.repository.mapper.BusinessContextValueFindAllMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
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
}
