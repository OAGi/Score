package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.SimpleACC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.*;

@Repository
public class SimpleACCRepository {

    @Autowired
    private EntityManager entityManager;

    private static final String FIND_ALL_STATEMENT =
            "SELECT acc.acc_id, acc.guid, acc.object_class_term, acc.oagis_component_type, module.module, " +
                    "acc.definition, acc.state, acc.owner_user_id, acc.is_deprecated, acc.is_abstract, " +
                    "acc.release_id, acc.revision_num, acc.revision_tracking_num " +
                    "FROM acc LEFT JOIN module ON acc.module_id = module.module_id " +
                    "WHERE acc.is_deprecated = 0";

    public List<SimpleACC> findAll(long releaseId) {
        StringBuilder sb = new StringBuilder(FIND_ALL_STATEMENT);
        if (releaseId > 0L) {
            sb.append(" AND acc.release_id <= :releaseId");
        } else {
            sb.append(" AND acc.release_id IS NULL AND acc.revision_num = 0");
        }
        Query query = entityManager.createNativeQuery(sb.toString(), SimpleACC.class);
        if (releaseId > 0L) {
            query.setParameter("releaseId", releaseId);
            List<SimpleACC> resultList = query.getResultList();
            return new ArrayList(resultList.stream()
                    .collect(groupingBy(SimpleACC::getGuid, collectingAndThen(
                            maxBy((o1, o2) -> {
                                long o1ReleaseId = o1.getReleaseId();
                                long o2ReleaseId = o2.getReleaseId();
                                if (o1ReleaseId == o2ReleaseId) {
                                    String o1Revision = o1.getRevision();
                                    String o2Revision = o2.getRevision();
                                    return o1Revision.compareTo(o2Revision);
                                } else {
                                    return Long.compare(o1ReleaseId, o2ReleaseId);
                                }
                            }), o -> o.get()))).values());
        } else {
            return query.getResultList();
        }

    }
}
