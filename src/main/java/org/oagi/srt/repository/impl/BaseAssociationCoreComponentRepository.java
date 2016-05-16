package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.AssociationCoreComponentRepository;
import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.mapper.AssociationCoreComponentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
public class BaseAssociationCoreComponentRepository extends NamedParameterJdbcDaoSupport
        implements AssociationCoreComponentRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_BY_FROM_ACC_ID_STATEMENT = "SELECT " +
            "ascc_id, guid, cardinality_min, cardinality_max, seq_key, " +
            "from_acc_id, to_asccp_id, den, definition, is_deprecated, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_ascc_id " +
            "FROM ascc " +
            "WHERE from_acc_id = :from_acc_id";

    @Override
    public List<AssociationCoreComponent> findByFromAccId(int fromAccId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("from_acc_id", fromAccId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_FROM_ACC_ID_STATEMENT,
                namedParameters, AssociationCoreComponentMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_ASCC_ID_STATEMENT = "SELECT " +
            "ascc_id, guid, cardinality_min, cardinality_max, seq_key, " +
            "from_acc_id, to_asccp_id, den, definition, is_deprecated, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_ascc_id " +
            "FROM ascc " +
            "WHERE ascc_id = :ascc_id";

    @Override
    public AssociationCoreComponent findOneByAsccId(int asccId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("ascc_id", asccId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_ASCC_ID_STATEMENT,
                namedParameters, AssociationCoreComponentMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO ascc (" +
            "guid, cardinality_min, cardinality_max, seq_key, " +
            "from_acc_id, to_asccp_id, den, definition, is_deprecated, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_ascc_id) VALUES (" +
            ":guid, :cardinality_min, :cardinality_max, :seq_key, " +
            ":from_acc_id, :to_asccp_id, :den, :definition, :is_deprecated, " +
            ":created_by, :owner_user_id, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
            ":state, :revision_num, :revision_tracking_num, :revision_action, :release_id, :current_ascc_id)";

    @Override
    public void save(AssociationCoreComponent ascc) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", ascc.getGuid())
                .addValue("cardinality_min", ascc.getCardinalityMin())
                .addValue("cardinality_max", ascc.getCardinalityMax())
                .addValue("seq_key", ascc.getSeqKey())
                .addValue("from_acc_id", ascc.getFromAccId())
                .addValue("to_asccp_id", ascc.getToAsccpId())
                .addValue("den", ascc.getDen())
                .addValue("definition", ascc.getDefinition())
                .addValue("is_deprecated", ascc.isDeprecated() ? 1 : 0)
                .addValue("created_by", ascc.getCreatedBy())
                .addValue("owner_user_id", ascc.getOwnerUserId())
                .addValue("last_updated_by", ascc.getLastUpdatedBy())
                .addValue("state", ascc.getState())
                .addValue("revision_num", ascc.getRevisionNum())
                .addValue("revision_tracking_num", ascc.getRevisionTrackingNum())
                .addValue("revision_action", ascc.getRevisionAction())
                .addValue("release_id", ascc.getReleaseId())
                .addValue("current_ascc_id", ascc.getCurrentAsccId());

        int associationCoreComponentId = doSave(namedParameters, ascc);
        ascc.setAsccId(associationCoreComponentId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, AssociationCoreComponent ascc) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"ascc_id"});
        return keyHolder.getKey().intValue();
    }
}
