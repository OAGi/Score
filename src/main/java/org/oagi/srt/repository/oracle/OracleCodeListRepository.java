package org.oagi.srt.repository.oracle;

import com.codepoetics.protonpack.StreamUtils;
import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.impl.BaseCodeListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.stream.Collectors;

@Repository
public class OracleCodeListRepository extends BaseCodeListRepository implements OracleRepository {

    @Autowired
    private OracleSequenceAccessor oracleSequenceAccessor;

    @Override
    public String getSequenceName() {
        return "CODE_LIST_ID_SEQ";
    }

    private final String SAVE_STATEMENT = "INSERT INTO code_list (" +
            "code_list_id, guid, enum_type_guid, name, list_id, agency_id, version_id, " +
            "definition, remark, definition_source, based_code_list_id, extensible_indicator, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp, state) VALUES (" +
            getSequenceName() + ".NEXTVAL, :guid, :enum_type_guid, :name, :list_id, :agency_id, :version_id, " +
            ":definition, :remark, :definition_source, :based_code_list_id, :extensible_indicator, " +
            ":created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :state)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, CodeList codeList) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"code_list_id"});
        return keyHolder.getKey().intValue();
    }

    private final String SAVE_STATEMENT_FOR_BATCH_UPDATE = "INSERT INTO code_list (" +
            "code_list_id, guid, enum_type_guid, name, list_id, agency_id, version_id, " +
            "definition, remark, definition_source, based_code_list_id, extensible_indicator, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp, state) VALUES (" +
            ":code_list_id, :guid, :enum_type_guid, :name, :list_id, :agency_id, :version_id, " +
            ":definition, :remark, :definition_source, :based_code_list_id, :extensible_indicator, " +
            ":created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :state)";

    public void saveBatch(Collection<CodeList> codeLists) {
        Collection<SqlParameterSource> sqlParameterSources =
                StreamUtils.zip(codeLists.stream(),
                        oracleSequenceAccessor.nextVals(this, getJdbcTemplate(), codeLists.size()).stream(),
                        ((codeList, codeListId) -> {
                            codeList.setCodeListId(codeListId);
                            return codeList;
                        })).map(codeList -> {
                    MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                            .addValue("code_list_id", codeList.getCodeListId())
                            .addValue("guid", codeList.getGuid())
                            .addValue("enum_type_guid", codeList.getEnumTypeGuid())
                            .addValue("name", codeList.getName())
                            .addValue("list_id", codeList.getListId())
                            .addValue("agency_id", codeList.getAgencyId())
                            .addValue("version_id", codeList.getVersionId())
                            .addValue("definition", codeList.getDefinition())
                            .addValue("remark", codeList.getRemark())
                            .addValue("definition_source", codeList.getDefinitionSource())
                            .addValue("based_code_list_id", codeList.getBasedCodeListId() == 0 ? null : codeList.getBasedCodeListId())
                            .addValue("extensible_indicator", codeList.isExtensibleIndicator() == true ? 1 : 0)
                            .addValue("module", codeList.getModule())
                            .addValue("created_by", codeList.getCreatedBy())
                            .addValue("last_updated_by", codeList.getLastUpdatedBy())
                            .addValue("state", codeList.getState());
                    return namedParameters;
                }).collect(Collectors.toList());

        getNamedParameterJdbcTemplate().batchUpdate(SAVE_STATEMENT_FOR_BATCH_UPDATE,
                sqlParameterSources.toArray(new SqlParameterSource[sqlParameterSources.size()]));
    }

}
