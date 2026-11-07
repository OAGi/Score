package org.oagi.score.gateway.http.api.oas_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.UpdateBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.UpdateBieForOasDocResponse;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocViolation;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocViolationException;
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;
import org.oagi.score.gateway.http.api.oas_management.repository.BieForOasDocCommandRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The inline edit of the Endpoint Details grid ({@code updateDetails}) must not leave two operations of a
 * document with the same operationId. The comparison is exact: operationId is case-sensitive.
 */
class OpenAPIDocServiceOperationIdUpdateTest {

    private static final OasDocId OAS_DOC_ID = new OasDocId(BigInteger.valueOf(1));
    private static final OasOperationId OP_1 = new OasOperationId(BigInteger.valueOf(11));
    private static final OasOperationId OP_2 = new OasOperationId(BigInteger.valueOf(12));

    private OpenAPIDocService service;
    private OasDocCommandRepository command;
    private BieForOasDocCommandRepository bieForOasDocCommand;
    private ScoreUser requester;

    @BeforeEach
    void setUp() {
        service = new OpenAPIDocService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        command = mock(OasDocCommandRepository.class);
        bieForOasDocCommand = mock(BieForOasDocCommandRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
                "tester@example.com", true, List.of(ScoreRole.DEVELOPER));
        when(repositoryFactory.oasDocCommandRepository(any())).thenReturn(command);
        when(repositoryFactory.bieForOasDocCommandRepository(any())).thenReturn(bieForOasDocCommand);
        when(command.findOperationIdsInOasDoc(any(), any(), any())).thenReturn(Set.of());
        when(bieForOasDocCommand.updateBieForOasDoc(any()))
                .thenReturn(new UpdateBieForOasDocResponse(OAS_DOC_ID, true));
    }

    private static BieForOasDoc row(OasOperationId oasOperationId, String operationId, String messageBody,
                                    String resourceName, String verb) {
        BieForOasDoc row = row(oasOperationId, operationId, messageBody);
        row.setResourceName(resourceName);
        row.setVerb(verb);
        return row;
    }

    private static BieForOasDoc row(OasOperationId oasOperationId, String operationId, String messageBody) {
        BieForOasDoc row = new BieForOasDoc();
        row.setOasOperationId(oasOperationId);
        row.setOperationId(operationId);
        row.setMessageBody(messageBody);
        return row;
    }

    private UpdateBieForOasDocRequest request(BieForOasDoc... rows) {
        UpdateBieForOasDocRequest request = new UpdateBieForOasDocRequest(requester);
        request.setOasDocId(OAS_DOC_ID);
        request.setBieForOasDocList(List.of(rows));
        return request;
    }

