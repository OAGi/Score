package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.entity.Release;
import org.oagi.srt.repository.impl.BaseReleaseRepository;
import org.oagi.srt.repository.mapper.ReleaseMapper;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
@CacheConfig(cacheNames = "Releases", keyGenerator = "simpleCacheKeyGenerator")
public class OracleReleaseRepository extends BaseReleaseRepository {

    private final String FIND_ONE_BY_RELEASE_NUM_STATEMENT = "SELECT " +
            "release_id, release_num, release_note, namespace_id " +
            "FROM release " +
            "WHERE release_num = :release_num";

    @Override
    @Cacheable("Releases")
    public Release findOneByReleaseNum(String releaseNum) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("release_num", releaseNum);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_RELEASE_NUM_STATEMENT,
                namedParameters, ReleaseMapper.INSTANCE);
    }
}
