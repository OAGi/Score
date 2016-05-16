package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;
import org.oagi.srt.repository.impl.BaseBasicBusinessInformationEntitySupplementaryComponentRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleBasicBusinessInformationEntitySupplementaryComponentRepository extends
        BaseBasicBusinessInformationEntitySupplementaryComponentRepository {

    private final String SAVE_STATEMENT = "INSERT INTO bbie_sc (" +
            "bbie_sc_id, bbie_id, dt_sc_id, dt_sc_pri_restri_id, code_list_id, " +
            "agency_id_list_id, min_cardinality, max_cardinality, default_value, fixed_value, " +
            "definition, remark, biz_term, is_used) VALUES (" +
            "bbie_sc_bbie_sc_id_seq.NEXTVAL, :bbie_id, :dt_sc_id, :dt_sc_pri_restri_id, :code_list_id, " +
            ":agency_id_list_id, :min_cardinality, :max_cardinality, :default_value, :fixed_value, " +
            ":definition, :remark, :biz_term, :is_used)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters,
                         BasicBusinessInformationEntitySupplementaryComponent bbiesc) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"bbie_sc_id"});
        return keyHolder.getKey().intValue();
    }
}
