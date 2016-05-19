package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.BusinessDataTypeSupplementaryComponentPrimitiveRestriction;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BusinessDataTypeSupplementaryComponentPrimitiveRestrictionMapper
        implements RowMapper<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> {

    public static BusinessDataTypeSupplementaryComponentPrimitiveRestrictionMapper INSTANCE =
            new BusinessDataTypeSupplementaryComponentPrimitiveRestrictionMapper();

    @Override
    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction mapRow(ResultSet rs, int rowNum) throws SQLException {
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
        bdtScPriRestri.setBdtScPriRestriId(rs.getInt("bdt_sc_pri_restri_id"));
        bdtScPriRestri.setBdtScId(rs.getInt("bdt_sc_id"));
        bdtScPriRestri.setCdtScAwdPriXpsTypeMapId(rs.getInt("cdt_sc_awd_pri_xps_type_map_id"));
        bdtScPriRestri.setCodeListId(rs.getInt("code_list_id"));
        bdtScPriRestri.setDefault(rs.getInt("is_default") == 1 ? true : false);
        bdtScPriRestri.setAgencyIdListId(rs.getInt("agency_id_list_id"));
        return bdtScPriRestri;
    }
}
