package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.openapi.BieOpenApiSchemaPolicy;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;

import java.util.Map;

/**
 * What the schemas of an OpenAPI Document express differently from the ones of a BIE generated as OpenAPI. Each of these
 * is kept as the OpenAPI Document generator expressed it before it built its schemas with the
 * {@code BieOpenApiSchemaBuilder}, so that the generated documents did not change.
 */
class OasDocumentSchemaPolicy implements BieOpenApiSchemaPolicy {

    /** Issue #1603: the DEN of the BIE of a schema. */
    @Override
    public void putRootExtensions(Map<String, Object> properties, AsccpSummaryRecord asccp) {
        properties.put("x-oagis-bie-den", asccp.propertyTerm());
    }

    @Override
    public boolean moveDeprecatedToAssociationArray() {
        return false;
    }

    @Override
    public boolean moveDescriptionToLeafArray() {
        return true;
    }

    /** The schema names are given by the operations, so two BIEs of one name can come to one document. */
    @Override
    public boolean separateSchemaForOtherBieOfSameName() {
        return true;
    }
}
