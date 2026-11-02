package org.oagi.score.gateway.http.api.module_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.module_management.controller.payload.*;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class ModuleManagementDeveloperRoleTest {

    private final RepositoryFactory repositories = mock(RepositoryFactory.class);
    private final ScoreUser endUser =
            new ScoreUser(new UserId(BigInteger.ONE), "user", "User", null, false, List.of(ScoreRole.END_USER));

    private <T> T service(T service) {
        ReflectionTestUtils.setField(service, "repositoryFactory", repositories);
        return service;
    }

    @Test
    void moduleSetWritesRequireTheDeveloperRole() {
        ModuleSetCommandService service = service(new ModuleSetCommandService());

        assertThatThrownBy(() -> service.create(endUser, mock(CreateModuleSetRequest.class)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.update(endUser, mock(UpdateModuleSetRequest.class)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.discard(endUser, new ModuleSetId(BigInteger.ONE)))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repositories);
    }

    @Test
    void moduleWritesRequireTheDeveloperRole() {
        ModuleSetModuleCommandService service = service(new ModuleSetModuleCommandService());

        assertThatThrownBy(() -> service.create(endUser, mock(CreateModuleSetModuleRequest.class)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.update(endUser, mock(UpdateModuleSetModuleRequest.class)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.discard(endUser, new ModuleId(BigInteger.ONE), new ModuleSetId(BigInteger.ONE)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.copyModule(endUser, mock(CopyModuleSetModuleRequest.class)))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repositories);
    }

    @Test
    void moduleSetReleaseWritesRequireTheDeveloperRole() {
        ModuleSetReleaseCommandService service = service(new ModuleSetReleaseCommandService());

        assertThatThrownBy(() -> service.create(endUser, mock(CreateModuleSetReleaseRequest.class)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.update(endUser, mock(UpdateModuleSetReleaseRequest.class)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.discard(endUser, new ModuleSetReleaseId(BigInteger.ONE)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.assignCc(endUser, mock(AssignCCToModuleRequest.class)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.unassignCc(endUser, mock(AssignCCToModuleRequest.class)))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repositories);
    }
}
