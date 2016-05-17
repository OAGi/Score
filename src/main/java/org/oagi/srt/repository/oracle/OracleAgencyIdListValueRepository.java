package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.AgencyIdListValue;
import org.oagi.srt.repository.impl.BaseAgencyIdListValueRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleAgencyIdListValueRepository extends BaseAgencyIdListValueRepository {

    private final String SAVE_STATEMENT = "INSERT INTO agency_id_list_value (" +
            "agency_id_list_value_id, value, name, definition, owner_list_id) VALUES (" +
            "agency_id_list_value_agency_id.NEXTVAL, :value, :name, :definition, :owner_list_id)";

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
