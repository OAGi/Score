package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.impl.BaseCodeListRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleBaseCodeListRepository extends BaseCodeListRepository {

    private final String SAVE_STATEMENT = "INSERT INTO code_list (" +
            "code_list_id, guid, enum_type_guid, name, list_id, agency_id, version_id, " +
            "definition, remark, definition_source, based_code_list_id, extensible_indicator, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp, state) VALUES (" +
            "code_list_code_list_id_SEQ.NEXTVAL, :guid, :enum_type_guid, :name, :list_id, :agency_id, :version_id, " +
            ":definition, :remark, :definition_source, :based_code_list_id, :extensible_indicator, " +
            ":created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :state)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, CodeList codeList) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"code_list_id"});
        return keyHolder.getKey().intValue();
    }

}
