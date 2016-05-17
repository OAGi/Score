package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.AgencyIdList;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AgencyIdListMapper implements RowMapper<AgencyIdList> {

    public static AgencyIdListMapper INSTANCE = new AgencyIdListMapper();

    @Override
    public AgencyIdList mapRow(ResultSet rs, int rowNum) throws SQLException {
        AgencyIdList agencyIdList = new AgencyIdList();
        agencyIdList.setAgencyIdListId(rs.getInt("agency_id_list_id"));
        agencyIdList.setGuid(rs.getString("guid"));
        agencyIdList.setEnumTypeGuid(rs.getString("enum_type_guid"));
        agencyIdList.setName(rs.getString("name"));
        agencyIdList.setListId(rs.getString("list_id"));
        agencyIdList.setAgencyId(rs.getInt("agency_id"));
        agencyIdList.setVersionId(rs.getString("version_id"));
        agencyIdList.setDefinition(rs.getString("definition"));
        return agencyIdList;
    }
}
