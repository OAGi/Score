package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.AggregateBusinessInformationEntityRepository;
import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.oagi.srt.repository.mapper.AggregateBusinessInformationEntityMapper;
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
public class BaseAggregateBusinessInformationEntityRepository extends NamedParameterJdbcDaoSupport
        implements AggregateBusinessInformationEntityRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_BY_TOP_LEVEL_STATEMENT = "SELECT " +
            "abie_id, guid, based_acc_id, is_top_level, biz_ctx_id, definition, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, client_id, version, status, remark, biz_term " +
            "FROM abie " +
            "WHERE is_top_level = :is_top_level";

    @Override
    public List<AggregateBusinessInformationEntity> findByTopLevel(boolean topLevel) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("is_top_level", topLevel == true ? 1 : 0);

        return getNamedParameterJdbcTemplate().query(FIND_BY_TOP_LEVEL_STATEMENT,
                namedParameters, AggregateBusinessInformationEntityMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_ABIE_ID_STATEMENT = "SELECT " +
            "abie_id, guid, based_acc_id, is_top_level, biz_ctx_id, definition, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, client_id, version, status, remark, biz_term " +
            "FROM abie " +
            "WHERE abie_id = :abie_id";

    @Override
    public AggregateBusinessInformationEntity findOneByAbieId(int abieId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("abie_id", abieId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_ABIE_ID_STATEMENT,
                namedParameters, AggregateBusinessInformationEntityMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO abie (" +
            "guid, based_acc_id, is_top_level, biz_ctx_id, definition, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, client_id, version, status, remark, biz_term) VALUES (" +
            ":guid, :based_acc_id, :is_top_level, :biz_ctx_id, :definition, " +
            ":created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
            ":state, :client_id, :version, :status, :remark, :biz_term)";

    @Override
    public void save(AggregateBusinessInformationEntity abie) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", abie.getGuid())
                .addValue("based_acc_id", abie.getBasedAccId())
                .addValue("is_top_level", abie.isTopLevel() == true ? 1 : 0)
                .addValue("biz_ctx_id", abie.getBizCtxId())
                .addValue("definition", abie.getDefinition())
                .addValue("created_by", abie.getCreatedBy())
                .addValue("last_updated_by", abie.getLastUpdatedBy())
                .addValue("state", abie.getState() == 0 ? null : abie.getState())
                .addValue("client_id", abie.getClientId() == 0 ? null : abie.getClientId())
                .addValue("version", abie.getVersion())
                .addValue("status", abie.getStatus())
                .addValue("remark", abie.getRemark())
                .addValue("biz_term", abie.getBizTerm());

        int aggregateBusinessInformationEntityId = doSave(namedParameters, abie);
        abie.setAbieId(aggregateBusinessInformationEntityId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, AggregateBusinessInformationEntity aggregateBusinessInformationEntity) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"abie_id"});
        return keyHolder.getKey().intValue();
    }

    private final String UPDATE_STATEMENT = "UPDATE abie SET " +
            "guid = :guid, based_acc_id = :based_acc_id, is_top_level = :is_top_level, biz_ctx_id = :biz_ctx_id, definition = :definition, " +
            "last_updated_by = :last_updated_by, last_update_timestamp = CURRENT_TIMESTAMP, " +
            "state = :state, client_id = :client_id, version = :version, status = :status, remark = :remark, biz_term = :biz_term " +
            "WHERE abie_id = :abie_id";

    @Override
    public void update(AggregateBusinessInformationEntity abie) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", abie.getGuid())
                .addValue("based_acc_id", abie.getBasedAccId())
                .addValue("is_top_level", abie.isTopLevel() == true ? 1 : 0)
                .addValue("biz_ctx_id", abie.getBizCtxId())
                .addValue("definition", abie.getDefinition())
                .addValue("last_updated_by", abie.getLastUpdatedBy())
                .addValue("state", abie.getState() == 0 ? null : abie.getState())
                .addValue("client_id", abie.getClientId())
                .addValue("version", abie.getVersion())
                .addValue("status", abie.getStatus())
                .addValue("remark", abie.getRemark())
                .addValue("biz_term", abie.getBizTerm())
                .addValue("abie_id", abie.getAbieId());

        getNamedParameterJdbcTemplate().update(UPDATE_STATEMENT, namedParameters);
    }
}
