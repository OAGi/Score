package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.oagi.srt.repository.impl.BaseAggregateBusinessInformationEntityRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleAggregateBusinessInformationEntityRepository extends BaseAggregateBusinessInformationEntityRepository {

    private final String SAVE_STATEMENT = "INSERT INTO abie (" +
            "abie_id, guid, based_acc_id, is_top_level, biz_ctx_id, definition, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "state, client_id, version, status, remark, biz_term) VALUES (" +
            "abie_abie_id_seq.NEXTVAL, :guid, :based_acc_id, :is_top_level, :biz_ctx_id, :definition, " +
            ":created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
            ":state, :client_id, :version, :status, :remark, :biz_term)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, AggregateBusinessInformationEntity aggregateBusinessInformationEntity) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"abie_id"});
        return keyHolder.getKey().intValue();
    }
}
