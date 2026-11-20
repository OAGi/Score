package org.oagi.score.gateway.http.api.cc_management.service;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ValueListCandidate;

import java.util.*;

/**
 * Which Code Lists or Agency ID Lists of a release a user can choose for a data type (or a supplementary component of
 * it) of that release: the one rule of the BIE editor and of uplifting a BIE. It works on what was read of the release,
 * and reads nothing.
 * <ol>
 *   <li>The lists the approved primitives of the data type name come first. When it names none, every list of the
 *       release is a candidate.</li>
 *   <li>A developer sees only the Published and Production lists; the other users see every list.</li>
 *   <li>The lists derived from, or based on, the lists named are candidates too, as {@link Expansion} says.</li>
 * </ol>
 * A developer who is to <em>assign</em> a list, and not only to see it, cannot assign a Production list or the list of an
 * end user: {@code limitToWhatADeveloperCanAssign} takes them out.
 */
public final class ValueListCandidateRule {

    /** How the lists named by the approved primitives reach the lists derived from, or based on, them. */
    public enum Expansion {
        /**
         * Code Lists: the lists a named list is based on, as far up as they go, the lists derived from each of those
         * (so the lists derived from the same base), and the lists derived directly from the named list.
         */
        BASES_AND_THEIR_DERIVED,
        /** Agency ID Lists: the lists derived from a named list, and from those, as far down as they go. */
        DERIVED
    }

    private ValueListCandidateRule() {
    }

    /**
     * @param releaseLists          the lists of the release of the data type
     * @param namedByDataType       the manifest of every list that an approved primitive of the data type names, whatever
     *                              state the list is in; empty when it names none
     * @param developer             whether the user is a developer
     * @param limitToWhatADeveloperCanAssign whether to leave out what a developer cannot assign (ignored for the others)
     * @return the manifests of the candidates, in the order of {@code releaseLists}
     */
    public static <I extends ManifestId> Set<I> resolve(
            Collection<ValueListCandidate<I>> releaseLists,
            Collection<I> namedByDataType,
            boolean developer,
            Expansion expansion,
            boolean limitToWhatADeveloperCanAssign) {

        Map<I, ValueListCandidate<I>> byManifest = new LinkedHashMap<>();
        Map<I, List<ValueListCandidate<I>>> derivedOf = new HashMap<>();
        for (ValueListCandidate<I> list : releaseLists) {
            if (list == null) {
                continue;
            }
            byManifest.put(list.manifestId(), list);
            if (list.basedManifestId() != null) {
                derivedOf.computeIfAbsent(list.basedManifestId(), k -> new ArrayList<>()).add(list);
            }
        }

        Set<I> reached = new HashSet<>();
        if (namedByDataType == null || namedByDataType.isEmpty()) {
            byManifest.values().stream().filter(e -> visible(e, developer)).forEach(e -> reached.add(e.manifestId()));
        } else {
            for (I named : namedByDataType) {
                ValueListCandidate<I> list = byManifest.get(named);
                if (list == null || !visible(list, developer)) {
                    continue;
                }
                if (expansion == Expansion.DERIVED) {
                    withDerived(list, derivedOf, developer, reached);
                } else {
                    withBasesAndTheirDerived(list, byManifest, derivedOf, developer, reached);
                }
            }
        }

        Set<I> candidates = new LinkedHashSet<>();
        for (ValueListCandidate<I> list : byManifest.values()) {
            if (reached.contains(list.manifestId())
                    && !(developer && limitToWhatADeveloperCanAssign && !assignableByDeveloper(list))) {
                candidates.add(list.manifestId());
            }
        }
        return candidates;
    }

    private static boolean visible(ValueListCandidate<?> list, boolean developer) {
        return !developer || list.state() == CcState.Published || list.state() == CcState.Production;
    }

    private static boolean assignableByDeveloper(ValueListCandidate<?> list) {
        return list.state() != CcState.Production && !list.endUserOwned();
    }

    private static <I extends ManifestId> void withDerived(
            ValueListCandidate<I> list, Map<I, List<ValueListCandidate<I>>> derivedOf,
            boolean developer, Set<I> reached) {
        if (!reached.add(list.manifestId())) {
            return;
        }
        for (ValueListCandidate<I> derived : derivedOf.getOrDefault(list.manifestId(), List.of())) {
            if (visible(derived, developer)) {
                withDerived(derived, derivedOf, developer, reached);
            }
        }
    }

    private static <I extends ManifestId> void withBasesAndTheirDerived(
            ValueListCandidate<I> list, Map<I, ValueListCandidate<I>> byManifest,
            Map<I, List<ValueListCandidate<I>>> derivedOf, boolean developer, Set<I> reached) {
        Set<I> visited = new HashSet<>();
        ValueListCandidate<I> current = list;
        while (current != null && visited.add(current.manifestId())) {
            reached.add(current.manifestId());
            for (ValueListCandidate<I> derived : derivedOf.getOrDefault(current.manifestId(), List.of())) {
                if (visible(derived, developer)) {
                    reached.add(derived.manifestId());
                }
            }
            ValueListCandidate<I> base = current.basedManifestId() == null ? null : byManifest.get(current.basedManifestId());
            current = (base != null && visible(base, developer)) ? base : null;
        }
    }
}
