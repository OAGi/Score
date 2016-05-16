package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.BasicBusinessInformationEntityPropertyRepository;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntityProperty;
import org.oagi.srt.repository.mapper.BasicBusinessInformationEntityPropertyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
public class BaseBasicBusinessInformationEntityPropertyRepository extends NamedParameterJdbcDaoSupport
        implements BasicBusinessInformationEntityPropertyRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_GREATEST_ID_STATEMENT = "SELECT MAX(bbiep_id) FROM bbiep";

    @Override
    public int findGreatestId() {
        return getJdbcTemplate().queryForObject(FIND_GREATEST_ID_STATEMENT, Integer.class);
    }

    private final String FIND_ONE_BY_BBIEP_ID_STATEMENT = "SELECT " +
            "bbiep_id, guid, based_bccp_id, definition, remark, biz_term, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp " +
            "FROM bbiep " +
            "WHERE bbiep_id = :bbiep_id";

    @Override
    public BasicBusinessInformationEntityProperty findOneByBbiepId(int bbiepId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bbiep_id", bbiepId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_BBIEP_ID_STATEMENT,
                namedParameters, BasicBusinessInformationEntityPropertyMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO bbiep (" +
            "guid, based_bccp_id, definition, remark, biz_term, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES (" +
            ":guid, :based_bccp_id, :definition, :remark, :biz_term, " +
            ":created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

    @Override
    public void save(BasicBusinessInformationEntityProperty bbiep) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", bbiep.getGuid())
                .addValue("based_bccp_id", bbiep.getBasedBccpId())
                .addValue("definition", bbiep.getDefinition())
                .addValue("remark", bbiep.getRemark())
                .addValue("biz_term", bbiep.getBizTerm())
                .addValue("created_by", bbiep.getCreatedBy())
                .addValue("last_updated_by", bbiep.getLastUpdatedBy());

        int basicBusinessInformationEntityPropertyId = doSave(namedParameters, bbiep);
        bbiep.setBbiepId(basicBusinessInformationEntityPropertyId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, BasicBusinessInformationEntityProperty bbiep) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"bbiep_id"});
        return keyHolder.getKey().intValue();
    }

    private final String UPDATE_STATEMENT = "UPDATE bbiep SET " +
            "guid = :guid, based_bccp_id = :based_bccp_id, definition = :definition, remark = :remark, biz_term = :biz_term, " +
            "last_updated_by = :last_updated_by, last_update_timestamp = CURRENT_TIMESTAMP " +
            "WHERE bbiep_id = :bbiep_id";

    @Override
    public void update(BasicBusinessInformationEntityProperty bbiep) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", bbiep.getGuid())
                .addValue("based_bccp_id", bbiep.getBasedBccpId())
                .addValue("definition", bbiep.getDefinition())
                .addValue("remark", bbiep.getRemark())
                .addValue("biz_term", bbiep.getBizTerm())
                .addValue("last_updated_by", bbiep.getLastUpdatedBy())
                .addValue("bbiep_id", bbiep.getBbiepId());

        getNamedParameterJdbcTemplate().update(UPDATE_STATEMENT, namedParameters);
    }
}
