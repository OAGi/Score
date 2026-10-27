package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.validation.Location;
import org.oagi.score.gateway.http.api.validation.PathSegment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What an ACC has once everything it inherits and every group it contains is laid out in one list: its own properties,
 * the properties of the groups it contains (in place), and those of its base ACCs. Every property carries the address
 * of its node in the tree of the screen, so that a problem can be shown where it is.
 */
public record AccValidationInput(AccSummaryRecord root,
                                AccSummaryRecord previous,
                                NamespaceSummaryRecord namespace,
                                List<Property> properties,
                                List<BaseCycle> baseCycles,
                                List<OwnAscc> ownAsccs,
                                List<OwnBcc> ownBccs,
                                List<Base> bases) {

    /** An ACC that is not a revision and has no namespace of a known kind, with the lists that need no lookup. */
    public AccValidationInput(AccSummaryRecord root, List<Property> properties, List<BaseCycle> baseCycles,
                             List<OwnAscc> ownAsccs, List<OwnBcc> ownBccs, List<Base> bases) {
        this(root, null, null, properties, baseCycles, ownAsccs, ownBccs, bases);
    }

    public enum Kind {ASCC, BCC}

    /**
     * One property (an ASCC or a BCC with its property).
     *
     * @param owner     the ACC that has the association (a group, a base ACC or the ACC itself)
     * @param inherited reached through a base ACC
     * @param viaGroup  reached through a group
     * @param path      the address of the property node, root first
     */
    public record Property(String term,
                           Kind kind,
                           AccSummaryRecord owner,
                           boolean inherited,
                           boolean viaGroup,
                           List<PathSegment> path) {

        public Location location(String role) {
            return new Location(term + " (" + owner.den() + ")", path, role);
        }
    }

    /**
     * An ASCC of the ACC itself; the property and the ACC of the property are null when the ASCC points at nothing.
     *
     * @param previous   the ASCC of the previous revision, or null
     * @param references how many ASCCs point at the property (counted only for a property that is not reusable)
     */
    public record OwnAscc(AsccSummaryRecord ascc, AsccpSummaryRecord asccp, AccSummaryRecord roleOf,
                          AsccSummaryRecord previous, int references, List<PathSegment> path) {

        public OwnAscc(AsccSummaryRecord ascc, AsccpSummaryRecord asccp, AccSummaryRecord roleOf, List<PathSegment> path) {
            this(ascc, asccp, roleOf, null, 1, path);
        }
    }

    /**
     * A BCC of the ACC itself; the property is null when the BCC points at nothing.
     *
     * @param previous                  the BCC of the previous revision, or null
     * @param targetHasSupplementaryComponents the data type of the property has a supplementary component that can occur
     */
    public record OwnBcc(BccSummaryRecord bcc, BccpSummaryRecord bccp, BccSummaryRecord previous,
                         boolean targetHasSupplementaryComponents, List<PathSegment> path) {

        public OwnBcc(BccSummaryRecord bcc, BccpSummaryRecord bccp, List<PathSegment> path) {
            this(bcc, bccp, null, false, path);
        }
    }

    /** A base ACC of the ACC, nearest first. */
    public record Base(AccSummaryRecord acc, List<PathSegment> path) {
    }

    /** A base ACC that is already on the way to the root: the chain of base ACCs comes back to itself. */
    public record BaseCycle(AccSummaryRecord acc, AccSummaryRecord base, List<PathSegment> path) {
    }

    /** The properties by term, only the terms that more than one property has. */
    public Map<String, List<Property>> duplicates() {
        Map<String, List<Property>> byTerm = new LinkedHashMap<>();
        for (Property property : properties) {
            byTerm.computeIfAbsent(property.term(), k -> new ArrayList<>()).add(property);
        }
        byTerm.values().removeIf(list -> list.size() < 2);
        return byTerm;
    }
}
