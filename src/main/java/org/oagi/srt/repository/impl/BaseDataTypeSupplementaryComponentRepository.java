package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.DataTypeSupplementaryComponentRepository;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;
import org.oagi.srt.repository.mapper.DataTypeSupplementaryComponentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
public class BaseDataTypeSupplementaryComponentRepository extends NamedParameterJdbcDaoSupport
        implements DataTypeSupplementaryComponentRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_BY_OWNER_DT_ID_STATEMENT = "SELECT " +
            "dt_sc_id, guid, property_term, representation_term, definition, owner_dt_id, " +
            "min_cardinality, max_cardinality, based_dt_sc_id " +
            "FROM dt_sc " +
            "WHERE owner_dt_id = :owner_dt_id";

    @Override
    public List<DataTypeSupplementaryComponent> findByOwnerDtId(int ownerDtId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("owner_dt_id", ownerDtId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_OWNER_DT_ID_STATEMENT,
                namedParameters, DataTypeSupplementaryComponentMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_DT_SC_ID_STATEMENT = "SELECT " +
            "dt_sc_id, guid, property_term, representation_term, definition, owner_dt_id, " +
            "min_cardinality, max_cardinality, based_dt_sc_id " +
            "FROM dt_sc " +
            "WHERE dt_sc_id = :dt_sc_id";

    @Override
    public DataTypeSupplementaryComponent findOneByDtScId(int dtScId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("dt_sc_id", dtScId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_DT_SC_ID_STATEMENT,
                namedParameters, DataTypeSupplementaryComponentMapper.INSTANCE);
    }
}
