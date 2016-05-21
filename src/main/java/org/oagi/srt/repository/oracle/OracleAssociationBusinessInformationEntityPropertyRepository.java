package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.oagi.srt.repository.impl.BaseAssociationBusinessInformationEntityPropertyRepository;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@CacheConfig(cacheNames = "ASBIEPs", keyGenerator = "simpleCacheKeyGenerator")
public class OracleAssociationBusinessInformationEntityPropertyRepository
        extends BaseAssociationBusinessInformationEntityPropertyRepository implements OracleRepository {

    @Override
    public String getSequenceName() {
        return "ASBIEP_ID_SEQ";
    }

    private final String FIND_GREATEST_ID_STATEMENT = "SELECT NVL(MAX(asbiep_id), 0) FROM asbiep";

    @Override
    @Cacheable("ASBIEPs")
    public int findGreatestId() {
        return getJdbcTemplate().queryForObject(FIND_GREATEST_ID_STATEMENT, Integer.class);
    }

    private final String SAVE_STATEMENT = "INSERT INTO asbiep (" +
            "asbiep_id, guid, based_asccp_id, role_of_abie_id, definition, remark, biz_term, " +
            "created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES (" +
            getSequenceName() + ".NEXTVAL, :guid, :based_asccp_id, :role_of_abie_id, :definition, :remark, :biz_term, " +
            ":created_by, :last_updated_by, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

    @Override
    protected int doSave(MapSqlParameterSource namedParameters, AssociationBusinessInformationEntityProperty asbiep) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"asbiep_id"});
        return keyHolder.getKey().intValue();
    }
}
