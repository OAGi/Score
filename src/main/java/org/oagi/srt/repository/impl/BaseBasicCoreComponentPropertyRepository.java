package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.BasicCoreComponentPropertyRepository;
import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.oagi.srt.repository.mapper.BasicCoreComponentPropertyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
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
    public BasicCoreComponentProperty findOneByBccpIdAndRevisionNum(int bccpId, int revisionNum) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bccp_id", bccpId)
                .addValue("revision_num", revisionNum);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_BCCP_ID_AND_REVISION_NUM_STATEMENT,
                namedParameters, BasicCoreComponentPropertyMapper.INSTANCE);
    }
}
