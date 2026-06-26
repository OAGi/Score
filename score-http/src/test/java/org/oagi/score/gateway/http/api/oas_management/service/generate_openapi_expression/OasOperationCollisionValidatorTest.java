package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;
import org.oagi.score.gateway.http.api.oas_management.model.Operation;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPITemplateForVerbOption;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Detects the illegal path-item collapse: two distinct operations fighting over the same body slot on one
 * (path, verb). Complementary Request/Response templates that merge cleanly -- including a legacy Request-only
 * + Response-only pair, even with different auto-derived operationIds -- must NOT be flagged.
 */
class OasOperationCollisionValidatorTest {

    private static OpenAPITemplateForVerbOption tpl(Operation verb, String path, long opId, String messageBody) {
        OpenAPITemplateForVerbOption t = new OpenAPITemplateForVerbOption(verb);
        t.setResourceName(path);
        t.setOasOperationId(new OasOperationId(BigInteger.valueOf(opId)));
        t.setMessageBodyType(messageBody);
        return t;
    }

    private static OpenAPITemplateForVerbOption tpl(Operation verb, String path, long opId, String messageBody,
                                                    String operationId) {
        OpenAPITemplateForVerbOption t = tpl(verb, path, opId, messageBody);
        t.setOperationId(operationId);
        return t;
    }

    @Test
    void noCollision_whenEachPathVerbHasOneOperation() {
        List<OpenAPITemplateForVerbOption> templates = List.of(
                tpl(Operation.POST, "/orders", 1, "Request"),
                tpl(Operation.GET, "/orders", 2, "Response"));
        assertTrue(OasOperationCollisionValidator.detectCollisions(templates, "1").isEmpty());
        assertDoesNotThrow(() -> OasOperationCollisionValidator.assertNoCollision(templates, "1"));
    }

    @Test
    void noCollision_forNormalRequestAndResponseTemplatesOfOneOperation() {
        // ONE operation -> two templates sharing the SAME (path, verb) AND the same oasOperationId.
        // This is the intended Request(->requestBody) + Response(->responses) split, not a collision.
        List<OpenAPITemplateForVerbOption> templates = List.of(
                tpl(Operation.POST, "/orders", 7, "Request"),
                tpl(Operation.POST, "/orders", 7, "Response"));
        assertTrue(OasOperationCollisionValidator.detectCollisions(templates, "1").isEmpty(),
                "Request+Response of one operation must not be a collision");
    }

    @Test
    void noCollision_forManyMessageBodiesUnderOneOperation() {
        // Issue #888 (multiple media types) / #1347 (extra error responses): a single operation may
        // legitimately yield more than two templates sharing one (path, verb) — as long as they all carry
        // the SAME oasOperationId, the guard must not flag them (it keys on distinct operation id, not count).
        List<OpenAPITemplateForVerbOption> templates = List.of(
                tpl(Operation.POST, "/orders", 5, "Request"),
                tpl(Operation.POST, "/orders", 5, "Request"),   // e.g. a second media type under one op
                tpl(Operation.POST, "/orders", 5, "Response"),
                tpl(Operation.POST, "/orders", 5, "Response"));  // e.g. an extra/confirm response under one op
        assertTrue(OasOperationCollisionValidator.detectCollisions(templates, "1").isEmpty(),
                "many message bodies of ONE operation must not be a collision");
    }

    @Test
    void collision_whenTwoDistinctOperationsShareSamePathAndVerb() {
        List<OpenAPITemplateForVerbOption> templates = List.of(
                tpl(Operation.POST, "/orders", 10, "Request"),
                tpl(Operation.POST, "/orders", 10, "Response"),
                tpl(Operation.POST, "/orders", 20, "Request"),
                tpl(Operation.POST, "/orders", 20, "Response"));
        List<OasOperationCollisionValidator.Collision> collisions =
                OasOperationCollisionValidator.detectCollisions(templates, "1");
        assertEquals(1, collisions.size());
        assertEquals("/orders", collisions.get(0).pathKey);
        assertEquals("post", collisions.get(0).verbKey);
        assertEquals(List.of(BigInteger.valueOf(10), BigInteger.valueOf(20)), collisions.get(0).operationIds);
    }

