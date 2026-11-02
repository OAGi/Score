package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.AccQueryRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.validation.*;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Asks what a change of structure (refactoring an association to a base ACC, appending an association, setting the base ACC) would break, before it is made: it lays the ACCs that the change affects out as they
 * are and as they would be afterwards, runs the rules that a change asks for ({@link ValidationTrigger#STRUCTURE_CHANGE})
 * on both, and keeps what only the second has. Every ACC that the change reaches is checked; they share one reading of
 * the release, so the number of them does not decide how long it takes to read.
 */
@Service
@Transactional(readOnly = true)
public class AccStructureChangeValidationService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private ValidationEngine validationEngine;

    @Autowired
    private AccSubjectValidator validator;

    public StructureChangeReport refactorAscc(ScoreUser requester, AsccManifestId asccManifestId, AccManifestId destination) {
        AccQueryRepository accQuery = repositoryFactory.accQueryRepository(requester);
        AsccSummaryRecord ascc = require(accQuery.getAsccSummary(asccManifestId), "ASCC", asccManifestId);
        CcDocument ccDocument = document(requester, ascc.release().releaseId());
        return check(ccDocument, ascc.fromAccManifestId(),
                AccStructureChange.refactor(ccDocument, ascc, destination, accQuery.getRefactorTargetAsccManifestList(asccManifestId, destination)));
    }

    public StructureChangeReport refactorBcc(ScoreUser requester, BccManifestId bccManifestId, AccManifestId destination) {
        AccQueryRepository accQuery = repositoryFactory.accQueryRepository(requester);
        BccSummaryRecord bcc = require(accQuery.getBccSummary(bccManifestId), "BCC", bccManifestId);
        CcDocument ccDocument = document(requester, bcc.release().releaseId());
        return check(ccDocument, bcc.fromAccManifestId(),
                AccStructureChange.refactor(ccDocument, bcc, destination, accQuery.getRefactorTargetBccManifestList(bccManifestId, destination)));
    }

    /** What appending an association to a property of an ASCCP would break. */
    public StructureChangeReport appendAscc(ScoreUser requester, AccManifestId accManifestId, AsccpManifestId asccpManifestId) {
        AccSummaryRecord acc = require(repositoryFactory.accQueryRepository(requester).getAccSummary(accManifestId), "ACC", accManifestId);
        CcDocument ccDocument = document(requester, acc.release().releaseId());
        AsccpSummaryRecord asccp = require(ccDocument.getAsccp(asccpManifestId), "ASCCP", asccpManifestId);
        return check(ccDocument, accManifestId, AccStructureChange.append(ccDocument, accManifestId, asccp));
    }

    /** What appending an association to a property of a BCCP would break. */
    public StructureChangeReport appendBcc(ScoreUser requester, AccManifestId accManifestId, BccpManifestId bccpManifestId) {
        AccSummaryRecord acc = require(repositoryFactory.accQueryRepository(requester).getAccSummary(accManifestId), "ACC", accManifestId);
        CcDocument ccDocument = document(requester, acc.release().releaseId());
        BccpSummaryRecord bccp = require(ccDocument.getBccp(bccpManifestId), "BCCP", bccpManifestId);
        return check(ccDocument, accManifestId, AccStructureChange.append(ccDocument, accManifestId, bccp));
    }

    /** What making the ACC based on another ACC would break. */
    public StructureChangeReport setBase(ScoreUser requester, AccManifestId accManifestId, AccManifestId basedAccManifestId) {
        AccSummaryRecord acc = require(repositoryFactory.accQueryRepository(requester).getAccSummary(accManifestId), "ACC", accManifestId);
        CcDocument ccDocument = document(requester, acc.release().releaseId());
        require(ccDocument.getAcc(basedAccManifestId), "ACC", basedAccManifestId);
        return check(ccDocument, accManifestId, AccStructureChange.setBase(ccDocument, accManifestId, basedAccManifestId));
    }

    private CcDocument document(ScoreUser requester, ReleaseId releaseId) {
        return repositoryFactory.ccDocumentFactory().create(requester, releaseId);
    }

    private static <T> T require(T value, String what, Object id) {
        if (value == null) {
            throw new IllegalArgumentException("The " + what + " manifest record with ID " + id + " could not be found.");
        }
        return value;
    }

    /** The problems that only the ACCs as they would be have. */
    StructureChangeReport check(CcDocument ccDocument, AccManifestId source, AccStructureChange change) {
        AccGraphSource asTheyAre = AccGraphSource.of(ccDocument);
        AccGraphSource asTheyWouldBe = new AccGraphOverlay(asTheyAre, change.afterwards(), change.accs());
        Set<AccManifestId> affected = AccStructureChange.affectedBy(ccDocument, change.seeds());

        List<Violation> found = new ArrayList<>();
        for (AccManifestId accManifestId : affected) {
            AccSummaryRecord acc = ccDocument.getAcc(accManifestId);
            ValidationSubject subject = new ValidationSubject(SubjectType.ACC, accManifestId.toString());
            Map<String, Integer> before = new HashMap<>();
            for (Violation v : violationsOf(subject, AccValidationInputLoader.layOut(asTheyAre, acc))) {
                before.merge(identity(v), size(v), Math::max);
            }
            SubjectRef about = new SubjectRef(SubjectType.ACC, accManifestId.toString(), acc.den());
            violationsOf(subject, AccValidationInputLoader.layOut(asTheyWouldBe, asTheyWouldBe.getAcc(accManifestId))).stream()
                    .filter(v -> size(v) > before.getOrDefault(identity(v), 0))
                    .map(v -> v.about(about))
                    .forEach(found::add);
        }
        found.sort(Comparator.comparing((Violation v) -> v.subject().label())
                .thenComparing(Violation::severity).thenComparing(Violation::ruleId)
                .thenComparing(v -> v.locations().isEmpty() ? "" : v.locations().get(0).label()));

        ReportStatus status = found.stream().anyMatch(v -> v.severity() == Severity.ERROR) ? ReportStatus.HAS_ERRORS
                : found.isEmpty() ? ReportStatus.VALID : ReportStatus.HAS_WARNINGS;
        return new StructureChangeReport(new ValidationReport(new ValidationSubject(SubjectType.ACC, source.toString()),
                new Date(), status, found), affected.size());
    }

    private List<Violation> violationsOf(ValidationSubject subject, AccValidationInput input) {
        return validationEngine.evaluate(validator, subject, input, ValidationTrigger.STRUCTURE_CHANGE);
    }

    /**
     * What makes a problem the same one before and after: the property term it is about (the rule can change when a
     * property moves from the ACC to its base ACC). It is new when more properties have the term than before: one more
     * property with a term that is twice there already is a problem that is new, one fewer is not.
     */
    private static String identity(Violation violation) {
        String term = violation.params().get("propertyTerm");
        return term != null ? "term:" + term : violation.ruleId() + ":" + violation.message();
    }

    /** How many properties the problem is about. */
    private static int size(Violation violation) {
        return violation.params().get("propertyTerm") != null ? violation.locations().size() : 1;
    }
}
