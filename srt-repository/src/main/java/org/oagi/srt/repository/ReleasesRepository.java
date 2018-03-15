package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.Releases;
import org.oagi.srt.repository.support.jpa.HibernatePropertyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Repository
public class ReleasesRepository {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private HibernatePropertyUtil hibernatePropertyUtil;

    private static final String FIND_ALL_STATEMENT = "SELECT r.release_id, r.release_num, r.release_note, r.state, n.uri, u.login_id, r.last_update_timestamp\n" +
            "FROM release r JOIN namespace n ON (r.namespace_id = n.namespace_id) JOIN app_user u ON (r.last_updated_by = u.app_user_id) ORDER BY r.RELEASE_NUM DESC";

    private static final String FIND_ALL_STATEMENT_FOR_MYSQL = "SELECT r.release_id, r.release_num, r.release_note, r.state, n.uri, u.login_id, r.last_update_timestamp\n" +
            "FROM `release` r JOIN namespace n ON (r.namespace_id = n.namespace_id) JOIN app_user u ON (r.last_updated_by = u.app_user_id) ORDER BY r.RELEASE_NUM DESC";

    private String getStatement() {
        if (hibernatePropertyUtil.getProperty("dialect").contains("MySQL")) {
            return FIND_ALL_STATEMENT_FOR_MYSQL;
        }
        return FIND_ALL_STATEMENT;
    }

    public List<Releases> findAll() {
        Query query = entityManager.createNativeQuery(getStatement(), Releases.class);
        return query.getResultList();
    }

}
