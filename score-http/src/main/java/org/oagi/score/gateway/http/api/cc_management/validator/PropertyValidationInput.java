package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.validation.PathSegment;

import java.util.List;

/** What an ASCCP and a BCCP have in common: the input the rules about a property of a core component are written for. */
public interface PropertyValidationInput {

    /** What the rules read of one revision of the property. */
    record Facts(String den,
                 String propertyTerm,
                 NamespaceId namespaceId,
                 Definition definition,
                 boolean deprecated,
                 boolean nillable,
                 int revisionNum,
                 UserSummaryRecord owner) {
    }

    /** The property itself. */
    Facts facts();

    /** The previous revision, or null when there is none. */
    Facts previousFacts();

    /** The namespace of the property, or null when it has none. */
    NamespaceSummaryRecord namespace();

    /** The address of the root node of the tree of the screen. */
    List<PathSegment> rootPath();
}
