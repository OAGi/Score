package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.impl.BaseAggregateCoreComponentRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleAggregateCoreComponentRepository extends BaseAggregateCoreComponentRepository implements OracleRepository {

    @Override
    public String getSequenceName() {
        return "ACC_ID_SEQ";
    }

    private final String SAVE_STATEMENT = "INSERT INTO acc (" +
            "acc_id, guid, object_class_term, den, definition, based_acc_id, object_class_qualifier, " +
            "oagis_component_type, module, namespace_id, created_by, owner_user_id, last_updated_by, " +
            "creation_timestamp, last_update_timestamp, state, revision_num, revision_tracking_num, " +
            "revision_action, release_id, current_acc_id, is_deprecated) VALUES (" +
            getSequenceName() + ".NEXTVAL, :guid, :object_class_term, :den, :definition, :based_acc_id, :object_class_qualifier, " +
            ":oagis_component_type, :module, :namespace_id, :created_by, :owner_user_id, :last_updated_by, " +
            "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :state, :revision_num, :revision_tracking_num, " +
            ":revision_action, :release_id, :current_acc_id, :is_deprecated)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, AggregateCoreComponent aggregateCoreComponent) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"acc_id"});
        return keyHolder.getKey().intValue();
    }
}
