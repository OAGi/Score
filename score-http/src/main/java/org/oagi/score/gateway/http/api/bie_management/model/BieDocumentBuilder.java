package org.oagi.score.gateway.http.api.bie_management.model;

import org.oagi.score.gateway.http.api.bie_management.repository.BieQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.service.BieDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocumentImpl;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

public final class BieDocumentBuilder {

    private ScoreUser requester;
    private RepositoryFactory repositoryFactory;
    private BieQueryRepository bieQueryRepository;

    private TopLevelAsbiepId topLevelAsbiepId;
    private boolean used;

    public BieDocumentBuilder(ScoreUser requester, RepositoryFactory repositoryFactory,
                              BieQueryRepository bieQueryRepository) {
        this.requester = requester;
        this.repositoryFactory = repositoryFactory;
        this.bieQueryRepository = bieQueryRepository;
    }

    public BieDocumentBuilder withTopLevelAsbiepId(TopLevelAsbiepId topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
        return this;
    }

    public BieDocumentBuilder onlyUsed(boolean used) {
        this.used = used;
        return this;
    }

    public BieDocument build() {
        BieSet bieSet = bieQueryRepository.getBieSet(topLevelAsbiepId, used);

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord topLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);

        BieDocumentImpl bieDocument = new BieDocumentImpl(bieSet);
        bieDocument.with(new CcDocumentImpl(requester, repositoryFactory, topLevelAsbiep.release().releaseId()));

        return bieDocument;
    }

    public static BieDocumentBuilder buildFrom(ScoreUser requester, RepositoryFactory repositoryFactory,
                                               BieQueryRepository bieQueryRepository) {
        return new BieDocumentBuilder(requester, repositoryFactory, bieQueryRepository);
    }

}
