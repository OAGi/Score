package org.oagi.score.gateway.http.api.module_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.module_management.controller.payload.CopyModuleSetModuleRequest;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSummaryRecord;
import org.oagi.score.gateway.http.api.module_management.model.ModuleType;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleCommandRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ModuleSetModuleCopyIntoDescendantTest {

    private static final ModuleSetId SET = new ModuleSetId(BigInteger.ONE);
    private static final ModuleId ROOT = new ModuleId(BigInteger.valueOf(1));
    private static final ModuleId D = new ModuleId(BigInteger.valueOf(2));
    private static final ModuleId SUB = new ModuleId(BigInteger.valueOf(3));
    private static final ModuleId OTHER = new ModuleId(BigInteger.valueOf(4));

    private final ScoreUser requester =
            new ScoreUser(new UserId(BigInteger.ONE), "dev", "Dev", null, false, List.of(ScoreRole.DEVELOPER));
    private final ModuleCommandRepository command = mock(ModuleCommandRepository.class);
    private ModuleSetModuleCommandService service;

    private static ModuleSummaryRecord module(ModuleId id, ModuleId parent, String name) {
        return new ModuleSummaryRecord(id, SET, parent, ModuleType.DIRECTORY, name, null, null, name, null);
    }

    @BeforeEach
    void setUp() {
        ModuleQueryRepository query = mock(ModuleQueryRepository.class);
        when(query.getModule(ROOT)).thenReturn(module(ROOT, null, ""));
        when(query.getModule(D)).thenReturn(module(D, ROOT, "D"));
        when(query.getModule(SUB)).thenReturn(module(SUB, D, "Sub"));
        when(query.getModule(OTHER)).thenReturn(module(OTHER, ROOT, "Other"));
        RepositoryFactory repositories = mock(RepositoryFactory.class);
        when(repositories.moduleQueryRepository(any())).thenReturn(query);
        when(repositories.moduleCommandRepository(any())).thenReturn(command);
        service = new ModuleSetModuleCommandService();
        ReflectionTestUtils.setField(service, "repositoryFactory", repositories);
    }

    @Test
    void rejectsCopyIntoTheModuleItself() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.copyModule(requester, new CopyModuleSetModuleRequest(SET, D, D, true)));
        assertEquals("Cannot copy 'D' into 'D': it cannot be copied into itself."
                + " Choose a destination outside of the module being copied.", ex.getMessage());
        verifyNoInteractions(command);
    }

    @Test
    void rejectsCopyIntoADescendant() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.copyModule(requester, new CopyModuleSetModuleRequest(SET, SUB, D, true)));
        assertEquals("Cannot copy 'D' into 'Sub': the destination 'Sub' is inside it."
                + " Choose a destination outside of the module being copied.", ex.getMessage());
        verifyNoInteractions(command);
    }
}
