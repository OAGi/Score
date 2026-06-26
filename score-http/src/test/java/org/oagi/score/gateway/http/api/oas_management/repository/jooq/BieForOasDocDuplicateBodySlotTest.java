package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #1492 (Option 2): inline-edit duplicate-body-slot guard
 * ({@link JooqBieForOasDocCommandRepository#assertNoDuplicateBodySlot}).
 *
 * The legitimate "one Request + one Response on a single (path, verb)" pair must NOT be rejected — whether
 * the two rows share one operation id OR sit on separate operation rows (the legacy/imported shape), and even
 * when those rows carry different auto-derived operationIds. Only a TRUE duplicate body slot (a 2nd Request or
 * a 2nd Response on one (path, verb)) throws with the contracted message.
 */
class BieForOasDocDuplicateBodySlotTest {

    private static BieForOasDoc row(String resourceName, String verb, String messageBody, Long opId) {
        BieForOasDoc r = new BieForOasDoc();
        r.setResourceName(resourceName);
        r.setVerb(verb);
        r.setMessageBody(messageBody);
        if (opId != null) {
            r.setOasOperationId(new OasOperationId(BigInteger.valueOf(opId)));
        }
        return r;
    }

    private static BieForOasDoc row(String resourceName, String verb, String messageBody, Long opId, String operationId) {
        BieForOasDoc r = row(resourceName, verb, messageBody, opId);
        r.setOperationId(operationId);
        return r;
    }

    @Test
    void legitimateRequestAndResponseOnOneOperation_doesNotThrow() {
        // ONE operation -> Request + Response sharing the SAME (path, verb) AND the same operation id.
        List<BieForOasDoc> rows = List.of(
                row("/orders", "POST", "Request", 7L),
                row("/orders", "POST", "Response", 7L));
        assertDoesNotThrow(() -> JooqBieForOasDocCommandRepository.assertNoDuplicateBodySlot(rows));
    }

    @Test
    void emptyOrNullList_doesNotThrow() {
        assertDoesNotThrow(() -> JooqBieForOasDocCommandRepository.assertNoDuplicateBodySlot(null));
        assertDoesNotThrow(() -> JooqBieForOasDocCommandRepository.assertNoDuplicateBodySlot(List.of()));
    }

    @Test
    void duplicateRequestBodyOnSamePathVerb_throws() {
        List<BieForOasDoc> rows = List.of(
                row("/orders", "POST", "Request", 7L),
                row("/orders", "POST", "Request", 7L));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> JooqBieForOasDocCommandRepository.assertNoDuplicateBodySlot(rows));
        assertTrue(ex.getMessage().contains("POST /orders"), ex.getMessage());
        assertTrue(ex.getMessage().contains("Request"), ex.getMessage());
        assertTrue(ex.getMessage().contains("at most one Request and one Response"), ex.getMessage());
    }

    @Test
    void duplicateResponseBodyOnSamePathVerb_throws() {
        List<BieForOasDoc> rows = List.of(
                row("/orders", "GET", "Response", 9L),
                row("/orders", "GET", "Response", 9L));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> JooqBieForOasDocCommandRepository.assertNoDuplicateBodySlot(rows));
        assertTrue(ex.getMessage().contains("GET /orders"), ex.getMessage());
        assertTrue(ex.getMessage().contains("Response"), ex.getMessage());
    }

    @Test
    void twoOperationRowsSharingOperationId_doNotThrow() {
        // Legacy/imported shape: same (path, verb), one Request + one Response, on DIFFERENT oas_operation
        // rows but agreeing on the SAME operationId identity. The generator merges them into one path item,
        // so saving such a doc must be allowed (this is exactly what a pre-#1492 / prod-imported doc looks like).
        List<BieForOasDoc> rows = List.of(
                row("/orders", "POST", "Request", 10L, "createOrder"),
                row("/orders", "POST", "Response", 20L, "createOrder"));
        assertDoesNotThrow(() -> JooqBieForOasDocCommandRepository.assertNoDuplicateBodySlot(rows));
    }

    @Test
    void twoOperationRowsWithDifferentOperationId_doNotThrow() {
        // Same (path, verb), complementary body types, on different operation rows with DIFFERENT operationIds
        // (each auto-derived from its own message BIE -- request and response BIEs commonly differ). The bodies
        // do not clash, so saving is allowed; the generator merges them into one path item.
        List<BieForOasDoc> rows = List.of(
                row("/orders", "POST", "Request", 10L, "createOrder"),
                row("/orders", "POST", "Response", 20L, "updateOrder"));
        assertDoesNotThrow(() -> JooqBieForOasDocCommandRepository.assertNoDuplicateBodySlot(rows));
    }

    @Test
    void differentPathsOrVerbs_doNotCollide() {
        List<BieForOasDoc> rows = List.of(
                row("/orders", "POST", "Request", 1L),
                row("/orders", "GET", "Response", 2L),
                row("/customers", "POST", "Request", 3L));
        assertDoesNotThrow(() -> JooqBieForOasDocCommandRepository.assertNoDuplicateBodySlot(rows));
    }

    @Test
    void nullFields_areSkipped_noNpe() {
        BieForOasDoc incomplete = new BieForOasDoc(); // null resourceName/verb/messageBody
        List<BieForOasDoc> rows = java.util.Arrays.asList(
                incomplete,
                row("/orders", "POST", "Request", 1L),
                null);
        assertDoesNotThrow(() -> JooqBieForOasDocCommandRepository.assertNoDuplicateBodySlot(rows));
    }
}
