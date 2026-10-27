package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.BccpQueryRepository;
import org.oagi.score.gateway.http.api.validation.ValidationInputLoader;
import org.oagi.score.gateway.http.api.validation.ValidationSubject;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

/** Reads a BCCP, its previous revision, its namespace and its data type. */
@Component
public class BccpValidationInputLoader implements ValidationInputLoader<BccpValidationInput> {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Override
    public BccpValidationInput load(ScoreUser requester, ValidationSubject subject) {
        BccpManifestId bccpManifestId;
        try {
            bccpManifestId = new BccpManifestId(new BigInteger(subject.id()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + subject.id() + "' is not a BCCP manifest id.");
        }
        BccpQueryRepository query = repositoryFactory.bccpQueryRepository(requester);
        BccpSummaryRecord bccp = query.getBccpSummary(bccpManifestId);
        if (bccp == null) {
            throw new IllegalArgumentException("The BCCP manifest record with ID " + bccpManifestId + " could not be found.");
        }
        CcDocument ccDocument = repositoryFactory.ccDocumentFactory().create(requester, bccp.release().releaseId());
        return new BccpValidationInput(bccp,
                bccp.prevBccpManifestId() != null ? query.getBccpSummary(bccp.prevBccpManifestId()) : null,
                bccp.namespaceId() != null ? ccDocument.getNamespace(bccp.namespaceId()) : null,
                bccp.dtManifestId() != null ? ccDocument.getDt(bccp.dtManifestId()) : null);
    }
}
