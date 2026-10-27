package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.validation.PathSegment;

import java.util.List;

/**
 * A BCCP with what its rules need of what it is made of.
 *
 * @param previous the previous revision, or null
 * @param bdt      the data type of the BCCP, or null when it has none
 */
public record BccpValidationInput(BccpSummaryRecord root,
                        BccpSummaryRecord previous,
                        NamespaceSummaryRecord namespace,
                        DtSummaryRecord bdt) implements PropertyValidationInput {

    static Facts factsOf(BccpSummaryRecord bccp) {
        return bccp == null ? null : new Facts(bccp.den(), bccp.propertyTerm(), bccp.namespaceId(), bccp.definition(),
                bccp.deprecated(), bccp.nillable(), bccp.revisionNum(), bccp.owner());
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
        return List.of(new PathSegment("BCCP", String.valueOf(root.bccpManifestId())));
    }
}
