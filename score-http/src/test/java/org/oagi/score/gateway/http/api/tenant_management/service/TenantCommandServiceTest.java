package org.oagi.score.gateway.http.api.tenant_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.api.tenant_management.repository.TenantCommandRepository;
import org.oagi.score.gateway.http.common.model.AccessControlException;
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
 * Tenant write operations must require the Administrator role; a non-administrator requester
 * must be rejected before any repository call is made (GitHub issue #13).
 */
class TenantCommandServiceTest {

    private TenantCommandService service;
    private RepositoryFactory repositoryFactory;
    private TenantCommandRepository command;

    private static final TenantId TENANT_ID = new TenantId(BigInteger.valueOf(1));
    private static final UserId USER_ID = new UserId(BigInteger.valueOf(2));
    private static final BusinessContextId BIZ_CTX_ID = new BusinessContextId(BigInteger.valueOf(3));

    private ScoreUser endUser;
    private ScoreUser administrator;

    @BeforeEach
    void setUp() {
        service = new TenantCommandService();
        repositoryFactory = mock(RepositoryFactory.class);
        command = mock(TenantCommandRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        endUser = new ScoreUser(new UserId(BigInteger.valueOf(42)), "enduser", "End User",
                "enduser@example.com", true, List.of(ScoreRole.END_USER));
        administrator = new ScoreUser(new UserId(BigInteger.valueOf(43)), "admin", "Admin",
                "admin@example.com", true, List.of(ScoreRole.ADMINISTRATOR));

        when(repositoryFactory.tenantCommandRepository(any())).thenReturn(command);
    }

    @Test
    void createTenant_rejects_a_non_administrator() {
        assertThrows(AccessControlException.class, () -> service.createTenant(endUser, "Acme"));
        verify(command, never()).createTenant(any());
    }

    @Test
    void createTenant_allows_an_administrator() {
        service.createTenant(administrator, "Acme");
        verify(command).createTenant("Acme");
    }

    @Test
    void createTenant_drops_the_spaces_around_the_name() {
        service.createTenant(administrator, " Acme ");
        verify(command).createTenant("Acme");
    }

    @Test
    void updateTenant_drops_the_spaces_around_the_name() {
        service.updateTenant(administrator, TENANT_ID, " ACME ");
        verify(command).updateTenant(TENANT_ID, "ACME");
    }

    @Test
    void updateTenant_rejects_a_non_administrator() {
        assertThrows(AccessControlException.class, () -> service.updateTenant(endUser, TENANT_ID, "Acme"));
        verify(command, never()).updateTenant(any(), any());
    }

    @Test
    void deleteTenant_rejects_a_non_administrator() {
        assertThrows(AccessControlException.class, () -> service.deleteTenant(endUser, TENANT_ID));
        verify(command, never()).deleteTenant(any());
    }

    @Test
    void addUserToTenant_rejects_a_non_administrator() {
        assertThrows(AccessControlException.class, () -> service.addUserToTenant(endUser, TENANT_ID, USER_ID));
        verify(command, never()).addUserToTenant(any(), any());
    }

    @Test
    void deleteTenantUser_rejects_a_non_administrator() {
        assertThrows(AccessControlException.class, () -> service.deleteTenantUser(endUser, TENANT_ID, USER_ID));
        verify(command, never()).deleteTenantUser(any(), any());
    }

    @Test
    void addBusinessCtxToTenant_rejects_a_non_administrator() {
        assertThrows(AccessControlException.class,
                () -> service.addBusinessCtxToTenant(endUser, TENANT_ID, BIZ_CTX_ID));
        verify(command, never()).addBusinessCtxToTenant(any(), any());
    }

    @Test
    void deleteTenantBusinessCtx_rejects_a_non_administrator() {
        assertThrows(AccessControlException.class,
                () -> service.deleteTenantBusinessCtx(endUser, TENANT_ID, BIZ_CTX_ID));
        verify(command, never()).deleteTenantBusinessCtx(any(), any());
    }
}
