package org.oagi.score.gateway.http.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SrtJdbcTemplate {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    private List<Class<?>> primitiveClasses = Arrays.asList(Integer.class, Long.class, Float.class, Double.class, String.class);

    public static MapSqlParameterSource newSqlParameterSource() {
        return new MapSqlParameterSource();
    }

    public void query(String query) {
        this.jdbcTemplate.query(query, e -> null);
    }

    public <T> T query(String query, ResultSetExtractor<T> rse) {
        return this.jdbcTemplate.query(query, rse);
    }

    public void query(String query, SqlParameterSource parameterSource, RowCallbackHandler rch) {
        this.jdbcTemplate.query(query, parameterSource, rch);
    }

    public <T> T queryForObject(String query, Class<T> clazz) {
        return this.queryForObject(query, null, clazz);
    }

    public <T> T queryForObject(String query, SqlParameterSource parameterSource, Class<T> clazz) {
        if (parameterSource == null) {
            parameterSource = EmptySqlParameterSource.INSTANCE;
        }

        if (isPrimitive(clazz)) {
            return this.jdbcTemplate.queryForObject(query, parameterSource, clazz);
        } else {
            return this.jdbcTemplate.queryForObject(query, parameterSource, new BeanPropertyRowMapper<>(clazz));
        }
    }

    public <T> List<T> queryForList(String query, Class<T> clazz) {
        return this.queryForList(query, null, clazz);
    }

    public <T> List<T> queryForList(String query, SqlParameterSource parameterSource, Class<T> clazz) {
        if (parameterSource == null) {
            parameterSource = EmptySqlParameterSource.INSTANCE;
        }

        if (isPrimitive(clazz)) {
            return this.jdbcTemplate.queryForList(query, parameterSource, clazz);
        } else {
            return this.jdbcTemplate.query(query, parameterSource, new BeanPropertyRowMapper<>(clazz));
        }
    }

    private boolean isPrimitive(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return true;
        }

        return primitiveClasses.contains(clazz);
    }

    public SimpleJdbcInsert insert() {
        return new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate().getDataSource());
    }

    public int update(String query, SqlParameterSource parameterSource) {
        return this.jdbcTemplate.update(query, parameterSource);
    }
}
