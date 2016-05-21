package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;
import org.oagi.srt.repository.impl.BaseBusinessDataTypePrimitiveRestrictionRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleBusinessDataTypePrimitiveRestrictionRepository
        extends BaseBusinessDataTypePrimitiveRestrictionRepository implements OracleRepository {

    @Override
    public String getSequenceName() {
        return "BDT_PRI_RESTRI_ID_SEQ";
    }

    private final String SAVE_STATEMENT = "INSERT INTO bdt_pri_restri (" +
            "bdt_pri_restri_id, bdt_id, cdt_awd_pri_xps_type_map_id, code_list_id, " +
            "is_default, agency_id_list_id) VALUES (" +
            getSequenceName() + ".NEXTVAL, :bdt_id, :cdt_awd_pri_xps_type_map_id, :code_list_id, " +
            ":is_default, :agency_id_list_id)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, BusinessDataTypePrimitiveRestriction bdtPriRestri) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"bdt_pri_restri_id"});
        return keyHolder.getKey().intValue();
    }

}
