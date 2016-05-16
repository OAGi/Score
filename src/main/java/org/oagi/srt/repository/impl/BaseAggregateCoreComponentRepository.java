package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.AggregateCoreComponentRepository;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.mapper.AggregateCoreComponentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
public class BaseAggregateCoreComponentRepository extends NamedParameterJdbcDaoSupport
        implements AggregateCoreComponentRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ONE_BY_ACC_ID_STATEMENT = "SELECT " +
            "acc_id, guid, object_class_term, den, definition, based_acc_id, object_class_qualifier, " +
            "oagis_component_type, module, namespace_id, created_by, owner_user_id, last_updated_by, " +
            "creation_timestamp, last_update_timestamp, state, revision_num, revision_tracking_num, " +
            "revision_action, release_id, current_acc_id, is_deprecated " +
            "FROM acc " +
            "WHERE acc_id = :acc_id";

    @Override
    public AggregateCoreComponent findOneByAccId(int accId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("acc_id", accId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_ACC_ID_STATEMENT,
                namedParameters, AggregateCoreComponentMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_ACC_ID_AND_REVISION_NUM_STATEMENT = "SELECT " +
            "acc_id, guid, object_class_term, den, definition, based_acc_id, object_class_qualifier, " +
            "oagis_component_type, module, namespace_id, created_by, owner_user_id, last_updated_by, " +
            "creation_timestamp, last_update_timestamp, state, revision_num, revision_tracking_num, " +
            "revision_action, release_id, current_acc_id, is_deprecated " +
            "FROM acc " +
            "WHERE acc_id = :acc_id AND revision_num = :revision_num";

    @Override
    public AggregateCoreComponent findOneByAccIdAndRevisionNum(int accId, int revisionNum) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("acc_id", accId)
                .addValue("revision_num", revisionNum);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_ACC_ID_AND_REVISION_NUM_STATEMENT,
                namedParameters, AggregateCoreComponentMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO acc (" +
            "guid, object_class_term, den, definition, based_acc_id, object_class_qualifier, " +
            "oagis_component_type, module, namespace_id, created_by, owner_user_id, last_updated_by, " +
            "creation_timestamp, last_update_timestamp, state, revision_num, revision_tracking_num, " +
            "revision_action, release_id, current_acc_id, is_deprecated) VALUES (" +
            ":guid, :object_class_term, :den, :definition, :based_acc_id, :object_class_qualifier, " +
            ":oagis_component_type, :module, :namespace_id, :created_by, :owner_user_id, :last_updated_by, " +
            "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :state, :revision_num, :revision_tracking_num, " +
            ":revision_action, :release_id, :current_acc_id, :is_deprecated)";

    @Override
    public void save(AggregateCoreComponent acc) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", acc.getGuid())
                .addValue("object_class_term", acc.getObjectClassTerm())
                .addValue("den", acc.getDen())
                .addValue("definition", acc.getDefinition())
                .addValue("based_acc_id", acc.getBasedAccId() == -1 ? null : acc.getBasedAccId())
                .addValue("object_class_qualifier", acc.getObjectClassQualifier())
                .addValue("oagis_component_type", acc.getOagisComponentType())
                .addValue("module", acc.getModule())
                .addValue("namespace_id", acc.getNamespaceId())
                .addValue("created_by", acc.getCreatedBy())
                .addValue("owner_user_id", acc.getOwnerUserId())
                .addValue("last_updated_by", acc.getLastUpdatedBy())
                .addValue("state", acc.getState())
                .addValue("revision_num", acc.getRevisionNum())
                .addValue("revision_tracking_num", acc.getRevisionTrackingNum())
                .addValue("revision_action", acc.getRevisionAction())
                .addValue("release_id", acc.getReleaseId())
                .addValue("current_acc_id", acc.getCurrentAccId())
                .addValue("is_deprecated", acc.isDeprecated() ? 1 : 0);

        int aggregateCoreComponentId = doSave(namedParameters, acc);
        acc.setAccId(aggregateCoreComponentId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, AggregateCoreComponent aggregateCoreComponent) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"acc_id"});
        return keyHolder.getKey().intValue();
    }
}
