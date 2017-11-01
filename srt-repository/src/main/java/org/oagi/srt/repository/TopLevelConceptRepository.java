package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.TopLevelConcept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

@Repository
public class TopLevelConceptRepository {

    @Autowired
    private EntityManager entityManager;

    private static final String FIND_ALL_STATEMENT =
            "select asccp.asccp_id, asccp.property_term, module.module, asccp.last_update_timestamp " +
            "from asccp, module " +
            "where asccp.release_id IS NULL and module.module_id = asccp.module_id " +
            "and module.module not like '%Components%' " +
            "and module.module not like '%Meta%' " +
            "and module.module not like '%Noun%' " +
            "and module.module not like '%Extension%'";

    public TopLevelConcept findOne(long asccpId) {
        Query query = entityManager.createNativeQuery(
                FIND_ALL_STATEMENT + " AND ASCCP.ASCCP_ID = ?", TopLevelConcept.class);
        query.setParameter(1, asccpId);
        return (TopLevelConcept) query.getSingleResult();
    }

    public List<TopLevelConcept> findAll() {
        Query query = entityManager.createNativeQuery(FIND_ALL_STATEMENT, TopLevelConcept.class);
        return query.getResultList();
    }

    public List<TopLevelConcept> findByPropertyTermContaining(String propertyTerm) {
        if (StringUtils.isEmpty(propertyTerm)) {
            return Collections.emptyList();
        }
        Query query = entityManager.createNativeQuery(
                FIND_ALL_STATEMENT + " AND ASCCP.PROPERTY_TERM = ?", TopLevelConcept.class);
        query.setParameter(1, propertyTerm);
        return query.getResultList();
    }

}
