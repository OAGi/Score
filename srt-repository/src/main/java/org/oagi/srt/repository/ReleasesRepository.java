package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.Releases;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Repository
public class ReleasesRepository {

    @Autowired
    private EntityManager entityManager;

    private static final String FIND_ALL_STATEMENT =
            "SELECT new Releases(r.releaseId, r.releaseNum, r.releaseNote, r.state, n.uri, u.loginId, r.lastUpdateTimestamp) " +
                    "FROM Release r, Namespace n, User u " +
                    "WHERE r.namespace.namespaceId = n.namespaceId AND r.lastUpdatedBy = u.appUserId " +
                    "ORDER BY r.releaseNum DESC";

    public List<Releases> findAll() {
        Query query = entityManager.createQuery(FIND_ALL_STATEMENT, Releases.class);
        return query.getResultList();
    }

}
