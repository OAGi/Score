package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.AssociationBusinessInformationEntityPropertyRepository;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.oagi.srt.repository.mapper.AssociationBusinessInformationEntityPropertyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
public class BaseAssociationBusinessInformationEntityPropertyRepository extends NamedParameterJdbcDaoSupport
        implements AssociationBusinessInformationEntityPropertyRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_GREATEST_ID_STATEMENT = "SELECT MAX(asbiep_id) FROM asbiep";

    @Override
    public int findGreatestId() {
        return getJdbcTemplate().queryForObject(FIND_GREATEST_ID_STATEMENT, Integer.class);
    }

    private final String FIND_ONE_BY_ROLE_OF_ABIE_ID_STATEMENT = "SELECT " +
            "asbiep_id, guid, based_asccp_id, role_of_abie_id, definition, remark, biz_term, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp " +
            "FROM asbiep " +
            "WHERE role_of_abie_id = :role_of_abie_id";

    @Override
    public AssociationBusinessInformationEntityProperty findOneByRoleOfAbieId(int roleOfAbieId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("role_of_abie_id", roleOfAbieId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_ROLE_OF_ABIE_ID_STATEMENT,
                namedParameters, AssociationBusinessInformationEntityPropertyMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_ASBIEP_ID_STATEMENT = "SELECT " +
            "asbiep_id, guid, based_asccp_id, role_of_abie_id, definition, remark, biz_term, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp " +
            "FROM asbiep " +
            "WHERE asbiep_id = :asbiep_id";

    @Override
    public AssociationBusinessInformationEntityProperty findOneByAsbiepId(int asbiepId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("asbiep_id", asbiepId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_ASBIEP_ID_STATEMENT,
                namedParameters, AssociationBusinessInformationEntityPropertyMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO asbiep (" +
            "guid, based_asccp_id, role_of_abie_id, definition, remark, biz_term, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES (" +
            ":guid, :based_asccp_id, :role_of_abie_id, :definition, :remark, :biz_term, " +
            ":created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

    @Override
    public void save(AssociationBusinessInformationEntityProperty asbiep) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", asbiep.getGuid())
                .addValue("based_asccp_id", asbiep.getBasedAsccpId())
                .addValue("role_of_abie_id", asbiep.getRoleOfAbieId())
                .addValue("definition", asbiep.getDefinition())
                .addValue("remark", asbiep.getRemark())
                .addValue("biz_term", asbiep.getBizTerm())
                .addValue("created_by", asbiep.getCreatedBy())
                .addValue("last_updated_by", asbiep.getLastUpdatedBy());

        int associationBusinessInformationEntityPropertyId = doSave(namedParameters, asbiep);
        asbiep.setAsbiepId(associationBusinessInformationEntityPropertyId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, AssociationBusinessInformationEntityProperty asbiep) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"asbiep_id"});
        return keyHolder.getKey().intValue();
    }

    private final String UPDATE_STATEMENT = "UPDATE asbiep SET " +
            "guid = :guid, based_asccp_id = :based_asccp_id, role_of_abie_id = :role_of_abie_id, " +
            "definition = :definition, remark = :remark, biz_term = :biz_term, " +
            "last_updated_by = :last_updated_by, last_update_timestamp = CURRENT_TIMESTAMP " +
            "WHERE asbiep_id = :asbiep_id";

    @Override
    public void update(AssociationBusinessInformationEntityProperty asbiep) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", asbiep.getGuid())
                .addValue("based_asccp_id", asbiep.getBasedAsccpId())
                .addValue("role_of_abie_id", asbiep.getRoleOfAbieId())
                .addValue("definition", asbiep.getDefinition())
                .addValue("remark", asbiep.getRemark())
                .addValue("biz_term", asbiep.getBizTerm())
                .addValue("last_updated_by", asbiep.getLastUpdatedBy())
                .addValue("asbiep_id", asbiep.getAsbiepId());

        getNamedParameterJdbcTemplate().update(UPDATE_STATEMENT, namedParameters);
    }
}
