package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BusinessDataTypePrimitiveRestrictionMapper implements RowMapper<BusinessDataTypePrimitiveRestriction> {

    public static BusinessDataTypePrimitiveRestrictionMapper INSTANCE = new BusinessDataTypePrimitiveRestrictionMapper();

    @Override
    public BusinessDataTypePrimitiveRestriction mapRow(ResultSet rs, int rowNum) throws SQLException {
        BusinessDataTypePrimitiveRestriction businessDataTypePrimitiveRestriction = new BusinessDataTypePrimitiveRestriction();
        businessDataTypePrimitiveRestriction.setBdtPriRestriId(rs.getInt("bdt_pri_restri_id"));
        businessDataTypePrimitiveRestriction.setBdtId(rs.getInt("bdt_id"));
        businessDataTypePrimitiveRestriction.setCdtAwdPriXpsTypeMapId(rs.getInt("cdt_awd_pri_xps_type_map_id"));
        businessDataTypePrimitiveRestriction.setCodeListId(rs.getInt("code_list_id"));
        businessDataTypePrimitiveRestriction.setDefault(rs.getInt("is_default") == 1 ? true : false);
        businessDataTypePrimitiveRestriction.setAgencyIdListId(rs.getInt("agency_id_list_id"));
        return businessDataTypePrimitiveRestriction;
    }
}
