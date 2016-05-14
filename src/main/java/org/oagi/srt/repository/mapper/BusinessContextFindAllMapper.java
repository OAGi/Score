package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.BusinessContext;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class BusinessContextFindAllMapper implements RowMapper<BusinessContext> {

    public static BusinessContextFindAllMapper INSTANCE = new BusinessContextFindAllMapper();

    @Override
    public BusinessContext mapRow(ResultSet rs, int rowNum) throws SQLException {
        BusinessContext businessContext = new BusinessContext();
        businessContext.setBizCtxId(rs.getInt("biz_ctx_id"));
        businessContext.setGuid(rs.getString("guid"));
        businessContext.setName(rs.getString("name"));
        businessContext.setCreatedBy(rs.getInt("created_by"));
        businessContext.setLastUpdatedBy(rs.getInt("last_updated_by"));
        businessContext.setCreationTimestamp(
                new Date(rs.getTimestamp("creation_timestamp").getTime()));
        businessContext.setLastUpdateTimestamp(
                new Date(rs.getTimestamp("last_update_timestamp").getTime()));
        return businessContext;
    }
}
