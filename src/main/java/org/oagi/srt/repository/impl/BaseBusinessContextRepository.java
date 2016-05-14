package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.BusinessContextRepository;
import org.oagi.srt.repository.entity.BusinessContext;
import org.oagi.srt.repository.mapper.BusinessContextFindAllMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
public class BaseBusinessContextRepository extends NamedParameterJdbcDaoSupport implements BusinessContextRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ONE_BY_BUSINESS_CONTEXT_ID_STATEMENT = "SELECT " +
            "biz_ctx_id, guid, name, created_by, last_updated_by, creation_timestamp, last_update_timestamp " +
            "FROM biz_ctx " +
            "WHERE biz_ctx_id = :biz_ctx_id";

    @Override
    public BusinessContext findOneByBusinessContextId(int businessContextId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("biz_ctx_id", businessContextId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_BUSINESS_CONTEXT_ID_STATEMENT,
                namedParameters, BusinessContextFindAllMapper.INSTANCE);
    }
}
