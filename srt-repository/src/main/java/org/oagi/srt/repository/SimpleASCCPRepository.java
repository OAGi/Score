package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.SimpleASCCP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Repository
public class SimpleASCCPRepository {

    @Autowired
    private EntityManager entityManager;

    private static final String FIND_ALL_STATEMENT =
            "SELECT asccp.asccp_id, asccp.guid, asccp.property_term, module.module, " +
            "asccp.definition, asccp.is_deprecated, asccp.state, asccp.owner_user_id " +
            "FROM asccp LEFT JOIN module ON asccp.module_id = module.module_id " +
            "WHERE asccp.revision_num = 0 AND asccp.is_deprecated = 0 AND asccp.reusable_indicator = 1";

    public List<SimpleASCCP> findAll() {
        Query query = entityManager.createNativeQuery(FIND_ALL_STATEMENT, SimpleASCCP.class);
        return query.getResultList();
    }
}
