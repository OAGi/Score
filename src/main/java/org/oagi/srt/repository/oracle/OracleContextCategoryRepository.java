package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.repository.impl.BaseContextCategoryRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleContextCategoryRepository extends BaseContextCategoryRepository {

    private final String SAVE_STATEMENT = "INSERT INTO ctx_category (" +
            "ctx_category_id, guid, name, description) VALUES (" +
            "ctx_category_ctx_category_id_s.NEXTVAL, :guid, :name, :description)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, ContextCategory contextCategory) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"ctx_category_id"});
        return keyHolder.getKey().intValue();
    }

}
