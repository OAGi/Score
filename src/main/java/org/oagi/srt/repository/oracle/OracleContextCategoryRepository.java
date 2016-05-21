package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.repository.impl.BaseContextCategoryRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleContextCategoryRepository extends BaseContextCategoryRepository implements OracleRepository {

    @Override
    public String getSequenceName() {
        return "CTX_CATEGORY_ID_SEQ";
    }

    private final String SAVE_STATEMENT = "INSERT INTO ctx_category (" +
            "ctx_category_id, guid, name, description) VALUES (" +
            getSequenceName() + ".NEXTVAL, :guid, :name, :description)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, ContextCategory contextCategory) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"ctx_category_id"});
        return keyHolder.getKey().intValue();
    }

}
