package org.oagi.score.gateway.http.api.account_management.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oagi.score.gateway.http.api.account_management.controller.payload.UpdateAccountRequest;
import org.oagi.score.gateway.http.api.account_management.model.AccountDetailsRecord;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.repository.AccountCommandRepository;
import org.oagi.score.gateway.http.api.account_management.repository.AccountQueryRepository;
import org.oagi.score.gateway.http.api.message_management.repository.MessageCommandRepository;
import org.oagi.score.gateway.http.api.passkey_management.repository.PasskeyCommandRepository;
import org.oagi.score.gateway.http.common.model.AccessControlException;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

    @Test
    void discardRemovesThePasskeyDataBeforeTheAccount() {
        AccountCommandService service = new AccountCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AccountCommandRepository command = mock(AccountCommandRepository.class);
        PasskeyCommandRepository passkeyCommand = mock(PasskeyCommandRepository.class);
        MessageCommandRepository messageCommand = mock(MessageCommandRepository.class);
        UserId target = new UserId(BigInteger.TEN);
        ScoreUser admin = new ScoreUser(new UserId(BigInteger.ONE), "admin", "Admin", null, true,
                List.of(ScoreRole.ADMINISTRATOR));

        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
        when(repositoryFactory.accountCommandRepository(admin, passwordEncoder)).thenReturn(command);
        when(repositoryFactory.passkeyCommandRepository(admin)).thenReturn(passkeyCommand);
        when(repositoryFactory.messageCommandRepository(admin)).thenReturn(messageCommand);
        when(command.delete(target)).thenReturn(true);

        assertThat(service.discard(admin, target)).isTrue();

        // app_webauthn_user refers to app_user, so deleting the account first would fail on the foreign key
        InOrder inOrder = inOrder(passkeyCommand, messageCommand, command);
        inOrder.verify(passkeyCommand).deletePasskeyDataByUserId(target);
        inOrder.verify(messageCommand).deleteMessagesReceivedBy(target);
        inOrder.verify(command).delete(target);
    }

    @Test
    void setEnableByNonAdminDeveloperOnAdminTargetThrowsAccessControlException() {
        AccountCommandService service = new AccountCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AccountQueryRepository query = mock(AccountQueryRepository.class);
        AccountCommandRepository command = mock(AccountCommandRepository.class);
        UserId target = new UserId(BigInteger.TEN);
        ScoreUser developer = new ScoreUser(new UserId(BigInteger.ONE), "developer", "Developer", null, false,
                List.of(ScoreRole.DEVELOPER));
        AccountDetailsRecord targetAccountDetails = accountDetails(target, true, true);

        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
        when(repositoryFactory.accountQueryRepository(developer)).thenReturn(query);
        when(query.getAccountDetails(target)).thenReturn(targetAccountDetails);

        assertThatThrownBy(() -> service.setEnable(developer, target, false))
                .isInstanceOf(AccessControlException.class);

        verify(repositoryFactory, never()).accountCommandRepository(developer, passwordEncoder);
    }

    @Test
    void setEnableByAdministratorOnAdminTargetSucceeds() {
        AccountCommandService service = new AccountCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AccountQueryRepository query = mock(AccountQueryRepository.class);
        AccountCommandRepository command = mock(AccountCommandRepository.class);
        UserId target = new UserId(BigInteger.TEN);
        ScoreUser admin = new ScoreUser(new UserId(BigInteger.ONE), "admin", "Admin", null, false,
                List.of(ScoreRole.DEVELOPER, ScoreRole.ADMINISTRATOR));
        AccountDetailsRecord targetAccountDetails = accountDetails(target, true, true);
        SessionService sessionService = mock(SessionService.class);

        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(service, "sessionService", sessionService);
        when(repositoryFactory.accountQueryRepository(admin)).thenReturn(query);
        when(query.getAccountDetails(target)).thenReturn(targetAccountDetails);
        when(repositoryFactory.accountCommandRepository(admin, passwordEncoder)).thenReturn(command);

        service.setEnable(admin, target, false);

        verify(command).setEnabled(target, false);
    }

    @Test
    void setEnableByNonAdminDeveloperOnNonAdminTargetSucceeds() {
        AccountCommandService service = new AccountCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AccountQueryRepository query = mock(AccountQueryRepository.class);
        AccountCommandRepository command = mock(AccountCommandRepository.class);
        UserId target = new UserId(BigInteger.TEN);
        ScoreUser developer = new ScoreUser(new UserId(BigInteger.ONE), "developer", "Developer", null, false,
                List.of(ScoreRole.DEVELOPER));
        AccountDetailsRecord targetAccountDetails = accountDetails(target, true, false);
        SessionService sessionService = mock(SessionService.class);

        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(service, "sessionService", sessionService);
        when(repositoryFactory.accountQueryRepository(developer)).thenReturn(query);
        when(query.getAccountDetails(target)).thenReturn(targetAccountDetails);
        when(repositoryFactory.accountCommandRepository(developer, passwordEncoder)).thenReturn(command);

        service.setEnable(developer, target, false);

        verify(command).setEnabled(target, false);
    }

    private static AccountDetailsRecord accountDetails(UserId userId, boolean enabled, boolean admin) {
        return new AccountDetailsRecord(userId, "login", "name", "org", "email@example.com",
                false, admin, enabled, true, false,
                null, null, null, null, null, null, null, null);
    }

}
