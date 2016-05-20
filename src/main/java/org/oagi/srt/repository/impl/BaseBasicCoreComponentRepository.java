package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.BasicCoreComponentRepository;
import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.oagi.srt.repository.mapper.BasicCoreComponentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
@CacheConfig(cacheNames = "BCCs", keyGenerator = "simpleCacheKeyGenerator")
public class BaseBasicCoreComponentRepository extends NamedParameterJdbcDaoSupport
        implements BasicCoreComponentRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_BY_FROM_ACC_ID_STATEMENT = "SELECT " +
            "bcc_id, guid, cardinality_min, cardinality_max, to_bccp_id, from_acc_id, " +
            "seq_key, entity_type, den, definition, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_bcc_id, is_deprecated " +
            "FROM bcc " +
            "WHERE from_acc_id = :from_acc_id";

    @Override
    @Cacheable("BCCs")
    public List<BasicCoreComponent> findByFromAccId(int fromAccId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("from_acc_id", fromAccId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_FROM_ACC_ID_STATEMENT,
                namedParameters, BasicCoreComponentMapper.INSTANCE);
    }

    private final String FIND_BY_DEN_STARTS_WITH_STATEMENT = "SELECT " +
            "bcc_id, guid, cardinality_min, cardinality_max, to_bccp_id, from_acc_id, " +
            "seq_key, entity_type, den, definition, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_bcc_id, is_deprecated " +
            "FROM bcc " +
            "WHERE den = :den";

    @Override
    @Cacheable("BCCs")
    public List<BasicCoreComponent> findByDenStartsWith(String den) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("den", den + "%");

        return getNamedParameterJdbcTemplate().query(FIND_BY_DEN_STARTS_WITH_STATEMENT,
                namedParameters, BasicCoreComponentMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_BCC_ID_STATEMENT = "SELECT " +
            "bcc_id, guid, cardinality_min, cardinality_max, to_bccp_id, from_acc_id, " +
            "seq_key, entity_type, den, definition, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_bcc_id, is_deprecated " +
            "FROM bcc " +
            "WHERE bcc_id = :bcc_id";

    @Override
    @Cacheable("BCCs")
    public BasicCoreComponent findOneByBccId(int bccId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bcc_id", bccId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_BCC_ID_STATEMENT,
                namedParameters, BasicCoreComponentMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_GUID_AND_TO_BCCP_ID_STATEMENT = "SELECT " +
            "bcc_id, guid, cardinality_min, cardinality_max, to_bccp_id, from_acc_id, " +
            "seq_key, entity_type, den, definition, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_bcc_id, is_deprecated " +
            "FROM bcc " +
            "WHERE guid = :guid AND to_bccp_id = :to_bccp_id";

    @Override
    @Cacheable("BCCs")
    public BasicCoreComponent findOnebyGuidAndToBccpId(String guid, int toBccpId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", guid)
                .addValue("to_bccp_id", toBccpId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_GUID_AND_TO_BCCP_ID_STATEMENT,
                namedParameters, BasicCoreComponentMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_GUID_AND_FROM_ACC_ID_AND_TO_BCCP_ID_STATEMENT = "SELECT " +
            "bcc_id, guid, cardinality_min, cardinality_max, to_bccp_id, from_acc_id, " +
            "seq_key, entity_type, den, definition, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_bcc_id, is_deprecated " +
            "FROM bcc " +
            "WHERE guid = :guid AND to_bccp_id = :to_bccp_id AND from_acc_id = :from_acc_id";

    @Override
    @Cacheable("BCCs")
    public BasicCoreComponent findOnebyGuidAndFromAccIdAndToBccpId(String guid, int fromAccId, int toBccpId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", guid)
                .addValue("from_acc_id", fromAccId)
                .addValue("to_bccp_id", toBccpId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_GUID_AND_FROM_ACC_ID_AND_TO_BCCP_ID_STATEMENT,
                namedParameters, BasicCoreComponentMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO bcc (" +
            "guid, cardinality_min, cardinality_max, to_bccp_id, from_acc_id, " +
            "seq_key, entity_type, den, definition, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_bcc_id, is_deprecated) VALUES (" +
            ":guid, :cardinality_min, :cardinality_max, :to_bccp_id, :from_acc_id, " +
            ":seq_key, :entity_type, :den, :definition, " +
            ":created_by, :owner_user_id, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
            ":state, :revision_num, :revision_tracking_num, :revision_action, :release_id, :current_bcc_id, :is_deprecated)";

    @Override
    @CacheEvict("BCCs")
    public void save(BasicCoreComponent basicCoreComponent) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", basicCoreComponent.getGuid())
                .addValue("cardinality_min", basicCoreComponent.getCardinalityMin())
                .addValue("cardinality_max", basicCoreComponent.getCardinalityMax())
                .addValue("from_acc_id", basicCoreComponent.getFromAccId())
                .addValue("to_bccp_id", basicCoreComponent.getToBccpId())
                .addValue("seq_key", basicCoreComponent.getSeqKey())
                .addValue("entity_type", basicCoreComponent.getEntityType())
                .addValue("den", basicCoreComponent.getDen())
                .addValue("definition", basicCoreComponent.getDefinition())
                .addValue("created_by", basicCoreComponent.getCreatedBy())
                .addValue("owner_user_id", basicCoreComponent.getOwnerUserId())
                .addValue("last_updated_by", basicCoreComponent.getLastUpdatedBy())
                .addValue("state", basicCoreComponent.getState())
                .addValue("revision_num", basicCoreComponent.getRevisionNum())
                .addValue("revision_tracking_num", basicCoreComponent.getRevisionTrackingNum())
                .addValue("revision_action", basicCoreComponent.getRevisionAction())
                .addValue("release_id", basicCoreComponent.getReleaseId())
                .addValue("current_bcc_id", basicCoreComponent.getCurrentBccId())
                .addValue("is_deprecated", basicCoreComponent.isDeprecated() ? 1 : 0);

        int basicCoreComponentId = doSave(namedParameters, basicCoreComponent);
        basicCoreComponent.setBccId(basicCoreComponentId);
    }

    protected int doSave(MapSqlParameterSource namedParameters,
                         BasicCoreComponent basicCoreComponent) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"bcc_id"});
        return keyHolder.getKey().intValue();
    }

    private final String UPDATE_STATEMENT = "UPDATE bcc SET " +
            "guid = :guid, cardinality_min = :cardinality_min, cardinality_max = :cardinality_max, " +
            "to_bccp_id = :to_bccp_id, from_acc_id = :from_acc_id, seq_key = :seq_key, entity_type = :entity_type, " +
            "den = :den, definition = :definition, " +
            "owner_user_id = :owner_user_id, last_updated_by = :last_updated_by, last_update_timestamp = CURRENT_TIMESTAMP, " +
            "state = :state, revision_num = :revision_num, revision_tracking_num = :revision_tracking_num, " +
            "revision_action = :revision_action, release_id = :release_id, current_bcc_id = :current_bcc_id, " +
            "is_deprecated = :is_deprecated " +
            "WHERE bcc_id = :bcc_id";

    @Override
    @CacheEvict(value = "BCCs")
    public void update(BasicCoreComponent basicCoreComponent) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", basicCoreComponent.getGuid())
                .addValue("cardinality_min", basicCoreComponent.getCardinalityMin())
                .addValue("cardinality_max", basicCoreComponent.getCardinalityMax())
                .addValue("from_acc_id", basicCoreComponent.getFromAccId())
                .addValue("to_bccp_id", basicCoreComponent.getToBccpId())
                .addValue("seq_key", basicCoreComponent.getSeqKey())
                .addValue("entity_type", basicCoreComponent.getEntityType())
                .addValue("den", basicCoreComponent.getDen())
                .addValue("definition", basicCoreComponent.getDefinition())
                .addValue("owner_user_id", basicCoreComponent.getOwnerUserId())
                .addValue("last_updated_by", basicCoreComponent.getLastUpdatedBy())
                .addValue("state", basicCoreComponent.getState())
                .addValue("revision_num", basicCoreComponent.getRevisionNum())
                .addValue("revision_tracking_num", basicCoreComponent.getRevisionTrackingNum())
                .addValue("revision_action", basicCoreComponent.getRevisionAction())
                .addValue("release_id", basicCoreComponent.getReleaseId())
                .addValue("current_bcc_id", basicCoreComponent.getCurrentBccId())
                .addValue("is_deprecated", basicCoreComponent.isDeprecated() ? 1 : 0)
                .addValue("bcc_id" , basicCoreComponent.getBccId());

        getNamedParameterJdbcTemplate().update(UPDATE_STATEMENT, namedParameters);
    }
}
