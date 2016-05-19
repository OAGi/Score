package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.BusinessDataTypeSupplementaryComponentPrimitiveRestriction;
import org.oagi.srt.repository.impl.BaseBusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleBusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository
        extends BaseBusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository {

    private final String SAVE_STATEMENT = "INSERT INTO bdt_sc_pri_restri (" +
            "bdt_sc_pri_restri_id, bdt_sc_id, cdt_sc_awd_pri_xps_type_map_id, " +
            "code_list_id, is_default, agency_id_list_id) VALUES (" +
            "bdt_sc_pri_restri_bdt_sc_pri_r.NEXTVAL, :bdt_sc_id, :cdt_sc_awd_pri_xps_type_map_id, " +
            ":code_list_id, :is_default, :agency_id_list_id)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters,
                         BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"bdt_sc_pri_restri_id"});
        return keyHolder.getKey().intValue();
    }
}
