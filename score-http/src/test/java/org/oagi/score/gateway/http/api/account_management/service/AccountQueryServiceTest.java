package org.oagi.score.gateway.http.api.account_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.AccountDetailsRecord;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.repository.AccountQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountQueryServiceTest {

    private final RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
    private final AccountQueryRepository query = mock(AccountQueryRepository.class);
    private final AccountQueryService service = new AccountQueryService();
    private final ScoreUser requester =
            new ScoreUser(new UserId(BigInteger.TWO), "user", "User", null, false, List.of(ScoreRole.END_USER));

    AccountQueryServiceTest() {
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        when(repositoryFactory.accountQueryRepository(requester)).thenReturn(query);
    }

    private static AccountDetailsRecord accountDetails(UserId userId, String loginId) {
        return new AccountDetailsRecord(userId, loginId, "name", "org", "email@example.com",
                false, false, true, true, false,
                null, null, null, null, null, null, null, null);
    }

    @Test
    void getAccountDetailsFallsBackToNumericLoginId() {
        AccountDetailsRecord byLoginId = accountDetails(new UserId(BigInteger.valueOf(7)), "1001");
        when(query.getAccountDetails(new UserId(BigInteger.valueOf(1001)))).thenReturn(null);
        when(query.getAccountDetailsByLoginId("1001")).thenReturn(byLoginId);

        assertThat(service.getAccountDetails(requester, "1001")).isSameAs(byLoginId);
    }

    @Test
    void getAccountDetailsPrefersUserIdOverLoginId() {
        AccountDetailsRecord byUserId = accountDetails(new UserId(BigInteger.valueOf(1001)), "someone");
        when(query.getAccountDetails(new UserId(BigInteger.valueOf(1001)))).thenReturn(byUserId);

        assertThat(service.getAccountDetails(requester, "1001")).isSameAs(byUserId);
    }

    @Test
    void getAccountDetailsByLoginIdDoesNotTreatNumericLoginIdAsUserId() {
        AccountDetailsRecord byLoginId = accountDetails(new UserId(BigInteger.valueOf(7)), "1001");
        when(query.getAccountDetailsByLoginId("1001")).thenReturn(byLoginId);

        assertThat(service.getAccountDetailsByLoginId(requester, "1001")).isSameAs(byLoginId);
        verify(query).getAccountDetailsByLoginId("1001");
    }
}
