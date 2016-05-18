package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.CoreDataTypePrimitive;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CoreDataTypePrimitiveMapper implements RowMapper<CoreDataTypePrimitive> {

    public static CoreDataTypePrimitiveMapper INSTANCE = new CoreDataTypePrimitiveMapper();

    @Override
    public CoreDataTypePrimitive mapRow(ResultSet rs, int rowNum) throws SQLException {
        CoreDataTypePrimitive cdtPri = new CoreDataTypePrimitive();
        cdtPri.setCdtPriId(rs.getInt("cdt_pri_id"));
        cdtPri.setName(rs.getString("name"));
        return cdtPri;
    }
}
