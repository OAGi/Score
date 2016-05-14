package org.oagi.srt.repository.impl;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.repository.CodeListRepository;
import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.mapper.CodeListFindAllMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
public class BaseCodeListRepository extends NamedParameterJdbcDaoSupport implements CodeListRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ALL_STATEMENT = "SELECT " +
            "code_list_id, guid, enum_type_guid, name, list_id, agency_id, version_id, definition, remark, " +
            "definition_source, based_code_list_id, extensible_indicator, created_by, last_updated_by, " +
            "creation_timestamp, last_update_timestamp, state " +
            "FROM code_list " +
            "ORDER BY creation_timestamp DESC";

    @Override
    public List<CodeList> findAll() {
        return getJdbcTemplate().query(FIND_ALL_STATEMENT, CodeListFindAllMapper.INSTANCE);
    }

    private final String FIND_BY_NAME_CONTAINING_STATEMENT = "SELECT " +
            "code_list_id, guid, enum_type_guid, name, list_id, agency_id, version_id, definition, remark, " +
            "definition_source, based_code_list_id, extensible_indicator, created_by, last_updated_by, " +
            "creation_timestamp, last_update_timestamp, state " +
            "FROM code_list " +
            "WHERE name LIKE :name";

    @Override
    public List<CodeList> findByNameContaining(String name) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("name", "%" + name + "%");
        return getNamedParameterJdbcTemplate().query(
                FIND_BY_NAME_CONTAINING_STATEMENT, namedParameters, CodeListFindAllMapper.INSTANCE);
    }

    private final String FIND_BY_NAME_CONTAINING_AND_STATE_IS_PUBLISHED_AND_EXTENSIBLE_INDICATOR_IS_TRUE_STATEMENT = "SELECT " +
            "code_list_id, guid, enum_type_guid, name, list_id, agency_id, version_id, definition, remark, " +
            "definition_source, based_code_list_id, extensible_indicator, created_by, last_updated_by, " +
            "creation_timestamp, last_update_timestamp, state " +
            "FROM code_list " +
            "WHERE name LIKE :name AND state = :state AND extensible_indicator = :extensible_indicator";

    @Override
    public List<CodeList> findByNameContainingAndStateIsPublishedAndExtensibleIndicatorIsTrue(String name) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("name", "%" + name + "%");
        namedParameters.addValue("state", SRTConstants.CODE_LIST_STATE_PUBLISHED);
        namedParameters.addValue("extensible_indicator", 1);
        return getNamedParameterJdbcTemplate().query(
                FIND_BY_NAME_CONTAINING_AND_STATE_IS_PUBLISHED_AND_EXTENSIBLE_INDICATOR_IS_TRUE_STATEMENT,
                namedParameters, CodeListFindAllMapper.INSTANCE);
    }

    private final String FIND_BY_CODE_LIST_ID_STATEMENT = "SELECT " +
            "code_list_id, guid, enum_type_guid, name, list_id, agency_id, version_id, definition, remark, " +
            "definition_source, based_code_list_id, extensible_indicator, created_by, last_updated_by, " +
            "creation_timestamp, last_update_timestamp, state " +
            "FROM code_list " +
            "WHERE code_list_id = :code_list_id";

    @Override
    public List<CodeList> findByCodeListId(int codeListId) {
        SqlParameterSource namedParameters = new MapSqlParameterSource("code_list_id", codeListId);
        return getNamedParameterJdbcTemplate().query(
                FIND_BY_CODE_LIST_ID_STATEMENT, namedParameters, CodeListFindAllMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_GUID_AND_ENUM_TYPE_GUID_AND_NAME_AND_DEFINITION_STATEMENT = "SELECT " +
            "code_list_id, guid, enum_type_guid, name, list_id, agency_id, version_id, definition, remark, " +
            "definition_source, based_code_list_id, extensible_indicator, created_by, last_updated_by, " +
            "creation_timestamp, last_update_timestamp, state " +
            "FROM code_list " +
            "WHERE guid = :guid AND enum_type_guid = :enum_type_guid AND name = :name AND definition = :definition";

    @Override
    public CodeList findOneByGuidAndEnumTypeGuidAndNameAndDefinition(String guid, String enumTypeGuid, String name, String definition) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", guid)
                .addValue("enum_type_guid", enumTypeGuid)
                .addValue("name", name)
                .addValue("definition", definition);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_GUID_AND_ENUM_TYPE_GUID_AND_NAME_AND_DEFINITION_STATEMENT,
                namedParameters, CodeListFindAllMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_GUID_AND_ENUM_TYPE_GUID_AND_CODE_LIST_ID_AND_NAME_AND_DEFINITION_STATEMENT = "SELECT " +
            "code_list_id, guid, enum_type_guid, name, list_id, agency_id, version_id, definition, remark, " +
            "definition_source, based_code_list_id, extensible_indicator, created_by, last_updated_by, " +
            "creation_timestamp, last_update_timestamp, state " +
            "FROM code_list " +
            "WHERE guid = :guid AND enum_type_guid = :enum_type_guid AND code_list_id = :code_list_id AND name = :name AND definition = :definition";

    @Override
    public CodeList findOneByGuidAndEnumTypeGuidAndCodeListIdAndNameAndDefinition(
            String guid, String enumTypeGuid, int codeListId, String name, String definition) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", guid)
                .addValue("enum_type_guid", enumTypeGuid)
                .addValue("code_list_id", codeListId)
                .addValue("name", name)
                .addValue("definition", definition);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_GUID_AND_ENUM_TYPE_GUID_AND_CODE_LIST_ID_AND_NAME_AND_DEFINITION_STATEMENT,
                namedParameters, CodeListFindAllMapper.INSTANCE);
    }

    private final String UPDATE_STATEMENT = "UPDATE code_list SET " +
            "guid = :guid, enum_type_guid = :enum_type_guid, name = :name, list_id = :list_id, agency_id = :agency_id, " +
            "version_id = :version_id, definition = :definition, remark = :remark, definition_source = :definition_source, " +
            "based_code_list_id = :based_code_list_id, extensible_indicator = :extensible_indicator, " +
            "last_updated_by = :last_updated_by, last_update_timestamp = CURRENT_TIMESTAMP, state = :state " +
            "WHERE code_list_id = :code_list_id";

    @Override
    public void update(CodeList codeList) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", codeList.getGuid())
                .addValue("enum_type_guid", codeList.getEnumTypeGuid())
                .addValue("name", codeList.getName())
                .addValue("list_id", codeList.getListId())
                .addValue("agency_id", codeList.getAgencyId())
                .addValue("version_id", codeList.getVersionId())
                .addValue("definition", codeList.getDefinition())
                .addValue("remark", codeList.getRemark())
                .addValue("definition_source", codeList.getDefinitionSource())
                .addValue("based_code_list_id", codeList.getBasedCodeListId())
                .addValue("extensible_indicator", codeList.isExtensibleIndicator() == true ? 1 : 0)
                .addValue("last_updated_by", codeList.getLastUpdatedBy())
                .addValue("state", codeList.getState())
                .addValue("code_list_id", codeList.getCodeListId());

        getNamedParameterJdbcTemplate().update(UPDATE_STATEMENT, namedParameters);
    }

    private final String UPDATE_STATE_BY_CODE_LIST_ID_STATEMENT =
            "UPDATE code_list SET state = :state WHERE code_list_id = :code_list_id";

    @Override
    public void updateStateByCodeListId(String state, int codeListId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("state", state)
                .addValue("code_list_id", codeListId);

        getNamedParameterJdbcTemplate().update(UPDATE_STATE_BY_CODE_LIST_ID_STATEMENT, namedParameters);
    }

    private final String SAVE_STATEMENT = "INSERT INTO code_list (" +
            "guid, enum_type_guid, name, list_id, agency_id, version_id, " +
            "definition, remark, definition_source, based_code_list_id, extensible_indicator, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp, state) VALUES (" +
            ":guid, :enum_type_guid, :name, :list_id, :agency_id, :version_id, " +
            ":definition, :remark, :definition_source, :based_code_list_id, :extensible_indicator, " +
            ":created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :state)";

    @Override
    public void save(CodeList codeList) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", codeList.getGuid())
                .addValue("enum_type_guid", codeList.getEnumTypeGuid())
                .addValue("name", codeList.getName())
                .addValue("list_id", codeList.getListId())
                .addValue("agency_id", codeList.getAgencyId())
                .addValue("version_id", codeList.getVersionId())
                .addValue("definition", codeList.getDefinition())
                .addValue("remark", codeList.getRemark())
                .addValue("definition_source", codeList.getDefinitionSource())
                .addValue("based_code_list_id", codeList.getBasedCodeListId())
                .addValue("extensible_indicator", codeList.isExtensibleIndicator() == true ? 1 : 0)
                .addValue("created_by", codeList.getCreatedBy())
                .addValue("last_updated_by", codeList.getLastUpdatedBy())
                .addValue("state", codeList.getState());

        int codeListId = doSave(namedParameters, codeList);
        codeList.setCodeListId(codeListId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, CodeList codeList) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"code_list_id"});
        return keyHolder.getKey().intValue();
    }
}
