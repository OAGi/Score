package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.DtQueryRepository;
import org.oagi.score.gateway.http.api.validation.PathSegment;
import org.oagi.score.gateway.http.api.validation.ValidationInputLoader;
import org.oagi.score.gateway.http.api.validation.ValidationSubject;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;

/** Reads a DT, its previous revision, its namespace, its supplementary components and their value domains. */
@Component
public class DtValidationInputLoader implements ValidationInputLoader<DtValidationInput> {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Override
    public DtValidationInput load(ScoreUser requester, ValidationSubject subject) {
        DtManifestId dtManifestId;
        try {
            dtManifestId = new DtManifestId(new BigInteger(subject.id()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + subject.id() + "' is not a DT manifest id.");
        }
        DtQueryRepository query = repositoryFactory.dtQueryRepository(requester);
        DtSummaryRecord dt = query.getDtSummary(dtManifestId);
        if (dt == null) {
            throw new IllegalArgumentException("The DT manifest record with ID " + dtManifestId + " could not be found.");
        }
        CcDocument ccDocument = repositoryFactory.ccDocumentFactory().create(requester, dt.release().releaseId());
        List<PathSegment> rootPath = List.of(new PathSegment("DT", String.valueOf(dtManifestId)));
        return new DtValidationInput(dt,
                dt.prevDtManifestId() != null ? query.getDtSummary(dt.prevDtManifestId()) : null,
                dt.namespaceId() != null ? ccDocument.getNamespace(dt.namespaceId()) : null,
                ccDocument.getDtAwdPriList(dtManifestId).stream().map(DtValidationInput.ValueDomain::of).toList(),
                ccDocument.getDtScList(dtManifestId).stream()
                        .filter(sc -> sc.state() != CcState.Deleted)
                        .map(sc -> new DtValidationInput.SupplementaryComponent(sc,
                                ccDocument.getDtScAwdPriList(sc.dtScManifestId()).stream().map(DtValidationInput.ValueDomain::of).toList(),
                                append(rootPath, new PathSegment("DT_SC", String.valueOf(sc.dtScManifestId())))))
                        .toList());
    }

    private static List<PathSegment> append(List<PathSegment> path, PathSegment segment) {
        return java.util.stream.Stream.concat(path.stream(), java.util.stream.Stream.of(segment)).toList();
    }
}
