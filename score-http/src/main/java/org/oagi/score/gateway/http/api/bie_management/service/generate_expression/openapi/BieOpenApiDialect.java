package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.openapi;

import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Decides how {@link BieOpenAPIGenerateExpression} expresses each kind of BIE node for one OpenAPI version.
 * <p>
 * The generator owns the traversal and everything the versions share. At each node it calls the matching
 * hook below, so a new version overrides only the nodes it expresses differently (a subclass such as
 * {@code 3.2 extends 3.1} overrides just what changed). A hook is called at the same position in the
 * schema-building sequence that the version-specific code used to occupy, so the key order of the
 * generated document is preserved.
 * <p>
 * The BBIE hooks cover a BBIE together with its BBIEP (BCCP) property, since both are one leaf property
 * of the schema.
 * <p>
 * The keyword primitives ({@link #nullable}, {@link #fixedValue}, {@link #example},
 * {@link #scContentCopyKeys}) are the building blocks the default node hooks are written with.
 */
public interface BieOpenApiDialect {

    /** The {@code openapi} version string of the generated document. */
    String openApiVersion();

    /** The XBT-to-OpenAPI built-in type map of the version. */
    String xbtOpenApiMap(XbtSummaryRecord xbt);

    /** Whether a DELETE operation can have a request body: OpenAPI 3.0 says to ignore it, 3.1 allows it. */
    boolean allowsDeleteRequestBody();

    // ---- keyword primitives -------------------------------------------------------------------

    /** Wraps a schema so that it also admits {@code null} when {@code isNillable}. */
    Map<String, Object> nullable(Map<String, Object> schema, boolean isNillable);

    /** Expresses a single fixed value. */
    void fixedValue(Map<String, Object> schema, String fixedValue);

    /** Marker for "write no example keyword". */
    Object NO_EXAMPLE = new Object();

    /** Puts the example under the version's keyword. */
    void example(Map<String, Object> schema, Object example);

    /** The keys moved from a BBIE schema into its {@code content} sub-schema when the BBIE has SCs. */
    List<String> scContentCopyKeys();

    // ---- node hooks ---------------------------------------------------------------------------

    /** ABIE: composes the alternatives of a Choice ABIE into {@code parent}. */
    default void abieChoice(Map<String, Object> parent, List<Object> alternatives, AbieSummaryRecord abie) {
        parent.clear();
        parent.put("oneOf", alternatives);
    }

    /** ASBIEP: finishes the object schema of an ASBIEP once its ABIE has been filled. */
    default Map<String, Object> asbiep(Map<String, Object> schema, AsbiepSummaryRecord asbiep) {
        return schema;
    }

    /** ASBIE: finishes an inline (not reused) association object schema. */
    default Map<String, Object> asbie(Map<String, Object> schema, AsbieSummaryRecord asbie, boolean isNillable) {
        return nullable(schema, isNillable);
    }

    /** BBIE: puts the fixed/default value and the example keywords, before the value definition. */
    default void bbieKeywords(Map<String, Object> schema, BbieSummaryRecord bbie, Object example) {
        valueConstraintKeywords(schema, bbie.valueConstraint(), example);
    }

    /** BBIE: finishes the schema of a BBIE without supplementary components. */
    default Map<String, Object> bbie(Map<String, Object> schema, BbieSummaryRecord bbie, boolean isNillable) {
        return nullable(schema, isNillable);
    }

    /** BBIE: finishes the {@code content} schema of a BBIE with supplementary components. */
    default Map<String, Object> bbieContent(Map<String, Object> content, BbieSummaryRecord bbie, boolean isNillable) {
        return nullable(content, isNillable);
    }

    /** BBIESC: puts the fixed/default value and the example keywords, before the value definition. */
    default void bbieScKeywords(Map<String, Object> schema, BbieScSummaryRecord bbieSc, Object example) {
        valueConstraintKeywords(schema, bbieSc.valueConstraint(), example);
    }

    /** BBIESC: finishes the schema of a supplementary component. */
    default Map<String, Object> bbieSc(Map<String, Object> schema, BbieScSummaryRecord bbieSc) {
        return schema;
    }

    // ---- shared helper ------------------------------------------------------------------------

    private void valueConstraintKeywords(Map<String, Object> schema, ValueConstraint valueConstraint, Object example) {
        if (valueConstraint != null) {
            if (StringUtils.hasLength(valueConstraint.fixedValue())) {
                fixedValue(schema, valueConstraint.fixedValue());
            } else if (StringUtils.hasLength(valueConstraint.defaultValue())) {
                schema.put("default", valueConstraint.defaultValue());
            }
        }
        if (example != NO_EXAMPLE) {
            example(schema, example);
        }
    }
}
