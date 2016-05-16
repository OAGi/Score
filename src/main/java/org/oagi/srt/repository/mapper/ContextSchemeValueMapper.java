package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.ContextSchemeValue;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ContextSchemeValueMapper implements RowMapper<ContextSchemeValue> {

    public static ContextSchemeValueMapper INSTANCE = new ContextSchemeValueMapper();

    @Override
    public ContextSchemeValue mapRow(ResultSet rs, int rowNum) throws SQLException {
        ContextSchemeValue contextSchemeValue = new ContextSchemeValue();
        contextSchemeValue.setCtxSchemeValueId(rs.getInt("ctx_scheme_value_id"));
        contextSchemeValue.setGuid(rs.getString("guid"));
        contextSchemeValue.setValue(rs.getString("value"));
        contextSchemeValue.setMeaning(rs.getString("meaning"));
        contextSchemeValue.setOwnerCtxSchemeId(rs.getInt("owner_ctx_scheme_id"));
        return contextSchemeValue;
    }
}
