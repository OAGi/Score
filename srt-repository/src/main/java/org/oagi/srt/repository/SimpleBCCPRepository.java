package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.SimpleBCCP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Repository
public class SimpleBCCPRepository {

    @Autowired
    private EntityManager entityManager;

    private static final String FIND_ALL_STATEMENT =
            "SELECT bccp.bccp_id, bccp.guid, bccp.property_term, module.module, bccp.definition, bccp.state " +
            "FROM bccp LEFT JOIN module ON bccp.module_id = module.module_id " +
            "WHERE bccp.revision_num = 0 AND bccp.is_deprecated = 0";

    public List<SimpleBCCP> findAll() {
        Query query = entityManager.createNativeQuery(FIND_ALL_STATEMENT, SimpleBCCP.class);
        return query.getResultList();
    }
}
