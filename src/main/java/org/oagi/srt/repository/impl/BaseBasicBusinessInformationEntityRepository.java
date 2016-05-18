package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.BasicBusinessInformationEntityRepository;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntity;
import org.oagi.srt.repository.mapper.BasicBusinessInformationEntityMapper;
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
public class BaseBasicBusinessInformationEntityRepository extends NamedParameterJdbcDaoSupport
        implements BasicBusinessInformationEntityRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_GREATEST_ID_STATEMENT = "SELECT MAX(bbie_id) FROM bbie";

    @Override
    public int findGreatestId() {
        return getJdbcTemplate().queryForObject(FIND_GREATEST_ID_STATEMENT, Integer.class);
    }

    private final String FIND_BY_FROM_ABIE_ID_STATEMENT = "SELECT " +
            "bbie_id, guid, based_bcc_id, from_abie_id, to_bbiep_id, bdt_pri_restri_id, code_list_id, " +
            "cardinality_min, cardinality_max, default_value, is_nillable, fixed_value, is_null, " +
            "definition, remark, created_by, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "seq_key, is_used " +
            "FROM bbie " +
            "WHERE from_abie_id = :from_abie_id";

    @Override
    public List<BasicBusinessInformationEntity> findByFromAbieId(int fromAbieId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("from_abie_id", fromAbieId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_FROM_ABIE_ID_STATEMENT,
                namedParameters, BasicBusinessInformationEntityMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO bbie (" +
            "guid, based_bcc_id, from_abie_id, to_bbiep_id, bdt_pri_restri_id, code_list_id, " +
            "cardinality_min, cardinality_max, default_value, is_nillable, fixed_value, is_null, " +
            "definition, remark, created_by, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "seq_key, is_used) VALUES (" +
            ":guid, :based_bcc_id, :from_abie_id, :to_bbiep_id, :bdt_pri_restri_id, :code_list_id, " +
            ":cardinality_min, :cardinality_max, :default_value, :is_nillable, :fixed_value, :is_null, " +
            ":definition, :remark, :created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
            ":seq_key, :is_used)";

    @Override
    public void save(BasicBusinessInformationEntity bbie) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", bbie.getGuid())
                .addValue("based_bcc_id", bbie.getBasedBccId())
                .addValue("from_abie_id", bbie.getFromAbieId())
                .addValue("to_bbiep_id", bbie.getToBbiepId())
                .addValue("bdt_pri_restri_id", bbie.getBdtPriRestriId() == 0 ? null : bbie.getBdtPriRestriId())
                .addValue("code_list_id", bbie.getCodeListId() == 0 ? null : bbie.getCodeListId())
                .addValue("cardinality_min", bbie.getCardinalityMin())
                .addValue("cardinality_max", bbie.getCardinalityMax())
                .addValue("default_value", bbie.getDefaultValue())
                .addValue("is_nillable", bbie.isNillable() ? 1 : 0)
                .addValue("fixed_value", bbie.getFixedValue())
                .addValue("is_null", bbie.isNill())
                .addValue("definition", bbie.getDefinition())
                .addValue("remark", bbie.getRemark())
                .addValue("created_by", bbie.getCreatedBy())
                .addValue("last_updated_by", bbie.getLastUpdatedBy())
                .addValue("seq_key", bbie.getSeqKey())
                .addValue("is_used", bbie.isUsed() ? 1 : 0);

        int basicBusinessInformationEntityId = doSave(namedParameters, bbie);
        bbie.setBbieId(basicBusinessInformationEntityId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, BasicBusinessInformationEntity bbie) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"bbie_id"});
        return keyHolder.getKey().intValue();
    }

    private final String UPDATE_STATEMENT = "UPDATE bbie SET " +
            "guid = :guid, based_bcc_id = :based_bcc_id, from_abie_id = :from_abie_id, to_bbiep_id = :to_bbiep_id, " +
            "bdt_pri_restri_id = :bdt_pri_restri_id, code_list_id = :code_list_id, " +
            "cardinality_min = :cardinality_min, cardinality_max = :cardinality_max, default_value = :default_value, " +
            "is_nillable = :is_nillable, fixed_value = :fixed_value, is_null = :is_null, " +
            "definition = :definition, remark = :remark, last_updated_by = :last_updated_by, last_update_timestamp = CURRENT_TIMESTAMP, " +
            "seq_key = :seq_key, is_used = :is_used" +
            "WHERE bbie_id = :bbie_id";

    @Override
    public void update(BasicBusinessInformationEntity bbie) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", bbie.getGuid())
                .addValue("based_bcc_id", bbie.getBasedBccId())
                .addValue("from_abie_id", bbie.getFromAbieId())
                .addValue("to_bbiep_id", bbie.getToBbiepId())
                .addValue("bdt_pri_restri_id", bbie.getBdtPriRestriId() == 0 ? null : bbie.getBdtPriRestriId())
                .addValue("code_list_id", bbie.getCodeListId() == 0 ? null : bbie.getCodeListId())
                .addValue("cardinality_min", bbie.getCardinalityMin())
                .addValue("cardinality_max", bbie.getCardinalityMax())
                .addValue("default_value", bbie.getDefaultValue())
                .addValue("is_nillable", bbie.isNillable() ? 1 : 0)
                .addValue("fixed_value", bbie.getFixedValue())
                .addValue("is_null", bbie.isNill())
                .addValue("definition", bbie.getDefinition())
                .addValue("remark", bbie.getRemark())
                .addValue("last_updated_by", bbie.getLastUpdatedBy())
                .addValue("seq_key", bbie.getSeqKey())
                .addValue("is_used", bbie.isUsed() ? 1 : 0)
                .addValue("bbie_id", bbie.getBbieId());

        getNamedParameterJdbcTemplate().update(UPDATE_STATEMENT, namedParameters);
    }
}
