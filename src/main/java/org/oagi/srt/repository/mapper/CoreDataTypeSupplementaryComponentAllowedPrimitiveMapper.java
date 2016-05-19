package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.CoreDataTypeSupplementaryComponentAllowedPrimitive;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CoreDataTypeSupplementaryComponentAllowedPrimitiveMapper
        implements RowMapper<CoreDataTypeSupplementaryComponentAllowedPrimitive> {

    public static CoreDataTypeSupplementaryComponentAllowedPrimitiveMapper INSTANCE
            = new CoreDataTypeSupplementaryComponentAllowedPrimitiveMapper();

    @Override
    public CoreDataTypeSupplementaryComponentAllowedPrimitive mapRow(ResultSet rs, int rowNum) throws SQLException {
        CoreDataTypeSupplementaryComponentAllowedPrimitive cdtScAwdPri =
                new CoreDataTypeSupplementaryComponentAllowedPrimitive();
        cdtScAwdPri.setCdtScAwdPriId(rs.getInt("cdt_sc_awd_pri_id"));
        cdtScAwdPri.setCdtScId(rs.getInt("cdt_sc_id"));
        cdtScAwdPri.setCdtPriId(rs.getInt("cdt_pri_id"));
        cdtScAwdPri.setDefault(rs.getInt("is_default") == 1 ? true : false);
        return cdtScAwdPri;
    }
}
