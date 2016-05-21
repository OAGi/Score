package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.oagi.srt.repository.impl.BaseBasicCoreComponentPropertyRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleBasicCoreComponentPropertyRepository extends BaseBasicCoreComponentPropertyRepository implements OracleRepository {

    @Override
    public String getSequenceName() {
        return "BCCP_ID_SEQ";
    }

    private final String SAVE_STATEMENT = "INSERT INTO bccp (" +
            "bccp_id, guid, property_term, representation_term, bdt_id, den, definition, " +
            "module, namespace_id, is_deprecated, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_bccp_id) VALUES (" +
            getSequenceName() + ".NEXTVAL, :guid, :property_term, :representation_term, :bdt_id, :den, :definition, " +
            ":module, :namespace_id, :is_deprecated, " +
            ":created_by, :owner_user_id, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
            ":state, :revision_num, :revision_tracking_num, :revision_action, :release_id, :current_bccp_id)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters,
                         BasicCoreComponentProperty bccp) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"bccp_id"});
        return keyHolder.getKey().intValue();
    }
}
