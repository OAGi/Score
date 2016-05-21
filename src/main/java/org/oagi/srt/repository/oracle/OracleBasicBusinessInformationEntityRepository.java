package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntity;
import org.oagi.srt.repository.impl.BaseBasicBusinessInformationEntityRepository;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@CacheConfig(cacheNames = "BBIEs", keyGenerator = "simpleCacheKeyGenerator")
public class OracleBasicBusinessInformationEntityRepository extends BaseBasicBusinessInformationEntityRepository implements OracleRepository {

    @Override
    public String getSequenceName() {
        return "BBIE_ID_SEQ";
    }

    private final String FIND_GREATEST_ID_STATEMENT = "SELECT NVL(MAX(bbie_id), 0) FROM bbie";

    @Override
    @Cacheable("BBIEs")
    public int findGreatestId() {
        return getJdbcTemplate().queryForObject(FIND_GREATEST_ID_STATEMENT, Integer.class);
    }

    private final String SAVE_STATEMENT = "INSERT INTO bbie (" +
            "bbie_id, guid, based_bcc_id, from_abie_id, to_bbiep_id, bdt_pri_restri_id, code_list_id, " +
            "cardinality_min, cardinality_max, default_value, is_nillable, fixed_value, is_null, " +
            "definition, remark, created_by, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "seq_key, is_used) VALUES (" +
            getSequenceName() + ".NEXTVAL, :guid, :based_bcc_id, :from_abie_id, :to_bbiep_id, :bdt_pri_restri_id, :code_list_id, " +
            ":cardinality_min, :cardinality_max, :default_value, :is_nillable, :fixed_value, :is_null, " +
            ":definition, :remark, :created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
            ":seq_key, :is_used)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, BasicBusinessInformationEntity bbie) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"bbie_id"});
        return keyHolder.getKey().intValue();
    }
}
