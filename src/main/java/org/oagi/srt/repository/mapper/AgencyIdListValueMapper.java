package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.AgencyIdListValue;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AgencyIdListValueMapper implements RowMapper<AgencyIdListValue> {

    public static AgencyIdListValueMapper INSTANCE = new AgencyIdListValueMapper();

    @Override
    public AgencyIdListValue mapRow(ResultSet rs, int rowNum) throws SQLException {
        AgencyIdListValue agencyIdListValue = new AgencyIdListValue();
        agencyIdListValue.setAgencyIdListValueId(rs.getInt("agency_id_list_value_id"));
        agencyIdListValue.setValue(rs.getString("value"));
        agencyIdListValue.setName(rs.getString("name"));
        agencyIdListValue.setDefinition(rs.getString("definition"));
        agencyIdListValue.setOwnerListId(rs.getInt("owner_list_id"));
        return agencyIdListValue;
    }
}
