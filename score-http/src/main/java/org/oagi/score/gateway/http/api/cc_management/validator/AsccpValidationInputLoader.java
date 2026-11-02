package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.AsccpQueryRepository;
import org.oagi.score.gateway.http.api.validation.ValidationInputLoader;
import org.oagi.score.gateway.http.api.validation.ValidationSubject;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

/** Reads an ASCCP, its previous revision, its namespace and its ACC. */
@Component
public class AsccpValidationInputLoader implements ValidationInputLoader<AsccpValidationInput> {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Override
    public AsccpValidationInput load(ScoreUser requester, ValidationSubject subject) {
        AsccpManifestId asccpManifestId;
        try {
            asccpManifestId = new AsccpManifestId(new BigInteger(subject.id()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + subject.id() + "' is not an ASCCP manifest id.");
        }
        AsccpQueryRepository query = repositoryFactory.asccpQueryRepository(requester);
        AsccpSummaryRecord asccp = query.getAsccpSummary(asccpManifestId);
        if (asccp == null) {
            throw new IllegalArgumentException("The ASCCP manifest record with ID " + asccpManifestId + " could not be found.");
        }
        CcDocument ccDocument = repositoryFactory.ccDocumentFactory().create(requester, asccp.release().releaseId());
        return new AsccpValidationInput(asccp,
                asccp.prevAsccpManifestId() != null ? query.getAsccpSummary(asccp.prevAsccpManifestId()) : null,
                asccp.namespaceId() != null ? ccDocument.getNamespace(asccp.namespaceId()) : null,
                asccp.roleOfAccManifestId() != null ? ccDocument.getAcc(asccp.roleOfAccManifestId()) : null);
    }
}
