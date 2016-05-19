package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapMapper
        implements RowMapper<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> {

    public static CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapMapper INSTANCE =
            new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapMapper();

    @Override
    public CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap mapRow(ResultSet rs, int rowNum) throws SQLException {
        CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();
        cdtScAwdPriXpsTypeMap.setCdtScAwdPriXpsTypeMapId(rs.getInt("cdt_sc_awd_pri_xps_type_map_id"));
        cdtScAwdPriXpsTypeMap.setCdtScAwdPri(rs.getInt("cdt_sc_awd_pri"));
        cdtScAwdPriXpsTypeMap.setXbtId(rs.getInt("xbt_id"));
        return cdtScAwdPriXpsTypeMap;
    }
}
