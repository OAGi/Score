package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.ContextCategoryRepository;
import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.repository.mapper.ContextCategoryFindAllMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
public class BaseContextCategoryRepository extends NamedParameterJdbcDaoSupport implements ContextCategoryRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String FIND_ALL_STATEMENT = "SELECT " +
            "ctx_category_id, guid, name, description " +
            "FROM ctx_category";

    @Override
    public List<ContextCategory> findAll() {
        return getJdbcTemplate().query(FIND_ALL_STATEMENT, ContextCategoryFindAllMapper.INSTANCE);
    }
}
