package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.BasicCoreComponentPropertyRepository;
import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.oagi.srt.repository.mapper.BasicCoreComponentPropertyMapper;
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

@Repository
@CacheConfig(cacheNames = "BCCPs", keyGenerator = "simpleCacheKeyGenerator")
public class BaseBasicCoreComponentPropertyRepository extends NamedParameterJdbcDaoSupport
        implements BasicCoreComponentPropertyRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ONE_BY_BCCP_ID_STATEMENT = "SELECT " +
            "bccp_id, guid, property_term, representation_term, bdt_id, den, definition, " +
            "module, namespace_id, is_deprecated, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_bccp_id " +
            "FROM bccp " +
            "WHERE bccp_id = :bccp_id";

    @Override
    @Cacheable("BCCPs")
    public BasicCoreComponentProperty findOneByBccpId(int bccpId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bccp_id", bccpId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_BCCP_ID_STATEMENT,
                namedParameters, BasicCoreComponentPropertyMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_BCCP_ID_AND_REVISION_NUM_STATEMENT = "SELECT " +
            "bccp_id, guid, property_term, representation_term, bdt_id, den, definition, " +
            "module, namespace_id, is_deprecated, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_bccp_id " +
            "FROM bccp " +
            "WHERE bccp_id = :bccp_id AND revision_num = :revision_num";

    @Override
    @Cacheable("BCCPs")
    public BasicCoreComponentProperty findOneByBccpIdAndRevisionNum(int bccpId, int revisionNum) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bccp_id", bccpId)
                .addValue("revision_num", revisionNum);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_BCCP_ID_AND_REVISION_NUM_STATEMENT,
                namedParameters, BasicCoreComponentPropertyMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_PROPERTY_TERM_STATEMENT = "SELECT " +
            "bccp_id, guid, property_term, representation_term, bdt_id, den, definition, " +
            "module, namespace_id, is_deprecated, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_bccp_id " +
            "FROM bccp " +
            "WHERE property_term = :property_term";

    @Override
    @Cacheable("BCCPs")
    public BasicCoreComponentProperty findOneByPropertyTerm(String propertyTerm) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("property_term", propertyTerm);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_PROPERTY_TERM_STATEMENT,
                namedParameters, BasicCoreComponentPropertyMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_GUID_STATEMENT = "SELECT " +
            "bccp_id, guid, property_term, representation_term, bdt_id, den, definition, " +
            "module, namespace_id, is_deprecated, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_bccp_id " +
            "FROM bccp " +
            "WHERE guid = :guid";

    @Override
    @Cacheable("BCCPs")
    public BasicCoreComponentProperty findOneByGuid(String guid) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", guid);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_GUID_STATEMENT,
                namedParameters, BasicCoreComponentPropertyMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO bccp (" +
            "guid, property_term, representation_term, bdt_id, den, definition, " +
            "module, namespace_id, is_deprecated, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, revision_num, revision_tracking_num, revision_action, release_id, current_bccp_id) VALUES (" +
            ":guid, :property_term, :representation_term, :bdt_id, :den, :definition, " +
            ":module, :namespace_id, :is_deprecated, " +
            ":created_by, :owner_user_id, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
            ":state, :revision_num, :revision_tracking_num, :revision_action, :release_id, :current_bccp_id)";

    @Override
    @CacheEvict("BCCPs")
    public void save(BasicCoreComponentProperty bccp) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", bccp.getGuid())
                .addValue("property_term", bccp.getPropertyTerm())
                .addValue("representation_term", bccp.getRepresentationTerm())
                .addValue("bdt_id", bccp.getBdtId())
                .addValue("den", bccp.getDen())
                .addValue("definition", bccp.getDefinition())
                .addValue("module", bccp.getModule())
                .addValue("namespace_id", bccp.getNamespaceId() == 0 ? null : bccp.getNamespaceId())
                .addValue("is_deprecated", bccp.isDeprecated() ? 1 : 0)
                .addValue("created_by", bccp.getCreatedBy())
                .addValue("owner_user_id", bccp.getOwnerUserId())
                .addValue("last_updated_by", bccp.getLastUpdatedBy())
                .addValue("state", bccp.getState())
                .addValue("revision_num", bccp.getRevisionNum())
                .addValue("revision_tracking_num", bccp.getRevisionTrackingNum())
                .addValue("revision_action", bccp.getRevisionAction())
                .addValue("release_id", bccp.getReleaseId())
                .addValue("current_bccp_id", bccp.getCurrentBccpId());

        int bccpId = doSave(namedParameters, bccp);
        bccp.setBccpId(bccpId);
    }

    protected int doSave(MapSqlParameterSource namedParameters,
                         BasicCoreComponentProperty bccp) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"bccp_id"});
        return keyHolder.getKey().intValue();
    }
}
