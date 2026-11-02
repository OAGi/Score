package org.oagi.score.gateway.http.api.account_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.OAuth2UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class PendingListServiceTest {

    private final RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
    private final PendingListService service = new PendingListService();

    PendingListServiceTest() {
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
    }

    private ScoreUser nonAdmin() {
        return new ScoreUser(new UserId(BigInteger.TWO), "user", "User", null, false, List.of(ScoreRole.END_USER));
    }

    @Test
    void getPendingRejectsNonAdministrator() {
        assertThatThrownBy(() -> service.getPending(nonAdmin(), new OAuth2UserId(BigInteger.ONE)))
                .isInstanceOf(InsufficientAuthenticationException.class);
        verifyNoInteractions(repositoryFactory);
    }

    @Test
    void deletePendingRejectsNonAdministrator() {
        assertThatThrownBy(() -> service.deletePending(nonAdmin(), new OAuth2UserId(BigInteger.ONE)))
                .isInstanceOf(InsufficientAuthenticationException.class);
        verifyNoInteractions(repositoryFactory);
    }

    @Test
    void linkPendingToAppUserRejectsNonAdministrator() {
        assertThatThrownBy(() -> service.linkPendingToAppUser(nonAdmin(), new OAuth2UserId(BigInteger.ONE), new UserId(BigInteger.ONE)))
                .isInstanceOf(InsufficientAuthenticationException.class);
        verifyNoInteractions(repositoryFactory);
    }
}
