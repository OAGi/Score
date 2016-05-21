package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.BusinessContext;
import org.oagi.srt.repository.impl.BaseBusinessContextRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleBusinessContextRepository extends BaseBusinessContextRepository implements OracleRepository {

    @Override
    public String getSequenceName() {
        return "BIZ_CTX_ID_SEQ";
    }

    private final String SAVE_STATEMENT = "INSERT INTO biz_ctx (" +
            "biz_ctx_id, guid, name, created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES (" +
            getSequenceName() + ".NEXTVAL, :guid, :name, :created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, BusinessContext contextCategory) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"biz_ctx_id"});
        return keyHolder.getKey().intValue();
    }

}
