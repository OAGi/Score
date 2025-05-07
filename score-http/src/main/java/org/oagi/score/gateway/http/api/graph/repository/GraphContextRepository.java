package org.oagi.score.gateway.http.api.graph.repository;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocumentImpl;
import org.oagi.score.gateway.http.api.graph.model.CodeListGraphContext;
import org.oagi.score.gateway.http.api.graph.model.CoreComponentGraphContext;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.tag_management.service.TagQueryService;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GraphContextRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private TagQueryService tagQueryService;

    public CoreComponentGraphContext buildGraphContext(ScoreUser requester, ReleaseId releaseId) {
        CcDocument ccDocument = new CcDocumentImpl(requester, repositoryFactory, releaseId);
        return new CoreComponentGraphContext(ccDocument);
    }

    public CoreComponentGraphContext buildGraphContext(ScoreUser requester, AccManifestRecord accManifest) {
        return buildGraphContext(requester, new ReleaseId(accManifest.getReleaseId().toBigInteger()));
    }

    public CoreComponentGraphContext buildGraphContext(ScoreUser requester, AsccpManifestRecord asccpManifest) {
        return buildGraphContext(requester, new ReleaseId(asccpManifest.getReleaseId().toBigInteger()));
    }

    public CoreComponentGraphContext buildGraphContext(ScoreUser requester, BccpManifestRecord bccpManifest) {
        return buildGraphContext(requester, new ReleaseId(bccpManifest.getReleaseId().toBigInteger()));
    }

    public CoreComponentGraphContext buildGraphContext(ScoreUser requester, DtManifestRecord dtManifest) {
        return buildGraphContext(requester, new ReleaseId(dtManifest.getReleaseId().toBigInteger()));
    }

    public CodeListGraphContext buildGraphContext(ScoreUser requester, CodeListManifestRecord codeListManifest) {
        return new CodeListGraphContext(requester, repositoryFactory);
    }
}
