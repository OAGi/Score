package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.ContextScheme;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContextSchemeFindAllMapper implements RowMapper<ContextScheme> {

    public static ContextSchemeFindAllMapper INSTANCE = new ContextSchemeFindAllMapper();

    @Override
    public ContextScheme mapRow(ResultSet rs, int rowNum) throws SQLException {
        ContextScheme contextScheme = new ContextScheme();
        contextScheme.setClassificationCtxSchemeId(rs.getInt("classification_ctx_scheme_id"));
        contextScheme.setGuid(rs.getString("guid"));
        contextScheme.setSchemeId(rs.getString("scheme_id"));
        contextScheme.setSchemeName(rs.getString("scheme_name"));
        contextScheme.setDescription(rs.getString("description"));
        contextScheme.setSchemeAgencyId(rs.getString("scheme_agency_id"));
        contextScheme.setSchemeVersionId(rs.getString("scheme_version_id"));
        contextScheme.setCtxCategoryId(rs.getInt("ctx_category_id"));
        contextScheme.setCreatedBy(rs.getInt("created_by"));
        contextScheme.setLastUpdatedBy(rs.getInt("last_updated_by"));
        contextScheme.setCreationTimestamp(new Date(rs.getTimestamp("creation_timestamp").getTime()));
        contextScheme.setLastUpdateTimestamp(new Date(rs.getTimestamp("last_update_timestamp").getTime()));
        return contextScheme;
    }
}
