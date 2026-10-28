package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;

import java.util.Map;

/**
 * What a user of {@link BieOpenApiSchemaBuilder} expresses differently in the schemas. The defaults are what the BIE
 * generator expresses; the OpenAPI Document generator overrides them.
 */
public interface BieOpenApiSchemaPolicy {

    /** The policy of the BIE generator. */
    BieOpenApiSchemaPolicy STANDARD = new BieOpenApiSchemaPolicy() {
    };

    /** Puts what is added to the root of the schema of a top-level BIE, after its version. */
    default void putRootExtensions(Map<String, Object> properties, AsccpSummaryRecord asccp) {
    }

    /** Whether the {@code deprecated} of an ASBIE moves from its items to the array schema. */
    default boolean moveDeprecatedToAssociationArray() {
        return true;
    }

    /** Whether the description of a BBIE moves from its items to the array schema. */
    default boolean moveDescriptionToLeafArray() {
        return false;
    }

    /**
     * Whether a BIE that is not the one of the schema of the same name gets a schema of its own, named after the name and
     * its top-level BIE.
     */
    default boolean separateSchemaForOtherBieOfSameName() {
        return false;
    }
}
