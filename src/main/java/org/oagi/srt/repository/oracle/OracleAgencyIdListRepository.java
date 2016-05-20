package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.AgencyIdList;
import org.oagi.srt.repository.impl.BaseAgencyIdListRepository;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OracleAgencyIdListRepository extends BaseAgencyIdListRepository {

    private final String SAVE_STATEMENT = "INSERT INTO agency_id_list (" +
            "agency_id_list_id, guid, enum_type_guid, name, list_id, agency_id, version_id, definition) VALUES (" +
            "agency_id_list_agency_id_list_.NEXTVAL, :guid, :enum_type_guid, :name, :list_id, :agency_id, :version_id, :definition)";

    protected int doSave(MapSqlParameterSource namedParameters, AgencyIdList agencyIdList) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"agency_id_list_id"});
        return keyHolder.getKey().intValue();
    }

}
