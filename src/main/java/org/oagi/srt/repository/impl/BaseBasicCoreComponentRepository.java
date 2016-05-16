package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.BasicCoreComponentRepository;
import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.oagi.srt.repository.mapper.BasicCoreComponentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
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
    public List<BasicCoreComponent> findByFromAccId(int fromAccId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("from_acc_id", fromAccId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_FROM_ACC_ID_STATEMENT,
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
    public BasicCoreComponent findOneByBccId(int bccId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bcc_id", bccId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_BCC_ID_STATEMENT,
                namedParameters, BasicCoreComponentMapper.INSTANCE);
    }
}
