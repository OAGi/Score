package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.ReleaseRepository;
import org.oagi.srt.repository.entity.Release;
import org.oagi.srt.repository.mapper.ReleaseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
public class BaseReleaseRepository extends NamedParameterJdbcDaoSupport implements ReleaseRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ONE_BY_RELEASE_NUM_STATEMENT = "SELECT " +
            "release_id, release_num, release_note, namespace_id " +
            "FROM `release` " +
            "WHERE release_num = :release_num";

    @Override
    public Release findOneByReleaseNum(String releaseNum) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("release_num", releaseNum);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_RELEASE_NUM_STATEMENT,
                namedParameters, ReleaseMapper.INSTANCE);
    }
}
