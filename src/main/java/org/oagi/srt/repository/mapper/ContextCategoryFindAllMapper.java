package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.ContextCategory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ContextCategoryFindAllMapper implements RowMapper<ContextCategory> {

    public static ContextCategoryFindAllMapper INSTANCE = new ContextCategoryFindAllMapper();

    @Override
    public ContextCategory mapRow(ResultSet rs, int rowNum) throws SQLException {
        ContextCategory contextCategory = new ContextCategory();
        contextCategory.setCtxCategoryId(rs.getInt("ctx_category_id"));
        contextCategory.setGuid(rs.getString("guid"));
        contextCategory.setName(rs.getString("name"));
        contextCategory.setDescription(rs.getString("description"));
        return contextCategory;
    }
}
