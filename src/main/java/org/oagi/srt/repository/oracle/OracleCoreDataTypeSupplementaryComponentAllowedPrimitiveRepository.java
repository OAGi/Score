package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.CoreDataTypeSupplementaryComponentAllowedPrimitive;
import org.oagi.srt.repository.impl.BaseCoreDataTypeSupplementaryComponentAllowedPrimitiveRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleCoreDataTypeSupplementaryComponentAllowedPrimitiveRepository
        extends BaseCoreDataTypeSupplementaryComponentAllowedPrimitiveRepository implements OracleRepository {

    @Override
    public String getSequenceName() {
        return "CDT_SC_AWD_PRI_ID_SEQ";
    }

    private final String SAVE_STATEMENT = "INSERT INTO cdt_sc_awd_pri (" +
            "cdt_sc_awd_pri_id, cdt_sc_id, cdt_pri_id, is_default) VALUES (" +
            getSequenceName() + ".NEXTVAL, :cdt_sc_id, :cdt_pri_id, :is_default)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters,
                         CoreDataTypeSupplementaryComponentAllowedPrimitive cdtScAwdPri) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"cdt_sc_awd_pri_id"});
        return keyHolder.getKey().intValue();
    }
}
