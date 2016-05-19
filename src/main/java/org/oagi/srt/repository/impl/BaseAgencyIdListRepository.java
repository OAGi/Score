package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.AgencyIdListRepository;
import org.oagi.srt.repository.entity.AgencyIdList;
import org.oagi.srt.repository.mapper.AgencyIdListMapper;
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
public class BaseAgencyIdListRepository extends NamedParameterJdbcDaoSupport implements AgencyIdListRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ALL_STATEMENT = "SELECT " +
            "agency_id_list_id, guid, enum_type_guid, name, list_id, agency_id, version_id, definition " +
            "FROM agency_id_list";

    @Override
    public List<AgencyIdList> findAll() {
        return getJdbcTemplate().query(FIND_ALL_STATEMENT, AgencyIdListMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_AGENCY_ID_LIST_ID_STATEMENT = "SELECT " +
            "agency_id_list_id, guid, enum_type_guid, name, list_id, agency_id, version_id, definition " +
            "FROM agency_id_list " +
            "WHERE agency_id_list_id = :agency_id_list_id";

    @Override
    public AgencyIdList findOneByAgencyIdListId(int agencyIdListId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("agency_id_list_id", agencyIdListId);

        return getNamedParameterJdbcTemplate().queryForObject(
                FIND_ONE_BY_AGENCY_ID_LIST_ID_STATEMENT, namedParameters, AgencyIdListMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_GUID_STATEMENT = "SELECT " +
            "agency_id_list_id, guid, enum_type_guid, name, list_id, agency_id, version_id, definition " +
            "FROM agency_id_list " +
            "WHERE guid = :guid";

    @Override
    public AgencyIdList findOneByGuid(String guid) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", guid);

        return getNamedParameterJdbcTemplate().queryForObject(
                FIND_ONE_BY_GUID_STATEMENT, namedParameters, AgencyIdListMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_NAME_STATEMENT = "SELECT " +
            "agency_id_list_id, guid, enum_type_guid, name, list_id, agency_id, version_id, definition " +
            "FROM agency_id_list " +
            "WHERE name = :name";

    @Override
    public AgencyIdList findOneByName(String name) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("name", name);

        return getNamedParameterJdbcTemplate().queryForObject(
                FIND_ONE_BY_NAME_STATEMENT, namedParameters, AgencyIdListMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO agency_id_list (" +
            "guid, enum_type_guid, name, list_id, agency_id, version_id, definition) VALUES (" +
            ":guid, :enum_type_guid, :name, :list_id, :agency_id, :version_id, :definition)";

    @Override
    public void save(AgencyIdList agencyIdList) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", agencyIdList.getGuid())
                .addValue("enum_type_guid", agencyIdList.getEnumTypeGuid())
                .addValue("name", agencyIdList.getName())
                .addValue("list_id", agencyIdList.getListId())
                .addValue("agency_id", agencyIdList.getAgencyId() == 0 ? null : agencyIdList.getAgencyId())
                .addValue("version_id", agencyIdList.getVersionId())
                .addValue("definition", agencyIdList.getDefinition());

        int agencyIdListId = doSave(namedParameters, agencyIdList);
        agencyIdList.setAgencyIdListId(agencyIdListId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, AgencyIdList agencyIdList) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"agency_id_list_id"});
        return keyHolder.getKey().intValue();
    }

    private final String UPDATE_STATEMENT = "UPDATE agency_id_list SET agency_id = :agency_id";

    @Override
    public void updateAgencyId(int agencyId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("agency_id", agencyId);

        getNamedParameterJdbcTemplate().update(UPDATE_STATEMENT, namedParameters);
    }
}
