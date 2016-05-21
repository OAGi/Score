package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.oagi.srt.repository.impl.BaseBasicCoreComponentRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleBasicCoreComponentRepository extends BaseBasicCoreComponentRepository implements OracleRepository {

    @Override
    public String getSequenceName() {
        return "BCC_ID_SEQ";
    }

    private final String SAVE_STATEMENT = "INSERT INTO bcc (" +
            "bcc_id, guid, cardinality_min, cardinality_max, to_bccp_id, from_acc_id, " +
            "seq_key, entity_type, den, definition, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_bcc_id, is_deprecated) VALUES (" +
            getSequenceName() + ".NEXTVAL, :guid, :cardinality_min, :cardinality_max, :to_bccp_id, :from_acc_id, " +
            ":seq_key, :entity_type, :den, :definition, " +
            ":created_by, :owner_user_id, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
            ":state, :revision_num, :revision_tracking_num, :revision_action, :release_id, :current_bcc_id, :is_deprecated)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters,
                         BasicCoreComponent basicCoreComponent) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"bcc_id"});
        return keyHolder.getKey().intValue();
    }
}
