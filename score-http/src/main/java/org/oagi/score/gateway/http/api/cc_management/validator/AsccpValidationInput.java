package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.validation.PathSegment;

import java.util.List;

/**
 * An ASCCP with what its rules need of what it is made of.
 *
 * @param previous the previous revision, or null
 * @param roleOf   the ACC of the ASCCP, or null when it has none
 */
public record AsccpValidationInput(AsccpSummaryRecord root,
                         AsccpSummaryRecord previous,
                         NamespaceSummaryRecord namespace,
                         AccSummaryRecord roleOf) implements PropertyValidationInput {

    static Facts factsOf(AsccpSummaryRecord asccp) {
        return asccp == null ? null : new Facts(asccp.den(), asccp.propertyTerm(), asccp.namespaceId(), asccp.definition(),
                asccp.deprecated(), asccp.nillable(), asccp.revisionNum(), asccp.owner());
    }

    @Override
    public Facts facts() {
        return factsOf(root);
    }

    @Override
    public Facts previousFacts() {
        return factsOf(previous);
    }

    @Override
    public List<PathSegment> rootPath() {
        return List.of(new PathSegment("ASCCP", String.valueOf(root.asccpManifestId())));
    }
}
