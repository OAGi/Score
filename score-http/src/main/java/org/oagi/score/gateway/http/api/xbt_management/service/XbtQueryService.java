package org.oagi.score.gateway.http.api.xbt_management.service;

import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.repository.XbtQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class XbtQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private XbtQueryRepository query(ScoreUser requester) {
        return repositoryFactory.xbtQueryRepository(requester);
    }

    public List<XbtSummaryRecord> getXbtSummaryList(
            ScoreUser requester, ReleaseId releaseId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (releaseId == null) {
            throw new IllegalArgumentException("`releaseId` must not be null");
        }

        return query(requester).getXbtSummaryList(releaseId);
    }

}
