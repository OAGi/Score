package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.AgencyIdListValueRepository;
import org.oagi.srt.repository.entity.AgencyIdListValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
public class BaseAgencyIdListValueRepository extends NamedParameterJdbcDaoSupport implements AgencyIdListValueRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String SAVE_STATEMENT = "INSERT INTO agency_id_list_value (" +
            "`value`, `name`, `definition`, `owner_list_id`) VALUES (" +
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
