package org.oagi.score.gateway.http.api.module_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.module_management.controller.payload.CreateModuleSetReleaseRequest;
import org.oagi.score.gateway.http.api.module_management.controller.payload.UpdateModuleSetReleaseRequest;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseDetailsRecord;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetReleaseCommandRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetReleaseQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ModuleSetReleaseCommandServiceBlankNameTest {

    private static final ModuleSetId SET = new ModuleSetId(BigInteger.ONE);
    private static final ModuleSetReleaseId MSR = new ModuleSetReleaseId(BigInteger.TEN);
    private static final String DEFAULT_NAME = "Set Module Set Release";

    private final ScoreUser requester =
            new ScoreUser(new UserId(BigInteger.ONE), "dev", "Dev", null, false, List.of(ScoreRole.DEVELOPER));
    private final ModuleSetReleaseCommandRepository command = mock(ModuleSetReleaseCommandRepository.class);
    private final ModuleSetReleaseQueryRepository query = mock(ModuleSetReleaseQueryRepository.class);
    private ModuleSetReleaseCommandService service;

    @BeforeEach
    void setUp() {
        RepositoryFactory repositories = mock(RepositoryFactory.class);
        when(repositories.moduleSetReleaseCommandRepository(any())).thenReturn(command);
        when(repositories.moduleSetReleaseQueryRepository(any())).thenReturn(query);
        when(query.getDefaultName(SET)).thenReturn(DEFAULT_NAME);

        ModuleSetReleaseDetailsRecord details = mock(ModuleSetReleaseDetailsRecord.class, RETURNS_DEEP_STUBS);
        when(details.moduleSet().moduleSetId()).thenReturn(SET);
        when(query.getModuleSetReleaseDetails(MSR)).thenReturn(details);

        service = new ModuleSetReleaseCommandService();
        ReflectionTestUtils.setField(service, "repositoryFactory", repositories);
    }

    @Test
    void createFallsBackToTheDefaultNameForABlankName() {
        for (String name : new String[]{null, "", "   ", "\t "}) {
            service.create(requester, new CreateModuleSetReleaseRequest(SET, null, null, name, "d", false));
        }
        verify(command, times(4)).create(SET, null, DEFAULT_NAME, "d", false);
    }

    @Test
    void createSavesTheTrimmedName() {
        service.create(requester, new CreateModuleSetReleaseRequest(SET, null, null, "  Name ", "d", false));
        verify(command).create(SET, null, "Name", "d", false);
    }

    @Test
    void updateFallsBackToTheDefaultNameForABlankName() {
        for (String name : new String[]{null, "", "   ", "\t "}) {
            service.update(requester, new UpdateModuleSetReleaseRequest(MSR, name, "d", false));
        }
        verify(command, times(4)).update(MSR, DEFAULT_NAME, "d", false);
    }

    @Test
    void updateSavesTheTrimmedName() {
        service.update(requester, new UpdateModuleSetReleaseRequest(MSR, "  Name ", "d", false));
        verify(command).update(MSR, "Name", "d", false);
    }
}
