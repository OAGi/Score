package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.AgencyIdListValueRepository;
import org.oagi.srt.repository.entity.AgencyIdListValue;
import org.oagi.srt.repository.mapper.AgencyIdListValueMapper;
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
public class BaseAgencyIdListValueRepository extends NamedParameterJdbcDaoSupport implements AgencyIdListValueRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ALL_STATEMENT = "SELECT " +
            "agency_id_list_value_id, `value`, `name`, definition, owner_list_id " +
            "FROM agency_id_list_value";

    @Override
    public List<AgencyIdListValue> findAll() {
        return getJdbcTemplate().query(FIND_ALL_STATEMENT, AgencyIdListValueMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_OWNER_LIST_ID_STATEMENT = "SELECT " +
            "agency_id_list_value_id, `value`, `name`, definition, owner_list_id " +
            "FROM agency_id_list_value " +
            "WHERE owner_list_id = :owner_list_id";

    @Override
    public List<AgencyIdListValue> findByOwnerListId(int ownerListId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("owner_list_id", ownerListId);

        return getNamedParameterJdbcTemplate().query(
                FIND_ONE_BY_OWNER_LIST_ID_STATEMENT, namedParameters, AgencyIdListValueMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_AGENCY_ID_LIST_VALUE_ID_STATEMENT = "SELECT " +
            "agency_id_list_value_id, `value`, `name`, definition, owner_list_id " +
            "FROM agency_id_list_value " +
            "WHERE agency_id_list_value_id = :agency_id_list_value_id";

    @Override
    public AgencyIdListValue findOneByAgencyIdListValueId(int agencyIdListValueId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("agency_id_list_value_id", agencyIdListValueId);

        return getNamedParameterJdbcTemplate().queryForObject(
                FIND_ONE_BY_AGENCY_ID_LIST_VALUE_ID_STATEMENT, namedParameters, AgencyIdListValueMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_VALUE_STATEMENT = "SELECT " +
            "agency_id_list_value_id, `value`, `name`, definition, owner_list_id " +
            "FROM agency_id_list_value " +
            "WHERE `value` = :value";

    @Override
    public AgencyIdListValue findOneByValue(String value) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("value", value);

        return getNamedParameterJdbcTemplate().queryForObject(
                FIND_ONE_BY_VALUE_STATEMENT, namedParameters, AgencyIdListValueMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO agency_id_list_value (" +
            "`value`, `name`, definition, owner_list_id) VALUES (" +
            ":value, :name, :definition, :owner_list_id)";

    @Override
    public void save(AgencyIdListValue agencyIdListValue) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("value", agencyIdListValue.getValue())
                .addValue("name", agencyIdListValue.getName())
                .addValue("definition", agencyIdListValue.getDefinition())
                .addValue("owner_list_id", agencyIdListValue.getOwnerListId());

        int agencyIdListValueId = doSave(namedParameters, agencyIdListValue);
        agencyIdListValue.setAgencyIdListValueId(agencyIdListValueId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, AgencyIdListValue agencyIdListValue) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"agency_id_list_value_id"});
        return keyHolder.getKey().intValue();
    }
}
