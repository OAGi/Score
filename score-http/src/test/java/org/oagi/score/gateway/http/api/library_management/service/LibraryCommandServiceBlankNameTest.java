package org.oagi.score.gateway.http.api.library_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.library_management.controller.payload.CreateLibraryRequest;
import org.oagi.score.gateway.http.api.library_management.controller.payload.UpdateLibraryRequest;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryCommandRepository;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LibraryCommandServiceBlankNameTest {

    private static final LibraryId LIBRARY = new LibraryId(BigInteger.ONE);

    private final ScoreUser requester =
            new ScoreUser(new UserId(BigInteger.ONE), "admin", "Admin", null, false, List.of(ScoreRole.ADMINISTRATOR));
    private final LibraryQueryRepository query = mock(LibraryQueryRepository.class);
    private final LibraryCommandRepository command = mock(LibraryCommandRepository.class);
    private LibraryCommandService service;

    @BeforeEach
    void setUp() {
        when(query.exists(LIBRARY)).thenReturn(true);
        RepositoryFactory repositories = mock(RepositoryFactory.class);
        when(repositories.libraryQueryRepository(any())).thenReturn(query);
        when(repositories.libraryCommandRepository(any())).thenReturn(command);
        service = new LibraryCommandService();
        ReflectionTestUtils.setField(service, "repositoryFactory", repositories);
    }

    @Test
    void rejectsACreateWithAWhitespaceOnlyName() {
        for (String name : new String[]{null, "", "   ", "	 "}) {
            assertThrows(IllegalArgumentException.class, () -> service.create(requester,
                    new CreateLibraryRequest("t", name, "org", null, "domain", null, "urn:blank", null)));
        }
        verifyNoInteractions(command);
    }

    @Test
    void rejectsAnUpdateWithAWhitespaceOnlyName() {
        assertThrows(IllegalArgumentException.class, () -> service.update(requester,
                new UpdateLibraryRequest(LIBRARY, "t", "   ", "org", null, "domain", null, null, null)));
        verify(command, never()).update(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void savesTheTrimmedNameOnUpdate() {
        service.update(requester, new UpdateLibraryRequest(LIBRARY, "t", "  Name ", "org", null, "domain", null, null, null));
        verify(query).hasDuplicateNameExcludingCurrent(LIBRARY, "Name");
        verify(command).update(LIBRARY, "t", "Name", "org", null, null, "domain", null, null);
    }

    @Test
    void rejectsACreateOrUpdateWithABlankType() {
        for (String type : new String[]{null, "", "   "}) {
            assertThrows(IllegalArgumentException.class, () -> service.create(requester,
                    new CreateLibraryRequest(type, "Name", "org", null, "domain", null, "urn:blank", null)));
            assertThrows(IllegalArgumentException.class, () -> service.update(requester,
                    new UpdateLibraryRequest(LIBRARY, type, "Name", "org", null, "domain", null, null, null)));
        }
        verifyNoInteractions(command);
    }
}
