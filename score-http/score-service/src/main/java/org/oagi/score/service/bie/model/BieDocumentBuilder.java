package org.oagi.score.service.bie.model;

import org.oagi.score.repo.api.bie.BieReadRepository;
import org.oagi.score.repo.api.bie.model.BieSet;
import org.oagi.score.repo.api.bie.model.GetBieSetRequest;
import org.oagi.score.repo.api.corecomponent.CcReadRepository;
import org.oagi.score.repo.api.corecomponent.model.GetCcPackageRequest;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.service.bie.BieDocument;
import org.oagi.score.service.corecomponent.model.CcDocumentImpl;

import java.math.BigInteger;

public final class BieDocumentBuilder {

    private BieReadRepository bieReadRepository;
    private CcReadRepository ccReadRepository;

    private BigInteger topLevelAsbiepId;
    private boolean used;
    private ScoreUser requester;

    public BieDocumentBuilder(BieReadRepository bieReadRepository, CcReadRepository ccReadRepository) {
        this.bieReadRepository = bieReadRepository;
        this.ccReadRepository = ccReadRepository;
    }

    public BieDocumentBuilder withTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
        return this;
    }

    public BieDocumentBuilder onlyUsed(boolean used) {
        this.used = used;
        return this;
    }

    public BieDocumentBuilder by(ScoreUser requester) {
        this.requester = requester;
        return this;
    }

    public BieDocument build() {
        BieSet bieSet = bieReadRepository
                .getBieSet(new GetBieSetRequest(requester)
                        .withTopLevelAsbiepId(topLevelAsbiepId)
                        .withUsed(used))
                .getBieSet();

        BieDocumentImpl bieDocument = new BieDocumentImpl(bieSet);
        bieDocument.with(new CcDocumentImpl(ccReadRepository
                .getCcPackage(new GetCcPackageRequest(requester)
                        .withAsccpManifestId(bieDocument.getRootAsbiep().getBasedAsccpManifestId()))
                .getCcPackage()));

        return bieDocument;
    }

    public static BieDocumentBuilder buildFrom(BieReadRepository bieReadRepository, CcReadRepository ccReadRepository) {
        return new BieDocumentBuilder(bieReadRepository, ccReadRepository);
    }

}
