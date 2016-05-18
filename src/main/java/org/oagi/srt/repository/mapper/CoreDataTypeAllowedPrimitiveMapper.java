package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.CoreDataTypeAllowedPrimitive;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CoreDataTypeAllowedPrimitiveMapper implements RowMapper<CoreDataTypeAllowedPrimitive> {

    public static CoreDataTypeAllowedPrimitiveMapper INSTANCE = new CoreDataTypeAllowedPrimitiveMapper();

    @Override
    public CoreDataTypeAllowedPrimitive mapRow(ResultSet rs, int rowNum) throws SQLException {
        CoreDataTypeAllowedPrimitive cdtAwdPri = new CoreDataTypeAllowedPrimitive();
        cdtAwdPri.setCdtAwdPriId(rs.getInt("cdt_awd_pri_id"));
        cdtAwdPri.setCdtId(rs.getInt("cdt_id"));
        cdtAwdPri.setCdtPriId(rs.getInt("cdt_pri_id"));
        cdtAwdPri.setDefault(rs.getInt("is_default") == 1 ? true : false);
        return cdtAwdPri;
    }
}
