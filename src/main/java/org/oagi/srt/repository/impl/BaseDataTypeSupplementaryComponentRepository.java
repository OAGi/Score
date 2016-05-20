package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.DataTypeSupplementaryComponentRepository;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;
import org.oagi.srt.repository.mapper.DataTypeSupplementaryComponentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
@CacheConfig(cacheNames = "DTSCs", keyGenerator = "simpleCacheKeyGenerator")
public class BaseDataTypeSupplementaryComponentRepository extends NamedParameterJdbcDaoSupport
        implements DataTypeSupplementaryComponentRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ALL_STATEMENT = "SELECT " +
            "dt_sc_id, guid, property_term, representation_term, definition, owner_dt_id, " +
            "min_cardinality, max_cardinality, based_dt_sc_id " +
            "FROM dt_sc";

    @Override
    @Cacheable("DTSCs")
    public List<DataTypeSupplementaryComponent> findAll() {
        return getJdbcTemplate().query(FIND_ALL_STATEMENT, DataTypeSupplementaryComponentMapper.INSTANCE);
    }

    private final String FIND_BY_OWNER_DT_ID_STATEMENT = "SELECT " +
            "dt_sc_id, guid, property_term, representation_term, definition, owner_dt_id, " +
            "min_cardinality, max_cardinality, based_dt_sc_id " +
            "FROM dt_sc " +
            "WHERE owner_dt_id = :owner_dt_id";

    @Override
    @Cacheable("DTSCs")
    public List<DataTypeSupplementaryComponent> findByOwnerDtId(int ownerDtId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("owner_dt_id", ownerDtId);

        return getNamedParameterJdbcTemplate().query(FIND_BY_OWNER_DT_ID_STATEMENT,
                namedParameters, DataTypeSupplementaryComponentMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_GUID_STATEMENT = "SELECT " +
            "dt_sc_id, guid, property_term, representation_term, definition, owner_dt_id, " +
            "min_cardinality, max_cardinality, based_dt_sc_id " +
            "FROM dt_sc " +
            "WHERE guid = :guid";

    @Override
    @Cacheable("DTSCs")
    public DataTypeSupplementaryComponent findOneByGuid(String guid) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", guid);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_GUID_STATEMENT,
                namedParameters, DataTypeSupplementaryComponentMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_DT_SC_ID_STATEMENT = "SELECT " +
            "dt_sc_id, guid, property_term, representation_term, definition, owner_dt_id, " +
            "min_cardinality, max_cardinality, based_dt_sc_id " +
            "FROM dt_sc " +
            "WHERE dt_sc_id = :dt_sc_id";

    @Override
    @Cacheable("DTSCs")
    public DataTypeSupplementaryComponent findOneByDtScId(int dtScId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("dt_sc_id", dtScId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_DT_SC_ID_STATEMENT,
                namedParameters, DataTypeSupplementaryComponentMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_GUID_AND_OWNER_DT_ID_STATEMENT = "SELECT " +
            "dt_sc_id, guid, property_term, representation_term, definition, owner_dt_id, " +
            "min_cardinality, max_cardinality, based_dt_sc_id " +
            "FROM dt_sc " +
            "WHERE guid = :guid AND owner_dt_id = :owner_dt_id";

    @Override
    @Cacheable("DTSCs")
    public DataTypeSupplementaryComponent findOneByGuidAndOwnerDtId(String guid, int ownerDtId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", guid)
                .addValue("owner_dt_id", ownerDtId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_GUID_AND_OWNER_DT_ID_STATEMENT,
                namedParameters, DataTypeSupplementaryComponentMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_OWNER_DT_ID_AND_PROPERTY_TERM_AND_REPRESENTATION_TERM_STATEMENT = "SELECT " +
            "dt_sc_id, guid, property_term, representation_term, definition, owner_dt_id, " +
            "min_cardinality, max_cardinality, based_dt_sc_id " +
            "FROM dt_sc " +
            "WHERE owner_dt_id = :owner_dt_id AND property_term = :property_term AND representation_term = :representation_term";

    @Override
    @Cacheable("DTSCs")
    public DataTypeSupplementaryComponent findOneByOwnerDtIdAndPropertyTermAndRepresentationTerm(
            int ownerDtId, String propertyTerm, String representationTerm) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("owner_dt_id", ownerDtId)
                .addValue("property_term", propertyTerm)
                .addValue("representation_term", representationTerm);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_OWNER_DT_ID_AND_PROPERTY_TERM_AND_REPRESENTATION_TERM_STATEMENT,
                namedParameters, DataTypeSupplementaryComponentMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_OWNER_DT_ID_AND_BASED_DT_SC_ID_STATEMENT = "SELECT " +
            "dt_sc_id, guid, property_term, representation_term, definition, owner_dt_id, " +
            "min_cardinality, max_cardinality, based_dt_sc_id " +
            "FROM dt_sc " +
            "WHERE owner_dt_id = :owner_dt_id AND based_dt_sc_id = :based_dt_sc_id";

    @Override
    @Cacheable("DTSCs")
    public DataTypeSupplementaryComponent findOneByOwnerDtIdAndBasedDtScId(int ownerDtId, int basedDtScId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("owner_dt_id", ownerDtId)
                .addValue("based_dt_sc_id", basedDtScId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_OWNER_DT_ID_AND_BASED_DT_SC_ID_STATEMENT,
                namedParameters, DataTypeSupplementaryComponentMapper.INSTANCE);
    }

    private final String SAVE_STATEMENT = "INSERT INTO dt_sc (" +
            "guid, property_term, representation_term, definition, owner_dt_id, " +
            "min_cardinality, max_cardinality, based_dt_sc_id) VALUES (" +
            ":guid, :property_term, :representation_term, :definition, :owner_dt_id, " +
            ":min_cardinality, :max_cardinality, :based_dt_sc_id)";

    @Override
    @CacheEvict("DTSCs")
    public void save(DataTypeSupplementaryComponent dtSc) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", dtSc.getGuid())
                .addValue("property_term", dtSc.getPropertyTerm())
                .addValue("representation_term", dtSc.getRepresentationTerm())
                .addValue("definition", dtSc.getDefinition())
                .addValue("owner_dt_id", dtSc.getOwnerDtId())
                .addValue("min_cardinality", dtSc.getMinCardinality())
                .addValue("max_cardinality", dtSc.getMaxCardinality())
                .addValue("based_dt_sc_id", dtSc.getBasedDtScId() == 0 ? null : dtSc.getBasedDtScId());

        int dtScId = doSave(namedParameters, dtSc);
        dtSc.setDtScId(dtScId);
    }

    protected int doSave(MapSqlParameterSource namedParameters, DataTypeSupplementaryComponent dtSc) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"dt_sc_id"});
        return keyHolder.getKey().intValue();
    }

    private final String UPDATE_STATEMENT = "UPDATE dt_sc SET " +
            "guid = :guid, property_term = :property_term, representation_term = :representation_term, " +
            "definition = :definition, owner_dt_id = :owner_dt_id, " +
            "min_cardinality = :min_cardinality, max_cardinality = :max_cardinality, based_dt_sc_id = :based_dt_sc_id " +
            "WHERE dt_sc_id = :dt_sc_id";

    @Override
    @CacheEvict("DTSCs")
    public void update(DataTypeSupplementaryComponent dtSc) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("guid", dtSc.getGuid())
                .addValue("property_term", dtSc.getPropertyTerm())
                .addValue("representation_term", dtSc.getRepresentationTerm())
                .addValue("definition", dtSc.getDefinition())
                .addValue("owner_dt_id", dtSc.getOwnerDtId())
                .addValue("min_cardinality", dtSc.getMinCardinality())
                .addValue("max_cardinality", dtSc.getMaxCardinality())
                .addValue("based_dt_sc_id", dtSc.getBasedDtScId())
                .addValue("dt_sc_id", dtSc.getDtScId());

        getNamedParameterJdbcTemplate().update(UPDATE_STATEMENT, namedParameters);
    }
}
