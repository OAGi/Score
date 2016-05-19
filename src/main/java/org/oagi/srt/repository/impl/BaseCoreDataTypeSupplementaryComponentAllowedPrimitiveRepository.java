package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository;
import org.oagi.srt.repository.entity.CoreDataTypeSupplementaryComponentAllowedPrimitive;
import org.oagi.srt.repository.mapper.CoreDataTypeSupplementaryComponentAllowedPrimitiveMapper;
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
public class BaseCoreDataTypeSupplementaryComponentAllowedPrimitiveRepository extends NamedParameterJdbcDaoSupport
        implements CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_BY_CDT_SC_ID_STATEMENT = "SELECT " +
            "cdt_sc_awd_pri_id, cdt_sc_id, cdt_pri_id, is_default " +
            "FROM cdt_sc_awd_pri " +
            "WHERE cdt_sc_id = :cdt_sc_id";

    @Override
    public List<CoreDataTypeSupplementaryComponentAllowedPrimitive> findByCdtScId(int cdtScId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("cdt_sc_id", cdtScId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_CDT_SC_ID_STATEMENT,
                namedParameters, CoreDataTypeSupplementaryComponentAllowedPrimitiveMapper.INSTANCE);
    }

    private final String FIND_BY_CDT_PRI_ID_STATEMENT = "SELECT " +
            "cdt_sc_awd_pri_id, cdt_sc_id, cdt_pri_id, is_default " +
            "FROM cdt_sc_awd_pri " +
            "WHERE cdt_pri_id = :cdt_pri_id";

    @Override
    public List<CoreDataTypeSupplementaryComponentAllowedPrimitive> findByCdtPriId(int cdtPriId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("cdt_pri_id", cdtPriId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_CDT_PRI_ID_STATEMENT,
                namedParameters, CoreDataTypeSupplementaryComponentAllowedPrimitiveMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_CDT_SC_AWD_PRI_ID_STATEMENT = "SELECT " +
            "cdt_sc_awd_pri_id, cdt_sc_id, cdt_pri_id, is_default " +
            "FROM cdt_sc_awd_pri " +
            "WHERE cdt_sc_awd_pri_id = :cdt_sc_awd_pri_id";

    @Override
    public CoreDataTypeSupplementaryComponentAllowedPrimitive findOneByCdtScAwdPriId(int cdtScAwdPriId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("cdt_sc_awd_pri_id", cdtScAwdPriId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_CDT_SC_AWD_PRI_ID_STATEMENT,
                namedParameters, CoreDataTypeSupplementaryComponentAllowedPrimitiveMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_CDT_SC_ID_AND_CDT_PRI_ID_STATEMENT = "SELECT " +
            "cdt_sc_awd_pri_id, cdt_sc_id, cdt_pri_id, is_default " +
            "FROM cdt_sc_awd_pri " +
            "WHERE cdt_sc_id = :cdt_sc_id AND cdt_pri_id = :cdt_pri_id";

    @Override
    public CoreDataTypeSupplementaryComponentAllowedPrimitive findOneByCdtScIdAndCdtPriId(int cdtScId, int cdtPriId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("cdt_sc_id", cdtScId)
                .addValue("cdt_pri_id", cdtPriId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_CDT_SC_ID_AND_CDT_PRI_ID_STATEMENT,
                namedParameters, CoreDataTypeSupplementaryComponentAllowedPrimitiveMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO cdt_sc_awd_pri (" +
            "cdt_sc_id, cdt_pri_id, is_default) VALUES (" +
            ":cdt_sc_id, :cdt_pri_id, :is_default)";

    @Override
    public void save(CoreDataTypeSupplementaryComponentAllowedPrimitive cdtScAwdPri) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("cdt_sc_id", cdtScAwdPri.getCdtScId())
                .addValue("cdt_pri_id", cdtScAwdPri.getCdtPriId())
                .addValue("is_default", cdtScAwdPri.isDefault() ? 1 : 0);

        int cdtScAwdPriId = doSave(namedParameters, cdtScAwdPri);
        cdtScAwdPri.setCdtScAwdPriId(cdtScAwdPriId);
    }

    protected int doSave(MapSqlParameterSource namedParameters,
                         CoreDataTypeSupplementaryComponentAllowedPrimitive cdtScAwdPri) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"cdt_sc_awd_pri_id"});
        return keyHolder.getKey().intValue();
    }
}
