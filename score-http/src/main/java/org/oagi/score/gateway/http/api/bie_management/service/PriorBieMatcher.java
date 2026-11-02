package org.oagi.score.gateway.http.api.bie_management.service;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.common.model.Guid;

import java.util.*;
import java.util.function.Function;

/**
 * Issue #1733: finds, for each BIE of a BIE Package, the BIE of the prior package version that it replaces, so that the
 * manifest can say what changed. A BIE of the prior package is the counterpart of at most one BIE of the package.
 *
 * <p>The counterpart is looked for in this order, and a BIE that is matched by an earlier step is not offered to the
 * later ones:
 * <ol>
 *   <li>the same BIE: a revision of a package starts with the BIEs of the prior package, so a BIE that has not been
 *   replaced is in both;</li>
 *   <li>the BIE it was made from: a BIE that was made from another BIE by a Copy or an Uplift has a link to it, and
 *   the link is followed through the BIEs that are in neither package until it reaches a BIE of the prior package;</li>
 *   <li>the same root ASCCP: the BIEs that are not matched by the steps above are paired in the order of the
 *   packages when they are about the same ASCCP.</li>
 * </ol>
 * The BIEs that are matched by the second and the third step must be about the same root ASCCP, since BIEs about
 * different ASCCPs cannot be compared.
 */
final class PriorBieMatcher {

    private PriorBieMatcher() {
    }

    /**
     * A BIE of a package.
     *
     * @param sourceId     the BIE it was made from, or {@code null}
     * @param rootAsccpGuid the GUID of the ASCCP of its root, or {@code null} when it cannot be told
     */
    record Candidate(TopLevelAsbiepId id, TopLevelAsbiepId sourceId, Guid rootAsccpGuid) {
    }

    /**
     * @param sourceOf the BIE that a BIE was made from, for the BIEs that are in neither package ({@code null} if it
     *                 has none or the BIE is gone)
     * @return the counterpart in the prior package, by the BIE of the package
     */
    static Map<TopLevelAsbiepId, TopLevelAsbiepId> match(List<Candidate> current, List<Candidate> prior,
                                                          Function<TopLevelAsbiepId, TopLevelAsbiepId> sourceOf) {
        Map<TopLevelAsbiepId, Candidate> priorById = new LinkedHashMap<>();
        prior.forEach(p -> priorById.putIfAbsent(p.id(), p));

        Map<TopLevelAsbiepId, TopLevelAsbiepId> matches = new LinkedHashMap<>();
        Set<TopLevelAsbiepId> claimed = new HashSet<>();

        for (Candidate c : current) {
            if (priorById.containsKey(c.id()) && claimed.add(c.id())) {
                matches.put(c.id(), c.id());
            }
        }

        for (Candidate c : current) {
            if (matches.containsKey(c.id())) {
                continue;
            }
            Set<TopLevelAsbiepId> visited = new HashSet<>();
            TopLevelAsbiepId source = c.sourceId();
            while (source != null && visited.add(source)) {
                Candidate p = priorById.get(source);
                if (p != null && !claimed.contains(p.id()) && sameAsccp(c, p)) {
                    matches.put(c.id(), p.id());
                    claimed.add(p.id());
                    break;
                }
                source = sourceOf.apply(source);
            }
        }

        for (Candidate c : current) {
            if (matches.containsKey(c.id())) {
                continue;
            }
            for (Candidate p : priorById.values()) {
                if (!claimed.contains(p.id()) && sameAsccp(c, p)) {
                    matches.put(c.id(), p.id());
                    claimed.add(p.id());
                    break;
                }
            }
        }
        return matches;
    }

    private static boolean sameAsccp(Candidate a, Candidate b) {
        return a.rootAsccpGuid() != null && a.rootAsccpGuid().equals(b.rootAsccpGuid());
    }
}
