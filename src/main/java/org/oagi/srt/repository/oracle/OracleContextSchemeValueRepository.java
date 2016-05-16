package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.ContextSchemeValue;
import org.oagi.srt.repository.impl.BaseContextSchemeValueRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleContextSchemeValueRepository extends BaseContextSchemeValueRepository {

    private final String SAVE_STATEMENT = "INSERT INTO ctx_scheme_value (" +
            "ctx_scheme_value_id, guid, value, meaning, owner_ctx_scheme_id) VALUES (" +
            "ctx_scheme_value_ctx_scheme_va.NEXTVAL, :guid, :value, :meaning, :owner_ctx_scheme_id)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, ContextSchemeValue contextSchemeValue) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"ctx_scheme_value_id"});
        return keyHolder.getKey().intValue();
    }

}
