package org.oagi.score.gateway.http.api.account_management.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oagi.score.gateway.http.api.account_management.controller.payload.UpdateAccountRequest;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.repository.AccountCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountCommandServiceTest {

    @Test
    void updateByNonAdminDoesNotWriteAdminFlag() {
        AccountCommandService service = new AccountCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AccountCommandRepository command = mock(AccountCommandRepository.class);
        UserId userId = new UserId(BigInteger.ONE);
        ScoreUser requester = new ScoreUser(userId, "user", "User", null, false, List.of(ScoreRole.END_USER));
        UpdateAccountRequest request = new UpdateAccountRequest(
                userId, "user", true, "User", "Org", null);

        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
        when(repositoryFactory.accountCommandRepository(requester, passwordEncoder)).thenReturn(command);

        service.update(requester, request);

        ArgumentCaptor<Boolean> adminCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(command).update(eq(userId), eq("user"), eq("User"), eq("Org"), adminCaptor.capture(), eq(null));
        assertThat(adminCaptor.getValue()).isNull();
    }

    @Test
    void updateByAdminCanWriteAdminFlag() {
        AccountCommandService service = new AccountCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AccountCommandRepository command = mock(AccountCommandRepository.class);
        UserId userId = new UserId(BigInteger.ONE);
        ScoreUser requester = new ScoreUser(
                userId, "admin", "Admin", null, false, List.of(ScoreRole.END_USER, ScoreRole.ADMINISTRATOR));
        UpdateAccountRequest request = new UpdateAccountRequest(
                userId, "user", true, "User", "Org", null);

        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
        when(repositoryFactory.accountCommandRepository(requester, passwordEncoder)).thenReturn(command);

        service.update(requester, request);

        ArgumentCaptor<Boolean> adminCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(command).update(eq(userId), eq("user"), eq("User"), eq("Org"), adminCaptor.capture(), eq(null));
        assertThat(adminCaptor.getValue()).isTrue();
    }
}
