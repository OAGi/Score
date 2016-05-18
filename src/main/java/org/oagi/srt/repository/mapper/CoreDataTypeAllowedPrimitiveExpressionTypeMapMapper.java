package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.CoreDataTypeAllowedPrimitiveExpressionTypeMap;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CoreDataTypeAllowedPrimitiveExpressionTypeMapMapper
        implements RowMapper<CoreDataTypeAllowedPrimitiveExpressionTypeMap> {

    public static CoreDataTypeAllowedPrimitiveExpressionTypeMapMapper INSTANCE =
            new CoreDataTypeAllowedPrimitiveExpressionTypeMapMapper();

    @Override
    public CoreDataTypeAllowedPrimitiveExpressionTypeMap mapRow(ResultSet rs, int rowNum) throws SQLException {
        CoreDataTypeAllowedPrimitiveExpressionTypeMap cdtAwdPriXpsMap = new CoreDataTypeAllowedPrimitiveExpressionTypeMap();
        cdtAwdPriXpsMap.setCdtAwdPriXpsTypeMapId(rs.getInt("cdt_awd_pri_xps_type_map_id"));
        cdtAwdPriXpsMap.setCdtAwdPriId(rs.getInt("cdt_awd_pri_id"));
        cdtAwdPriXpsMap.setXbtId(rs.getInt("xbt_id"));
        return cdtAwdPriXpsMap;
    }
}
