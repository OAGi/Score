package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.CodeListValue;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CodeListValueFindAllMapper implements RowMapper<CodeListValue> {

    public static final CodeListValueFindAllMapper INSTANCE = new CodeListValueFindAllMapper();

    @Override
    public CodeListValue mapRow(ResultSet rs, int rowNum) throws SQLException {
        CodeListValue codeListValue = new CodeListValue();
        codeListValue.setCodeListValueId(rs.getInt("code_list_value_id"));
        codeListValue.setCodeListId(rs.getInt("code_list_id"));
        codeListValue.setValue(rs.getString("value"));
        codeListValue.setName(rs.getString("name"));
        codeListValue.setDefinition(rs.getString("definition"));
        codeListValue.setDefinitionSource(rs.getString("definition_source"));
        codeListValue.setUsedIndicator(rs.getInt("used_indicator") == 1 ? true : false);
        codeListValue.setLockedIndicator(rs.getInt("locked_indicator") == 1 ? true : false);
        codeListValue.setExtensionIndicator(rs.getInt("extension_indicator") == 1 ? true : false);
        return codeListValue;
    }

}