    @Test
    void operationIdOfAnotherOperationOfTheDocument_isRejected() {
        when(command.findOperationIdsInOasDoc(OAS_DOC_ID, Set.of("getOrder"), Set.of(OP_1)))
                .thenReturn(Set.of("getOrder"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateDetails(requester, request(row(OP_1, "getOrder", "Response"))));
        assertTrue(ex.getMessage().contains("Operation ID must be unique within the document."), ex.getMessage());
        verify(bieForOasDocCommand, never()).updateBieForOasDoc(any());
    }

    @Test
    void sameOperationIdOnTwoSubmittedOperations_isRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateDetails(requester, request(
                        row(OP_1, "getOrder", "Response"), row(OP_2, "getOrder", "Response"))));
        verify(bieForOasDocCommand, never()).updateBieForOasDoc(any());
    }

    @Test
    void requestAndResponseRowsOfOneOperation_areNotADuplicate() {
        UpdateBieForOasDocResponse response = service.updateDetails(requester, request(
                row(OP_1, "createOrder", "Request"), row(OP_1, "createOrder", "Response")));

        assertEquals(OAS_DOC_ID, response.getOasDocId());
    }

    @Test
    void operationIdsDifferingOnlyInCase_areNotADuplicate() {
        UpdateBieForOasDocResponse response = service.updateDetails(requester, request(
                row(OP_1, "getOrder", "Response"), row(OP_2, "GetOrder", "Response")));

        assertEquals(OAS_DOC_ID, response.getOasDocId());
    }

    @Test
    void submittedOperationsAreExcludedFromTheLookup_soTheirIdsCanBeSwapped() {
        service.updateDetails(requester, request(
                row(OP_1, "b", "Response"), row(OP_2, "a", "Response")));

        verify(command).findOperationIdsInOasDoc(OAS_DOC_ID, Set.of("a", "b"), Set.of(OP_1, OP_2));
    }

    @Test
    void requestAndResponseStoredAsTwoOperationsOfOneEndpoint_shareTheirOperationId() {
        UpdateBieForOasDocResponse response = service.updateDetails(requester, request(
                row(OP_1, "createOrder", "Request", "/orders", "POST"),
                row(OP_2, "createOrder", "Response", "/orders", "POST")));

        assertEquals(OAS_DOC_ID, response.getOasDocId());
    }

    @Test
    void sameOperationIdOnTwoEndpoints_isRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateDetails(requester, request(
                        row(OP_1, "createOrder", "Request", "/orders", "POST"),
                        row(OP_2, "createOrder", "Response", "/orders", "PUT"))));
        verify(bieForOasDocCommand, never()).updateBieForOasDoc(any());
    }

    @Test
    void operationsOnTheEndpointThatAreNotSubmitted_areLeftOutOfTheLookup() {
        OasOperationId twin = new OasOperationId(BigInteger.valueOf(13));
        when(command.findOasOperationIdsAtEndpoint(OAS_DOC_ID, "/orders", "POST")).thenReturn(Set.of(OP_1, twin));

        service.updateDetails(requester, request(row(OP_1, "createOrder", "Request", "/orders", "POST")));

        verify(command).findOperationIdsInOasDoc(OAS_DOC_ID, Set.of("createOrder"), Set.of(OP_1, twin));
    }

    @Test
    void anOperationIdOnTwoEndpoints_saysWhichOperationsHaveIt() {
        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> service.updateDetails(requester, request(
                        row(OP_1, "createOrder", "Request", "/orders", "POST"),
                        row(OP_2, "createOrder", "Response", "/orders", "PUT"))));

        assertEquals(List.of(OP_1.value(), OP_2.value()),
                e.violations().stream().map(OasDocViolation::oasOperationId).toList());
        assertTrue(e.violations().stream().allMatch(v -> "operationId".equals(v.field())));
    }

    @Test
    void aBodyThatAnOperationOfAnotherPageAlreadyHas_isRefusedAtTheRow() {
        OasOperationId otherPage = new OasOperationId(BigInteger.valueOf(99));
        when(command.findOperationsWithBodyAtEndpoint(OAS_DOC_ID, "/orders", "POST", true)).thenReturn(Set.of(otherPage));

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> service.updateDetails(requester, request(row(OP_1, "createOrder", "Request", "/orders", "POST"))));

        assertEquals(OP_1.value(), e.violations().get(0).oasOperationId());
        assertEquals("messageBody", e.violations().get(0).field());
        verify(bieForOasDocCommand, never()).updateBieForOasDoc(any());
    }

    @Test
    void theBodyOfTheOperationThatIsBeingChanged_isNotAConflict() {
        when(command.findOperationsWithBodyAtEndpoint(OAS_DOC_ID, "/orders", "POST", true)).thenReturn(Set.of(OP_1));

        service.updateDetails(requester, request(row(OP_1, "createOrder", "Request", "/orders", "POST")));

        verify(bieForOasDocCommand).updateBieForOasDoc(any());
    }

    @Test
    void everyOperationIdThatIsUsedTwice_isSaidAtOnce() {
        OasOperationId op3 = new OasOperationId(BigInteger.valueOf(13));
        OasOperationId op4 = new OasOperationId(BigInteger.valueOf(14));

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> service.updateDetails(requester, request(
                        row(OP_1, "a", "Response", "/a", "GET"), row(OP_2, "a", "Response", "/b", "GET"),
                        row(op3, "b", "Response", "/c", "GET"), row(op4, "b", "Response", "/d", "GET"))));

        assertEquals(List.of(OP_1.value(), OP_2.value(), op3.value(), op4.value()),
                e.violations().stream().map(OasDocViolation::oasOperationId).toList());
    }

    @Test
    void aSecondBodyOnTheSameEndpointAmongTheRows_isRefusedAtTheSecondRow() {
        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> service.updateDetails(requester, request(
                        row(OP_1, "a", "Response", "/orders", "GET"), row(OP_2, "a", "Response", "/orders", "GET"))));

        assertEquals(List.of(OP_2.value()), e.violations().stream().filter(v -> "messageBody".equals(v.field()))
                .map(OasDocViolation::oasOperationId).toList());
    }
}
