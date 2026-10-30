package org.oagi.score.gateway.http.api.module_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.module_management.controller.payload.CreateModuleSetRequest;
import org.oagi.score.gateway.http.api.module_management.controller.payload.UpdateModuleSetRequest;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ModuleSetCommandServiceBlankNameTest {

    private static final ModuleSetId SET = new ModuleSetId(BigInteger.ONE);
    private static final LibraryId LIBRARY = new LibraryId(BigInteger.ONE);

    private final ScoreUser requester =
            new ScoreUser(new UserId(BigInteger.ONE), "dev", "Dev", null, false, List.of(ScoreRole.DEVELOPER));
    private final ModuleSetCommandRepository command = mock(ModuleSetCommandRepository.class);
    private ModuleSetCommandService service;

    @BeforeEach
    void setUp() {
        RepositoryFactory repositories = mock(RepositoryFactory.class);
        when(repositories.moduleSetCommandRepository(any())).thenReturn(command);
        service = new ModuleSetCommandService();
        ReflectionTestUtils.setField(service, "repositoryFactory", repositories);
    }

    @Test
    void rejectsACreateWithAWhitespaceOnlyName() {
        for (String name : new String[]{null, "", "   ", "	 "}) {
            assertThrows(IllegalArgumentException.class, () -> service.create(requester,
                    new CreateModuleSetRequest(LIBRARY, name, null, false, null, null)));
        }
        verifyNoInteractions(command);
    }

    @Test
    void rejectsAnUpdateWithAWhitespaceOnlyName() {
        assertThrows(IllegalArgumentException.class, () -> service.update(requester,
                new UpdateModuleSetRequest(SET, "   ", null)));
        verifyNoInteractions(command);
    }

    @Test
    void savesTheTrimmedNameOnUpdate() {
        service.update(requester, new UpdateModuleSetRequest(SET, "  Name ", "d"));
        verify(command).update(SET, "Name", "d");
    }
}
