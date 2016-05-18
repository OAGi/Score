package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.XSDBuiltInType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class XSDBuiltInTypeMapper implements RowMapper<XSDBuiltInType> {

    public static XSDBuiltInTypeMapper INSTANCE = new XSDBuiltInTypeMapper();

    @Override
    public XSDBuiltInType mapRow(ResultSet rs, int rowNum) throws SQLException {
        XSDBuiltInType xbt = new XSDBuiltInType();
        xbt.setXbtId(rs.getInt("xbt_id"));
        xbt.setName(rs.getString("name"));
        xbt.setBuiltInType(rs.getString("builtIn_type"));
        xbt.setSubtypeOfXbtId(rs.getInt("subtype_of_xbt_id"));
        return xbt;
    }
}
