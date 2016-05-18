package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.DataType;
import org.oagi.srt.repository.impl.BaseDataTypeRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleDataTypeRepository extends BaseDataTypeRepository {

    private final String SAVE_STATEMENT = "INSERT INTO dt (" +
            "dt_id, guid, type, version_num, previous_version_dt_id, data_type_term, qualifier, " +
            "based_dt_id, den, content_component_den, definition, content_component_definition, revision_doc, state, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_bdt_id, is_deprecated) VALUES (" +
            "dt_dt_id_seq.NEXTVAL, :guid, :type, :version_num, :previous_version_dt_id, :data_type_term, :qualifier, " +
            ":based_dt_id, :den, :content_component_den, :definition, :content_component_definition, :revision_doc, :state, " +
            ":created_by, :owner_user_id, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
            ":revision_num, :revision_tracking_num, :revision_action, :release_id, :current_bdt_id, :is_deprecated)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, DataType dataType) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"dt_id"});
        return keyHolder.getKey().intValue();
    }

}
