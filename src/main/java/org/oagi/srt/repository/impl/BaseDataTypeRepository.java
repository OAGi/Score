package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.DataTypeRepository;
import org.oagi.srt.repository.entity.DataType;
import org.oagi.srt.repository.mapper.DataTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
@CacheConfig(cacheNames = "DTs", keyGenerator = "simpleCacheKeyGenerator")
public class BaseDataTypeRepository extends NamedParameterJdbcDaoSupport implements DataTypeRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ONE_BY_DT_ID_STATEMENT = "SELECT " +
            "dt_id, guid, type, version_num, previous_version_dt_id, data_type_term, qualifier, " +
            "based_dt_id, den, content_component_den, definition, content_component_definition, revision_doc, module, state, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_bdt_id, is_deprecated " +
            "FROM dt " +
            "WHERE dt_id = :dt_id";

    @Override
    @Cacheable("DTs")
    public DataType findOneByDtId(int dtId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("dt_id", dtId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_DT_ID_STATEMENT,
                namedParameters, DataTypeMapper.INSTANCE);
    }

    private final String FIND_BY_TYPE_STATEMENT = "SELECT " +
            "dt_id, guid, type, version_num, previous_version_dt_id, data_type_term, qualifier, " +
            "based_dt_id, den, content_component_den, definition, content_component_definition, revision_doc, module, state, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_bdt_id, is_deprecated " +
            "FROM dt " +
            "WHERE type = :type";

    @Override
    @Cacheable("DTs")
    public List<DataType> findByType(int type) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("type", type);

        return getNamedParameterJdbcTemplate().query(FIND_BY_TYPE_STATEMENT,
                namedParameters, DataTypeMapper.INSTANCE);
    }

    private final String FIND_BY_DATA_TYPE_TERM_STATEMENT = "SELECT " +
            "dt_id, guid, type, version_num, previous_version_dt_id, data_type_term, qualifier, " +
            "based_dt_id, den, content_component_den, definition, content_component_definition, revision_doc, module, state, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_bdt_id, is_deprecated " +
            "FROM dt " +
            "WHERE data_type_term = :data_type_term";

    @Override
    @Cacheable("DTs")
    public List<DataType> findByDataTypeTerm(String dataTypeTerm) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("data_type_term", dataTypeTerm);

        return getNamedParameterJdbcTemplate().query(FIND_BY_DATA_TYPE_TERM_STATEMENT,
                namedParameters, DataTypeMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_DATA_TYPE_TERM_AND_TYPE_STATEMENT = "SELECT " +
            "dt_id, guid, type, version_num, previous_version_dt_id, data_type_term, qualifier, " +
            "based_dt_id, den, content_component_den, definition, content_component_definition, revision_doc, module, state, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_bdt_id, is_deprecated " +
            "FROM dt " +
            "WHERE data_type_term = :data_type_term AND type = :type";

    @Override
    @Cacheable("DTs")
    public DataType findOneByDataTypeTermAndType(String dataTypeTerm, int type) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("data_type_term", dataTypeTerm)
                .addValue("type", type);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_DATA_TYPE_TERM_AND_TYPE_STATEMENT,
                namedParameters, DataTypeMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_GUID_STATEMENT = "SELECT " +
            "dt_id, guid, type, version_num, previous_version_dt_id, data_type_term, qualifier, " +
            "based_dt_id, den, content_component_den, definition, content_component_definition, revision_doc, module, state, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_bdt_id, is_deprecated " +
            "FROM dt " +
            "WHERE guid = :guid";

    @Override
    @Cacheable("DTs")
    public DataType findOneByGuid(String guid) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", guid);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_GUID_STATEMENT,
                namedParameters, DataTypeMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_GUID_AND_TYPE_STATEMENT = "SELECT " +
            "dt_id, guid, type, version_num, previous_version_dt_id, data_type_term, qualifier, " +
            "based_dt_id, den, content_component_den, definition, content_component_definition, revision_doc, module, state, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_bdt_id, is_deprecated " +
            "FROM dt " +
            "WHERE guid = :guid AND type = :type";

    @Override
    @Cacheable("DTs")
    public DataType findOneByGuidAndType(String guid, int type) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", guid)
                .addValue("type", type);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_GUID_AND_TYPE_STATEMENT,
                namedParameters, DataTypeMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_DEN_STATEMENT = "SELECT " +
            "dt_id, guid, type, version_num, previous_version_dt_id, data_type_term, qualifier, " +
            "based_dt_id, den, content_component_den, definition, content_component_definition, revision_doc, module, state, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_bdt_id, is_deprecated " +
            "FROM dt " +
            "WHERE den = :den";

    @Override
    @Cacheable("DTs")
    public DataType findOneByDen(String den) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("den", den);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_DEN_STATEMENT,
                namedParameters, DataTypeMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_TYPE_AND_DEN_STATEMENT = "SELECT " +
            "dt_id, guid, type, version_num, previous_version_dt_id, data_type_term, qualifier, " +
            "based_dt_id, den, content_component_den, definition, content_component_definition, revision_doc, module, state, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_bdt_id, is_deprecated " +
            "FROM dt " +
            "WHERE den = :den AND type = :type";

    @Override
    @Cacheable("DTs")
    public DataType findOneByTypeAndDen(int type, String den) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("den", den)
                .addValue("type", type);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_TYPE_AND_DEN_STATEMENT,
                namedParameters, DataTypeMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_GUID_AND_DEN_STATEMENT = "SELECT " +
            "dt_id, guid, type, version_num, previous_version_dt_id, data_type_term, qualifier, " +
            "based_dt_id, den, content_component_den, definition, content_component_definition, revision_doc, module, state, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_bdt_id, is_deprecated " +
            "FROM dt " +
            "WHERE guid = :guid AND type = :type";

    @Override
    @Cacheable("DTs")
    public DataType findOneByGuidAndDen(String guid, String den) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("den", den)
                .addValue("guid", guid);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_GUID_AND_DEN_STATEMENT,
                namedParameters, DataTypeMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO dt (" +
            "guid, type, version_num, previous_version_dt_id, data_type_term, qualifier, " +
            "based_dt_id, den, content_component_den, definition, content_component_definition, revision_doc, module, state, " +
            "created_by, owner_user_id, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "revision_num, revision_tracking_num, revision_action, release_id, current_bdt_id, is_deprecated) VALUES (" +
            ":guid, :type, :version_num, :previous_version_dt_id, :data_type_term, :qualifier, " +
            ":based_dt_id, :den, :content_component_den, :definition, :content_component_definition, :revision_doc, :module, :state, " +
            ":created_by, :owner_user_id, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
            ":revision_num, :revision_tracking_num, :revision_action, :release_id, :current_bdt_id, :is_deprecated)";

    @Override
    @CacheEvict("DTs")
    public void save(DataType dataType) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", dataType.getGuid())
                .addValue("type", dataType.getType())
                .addValue("version_num", dataType.getVersionNum())
                .addValue("previous_version_dt_id", dataType.getPreviousVersionDtId() == 0 ? null : dataType.getPreviousVersionDtId())
                .addValue("data_type_term", dataType.getDataTypeTerm())
                .addValue("qualifier", dataType.getQualifier())
                .addValue("based_dt_id", dataType.getBasedDtId() == 0 ? null : dataType.getBasedDtId())
                .addValue("den", dataType.getDen())
                .addValue("content_component_den", dataType.getContentComponentDen())
                .addValue("definition", dataType.getDefinition())
                .addValue("content_component_definition", dataType.getContentComponentDefinition())
                .addValue("revision_doc", dataType.getRevisionDoc())
                .addValue("module", dataType.getModule())
                .addValue("state", dataType.getState())
                .addValue("created_by", dataType.getCreatedBy())
                .addValue("owner_user_id", dataType.getOwnerUserId())
                .addValue("last_updated_by", dataType.getLastUpdatedBy())
                .addValue("revision_num", dataType.getRevisionNum())
                .addValue("revision_tracking_num", dataType.getRevisionTrackingNum())
                .addValue("revision_action", dataType.getRevisionAction())
                .addValue("release_id", dataType.getReleaseId() == 0 ? null : dataType.getReleaseId())
                .addValue("current_bdt_id", dataType.getCurrentBdtId() == 0 ? null : dataType.getCurrentBdtId())
                .addValue("is_deprecated", dataType.isDeprecated() ? 1 : 0);

        int dtId = doSave(namedParameters, dataType);
        dataType.setDtId(dtId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, DataType dataType) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"dt_id"});
        return keyHolder.getKey().intValue();
    }
}
