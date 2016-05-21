package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.BusinessContextValue;
import org.oagi.srt.repository.impl.BaseBusinessContextValueRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleBusinessContextValueRepository extends BaseBusinessContextValueRepository implements OracleRepository {

    @Override
    public String getSequenceName() {
        return "BIZ_CTX_VALUE_ID_SEQ";
    }

    private final String SAVE_STATEMENT = "INSERT INTO biz_ctx_value (" +
            "biz_ctx_value_id, biz_ctx_id, ctx_scheme_value_id) VALUES (" +
            getSequenceName() + ".NEXTVAL, :biz_ctx_id, :ctx_scheme_value_id)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, BusinessContextValue contextCategory) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"biz_ctx_value_id"});
        return keyHolder.getKey().intValue();
    }

}
