package org.oagi.score.gateway.http.api.oas_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.service.BusinessContextQueryService;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetOasDocResponse;
import org.oagi.score.gateway.http.api.oas_management.model.*;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** What the server makes of an operation that is about to be added, and whether it can be added. */
class OasOperationProposerTest {

    private static final OasDocId DOC = new OasDocId(BigInteger.valueOf(1));
    private static final TopLevelAsbiepId BIE = new TopLevelAsbiepId(BigInteger.valueOf(5));

    private OasOperationProposer proposer;
    private OasDocCommandRepository command;
    private ScoreUser requester;

    @BeforeEach
    void setUp() {
        proposer = new OasOperationProposer();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        command = mock(OasDocCommandRepository.class);
        OasDocQueryRepository query = mock(OasDocQueryRepository.class);
        BusinessContextQueryService contexts = mock(BusinessContextQueryService.class);
        ReflectionTestUtils.setField(proposer, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(proposer, "businessContextQueryService", contexts);
        requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
                "tester@example.com", true, List.of(ScoreRole.DEVELOPER));
        when(repositoryFactory.oasDocCommandRepository(any())).thenReturn(command);
        when(repositoryFactory.oasDocQueryRepository(any())).thenReturn(query);
        OasDoc doc = new OasDoc();
        doc.setVersion("1.0");
        when(query.getOasDoc(any())).thenReturn(new GetOasDocResponse(doc));
        BusinessContextSummaryRecord context = mock(BusinessContextSummaryRecord.class);
        when(context.name()).thenReturn("My Company");
        when(contexts.getBusinessContextSummaryList(any(), eq(BIE), any())).thenReturn(List.of(context));
        when(command.findOperationsWithBodyAtEndpoint(any(), any(), any(), eq(false))).thenReturn(Set.of());
        when(command.findOasOperationIdsAtEndpoint(any(), any(), any())).thenReturn(Set.of());
        when(command.findOperationIdsInOasDoc(any(), any(), any())).thenReturn(Set.of());
    }

    private static OasOperationCandidate bie(String verb, String messageBody, boolean array) {
        return new OasOperationCandidate(BIE, "Purchase Order", verb, messageBody, array, null, null);
    }

    @Test
    void makesTheResourceNameAndTheOperationIdOfABie() {
        OasOperationProposal proposal = proposer.propose(requester, DOC, bie("GET", "Response", true));

        assertEquals("/my-company/1.0/purchase-order-list", proposal.resourceName());
        assertEquals("queryPurchaseOrderList", proposal.operationId());
        assertTrue(proposal.valid());
    }

    @Test
    void takesWhatTheUserGave() {
        OasOperationProposal proposal = proposer.propose(requester, DOC,
                new OasOperationCandidate(null, null, "DELETE", "Response", false, "/orders/{id}", "removeOrder"));

        assertEquals("/orders/{id}", proposal.resourceName());
        assertEquals("removeOrder", proposal.operationId());
        assertTrue(proposal.valid());
    }

    @Test
    void makesTheOperationIdOfAnOperationWithoutABieFromItsPath() {
        OasOperationProposal proposal = proposer.propose(requester, DOC,
                new OasOperationCandidate(null, null, "PATCH", "Request", false, "/production-order/{id}", null));

        assertEquals("updateProductionOrder", proposal.operationId());
    }

    @Test
    void aGetHasNoRequestBody() {
        OasOperationProposal proposal = proposer.propose(requester, DOC, bie("GET", "Request", false));

        assertEquals("messageBody", proposal.violations().get(0).field());
    }

    @Test
    void aBodyTheEndpointAlreadyHas_isAViolation() {
        when(command.findOperationsWithBodyAtEndpoint(any(), eq("/my-company/1.0/purchase-order"), eq("GET"), eq(false)))
                .thenReturn(Set.of(new OasOperationId(BigInteger.TEN)));

        OasOperationProposal proposal = proposer.propose(requester, DOC, bie("GET", "Response", false));

        assertFalse(proposal.valid());
        assertEquals("messageBody", proposal.violations().get(0).field());
    }

    @Test
    void anOperationIdOfAnotherEndpoint_isAViolation() {
        when(command.findOperationIdsInOasDoc(any(), eq(Set.of("queryPurchaseOrder")), any())).thenReturn(Set.of("queryPurchaseOrder"));

        OasOperationProposal proposal = proposer.propose(requester, DOC, bie("GET", "Response", false));

        assertEquals("operationId", proposal.violations().get(0).field());
    }

    @Test
    void aResourceNameThatIsNotAPath_isAViolationOfItsField() {
        OasOperationProposal proposal = proposer.propose(requester, DOC,
                new OasOperationCandidate(null, null, "GET", "Response", false, "orders", "getOrders"));

        assertEquals("resourceName", proposal.violations().get(0).field());
    }

    @Test
    void whatIsNeitherGivenNorMadeIsRequired() {
        OasOperationProposal proposal = proposer.propose(requester, DOC,
                new OasOperationCandidate(null, null, "BOGUS", "Response", false, null, null));

        assertEquals(List.of("resourceName", "operationId"), proposal.violations().stream().map(OasDocViolation::field).toList());
    }

    private static OasOperationCandidate operation(String resourceName, String operationId, String messageBody) {
        return new OasOperationCandidate(null, null, "GET", messageBody, false, resourceName, operationId);
    }

    @Test
    void anOperationIdThatTwoEndpointsOfTheSameAddWouldHave_isAViolationOfTheLaterOne() {
        List<OasOperationProposal> proposals = proposer.proposeAll(requester, DOC,
                List.of(operation("/a/orders", "queryOrder", "Response"), operation("/b/orders", "queryOrder", "Response")));

        assertTrue(proposals.get(0).valid());
        assertEquals(List.of("Operation ID must be unique within the document."),
                proposals.get(1).violations().stream().map(OasDocViolation::message).toList());
        assertEquals("operationId", proposals.get(1).violations().get(0).field());
    }

    @Test
    void theRequestAndTheResponseOfOneEndpointOfTheSameAddMayShareAnOperationId() {
        List<OasOperationProposal> proposals = proposer.proposeAll(requester, DOC, List.of(
                new OasOperationCandidate(null, null, "POST", "Request", false, "/orders", "createOrder"),
                new OasOperationCandidate(null, null, "POST", "Response", false, "/orders", "createOrder")));

        assertTrue(proposals.get(0).valid());
        assertTrue(proposals.get(1).valid());
    }

    @Test
    void aSecondBodyOfOneEndpointOfTheSameAdd_isAViolationOfTheLaterOne() {
        List<OasOperationProposal> proposals = proposer.proposeAll(requester, DOC,
                List.of(operation("/orders", "queryOrder", "Response"), operation("/orders", "queryOrder", "Response")));

        assertTrue(proposals.get(0).valid());
        assertEquals(List.of("This endpoint already has a Response body."),
                proposals.get(1).violations().stream().map(OasDocViolation::message).toList());
    }
}
