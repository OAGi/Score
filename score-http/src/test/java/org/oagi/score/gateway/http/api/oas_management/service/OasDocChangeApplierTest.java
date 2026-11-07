package org.oagi.score.gateway.http.api.oas_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetOasDocResponse;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.UpdateBieForOasDocResponse;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.UpdateOasDocResponse;
import org.oagi.score.gateway.http.api.oas_management.model.*;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocViolation.Scope;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * A change set is applied document first, then operations, then the entries; a rule that is broken is a violation of the
 * part that breaks it, and a check keeps nothing.
 */
class OasDocChangeApplierTest {

    private static final OasDocId DOC = new OasDocId(BigInteger.valueOf(1));
    private static final BigInteger OP = BigInteger.valueOf(11);

    private OasDocChangeApplier applier;
    private OpenAPIDocService oasDocService;
    private OasDocCommandRepository command;
    private ScoreUser requester;
    private int rollbacks;

    @BeforeEach
    void setUp() {
        rollbacks = 0;
        applier = new OasDocChangeApplier() {
            @Override
            protected void markRollbackOnly() {
                rollbacks++;
            }
        };
        oasDocService = mock(OpenAPIDocService.class);
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        command = mock(OasDocCommandRepository.class);
        OasDocQueryRepository query = mock(OasDocQueryRepository.class);
        ReflectionTestUtils.setField(applier, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(applier, "oasDocService", oasDocService);
        requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
                "tester@example.com", true, List.of(ScoreRole.DEVELOPER));
        when(repositoryFactory.oasDocCommandRepository(any())).thenReturn(command);
        when(repositoryFactory.oasDocQueryRepository(any())).thenReturn(query);
        OasDoc saved = new OasDoc();
        saved.setOpenAPIVersion("3.1");
        when(query.getOasDoc(any())).thenReturn(new GetOasDocResponse(saved));
        when(oasDocService.updateOasDoc(any(), any())).thenReturn(new UpdateOasDocResponse(DOC.value(), true));
        when(oasDocService.updateDetails(any(), any())).thenReturn(new UpdateBieForOasDocResponse(DOC, true));
        when(command.applyHeaderChanges(any(), any())).thenReturn(List.of());
        when(command.findResolvedOperationPaths(any())).thenReturn(Map.of());
    }

    private static BieForOasDoc row(String operationId) {
        BieForOasDoc row = new BieForOasDoc();
        row.setOasOperationId(new OasOperationId(OP));
        row.setMessageBody("Request");
        row.setOperationId(operationId);
        return row;
    }

    private static OasDocHttpHeader pathParameter(String name) {
        OasDocHttpHeader header = new OasDocHttpHeader();
        header.setOasOperationId(OP);
        header.setDirection(OasDocHttpHeader.Direction.REQUEST);
        header.setIn("path");
        header.setName(name);
        header.setRequired(true);
        header.setSchema(OasSchema.fromJson("{\"type\":\"string\"}"));
        return header;
    }

    private OasDocChangeSet changes(OasDoc document, List<BieForOasDoc> operations, OasDocChangeSet.HeaderChanges headers) {
        return new OasDocChangeSet(document, operations, headers);
    }

    @Test
    void appliesTheDocumentThenTheOperationsThenTheEntries() {
        OasDoc document = new OasDoc();
        document.setTitle("t");

        applier.apply(requester, DOC, changes(document, List.of(row("getA")), OasDocChangeSet.HeaderChanges.NONE), false);

        InOrder order = inOrder(oasDocService, command);
        order.verify(oasDocService).updateOasDoc(any(), any());
        order.verify(oasDocService).updateDetails(any(), any());
        order.verify(command).applyHeaderChanges(any(), any());
        assertEquals(0, rollbacks);
    }

    @Test
    void theEntriesOfTheDocumentFormAreNotSaved_theyAreSavedFromWhatWasAddedChangedAndDeleted() {
        OasDoc document = new OasDoc();
        document.setHttpHeaders(List.of(pathParameter("stale")));

        applier.apply(requester, DOC, changes(document, null, null), false);

        verify(oasDocService).updateOasDoc(any(), org.mockito.ArgumentMatchers.argThat(r -> r.getHttpHeaders() == null));
    }

    @Test
    void leavesOutTheStepsThatHaveNothingToDo() {
        applier.apply(requester, DOC, changes(null, null, null), false);

        verify(oasDocService, never()).updateOasDoc(any(), any());
        verify(oasDocService, never()).updateDetails(any(), any());
        verify(command).applyHeaderChanges(any(), any());
    }

