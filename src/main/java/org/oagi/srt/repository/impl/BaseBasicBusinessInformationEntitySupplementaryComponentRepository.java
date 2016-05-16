package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.BasicBusinessInformationEntitySupplementaryComponentRepository;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;
import org.oagi.srt.repository.mapper.BasicBusinessInformationEntitySupplementaryComponentMapper;
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
public class BaseBasicBusinessInformationEntitySupplementaryComponentRepository extends NamedParameterJdbcDaoSupport
        implements BasicBusinessInformationEntitySupplementaryComponentRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_GREATEST_ID_STATEMENT = "SELECT MAX(bbie_sc_id) FROM bbie_sc";

    @Override
    public int findGreatestId() {
        return getJdbcTemplate().queryForObject(FIND_GREATEST_ID_STATEMENT, Integer.class);
    }

    private final String FIND_BY_BBIE_ID_STATEMENT = "SELECT " +
            "bbie_sc_id, bbie_id, dt_sc_id, dt_sc_pri_restri_id, code_list_id, " +
            "agency_id_list_id, min_cardinality, max_cardinality, default_value, fixed_value, " +
            "definition, remark, biz_term, is_used " +
            "FROM bbie_sc " +
            "WHERE bbie_id = :bbie_id";

    @Override
    public List<BasicBusinessInformationEntitySupplementaryComponent> findByBbieId(int bbieId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bbie_id", bbieId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_BBIE_ID_STATEMENT,
                namedParameters, BasicBusinessInformationEntitySupplementaryComponentMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO bbie_sc (" +
            "bbie_id, dt_sc_id, dt_sc_pri_restri_id, code_list_id, " +
            "agency_id_list_id, min_cardinality, max_cardinality, default_value, fixed_value, " +
            "definition, remark, biz_term, is_used) VALUES (" +
            ":bbie_id, :dt_sc_id, :dt_sc_pri_restri_id, :code_list_id, " +
            ":agency_id_list_id, :min_cardinality, :max_cardinality, :default_value, :fixed_value, " +
            ":definition, :remark, :biz_term, :is_used)";

    @Override
    public void save(BasicBusinessInformationEntitySupplementaryComponent bbiesc) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bbie_id", bbiesc.getBbieId())
                .addValue("dt_sc_id", bbiesc.getDtScId())
                .addValue("dt_sc_pri_restri_id", bbiesc.getDtScPriRestriId() == 0 ? null : bbiesc.getDtScPriRestriId())
                .addValue("code_list_id", bbiesc.getCodeListId() == 0 ? null : bbiesc.getCodeListId())
                .addValue("agency_id_list_id", bbiesc.getAgencyIdListId() == 0 ? null : bbiesc.getAgencyIdListId())
                .addValue("min_cardinality", bbiesc.getMinCardinality())
                .addValue("max_cardinality", bbiesc.getMaxCardinality())
                .addValue("default_value", bbiesc.getDefaultValue())
                .addValue("fixed_value", bbiesc.getFixedValue())
                .addValue("definition", bbiesc.getDefinition())
                .addValue("remark", bbiesc.getRemark())
                .addValue("biz_term", bbiesc.getBizTerm())
                .addValue("is_used", bbiesc.isUsed() ? 1 : 0);

        int basicBusinessInformationEntitySupplementaryComponentId = doSave(namedParameters, bbiesc);
        bbiesc.setBbieScId(basicBusinessInformationEntitySupplementaryComponentId);
    }

    protected int doSave(MapSqlParameterSource namedParameters,
                         BasicBusinessInformationEntitySupplementaryComponent bbiesc) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"bbie_sc_id"});
        return keyHolder.getKey().intValue();
    }

    private final String UPDATE_STATEMENT = "UPDATE bbie_sc SET " +
            "bbie_id = :bbie_id, dt_sc_id = :dt_sc_id, dt_sc_pri_restri_id = :dt_sc_pri_restri_id, code_list_id = :code_list_id, " +
            "agency_id_list_id = :agency_id_list_id, min_cardinality = :min_cardinality, max_cardinality = :max_cardinality, " +
            "default_value = :default_value, fixed_value = :fixed_value, definition = :definition, remark = :remark, " +
            "biz_term = :biz_term, is_used = :is_used " +
            "WHERE bbie_sc_id = :bbie_sc_id";

    @Override
    public void update(BasicBusinessInformationEntitySupplementaryComponent bbiesc) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("bbie_id", bbiesc.getBbieId())
                .addValue("dt_sc_id", bbiesc.getDtScId())
                .addValue("dt_sc_pri_restri_id", bbiesc.getDtScPriRestriId() == 0 ? null : bbiesc.getDtScPriRestriId())
                .addValue("code_list_id", bbiesc.getCodeListId() == 0 ? null : bbiesc.getCodeListId())
                .addValue("agency_id_list_id", bbiesc.getAgencyIdListId() == 0 ? null : bbiesc.getAgencyIdListId())
                .addValue("min_cardinality", bbiesc.getMinCardinality())
                .addValue("max_cardinality", bbiesc.getMaxCardinality())
                .addValue("default_value", bbiesc.getDefaultValue())
                .addValue("fixed_value", bbiesc.getFixedValue())
                .addValue("definition", bbiesc.getDefinition())
                .addValue("remark", bbiesc.getRemark())
                .addValue("biz_term", bbiesc.getBizTerm())
                .addValue("is_used", bbiesc.isUsed() ? 1 : 0);

        getNamedParameterJdbcTemplate().update(UPDATE_STATEMENT, namedParameters);
    }
}
