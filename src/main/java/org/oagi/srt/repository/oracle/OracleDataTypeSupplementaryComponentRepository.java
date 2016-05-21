package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;
import org.oagi.srt.repository.impl.BaseDataTypeSupplementaryComponentRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleDataTypeSupplementaryComponentRepository extends BaseDataTypeSupplementaryComponentRepository implements OracleRepository {

    @Override
    public String getSequenceName() {
        return "DT_SC_ID_SEQ";
    }

    private final String SAVE_STATEMENT = "INSERT INTO dt_sc (" +
            "dt_sc_id, guid, property_term, representation_term, definition, owner_dt_id, " +
            "min_cardinality, max_cardinality, based_dt_sc_id) VALUES (" +
            getSequenceName() + ".NEXTVAL, :guid, :property_term, :representation_term, :definition, :owner_dt_id, " +
            ":min_cardinality, :max_cardinality, :based_dt_sc_id)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, DataTypeSupplementaryComponent dtSc) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"dt_sc_id"});
        return keyHolder.getKey().intValue();
    }
}
