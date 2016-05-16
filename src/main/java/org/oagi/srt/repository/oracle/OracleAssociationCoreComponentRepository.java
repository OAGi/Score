package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.impl.BaseAssociationCoreComponentRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleAssociationCoreComponentRepository extends BaseAssociationCoreComponentRepository {

    private final String SAVE_STATEMENT = "INSERT INTO ascc (" +
            "ascc_id, guid, cardinality_min, cardinality_max, seq_key, " +
            "from_acc_id, to_asccp_id, den, definition, is_deprecated, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_ascc_id) VALUES (" +
            "ascc_ascc_id_seq.NEXTVAL, :guid, :cardinality_min, :cardinality_max, :seq_key, " +
            ":from_acc_id, :to_asccp_id, :den, :definition, :is_deprecated, " +
            ":created_by, :owner_user_id, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
            ":state, :revision_num, :revision_tracking_num, :revision_action, :release_id, :current_ascc_id)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, AssociationCoreComponent ascc) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"ascc_id"});
        return keyHolder.getKey().intValue();
    }
}
