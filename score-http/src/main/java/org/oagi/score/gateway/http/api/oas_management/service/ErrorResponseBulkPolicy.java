package org.oagi.score.gateway.http.api.oas_management.service;

import org.oagi.score.gateway.http.api.oas_management.model.OpenAPIErrorResponseBodyType;
import org.oagi.score.gateway.http.api.oas_management.model.OperationErrorResponseAssignment;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Issue #1347: the pure decision logic for the document-level Error Response Body Type — kept free of
 * jOOQ/Spring so it can be unit-tested directly. The service groups the document's operation rows into
 * plain maps (operation id -> set of release ids of its bodies; empty set = bodyless) and delegates
 * here.
 */
public final class ErrorResponseBulkPolicy {

    private ErrorResponseBulkPolicy() {
    }

    /** The value a newly-created operation inherits from the document's prevailing state. */
    public record Inherited(String bodyType, BigInteger confirmTopLevelAsbiepId) {
    }

    /**
     * Which operations get which value for a "apply to all" bulk request.
     * <ul>
     *   <li>NONE / PROBLEM_DETAILS -> every operation, no ConfirmMessage BIE.</li>
     *   <li>CONFIRM_MESSAGE -> operations whose BIE is in {@code releaseId}, plus bodyless operations
     *       (no release to match); operations only in other releases are omitted (left unchanged).</li>
     * </ul>
     */
    public static List<OperationErrorResponseAssignment> resolveBulkAssignments(
            Map<BigInteger, Set<BigInteger>> releasesByOp, String bodyType,
            BigInteger releaseId, BigInteger confirmTopLevelAsbiepId) {
        OpenAPIErrorResponseBodyType type = OpenAPIErrorResponseBodyType.from(bodyType);
        List<OperationErrorResponseAssignment> assignments = new ArrayList<>();
        for (Map.Entry<BigInteger, Set<BigInteger>> entry : releasesByOp.entrySet()) {
            BigInteger oasOperationId = entry.getKey();
            if (type == OpenAPIErrorResponseBodyType.CONFIRM_MESSAGE) {
                boolean bodyless = entry.getValue().isEmpty();
                boolean matchesRelease = releaseId != null && entry.getValue().contains(releaseId);
                if (bodyless || matchesRelease) {
                    assignments.add(new OperationErrorResponseAssignment(
                            oasOperationId, type.name(), confirmTopLevelAsbiepId));
                }
            } else {
                assignments.add(new OperationErrorResponseAssignment(oasOperationId, type.name(), null));
            }
        }
        return assignments;
    }

    /**
     * The error-response body type a newly-created operation inherits from the document's OTHER
     * operations.
     * <ul>
     *   <li>All other operations PROBLEM_DETAILS -> PROBLEM_DETAILS.</li>
     *   <li>All other operations CONFIRM_MESSAGE -> the ConfirmMessage BIE of the new operation's own
     *       release (a BIE-backed op) or the single doc-wide ConfirmMessage (a bodyless op), but only
     *       when that BIE is unambiguous; otherwise NONE.</li>
     *   <li>Otherwise (no other operations, all NONE, or a mix) -> NONE.</li>
     * </ul>
     *
     * @param typeByOp      other operations' body types, keyed by operation id
     * @param confirmByOp   other operations' ConfirmMessage BIE ids, keyed by operation id (absent when none)
     * @param releasesByOp  other operations' release ids, keyed by operation id (empty = bodyless)
     * @param newOpReleases the new operation's own release ids (empty = the new op is bodyless)
     */
    public static Inherited computeInherited(
            Map<BigInteger, String> typeByOp,
            Map<BigInteger, BigInteger> confirmByOp,
            Map<BigInteger, Set<BigInteger>> releasesByOp,
            Set<BigInteger> newOpReleases) {
        if (typeByOp == null || typeByOp.isEmpty()) {
            return new Inherited(OpenAPIErrorResponseBodyType.NONE.name(), null);
        }
        boolean allProblemDetails = typeByOp.values().stream()
                .allMatch(t -> OpenAPIErrorResponseBodyType.from(t) == OpenAPIErrorResponseBodyType.PROBLEM_DETAILS);
        boolean allConfirmMessage = typeByOp.values().stream()
                .allMatch(t -> OpenAPIErrorResponseBodyType.from(t) == OpenAPIErrorResponseBodyType.CONFIRM_MESSAGE);

        if (allProblemDetails) {
            return new Inherited(OpenAPIErrorResponseBodyType.PROBLEM_DETAILS.name(), null);
        }
        if (allConfirmMessage) {
            Set<BigInteger> candidateConfirmIds = new HashSet<>();
            for (Map.Entry<BigInteger, Set<BigInteger>> entry : releasesByOp.entrySet()) {
                // A BIE-backed new op (has a release) matches only same-release operations; a bodyless
                // new op (no release) considers every operation.
                boolean consider = (newOpReleases == null || newOpReleases.isEmpty())
                        || entry.getValue().stream().anyMatch(newOpReleases::contains);
                BigInteger confirmId = confirmByOp.get(entry.getKey());
                if (consider && confirmId != null) {
                    candidateConfirmIds.add(confirmId);
                }
            }
            if (candidateConfirmIds.size() == 1) {
                return new Inherited(OpenAPIErrorResponseBodyType.CONFIRM_MESSAGE.name(),
                        candidateConfirmIds.iterator().next());
            }
        }
        return new Inherited(OpenAPIErrorResponseBodyType.NONE.name(), null);
    }
}
