package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.DataTypeRepository;
import org.oagi.srt.repository.entity.DataType;
import org.oagi.srt.repository.mapper.DataTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
public class BaseDataTypeRepository extends NamedParameterJdbcDaoSupport implements DataTypeRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ONE_BY_DT_ID_STATEMENT = "SELECT " +
            "dt_id, guid, type, version_num, previous_version_dt_id, data_type_term, qualifier, " +
            "based_dt_id, den, content_component_den, definition, content_component_definition, revision_doc, state, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_bdt_id, is_deprecated " +
            "FROM dt " +
            "WHERE dt_id = :dt_id";

    @Override
    public DataType findOneByDtId(int dtId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("dt_id", dtId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_DT_ID_STATEMENT,
                namedParameters, DataTypeMapper.INSTANCE);
    }
}
