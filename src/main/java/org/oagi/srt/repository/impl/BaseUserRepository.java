package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.UserRepository;
import org.oagi.srt.repository.entity.User;
import org.oagi.srt.repository.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
@CacheConfig(cacheNames = "Users", keyGenerator = "simpleCacheKeyGenerator")
public class BaseUserRepository extends NamedParameterJdbcDaoSupport implements UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ONE_BY_LOGIN_ID_STATEMENT = "SELECT " +
            "app_user_id, login_id, password, name, organization, oagis_developer_indicator " +
            "FROM app_user " +
            "WHERE login_id = :login_id";

    @Override
    @Cacheable("Users")
    public User findOneByLoginId(String loginId) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("login_id", loginId);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_LOGIN_ID_STATEMENT,
                namedParameters, UserMapper.INSTANCE);
    }

    private final String FIND_ONE_BY_NAME_STATEMENT = "SELECT " +
            "app_user_id, login_id, password, name, organization, oagis_developer_indicator " +
            "FROM app_user " +
            "WHERE name = :name";

    @Override
    @Cacheable("Users")
    public User findOneByName(String name) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("name", name);

        return getNamedParameterJdbcTemplate().queryForObject(FIND_ONE_BY_NAME_STATEMENT,
                namedParameters, UserMapper.INSTANCE);
    }
}
