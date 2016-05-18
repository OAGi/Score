package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.CodeList;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CodeListMapper implements RowMapper<CodeList> {

    public static CodeListMapper INSTANCE = new CodeListMapper();

    @Override
    public CodeList mapRow(ResultSet rs, int rowNum) throws SQLException {
        CodeList codeList = new CodeList();
        codeList.setCodeListId(rs.getInt("code_list_id"));
        codeList.setGuid(rs.getString("guid"));
        codeList.setEnumTypeGuid(rs.getString("enum_type_guid"));
        codeList.setName(rs.getString("name"));
        codeList.setListId(rs.getString("list_id"));
        codeList.setAgencyId(rs.getInt("agency_id"));
        codeList.setVersionId(rs.getString("version_id"));
        codeList.setDefinition(rs.getString("definition"));
        codeList.setRemark(rs.getString("remark"));
        codeList.setDefinitionSource(rs.getString("definition_source"));
        codeList.setBasedCodeListId(rs.getInt("based_code_list_id"));
        codeList.setExtensibleIndicator(rs.getShort("extensible_indicator") == 1 ? true : false);
        codeList.setModule(rs.getString("module"));
        codeList.setCreatedBy(rs.getInt("created_by"));
        codeList.setLastUpdatedBy(rs.getInt("last_updated_by"));
        codeList.setCreationTimestamp(new Date(rs.getTimestamp("creation_timestamp").getTime()));
        codeList.setLastUpdateTimestamp(new Date(rs.getTimestamp("last_update_timestamp").getTime()));
        codeList.setState(rs.getString("state"));
        return codeList;
    }

}
