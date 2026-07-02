package org.oagi.score.gateway.http.api.context_management.business_context.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.repository.TopLevelAsbiepQueryRepository;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextCommandRepository;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Issue #1312: a non-owner may open a WIP BIE read-only, so the business-context assign/unassign
 * endpoints must reject a non-owner writing a WIP BIE server-side (the same owner+WIP guard used by
 * the detail-save, state-transition, and business-term paths). Scoped to WIP: QA/Production behavior
 * is unchanged.
 */
@ExtendWith(MockitoExtension.class)
class BusinessContextViewOnlyGuardTest {

    private static final UserId OWNER = new UserId(BigInteger.valueOf(1));
    private static final UserId OTHER = new UserId(BigInteger.valueOf(2));
    private static final BusinessContextId BIZ_CTX = new BusinessContextId(BigInteger.valueOf(10));
    private static final TopLevelAsbiepId BIE = new TopLevelAsbiepId(BigInteger.valueOf(100));

    @Mock
    private RepositoryFactory repositoryFactory;

    @InjectMocks
    private BusinessContextCommandService service;

    private static ScoreUser user(UserId userId, ScoreRole... roles) {
        return new ScoreUser(userId, "u" + userId.value(), "name", "e@x.com", true, List.of(roles));
    }

    private static TopLevelAsbiepSummaryRecord bie(UserId ownerId, BieState state) {
        UserSummaryRecord owner = new UserSummaryRecord(ownerId, "login", "name", List.of(ScoreRole.DEVELOPER));
        return new TopLevelAsbiepSummaryRecord(
                null, null, BIE, null, null, null,
                "den", "propertyTerm", "displayName", "1.0", "status",
                state, false, false, owner, null, null);
    }

    private void stubBie(ScoreUser requester, TopLevelAsbiepSummaryRecord summary) {
        TopLevelAsbiepQueryRepository topLevelAsbiepQuery = mock(TopLevelAsbiepQueryRepository.class);
        when(repositoryFactory.topLevelAsbiepQueryRepository(requester)).thenReturn(topLevelAsbiepQuery);
        when(topLevelAsbiepQuery.getTopLevelAsbiepSummary(BIE)).thenReturn(summary);
    }

    private BusinessContextCommandRepository stubWriteRepos(ScoreUser requester) {
        BusinessContextQueryRepository query = mock(BusinessContextQueryRepository.class);
        BusinessContextCommandRepository command = mock(BusinessContextCommandRepository.class);
        lenient().when(repositoryFactory.businessContextQueryRepository(requester)).thenReturn(query);
        lenient().when(repositoryFactory.businessContextCommandRepository(requester)).thenReturn(command);
        lenient().when(query.isBusinessContextAssigned(any(), any())).thenReturn(false);
        return command;
    }

    @Test
    void assign_nonOwnerOnWip_isForbidden_andDoesNotWrite() {
        ScoreUser requester = user(OTHER, ScoreRole.END_USER);
        stubBie(requester, bie(OWNER, BieState.WIP));
        BusinessContextCommandRepository command = stubWriteRepos(requester);

        assertThatThrownBy(() -> service.assignBusinessContext(requester, BIZ_CTX, BIE))
                .isInstanceOf(DataAccessForbiddenException.class);

        verify(command, never()).createAssignment(any(), any());
    }

    @Test
    void assign_ownerOnWip_writes() {
        ScoreUser requester = user(OWNER, ScoreRole.END_USER);
        stubBie(requester, bie(OWNER, BieState.WIP));
        BusinessContextCommandRepository command = stubWriteRepos(requester);

        service.assignBusinessContext(requester, BIZ_CTX, BIE);

        verify(command).createAssignment(BIZ_CTX, BIE);
    }

    @Test
    void assign_administratorOnWip_writes() {
        ScoreUser requester = user(OTHER, ScoreRole.ADMINISTRATOR);
        // Administrator short-circuits before the BIE is even fetched.
        BusinessContextCommandRepository command = stubWriteRepos(requester);

        service.assignBusinessContext(requester, BIZ_CTX, BIE);

        verify(command).createAssignment(BIZ_CTX, BIE);
    }

    @Test
    void assign_nonOwnerOnQa_writes_notWipScoped() {
        ScoreUser requester = user(OTHER, ScoreRole.END_USER);
        stubBie(requester, bie(OWNER, BieState.QA));
        BusinessContextCommandRepository command = stubWriteRepos(requester);

        service.assignBusinessContext(requester, BIZ_CTX, BIE);

        verify(command).createAssignment(BIZ_CTX, BIE);
    }
}
