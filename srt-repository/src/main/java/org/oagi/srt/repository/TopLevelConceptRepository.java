package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.Release;
import org.oagi.srt.repository.entity.TopLevelConcept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

@Repository
public class TopLevelConceptRepository {

    @Autowired
    private EntityManager entityManager;

    private static final String FIND_ALL_STATEMENT =
            "select asccp.asccp_id, asccp.guid, asccp.release_id, asccp.property_term, module.module, asccp.last_update_timestamp " +
            "from asccp LEFT JOIN module ON asccp.module_id = module.module_id " +
            "where #COND# ORDER BY asccp.property_term";

    public TopLevelConcept findOne(long asccpId) {
        Query query = entityManager.createNativeQuery(FIND_ALL_STATEMENT + " AND ASCCP.ASCCP_ID = ?", TopLevelConcept.class);
        query.setParameter(1, asccpId);
        return (TopLevelConcept) query.getSingleResult();
    }

    public List<TopLevelConcept> findAll(Release release) {
        Query query;
        if (release == Release.WORKING_RELEASE) {
            String statement = FIND_ALL_STATEMENT.replace("#COND#",
                    "asccp.revision_num = 0 and asccp.release_id IS NULL");
            query = entityManager.createNativeQuery(statement, TopLevelConcept.class);
        } else {
            String statement = FIND_ALL_STATEMENT.replace("#COND#",
                    "asccp.revision_num > 0 and asccp.release_id <= ?");
            query = entityManager.createNativeQuery(statement, TopLevelConcept.class);
            query.setParameter(1, release.getReleaseId());
        }
        return distinct(query.getResultList());
    }

    public List<TopLevelConcept> findByPropertyTermContaining(Release release, String propertyTerm) {
        if (StringUtils.isEmpty(propertyTerm)) {
            return Collections.emptyList();
        }

        Query query;
        if (release == Release.WORKING_RELEASE) {
            String statement = FIND_ALL_STATEMENT.replace("#COND#",
                    "asccp.revision_num = 0 and asccp.release_id IS NULL") + " AND ASCCP.PROPERTY_TERM = ?";
            query = entityManager.createNativeQuery(statement, TopLevelConcept.class);
        } else {
            String statement = FIND_ALL_STATEMENT.replace("#COND#",
                    "asccp.revision_num > 0 and asccp.release_id <= ?") + " AND ASCCP.PROPERTY_TERM = ?";
            query = entityManager.createNativeQuery(statement, TopLevelConcept.class);
            query.setParameter(1, release.getReleaseId());
        }
        query.setParameter(2, propertyTerm);

        return distinct(query.getResultList());
    }

    public List<TopLevelConcept> distinct(List<TopLevelConcept> result) {
        Map<String, TopLevelConcept> map = new LinkedHashMap();
        result.stream().forEachOrdered(e -> {
            String guid = e.getGuid();
            if (map.containsKey(guid)) {
                TopLevelConcept p = map.get(guid);
                Long pReleaseId = p.getReleaseId();
                Long eReleaseId = e.getReleaseId();
                if ((pReleaseId != null && eReleaseId != null) && pReleaseId > eReleaseId) {
                    return;
                }
            }

            map.put(guid, e);
        });

        return new ArrayList(map.values());
    }

}
