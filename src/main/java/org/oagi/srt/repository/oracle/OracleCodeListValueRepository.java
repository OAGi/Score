package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.CodeListValue;
import org.oagi.srt.repository.impl.CodeListValueRepositoryImpl;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleCodeListValueRepository extends CodeListValueRepositoryImpl {

    private final String SAVE_STATEMENT = "INSERT INTO code_list_value (" +
            "code_list_value_id, code_list_id, value, name, definition, definition_source, " +
            "used_indicator, locked_indicator, extension_indicator) VALUES (" +
            "code_list_value_code_list_valu.NEXTVAL, :code_list_id, :value, :name, :definition, :definition_source, " +
            ":used_indicator, :locked_indicator, :extension_indicator)";

    protected int doSave(MapSqlParameterSource namedParameters, CodeListValue codeListValue) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"code_list_value_id"});
        return keyHolder.getKey().intValue();
    }

}
