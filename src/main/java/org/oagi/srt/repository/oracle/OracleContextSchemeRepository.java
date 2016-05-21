package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.ContextScheme;
import org.oagi.srt.repository.impl.BaseContextSchemeRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleContextSchemeRepository extends BaseContextSchemeRepository implements OracleRepository {

    @Override
    public String getSequenceName() {
        return "CTX_SCHEME_ID_SEQ";
    }

    private final String SAVE_STATEMENT = "INSERT INTO classification_ctx_scheme (" +
            "classification_ctx_scheme_id, guid, scheme_id, scheme_name, description, " +
            "scheme_agency_id, scheme_version_id, ctx_category_id, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES (" +
            getSequenceName() + ".NEXTVAL, :guid, :scheme_id, :scheme_name, :description, " +
            ":scheme_agency_id, :scheme_version_id, :ctx_category_id, " +
            ":created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, ContextScheme contextScheme) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"classification_ctx_scheme_id"});
        return keyHolder.getKey().intValue();
    }

}
