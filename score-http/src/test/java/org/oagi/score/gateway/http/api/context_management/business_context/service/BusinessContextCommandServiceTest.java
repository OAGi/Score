package org.oagi.score.gateway.http.api.context_management.business_context.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.repository.TopLevelAsbiepQueryRepository;
import org.oagi.score.gateway.http.api.context_management.business_context.controller.payload.CreateBusinessContextRequest;
import org.oagi.score.gateway.http.api.context_management.business_context.controller.payload.UpdateBusinessContextRequest;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextCommandRepository;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Guards added for the Context Management audit: blank-name validation (B9), in-use discard guard (B4),
 * idempotent assign (B6), and the last-business-context unassign guard (B5). Repositories are mocked
 * (no DB); each test asserts the guard's effect on the command repository.
 */
class BusinessContextCommandServiceTest {

    private BusinessContextCommandService service;
    private RepositoryFactory repositoryFactory;
    private BusinessContextCommandRepository command;
    private BusinessContextQueryRepository query;

    private static final BusinessContextId BC_ID = new BusinessContextId(BigInteger.valueOf(5));
    private static final TopLevelAsbiepId TLA_ID = new TopLevelAsbiepId(BigInteger.valueOf(1001));

    private ScoreUser requester;

    @BeforeEach
    void setUp() {
        service = new BusinessContextCommandService();
        repositoryFactory = mock(RepositoryFactory.class);
        command = mock(BusinessContextCommandRepository.class);
        query = mock(BusinessContextQueryRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
                "tester@example.com", true, List.of(ScoreRole.DEVELOPER));

        when(repositoryFactory.businessContextCommandRepository(any())).thenReturn(command);
        when(repositoryFactory.businessContextQueryRepository(any())).thenReturn(query);

        // Issue #1312 assign/unassign owner+WIP guard: make the requester the owner so these
        // assignment-logic tests exercise their original concern (idempotency / last-context).
        TopLevelAsbiepQueryRepository topLevelAsbiepQuery = mock(TopLevelAsbiepQueryRepository.class);
        UserSummaryRecord owner = new UserSummaryRecord(requester.userId(), "tester", "Tester",
                List.of(ScoreRole.DEVELOPER));
        TopLevelAsbiepSummaryRecord ownedByRequester = new TopLevelAsbiepSummaryRecord(
                null, null, TLA_ID, null, null, null,
                "den", "propertyTerm", "displayName", "1.0", "status",
                BieState.WIP, false, false, owner, null, null);
        lenient().when(repositoryFactory.topLevelAsbiepQueryRepository(any())).thenReturn(topLevelAsbiepQuery);
        lenient().when(topLevelAsbiepQuery.getTopLevelAsbiepSummary(any())).thenReturn(ownedByRequester);
    }

    // ----- name validation (B9) -----

    @Test
    void create_rejects_a_blank_name() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create(requester, new CreateBusinessContextRequest("   ", List.of())));
        verify(command, never()).create(any());
    }

    @Test
    void update_rejects_a_blank_name() {
        assertThrows(IllegalArgumentException.class, () -> service.update(requester,
                new UpdateBusinessContextRequest(BC_ID, "   ", List.of())));
        verify(command, never()).update(any(), any());
    }

    // ----- discard in-use guard (B4) -----

    @Test
    void discard_rejects_a_business_context_assigned_to_a_bie() {
        when(query.isBusinessContextUsed(BC_ID)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.discard(requester, BC_ID));
        verify(command, never()).delete(any());
    }

    @Test
    void bulk_discard_skips_used_business_contexts_and_deletes_the_rest() {
        BusinessContextId used = new BusinessContextId(BigInteger.valueOf(6));
        when(query.isBusinessContextUsed(BC_ID)).thenReturn(false);
        when(query.isBusinessContextUsed(used)).thenReturn(true);
        when(command.delete(any())).thenReturn(1);

        service.discard(requester, List.of(BC_ID, used));

        verify(command).delete(List.of(BC_ID));
    }

    @Test
    void bulk_discard_rejects_when_all_are_in_use() {
        when(query.isBusinessContextUsed(any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.discard(requester, List.of(BC_ID)));
        verify(command, never()).delete(any());
    }

    // ----- assign idempotency (B6) -----

    @Test
    void assign_is_a_no_op_when_the_assignment_already_exists() {
        when(query.isBusinessContextAssigned(BC_ID, TLA_ID)).thenReturn(true);

        service.assignBusinessContext(requester, BC_ID, TLA_ID);

        verify(command, never()).createAssignment(any(), any());
    }

    @Test
    void assign_creates_the_assignment_when_it_does_not_exist() {
        when(query.isBusinessContextAssigned(BC_ID, TLA_ID)).thenReturn(false);

        service.assignBusinessContext(requester, BC_ID, TLA_ID);

        verify(command).createAssignment(BC_ID, TLA_ID);
    }

    // ----- last-context unassign guard (B5) -----

    @Test
    void unassign_rejects_removing_the_last_business_context() {
        when(query.countAssignments(TLA_ID)).thenReturn(1);

        assertThrows(IllegalArgumentException.class,
                () -> service.unassignBusinessContext(requester, BC_ID, TLA_ID));
        verify(command, never()).deleteAssignment(any(), any());
    }

    @Test
    void unassign_removes_a_non_last_business_context() {
        when(query.countAssignments(TLA_ID)).thenReturn(2);

        service.unassignBusinessContext(requester, BC_ID, TLA_ID);

        verify(command).deleteAssignment(BC_ID, TLA_ID);
    }
}
