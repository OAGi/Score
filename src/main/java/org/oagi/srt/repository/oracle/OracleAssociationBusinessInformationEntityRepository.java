package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.AssociationBusinessInformationEntity;
import org.oagi.srt.repository.impl.BaseAssociationBusinessInformationEntityRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleAssociationBusinessInformationEntityRepository
        extends BaseAssociationBusinessInformationEntityRepository {

    private final String SAVE_STATEMENT = "INSERT INTO asbie (" +
            "asbie_id, guid, from_abie_id, to_asbiep_id, based_ascc, definition, " +
            "cardinality_min, cardinality_max, is_nillable, remark, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp, " +
            "seq_key, is_used) VALUES (" +
            "asbie_asbie_id_seq.NEXTVAL, :guid, :from_abie_id, :to_asbiep_id, :based_ascc, :definition, " +
            ":cardinality_min, :cardinality_max, :is_nillable, :remark, " +
            ":created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
            ":seq_key, :is_used)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, AssociationBusinessInformationEntity asbie) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"asbie_id"});
        return keyHolder.getKey().intValue();
    }
}
