package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.repository.impl.BaseAssociationCoreComponentPropertyRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleAssociationCoreComponentPropertyRepository extends BaseAssociationCoreComponentPropertyRepository implements OracleRepository {

    @Override
    public String getSequenceName() {
        return "ASCCP_ID_SEQ";
    }

    private final String SAVE_STATEMENT = "INSERT INTO asccp (" +
            "asccp_id, guid, property_term, definition, role_of_acc_id, den, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, module, namespace_id, reusable_indicator, is_deprecated, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_asccp_id) VALUES (" +
            getSequenceName() + ".NEXTVAL, :guid, :property_term, :definition, :role_of_acc_id, :den, " +
            ":created_by, :owner_user_id, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
            ":state, :module, :namespace_id, :reusable_indicator, :is_deprecated, " +
            ":revision_num, :revision_tracking_num, :revision_action, :release_id, :current_asccp_id)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, AssociationCoreComponentProperty asccp) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"asccp_id"});
        return keyHolder.getKey().intValue();
    }
}
