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
import org.oagi.score.gateway.http.api.oas_management.repository.BieForOasDocCommandRepository;
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
    private OasOperationProposer proposer;
    private ScoreUser requester;
    private RepositoryFactory repositoryFactory;
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
        repositoryFactory = mock(RepositoryFactory.class);
        command = mock(OasDocCommandRepository.class);
        OasDocQueryRepository query = mock(OasDocQueryRepository.class);
        ReflectionTestUtils.setField(applier, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(applier, "oasDocService", oasDocService);
        proposer = mock(OasOperationProposer.class);
        ReflectionTestUtils.setField(applier, "proposer", proposer);
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

    private static OasOperationEndpoint stored(long oasOperationId, String path, String verb, String operationId, boolean request, boolean response) {
        return new OasOperationEndpoint(BigInteger.valueOf(oasOperationId), path, verb, operationId, request, response);
    }

    @Test
    void saysAnOperationIdOfTwoEndpointsOfTheDocumentAsItIsEvenWhenNothingIsChanged() {
        when(command.findOperationEndpoints(any())).thenReturn(List.of(
                stored(1, "/a", "GET", "getA", false, true), stored(2, "/b", "GET", "getA", false, true),
                stored(3, "/c", "GET", "getC", false, true)));

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, new OasDocChangeSet(null, null, null), true));

        assertEquals(List.of(BigInteger.valueOf(1), BigInteger.valueOf(2)),
                e.violations().stream().map(OasDocViolation::oasOperationId).toList());
        assertTrue(e.violations().stream().allMatch(v -> "operationId".equals(v.field())));
    }

    @Test
    void letsTheRequestAndTheResponseOfOneEndpointShareAnOperationId() {
        when(command.findOperationEndpoints(any())).thenReturn(List.of(
                stored(1, "/a", "POST", "createA", true, false), stored(2, "/a", "POST", "createA", false, true)));

        assertDoesNotThrow(() -> applier.apply(requester, DOC, new OasDocChangeSet(null, null, null), true));
    }

    @Test
    void saysASecondBodyOfAnEndpointOfTheDocumentAsItIs() {
        when(command.findOperationEndpoints(any())).thenReturn(List.of(
                stored(1, "/a", "POST", "createA", true, false), stored(2, "/a", "POST", "createA", true, false)));

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, new OasDocChangeSet(null, null, null), true));

        assertEquals(2, e.violations().size());
        assertTrue(e.violations().stream().allMatch(v -> "messageBody".equals(v.field()) && "Request".equals(v.messageBody())));
    }

    private static OasOperationAddition added(String clientKey, String resourceName, String operationId, String messageBody) {
        return new OasOperationAddition(clientKey, null, null, "POST", messageBody, false, true, resourceName, operationId, null, null, null, null);
    }

    private void namesAre(String resourceName, String operationId) {
        when(proposer.names(any(), any(), any())).thenReturn(new OasOperationProposal(resourceName, operationId, List.of()));
    }

    @Test
    void addsTheOperationsTheUserAddedAndAnswersTheNamesByTheirKeys() {
        namesAre("/orders", "createOrder");

        OasDocChangeResult result = applier.apply(requester, DOC, new OasDocChangeSet(null, null, null,
                List.of(added("new-1", null, null, "Request")), null), true);

        verify(oasDocService).addOperationForOasDoc(any(), argThat(r ->
                r.getPath().equals("/orders") && r.getOperationId().equals("createOrder") && r.isOperationIdJudgedLater()));
        assertEquals(List.of(new OasDocChangeResult.AddedOperation("new-1", "createOrder", "/orders", "Request")), result.added());
        assertEquals(1, rollbacks);
    }

    @Test
    void saysAnOperationIdOfTwoAddedOperationsAtBothOfThem() {
        namesAre("/a", "queryOrder");
        when(command.findOasResourceId(any(), any())).thenReturn(new OasResourceId(BigInteger.ONE), new OasResourceId(BigInteger.TWO));
        when(command.findOasOperationId(any(), any())).thenReturn(new OasOperationId(BigInteger.valueOf(21)), new OasOperationId(BigInteger.valueOf(22)));
        when(command.findOperationEndpoints(any())).thenReturn(List.of(
                new OasOperationEndpoint(BigInteger.valueOf(21), "/a", "POST", "queryOrder", false, true),
                new OasOperationEndpoint(BigInteger.valueOf(22), "/b", "POST", "queryOrder", false, true)));

        OasDocViolationException e = assertThrows(OasDocViolationException.class, () -> applier.apply(requester, DOC,
                new OasDocChangeSet(null, null, null, List.of(added("k1", null, null, "Response"), added("k2", null, null, "Response")), null), true));

        assertEquals(List.of("k1", "k2"), e.violations().stream().map(OasDocViolation::clientKey).toList());
        assertTrue(e.violations().stream().allMatch(v -> "operationId".equals(v.field()) && v.oasOperationId() == null));
    }

    @Test
    void deletesTheOperationsTheUserRemovedBeforeAddingTheOnesTheUserAdded() {
        namesAre("/orders", "createOrder");

        applier.apply(requester, DOC, new OasDocChangeSet(null, null, null,
                List.of(added("k1", null, null, "Request")), List.of(row("getA"))), false);

        InOrder order = inOrder(oasDocService);
        order.verify(oasDocService).deleteBieForOasDoc(any(), any());
        order.verify(oasDocService).addOperationForOasDoc(any(), any());
        assertEquals(0, rollbacks);
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
    void aChangeThatCannotBeSaved_isAnsweredWithTheEntriesTheDocumentWouldHave_forTheRegisteredOnesFollowTheOperationsAnyway() {
        OasDocHttpHeader registered = pathParameter("ids");
        when(command.applyHeaderChanges(any(), any())).thenReturn(List.of(registered));
        when(command.findResolvedOperationPaths(any())).thenReturn(Map.of(new OasOperationId(OP), "/orders/{ids}"));
        when(command.findOperationEndpoints(any())).thenReturn(List.of(
                stored(1, "/a", "GET", "getA", false, true), stored(2, "/b", "GET", "getA", false, true)));

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(null, List.of(row("getA")), null), true));

        assertEquals(List.of(registered), e.httpHeaders());
        assertEquals(List.of(registered), OasDocChangeResult.invalid(e.violations(), e.derived(), e.added(), e.httpHeaders()).httpHeaders());
    }

    @Test
    void anOperationRuleThatIsBroken_stillLetsARegisteredEntryFollowTheOperationInACheck_butNotInASave() {
        BieForOasDocCommandRepository bieCommand = mock(BieForOasDocCommandRepository.class);
        when(repositoryFactory.bieForOasDocCommandRepository(any())).thenReturn(bieCommand);
        OasDocHttpHeader registered = pathParameter("ids");
        when(command.applyHeaderChanges(any(), any())).thenReturn(List.of(registered));
        when(oasDocService.updateDetails(any(), any()))
                .thenThrow(new OasDocViolationException(OasDocViolation.ofOperation(OP, "operationId", "used")));

        OasDocViolationException checked = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(null, List.of(row("getA")), null), true));
        verify(bieCommand).updateBieForOasDoc(any()); // the operations are written (and rolled back) so that the entries follow them
        assertEquals(List.of(registered), checked.httpHeaders());

        assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(null, List.of(row("getA")), null), false));
        verify(bieCommand, times(1)).updateBieForOasDoc(any()); // a save writes nothing of what is refused
    }

    @Test
    void anOperationRuleThatIsBroken_isNotHiddenByOperationsThatCannotBeWrittenEither() {
        BieForOasDocCommandRepository bieCommand = mock(BieForOasDocCommandRepository.class);
        when(repositoryFactory.bieForOasDocCommandRepository(any())).thenReturn(bieCommand);
        when(bieCommand.updateBieForOasDoc(any())).thenThrow(new IllegalStateException("two bodies"));
        OasDocViolation where = OasDocViolation.ofOperation(OP, "operationId", "used");
        when(oasDocService.updateDetails(any(), any())).thenThrow(new OasDocViolationException(where));

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(null, List.of(row("getA")), null), true));

        assertEquals(List.of(where), e.violations());
    }

    @Test
    void aChangeThatStopsAtTheDocument_hasNoEntriesToAnswer() {
        when(oasDocService.updateOasDoc(any(), any())).thenThrow(new IllegalArgumentException("The title is wrong."));

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(new OasDoc(), List.of(row("getA")), null), true));

        assertNull(e.httpHeaders()); // not "no entries": the page keeps its own
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
    }

    @Test
    void anOperationRuleWithoutAPlace_isAViolationOfTheOperations() {
        when(oasDocService.updateDetails(any(), any())).thenThrow(new IllegalArgumentException("nope"));

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(null, List.of(row("getA")), null), false));

        assertEquals(Scope.OPERATION, e.violations().get(0).scope());
    }

    @Test
    void anOperationWithoutAnOperationId_isAViolationOfTheRow() {
        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(new OasDoc(), List.of(row(" ")), null), false));

        assertEquals("operationId", e.violations().get(0).field());
        assertEquals(OP, e.violations().get(0).oasOperationId());
    }

    @Test
    void everyRowThatIsWrongIsSaidAtOnce() {
        BieForOasDoc blank = named(11, "Response", "GET", false, " ", "/a", "A");
        BieForOasDoc notAPath = named(12, "Response", "GET", false, "queryB", "b", "B");

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(new OasDoc(), List.of(blank, notAPath), null), true));

        assertEquals(List.of("operationId", "resourceName"), e.violations().stream().map(OasDocViolation::field).toList());
    }

    @Test
    void theViolationsOfTheRowsAndOfTheOperationsStepAreSaidTogether() {
        BieForOasDoc notAPath = named(11, "Response", "GET", false, "queryA", "a", "A");
        when(oasDocService.updateDetails(any(), any())).thenThrow(new OasDocViolationException(
                OasDocViolation.ofOperation(OP, "operationId", "used")));

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(null, List.of(notAPath), null), true));

        assertEquals(List.of("resourceName", "operationId"), e.violations().stream().map(OasDocViolation::field).toList());
    }

    @Test
    void whenTheDocumentCannotBeSaved_theStepsThatReferToItAreNotJudged() {
        when(oasDocService.updateOasDoc(any(), any())).thenThrow(new IllegalArgumentException("The title is wrong."));
        BieForOasDoc row = named(11, "Response", "GET", false, "queryA", "/a", "A");

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(new OasDoc(), List.of(row), null), true));

        assertEquals(List.of(Scope.DOCUMENT), e.violations().stream().map(OasDocViolation::scope).toList());
        verify(oasDocService, never()).updateDetails(any(), any());
        verify(command, never()).applyHeaderChanges(any(), any());
    }

    @Test
    void everyEntryThatIsWrongIsSaidAtOnce() {
        OasDocHttpHeader first = pathParameter("tru");
        OasDocHttpHeader second = pathParameter("also-not-in-the-path");
        when(command.applyHeaderChanges(any(), any())).thenReturn(List.of(first, second));
        when(command.findResolvedOperationPaths(any())).thenReturn(Map.of(new OasOperationId(OP), "/orders/{id}"));

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(null, null, null), false));

        assertEquals(2, e.violations().size());
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

    private static BieForOasDoc named(long oasOperationId, String messageBody, String verb, boolean array, String operationId,
                                      String resourceName, String propertyTerm, String... touched) {
        BieForOasDoc row = new BieForOasDoc();
        row.setOasOperationId(new OasOperationId(BigInteger.valueOf(oasOperationId)));
        row.setMessageBody(messageBody);
        row.setVerb(verb);
        row.setArrayIndicator(array);
        row.setOperationId(operationId);
        row.setResourceName(resourceName);
        row.setPropertyTerm(propertyTerm);
        row.setTouched(List.of(touched));
        return row;
    }

    @Test
    void aVerbThatWasChanged_makesTheOperationIdAndMovesAGetRequestBodyToTheResponse() {
        BieForOasDoc row = named(11, "Request", "GET", false, "createPurchaseOrder", "/po", "Purchase Order", "verb");

        OasDocChangeResult result = applier.apply(requester, DOC, changes(null, List.of(row), null), true);

        assertEquals("queryPurchaseOrder", row.getOperationId());
        assertEquals("Response", row.getMessageBody());
        assertEquals(List.of(new OasDocChangeResult.DerivedOperation(OP, "Request", "queryPurchaseOrder", "/po", "Response")),
                result.operations());
    }

    @Test
    void anArrayIndicatorThatWasChanged_makesTheOperationIdAndTheMarkerOfThePath() {
        BieForOasDoc row = named(11, "Response", "GET", true, "queryPurchaseOrder", "/bc/1.0/purchase-order", "Purchase Order",
                "arrayIndicator");

        OasDocChangeResult result = applier.apply(requester, DOC, changes(null, List.of(row), null), true);

        assertEquals("queryPurchaseOrderList", row.getOperationId());
        assertEquals("/bc/1.0/purchase-order-list", row.getResourceName());
        assertEquals("/bc/1.0/purchase-order-list", result.operations().get(0).resourceName());
    }

    @Test
    void aPathThatEndsInAPathParameter_isNotMadeAgainFromTheBie() {
        BieForOasDoc row = named(11, "Response", "GET", true, "queryContainer", "/containers/{id}", "Container", "arrayIndicator");

        applier.apply(requester, DOC, changes(null, List.of(row), null), true);

        assertEquals("/containers/{id}", row.getResourceName());
    }

    @Test
    void whatTheUserDidNotChange_isNotMadeAgain() {
        BieForOasDoc row = named(11, "Response", "GET", true, "myOwnName", "/my/own/path", "Purchase Order");

        OasDocChangeResult result = applier.apply(requester, DOC, changes(null, List.of(row), null), true);

        assertEquals("myOwnName", row.getOperationId());
        assertEquals("/my/own/path", row.getResourceName());
        assertTrue(result.operations().isEmpty());
    }

    @Test
    void theOtherRowOfTheOperation_getsTheSameNames() {
        BieForOasDoc request = named(11, "Request", "POST", false, "createOrder", "/orders", "Order");
        BieForOasDoc response = named(11, "Response", "POST", true, "createOrder", "/orders", "Order", "arrayIndicator");

        OasDocChangeResult result = applier.apply(requester, DOC, changes(null, List.of(request, response), null), true);

        assertEquals("createOrderList", request.getOperationId());
        assertEquals("/order-list", request.getResourceName());
        assertEquals(2, result.operations().size());
    }

    @Test
    void whatWasMadeOfTheNames_isAnsweredWhenTheChangeCannotBeSaved_forTheUserSeesItNextToWhatIsWrong() {
        BieForOasDoc row = named(11, "Response", "GET", true, "queryPurchaseOrder", "/po", "Purchase Order", "arrayIndicator");
        when(oasDocService.updateDetails(any(), any())).thenThrow(new OasDocViolationException(
                OasDocViolation.ofOperation(OP, "operationId", "used")));

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(null, List.of(row), null), true));

        assertEquals("queryPurchaseOrderList", e.derived().get(0).operationId());
    }

    @Test
    void aResourceNameThatIsNotAPath_isAViolationOfTheRow() {
        BieForOasDoc row = named(11, "Response", "GET", false, "queryOrder", "orders/{id", "Order");

        OasDocViolationException e = assertThrows(OasDocViolationException.class,
                () -> applier.apply(requester, DOC, changes(null, List.of(row), null), true));

        assertEquals("resourceName", e.violations().get(0).field());
        assertEquals(OP, e.violations().get(0).oasOperationId());
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
