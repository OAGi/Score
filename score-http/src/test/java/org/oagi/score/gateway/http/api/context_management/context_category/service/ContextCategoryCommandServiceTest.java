package org.oagi.score.gateway.http.api.context_management.context_category.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.context_management.context_category.controller.payload.CreateContextCategoryRequest;
import org.oagi.score.gateway.http.api.context_management.context_category.controller.payload.UpdateContextCategoryRequest;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_category.repository.ContextCategoryCommandRepository;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.ContextSchemeQueryRepository;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Context Category audit guards: whitespace-only name rejection (B8) and the in-use discard guard now
 * surfaced as a 4xx-mapped {@link IllegalArgumentException} (previously {@code IllegalStateException} -> 500).
 */
class ContextCategoryCommandServiceTest {

    private ContextCategoryCommandService service;
    private RepositoryFactory repositoryFactory;
    private ContextCategoryCommandRepository command;
    private ContextSchemeQueryRepository schemeQuery;

    private static final ContextCategoryId CATEGORY_ID = new ContextCategoryId(BigInteger.valueOf(3));

    private ScoreUser requester;

    @BeforeEach
    void setUp() {
        service = new ContextCategoryCommandService();
        repositoryFactory = mock(RepositoryFactory.class);
        command = mock(ContextCategoryCommandRepository.class);
        schemeQuery = mock(ContextSchemeQueryRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
                "tester@example.com", true, List.of(ScoreRole.DEVELOPER));

        when(repositoryFactory.contextCategoryCommandRepository(any())).thenReturn(command);
        when(repositoryFactory.contextSchemeQueryRepository(any())).thenReturn(schemeQuery);
    }

    private ContextSchemeSummaryRecord aSchemeSummary() {
        return new ContextSchemeSummaryRecord(null, new Guid("0123456789abcdef0123456789abcdef"),
                null, "GEO", "Geopolitical", "OAGi", "1.0", "desc");
    }

    // ----- name validation (B8) -----

    @Test
    void create_rejects_a_whitespace_only_name() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create(requester, new CreateContextCategoryRequest("   ", "desc")));
        verify(command, never()).create(any(), any());
    }

    @Test
    void create_persists_a_valid_name() {
        when(command.create(any(), any())).thenReturn(CATEGORY_ID);

        service.create(requester, new CreateContextCategoryRequest("Geography", "desc"));

        verify(command).create("Geography", "desc");
    }

    @Test
    void update_rejects_a_whitespace_only_name() {
        assertThrows(IllegalArgumentException.class, () -> service.update(requester,
                new UpdateContextCategoryRequest(CATEGORY_ID, "  ", "desc")));
        verify(command, never()).update(any(), any(), any());
    }

    // ----- discard in-use guard -----

    @Test
    void discard_rejects_a_category_that_still_has_schemes() {
        when(schemeQuery.getContextSchemeSummaryList(CATEGORY_ID)).thenReturn(List.of(aSchemeSummary()));

        assertThrows(IllegalArgumentException.class, () -> service.discard(requester, CATEGORY_ID));
        verify(command, never()).delete(anyList());
    }

    @Test
    void discard_deletes_a_category_with_no_schemes() {
        when(schemeQuery.getContextSchemeSummaryList(CATEGORY_ID)).thenReturn(List.of());
        when(command.delete(anyList())).thenReturn(1);

        assertTrue(service.discard(requester, CATEGORY_ID));
        verify(command).delete(List.of(CATEGORY_ID));
    }
}
