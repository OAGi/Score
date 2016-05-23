package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.CodeListValue;
import org.oagi.srt.repository.impl.BaseCodeListValueRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleCodeListValueRepository extends BaseCodeListValueRepository implements OracleRepository {

    @Override
    public String getSequenceName() {
        return "CODE_LIST_VALUE_ID_SEQ";
    }

    private final String SAVE_STATEMENT = "INSERT INTO code_list_value (" +
            "code_list_value_id, code_list_id, value, name, definition, definition_source, " +
            "used_indicator, locked_indicator, extension_indicator) VALUES (" +
            getSequenceName() + ".NEXTVAL, :code_list_id, :value, :name, :definition, :definition_source, " +
            ":used_indicator, :locked_indicator, :extension_indicator)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, CodeListValue codeListValue) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"code_list_value_id"});
        return keyHolder.getKey().intValue();
    }

}