    @Test
    void collision_detectedAfterVersionPlaceholderSubstitution() {
        // Two operations whose stored paths differ ONLY by the {version} placeholder resolve to the same
        // path for documentVersion "1" -> must be flagged.
        List<OpenAPITemplateForVerbOption> templates = List.of(
                tpl(Operation.GET, "/test/{version}/expense-report", 1, "Response"),
                tpl(Operation.GET, "/test/1/expense-report", 2, "Response"));
        List<OasOperationCollisionValidator.Collision> collisions =
                OasOperationCollisionValidator.detectCollisions(templates, "1");
        assertEquals(1, collisions.size());
        assertEquals("/test/1/expense-report", collisions.get(0).pathKey);
    }

    @Test
    void noCollision_sameVerbDifferentPaths() {
        List<OpenAPITemplateForVerbOption> templates = List.of(
                tpl(Operation.POST, "/orders", 1, "Request"),
                tpl(Operation.POST, "/customers", 2, "Request"));
        assertTrue(OasOperationCollisionValidator.detectCollisions(templates, "1").isEmpty());
    }

    @Test
    void assertNoCollision_throwsWithInformativeMessage() {
        List<OpenAPITemplateForVerbOption> templates = List.of(
                tpl(Operation.PUT, "/orders/{id}", 3, "Request"),
                tpl(Operation.PUT, "/orders/{id}", 4, "Request"));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> OasOperationCollisionValidator.assertNoCollision(templates, "1"));
        assertTrue(ex.getMessage().contains("PUT /orders/{id}"), ex.getMessage());
        assertTrue(ex.getMessage().contains("3") && ex.getMessage().contains("4"), ex.getMessage());
    }

    @Test
    void nullsAreSkipped_noFalsePositiveNoNpe() {
        OpenAPITemplateForVerbOption noOpId = new OpenAPITemplateForVerbOption(Operation.POST);
        noOpId.setResourceName("/orders");
        // null oasOperationId -> cannot be attributed, skipped
        List<OpenAPITemplateForVerbOption> templates = List.of(
                noOpId,
                tpl(Operation.POST, "/orders", 1, "Request"));
        assertTrue(OasOperationCollisionValidator.detectCollisions(templates, "1").isEmpty());
        assertTrue(OasOperationCollisionValidator.detectCollisions(null, "1").isEmpty());
    }

    @Test
    void noCollision_forLegacyRequestOnlyAndResponseOnlyOperations() {
        // Pre-#1492 / imported shape: ONE logical endpoint stored as a Request-only operation and a
        // Response-only operation on the same (path, verb). They are complementary (one fills requestBody, the
        // other fills responses), so the generator merges them into one correct path item -> NOT a collision.
        List<OpenAPITemplateForVerbOption> templates = List.of(
                tpl(Operation.PATCH, "/scale-tickets/{id}/status", 139, "Request", "updateStatus"),
                tpl(Operation.PATCH, "/scale-tickets/{id}/status", 144, "Response", "updateStatus"));
        assertTrue(OasOperationCollisionValidator.detectCollisions(templates, "1").isEmpty(),
                "complementary Request-only + Response-only ops must not collide");
        assertDoesNotThrow(() -> OasOperationCollisionValidator.assertNoCollision(templates, "1"));
    }

    @Test
    void noCollision_forComplementaryOperationsWithDifferentOperationIds() {
        // Same (path, verb), one Request-only + one Response-only, with DIFFERENT operationIds. Each operationId
        // is auto-derived from its own message BIE (issue #1732) and request/response BIEs commonly differ; the
        // merged path item simply takes one. The bodies do not clash, so this is NOT a collision.
        List<OpenAPITemplateForVerbOption> templates = List.of(
                tpl(Operation.PATCH, "/shipment-reference-list", 84, "Request", "updateShipmentReferenceList"),
                tpl(Operation.PATCH, "/shipment-reference-list", 77, "Response", "updateShippedItemInstanceList"));
        assertTrue(OasOperationCollisionValidator.detectCollisions(templates, "1").isEmpty(),
                "complementary ops with different operationIds must not collide");
        assertDoesNotThrow(() -> OasOperationCollisionValidator.assertNoCollision(templates, "1"));
    }

    @Test
    void collision_whenTwoDistinctOperationsBothSupplyRequest() {
        // Two DISTINCT operations both filling the requestBody slot would clobber one body -> a real collision,
        // independent of operationId.
        List<OpenAPITemplateForVerbOption> templates = List.of(
                tpl(Operation.POST, "/orders", 1, "Request", "createOrder"),
                tpl(Operation.POST, "/orders", 2, "Request", "createOrder"));
        assertEquals(1, OasOperationCollisionValidator.detectCollisions(templates, "1").size());
    }
}
