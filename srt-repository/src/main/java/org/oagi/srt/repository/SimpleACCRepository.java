package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.SimpleACC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

import static org.oagi.srt.repository.entity.CoreComponentState.Published;

@Repository
public class SimpleACCRepository {

    @Autowired
    private EntityManager entityManager;

    private static final String FIND_ALL_STATEMENT =
            "SELECT acc.acc_id, acc.guid, acc.object_class_term, acc.oagis_component_type, module.module, " +
            "acc.definition, acc.state, acc.owner_user_id, acc.is_abstract " +
            "FROM acc LEFT JOIN module ON acc.module_id = module.module_id " +
            "WHERE acc.revision_num = 0 AND acc.state = :state AND acc.is_deprecated = 0";

    public List<SimpleACC> findAll() {
        Query query = entityManager.createNativeQuery(FIND_ALL_STATEMENT, SimpleACC.class);
        query.setParameter("state", Published.getValue());
        return query.getResultList();
    }
}
