package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.AssociationCoreComponentPropertyRepository;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.repository.mapper.AssociationCoreComponentPropertyMapper;
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
public class BaseAssociationCoreComponentPropertyRepository extends NamedParameterJdbcDaoSupport
        implements AssociationCoreComponentPropertyRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ALL_STATEMENT = "SELECT " +
            "asccp_id, guid, property_term, definition, role_of_acc_id, den, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, module, namespace_id, reusable_indicator, is_deprecated, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_asccp_id " +
            "FROM asccp " +
            "ORDER BY property_term ASC";

    @Override
    public List<AssociationCoreComponentProperty> findAll() {
        return getJdbcTemplate().query(FIND_ALL_STATEMENT, AssociationCoreComponentPropertyMapper.INSTANCE);
    }

    private final String FIND_BY_PROPERTY_TERM_CONTAINING_STATEMENT = "SELECT " +
            "asccp_id, guid, property_term, definition, role_of_acc_id, den, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, module, namespace_id, reusable_indicator, is_deprecated, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_asccp_id " +
            "FROM asccp " +
            "WHERE property_term LIKE :property_term";

    @Override
    public List<AssociationCoreComponentProperty> findByPropertyTermContaining(String propertyTerm) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("property_term", "%" + propertyTerm + "%");

        return getNamedParameterJdbcTemplate().query(FIND_BY_PROPERTY_TERM_CONTAINING_STATEMENT,
                namedParameters, AssociationCoreComponentPropertyMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_ASCCP_ID_STATEMENT = "SELECT " +
            "asccp_id, guid, property_term, definition, role_of_acc_id, den, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, module, namespace_id, reusable_indicator, is_deprecated, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_asccp_id " +
            "FROM asccp " +
            "WHERE asccp_id = :asccp_id";

    @Override
    public AssociationCoreComponentProperty findOneByAsccpId(int asccpId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("asccp_id", asccpId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_ASCCP_ID_STATEMENT,
                namedParameters, AssociationCoreComponentPropertyMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_ASCCP_ID_AND_REVISION_NUM_STATEMENT = "SELECT " +
            "asccp_id, guid, property_term, definition, role_of_acc_id, den, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, module, namespace_id, reusable_indicator, is_deprecated, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_asccp_id " +
            "FROM asccp " +
            "WHERE asccp_id = :asccp_id AND revision_num = :revision_num";

    @Override
    public AssociationCoreComponentProperty findOneByAsccpIdAndRevisionNum(int asccpId, int revisionNum) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("asccp_id", asccpId)
                .addValue("revision_num", revisionNum);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_ASCCP_ID_AND_REVISION_NUM_STATEMENT,
                namedParameters, AssociationCoreComponentPropertyMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_ROLE_OF_ACC_ID_STATEMENT = "SELECT " +
            "asccp_id, guid, property_term, definition, role_of_acc_id, den, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, module, namespace_id, reusable_indicator, is_deprecated, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_asccp_id " +
            "FROM asccp " +
            "WHERE role_of_acc_id = :role_of_acc_id";

    @Override
    public AssociationCoreComponentProperty findOneByRoleOfAccId(int roleOfAccId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("role_of_acc_id", roleOfAccId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_ROLE_OF_ACC_ID_STATEMENT,
                namedParameters, AssociationCoreComponentPropertyMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_GUID_STATEMENT = "SELECT " +
            "asccp_id, guid, property_term, definition, role_of_acc_id, den, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, module, namespace_id, reusable_indicator, is_deprecated, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_asccp_id " +
            "FROM asccp " +
            "WHERE guid = :guid";

    @Override
    public AssociationCoreComponentProperty findOneByGuid(String guid) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", guid);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_GUID_STATEMENT,
                namedParameters, AssociationCoreComponentPropertyMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO asccp (" +
            "guid, property_term, definition, role_of_acc_id, den, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, module, namespace_id, reusable_indicator, is_deprecated, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_asccp_id) VALUES (" +
            ":guid, :property_term, :definition, :role_of_acc_id, :den, " +
            ":created_by, :owner_user_id, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
            ":state, :module, :namespace_id, :reusable_indicator, :is_deprecated, " +
            ":revision_num, :revision_tracking_num, :revision_action, :release_id, :current_asccp_id)";

    @Override
    public void save(AssociationCoreComponentProperty asccp) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", asccp.getGuid())
                .addValue("property_term", asccp.getPropertyTerm())
                .addValue("definition", asccp.getDefinition())
                .addValue("role_of_acc_id", asccp.getRoleOfAccId())
                .addValue("den", asccp.getDen())
                .addValue("created_by", asccp.getCreatedBy())
                .addValue("owner_user_id", asccp.getOwnerUserId())
                .addValue("last_updated_by", asccp.getLastUpdatedBy())
                .addValue("state", asccp.getState())
                .addValue("module", asccp.getModule())
                .addValue("namespace_id", asccp.getNamespaceId())
                .addValue("reusable_indicator", asccp.isReusableIndicator() ? 1 : 0)
                .addValue("is_deprecated", asccp.isDeprecated() ? 1 : 0)
                .addValue("revision_num", asccp.getRevisionNum())
                .addValue("revision_tracking_num", asccp.getRevisionTrackingNum())
                .addValue("revision_action", asccp.getRevisionAction())
                .addValue("release_id", asccp.getReleaseId())
                .addValue("current_asccp_id", asccp.getCurrentAsccpId());

        int associationCoreComponentPropertyId = doSave(namedParameters, asccp);
        asccp.setAsccpId(associationCoreComponentPropertyId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, AssociationCoreComponentProperty asccp) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"asccp_id"});
        return keyHolder.getKey().intValue();
    }
}
