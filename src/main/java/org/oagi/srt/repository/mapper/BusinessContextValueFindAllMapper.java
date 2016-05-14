package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.BusinessContextValue;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BusinessContextValueFindAllMapper implements RowMapper<BusinessContextValue> {

    public static BusinessContextValueFindAllMapper INSTANCE = new BusinessContextValueFindAllMapper();

    @Override
    public BusinessContextValue mapRow(ResultSet rs, int rowNum) throws SQLException {
        BusinessContextValue businessContextValue = new BusinessContextValue();
        businessContextValue.setBizCtxValueId(rs.getInt("biz_ctx_value_id"));
        businessContextValue.setBizCtxId(rs.getInt("biz_ctx_id"));
        businessContextValue.setCtxSchemeValueId(rs.getInt("ctx_scheme_value_id"));
        return businessContextValue;
    }
}
