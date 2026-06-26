package org.oagi.score.gateway.http.api.oas_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.AddBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.AddBieForOasDocResponse;
import org.oagi.score.gateway.http.api.oas_management.model.*;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.criteria.InsertOasOperationArguments;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Issue #1492 (Option 2): the Add-BIE service path now find-or-creates the (oasDocId, path) resource
 * and the (resourceId, verb) operation, then rejects a 2nd body of an already-present type. These
 * tests mock the command repository (no DB) and assert the wiring + the dup-body 400.
 */
class OpenAPIDocServiceAddBieTest {

    private OpenAPIDocService service;
    private RepositoryFactory repositoryFactory;
    private OasDocCommandRepository command;

    private static final OasDocId OAS_DOC_ID = new OasDocId(BigInteger.valueOf(1));
    private static final OasResourceId RESOURCE_ID = new OasResourceId(BigInteger.valueOf(100));
    private static final OasOperationId OPERATION_ID = new OasOperationId(BigInteger.valueOf(200));

    private ScoreUser requester;

    @BeforeEach
    void setUp() {
        service = new OpenAPIDocService();
        repositoryFactory = mock(RepositoryFactory.class);
        command = mock(OasDocCommandRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
                "tester@example.com", true, List.of(ScoreRole.DEVELOPER));

        when(repositoryFactory.oasDocCommandRepository(any())).thenReturn(command);
        when(command.insertOasMessageBody(any())).thenReturn(new OasMessageBodyId(BigInteger.valueOf(300)));
        when(command.findOrCreateOasResource(any())).thenReturn(RESOURCE_ID);
        when(command.findOrCreateOasOperation(any())).thenReturn(OPERATION_ID);
        when(command.insertOasRequest(any())).thenReturn(new OasRequestId(BigInteger.valueOf(400)));
        when(command.insertOasResponse(any())).thenReturn(new OasResponseId(BigInteger.valueOf(500)));
    }

    private AddBieForOasDocRequest addRequest(boolean isOasRequest) {
        AddBieForOasDocRequest request = new AddBieForOasDocRequest(requester);
        request.setOasDocId(OAS_DOC_ID);
        request.setTopLevelAsbiepId(new TopLevelAsbiepId(BigInteger.valueOf(7)));
        request.setPath("/orders");
        request.setVerb("POST");
        request.setOperationId("createOrder");
        request.setOasRequest(isOasRequest);
        return request;
    }

    @Test
    void secondAddOfDifferentBodyType_reusesTheSameOperation() {
        // Operation does not exist yet for the (resource, verb) -> first add creates Request.
        when(command.findOasOperationId(any(), eq("POST"))).thenReturn(null);
        when(command.operationHasBody(any(), anyBoolean())).thenReturn(false);

        AddBieForOasDocResponse first = service.addBieForOasDoc(requester, addRequest(true));
        assertEquals(BigInteger.valueOf(400), first.getOasRequestId().value());

        // Second add (Response) on the SAME (path, verb): find-or-create returns the SAME operation id,
        // and the Response is attached to that operation (no second operation minted).
        ArgumentCaptor<InsertOasOperationArguments> opCaptor = ArgumentCaptor.forClass(InsertOasOperationArguments.class);
        AddBieForOasDocResponse second = service.addBieForOasDoc(requester, addRequest(false));
        assertEquals(BigInteger.valueOf(500), second.getOasResponseId().value());

        verify(command, org.mockito.Mockito.times(2)).findOrCreateOasOperation(opCaptor.capture());
        // Both adds resolved the same (resourceId, verb) -> the find-or-create returns the shared op.
        for (InsertOasOperationArguments args : opCaptor.getAllValues()) {
            assertEquals("POST", args.getVerb());
        }
        // The Response insert was attached to the shared operation id (200).
        verify(command).insertOasResponse(any());
    }

    @Test
    void existingOperationIsNotRetagged_onSecondAdd() {
        // The operation already exists for (resource, verb) -> tag must NOT be inserted again.
        when(command.findOasOperationId(any(), eq("POST"))).thenReturn(OPERATION_ID);
        when(command.operationHasBody(any(), anyBoolean())).thenReturn(false);

        AddBieForOasDocRequest request = addRequest(false);
        request.setTagName("orders-tag");
        service.addBieForOasDoc(requester, request);

        verify(command, never()).insertOasTag(any());
        verify(command, never()).insertOasResourceTag(any());
    }

    @Test
    void secondRequestBodyOnSameOperation_isRejectedWith400Message() {
        when(command.findOasOperationId(any(), eq("POST"))).thenReturn(OPERATION_ID);
        // The operation already has a Request body -> adding another Request must be rejected.
        when(command.operationHasBody(OPERATION_ID, true)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.addBieForOasDoc(requester, addRequest(true)));
        assertTrue(ex.getMessage().contains("POST /orders"), ex.getMessage());
        assertTrue(ex.getMessage().contains("Request"), ex.getMessage());
        assertTrue(ex.getMessage().contains("at most one Request and one Response body"), ex.getMessage());

        // The body insert must NOT have happened.
        verify(command, never()).insertOasRequest(any());
    }

    @Test
    void secondResponseBodyOnSameOperation_isRejectedWith400Message() {
        when(command.findOasOperationId(any(), eq("POST"))).thenReturn(OPERATION_ID);
        when(command.operationHasBody(OPERATION_ID, false)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.addBieForOasDoc(requester, addRequest(false)));
        assertTrue(ex.getMessage().contains("POST /orders"), ex.getMessage());
        assertTrue(ex.getMessage().contains("Response"), ex.getMessage());

        verify(command, never()).insertOasResponse(any());
    }
}
