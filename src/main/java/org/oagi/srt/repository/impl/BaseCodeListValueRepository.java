package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.CodeListValueRepository;
import org.oagi.srt.repository.entity.CodeListValue;
import org.oagi.srt.repository.mapper.CodeListValueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
public class BaseCodeListValueRepository extends NamedParameterJdbcDaoSupport implements CodeListValueRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_BY_CODE_LIST_ID_STATEMENT = "SELECT " +
            "code_list_value_id, code_list_id, value, name, definition, definition_source, used_indicator, locked_indicator, extension_indicator " +
            "FROM code_list_value " +
            "WHERE code_list_id = :code_list_id";

    @Override
    public List<CodeListValue> findByCodeListId(int codeListId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("code_list_id", codeListId);
        return getNamedParameterJdbcTemplate().query(
                FIND_BY_CODE_LIST_ID_STATEMENT, namedParameters, CodeListValueMapper.INSTANCE);
    }

    private final String UPDATE_CODE_LIST_ID_BY_CODE_LIST_VALUE_ID_STATEMENT = "UPDATE code_list_value SET " +
            "code_list_id = :code_list_id WHERE code_list_value_id = :code_list_value_id";

    @Override
    public void updateCodeListIdByCodeListValueId(int codeListId, int codeListValueId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("code_list_id", codeListId)
                .addValue("code_list_value_id", codeListValueId);
        getNamedParameterJdbcTemplate().update(UPDATE_CODE_LIST_ID_BY_CODE_LIST_VALUE_ID_STATEMENT, namedParameters);
    }

    private final String SAVE_STATEMENT = "INSERT INTO code_list_value (" +
            "code_list_id, value, name, definition, definition_source, used_indicator, locked_indicator, extension_indicator) VALUES (" +
            ":code_list_id, :value, :name, :definition, :definition_source, :used_indicator, :locked_indicator, :extension_indicator)";

    @Override
    public void save(CodeListValue codeListValue) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("code_list_id", codeListValue.getCodeListId())
                .addValue("value", codeListValue.getValue())
                .addValue("name", codeListValue.getName())
                .addValue("definition", codeListValue.getDefinition())
                .addValue("definition_source", codeListValue.getDefinitionSource())
                .addValue("used_indicator", codeListValue.isUsedIndicator() == true ? 1 : 0)
                .addValue("locked_indicator", codeListValue.isLockedIndicator() == true ? 1 : 0)
                .addValue("extension_indicator", codeListValue.isExtensionIndicator() == true ? 1 : 0);

        int codeListValueId = doSave(namedParameters, codeListValue);
        codeListValue.setCodeListValueId(codeListValueId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, CodeListValue codeListValue) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"code_list_value_id"});
        return keyHolder.getKey().intValue();
    }
}
