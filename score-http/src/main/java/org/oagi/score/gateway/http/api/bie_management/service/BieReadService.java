package org.oagi.score.gateway.http.api.bie_management.service;

import org.oagi.score.gateway.http.api.bie_management.model.BieDocumentBuilder;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BieReadService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    public BieDocument getBieDocument(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId) {
        BieDocument bieDocument = BieDocumentBuilder.buildFrom(
                        requester,
                        repositoryFactory,
                        repositoryFactory.bieQueryRepository(requester))
                .withTopLevelAsbiepId(topLevelAsbiepId)
                .onlyUsed(true)
                .build();

        return bieDocument;
    }

}
