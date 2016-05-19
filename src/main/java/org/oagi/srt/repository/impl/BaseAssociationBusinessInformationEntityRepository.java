package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.AssociationBusinessInformationEntityRepository;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntity;
import org.oagi.srt.repository.mapper.AssociationBusinessInformationEntityMapper;
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
public class BaseAssociationBusinessInformationEntityRepository extends NamedParameterJdbcDaoSupport
        implements AssociationBusinessInformationEntityRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_GREATEST_ID_STATEMENT = "SELECT IFNULL(MAX(asbie_id), 0) FROM asbie";

    @Override
    public int findGreatestId() {
        return getJdbcTemplate().queryForObject(FIND_GREATEST_ID_STATEMENT, Integer.class);
    }

    private final String FIND_BY_FROM_ABIE_ID_STATEMENT =  "SELECT " +
            "asbie_id, guid, from_abie_id, to_asbiep_id, based_ascc, definition, " +
            "cardinality_min, cardinality_max, is_nillable, remark, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "seq_key, is_used " +
            "FROM asbie " +
            "WHERE from_abie_id = :from_abie_id";

    @Override
    public List<AssociationBusinessInformationEntity> findByFromAbieId(int fromAbieId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("from_abie_id", fromAbieId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_FROM_ABIE_ID_STATEMENT,
                namedParameters, AssociationBusinessInformationEntityMapper.INSTANCE);
    }

    private final String FIND_BY_FROM_ABIE_ID_AND_USED_STATEMENT =  "SELECT " +
            "asbie_id, guid, from_abie_id, to_asbiep_id, based_ascc, definition, " +
            "cardinality_min, cardinality_max, is_nillable, remark, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "seq_key, is_used " +
            "FROM asbie " +
            "WHERE from_abie_id = :from_abie_id AND is_used = :is_used";

    @Override
    public List<AssociationBusinessInformationEntity> findByFromAbieIdAndUsed(int fromAbieId, boolean used) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("from_abie_id", fromAbieId)
                .addValue("is_used", used ? 1 : 0);

        return getNamedParameterJdbcTemplate().query(FIND_BY_FROM_ABIE_ID_AND_USED_STATEMENT,
                namedParameters, AssociationBusinessInformationEntityMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO asbie (" +
            "guid, from_abie_id, to_asbiep_id, based_ascc, definition, " +
            "cardinality_min, cardinality_max, is_nillable, remark, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "seq_key, is_used) VALUES (" +
            ":guid, :from_abie_id, :to_asbiep_id, :based_ascc, :definition, " +
            ":cardinality_min, :cardinality_max, :is_nillable, :remark, " +
            ":created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
            ":seq_key, :is_used)";

    @Override
    public void save(AssociationBusinessInformationEntity asbie) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", asbie.getGuid())
                .addValue("from_abie_id", asbie.getFromAbieId())
                .addValue("to_asbiep_id", asbie.getToAsbiepId())
                .addValue("based_ascc", asbie.getBasedAscc())
                .addValue("definition", asbie.getDefinition())
                .addValue("cardinality_min", asbie.getCardinalityMin())
                .addValue("cardinality_max", asbie.getCardinalityMax())
                .addValue("is_nillable", asbie.isNillable() ? 1 : 0)
                .addValue("remark", asbie.getRemark())
                .addValue("created_by", asbie.getCreatedBy())
                .addValue("last_updated_by", asbie.getLastUpdatedBy())
                .addValue("seq_key", asbie.getSeqKey())
                .addValue("is_used", asbie.isUsed() ? 1 : 0);

        int associationBusinessInformationEntityId = doSave(namedParameters, asbie);
        asbie.setAsbieId(associationBusinessInformationEntityId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, AssociationBusinessInformationEntity asbie) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"asbie_id"});
        return keyHolder.getKey().intValue();
    }

    private final String UPDATE_STATEMENT = "UPDATE asbie SET " +
            "guid = :guid, from_abie_id = :from_abie_id, to_asbiep_id = :to_asbiep_id, based_ascc = :based_ascc, " +
            "definition = :definition, cardinality_min = :cardinality_min, cardinality_max = :cardinality_max, " +
            "is_nillable = :is_nillable, remark = :remark, " +
            "last_updated_by = :last_updated_by, last_update_timestamp = CURRENT_TIMESTAMP, " +
            "seq_key = :seq_key, is_used = :is_used " +
            "WHERE asbie_id = :asbie_id";

    @Override
    public void update(AssociationBusinessInformationEntity asbie) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", asbie.getGuid())
                .addValue("from_abie_id", asbie.getFromAbieId())
                .addValue("to_asbiep_id", asbie.getToAsbiepId())
                .addValue("based_ascc", asbie.getBasedAscc())
                .addValue("definition", asbie.getDefinition())
                .addValue("cardinality_min", asbie.getCardinalityMin())
                .addValue("cardinality_max", asbie.getCardinalityMax())
                .addValue("is_nillable", asbie.isNillable() ? 1 : 0)
                .addValue("remark", asbie.getRemark())
                .addValue("last_updated_by", asbie.getLastUpdatedBy())
                .addValue("seq_key", asbie.getSeqKey())
                .addValue("is_used", asbie.isUsed() ? 1 : 0)
                .addValue("asbie_id", asbie.getAsbieId());

        getNamedParameterJdbcTemplate().update(UPDATE_STATEMENT, namedParameters);
    }
}