    @Test
    void aCheckKeepsNothingAndAnswersWithTheEntriesTheDocumentWouldHave() {
        OasDocHttpHeader registered = pathParameter("id");
        when(command.applyHeaderChanges(any(), any())).thenReturn(List.of(registered));
        when(command.findResolvedOperationPaths(any())).thenReturn(Map.of(new OasOperationId(OP), "/orders/{id}"));

        OasDocChangeResult result = applier.apply(requester, DOC, changes(null, List.of(row("getA")), null), true);

        assertTrue(result.valid());
        assertEquals(List.of(registered), result.httpHeaders());
        assertEquals(1, rollbacks);
    }

    @Test
    void aRuleOfTheDocumentThatIsBroken_isAViolationOfTheDocument() {
        when(oasDocService.updateOasDoc(any(), any())).thenThrow(new IllegalArgumentException("The title is wrong."));

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(new OasDoc(), List.of(row("getA")), null), false));

        assertEquals(Scope.DOCUMENT, e.violations().get(0).scope());
        assertEquals("The title is wrong.", e.violations().get(0).message());
        verify(oasDocService, never()).updateDetails(any(), any());
    }

    @Test
    void aRuleOfTheOperationsThatIsBroken_keepsWhereItIs() {
        OasDocViolation where = OasDocViolation.ofOperation(OP, "operationId", "used");
        when(oasDocService.updateDetails(any(), any())).thenThrow(new OasDocViolationException(where));

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(null, List.of(row("getA")), null), true));

        assertEquals(List.of(where), e.violations());
        verify(command, never()).applyHeaderChanges(any(), any());
    }

    @Test
    void anOperationRuleWithoutAPlace_isAViolationOfTheOperations() {
        when(oasDocService.updateDetails(any(), any())).thenThrow(new IllegalArgumentException("nope"));

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(null, List.of(row("getA")), null), false));

        assertEquals(Scope.OPERATION, e.violations().get(0).scope());
    }

    @Test
    void anOperationWithoutAnOperationId_isRefusedBeforeAnythingIsSaved() {
        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(new OasDoc(), List.of(row(" ")), null), false));

        assertEquals("operationId", e.violations().get(0).field());
        assertEquals(OP, e.violations().get(0).oasOperationId());
        verifyNoInteractions(oasDocService);
    }

    @Test
    void aPathParameterThatThePathOfItsOperationDoesNotHave_isAViolationOfTheEntry() {
        when(command.applyHeaderChanges(any(), any())).thenReturn(List.of(pathParameter("tru")));
        when(command.findResolvedOperationPaths(any())).thenReturn(Map.of(new OasOperationId(OP), "/orders/{id}"));

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(null, null, null), false));

        assertEquals(Scope.HEADER, e.violations().get(0).scope());
        assertEquals(OP, e.violations().get(0).oasOperationId());
        assertTrue(e.violations().get(0).message().contains("tru"));
    }

    @Test
    void aPathParameterOfTheVersionOfThePath_isInThePath() {
        when(command.applyHeaderChanges(any(), any())).thenReturn(List.of(pathParameter("id")));
        when(command.findResolvedOperationPaths(any())).thenReturn(Map.of(new OasOperationId(OP), "/3.1/orders/{id}"));

        assertTrue(applier.apply(requester, DOC, changes(null, null, null), false).valid());
    }

    @Test
    void theFacade_turnsAViolationIntoAnAnswerForACheck_andPassesItOnForASave() {
        OasDocChangeService service = new OasDocChangeService();
        OasDocChangeApplier mocked = mock(OasDocChangeApplier.class);
        ReflectionTestUtils.setField(service, "applier", mocked);
        OasDocViolation violation = OasDocViolation.of(Scope.DOCUMENT, "bad");
        OasDocChangeSet changeSet = changes(null, null, null);
        when(mocked.apply(any(), any(), any(), eq(true))).thenThrow(new OasDocViolationException(violation));
        when(mocked.apply(any(), any(), any(), eq(false))).thenThrow(new OasDocViolationException(violation));

        OasDocChangeResult checked = service.check(requester, DOC, changeSet);

        assertFalse(checked.valid());
        assertEquals(List.of(violation), checked.violations());
        assertThrows(OasDocViolationException.class, () -> service.save(requester, DOC, changeSet));
    }
}
